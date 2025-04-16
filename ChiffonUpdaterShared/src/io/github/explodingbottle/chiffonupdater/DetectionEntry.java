/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

class DetectionEntry {

	private File productRoot;
	private File productReportedBinary;

	public DetectionEntry(File productRoot, File productReportedBinary) {
		this.productRoot = productRoot;
		this.productReportedBinary = productReportedBinary;
	}

	public File getProductRoot() {
		return productRoot;
	}

	public File getProductReportedBinary() {
		return productReportedBinary;
	}

}
