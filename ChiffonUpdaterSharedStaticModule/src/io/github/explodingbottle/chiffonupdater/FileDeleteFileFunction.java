/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// <filename>
public class FileDeleteFileFunction implements FileFunction {

	private Translator translator;

	public FileDeleteFileFunction() {
		translator = StandaloneUpdaterFunctionsModule.staticModuleTranslator;
	}

	@Override
	public boolean runOperation(File productRoot, File updateRoot, String[] arguments) {
		if (arguments.length == 1) {
			File toDelete = new File(productRoot, arguments[0]);
			return toDelete.delete();
		}
		return false;
	}

	@Override
	public boolean backupOperation(File productRoot, File backupFolder, RollbackRegister rollbackRegister,
			String[] arguments) {
		if (arguments.length == 1) {
			File toDelete = new File(productRoot, arguments[0]);
			if (toDelete.isDirectory()) {
				rollbackRegister.addRollbackInstruction(
						StandalonePackageCommandNames.FOLDER_CREATE_COMMAND + ";" + arguments[0]);
				return true;
			} else {
				String nextFileName = rollbackRegister.getNextBackupFileName();
				File toBackup = new File(backupFolder, nextFileName);
				if (toBackup.exists()) {
					return false;
				}
				if (toDelete.exists()) {
					try {
						Files.copy(toDelete.toPath(), toBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
						rollbackRegister.addRollbackInstruction(StandalonePackageCommandNames.FILE_COPY_COMMAND + ";"
								+ nextFileName + ";" + arguments[0]);

					} catch (IOException e) {
						return false;
					}
					return true;
				}
			}

		}
		return false;
	}

	@Override
	public boolean preActionCheck(File productRoot, File updateRoot, String[] arguments) {
		if (arguments.length < 1) {
			return false;
		}

		return new File(productRoot, arguments[0]).canWrite();
	}

	@Override
	public String getUserFriendlyCommandDetail(String[] arguments) {
		return translator.getTranslation("operation.delete", arguments[0]);
	}

}
