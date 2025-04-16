/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.github.explodingbottle.chiffonupdater.FileInfos;

public class ProductFilesInfo implements Serializable {

	private static final long serialVersionUID = -6662751955315991639L;

	private List<FileInfos> fileInformations;

	private VersionInfo parent;

	public ProductFilesInfo(VersionInfo parent) {
		fileInformations = new ArrayList<FileInfos>();
		this.parent = parent;
	}

	public List<FileInfos> getFileHashesListReference() {
		return fileInformations;
	}

	public VersionInfo getParent() {
		return parent;
	}

}
