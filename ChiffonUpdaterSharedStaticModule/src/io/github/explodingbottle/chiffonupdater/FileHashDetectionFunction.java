/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

//<to hash>
//returns: hash
public class FileHashDetectionFunction implements DetectionFunction {

	private Translator translator;
	private SharedLogger logger;
	
	public FileHashDetectionFunction(SharedLogger logger) {
		translator = StandaloneUpdaterFunctionsModule.staticModuleTranslator;
		this.logger = logger;
	}

	@Override
	public String runDetection(File productRoot, String[] arguments) {
		if (arguments.length == 1) {
			File targetFile = new File(productRoot, arguments[0]);
			HashComputer computer = new HashComputer(targetFile, logger);
			return computer.computeHash();
		}
		return null;
	}

	@Override
	public String getUserFriendlyCommandDetail(String[] arguments) {
		return translator.getTranslation("operation.check.hash", arguments[0]);
	}

}
