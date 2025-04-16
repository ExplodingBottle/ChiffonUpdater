/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class HybridInformations {
	private String versionName;
	private String description;
	private Long releaseDate;
	private Long installDate;
	private boolean forUninstall;
	private String binaryHash;
	private String eulaHash;

	public HybridInformations(String versionName, String description, Long releaseDate, Long installDate,
			boolean forUninstall, String binaryHash, String eulaHash) {
		this.versionName = versionName;
		this.description = description;
		this.releaseDate = releaseDate;
		this.installDate = installDate;
		this.forUninstall = forUninstall;
		this.binaryHash = binaryHash;
		this.eulaHash = eulaHash;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getUpdateDescription() {
		return description;
	}

	public Long getUpdateReleaseDate() {
		return releaseDate;
	}

	public Long getUpdateInstallDate() {
		return installDate;
	}

	public boolean isInformationForUninstall() {
		return forUninstall;
	}

	public String getBinaryHash() {
		return binaryHash;
	}

	public String getEULAHash() {
		return eulaHash;
	}
}
