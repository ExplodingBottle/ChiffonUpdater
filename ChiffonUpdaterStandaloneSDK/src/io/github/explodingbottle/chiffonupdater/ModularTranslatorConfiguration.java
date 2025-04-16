/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

/**
 * This implementation of the translation configuration will allow to configure
 * easily your translator
 * 
 * @author ExplodingBottle
 *
 */
public class ModularTranslatorConfiguration implements TranslatorConfiguration {

	private static final String FALLBACK_LANGUAGE = "en";

	private String pathTemplate;

	/**
	 * Constructs an instance of the configuration for a path template.
	 * 
	 * @param pathTemplate Must contain the ${lang} placeholder that will be
	 *                     replaced.<br>
	 *                     Example: translations/${lang}.properties
	 */
	public ModularTranslatorConfiguration(String pathTemplate) {
		this.pathTemplate = pathTemplate;
	}

	@Override
	public String getFilePathFromLanguage(String languageCode) {
		return pathTemplate.replace("${lang}", languageCode);
	}

	@Override
	public String findFallback(String languageCode) {
		if (FALLBACK_LANGUAGE.equals(languageCode)) {
			return null;
		} else {
			return FALLBACK_LANGUAGE;
		}
	}

	@Override
	public String getForcedLanguageCode() {
		return null;
	}

}
