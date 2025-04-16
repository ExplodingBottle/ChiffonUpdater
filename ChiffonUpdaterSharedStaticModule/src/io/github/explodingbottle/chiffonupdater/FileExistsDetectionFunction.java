/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

//<to check>;[force_type]
//returns: true/false
public class FileExistsDetectionFunction implements DetectionFunction {

	private Translator translator;

	public FileExistsDetectionFunction() {
		translator = StandaloneUpdaterFunctionsModule.staticModuleTranslator;
	}

	@Override
	public String runDetection(File productRoot, String[] arguments) {
		if (arguments.length >= 1) {
			File targetFile = new File(productRoot, arguments[0]);
			if (targetFile.exists()) {
				if (arguments.length == 2) {
					if (arguments[1].equals("file") && targetFile.isFile()) {
						return "true";
					}
					if (arguments[1].equals("folder") && targetFile.isDirectory()) {
						return "true";
					}
				}
				return "false";
			} else {
				return "false";
			}
		}
		return null;
	}

	@Override
	public String getUserFriendlyCommandDetail(String[] arguments) {
		return translator.getTranslation("operation.check.exists", arguments[0]);
	}

}
