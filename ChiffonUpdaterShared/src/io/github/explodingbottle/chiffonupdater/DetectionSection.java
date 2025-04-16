/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

class DetectionSection {
	private String sectionId;
	private String version;

	public DetectionSection(String sectionId, String version) {
		this.sectionId = sectionId;
		this.version = version;
	}

	public String getSectionId() {
		return sectionId;
	}

	public String getVersion() {
		return version;
	}

	public String toString() {
		return sectionId + "=" + version;
	}
}
