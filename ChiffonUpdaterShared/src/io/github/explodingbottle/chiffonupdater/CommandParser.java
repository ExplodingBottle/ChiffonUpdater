/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

class CommandParser {

	public FileCommand parseFileFunctionFromString(String propVal) {
		String[] splited = propVal.split(";");
		if (splited.length < 1) {
			return null;
		} else {
			String commandName = splited[0];
			String[] args;
			if (splited.length > 1) {
				args = new String[splited.length - 1];
				for (int i = 1; i < splited.length; i++) {
					args[i - 1] = splited[i];
				}
			} else {
				args = new String[] {};
			}
			return new FileCommand(commandName, args, propVal);
		}
	};

	public DetectionCommand parseDetectionCommandFromString(String propVal) {
		String[] splited = propVal.split(";");
		if (splited.length < 2) {
			return null;
		} else {
			String expectedResult = splited[0];
			String commandName = splited[1];
			String[] args;
			if (splited.length > 2) {
				args = new String[splited.length - 2];
				for (int i = 2; i < splited.length; i++) {
					args[i - 2] = splited[i];
				}
			} else {
				args = new String[] {};
			}
			return new DetectionCommand(commandName, expectedResult, args);
		}
	};

}
