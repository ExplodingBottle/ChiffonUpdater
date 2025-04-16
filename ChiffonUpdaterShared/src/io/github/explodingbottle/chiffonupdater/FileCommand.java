/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

class FileCommand {
	private String command;
	private String[] commandArgs;
	private String rawCommand;
	private boolean checkSkipped;

	public FileCommand(String command, String[] commandArgs, String rawCommand) {
		this.command = command;
		this.commandArgs = commandArgs;
		this.rawCommand = rawCommand;
		checkSkipped = false;
	}

	public String getCommand() {
		return command;
	}

	public boolean isCheckSkipped() {
		return checkSkipped;
	}

	public void setCheckSkipped(boolean checkSkipped) {
		this.checkSkipped = checkSkipped;
	}

	public String[] getCommandArguments() {
		return commandArgs;
	}

	public String getRawCommand() {
		return rawCommand;
	}
}
