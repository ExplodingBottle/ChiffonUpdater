/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class ImageResourcesLoader {

	private static final String[] RESOURCES_NAMES = { "detection.gif", "wizard_icon.png", "wizard.png", "arrow.png",
			"check.png" };
	private Map<String, ImageIcon> loaded;

	public ImageResourcesLoader() {
		loaded = new HashMap<String, ImageIcon>();
		for (String res : RESOURCES_NAMES) {
			URL url = this.getClass().getClassLoader().getResource("images/" + res);
			if (url != null) {
				ImageIcon icon = new ImageIcon(url);
				if (icon != null) {
					loaded.put(res, icon);
				}
			}
		}
	}

	public ImageIcon getLoadedResource(String imageName) {
		return loaded.get(imageName);
	}

}
