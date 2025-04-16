/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class StandaloneUpdaterFunctionsModule implements FunctionsPublisher {

	public final static Translator staticModuleTranslator = new Translator(
			new ModularTranslatorConfiguration("static/${lang}.properties"),
			StandaloneUpdaterFunctionsModule.class.getClassLoader());

	private SharedLogger logger;

	public StandaloneUpdaterFunctionsModule(SharedLogger logger) {
		this.logger = logger;
	}

	@Override
	public void publishModuleFunctions(FunctionsGatherer gatherer) {
		gatherer.registerDetectionFunction(StandalonePackageCommandNames.FILE_HASH_COMMAND,
				new FileHashDetectionFunction(logger));
		gatherer.registerDetectionFunction(StandalonePackageCommandNames.FILE_EXISTS_COMMAND,
				new FileExistsDetectionFunction());
		gatherer.registerFileFunction(StandalonePackageCommandNames.FOLDER_CREATE_COMMAND,
				new FolderCreateFileFunction());
		gatherer.registerFileFunction(StandalonePackageCommandNames.FILE_COPY_COMMAND, new FileCopyFileFunction());
		gatherer.registerFileFunction(StandalonePackageCommandNames.FILE_DELETE_COMMAND, new FileDeleteFileFunction());
		gatherer.registerInterFeatureApplicabilityComputer(new PackageIfaComputer());
	}
}
