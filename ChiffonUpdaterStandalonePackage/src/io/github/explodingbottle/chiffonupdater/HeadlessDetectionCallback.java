/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;

public class HeadlessDetectionCallback implements DetectionDoneCallback {

	private UpdaterThread parentThread;

	public HeadlessDetectionCallback(UpdaterThread parentThread) {
		this.parentThread = parentThread;
	}

	@Override
	public void calllbackOnDetectionDone(List<DetectionResult> results) {
		SharedLogger logger = PackageMain.returnUpdaterState().getSharedLogger();
		if (results.size() != 1) {
			logger.log("HDLD", LogLevel.ERROR, "More than one or no products were detected.");
			return;
		}
		DetectionResult productToUpdate = results.get(0);
		UpdateProcessor processor = new UpdateProcessor(PackageMain.returnUpdaterState(),
				parentThread.getUpdatePropertiesFile(), parentThread.getDetectionFiles(), productToUpdate, logger,
				parentThread.getUpdaterFunctions(), new HeadlessUpdateInterface(), parentThread.getModulePath(),
				parentThread.getModuleClassName(), parentThread.getReleaseDate(), parentThread.getDescriptionText());
		processor.start();
	}

}
