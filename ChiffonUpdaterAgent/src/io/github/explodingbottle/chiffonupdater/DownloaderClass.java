/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloaderClass {

	private File output;
	private URL input;
	private SharedLogger logger;

	private byte[] buffer;

	private static final int MAX_BUFF_SIZE = 4096;

	private static final String CMPN = "DLCL";

	public DownloaderClass(File output, URL input) {
		this.output = output;
		this.input = input;
		this.logger = AgentMain.getSharedLogger();
		buffer = new byte[MAX_BUFF_SIZE];
	}

	public boolean download() {
		URLConnection con;
		try {
			con = input.openConnection();
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to start download from " + input.toString() + ".");
			return false;
		}
		logger.log(CMPN, LogLevel.INFO, "A downloader instance is now downloading from " + input.toString() + ".");
		try (InputStream is = con.getInputStream(); FileOutputStream os = new FileOutputStream(output)) {
			int readed = is.read(buffer, 0, MAX_BUFF_SIZE);
			while (readed != -1) {
				os.write(buffer, 0, readed);
				readed = is.read(buffer, 0, MAX_BUFF_SIZE);
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed downloading from " + input.toString() + ".");
			return false;
		}
		return true;
	}

}
