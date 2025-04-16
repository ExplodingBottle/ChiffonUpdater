/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class FileInfosComp {

	private FileInfos infos;
	private String feature;

	public FileInfosComp(FileInfos infos, String feature) {
		this.infos = infos;
		this.feature = feature;
	}

	public FileInfos getInfos() {
		return infos;
	}

	public void setInfos(FileInfos infos) {
		this.infos = infos;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	};

}
