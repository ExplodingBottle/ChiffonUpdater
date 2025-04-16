/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.Comparator;

class FolderTreeComparator implements Comparator<FileInfosComp> {

	private boolean reversed;

	public FolderTreeComparator(boolean reversed) {
		this.reversed = reversed;
	}

	@Override
	public int compare(FileInfosComp o1, FileInfosComp o2) {
		int o1slashes = 0, o2slashes = 0;
		for (char chr : o1.getInfos().getFilePath().toCharArray()) {
			if (chr == '/') {
				o1slashes++;
			}
		}
		for (char chr : o2.getInfos().getFilePath().toCharArray()) {
			if (chr == '/') {
				o2slashes++;
			}
		}
		if (o1slashes < o2slashes) {
			return reversed ? 1 : -1;
		}
		if (o1slashes > o2slashes) {
			return reversed ? -1 : 1;
		}

		return 0;
	}

}
