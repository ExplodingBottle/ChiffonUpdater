/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

import javax.swing.JOptionPane;

class ExternalLibraryWrapper {

	public static void main(String[] args) {
		String command = System.getProperty("cuwrapper.command");
		String binaryFile = System.getProperty("cuwrapper.binaryFile");
		String logSystemOut = System.getProperty("cuwrapper.logOnConsole");

		Translator translator = new Translator(new ModularTranslatorConfiguration("translations/${lang}.properties"),
				ExternalLibraryWrapper.class.getClassLoader());

		if (command == null) {
			String noDirectTranslation = translator.getTranslation("wrapper.nodirect");
			System.out.println(noDirectTranslation);
			JOptionPane.showMessageDialog(null, noDirectTranslation, translator.getTranslation("wrapper.error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		File binaryPath = null;
		if (binaryFile != null) {
			binaryPath = new File(binaryFile);
		}
		boolean canAllowSystemOut = logSystemOut != null && logSystemOut.equalsIgnoreCase("true");
		ChiffonUpdaterTool updaterTool = new ChiffonUpdaterTool(canAllowSystemOut);
		updaterTool.initialize();
		if (command.equalsIgnoreCase("hash")) {
			System.out.println("HASH=" + updaterTool.computeHash(binaryPath));
		}
		if (command.equalsIgnoreCase("license")) {
			System.out
					.println(translator.getTranslation("wrapper.licenseloc", ChiffonUpdaterTool.getLicenseFileName()));
		}
		if (command.equalsIgnoreCase("updatelist")) {
			updaterTool.updateProgramsList();
		}
		if (command.equalsIgnoreCase("regbin")) {
			updaterTool.registerProgram(binaryPath);
		}
	}
}
