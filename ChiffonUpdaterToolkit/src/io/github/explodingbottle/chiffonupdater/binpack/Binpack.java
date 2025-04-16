/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.binpack;

import java.io.File;

public class Binpack {

	private File root;

	private File agentFile;
	private File standalonePackageFile;
	private File externalLibFile;
	private File packageVersionsFile;
	private File selfExtractFile;

	public Binpack(File root) {
		this.root = root;
		this.agentFile = new File(root, "agent.jar");
		this.standalonePackageFile = new File(root, "updater.jar");
		this.externalLibFile = new File(root, "external.jar");
		this.packageVersionsFile = new File(root, "package-versions.properties");
		this.selfExtractFile = new File(root, "self-extract.jar");
	}

	public boolean isValid() {
		return agentFile.exists() && standalonePackageFile.exists() && externalLibFile.exists()
				&& packageVersionsFile.exists() && selfExtractFile.exists();
	}

	public File getAgentFile() {
		return agentFile;
	}

	public File getStandalonePackageFile() {
		return standalonePackageFile;
	}

	public File getExternalLibraryFile() {
		return externalLibFile;
	}

	public File getPackageVersionsFile() {
		return packageVersionsFile;
	}

	public File getSelfExtractFile() {
		return selfExtractFile;
	}

	public File getBinpackRoot() {
		return root;
	}
}
