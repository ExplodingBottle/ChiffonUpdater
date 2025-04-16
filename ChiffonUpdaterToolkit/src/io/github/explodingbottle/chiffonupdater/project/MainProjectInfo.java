/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.project;

import java.io.Serializable;
import java.util.Properties;

public class MainProjectInfo implements Serializable {

	private static final long serialVersionUID = 5043591278504633612L;

	private String globalPath;
	private boolean useGlobalPath;

	private String windowsPath;
	private String macPath;
	private String unixPath;

	private WebsiteInfo websiteInfos;
	private CustomizationInfos customizationInfos;

	public MainProjectInfo() {
		windowsPath = "";
		macPath = "";
		unixPath = "";
		globalPath = "";
		useGlobalPath = false;

		websiteInfos = new WebsiteInfo();
		customizationInfos = new CustomizationInfos();
	}

	public CustomizationInfos getCustomizationInfos() {
		return customizationInfos;
	}

	public void setCustomizationInfos(CustomizationInfos customizationInfos) {
		this.customizationInfos = customizationInfos;
	}

	public WebsiteInfo getWebsiteInfos() {
		return websiteInfos;
	}

	public void setWebsiteInfos(WebsiteInfo websiteInfos) {
		this.websiteInfos = websiteInfos;
	}

	public String getGlobalPath() {
		return globalPath;
	}

	public void setGlobalPath(String globalPath) {
		this.globalPath = globalPath;
	}

	public boolean usesGlobalPath() {
		return useGlobalPath;
	}

	public void setUseGlobalPath(boolean useGlobalPath) {
		this.useGlobalPath = useGlobalPath;
	}

	public String getWindowsPath() {
		return windowsPath;
	}

	public void setWindowsPath(String windowsPath) {
		this.windowsPath = windowsPath;
	}

	public String getMacPath() {
		return macPath;
	}

	public void setMacPath(String macPath) {
		this.macPath = macPath;
	}

	public String getUnixPath() {
		return unixPath;
	}

	public void setUnixPath(String unixPath) {
		this.unixPath = unixPath;
	}

	public Properties createPropertiesFromInfos() {
		Properties newProps = new Properties();
		if (useGlobalPath) {
			newProps.setProperty("use-global", "true");
			newProps.setProperty("global-path", globalPath);
		} else {
			newProps.setProperty("use-global", "false");
			newProps.setProperty("windows-path", windowsPath);
			newProps.setProperty("mac-path", macPath);
			newProps.setProperty("unix-path", unixPath);

		}
		return newProps;
	}

}
