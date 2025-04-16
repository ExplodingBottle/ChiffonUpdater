/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

/**
 * This interface is used by the translator to know where to find translation
 * files.
 * 
 * @author ExplodingBottle
 *
 */
public interface TranslatorConfiguration {

	/**
	 * This function must return the place where you will find your language files
	 * according to the language code.
	 * 
	 * @param languageCode A string corresponding to the language code, for example
	 *                     en or fr.
	 * @return A path to use to get the translation. For example
	 *         translations/en.properties
	 */
	public String getFilePathFromLanguage(String languageCode);

	/**
	 * This function must return a fallback language code from the given language
	 * code.
	 * 
	 * @param languageCode The language code that failed to load.
	 * @return The fallback language code. If null is returned, this will cause the
	 *         translation to be not loaded.
	 */
	public String findFallback(String languageCode);

	/**
	 * This function will return a forced language code.
	 * 
	 * @return A language code or null if we are not forcing a language code.
	 */
	public String getForcedLanguageCode();

}
