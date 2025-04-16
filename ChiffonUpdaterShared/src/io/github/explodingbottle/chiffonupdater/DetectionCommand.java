/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

class DetectionCommand {

	private String command;
	private String expectedResult;
	private String[] commandArgs;

	public DetectionCommand(String command, String expectedResult, String[] commandArgs) {
		this.command = command;
		this.expectedResult = expectedResult;
		this.commandArgs = commandArgs;
	}

	public String getCommand() {
		return command;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public String[] getCommandArguments() {
		return commandArgs;
	}

}
