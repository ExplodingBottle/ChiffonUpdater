/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ConfigurationDownloadThread extends Thread {

	private SharedLogger logger;
	private static final String CMPN = "CDTH";

	public ConfigurationDownloadThread() {
		logger = AgentMain.getSharedLogger();
	}

	public void run() {
		logger.log(CMPN, LogLevel.INFO, "Agent is downloading the configuration");
		URI agentCfg = AgentMain.getCurrentSession().getBackendURI().resolve("config/agentcfg1.zip");
		URLConnection conn;
		try {
			conn = agentCfg.toURL().openConnection();
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to open connection");
			AgentMain.getCurrentSession().setState(AgentSessionState.UNCONFIGURED);
			return;
		}

		AgentSettings settings = null;
		try (ZipInputStream zis = new ZipInputStream(conn.getInputStream())) {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				if (entry.getName().equals("provider.properties")) {
					UpdaterPathProvider upp = new UpdaterPathProvider(AgentMain.getOperatingSystemDetector(), logger);
					upp.updatePathFromStream(zis);
					AgentMain.getCurrentSession().setPathProvider(upp);
				}
				if (entry.getName().equals("agent.properties")) {
					Properties agentProps = new Properties();
					agentProps.load(zis);
					settings = AgentSettings.createFromPropertiesFile(agentProps);
				}
				entry = zis.getNextEntry();
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to parse the configuration files");
		}
		AgentSession currentSession = AgentMain.getCurrentSession();
		if (settings == null) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to load agent settings.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		} else {
			settings.printSettings(logger);
		}
		if (currentSession.getPathProvider() == null) {
			logger.log(CMPN, LogLevel.ERROR, "Path provider not correctly configured.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}
		File accessibleFolder = currentSession.getPathProvider().getAccessibleFolder();
		logger.log(CMPN, LogLevel.INFO, "UpdaterPath: " + accessibleFolder.getAbsolutePath());

		File packageUpdFolder = new File(accessibleFolder, "PkgUpd");
		if (!packageUpdFolder.exists() && !packageUpdFolder.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to create the package update folder.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}
		logger.log(CMPN, LogLevel.INFO, "PkgUpdPath: " + packageUpdFolder.getAbsolutePath());

		File detFolder = new File(accessibleFolder, "detection");
		if (!detFolder.exists() && !detFolder.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to create the detection folder.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}
		logger.log(CMPN, LogLevel.INFO, "DetectionFolderPath: " + detFolder.getAbsolutePath());

		File localUpdatesFolder = new File(accessibleFolder, "local");
		if (!localUpdatesFolder.exists() && !localUpdatesFolder.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to create the local updates folder.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}
		logger.log(CMPN, LogLevel.INFO, "LocalUpdatesPath: " + localUpdatesFolder.getAbsolutePath());

		File customizationFolder = new File(accessibleFolder, "customization");
		if (!customizationFolder.exists() && !customizationFolder.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to create the customization informations folder.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}
		logger.log(CMPN, LogLevel.INFO, "CustomizationInformationsPath: " + customizationFolder.getAbsolutePath());

		File agentlog = new File(accessibleFolder, "agentlog.log");
		logger.log(CMPN, LogLevel.INFO, "AgentLog: " + agentlog.getAbsolutePath());
		logger.setNewDestination(agentlog);
		logger.openFileLogging(true);
		logger.log(CMPN, LogLevel.INFO, "======================= Log Start =======================");

		DetectionFilesManager dfm = new DetectionFilesManager(detFolder, AgentMain.getCurrentSession().getBackendURI());
		if (!dfm.prepareFolderStructure()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to prepare the detection folder structure.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}

		LocalUpdatesManager lum = new LocalUpdatesManager(localUpdatesFolder,
				AgentMain.getCurrentSession().getBackendURI());
		if (!lum.prepareFolderStructure()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to prepare the folder structure for the local updates manager.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}

		logger.log(CMPN, LogLevel.INFO, "Proceeding with package update");

		File targetPackageFile = new File(packageUpdFolder, "inet_package.jar");
		File targetPackageVersionsFile = new File(packageUpdFolder, "inet_package.ver");
		DownloaderClass dlclPackage, dlclVers;
		try {
			dlclPackage = new DownloaderClass(targetPackageFile, settings.getPackageDownloadURI().toURL());
			dlclVers = new DownloaderClass(targetPackageVersionsFile, settings.getPkgVersionsDownloadURI().toURL());
		} catch (MalformedURLException e) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to prepare the download of the update package.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}
		if (!dlclPackage.download() || !dlclVers.download()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to download the update package files.");
			currentSession.setState(AgentSessionState.UNCONFIGURED);
			return;
		}
		logger.log(CMPN, LogLevel.INFO, "Package files downloaded.");

		SharedStandalonePackageUpdater pu = new SharedStandalonePackageUpdater(accessibleFolder, logger);
		File updatedPackageFile = pu.updateOrReturnUpdatedFile(targetPackageFile, targetPackageVersionsFile);
		if (updatedPackageFile != null) {
			logger.log(CMPN, LogLevel.INFO, "Installed package files have been changed.");
		} else {
			logger.log(CMPN, LogLevel.INFO, "No installed files change needed.");
		}

		CatalogManager catalogManager = new CatalogManager(AgentMain.getCurrentSession().getBackendURI());

		currentSession.setDetFilesManager(dfm);
		currentSession.setLocalUpdatesManager(lum);
		currentSession.setCurrentUpdatePackage(targetPackageFile);
		currentSession.setCatalogManager(catalogManager);

		currentSession.setHistoryFileUtil(new HistoryFileUtil(new File(accessibleFolder, "history.dat")));

		CustomizationInformationsManager customizationMgr = new CustomizationInformationsManager(customizationFolder,
				AgentMain.getCurrentSession().getBackendURI(), settings);
		if (!customizationMgr.prepareCustomizationFolder()) {
			logger.log(CMPN, LogLevel.WARNING,
					"Customization preparation failed. Customization might not be available.");
		}

		currentSession.setCustomizationManager(customizationMgr);

		logger.log(CMPN, LogLevel.INFO, "Agent configured with success.");
		currentSession.setState(AgentSessionState.IDLE);
	}
}
