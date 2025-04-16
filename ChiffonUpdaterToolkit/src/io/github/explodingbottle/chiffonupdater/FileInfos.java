/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.Serializable;

public class FileInfos implements Serializable {

	private static final long serialVersionUID = 5977318617962432358L;

	private String filePath;
	private String fileHash;

	public FileInfos(String filePath, String fileHash) {
		this.filePath = filePath;
		this.fileHash = fileHash;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

}
