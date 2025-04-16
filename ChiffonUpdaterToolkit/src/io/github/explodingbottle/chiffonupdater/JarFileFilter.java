/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class JarFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".jar") || !f.isFile();
	}

	@Override
	public String getDescription() {
		return "Java Library (.jar)";
	}

}
