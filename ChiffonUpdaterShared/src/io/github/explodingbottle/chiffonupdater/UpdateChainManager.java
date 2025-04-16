/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

class UpdateChainManager {
	private File updateChain;

	public UpdateChainManager(File updateChain) {
		this.updateChain = updateChain;
	}

	public boolean getVersionsChain(List<String> orderedVersion) {
		boolean success = true;
		try (FileInputStream fis = new FileInputStream(updateChain)) {
			Properties props = new Properties();
			props.load(fis);
			String versionsChain = props.getProperty("versions.chain");
			if (versionsChain != null) {
				String[] versions = versionsChain.split(";");
				for (String vers : versions) {
					orderedVersion.add(vers);
				}
			} else {
				success = false;
			}
		} catch (IOException e) {
			success = false;
		}
		return success;
	}

	public boolean updateChain(List<String> orderedVersion) {
		boolean success = true;
		try (FileOutputStream fos = new FileOutputStream(updateChain)) {
			Properties props = new Properties();
			props.setProperty("versions.chain", String.join(";", orderedVersion));
			props.store(fos, "Update chain for rolling back.\r\nDO NOT EDIT!");

		} catch (IOException e) {
			success = false;
		}
		return success;
	}
}
