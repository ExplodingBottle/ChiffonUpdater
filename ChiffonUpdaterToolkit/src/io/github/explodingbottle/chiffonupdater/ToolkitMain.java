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

public class ToolkitMain {

	private static File cuToolkit;
	private static GlobalLogger logger;
	private static File cuTempFolder;

	private static String mitLicenseString;

	private static PersistentStorage peristentStorage;

	public static String getMITLicenseString() {
		return mitLicenseString;
	}

	public static GlobalLogger getGlobalLogger() {
		return logger;
	}

	public static File getCuToolkitRootPath() {
		return cuToolkit;
	}

	public static File getCuToolkitTempFolder() {
		return cuTempFolder;
	}

	public static PersistentStorage getPersistentStorage() {
		return peristentStorage;
	}

	private static boolean dirCleanupForTmpFolder(File dir) {
		boolean succeed = true;

		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				succeed &= file.delete();
			}
			if (file.isDirectory()) {
				succeed &= dirCleanupForTmpFolder(file);
			}
		}
		if (!dir.equals(cuTempFolder)) {
			succeed &= dir.delete();
		}
		return succeed;
	}

	public static void main(String[] args) {
		InputStream mitLicenseStream = ToolkitMain.class.getClassLoader()
				.getResourceAsStream("licenses/mit_license.txt");
		if (mitLicenseStream == null) {
			System.err.println("License is missing");
			return;
		}
		BufferedReader rdrMit = new BufferedReader(new InputStreamReader(mitLicenseStream));
		StringBuilder strBuilder = new StringBuilder();
		char[] cbuff = new char[4096];
		try {
			int read = rdrMit.read(cbuff, 0, cbuff.length);
			while (read != -1) {
				strBuilder.append(cbuff, 0, read);
				read = rdrMit.read(cbuff, 0, cbuff.length);
			}
			rdrMit.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't load MIT License.");
			return;
		}
		mitLicenseString = strBuilder.toString();

		File mainPath = new File(System.getProperty("user.home"));
		logger = new GlobalLogger();
		if (!mainPath.exists()) {
			System.err.println("No user.home");
			return;
		}
		cuToolkit = new File(mainPath, ".cutoolkit");
		if (!cuToolkit.exists()) {
			if (!cuToolkit.mkdir()) {
				System.err.println("Couldn't create .cutoolkit");
				return;
			}
		}

		cuTempFolder = new File(cuToolkit, "temp");
		if (!cuTempFolder.exists()) {
			if (!cuTempFolder.mkdir()) {
				System.err.println("Couldn't create temp folder");
				return;
			}
		}
		if (!dirCleanupForTmpFolder(cuTempFolder)) {
			System.err.println("Couldn't clean temp folder up");
		}
		peristentStorage = new PersistentStorage();

		ToolkitWindow window = new ToolkitWindow();
		if (window.getCurrentConnectedSource() == null
				&& !"true".equals(peristentStorage.getSettings().getProperty("source.warning.hide"))) {
			SourceConnectionWarning warning = new SourceConnectionWarning(window);
			warning.setVisible(true);
		}
		window.setVisible(true);
	}

}
