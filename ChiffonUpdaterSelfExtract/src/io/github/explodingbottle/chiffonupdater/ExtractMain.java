/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

public class ExtractMain {

	private static void missingResourceEnd(Translator translator) {
		String message = translator.getTranslation("extract.missingtext");
		System.err.println(message);
		JOptionPane.showMessageDialog(null, message, translator.getTranslation("extract.missingtitle"),
				JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}

	public static void main(String[] args) {

		String potentialOverridenUpdateDir = System.getProperty("cupackage.updatedir");
		String potentialOverridenProductDir = System.getProperty("cupackage.productdir");
		String noConsoleSwitch = System.getProperty("cupackage.noconsole");
		String noGraphicsSwitch = System.getProperty("cupackage.nographics");
		String eulaAcceptSwitch = System.getProperty("cupackage.accepteula");
		String noVersionCheck = System.getProperty("cupackage.noverscheck");
		String uninstVers = System.getProperty("cupackage.uninstallversion");
		String custLog = System.getProperty("cupackage.customlog");
		String nologging = System.getProperty("cupackage.nologging");

		Translator translator = new Translator(new ModularTranslatorConfiguration("translations/${lang}.properties"),
				ExtractMain.class.getClassLoader());

		InputStream stream = ExtractMain.class.getClassLoader().getResourceAsStream("extract.properties");
		File jarFile = null;
		try {
			jarFile = new File(ExtractMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.err.println("Failed to find the jar path.");
			missingResourceEnd(translator);
		}
		if (stream == null) {
			missingResourceEnd(translator);
		}

		Properties extractConfig = new Properties();
		try {
			extractConfig.load(stream);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to load the configuration.");
			missingResourceEnd(translator);
		}

		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't close the extract stream.");
		}

		String extractFolder = extractConfig.getProperty("content");
		String mainJar = extractConfig.getProperty("mainJar");

		if (extractFolder == null || mainJar == null) {
			System.err.println("Invalid configuration.");
			missingResourceEnd(translator);
		}

		Random random = new Random();
		File extractOutput = new File(System.getProperty("java.io.tmpdir"), "cuext" + random.nextInt(99999999));

		extractOutput.deleteOnExit();

		if (!extractOutput.mkdir()) {
			System.err.println("Cannot create the output folder.");
			missingResourceEnd(translator);
		}

		byte[] buffer = new byte[4096];

		boolean canLaunch = true;
		try (FileInputStream input = new FileInputStream(jarFile); ZipInputStream jInput = new ZipInputStream(input);) {
			ZipEntry entry = jInput.getNextEntry();
			while (entry != null) {
				if (entry.getName().startsWith(extractFolder)) {
					if (entry.isDirectory()) {
						File dir = new File(extractOutput, entry.getName());
						if (!dir.mkdirs()) {
							canLaunch = false;
							break;
						} else {
							dir.deleteOnExit();
						}
					} else {
						File out = new File(extractOutput, entry.getName());
						if (!out.getParentFile().exists()) {
							if (!out.getParentFile().mkdirs()) {
								canLaunch = false;
								break;
							} else {
								out.getParentFile().deleteOnExit();
							}
						}

						try (FileOutputStream fos = new FileOutputStream(out)) {
							out.deleteOnExit();
							int length = jInput.read(buffer, 0, buffer.length);
							while (length != -1) {
								fos.write(buffer, 0, length);
								length = jInput.read(buffer, 0, buffer.length);
							}
						}
					}
				}
				entry = jInput.getNextEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't extract files.");
			missingResourceEnd(translator);
		}

		if (!canLaunch) {
			System.err.println("Couldn't extract files.");
			missingResourceEnd(translator);
		}

		File extractOutputName = new File(extractOutput, extractFolder);
		File toRunFile = new File(extractOutputName, mainJar);
		File updateFolder = extractOutputName;

		if (potentialOverridenUpdateDir != null) {
			updateFolder = new File(potentialOverridenUpdateDir);
		}

		JarJumpUtil.jumpToOther(toRunFile, updateFolder, potentialOverridenProductDir, noConsoleSwitch,
				noGraphicsSwitch, uninstVers, noVersionCheck, custLog, nologging, eulaAcceptSwitch);

	}

}
