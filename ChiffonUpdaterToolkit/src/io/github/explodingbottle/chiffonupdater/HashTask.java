/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.explodingbottle.chiffonupdater.tasks.ToolkitTask;

public class HashTask extends ToolkitTask {

	private File root;
	private HashFinishedListener listener;

	public HashTask(ToolkitWindow toolkitWindow, File root, HashFinishedListener listener) {
		super(toolkitWindow);
		this.root = root;
		this.listener = listener;
	}

	private boolean fileWalk(File root, List<FileInfos> infosRef, File rootAll) {
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				if (file.listFiles().length == 0) {
					infosRef.add(new FileInfos(rootAll.toPath().relativize(file.toPath()).toString().replace("\\", "/"),
							""));
				} else {
					if (!fileWalk(file, infosRef, rootAll)) {
						return false;
					}
				}
			} else {
				HashComputer computer = new HashComputer(file, new SharedLoggerAdapter(getLogger()));
				String hash = computer.computeHash();
				if (hash == null) {
					return false;
				}
				infosRef.add(
						new FileInfos(rootAll.toPath().relativize(file.toPath()).toString().replace("\\", "/"), hash));
			}
		}
		return true;
	}

	@Override
	public void runTask() {
		List<FileInfos> infosRef = new ArrayList<FileInfos>();
		if (fileWalk(root, infosRef, root)) {
			listener.onHashFinished(infosRef, this);
		} else {
			listener.onHashFinished(null, this);
		}
	}

}
