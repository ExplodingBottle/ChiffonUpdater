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

// <source>;<destination>
public class FileCopyFileFunction implements FileFunction {

	private Translator translator;

	public FileCopyFileFunction() {
		translator = StandaloneUpdaterFunctionsModule.staticModuleTranslator;
	}

	@Override
	public boolean runOperation(File productRoot, File updateRoot, String[] arguments) {
		if (arguments.length == 2) {
			File toCopy = new File(updateRoot, arguments[0]);
			File output = new File(productRoot, arguments[1]);
			try {
				Files.copy(toCopy.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean backupOperation(File productRoot, File backupFolder, RollbackRegister rollbackRegister,
			String[] arguments) {
		if (arguments.length == 2) {
			File toCopy = new File(productRoot, arguments[1]);

			if (toCopy.exists()) {
				String nextFileName = rollbackRegister.getNextBackupFileName();
				File toBackup = new File(backupFolder, nextFileName);
				if (toBackup.exists()) {
					return false;
				}
				// be rolled back
				try {
					Files.copy(toCopy.toPath(), toBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
					rollbackRegister.addRollbackInstruction(
							StandalonePackageCommandNames.FILE_COPY_COMMAND + ";" + nextFileName + ";" + arguments[1]);

				} catch (IOException e) {
					return false;
				}
				return true;
			} else {
				rollbackRegister
						.addRollbackInstruction(StandalonePackageCommandNames.FILE_DELETE_COMMAND + ";" + arguments[1]);
				return true;
			}

		}
		return false;
	}

	@Override
	public boolean preActionCheck(File productRoot, File updateRoot, String[] arguments) {
		File source = new File(updateRoot, arguments[0]);
		if (!source.exists() || !source.canRead()) {
			return false;
		}
		if (arguments.length == 2) {
			File dest = new File(productRoot, arguments[1]);
			if (dest.exists()) {
				return dest.canWrite();
			} else {
				File parent = dest.getParentFile();
				if (parent != null && parent.exists() && parent.isDirectory()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getUserFriendlyCommandDetail(String[] arguments) {
		return translator.getTranslation("operation.copy", arguments[0], arguments[1]);
	}

}
