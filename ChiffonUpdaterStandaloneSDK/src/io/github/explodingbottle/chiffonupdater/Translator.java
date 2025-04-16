/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * This class represents a translator for your module.
 * 
 * @author ExplodingBottle
 *
 */
final public class Translator {

	private List<Properties> loadedTranslations;

	/**
	 * Initializes the translator with a configuration.
	 * 
	 * @param configuration The translator configuration.
	 * @param loader        The loaded that will be used to find the translation
	 *                      files.
	 */
	public Translator(TranslatorConfiguration configuration, ClassLoader loader) {
		loadedTranslations = new ArrayList<Properties>();
		String language = configuration.getForcedLanguageCode();
		if (language == null) {
			language = Locale.getDefault().getLanguage();
		}
		while (language != null) {
			String path = configuration.getFilePathFromLanguage(language);
			if (path != null) {
				InputStream input = loader.getResourceAsStream(path);
				if (input != null) {
					boolean canAdd = true;
					Properties translation = new Properties();
					try {
						translation.load(new InputStreamReader(input, Charset.forName("UTF-8")));
					} catch (IOException e) {
						canAdd = false;
					}
					try {
						input.close();
					} catch (IOException e) {

					}
					if (canAdd) {
						loadedTranslations.add(translation);
					}
				}
			}
			String fallbackLanguage = configuration.findFallback(language);
			if (!language.equals(fallbackLanguage)) {
				language = fallbackLanguage;
			} else {
				break;
			}
		}

	}

	/**
	 * Returns the translation taken from a key.
	 * 
	 * @param key      The translation key.
	 * @param textArgs These arguments will replace the ${0}, ${1} and so on...
	 * @return The translated text.
	 */
	public String getTranslation(String key, String... textArgs) {
		String loaded = null;
		for (Properties props : loadedTranslations) {
			loaded = props.getProperty(key);
			if (loaded != null) {
				break;
			}
		}
		if (loaded == null) {
			return key;
		}
		for (int i = 0; i < textArgs.length; i++) {
			loaded = loaded.replace("${" + i + "}", textArgs[i]);
		}
		return loaded;
	}

}
