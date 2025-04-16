/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

// NOTE: This file might need a bit of cleaning
public class UpdateProcessor extends Thread {

	private UpdaterState state;
	private Properties updateProperties;
	private DetectionResult productToUpdate;
	private SharedLogger logger;
	private CommandParser commandParser;
	private FunctionsGatherer functions;
	private UpdateInterface uInterface;
	private List<Properties> detectionFiles;
	private File customModule;
	private boolean cancelRemoveDbChainFile;
	private String customModuleClass;
	private long updReleaseDate;
	private String updDescription;

	private Translator translator;

	private boolean prereqOK;

	private File productUpdateDb;

	private static final String COMPONENT_NAME = "UPDP";

	public UpdateProcessor(UpdaterState state, Properties updateProperties, List<Properties> detectionFiles,
			DetectionResult productToUpdate, SharedLogger logger, FunctionsGatherer functions,
			UpdateInterface uInterface, File customModule, String customModuleClass, long updReleaseDate,
			String updDescription) {
		this.detectionFiles = detectionFiles;
		this.state = state;
		this.updateProperties = updateProperties;
		this.productToUpdate = productToUpdate;
		this.commandParser = new CommandParser();
		this.logger = logger;
		this.functions = functions;
		this.uInterface = uInterface;
		this.customModule = customModule;
		this.customModuleClass = customModuleClass;
		this.updReleaseDate = updReleaseDate;
		this.updDescription = updDescription;

		this.prereqOK = true;

		cancelRemoveDbChainFile = false;

		translator = PackageMain.getTranslator();
	}

	public void markPrereqCheckFailed() {
		prereqOK = false;
	}

	private void updatePBar(int currAdv, int maxCmds) {
		uInterface.onNewPercentage(Math.round((currAdv * 100) / maxCmds));
	}

	private void filewalk(File root, List<File> files) {
		if (root != null) {
			files.add(0, root);
			for (File file : root.listFiles()) {
				if (file.isDirectory()) {
					filewalk(file, files);
				} else {
					files.add(0, file);
				}
			}
		}

	}

	private void cancelAll(RollbackRegister register, File updateFolder, boolean mustReRegister) {
		uInterface.onNewUpdateStatus(translator.getTranslation("update.status.cancelling"));
		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.rollback"));
		File backups = new File(updateFolder, "bak_rlbk");
		if (register != null) {
			Properties physProps = register.returnPhysicalBackupRegister();
			int max = physProps.size();
			uInterface.onNewProgressBarIndetermination(false);
			List<String> upsideDownCommands = new ArrayList<String>();
			for (int iter = 0; iter < physProps.size(); iter++) {
				upsideDownCommands.add(0, physProps.getProperty("rollback.instructions.instruction" + iter));
			}
			for (int i = 0; i < upsideDownCommands.size(); i++) {
				if (upsideDownCommands.get(i) != null) {
					FileCommand command = commandParser.parseFileFunctionFromString(upsideDownCommands.get(i));
					if (command != null) {
						if (functions.isFileFunctionPresent(command.getCommand())) {
							uInterface.onNewDetailsAvailable(functions.getFileFunctionUserFriendlyDetails(
									command.getCommand(), command.getCommandArguments()));
							if (!functions.runFileFunction(command.getCommand(), productToUpdate.getRootProductFolder(),
									backups, command.getCommandArguments())) {
								logger.log(COMPONENT_NAME, LogLevel.WARNING,
										"Failed rollback command (might be not important): "
												+ upsideDownCommands.get(i));
							}
						} else {
							logger.log(COMPONENT_NAME, LogLevel.WARNING,
									"Unknown command on rollback file: " + upsideDownCommands.get(i));
						}
					}
				}
				updatePBar(i, max);
			}

		}
		if (updateFolder != null) {
			uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.deleting"));
			uInterface.onNewProgressBarIndetermination(true);
			List<File> toDeleteFiles = new ArrayList<File>();
			filewalk(updateFolder, toDeleteFiles);
			int max = toDeleteFiles.size();
			int cur = 0;
			uInterface.onNewPercentage(0);
			uInterface.onNewProgressBarIndetermination(false);
			for (File toDel : toDeleteFiles) {
				uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.deleting2",
						updateFolder.getParentFile().toPath().relativize(toDel.toPath()).toString()));

				if (!toDel.delete()) {
					logger.log(COMPONENT_NAME, LogLevel.WARNING, "Failed to delete " + toDel.getAbsolutePath());
				}
				cur++;
				updatePBar(cur, max);
			}
		}
		if (mustReRegister) {
			uInterface.onNewProgressBarIndetermination(true);
			uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.reregister"));
			List<File> filesToAdd = new ArrayList<File>();
			filesToAdd.add(productToUpdate.getReportingFilePath());
			state.getProductsListManager().update(filesToAdd, null);
		}
		if (cancelRemoveDbChainFile) {
			File updChainFile = new File(productUpdateDb, HardcodedValues.returnUpdateChainFileName());
			updChainFile.delete();
		}
		uInterface.onUpdateDone(false);

	}

	private boolean tryAndCatch(Supplier<Boolean> action, String errorMessage) {
		boolean lastResult = action.get();
		while (!lastResult) {
			if (uInterface.onError(errorMessage)) {
				lastResult = action.get();
			} else {
				return false;
			}
		}
		return true;
	}

	public void run() {
		uInterface.onNewUpdateStatus(translator.getTranslation("update.status.prereq"));
		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.prereq"));
		uInterface.onNewProgressBarIndetermination(true);
		File productRoot = productToUpdate.getRootProductFolder();

		productUpdateDb = new File(productRoot, HardcodedValues.returnProductSpecificUpdateFolderName());
		if (!productUpdateDb.exists()) {
			logger.log(COMPONENT_NAME, LogLevel.INFO, "Must create a database folder");
			if (!tryAndCatch(() -> {
				return productUpdateDb.mkdir();
			}, translator.getTranslation("update.error.dbffail"))) {
				logger.log(COMPONENT_NAME, LogLevel.ERROR, "Failed create the database folder");
				uInterface.onUpdateDone(false);
				return;
			}
		}

		File updateFolder = new File(productUpdateDb, updateProperties.getProperty("update.targetversion"));
		if (!tryAndCatch(() -> {
			return !updateFolder.exists();
		}, translator.getTranslation("update.error.uvaexist"))) {
			logger.log(COMPONENT_NAME, LogLevel.ERROR, "Update folder is already present");
			uInterface.onUpdateDone(false);
			return;
		}
		if (!tryAndCatch(() -> {
			return updateFolder.mkdir();
		}, translator.getTranslation("update.error.uvafail"))) {
			logger.log(COMPONENT_NAME, LogLevel.ERROR, "Update folder cannot be created");
			uInterface.onUpdateDone(false);
			return;
		}

		File backupFolder = new File(updateFolder, "bak_rlbk");
		if (!tryAndCatch(() -> {
			return backupFolder.mkdir();
		}, translator.getTranslation("update.error.mkbkerr"))) {
			logger.log(COMPONENT_NAME, LogLevel.ERROR, "Backup folder cannot be created");
			uInterface.onUpdateDone(false);
			return;
		}
		File rollbackRegister = new File(updateFolder, HardcodedValues.returnRollbackFileName());
		if (!tryAndCatch(() -> {
			return !rollbackRegister.exists();
		}, translator.getTranslation("update.error.rolbkex"))) {
			logger.log(COMPONENT_NAME, LogLevel.ERROR, "Rollback properties file already exists");
			uInterface.onUpdateDone(false);
			return;
		}

		if (state.isUpdateCancelled()) {
			cancelAll(null, updateFolder, false);
			return;
		}

		logger.log(COMPONENT_NAME, LogLevel.INFO, "Running prerequisites check");
		List<DetectionCommand> prereqCommands = new ArrayList<DetectionCommand>();
		List<FileCommand> fileCommands = new ArrayList<FileCommand>();
		Map<Integer, FileCommand> unorderedCmds = new HashMap<Integer, FileCommand>();
		updateProperties.forEach((_key, _value) -> {
			String key = (String) _key, value = (String) _value;
			boolean isKeyCompatible = false;
			int cmdOrder = -1;

			for (String feature : productToUpdate.getProductFeatures()) {
				if (key.startsWith("update." + feature + "." + productToUpdate.getVersion() + ".")) {
					isKeyCompatible = true;
					cmdOrder = Integer
							.parseInt(key.replace("update." + feature + "." + productToUpdate.getVersion() + ".", ""));
					break;
				}
			}
			if (isKeyCompatible) {
				FileCommand command = commandParser.parseFileFunctionFromString(value);
				if (command != null) {
					if (functions.isFileFunctionPresent(command.getCommand())) {
						// fileCommands.add(command);
						unorderedCmds.put(cmdOrder, command);
					} else {
						logger.log(COMPONENT_NAME, LogLevel.WARNING,
								"Unknown file command on line: " + key + "=" + value);
						uInterface.onUnrecoverableError(
								translator.getTranslation("update.error.unkcmd", key + "=" + value));
						markPrereqCheckFailed();
					}
				} else {
					logger.log(COMPONENT_NAME, LogLevel.WARNING, "Failed to parse file command: " + key + "=" + value);
					uInterface.onUnrecoverableError(
							translator.getTranslation("update.error.commandparseerr", key + "=" + value));
					markPrereqCheckFailed();
				}
			}
			if (key.startsWith("update.prerequisites.")) {
				DetectionCommand command = commandParser.parseDetectionCommandFromString(value);
				if (command != null) {
					if (functions.isDetectionFunctionPresent(command.getCommand())) {
						prereqCommands.add(command);
					} else {
						logger.log(COMPONENT_NAME, LogLevel.WARNING,
								"Unknown prerequisite command on line: " + key + "=" + value);
						uInterface.onUnrecoverableError(
								translator.getTranslation("update.error.unkcmd", key + "=" + value));
						markPrereqCheckFailed();
					}
				} else {
					logger.log(COMPONENT_NAME, LogLevel.WARNING,
							"Failed to parse prerequisite command: " + key + "=" + value);
					uInterface.onUnrecoverableError(
							translator.getTranslation("update.error.commandparseerr", key + "=" + value));
					markPrereqCheckFailed();
				}
			}
		});
		if (!prereqOK) {
			cancelAll(null, updateFolder, false);
			return;
		}
		if (state.isUpdateCancelled()) {
			cancelAll(null, updateFolder, false);
			return;
		}
		{
			int i = 0;
			do {
				FileCommand cmd = unorderedCmds.get(i);
				if (cmd != null) {
					fileCommands.add(cmd);
					unorderedCmds.remove(i);
				}
				i++;
			} while (!unorderedCmds.isEmpty());
		}
		int maxCmds = prereqCommands.size() + fileCommands.size();
		int currAdv = 0;
		uInterface.onNewProgressBarIndetermination(false);
		for (DetectionCommand comm : prereqCommands) {
			if (!tryAndCatch(() -> {
				uInterface.onNewDetailsAvailable(functions.getDetectionFunctionUserFriendlyDetails(comm.getCommand(),
						comm.getCommandArguments()));
				String detectionRes = functions.runDetectionFunction(comm.getCommand(), productRoot,
						comm.getCommandArguments());
				if (detectionRes == null && !comm.getExpectedResult().isEmpty()) {
					return false;
				}
				if (!detectionRes.equals(comm.getExpectedResult())) {
					return false;
				}
				return true;
			}, translator.getTranslation("update.error.prereq"))) {
				logger.log(COMPONENT_NAME, LogLevel.WARNING,
						"Prerequisite command failed. Update Processor will stop.");
				cancelAll(null, updateFolder, false);
				return;
			}

			if (state.isUpdateCancelled()) {
				cancelAll(null, updateFolder, false);
				return;
			}
			currAdv++;
			updatePBar(currAdv, maxCmds);
		}

		currAdv = 0;
		updatePBar(currAdv, maxCmds);
		List<FileCommand> newCommandsList = new ArrayList<FileCommand>();
		for (FileCommand cmd : fileCommands) {
			newCommandsList.add(cmd);
		}
		// for (int i = fileCommands.size() - 1; i >= 0; i--) {
		for (int i = 0; i < fileCommands.size(); i++) {
			FileCommand command = fileCommands.get(i);
			uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.verappli",
					functions.getFileFunctionUserFriendlyDetails(command.getCommand(), command.getCommandArguments())));
			List<String> commandsToCheck = new ArrayList<String>();
			for (FileCommand cmd : newCommandsList) {
				if (cmd != command) {
					commandsToCheck.add(cmd.getRawCommand());
				}
			}
			/*
			 * InterFeatureApplicabilityCheckStatus result =
			 * functions.runFileFunctionInterFeatureApplicabilityCheck(
			 * command.getCommand(), commandsToCheck, command.getCommandArguments());
			 */
			InterFeatureApplicabilityCheckStatus result = functions
					.computeInterFeatureApplicability(command.getRawCommand(), commandsToCheck);
			if (result == InterFeatureApplicabilityCheckStatus.SKIP) {
				newCommandsList.remove(command);
				logger.log(COMPONENT_NAME, LogLevel.INFO, "Command skipped: " + command.getCommand() + ";"
						+ String.join(";", command.getCommandArguments()));
			} else if (result == InterFeatureApplicabilityCheckStatus.SKIP_CHECK) {
				logger.log(COMPONENT_NAME, LogLevel.INFO, "Command check skipped: " + command.getCommand() + ";"
						+ String.join(";", command.getCommandArguments()));

				command.setCheckSkipped(true);
			} else if (result == InterFeatureApplicabilityCheckStatus.FAIL) {
				uInterface.onUnrecoverableError(translator.getTranslation("update.error.verappli", functions
						.getFileFunctionUserFriendlyDetails(command.getCommand(), command.getCommandArguments())));
				cancelAll(null, updateFolder, false);
				return;
			}
			currAdv++;
			updatePBar(currAdv, maxCmds);

		}
		currAdv = 0;
		updatePBar(currAdv, maxCmds);
		for (FileCommand comm : newCommandsList) {

			uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.veroper",
					functions.getFileFunctionUserFriendlyDetails(comm.getCommand(), comm.getCommandArguments())));

			if (!tryAndCatch(() -> {
				if (comm.isCheckSkipped()) {
					return true;
				}
				return functions.runFileFunctionPreCheck(comm.getCommand(), productRoot, state.getUpdateRoot(),
						comm.getCommandArguments());
			}, translator.getTranslation("update.error.veroper",
					functions.getFileFunctionUserFriendlyDetails(comm.getCommand(), comm.getCommandArguments())))) {
				cancelAll(null, updateFolder, false);
				return;
			}

			if (state.isUpdateCancelled()) {
				cancelAll(null, updateFolder, false);
				return;
			}
			currAdv++;
			updatePBar(currAdv, maxCmds);
		}

		logger.log(COMPONENT_NAME, LogLevel.INFO, "Prerequisite checks are all done.");

		logger.log(COMPONENT_NAME, LogLevel.INFO, "Now in backup phase...");

		uInterface.onNewUpdateStatus(translator.getTranslation("update.status.backup"));
		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.backup"));
		uInterface.onNewPercentage(0);

		Properties rollbackProperties = new Properties();
		RollbackRegister register = new RollbackRegister(rollbackProperties, productToUpdate.getProductFeatures());

		maxCmds = newCommandsList.size();
		currAdv = 0;

		for (FileCommand comm : newCommandsList) {
			uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.cancelop",
					functions.getFileFunctionUserFriendlyDetails(comm.getCommand(), comm.getCommandArguments())));

			if (!tryAndCatch(() -> {
				return functions.runFileFunctionBackup(comm.getCommand(), productRoot, backupFolder, register,
						comm.getCommandArguments());
			}, translator.getTranslation("update.error.cancelop",
					functions.getFileFunctionUserFriendlyDetails(comm.getCommand(), comm.getCommandArguments())))) {
				logger.log(COMPONENT_NAME, LogLevel.ERROR, "Backup failed !");
				cancelAll(null, updateFolder, false);
				return;
			}

			if (state.isUpdateCancelled()) {
				cancelAll(null, updateFolder, false);
				return;
			}
			currAdv++;
			updatePBar(currAdv, maxCmds);
		}

		uInterface.onNewPercentage(0);
		uInterface.onNewProgressBarIndetermination(true);
		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.ccustmodule"));

		if (!tryAndCatch(() -> {
			if (customModule != null) {
				try {
					Files.copy(customModule.toPath(), new File(updateFolder, "custom.jar").toPath());
				} catch (IOException e) {
					logger.log(COMPONENT_NAME, LogLevel.ERROR,
							"Backup failed, impossible to save the backup custom module");
					return false;
				}
			}
			return true;
		}, translator.getTranslation("update.error.ccustmodule"))) {
			logger.log(COMPONENT_NAME, LogLevel.ERROR, "Failed to copy the custom module.");
			cancelAll(null, updateFolder, false);
			return;
		}

		if (state.isUpdateCancelled()) {
			cancelAll(null, updateFolder, false);
			return;
		}

		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.pathcfgc"));

		if (!tryAndCatch(() -> {
			try {
				Files.copy(new File(state.getUpdateRoot(), "provider.properties").toPath(),
						new File(productUpdateDb, "provider.properties").toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.log(COMPONENT_NAME, LogLevel.ERROR,
						"Backup failed, impossible to copy the path configuration file");
				return false;
			}
			return true;
		}, translator.getTranslation("update.error.pathcfgc"))) {
			logger.log(COMPONENT_NAME, LogLevel.ERROR, "Failed to copy the custom module.");
			cancelAll(null, updateFolder, false);
			return;
		}

		if (state.isUpdateCancelled()) {
			cancelAll(null, updateFolder, false);
			return;
		}

		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.rewdetec"));

		List<String> detectionFilesNames = new ArrayList<String>();
		if (detectionFiles != null) {
			int[] icop = new int[1];
			for (int iter = 0; iter < detectionFiles.size(); iter++) {
				icop[0] = iter;
				if (!tryAndCatch(() -> {
					String dfname = "det" + icop[0] + ".properties";
					detectionFilesNames.add(dfname);
					File detecFile = new File(updateFolder, dfname);
					try (FileOutputStream fos = new FileOutputStream(detecFile)) {
						detectionFiles.get(icop[0]).store(fos,
								"Detection file " + icop[0] + ": " + productToUpdate.getProductName() + "@"
										+ productToUpdate.getVersion() + "\r\nDO NOT EDIT!");
					} catch (IOException e) {
						return false;
					}
					return true;
				}, translator.getTranslation("update.error.rewdetec"))) {
					logger.log(COMPONENT_NAME, LogLevel.ERROR, "Failed to rewrite a detection configuration.");
					cancelAll(null, updateFolder, false);
					return;
				}
			}
			register.setDetectionFilesList(detectionFilesNames);
		}

		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.storrlbkc"));

		if (customModuleClass != null) {
			register.setCustomModuleClass(customModuleClass);
		}
		register.setInstallationDate(System.currentTimeMillis());
		register.setUpdateReleaseDate(updReleaseDate);
		if (updDescription != null) {
			register.setUpdateDescription(updDescription);
		}

		if (!tryAndCatch(() -> {
			try (FileOutputStream fos = new FileOutputStream(rollbackRegister)) {
				rollbackProperties.store(fos, "Rollback File: " + productToUpdate.getProductName() + "@"
						+ productToUpdate.getVersion() + "\r\nDO NOT EDIT!");
			} catch (IOException e) {
				return false;
			}
			return true;
		}, translator.getTranslation("update.error.storrlbkc"))) {
			logger.log(COMPONENT_NAME, LogLevel.ERROR, "Failed to write the rollback register.");
			cancelAll(null, updateFolder, false);
			return;
		}

		if (state.isUpdateCancelled()) {
			cancelAll(null, updateFolder, false);
			return;
		}

		logger.log(COMPONENT_NAME, LogLevel.INFO, "Now in update phase...");
		uInterface.onNewUpdateStatus(translator.getTranslation("update.status.installing"));
		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.installing"));
		uInterface.onNewProgressBarIndetermination(false);
		maxCmds = newCommandsList.size();
		currAdv = 0;
		for (FileCommand comm : newCommandsList) {
			uInterface.onNewDetailsAvailable(
					functions.getFileFunctionUserFriendlyDetails(comm.getCommand(), comm.getCommandArguments()));

			if (!tryAndCatch(() -> {
				return functions.runFileFunction(comm.getCommand(), productRoot, state.getUpdateRoot(),
						comm.getCommandArguments());
			}, translator.getTranslation("update.error.installing",
					functions.getFileFunctionUserFriendlyDetails(comm.getCommand(), comm.getCommandArguments())))) {
				cancelAll(register, updateFolder, false);
				return;
			}

			if (state.isUpdateCancelled()) {
				cancelAll(register, updateFolder, false);
				return;
			}
			currAdv++;
			updatePBar(currAdv, maxCmds);
		}

		logger.log(COMPONENT_NAME, LogLevel.INFO, "Update phase done ! Finishing program registration...");

		uInterface.onNewProgressBarIndetermination(true);
		uInterface.onNewUpdateStatus(translator.getTranslation("update.status.finishing"));
		uInterface.onNewDetailsAvailable(translator.getTranslation("update.details.finishing"));
		List<File> toAdd = new ArrayList<File>();
		for (Properties detectionFile : detectionFiles) {
			String productName = detectionFile.getProperty("detection.productname");
			if (productName != null && productToUpdate.getProductName().equals(productName)) {
				detectionFile.forEach((_key, _value) -> {
					String key = (String) _key, value = (String) _value;
					String sectionStart = "detection.section.";
					if (key.startsWith(sectionStart)) {
						String sectionName = key.substring(sectionStart.length());
						DetectionSection section = new DetectionSection(sectionName, value);
						if (updateProperties.getProperty("update.targetversion").equals(value)) {
							File reportingFile = new File(productToUpdate.getRootProductFolder(),
									detectionFile.getProperty(section.getSectionId() + ".reportingfile"));
							logger.log(COMPONENT_NAME, LogLevel.INFO,
									"Reporting file will be: " + reportingFile.getAbsolutePath());
							toAdd.add(reportingFile);
						}

					}
				});
			}
		}
		if (state.isUpdateCancelled()) {
			cancelAll(register, updateFolder, false);
			return;
		}
		state.getProductsListManager().update(toAdd, null);
		if (state.isUpdateCancelled()) {
			cancelAll(register, updateFolder, true);
			return;
		}

		String uninstFileName = updateProperties.getProperty("update.uninstaller.name");
		if (uninstFileName != null && !uninstFileName.isEmpty()) {
			tryAndCatch(() -> {
				try {
					File jarFile = new File(
							PackageMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
					if (jarFile.exists() && jarFile.canRead() && jarFile.isFile()) {
						File uninstJar = new File(productRoot, uninstFileName);
						Files.copy(jarFile.toPath(), uninstJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
						return true;
					}
				} catch (Exception e) {
				}
				return false;
			}, translator.getTranslation("update.error.uninstcp"));
		}

		tryAndCatch(() -> {
			return PackageMain.getCustomizationEngine().copyCustomizationToUninstallDatabase(productUpdateDb);
		}, translator.getTranslation("update.error.custocp"));

		logger.log(COMPONENT_NAME, LogLevel.INFO, "Updating updates chain.");

		File updChainFile = new File(productUpdateDb, HardcodedValues.returnUpdateChainFileName());
		UpdateChainManager mgrUpdChain = new UpdateChainManager(updChainFile);
		if (updChainFile.exists()) {
			List<String> versionsChain = new ArrayList<String>();
			if (!tryAndCatch(() -> {
				return mgrUpdChain.getVersionsChain(versionsChain);
			}, translator.getTranslation("update.error.obtuchain"))) {
				cancelAll(register, updateFolder, true);
				return;
			}
			versionsChain.add(updateProperties.getProperty("update.targetversion"));
			if (!tryAndCatch(() -> {
				return mgrUpdChain.updateChain(versionsChain);
			}, translator.getTranslation("update.error.upduchain"))) {
				cancelAll(register, updateFolder, true);
				return;
			}

		} else {
			cancelRemoveDbChainFile = true;

			List<String> versionsChain = new ArrayList<String>();
			versionsChain.add(productToUpdate.getVersion());
			versionsChain.add(updateProperties.getProperty("update.targetversion"));

			if (!tryAndCatch(() -> {
				return mgrUpdChain.updateChain(versionsChain);
			}, translator.getTranslation("update.error.upduchain"))) {
				cancelAll(register, updateFolder, true);
				return;
			}
		}

		logger.log(COMPONENT_NAME, LogLevel.INFO, "Everything finished.");
		uInterface.onUpdateDone(true);

	}

}
