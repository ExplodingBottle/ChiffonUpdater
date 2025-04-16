/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class AgentSettings {

	private URI packageDownloadURI;
	private URI pkgVersionsDownloadURI;

	private String customAgentTitle;
	private String customPackageTitle;

	private String customAgentIconFileName;
	private String customPackageIconFileName;
	private String customPackageBannerFileName;

	private AgentSettings(URI packageDownloadURI, URI pkgVersionsDownloadURI, String customAgentTitle,
			String customPackageTitle, String customAgentIconFileName, String customPackageIconFileName,
			String customPackageBannerFileName) {
		this.packageDownloadURI = packageDownloadURI;
		this.pkgVersionsDownloadURI = pkgVersionsDownloadURI;

		this.customAgentTitle = customAgentTitle;
		this.customPackageTitle = customPackageIconFileName;

		this.customAgentIconFileName = customAgentIconFileName;
		this.customPackageIconFileName = customPackageIconFileName;
		this.customPackageBannerFileName = customPackageBannerFileName;
	}

	public void printSettings(SharedLogger logger) {
		logger.log("AGSE", LogLevel.INFO, "PackageDownload: " + packageDownloadURI);
		logger.log("AGSE", LogLevel.INFO, "PackageVersionsDownload: " + pkgVersionsDownloadURI);

		logger.log("AGSE", LogLevel.INFO, "CustomAgentTitle: " + customAgentTitle);
		logger.log("AGSE", LogLevel.INFO, "CustomPackageTitle: " + customPackageTitle);

		logger.log("AGSE", LogLevel.INFO, "CustomAgentIconFile: " + customAgentIconFileName);
		logger.log("AGSE", LogLevel.INFO, "CustomPackageIconFile: " + customPackageIconFileName);
		logger.log("AGSE", LogLevel.INFO, "CustomPackageBannerFile: " + customPackageBannerFileName);
	}

	public static AgentSettings createFromPropertiesFile(Properties props) {
		String dlUrlStr = props.getProperty("agent.package.dlurl");
		String vdlUrlStr = props.getProperty("agent.package.vrurl");

		if (dlUrlStr == null || vdlUrlStr == null) {
			return null;
		}

		URI dlUri, vdlUri;
		try {
			dlUri = new URI(dlUrlStr);
			vdlUri = new URI(vdlUrlStr);
		} catch (URISyntaxException e) {
			return null;
		}

		String agtTitleStr = props.getProperty("agent.customization.title");
		String pkgTitleStr = props.getProperty("agent.package.customization.title");
		String agtIconStr = props.getProperty("agent.customization.icon");
		String pkgIconStr = props.getProperty("agent.package.customization.icon");
		String pkgBannerStr = props.getProperty("agent.package.customization.banner");

		return new AgentSettings(dlUri, vdlUri, agtTitleStr, pkgTitleStr, agtIconStr, pkgIconStr, pkgBannerStr);
	}

	public String getCustomAgentTitle() {
		return customAgentTitle;
	}

	public String getCustomPackageTitle() {
		return customPackageTitle;
	}

	public String getCustomAgentIconFileName() {
		return customAgentIconFileName;
	}

	public String getCustomPackageIconFileName() {
		return customPackageIconFileName;
	}

	public String getCustomPackageBannerFileName() {
		return customPackageBannerFileName;
	}

	public URI getPackageDownloadURI() {
		return packageDownloadURI;
	}

	public URI getPkgVersionsDownloadURI() {
		return pkgVersionsDownloadURI;
	}

}
