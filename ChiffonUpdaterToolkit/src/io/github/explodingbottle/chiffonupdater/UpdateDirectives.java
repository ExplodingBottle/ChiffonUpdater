/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.Map;
import java.util.Properties;

public class UpdateDirectives {

	private Properties updateProperties;
	private Map<String, String> fileNamesMap;
	private Map<String, String> fileFeaturesMap;

	public UpdateDirectives(Properties updateProperties, Map<String, String> fileNamesMap,
			Map<String, String> fileFeaturesMap) {
		this.updateProperties = updateProperties;
		this.fileNamesMap = fileNamesMap;
		this.fileFeaturesMap = fileFeaturesMap;
	}

	public Properties getUpdateProperties() {
		return updateProperties;
	}

	public Map<String, String> getFileNamesMap() {
		return fileNamesMap;
	}

	public Map<String, String> getFileFeaturesMap() {
		return fileFeaturesMap;
	}

}
