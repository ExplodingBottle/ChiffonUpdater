/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HistoryFileUtil {

	private File historyFile;
	private SharedLogger logger;

	private static final String CMPN = "HSFU";

	public HistoryFileUtil(File historyFile) {
		this.historyFile = historyFile;
		logger = AgentMain.getSharedLogger();
	}

	public boolean addHistoryItem(ActionRecord item) {
		List<ActionRecord> ac = new ArrayList<ActionRecord>();
		ac.add(item);
		return addHistoryItems(ac);
	}

	public boolean addHistoryItems(List<ActionRecord> items) {
		List<ActionRecord> loaded = new ArrayList<ActionRecord>();
		if (historyFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(historyFile)))) {
				List<?> list = (List<?>) ois.readObject();
				for (Object o : list) {
					loaded.add(0, (ActionRecord) o);
				}
			} catch (Exception e) {
				logger.log(CMPN, LogLevel.WARNING, "Couldn't load the older history");
				return false;
			}
		}
		loaded.addAll(items);
		loaded.sort(new Comparator<ActionRecord>() {

			@Override
			public int compare(ActionRecord o1, ActionRecord o2) {
				if (o1.getActionDate() > o2.getActionDate()) {
					return 1;
				} else if (o1.getActionDate() < o2.getActionDate()) {
					return -1;
				}
				return 0;
			}
		});
		try (ObjectOutputStream ois = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(historyFile)))) {
			ois.writeObject(loaded);
		} catch (Exception e) {
			logger.log(CMPN, LogLevel.WARNING, "Couldn't save the newer history");
			return false;
		}
		return true;
	}

	public List<ActionRecord> getHistory() {
		List<ActionRecord> loaded = new ArrayList<ActionRecord>();
		if (historyFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(historyFile)))) {
				List<?> list = (List<?>) ois.readObject();
				for (Object o : list) {
					loaded.add(0, (ActionRecord) o);
				}
			} catch (Exception e) {
				logger.log(CMPN, LogLevel.WARNING, "Couldn't load the older history");
				return null;
			}
			return loaded;
		}
		return null;
	}
}
