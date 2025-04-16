/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.github.explodingbottle.chiffonupdater.ChiffonUpdaterProject;

public class ProductInfo implements Serializable {

	private static final long serialVersionUID = -8230313314207535247L;

	private String productName;
	private List<String> features;
	private List<VersionInfo> versions;
	private ChiffonUpdaterProject parent;

	private String uninstallerName;
	private boolean doNotCreateUninstaller;

	transient private String onDiskName;

	public ProductInfo(ChiffonUpdaterProject parent) {
		productName = "";
		uninstallerName = "Uninstall updates.jar";
		features = new ArrayList<String>();
		versions = new ArrayList<VersionInfo>();
		doNotCreateUninstaller = false;
		this.parent = parent;
	}

	public void updateOnDiskName() {
		onDiskName = productName;
	}

	public String getOnDiskName() {
		return onDiskName;
	}

	public boolean doNotCreateUninstaller() {
		return doNotCreateUninstaller;
	}

	public void setDoNotCreateUninstaller(boolean doNotCreateUninstaller) {
		this.doNotCreateUninstaller = doNotCreateUninstaller;
	}

	public String getUninstallerName() {
		return uninstallerName;
	}

	public void setUninstallerName(String uninstallerName) {
		this.uninstallerName = uninstallerName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public List<String> getFeaturesReference() {
		return features;
	}

	public List<VersionInfo> getVersionsReference() {
		return versions;
	}

	public VersionInfo getLatestVersion() {
		VersionInfo latest = null;

		for (VersionInfo info : versions) {
			if (latest == null || info.getIterationNumber() > latest.getIterationNumber()) {
				latest = info;
			}
		}

		return latest;
	}

	public ChiffonUpdaterProject getParent() {
		return parent;
	}
}
