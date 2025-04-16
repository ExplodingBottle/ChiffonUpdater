/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

public class UninstallProcessor extends Thread {

	private UninstallInterface uInterface;
	private SharedLogger logger;
	private String targetVersion;
	private List<String> versionsChain;
	private File productRoot;

	private static final String LOGGER_COMPONENT = "UNSP";

	public UninstallProcessor(UninstallInterface uInterface, String targetVersion, SharedLogger logger,
			List<String> versionsChain, File productRoot) {
		this.uInterface = uInterface;
		this.logger = logger;
		this.targetVersion = targetVersion;
		this.versionsChain = versionsChain;
		this.productRoot = productRoot;
	}

	private boolean tryAndCatch(Supplier<Boolean> action, String errorMessage) {
		boolean lastResult = action.get();
		while (!lastResult) {
			if (uInterface.onRecoverableError(errorMessage)) {
				lastResult = action.get();
			} else {
				return false;
			}
		}
		return true;
	}

	private void filewalk(File root, List<File> files) {
		files.add(0, root);
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				filewalk(file, files);
			} else {
				files.add(0, file);
			}
		}
	}

	public void run() {
		Translator translator = PackageMain.getTranslator();

		logger.log(LOGGER_COMPONENT, LogLevel.INFO, "Started downgrade. Target version: " + targetVersion);
		uInterface.onNewUninstallState(UninstallState.INSPECTING_CONFIGURATION);
		List<String> downgradeOrdered = new ArrayList<String>();
		String currentVersion = versionsChain.get(versionsChain.size() - 1);
		for (int i = versionsChain.size() - 1; i >= 0; i--) {
			String vers = versionsChain.get(i);
			if (vers.equals(targetVersion)) {
				break;
			}
			downgradeOrdered.add(vers);
		}
		CommandParser commandParser = new CommandParser();

		logger.log(LOGGER_COMPONENT, LogLevel.INFO, "Rollback order: " + String.join(", ", downgradeOrdered));
		File dbFolder = new File(productRoot, HardcodedValues.returnProductSpecificUpdateFolderName());
		if (!dbFolder.exists()) {
			uInterface.onUnrecoverableError(translator.getTranslation("downgrade.nodb"));
			return;
		}

		List<String> currentFeatures = null;
		List<Properties> detectionFiles = null;
		Map<String, FunctionsGatherer> gatherers = new HashMap<String, FunctionsGatherer>();
		Map<String, List<FileCommand>> commandsPerVersion = new HashMap<String, List<FileCommand>>();
		List<URLClassLoader> toCloseLoaders = new ArrayList<URLClassLoader>();

		for (String currentDowngrade : downgradeOrdered) {
			File versFolder = new File(dbFolder, currentDowngrade);
			if (!versFolder.exists()) {
				logger.log(LOGGER_COMPONENT, LogLevel.ERROR, "Could not locate rollback data for " + currentDowngrade);
				uInterface.onUnrecoverableError(translator.getTranslation("downgrade.norbdata", currentDowngrade));
				return;
			}
			PropertiesLoader loader = new PropertiesLoader(versFolder, logger);
			if (!loader.loadAll()) {
				logger.log(LOGGER_COMPONENT, LogLevel.ERROR, "Could not load property files for " + currentDowngrade);
				uInterface.onUnrecoverableError(translator.getTranslation("downgrade.noprops", currentDowngrade));
				return;
			}
			Properties rollbackInfos = loader.getProperties(HardcodedValues.returnRollbackFileName());

			if (rollbackInfos == null) {
				logger.log(LOGGER_COMPONENT, LogLevel.ERROR,
						"Could not load rollback instructions for " + currentDowngrade);
				uInterface.onUnrecoverableError(translator.getTranslation("downgrade.norbinfo", currentDowngrade));
				return;
			}

			List<FileCommand> orderedRollbackCommands = new ArrayList<FileCommand>();
			for (int iter = 0; iter < rollbackInfos.size(); iter++) {
				String rollbackInstruction = rollbackInfos.getProperty("rollback.instructions.instruction" + iter);
				if (rollbackInstruction != null) {
					FileCommand command = commandParser.parseFileFunctionFromString(rollbackInstruction);
					if (command != null) {
						orderedRollbackCommands.add(0, command);
					}
				}
			}
			commandsPerVersion.put(currentDowngrade, orderedRollbackCommands);

			if (currentVersion.equals(currentDowngrade)) {
				String configuredFeaturesText = rollbackInfos.getProperty("rollback.configuration.features");
				String detectionFilesText = rollbackInfos.getProperty("rollback.configuration.detectionfiles");
				if (configuredFeaturesText == null || detectionFilesText == null) {
					logger.log(LOGGER_COMPONENT, LogLevel.ERROR,
							"Invalid rollback instructions for " + currentDowngrade);
					uInterface
							.onUnrecoverableError(translator.getTranslation("downgrade.invalidinsn", currentDowngrade));
					return;
				}
				currentFeatures = Arrays.asList(configuredFeaturesText.split(";"));
				detectionFiles = new ArrayList<Properties>();
				for (String detectionFileName : detectionFilesText.split(";")) {
					Properties loadedDetectionProps = loader.getProperties(detectionFileName);
					if (loadedDetectionProps == null) {
						logger.log(LOGGER_COMPONENT, LogLevel.ERROR,
								"Could not load detection file " + detectionFileName + " for " + currentDowngrade);
						uInterface.onUnrecoverableError(
								translator.getTranslation("downgrade.rbfilefail", detectionFileName, currentDowngrade));
						return;
					} else {
						detectionFiles.add(loadedDetectionProps);
					}
				}
			}
			FunctionsGatherer gatherer = new FunctionsGatherer();
			new StandaloneUpdaterFunctionsModule(logger).publishModuleFunctions(gatherer);
			File customModule = new File(versFolder, "custom.jar");
			if (customModule.exists()) {
				logger.log(LOGGER_COMPONENT, LogLevel.INFO,
						"Will load a custom module for version " + currentDowngrade);
				String moduleClassName = rollbackInfos.getProperty("rollback.configuration.moduleclass");
				if (moduleClassName == null) {
					logger.log(LOGGER_COMPONENT, LogLevel.ERROR,
							"Could not find class name of custom module for " + currentDowngrade);
					uInterface.onUnrecoverableError(translator.getTranslation("downgrade.cmodulecn", currentDowngrade));
					return;
				}
				try {
					URLClassLoader urlClassLoder = new URLClassLoader(new URL[] { customModule.toURI().toURL() });
					toCloseLoaders.add(urlClassLoder);
					Class<?> moduleClass = Class.forName(moduleClassName, true, urlClassLoder);
					if (FunctionsPublisher.class.isAssignableFrom(moduleClass)) {
						FunctionsPublisher publisher = (FunctionsPublisher) moduleClass.newInstance();
						publisher.publishModuleFunctions(gatherer);

					} else {
						logger.log(LOGGER_COMPONENT, LogLevel.ERROR,
								"Could not load a custom module for " + currentDowngrade);
						uInterface.onUnrecoverableError(
								translator.getTranslation("downgrade.cmodulefail", currentDowngrade));
						return;
					}
				} catch (Exception e) {
					logger.log(LOGGER_COMPONENT, LogLevel.ERROR,
							"Could not load a custom module for " + currentDowngrade);
					uInterface
							.onUnrecoverableError(translator.getTranslation("downgrade.cmodulefail", currentDowngrade));
					return;
				}
			}
			gatherers.put(currentDowngrade, gatherer);
		}

		ProductDetector detector = new ProductDetector(logger,
				PackageMain.returnUpdaterState().getProductsListManager(), detectionFiles,
				gatherers.get(currentVersion), productRoot);
		List<DetectionResult> results = detector.detectProducts();

		DetectionResult foundCandidateNotFinal = null;
		for (DetectionResult result : results) {
			if (result.getVersion().equals(currentVersion) && result.getProductFeatures().equals(currentFeatures)) {
				foundCandidateNotFinal = result;
			}
		}
		if (foundCandidateNotFinal == null) {
			logger.log(LOGGER_COMPONENT, LogLevel.ERROR, "No initial candidate for rollback");
			uInterface.onUnrecoverableError(translator.getTranslation("downgrade.modified"));
			return;
		}
		final DetectionResult foundCandidate = foundCandidateNotFinal;

		if (PackageMain.returnUpdaterState().isUpdateCancelled()) {
			return;
		}

		boolean[] canCancelAll = new boolean[1];

		/*
		 * uInterface.onNewUninstallState(UninstallState.CHECKING_PREREQUISITES);
		 * 
		 * commandsPerVersion.forEach((vers, commands) -> {
		 * 
		 * File versFolder = new File(dbFolder, vers); File rollbackFiles = new
		 * File(versFolder, "bak_rlbk");
		 * 
		 * FunctionsGatherer linkedGatherer = gatherers.get(vers);
		 * commands.forEach(command -> { if
		 * (linkedGatherer.isFileFunctionPresent(command.getCommand())) { String
		 * funcUFriendly =
		 * linkedGatherer.getFileFunctionUserFriendlyDetails(command.getCommand(),
		 * command.getCommandArguments()); if (!tryAndCatch(() -> { return
		 * linkedGatherer.runFileFunctionPreCheck(command.getCommand(), productRoot,
		 * rollbackFiles, command.getCommandArguments()); },
		 * "Failed the prerequisite check for: " + funcUFriendly)) {
		 * uInterface.onUninstallInterrupted(); canCancelAll[0] = true; return; } } else
		 * {
		 * uInterface.onUnrecoverableError("One of the file function is not existing");
		 * canCancelAll[0] = true; return; } }); if (canCancelAll[0]) { return; } }); if
		 * (canCancelAll[0]) { return; } if
		 * (PackageMain.returnUpdaterState().isUpdateCancelled()) { return; }
		 */
		uInterface.onNewUninstallState(UninstallState.DOWNGRADING);

		canCancelAll[0] = false;
		downgradeOrdered.forEach(version -> {
			File versFolder = new File(dbFolder, version);
			File rollbackFiles = new File(versFolder, "bak_rlbk");
			FunctionsGatherer linkedGatherer = gatherers.get(version);
			for (FileCommand command : commandsPerVersion.get(version)) {
				String funcUFriendly = linkedGatherer.getFileFunctionUserFriendlyDetails(command.getCommand(),
						command.getCommandArguments());
				if (!tryAndCatch(() -> {
					return linkedGatherer.runFileFunction(command.getCommand(), productRoot, rollbackFiles,
							command.getCommandArguments());
				}, translator.getTranslation("downgrade.opfailed", funcUFriendly))) {
					uInterface.onUninstallInterrupted();
					canCancelAll[0] = true;
					return;
				}
			}
		});

		uInterface.onNewUninstallState(UninstallState.POSTDOWNGRADE);
		toCloseLoaders.forEach(loader -> {
			try {
				loader.close();
			} catch (IOException e) {
				logger.log(LOGGER_COMPONENT, LogLevel.INFO, "Failed to close one of the class loaders.");
			}
		});

		downgradeOrdered.forEach(version -> {
			File versFolder = new File(dbFolder, version);
			List<File> toDeleteFiles = new ArrayList<File>();
			filewalk(versFolder, toDeleteFiles);
			toDeleteFiles.forEach(file -> {
				file.delete();
			});
		});
		UpdateChainManager mgr = new UpdateChainManager(
				new File(dbFolder, HardcodedValues.returnUpdateChainFileName()));
		versionsChain.removeAll(downgradeOrdered);
		mgr.updateChain(versionsChain);

		logger.log(LOGGER_COMPONENT, LogLevel.INFO, "Registering binaries after uninstall.");

		List<File> toAdd = new ArrayList<File>();
		for (Properties detectionFile : detectionFiles) {
			String productName = detectionFile.getProperty("detection.productname");
			if (productName != null && foundCandidate.getProductName().equals(productName)) {
				detectionFile.forEach((_key, _value) -> {
					String key = (String) _key, value = (String) _value;
					String sectionStart = "detection.section.";
					if (key.startsWith(sectionStart)) {
						String sectionName = key.substring(sectionStart.length());
						DetectionSection section = new DetectionSection(sectionName, value);
						if (targetVersion.equals(value)) {
							File reportingFile = new File(foundCandidate.getRootProductFolder(),
									detectionFile.getProperty(section.getSectionId() + ".reportingfile"));
							logger.log(LOGGER_COMPONENT, LogLevel.INFO,
									"Reporting file will be: " + reportingFile.getAbsolutePath());
							toAdd.add(reportingFile);
						}

					}
				});
			}
		}
		PackageMain.returnUpdaterState().getProductsListManager().update(toAdd, null);

		uInterface.onNewUninstallState(UninstallState.FINISHED);
	}

}
