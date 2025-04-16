/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.project;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VersionInfo implements Serializable {

	private static final long serialVersionUID = 4005738781546409265L;

	private Map<String, ProductFilesInfo> filesForFeatureMap;
	private Map<String, String> customDetectionCommandsByFeatureMap;
	private Map<String, String> customActionCommandsByFeatureMap;
	private String customPrereqCommands;
	private String versionName;
	private String reportingFileName;
	private long iterationNumber;
	private ProductInfo parent;
	private String versionDescription;
	private boolean showVersionDescription;
	private String customModulePath;
	private String customEulaPath;
	private String customModulePublisher;
	private long releaseDate;
	private boolean shouldHideInCatalog;
	transient private String onDiskName;

	public VersionInfo(ProductInfo parent) {
		filesForFeatureMap = new HashMap<String, ProductFilesInfo>();
		customDetectionCommandsByFeatureMap = new HashMap<String, String>();
		customActionCommandsByFeatureMap = new HashMap<String, String>();
		versionName = "";
		reportingFileName = "";
		versionDescription = "";
		customModulePath = "";
		customEulaPath = "";
		customModulePublisher = "";
		customPrereqCommands = "";
		iterationNumber = 0;
		showVersionDescription = true;
		shouldHideInCatalog = false;
		this.parent = parent;
	}

	public void updateOnDiskName() {
		onDiskName = versionName;
	}

	public String getOnDiskName() {
		return onDiskName;
	}

	public long getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(long releaseDate) {
		this.releaseDate = releaseDate;
	}

	public boolean getShowVersionDescription() {
		return showVersionDescription;
	}

	public void setShowVersionDescription(boolean showVersionDescription) {
		this.showVersionDescription = showVersionDescription;
	}

	public String getCustomModulePublisher() {
		return customModulePublisher;
	}

	public void setCustomModulePublisher(String customModulePublisher) {
		this.customModulePublisher = customModulePublisher;
	}

	public String getCustomModulePath() {
		return customModulePath;
	}

	public void setCustomModulePath(String customModulePath) {
		this.customModulePath = customModulePath;
	}

	public String getCustomEulaPath() {
		return customEulaPath;
	}

	public void setCustomEulaPath(String customEulaPath) {
		this.customEulaPath = customEulaPath;
	}

	public long getIterationNumber() {
		return iterationNumber;
	}

	public void setIterationNumber(long iterationNumber) {
		this.iterationNumber = iterationNumber;
	}

	public String getReportingFileName() {
		return reportingFileName;
	}

	public void setReportingFileName(String reportingFileName) {
		this.reportingFileName = reportingFileName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String newVersion) {
		versionName = newVersion;
	}

	public void setVersionDescription(String versionDescription) {
		this.versionDescription = versionDescription;
	}

	public Map<String, String> customDetectionCommandsByFeatureMap() {
		return customDetectionCommandsByFeatureMap;
	}

	public Map<String, String> customActionCommandsByFeatureMap() {
		return customActionCommandsByFeatureMap;
	}

	public Map<String, ProductFilesInfo> filesForFeatureMap() {
		return filesForFeatureMap;
	}

	public String getCustomPrerequisiteCommands() {
		return customPrereqCommands;
	}

	public void setCustomPrerequisiteCommands(String customPrereqCommands) {
		this.customPrereqCommands = customPrereqCommands;
	}

	public void setShouldHideInCatalog(boolean shouldHideInCatalog) {
		this.shouldHideInCatalog = shouldHideInCatalog;
	}

	public boolean shouldHideInCatalog() {
		return shouldHideInCatalog;
	}

	public String getVersionDescription() {
		return versionDescription;
	}

	public ProductInfo getParent() {
		return parent;
	}

}
