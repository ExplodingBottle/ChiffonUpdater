/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class CatalogItem {

	private String productName;
	private String productVersion;
	private long releaseDate;
	private String productDescription;
	private String downloadFileName;

	public CatalogItem(String productName, String productVersion, long releaseDate, String productDescription,
			String downloadFileName) {
		this.productName = productName;
		this.productVersion = productVersion;
		this.releaseDate = releaseDate;
		this.productDescription = productDescription;
		this.downloadFileName = downloadFileName;
	}

	public String getProductName() {
		return productName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public long getReleaseDate() {
		return releaseDate;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public String getDownloadFileName() {
		return downloadFileName;
	}

}
