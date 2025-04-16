/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.ImageIcon;

public class CustomizationInformationsManager {

	private File rootCustFolder;
	private AgentSettings settings;
	private URI customizationURI;
	private SharedLogger logger;

	private ImageIcon customIcon;

	private String customAgentTitle;
	private File customAgentIconFile;
	private File customPackageIconFile;
	private File customPackageBannerFile;
	private File customPackageSettings;

	private static final String CMPN = "CSMG";

	public CustomizationInformationsManager(File rootCustFolder, URI backendURI, AgentSettings settings) {
		this.rootCustFolder = rootCustFolder;
		this.settings = settings;
		customizationURI = backendURI.resolve("config/customization.zip");
		logger = AgentMain.getSharedLogger();
	}

	private boolean dumpContentFromZip(File target, ZipInputStream in) {
		byte[] buffer = new byte[4096];
		try (FileOutputStream fos = new FileOutputStream(target)) {
			int read = in.read(buffer, 0, buffer.length);
			while (read != -1) {
				fos.write(buffer, 0, read);
				read = in.read(buffer, 0, buffer.length);
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Couldn't extract content from ZIP file to " + target.getName());
			return false;
		}
		return true;
	}

	public boolean prepareCustomizationFolder() {
		logger.log(CMPN, LogLevel.INFO, "Preparing customization folder.");
		for (File file : rootCustFolder.listFiles()) {
			if (file.isFile() && !file.delete()) {
				logger.log(CMPN, LogLevel.WARNING,
						"Customization folder clean up failed to clean file " + file.getName());
			}
		}
		if (settings.getCustomAgentIconFileName() != null || settings.getCustomPackageBannerFileName() != null
				|| settings.getCustomPackageIconFileName() != null) {
			logger.log(CMPN, LogLevel.INFO, "Download of custom icons is required.");
			URL castedURL;
			try {
				castedURL = customizationURI.toURL();
			} catch (MalformedURLException e) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to cast the customization archive URI to URL.");
				return false;
			}
			URLConnection connection;
			try {
				connection = castedURL.openConnection();
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to open a connection to get the customization archive.");
				return false;
			}
			try (ZipInputStream reader = new ZipInputStream(connection.getInputStream())) {
				ZipEntry entry = reader.getNextEntry();
				while (entry != null) {
					if (entry.getName().equalsIgnoreCase(settings.getCustomAgentIconFileName())) {
						File tempFile = new File(rootCustFolder, "agent_icon.png");
						if (!dumpContentFromZip(tempFile, reader)) {
							logger.log(CMPN, LogLevel.ERROR, "Failed to read agent icon.");
							return false;
						}
						customAgentIconFile = tempFile;
					}
					if (entry.getName().equalsIgnoreCase(settings.getCustomPackageIconFileName())) {
						File tempFile = new File(rootCustFolder, "package_icon.png");
						if (!dumpContentFromZip(tempFile, reader)) {
							logger.log(CMPN, LogLevel.ERROR, "Failed to read package icon.");
							return false;
						}
						customPackageIconFile = tempFile;
					}
					if (entry.getName().equalsIgnoreCase(settings.getCustomPackageBannerFileName())) {
						File tempFile = new File(rootCustFolder, "package_banner.png");
						if (!dumpContentFromZip(tempFile, reader)) {
							logger.log(CMPN, LogLevel.ERROR, "Failed to read package banner.");
							return false;
						}
						customPackageBannerFile = tempFile;
					}
					entry = reader.getNextEntry();

				}
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to get the content from the customization archive.");
				return false;
			}
		}
		if (settings.getCustomPackageTitle() != null || settings.getCustomPackageBannerFileName() != null
				|| settings.getCustomPackageIconFileName() != null) {
			logger.log(CMPN, LogLevel.INFO, "Generation of a custom settings file for package is required.");
			Properties props = new Properties();
			if (settings.getCustomPackageTitle() != null) {
				props.setProperty("package.customization.title", settings.getCustomPackageTitle());
			}
			if (settings.getCustomPackageIconFileName() != null) {
				props.setProperty("package.customization.icon", "package_custom_icon.png");
			}
			if (settings.getCustomPackageBannerFileName() != null) {
				props.setProperty("package.customization.banner", "package_custom_banner.png");
			}
			File temp = new File(rootCustFolder, "customization.properties");
			try (FileOutputStream fos = new FileOutputStream(temp)) {
				props.store(fos, "Written by the agent to allow package customization.");
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to write a custom settings file for the package.");
				return false;
			}
			customPackageSettings = temp;
		}

		if (customAgentIconFile != null) {
			try {
				customIcon = new ImageIcon(customAgentIconFile.toURI().toURL());
				AgentMain.getAgentPresence().setTrayIcon(customIcon.getImage());
			} catch (MalformedURLException e) {
				logger.log(CMPN, LogLevel.WARNING, "Failed to load the custom icon for the agent.");
			}
		}
		customAgentTitle = settings.getCustomAgentTitle();
		if (customAgentTitle != null) {
			AgentMain.getAgentPresence().updateAgentTitleUsage(customAgentTitle);
		}

		return true;
	}

	public String getCustomAgentTitle() {
		return customAgentTitle;
	}

	public boolean copyCustomizationsToExtractedFolder(File target) {
		try {
			if (customPackageIconFile != null) {
				Files.copy(customPackageIconFile.toPath(), new File(target, "package_custom_icon.png").toPath());
			}
			if (customPackageBannerFile != null) {
				Files.copy(customPackageBannerFile.toPath(), new File(target, "package_custom_banner.png").toPath());
			}
			if (customPackageSettings != null) {
				Files.copy(customPackageSettings.toPath(), new File(target, "customization.properties").toPath());
			}

		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to copy customizations due to IOException.");
			return false;
		}
		return true;
	}

	public ImageIcon getIconToUse() {
		return customIcon != null ? customIcon : AgentMain.getAgentIcon();
	}

}
