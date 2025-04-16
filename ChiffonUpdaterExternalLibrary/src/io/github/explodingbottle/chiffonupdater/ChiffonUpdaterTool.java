/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the only class that receives documentation as this is the only one
 * supposed to be used by a developer.<br>
 * <br>
 * 
 * When implementing in a program, you would do the following: <br>
 * 
 * <pre>
 * ChiffonUpdaterTool updaterTool = new ChiffonUpdaterTool(false);
 * if (updaterTool.initialize()) {
 * 	try {
 * 		updaterTool.registerProgram(
 * 				new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
 * 	} catch (URISyntaxException e) {
 * 		// You can choose to let this like this or handle the error your way.
 * 	}
 * }
 * </pre>
 * 
 * 
 * @author ExplodingBottle
 *
 */
public class ChiffonUpdaterTool {

	private OperatingSystemDetector detector;
	private SharedLogger logger;
	private UpdaterPathProvider pathProvider;
	private boolean isInitialized;
	private ProductsListManager plManager;
	private File updaterFolder;

	private static final String HARDCODED_PROPERTIES_FILE_NAME = "provider.properties";
	private static final String HARDCODED_LICENSE_FILE_NAME = "mit_license.txt";

	/**
	 * This function returns the path under which you can find the license this
	 * external library is licensed under.
	 * 
	 * @return The path relative to the root of the JAR file.
	 */
	public static String getLicenseFileName() {
		return HARDCODED_LICENSE_FILE_NAME;
	}

	/**
	 * This constructor is the only one that you should use.
	 * 
	 * @param canPrintOnSystemOut Whether the log can be displayed on the standard
	 *                            output or not.
	 */
	public ChiffonUpdaterTool(boolean canPrintOnSystemOut) {
		detector = new OperatingSystemDetector();
		logger = new SharedLogger(null, canPrintOnSystemOut);
		pathProvider = new UpdaterPathProvider(detector, logger);
	}

	/**
	 * Allows you to check if the tool has been already initialized.
	 * 
	 * @return Is the tool initialized.
	 */
	public boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * This function will initialize the tool by preparing everything for use. <br>
	 * This function will return if the initialization has been done successfully or
	 * not. <br>
	 * 
	 * <i>NOTE: If the initialization fails, consider not using other functions as
	 * they will throw a {@code IllegalStateException}.</i>
	 * 
	 * @return
	 */
	public boolean initialize() {
		if (isInitialized) {
			throw new IllegalStateException("The tool has already been initialized.");
		}
		if (this.getClass().getClassLoader().getResource(HARDCODED_LICENSE_FILE_NAME) == null) {
			logger.log("EXTLIB", LogLevel.WARNING, "License file is missing, aborting launch.");
			return false;
		}
		InputStream propertiesFile = this.getClass().getClassLoader()
				.getResourceAsStream(HARDCODED_PROPERTIES_FILE_NAME);
		if (propertiesFile == null) {
			logger.log("EXTLIB", LogLevel.WARNING, "Failed to open the properties file.");
			return false;
		}
		pathProvider.updatePathFromStream(propertiesFile);
		try {
			propertiesFile.close();
		} catch (IOException e) {
			logger.log("EXTLIB", LogLevel.WARNING, "Failed to close the opened properties file.");
		}
		updaterFolder = pathProvider.getAccessibleFolder();
		if (updaterFolder == null) {
			logger.log("EXTLIB", LogLevel.WARNING, "Path Provider signals a null directory.");
			return false;
		}
		plManager = new ProductsListManager(new File(updaterFolder, HardcodedValues.returnProductsReglistName()),
				logger);
		isInitialized = true;
		return true;
	}

	/**
	 * This function will use the internal shared code in order to compute the hash
	 * of a file. <br>
	 * The hashing algorithm might be subject to change and is determined by its
	 * effectiveness. <br>
	 * 
	 * The purpose of using this function is only in cases you need to compute the
	 * hash of a file for a use in the update system.<br>
	 * 
	 *
	 * @param fileToHash The file from which you would like to compute the hash.
	 * @return The hexadecimal representation of the hash or {@code null} if it
	 *         failed.
	 */
	public String computeHash(File fileToHash) {
		if (!isInitialized) {
			throw new IllegalStateException("The tool has not been initialized yet.");
		}
		HashComputer computer = new HashComputer(fileToHash, logger);
		return computer.computeHash();
	}

	/**
	 * Use this function each time you start your program to write to the products
	 * registry list your location and hash.<br>
	 * 
	 * 
	 * @param programPath The path where to find the main program file.
	 */
	public void registerProgram(File programPath) {
		if (!isInitialized) {
			throw new IllegalStateException("The tool has not been initialized yet.");
		}
		List<File> toAdd = new ArrayList<File>();
		toAdd.add(programPath);
		plManager.update(toAdd, null);
	}

	/**
	 * The only role of this function is to refresh the list of registered programs.
	 * Usually this function is not used.<br>
	 *
	 */
	public void updateProgramsList() {
		if (!isInitialized) {
			throw new IllegalStateException("The tool has not been initialized yet.");
		}
		plManager.update(null, null);
	}

}
