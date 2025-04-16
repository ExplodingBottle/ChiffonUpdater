/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class SharedLoggerAdapter extends SharedLogger {

	private GlobalLogger logger;

	public SharedLoggerAdapter(GlobalLogger logger) {
		super(null, false);
		this.logger = logger;
	}

	@Override
	public void log(String component, LogLevel level, String message) {
		logger.print("(Component: " + component + ")[" + level.toString() + ": " + message);
	}

	@Override
	public void openFileLogging(boolean append) {

	}

	@Override
	public void closeFileLogging() {

	}

}
