/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;

class ProductRollbackInformations {

	private RollbackEnvironmentState state;
	private List<VersionRollbackTargetInformation> versions;

	private List<String> originalVersionsChain;

	public ProductRollbackInformations() {
		state = RollbackEnvironmentState.CONFIG_NONPRESENT;
	}

	public ProductRollbackInformations(List<VersionRollbackTargetInformation> versions,
			List<String> originalVersionsChain) {
		this.versions = versions;
		this.originalVersionsChain = originalVersionsChain;
		state = versions == null ? RollbackEnvironmentState.ALL_UPDATES_BROKEN
				: RollbackEnvironmentState.ROLLBACK_READY;
	}

	public List<String> getOriginalVersionsChain() {
		return originalVersionsChain;
	}

	public RollbackEnvironmentState getState() {
		return state;
	}

	public List<VersionRollbackTargetInformation> getVersions() {
		return versions;
	}

}
