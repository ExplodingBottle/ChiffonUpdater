/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.cudemo;

import java.io.File;

import io.github.explodingbottle.chiffonupdater.DetectionFunction;
import io.github.explodingbottle.chiffonupdater.Translator;

public class CUDemoReturnTrue implements DetectionFunction {

	private Translator translator;

	public CUDemoReturnTrue(Translator translator) {
		this.translator = translator;
	}

	@Override
	public String runDetection(File productRoot, String[] arguments) {
		return "true";
	}

	@Override
	public String getUserFriendlyCommandDetail(String[] arguments) {
		return translator.getTranslation("test");
	}

}
