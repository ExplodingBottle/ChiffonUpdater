/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class ProjectTreeDisplayObject {

	private String text;
	private ProjectTreeDisplayObjectType displayType;

	public ProjectTreeDisplayObject(String text, ProjectTreeDisplayObjectType displayType) {
		this.text = text;
		this.displayType = displayType;
	}

	public String getText() {
		return text;
	}

	public ProjectTreeDisplayObjectType getDisplayType() {
		return displayType;
	}

}
