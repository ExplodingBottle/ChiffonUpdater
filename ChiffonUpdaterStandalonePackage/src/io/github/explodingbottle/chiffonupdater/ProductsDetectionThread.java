/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ProductsDetectionThread extends Thread {

	private DetectionDoneCallback doneCallback;
	private UpdaterState state;
	private String targetProduct;
	private String targetVersion;
	private FunctionsGatherer updaterFunctions;
	private Properties iterationsPropFile;
	private List<Properties> detectionFiles;

	public ProductsDetectionThread(DetectionDoneCallback doneCallback, UpdaterState state, String targetProduct,
			String targetVersion, FunctionsGatherer updaterFunctions, Properties iterationsPropFile,
			List<Properties> detectionFiles) {
		this.doneCallback = doneCallback;
		this.state = state;
		this.targetProduct = targetProduct;
		this.targetVersion = targetVersion;
		this.updaterFunctions = updaterFunctions;
		this.iterationsPropFile = iterationsPropFile;
		this.detectionFiles = detectionFiles;
	}

	public void run() {
		ProductDetector prodDetect = new ProductDetector(state.getSharedLogger(), state.getProductsListManager(),
				detectionFiles, updaterFunctions, state.getCustomProductDirectory());
		List<DetectionResult> products = prodDetect.detectProducts();
		List<DetectionResult> usableProducts = new ArrayList<DetectionResult>();

		String targetLinkedVersionId = iterationsPropFile.getProperty(targetVersion);
		int targetProductId = -1;
		if (targetLinkedVersionId != null) {
			try {
				targetProductId = Integer.parseInt(targetLinkedVersionId);
			} catch (NumberFormatException e) {
				state.getSharedLogger().log("PMTH", LogLevel.WARNING,
						"Ignored a malformed iterations file for target version.");
			}
		}

		for (DetectionResult product : products) {
			if (product.getProductName().equals(targetProduct)) {
				String linkedName = iterationsPropFile.getProperty(product.getVersion());
				if (linkedName != null) {
					try {
						int casted = Integer.parseInt(linkedName);
						if (casted < targetProductId) {
							usableProducts.add(product);
						}
					} catch (NumberFormatException e) {
						state.getSharedLogger().log("PMTH", LogLevel.WARNING, "Ignored a malformed iterations file.");
					}
				}
			}
		}
		doneCallback.calllbackOnDetectionDone(usableProducts);
	}

}
