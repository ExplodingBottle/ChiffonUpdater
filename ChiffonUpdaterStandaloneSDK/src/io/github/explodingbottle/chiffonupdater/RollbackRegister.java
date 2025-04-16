/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;
import java.util.Properties;

/**
 * This class represents the Rollback Register. Whenever a command will be
 * executed, all the backup commands will be first saved in the Rollback
 * Register in order to cancel the update.
 * 
 * @author ExplodingBottle
 *
 */
final public class RollbackRegister {

	private Properties backupPhysicalRegister;
	private int backupFilesCount;

	RollbackRegister(Properties backupPhysicalRegister, List<String> productFeatures) {
		backupPhysicalRegister.setProperty("rollback.configuration.features", String.join(";", productFeatures));
		this.backupPhysicalRegister = backupPhysicalRegister;
		this.backupFilesCount = 0;
	}

	/**
	 * Add in the register a rollback instruction. It will be executed when
	 * uninstalling the product.
	 * 
	 * @param instruction The rollback instruction
	 */
	public void addRollbackInstruction(String instruction) {
		int keyId = backupPhysicalRegister.size();
		backupPhysicalRegister.setProperty("rollback.instructions.instruction" + keyId, instruction);
	}

	/**
	 * This command is used to provide a name for any backup file. <br>
	 * 
	 * <b>WARNING:</b> Using this command will update the file name iterator.
	 * 
	 * @return The backup file that you should use.
	 */
	public String getNextBackupFileName() {
		String newName = "file" + backupFilesCount + ".bk1";
		backupFilesCount++;
		return newName;
	}

	void setDetectionFilesList(List<String> dFiles) {
		backupPhysicalRegister.setProperty("rollback.configuration.detectionfiles", String.join(";", dFiles));
	}

	void setCustomModuleClass(String customModuleClass) {
		backupPhysicalRegister.setProperty("rollback.configuration.moduleclass", customModuleClass);
	}

	void setInstallationDate(long dateTimestamp) {
		backupPhysicalRegister.setProperty("rollback.configuration.installdate", "" + dateTimestamp);
	}

	void setUpdateReleaseDate(long dateTimestamp) {
		backupPhysicalRegister.setProperty("rollback.configuration.releasedate", "" + dateTimestamp);
	}

	void setUpdateDescription(String description) {
		backupPhysicalRegister.setProperty("rollback.configuration.description", description);
	}

	Properties returnPhysicalBackupRegister() {
		return backupPhysicalRegister;
	}
}
