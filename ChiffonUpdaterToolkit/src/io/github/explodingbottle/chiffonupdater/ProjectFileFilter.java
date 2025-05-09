/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ProjectFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".cupj") || !f.isFile();
	}

	@Override
	public String getDescription() {
		return "Chiffon Updating project files (.cupj)";
	}

}
