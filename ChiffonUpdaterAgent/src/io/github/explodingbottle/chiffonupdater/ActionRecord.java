/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class ActionRecord implements Serializable {

	private static final long serialVersionUID = -5627823587205234390L;

	private ActionResult status;
	private long actionDate;
	private boolean wasDowngrade;
	private File installLocation;
	private String installVersion;
	private String targetVersion;
	private String productName;
	private List<String> features;

	public ActionRecord(String productName, ActionResult status, long actionDate, boolean wasDowngrade,
			File installLocation, String installVersion, String targetVersion, List<String> features) {
		this.status = status;
		this.actionDate = actionDate;
		this.wasDowngrade = wasDowngrade;
		this.installLocation = installLocation;
		this.installVersion = installVersion;
		this.targetVersion = targetVersion;
		this.features = features;
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public ActionResult getStatus() {
		return status;
	}

	public long getActionDate() {
		return actionDate;
	}

	public boolean wasDowngrade() {
		return wasDowngrade;
	}

	public File getInstallLocation() {
		return installLocation;
	}

	public String getInstallVersion() {
		return installVersion;
	}

	public String getTargetVersion() {
		return targetVersion;
	}

	public List<String> getFeatures() {
		return features;
	}

}
