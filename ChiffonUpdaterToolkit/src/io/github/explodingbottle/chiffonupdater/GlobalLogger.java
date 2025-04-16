/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GlobalLogger {

	private ConsoleViewer viewerInterface;
	private StringBuilder cache;

	public void registerViewer(ConsoleViewer viewerInterface) {
		this.viewerInterface = viewerInterface;
		cache = new StringBuilder();
		flushCache();
	}

	private void flushCache() {
		if (viewerInterface != null) {
			viewerInterface.appendToConsole(cache.toString());
			cache.setLength(0);
		}
	}

	public void printThrowable(Throwable e) {
		StringBuilder excStr = new StringBuilder();
		e.printStackTrace(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				excStr.append((char) b);
			}
		}));
		String messageToPrint = "[";
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		if (stackTrace.length >= 2) {
			StackTraceElement parent = stackTrace[1];
			messageToPrint += parent.getClassName();
		} else {
			messageToPrint += "?";
		}
		messageToPrint += "] " + excStr.toString();
		cache.append(messageToPrint + "\n");
		flushCache();
	}

	public void print(String message) {
		String messageToPrint = "[";
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		if (stackTrace.length >= 2) {
			StackTraceElement parent = stackTrace[1];
			messageToPrint += parent.getClassName();
		} else {
			messageToPrint += "?";
		}
		messageToPrint += "] " + message;
		cache.append(messageToPrint + "\n");
		flushCache();
	}

}
