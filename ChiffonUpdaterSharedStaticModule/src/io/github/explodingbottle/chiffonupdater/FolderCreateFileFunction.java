/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

//<new folder path>
public class FolderCreateFileFunction implements FileFunction {

	private Translator translator;

	public FolderCreateFileFunction() {
		translator = StandaloneUpdaterFunctionsModule.staticModuleTranslator;
	}

	@Override
	public boolean runOperation(File productRoot, File updateRoot, String[] arguments) {
		if (arguments.length == 1) {
			File folderToCreate = new File(productRoot, arguments[0]);
			if (folderToCreate.mkdir()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean backupOperation(File productRoot, File backupFolder, RollbackRegister rollbackRegister,
			String[] arguments) {
		if (arguments.length == 1) {
			File folderToCreate = new File(productRoot, arguments[0]);
			if (folderToCreate.exists()) {
				return false;
			}
			rollbackRegister
					.addRollbackInstruction(StandalonePackageCommandNames.FILE_DELETE_COMMAND + ";" + arguments[0]);
			return true;
		}
		return false;
	}

	@Override
	public boolean preActionCheck(File productRoot, File updateRoot, String[] arguments) {
		if (arguments.length == 1) {
			File folderToCreate = new File(productRoot, arguments[0]);
			File parent = folderToCreate.getParentFile();
			if (!folderToCreate.exists() && parent != null && parent.exists() && parent.isDirectory()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getUserFriendlyCommandDetail(String[] arguments) {
		return translator.getTranslation("operation.mkdir", arguments[0]);
	}

}
