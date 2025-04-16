/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class SharedLogger {

	private File destination;
	private boolean allowPrinting;
	private FileOutputStream fos;

	public SharedLogger(File destination, boolean allowPrinting) {
		this.destination = destination;
		this.allowPrinting = allowPrinting;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			closeFileLogging();
		}));
	}

	public void setNewDestination(File newDestination) {
		if (fos == null) {
			destination = newDestination;
		}
	}

	public void log(String component, LogLevel level, String message) {
		LocalDateTime localDate = LocalDateTime.now();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String logMessage = dateFormatter.format(localDate) + " [" + component + "](" + level.name() + "): " + message;
		if (allowPrinting) {
			System.out.println(logMessage);
		}
		if (fos != null) {
			try {
				fos.write((logMessage + "\r\n").getBytes());
			} catch (IOException e) {

			}
		}
	}

	public void closeFileLogging() {
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				log("LOGGER", LogLevel.WARNING, "Failed to close the log file.");
			}
			fos = null;
		}
	}

	public void openFileLogging(boolean append) {
		if (fos != null) {
			log("LOGGER", LogLevel.WARNING, "Ignored an attempt to open again the logging file.");
		} else {
			try {
				fos = new FileOutputStream(destination, append);
			} catch (FileNotFoundException e) {
				log("LOGGER", LogLevel.WARNING,
						"Failed to open the logging file. Logging will only be done through console.");
			}
		}
	}

}
