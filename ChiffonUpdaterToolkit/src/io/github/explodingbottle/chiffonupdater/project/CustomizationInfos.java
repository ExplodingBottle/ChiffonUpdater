/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.project;

import java.io.Serializable;

public class CustomizationInfos implements Serializable {

	private static final long serialVersionUID = 3566942443457544964L;

	private String customAgentTitle;
	private String customPackageTitle;

	private String customAgentIconPath;
	private String customPackageIconPath;
	private String customPackageBannerPath;

	public CustomizationInfos() {
		customAgentTitle = "";
		customPackageTitle = "";
		customAgentIconPath = "";
		customPackageIconPath = "";
		customPackageBannerPath = "";
	}

	public String getCustomAgentIconPath() {
		return customAgentIconPath;
	}

	public void setCustomAgentIconPath(String customAgentIconPath) {
		this.customAgentIconPath = customAgentIconPath;
	}

	public String getCustomPackageIconPath() {
		return customPackageIconPath;
	}

	public void setCustomPackageIconPath(String customPackageIconPath) {
		this.customPackageIconPath = customPackageIconPath;
	}

	public String getCustomPackageBannerPath() {
		return customPackageBannerPath;
	}

	public void setCustomPackageBannerPath(String customPackageBannerPath) {
		this.customPackageBannerPath = customPackageBannerPath;
	}

	public String getCustomAgentTitle() {
		return customAgentTitle;
	}

	public void setCustomAgentTitle(String customAgentTitle) {
		this.customAgentTitle = customAgentTitle;
	}

	public String getCustomPackageTitle() {
		return customPackageTitle;
	}

	public void setCustomPackageTitle(String customPackageTitle) {
		this.customPackageTitle = customPackageTitle;
	}

}
