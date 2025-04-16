/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;
import java.util.Map;

class DetectionSectionContent {

	private DetectionSection parent;
	private String reportingFile;
	private Map<String, List<DetectionCommand>> commandsPerFeature;

	public DetectionSectionContent(DetectionSection parent, String reportingFile,
			Map<String, List<DetectionCommand>> commandsPerFeature) {
		this.parent = parent;
		this.reportingFile = reportingFile;
		this.commandsPerFeature = commandsPerFeature;
	}

	public DetectionSection getParentingSection() {
		return parent;
	}

	public String getReportingFileName() {
		return reportingFile;
	}

	public Map<String, List<DetectionCommand>> returnDetectionCommandsListPerFeature() {
		return commandsPerFeature;
	}
}
