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
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Properties;

public class PersistentAccessKeyUtil {

	private static final String PERSISTENT_ACCESS_KEY_NAME = "persacc.key";

	public static String createValidKeyForFolder(File folder) {
		Encoder encoder = Base64.getEncoder();
		File keyFile = new File(folder, PERSISTENT_ACCESS_KEY_NAME);
		String key = null;
		if (keyFile.exists()) {
			try (FileInputStream input = new FileInputStream(keyFile)) {
				Properties loadedKey = new Properties();
				loadedKey.load(input);
				if ("cert1".equals(loadedKey.getProperty("magic1")) && "chiffon".equals(loadedKey.getProperty("magic2"))
						&& "security".equals(loadedKey.getProperty("magic3"))) {
					key = loadedKey.getProperty("magic4");
				}
			} catch (IOException e) {

			}
		}
		if (key == null) {
			SecureRandom secureRandom = new SecureRandom();
			key = Integer.toHexString(secureRandom.nextInt());
			Properties toWrite = new Properties();
			toWrite.setProperty("magic1", "cert1");
			toWrite.setProperty("magic2", "chiffon");
			toWrite.setProperty("magic3", "security");
			toWrite.setProperty("magic4", key);

			try (FileOutputStream output = new FileOutputStream(keyFile)) {
				toWrite.store(output, "Persistent access key. Remove the file if the key has been compromised.");
			} catch (IOException e) {
				return null;
			}
		}
		return encoder.encodeToString(folder.getAbsolutePath().getBytes()) + ":" + key;
	}

	public static boolean checkKeyValidity(String key) {
		String keySplit[] = key.split(":");
		if (keySplit.length != 2) {
			return false;
		}
		Decoder decoder = Base64.getDecoder();
		String path = null;
		try {
			path = new String(decoder.decode(keySplit[0]));
		} catch (IllegalArgumentException e) {
			return false;
		}
		File keyFile = new File(path, PERSISTENT_ACCESS_KEY_NAME);
		if (!keyFile.exists()) {
			return false;
		}
		try (FileInputStream input = new FileInputStream(keyFile)) {
			Properties loadedKey = new Properties();
			loadedKey.load(input);
			if ("cert1".equals(loadedKey.getProperty("magic1")) && "chiffon".equals(loadedKey.getProperty("magic2"))
					&& "security".equals(loadedKey.getProperty("magic3"))) {
				return keySplit[1].equals(loadedKey.getProperty("magic4"));
			}
		} catch (IOException e) {

		}
		return false;
	}

}
