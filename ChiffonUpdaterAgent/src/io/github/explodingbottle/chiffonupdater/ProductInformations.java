/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductInformations {

	private String productName;
	private String currentVersion;
	private File productInstallationPath;
	private List<String> features;
	private List<HybridInformations> hybridInformations;

	public ProductInformations(String productName, List<String> features, File productInstallationPath,
			String currentVersion) {
		this.productName = productName;
		this.features = features;
		this.productInstallationPath = productInstallationPath;
		hybridInformations = new ArrayList<HybridInformations>();
		this.currentVersion = currentVersion;
	}

	public HybridInformations searchUpdateInformation() {
		HybridInformations updateInfo = null;
		for (HybridInformations info : hybridInformations) {
			if (!info.isInformationForUninstall()) {
				if (updateInfo == null) {
					updateInfo = info;
				} else {
					return null;
				}
			}
		}
		return updateInfo;
	}

	public File getProductInstallationPath() {
		return productInstallationPath;
	}

	public String getProductName() {
		return productName;
	}

	public List<String> getProductFeatures() {
		return features;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public List<HybridInformations> getHybridInformations() {
		return hybridInformations;
	}

}
