/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.nio.file.Paths;

public class FileStartsWith {

	public static boolean filePathStartsWith(String path, String startsWith) {
		return Paths.get(path).startsWith(Paths.get(startsWith));
	}
	

}
