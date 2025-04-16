/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.util.List;

class DetectionResult {
	private String productName;
	private List<String> features;
	private String version;
	private File reportingFilePath;
	private File rootProductFolder;

	public DetectionResult(String productName, List<String> features, String version, File reportingFilePath,
			File rootProductFolder) {
		this.productName = productName;
		this.features = features;
		this.version = version;
		this.reportingFilePath = reportingFilePath;
		this.rootProductFolder = rootProductFolder;
	}

	public String getProductName() {
		return productName;
	}

	public List<String> getProductFeatures() {
		return features;
	}

	public String getVersion() {
		return version;
	}

	public File getReportingFilePath() {
		return reportingFilePath;
	}

	public File getRootProductFolder() {
		return rootProductFolder;
	}

}
