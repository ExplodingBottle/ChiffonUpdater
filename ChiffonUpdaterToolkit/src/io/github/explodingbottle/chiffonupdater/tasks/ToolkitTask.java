/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.tasks;

import io.github.explodingbottle.chiffonupdater.GlobalLogger;
import io.github.explodingbottle.chiffonupdater.ToolkitMain;
import io.github.explodingbottle.chiffonupdater.ToolkitWindow;

public abstract class ToolkitTask extends Thread {

	private ToolkitWindow toolkitWindow;
	private GlobalLogger logger;
	private boolean shouldImpactMainWindow;

	public ToolkitTask(ToolkitWindow toolkitWindow) {
		this.toolkitWindow = toolkitWindow;
		shouldImpactMainWindow = true;
		logger = ToolkitMain.getGlobalLogger();
	}

	public final void setShouldImpactMainWindow(boolean newImpact) {
		shouldImpactMainWindow = newImpact;
	}

	public final GlobalLogger getLogger() {
		return logger;
	}

	public final ToolkitWindow returnWindow() {
		return toolkitWindow;
	}

	public final void start() {
		if (shouldImpactMainWindow) {
			toolkitWindow.setIsWindowBusy(true);
		}
		super.start();
	}

	public final void run() {
		try {
			runTask();
		} catch (Exception e) {
			if (shouldImpactMainWindow) {
				toolkitWindow.setIsWindowBusy(false);
			}
			throw e;
		}
		if (shouldImpactMainWindow) {
			toolkitWindow.setIsWindowBusy(false);
		}
	}

	public abstract void runTask();

}
