/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.project;

import java.io.Serializable;

public class WebsiteInfo implements Serializable {

	private static final long serialVersionUID = -5191426190798030679L;

	private String backendUrl;
	private String agentDlUrl;
	private String packageDlUrl;
	private String packageVersDlUrl;
	private boolean allowCatalogUsage;

	public WebsiteInfo() {
		backendUrl = "";
		agentDlUrl = "";
		packageDlUrl = "";
		packageVersDlUrl = "";
		allowCatalogUsage = false;
	}

	public String getBackendUrl() {
		return backendUrl;
	}

	public void setBackendUrl(String backendUrl) {
		this.backendUrl = backendUrl;
	}

	public String getAgentDlUrl() {
		return agentDlUrl;
	}

	public void setAgentDlUrl(String agentDlUrl) {
		this.agentDlUrl = agentDlUrl;
	}

	public String getPackageDlUrl() {
		return packageDlUrl;
	}

	public void setPackageDlUrl(String packageDlUrl) {
		this.packageDlUrl = packageDlUrl;
	}

	public String getPackageVersDlUrl() {
		return packageVersDlUrl;
	}

	public void setPackageVersDlUrl(String packageVersDlUrl) {
		this.packageVersDlUrl = packageVersDlUrl;
	}

	public void setAllowCatalogUsage(boolean allowCatalogUsage) {
		this.allowCatalogUsage = allowCatalogUsage;
	}

	public boolean allowsCatalogUsage() {
		return allowCatalogUsage;
	}

}
