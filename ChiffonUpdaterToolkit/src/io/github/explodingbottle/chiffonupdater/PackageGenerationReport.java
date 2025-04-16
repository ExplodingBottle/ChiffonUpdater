/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class PackageGenerationReport {

	private boolean packageChanged;
	private boolean packageCreationFailed;

	public PackageGenerationReport() {
		packageChanged = false;
		packageCreationFailed = false;
	}

	public boolean hasPackageChanged() {
		return packageChanged;
	}

	public void setPackageChanged(boolean packageChanged) {
		this.packageChanged = packageChanged;
	}

	public boolean hasPackageCreationFailed() {
		return packageCreationFailed;
	}

	public void setPackageCreationFailed(boolean packageCreationFailed) {
		this.packageCreationFailed = packageCreationFailed;
	}

}
