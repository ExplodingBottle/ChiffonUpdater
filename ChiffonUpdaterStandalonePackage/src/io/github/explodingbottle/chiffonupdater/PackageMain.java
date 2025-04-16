/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class PackageMain {

	private static UpdaterState updaterState;
	private static ImageResourcesLoader irl;
	private static Translator translator;
	private static String licenseText;
	private static PackageCustomizationEngine customizationEngine;

	public static UpdaterState returnUpdaterState() {
		return updaterState;
	}

	public static PackageCustomizationEngine getCustomizationEngine() {
		return customizationEngine;
	}

	public static ImageResourcesLoader getImageResourcesLoader() {
		return irl;
	}

	public static String getLicenseText() {
		return licenseText;
	}

	public static Translator getTranslator() {
		return translator;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {

		}

		irl = new ImageResourcesLoader();

		File updateFolder = new File(System.getProperty("user.dir"));
		String potentialOverridenUpdateDir = System.getProperty("cupackage.updatedir");
		String potentialOverridenProductDir = System.getProperty("cupackage.productdir");
		String noConsoleSwitch = System.getProperty("cupackage.noconsole");
		String noGraphicsSwitch = System.getProperty("cupackage.nographics");
		String eulaAcceptSwitch = System.getProperty("cupackage.accepteula");
		String noVersionCheck = System.getProperty("cupackage.noverscheck");
		String uninstVers = System.getProperty("cupackage.uninstallversion");
		String custLog = System.getProperty("cupackage.customlog");
		String nologging = System.getProperty("cupackage.nologging");
		boolean allowGraphics = noGraphicsSwitch == null
				|| (!noGraphicsSwitch.equalsIgnoreCase("true") && !noGraphicsSwitch.equalsIgnoreCase("showeula"));
		boolean showEulaAnyways = allowGraphics || noGraphicsSwitch.equalsIgnoreCase("showeula");
		boolean headlessEulaAccepted = eulaAcceptSwitch != null && eulaAcceptSwitch.equalsIgnoreCase("true");

		boolean packageShouldVersCheck = noVersionCheck == null || !noVersionCheck.equalsIgnoreCase("true");
		boolean allowLogging = nologging == null || !nologging.equalsIgnoreCase("true");

		SharedLogger logger = new SharedLogger(null,
				noConsoleSwitch == null || !noConsoleSwitch.equalsIgnoreCase("true"));
		File customProductDir = null;
		if (potentialOverridenProductDir != null) {
			customProductDir = new File(potentialOverridenProductDir);
		}
		if (potentialOverridenUpdateDir != null) {
			updateFolder = new File(potentialOverridenUpdateDir);
		}
		OperatingSystemDetector detector = new OperatingSystemDetector();
		detector.detectOperatingSystem();

		logger.log("SUMN", LogLevel.INFO, "Detected operating system: " + detector.getCurrentOperatingSystem());
		logger.log("SUMN", LogLevel.INFO, "Global ChiffonUpdater version: " + ChiffonUpdaterVersion.getGlobalVersion());

		boolean isUninstall = false;

		File curDb = new File(updateFolder, HardcodedValues.returnProductSpecificUpdateFolderName());
		File providerFileForTests = new File(updateFolder, "provider.properties");

		if (curDb.exists() && !providerFileForTests.exists()) {
			isUninstall = true;
			logger.log("SUMN", LogLevel.INFO, "Detected an uninstall configuration.");
		}

		InputStream licenseStream = PackageMain.class.getClassLoader().getResourceAsStream("licenses/mit_license.txt");
		if (licenseStream == null) {
			logger.log("SUMN", LogLevel.ERROR, "Couldn't open license file.");
			if (allowGraphics) {
				JOptionPane.showMessageDialog(null, "Couldn't open license file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		BufferedReader licenseBufferedReader = new BufferedReader(new InputStreamReader(licenseStream));
		StringBuilder strBuilder = new StringBuilder();
		char[] cbuff = new char[4096];
		try {
			int read = licenseBufferedReader.read(cbuff, 0, cbuff.length);
			while (read != -1) {
				strBuilder.append(cbuff, 0, read);
				read = licenseBufferedReader.read(cbuff, 0, cbuff.length);
			}
			licenseBufferedReader.close();
		} catch (IOException e) {
			logger.log("SUMN", LogLevel.ERROR, "Couldn't load license text.");
			if (allowGraphics) {
				JOptionPane.showMessageDialog(null, "Couldn't load license text.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		licenseText = strBuilder.toString();

		PropertiesLoader loader = null;
		if (isUninstall) {
			loader = new PropertiesLoader(curDb, logger);
		} else {
			loader = new PropertiesLoader(updateFolder, logger);
		}
		if (!loader.loadAll()) {
			logger.log("SUMN", LogLevel.ERROR, "Failure while loading property files.");
			if (allowGraphics) {
				JOptionPane.showMessageDialog(null, "Failed to load property files.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		Properties providerProps = loader.getProperties("provider.properties");
		if (providerProps == null) {
			logger.log("SUMN", LogLevel.ERROR, "Failure while loading provider property file.");
			if (allowGraphics) {
				JOptionPane.showMessageDialog(null, "Failed to load provider property file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		UpdaterPathProvider pathProvider = new UpdaterPathProvider(detector, logger);
		pathProvider.updatePathFromProperties(providerProps);

		if (pathProvider.getAccessibleFolder() == null) {
			logger.log("SUMN", LogLevel.ERROR, "No accessible global data folder.");
			if (allowGraphics) {
				JOptionPane.showMessageDialog(null, "Failed to open the global data folder.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		File logDest = null;
		if (allowLogging) {
			if (custLog != null) {
				logDest = new File(custLog);
			} else {
				File logs = new File(pathProvider.getAccessibleFolder(), "PackageLogs");
				boolean canCreate = true;
				if (!logs.exists()) {
					if (!logs.mkdir()) {
						logger.log("SUMN", LogLevel.WARNING, "Failed to create the package logs folder.");
						canCreate = false;
					}
				}
				if (canCreate) {
					DateTimeFormatter logDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
					logDest = new File(logs, logDate.format(LocalDateTime.now()) + ".log");
				}
			}
		}
		if (logDest != null) {
			logger.setNewDestination(logDest);
			logger.openFileLogging(false);
			logger.log("SUMN", LogLevel.INFO, "Opened log file for write.");
		}

		if (packageShouldVersCheck) {

			Properties packageVersionsOld = loader.getProperties("pversions.properties");
			if (packageVersionsOld == null) {
				logger.log("SUMN", LogLevel.WARNING, "Cannot find the package versions file.");
			} else {
				SharedStandalonePackageUpdater updater = new SharedStandalonePackageUpdater(
						pathProvider.getAccessibleFolder(), logger);

				try {
					File jarFile = new File(
							PackageMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
					File upToDate = updater.updateOrReturnUpdatedFile(jarFile, packageVersionsOld);
					if (upToDate != null) {
						if (JarJumpUtil.jumpToOther(upToDate, updateFolder, potentialOverridenProductDir,
								noConsoleSwitch, noGraphicsSwitch, uninstVers, custLog, nologging, eulaAcceptSwitch)) {
							return;
						}
					}
				} catch (URISyntaxException e) {
					logger.log("SUMN", LogLevel.WARNING, "Failed to get path of current file.");
				}

			}
		}

		translator = new Translator(new ModularTranslatorConfiguration("translations/${lang}.properties"),
				PackageMain.class.getClassLoader());

		ProductsListManager manager = new ProductsListManager(
				new File(pathProvider.getAccessibleFolder(), HardcodedValues.returnProductsReglistName()), logger);

		updaterState = new UpdaterState(updateFolder, logger, detector, loader, pathProvider, manager, customProductDir,
				!allowGraphics, showEulaAnyways, headlessEulaAccepted);

		customizationEngine = new PackageCustomizationEngine(loader, translator, logger);
		customizationEngine.initEngine();

		if (isUninstall) {
			UninstallThread uninstThread = new UninstallThread(uninstVers);
			uninstThread.start();
		} else {

			UpdaterThread updaterThread = new UpdaterThread();
			updaterThread.start();
		}

	}

}
