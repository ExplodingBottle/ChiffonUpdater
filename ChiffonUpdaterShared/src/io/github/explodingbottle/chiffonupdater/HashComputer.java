/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class HashComputer {

	private File file;
	private SharedLogger logger;
	private InputStream targetStream;

	public HashComputer(File file, SharedLogger logger) {
		this.file = file;
		this.logger = logger;
	}

	public HashComputer(InputStream targetStream, SharedLogger logger) {
		this.targetStream = targetStream;
		this.logger = logger;
	}

	public String computeHash() {
		MessageDigest digest;
		boolean invalidated = false;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			logger.log("HCMP", LogLevel.ERROR, "SHA-256 algorithm not found.");
			return null;
		}
		InputStream inputStream;
		if (targetStream == null) {
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				logger.log("HCMP", LogLevel.WARNING, "Failed to find the requested file.");
				return null;
			}
		} else {
			inputStream = targetStream;
		}

		try {
			byte[] buffer = new byte[4096];
			int read = inputStream.read(buffer, 0, buffer.length);
			while (read != -1) {
				digest.update(buffer, 0, read);
				read = inputStream.read(buffer, 0, buffer.length);
			}

		} catch (IOException e) {
			invalidated = true;
			logger.log("HCMP", LogLevel.WARNING, "Read failure.");
		}
		if (targetStream == null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.log("HCMP", LogLevel.WARNING, "Failed to close the requested file.");
			}
		}

		if (!invalidated) {
			byte[] byteDigest = digest.digest();
			BigInteger integer = new BigInteger(1, byteDigest);
			String hashHex = integer.toString(16).toLowerCase();
			while (hashHex.length() < 64) {
				hashHex = "0" + hashHex;
			}
			return hashHex;
		}
		return null;
	}

}
