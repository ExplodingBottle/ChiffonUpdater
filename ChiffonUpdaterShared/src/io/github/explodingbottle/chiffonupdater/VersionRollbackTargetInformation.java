/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

class VersionRollbackTargetInformation {

	private VersionRollbackState state;
	private String newerVersion;
	private String targetVersion;
	private String description;
	private Long installDate;
	private Long releaseDate;

	public VersionRollbackTargetInformation(VersionRollbackState state, String targetVersion, String newerVersion,
			String description, Long installDate, Long releaseDate) {
		this.state = state;
		this.targetVersion = targetVersion;
		this.description = description;
		this.installDate = installDate;
		this.releaseDate = releaseDate;
	}

	public String getNewerVersion() {
		return newerVersion;
	}

	public String getTargetVersion() {
		return targetVersion;
	}

	public String getDescription() {
		return description;
	}

	public Long getInstallDate() {
		return installDate;
	}

	public Long getReleaseDate() {
		return releaseDate;
	}

	public VersionRollbackState getState() {
		return state;
	}

}
