/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.explodingbottle.chiffonupdater.project.VersionInfo;

public class CatalogHeaderWriter {

	private int cathIdx;
	private int cathCnt;
	private DataOutputStream currentCat;
	private GlobalLogger logger;
	private File catFolder;
	private List<File> catFiles;

	private static final int MAX_ENTRIES_PER_FILE = 1024;

	public CatalogHeaderWriter(File catalogFolder) {
		cathIdx = 0;
		cathCnt = 0;
		currentCat = null;
		logger = ToolkitMain.getGlobalLogger();
		catFolder = catalogFolder;
		catFiles = new ArrayList<File>();
	}

	public void closeCatalog() {
		if (currentCat != null) {
			try {
				currentCat.close();
			} catch (IOException e) {
				logger.printThrowable(e);
			}
		}
	}

	public String getCurrentHeaderFile() {
		return "catinf" + cathIdx + ".chd";
	}

	public boolean openNewCatalog() {
		if (currentCat != null) {
			try {
				currentCat.writeBytes("CNEXT");
			} catch (IOException e) {
				logger.printThrowable(e);
				return false;
			}
		}
		closeCatalog();
		try {
			File fl = new File(catFolder, "catinf" + cathIdx + ".chd");
			currentCat = new DataOutputStream(new HighCompressionGZOutputStream(new FileOutputStream(fl)));
			cathIdx++;
			cathCnt = 0;
			catFiles.add(fl);
		} catch (IOException e) {
			logger.printThrowable(e);
			return false;
		}
		return true;
	}

	private void writeString(String s) throws IOException {
		currentCat.writeInt(s.length());
		currentCat.writeChars(s);
	}

	public boolean addCatalogEntry(VersionInfo targetVersion, String downloadFileName) {
		if (cathCnt >= MAX_ENTRIES_PER_FILE || currentCat == null) {
			if (!openNewCatalog()) {
				return false;
			}
		}
		String productName = targetVersion.getParent().getProductName();
		String versionName = targetVersion.getVersionName();
		String description = targetVersion.getVersionDescription();

		try {
			currentCat.writeBytes("CNTRY");
			currentCat.writeLong(targetVersion.getReleaseDate());
			writeString(productName);
			writeString(versionName);
			writeString(description);
			writeString(downloadFileName);
			cathCnt++;
		} catch (IOException e) {
			logger.printThrowable(e);
			return false;
		}

		return true;

	}

	public List<File> getWrittenCatalogsList() {
		return catFiles;
	}

}
