/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.binpack;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import io.github.explodingbottle.chiffonupdater.GlobalLogger;
import io.github.explodingbottle.chiffonupdater.ToolkitMain;

public class BinpackProvider {

	private GlobalLogger logger;

	public BinpackProvider() {
		logger = ToolkitMain.getGlobalLogger();
	}

	private Binpack browseBinpack(Component parent) {
		Properties settings = ToolkitMain.getPersistentStorage().getSettings();
		String lastPath = settings.getProperty("binpack.lastpath");
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (lastPath != null) {
			chooser.setSelectedFile(new File(lastPath));
		}
		int returnVal = chooser.showDialog(parent, "Select BinPack");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			logger.print("Approved BinPack browse.");
			settings.setProperty("binpack.lastpath", chooser.getSelectedFile().getAbsolutePath());
			ToolkitMain.getPersistentStorage().persistentStorageSave();
			return new Binpack(chooser.getSelectedFile());
		}
		return null;
	}

	private Binpack doReturnForBrowse(Component parent) {
		Binpack bPack = browseBinpack(parent);
		if (bPack != null) {
			if (!bPack.isValid()) {
				logger.print("Invalid BinPack.");
				JOptionPane.showMessageDialog(parent, "The selected folder is not a valid BinPack.", "Invalid BinPack",
						JOptionPane.ERROR_MESSAGE);
				return null;
			} else {

				return bPack;
			}
		} else {
			logger.print("BinPack selection cancelled.");
			return null;
		}
	}

	private boolean downloadSubtask(File target, URL targUrl) {
		try (FileOutputStream writer = new FileOutputStream(target)) {
			URLConnection conn = targUrl.openConnection();
			try (InputStream stream = conn.getInputStream()) {
				byte[] buffer = new byte[4096];
				int length = stream.read(buffer, 0, buffer.length);
				while (length != -1) {
					writer.write(buffer, 0, length);
					length = stream.read(buffer, 0, buffer.length);
				}
			}

		} catch (IOException e) {
			logger.printThrowable(e);
			return false;
		}
		return true;
	}

	public static InputStream getDefaultBinpackConfigStream() {
		return BinpackProvider.class.getClassLoader().getResourceAsStream("download_binpack.properties");
	}

	private Binpack downloadBinpack(Component parent, File target) {
		InputStream inpt = BinpackProvider.getDefaultBinpackConfigStream();
		if (inpt == null) {
			logger.print("download_binpack.properties not accessible.");
			return null;
		}
		if (!target.exists()) {
			if (!target.mkdir()) {
				logger.print("Cannot create the target folder.");
				return null;
			}
		}
		Binpack targetBinpack = new Binpack(target);
		boolean canContinue = true;

		Properties props = new Properties();
		try {
			props.load(inpt);
		} catch (IOException e1) {
			canContinue = false;
			logger.printThrowable(e1);
			logger.print("download_binpack.properties couldn't be loaded.");
		}

		try {
			inpt.close();
		} catch (IOException e) {
			logger.printThrowable(e);
			logger.print("download_binpack.properties couldn't be closed.");
		}
		if (canContinue) {
			String agentURLString = props.getProperty("agent");
			String updaterURLString = props.getProperty("updater");
			String externalURLString = props.getProperty("external");
			String pversionsURLString = props.getProperty("package-versions");
			String selfExtractURLString = props.getProperty("self-extract");
			if (agentURLString != null && updaterURLString != null && externalURLString != null
					&& pversionsURLString != null && selfExtractURLString != null) {
				try {
					URL agentURL = new URL(agentURLString);
					URL updaterURL = new URL(updaterURLString);
					URL externalURL = new URL(externalURLString);
					URL pversionsURL = new URL(pversionsURLString);
					URL selfExtractURL = new URL(selfExtractURLString);
					File agentFile = targetBinpack.getAgentFile();
					File updaterFile = targetBinpack.getStandalonePackageFile();
					File externalFile = targetBinpack.getExternalLibraryFile();
					File pversionsFile = targetBinpack.getPackageVersionsFile();
					File selfExtractFile = targetBinpack.getSelfExtractFile();
					agentFile.delete();
					updaterFile.delete();
					externalFile.delete();
					pversionsFile.delete();
					selfExtractFile.delete();
					if (!downloadSubtask(agentFile, agentURL)) {
						return null;
					}
					if (!downloadSubtask(updaterFile, updaterURL)) {
						return null;
					}
					if (!downloadSubtask(externalFile, externalURL)) {
						return null;
					}
					if (!downloadSubtask(pversionsFile, pversionsURL)) {
						return null;
					}
					if (!downloadSubtask(selfExtractFile, selfExtractURL)) {
						return null;
					}
					if (targetBinpack.isValid()) {
						logger.print("Successfully downloaded the BinPack.");
						return targetBinpack;
					}
				} catch (MalformedURLException e) {
					logger.printThrowable(e);
					logger.print("Malformed URL.");
				}
			}
		}

		return null;
	}

	public Binpack getBinPack(Component parent) {
		String[] choicesNonPresent = { "Download from GitHub", "Browse a BinaryPack", "Cancel" };
		String[] choicesPresent = { "Use cached BinaryPack", "Download from GitHub", "Browse a BinaryPack", "Cancel" };

		String[] definiteChoices;

		File cachedBinpackFile = new File(ToolkitMain.getCuToolkitRootPath(), "latest_binpack");

		Binpack cachedBinpack = new Binpack(cachedBinpackFile);

		int choice = 0;
		boolean isBinpackValid = cachedBinpack.isValid();

		if (isBinpackValid) {
			definiteChoices = choicesPresent;
		} else {
			definiteChoices = choicesNonPresent;
		}

		choice = JOptionPane.showOptionDialog(parent, "Please select how to find the BinPack.", "BinPack Selection",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, definiteChoices, definiteChoices[0]);

		if (isBinpackValid) {
			if (choice == 0) {
				// Use cached
				return cachedBinpack;
			}
			if (choice == 1) {
				return downloadBinpack(parent, cachedBinpackFile);
				// Download
			}
			if (choice == 2) {
				// Browse
				return doReturnForBrowse(parent);
			}
		} else {
			if (choice == 0) {
				return downloadBinpack(parent, cachedBinpackFile);
				// Download
			}
			if (choice == 1) {
				// Browse
				return doReturnForBrowse(parent);
			}
		}
		return null;
	}

}
