/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.cudemo;

import io.github.explodingbottle.chiffonupdater.FunctionsGatherer;
import io.github.explodingbottle.chiffonupdater.FunctionsPublisher;
import io.github.explodingbottle.chiffonupdater.ModularTranslatorConfiguration;
import io.github.explodingbottle.chiffonupdater.Translator;

public class CUDemoModule implements FunctionsPublisher {

	@Override
	public void publishModuleFunctions(FunctionsGatherer gatherer) {
		gatherer.registerDetectionFunction("CUDemo",
				new CUDemoReturnTrue(new Translator(new ModularTranslatorConfiguration("${lang}.properties"),
						this.getClass().getClassLoader())));
	}

}
