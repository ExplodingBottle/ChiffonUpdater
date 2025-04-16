/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.Serializable;

public class UpdateHistoryEntry implements Serializable {

	private static final long serialVersionUID = 6811225018819543687L;
	private File folderPath;
	private String productName;
	private String version;
	private String updateDescription;
	private long updateTime;

	public UpdateHistoryEntry(File folderPath, String productName, String version, String updateDescription,
			long updateTime) {
		this.folderPath = folderPath;
		this.productName = productName;
		this.version = version;
		this.updateDescription = updateDescription;
		this.updateTime = updateTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public File getFolderPath() {
		return folderPath;
	}

	public String getProductName() {
		return productName;
	}

	public String getVersion() {
		return version;
	}

	public String getUpdateDescription() {
		return updateDescription;
	}

}
