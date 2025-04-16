/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class ProductDetector {

	private SharedLogger detectorLogger;
	private ProductsListManager manager;
	private List<Properties> detectionFiles;
	private FunctionsGatherer functions;
	private File overridenProductFolder;
	private CommandParser commandParser;

	private static final String COMPONENT_NAME = "PDET";

	public ProductDetector(SharedLogger detectorLogger, ProductsListManager manager, List<Properties> detectionFiles,
			FunctionsGatherer functions, File overridenProductFolder) {
		this.detectorLogger = detectorLogger;
		this.manager = manager;
		this.detectionFiles = detectionFiles;
		this.functions = functions;
		this.overridenProductFolder = overridenProductFolder;

		this.commandParser = new CommandParser();
	}

	public List<DetectionResult> detectProducts() {
		detectorLogger.log(COMPONENT_NAME, LogLevel.INFO,
				"Starting detection with " + detectionFiles.size() + " detection files.");
		Map<File, String> registeredProducts = manager.update(null, null);
		List<DetectionResult> results = new ArrayList<DetectionResult>();
		for (Properties props : detectionFiles) {
			String productName = props.getProperty("detection.productname");
			String featuresNotSplit = props.getProperty("detection.features");
			String[] features = featuresNotSplit.split(";");
			List<DetectionSection> detectionSections = new ArrayList<DetectionSection>();
			props.forEach((_key, _value) -> {
				String key = (String) _key, value = (String) _value;
				String sectionStart = "detection.section.";
				if (key.startsWith(sectionStart)) {
					String sectionName = key.substring(sectionStart.length());
					DetectionSection section = new DetectionSection(sectionName, value);
					detectionSections.add(section);
					detectorLogger.log(COMPONENT_NAME, LogLevel.INFO, section.toString());
				}
			});
			List<DetectionSectionContent> detectionSectionsContent = new ArrayList<DetectionSectionContent>();
			for (DetectionSection detectionSection : detectionSections) {
				Map<String, List<DetectionCommand>> commands = new HashMap<String, List<DetectionCommand>>();
				String[] reportingFileNameContainer = new String[1];
				props.forEach((_key, _value) -> {
					String key = (String) _key, value = (String) _value;
					if (key.equals(detectionSection.getSectionId() + ".reportingfile")) {
						reportingFileNameContainer[0] = value;
					} else if (key.startsWith(detectionSection.getSectionId() + ".")) {
						for (String feature : features) {
							if (key.startsWith(detectionSection.getSectionId() + "." + feature + "")) {
								DetectionCommand parsed = commandParser.parseDetectionCommandFromString(value);
								if (parsed == null) {
									detectorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
											"Invalid command detected: " + key + "=" + value);
								} else {
									if (commands.get(feature) == null) {
										commands.put(feature, new ArrayList<DetectionCommand>());
									}
									commands.get(feature).add(parsed);
								}
							}
						}
					}
				});
				if (reportingFileNameContainer[0] != null) {
					detectionSectionsContent.add(
							new DetectionSectionContent(detectionSection, reportingFileNameContainer[0], commands));
				} else {
					detectorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
							"Detection section is missing a reporting file name.");
				}
			}
			for (DetectionSectionContent detectionSectionContent : detectionSectionsContent) {

				List<DetectionEntry> productRoots = new ArrayList<DetectionEntry>();

				if (overridenProductFolder == null) {
					registeredProducts.forEach((productFile, fileHash) -> {
						Path productFilePath = productFile.toPath();
						Path reportingFilePartialPath = Paths.get(detectionSectionContent.getReportingFileName());
						if (productFilePath.endsWith(reportingFilePartialPath)) {
							File productRoot = productFilePath.getRoot()
									.resolve(productFilePath.subpath(0,
											productFilePath.getNameCount() - reportingFilePartialPath.getNameCount()))
									.toFile();
							productRoots.add(new DetectionEntry(productRoot, productFile));

						}
					});
				} else {
					File productReportedBinary = new File(overridenProductFolder,
							detectionSectionContent.getReportingFileName());
					if (productReportedBinary.exists() && productReportedBinary.isFile()) {
						productRoots.add(new DetectionEntry(overridenProductFolder, productReportedBinary));
					}
				}

				productRoots.forEach(detectionEntry -> {
					File productRoot = detectionEntry.getProductRoot(),
							productFile = detectionEntry.getProductReportedBinary();
					List<String> featuresDiscovered = new ArrayList<String>();
					detectionSectionContent.returnDetectionCommandsListPerFeature().forEach((feature, commands) -> {
						boolean allPassed = true;
						for (DetectionCommand command : commands) {
							if (!functions.isDetectionFunctionPresent(command.getCommand())) {
								detectorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
										"Encountered an unexisting function: " + command.getCommand());
								allPassed = false;
								continue;
							}
							String response = functions.runDetectionFunction(command.getCommand(), productRoot,
									command.getCommandArguments());
							if (response == null) {
								allPassed = false;
							} else if (!response.equals(command.getExpectedResult())) {
								allPassed = false;
							}

						}
						if (allPassed) {
							featuresDiscovered.add(feature);
						}

					});
					if (featuresDiscovered.size() > 0) {

						String version = detectionSectionContent.getParentingSection().getVersion();
						detectorLogger.log(COMPONENT_NAME, LogLevel.INFO,
								"Identified " + productFile.getAbsolutePath() + " as " + productName + " | Features: "
										+ String.join(", ", featuresDiscovered) + " | Version: " + version);
						results.add(new DetectionResult(productName, featuresDiscovered, version, productFile,
								productRoot));
					}
				});
			}
		}
		return results;
	}
}
