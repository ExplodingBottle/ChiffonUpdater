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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

public class UpdaterThread extends Thread {
	private UpdaterState state;
	private PageWindowTemplate window;

	private String productTargetVersion;
	private String productToUpdateName;
	private FunctionsGatherer updaterFunctions;
	private Properties iterationsProperties;
	private List<Properties> detectionFiles;
	private Properties updateProperties;
	private DetectionResult selectedInstallation;
	private String eula;
	private File eulaFileRef;
	private String description;
	private boolean showDescription;
	private File rightPathModule;
	private long releaseDate;

	private String moduleClassName;

	public UpdaterThread() {
		this.state = PackageMain.returnUpdaterState();
	}

	public void run() {
		Translator translator = PackageMain.getTranslator();
		detectionFiles = new ArrayList<Properties>();
		detectionFiles.add(state.getProperties().getProperties("detection.properties"));
		updateProperties = state.getProperties().getProperties("update.properties");
		iterationsProperties = state.getProperties().getProperties("iterations.properties");

		if (updateProperties == null || iterationsProperties == null) {
			state.getSharedLogger().log("PMTH", LogLevel.ERROR, "Missing properties file.");
			if (!state.shouldBeHeadless())
				JOptionPane.showMessageDialog(null, translator.getTranslation("wizard.error.filemissing"),
						translator.getTranslation("wizard.error.title"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		String eulaFile = updateProperties.getProperty("update.eula");
		description = updateProperties.getProperty("update.description");

		showDescription = "1".equals(updateProperties.getProperty("update.show.description"));

		if (description == null || description.isEmpty()) {
			if (showDescription) {
				state.getSharedLogger().log("PMTH", LogLevel.WARNING,
						"Should show a description but no description or blank description present.");
			}
			showDescription = false;
		}

		if (eulaFile != null && !eulaFile.isEmpty()) {

			try {
				eulaFileRef = new File(state.getUpdateRoot(), eulaFile);
				eula = new String(Files.readAllBytes(eulaFileRef.toPath()));
			} catch (IOException e) {
				state.getSharedLogger().log("PMTH", LogLevel.ERROR, "Missing EULA file.");
				if (!state.shouldBeHeadless())
					JOptionPane.showMessageDialog(null, translator.getTranslation("wizard.error.filemissing"),
							translator.getTranslation("wizard.error.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		if (!state.shouldShowEulaAnyways() && !state.isEulaExplicitelyAccepted()) {
			state.getSharedLogger().log("PMTH", LogLevel.ERROR,
					"The EULA hasn't been accepted. Please read it at " + eulaFile + ".");
			state.getSharedLogger().log("PMTH", LogLevel.ERROR,
					"If you agree with it, add the -Dcupackage.accepteula=true option.");
			return;
		}

		productToUpdateName = updateProperties.getProperty("update.productname");
		productTargetVersion = updateProperties.getProperty("update.targetversion");
		String updateCustomName = updateProperties.getProperty("update.custom");

		String releaseDateText = updateProperties.getProperty("update.releasedate");
		if (releaseDateText != null) {
			try {
				releaseDate = Long.parseLong(releaseDateText);
			} catch (NumberFormatException e) {
				state.getSharedLogger().log("PMTH", LogLevel.WARNING, "Couldn't parse release date.");
			}
		} else {
			state.getSharedLogger().log("PMTH", LogLevel.WARNING, "Missing release date.");
		}

		updaterFunctions = new FunctionsGatherer();
		boolean failedToLoadCustomModule = false;
		if (updateCustomName != null && !updateCustomName.isEmpty()) {
			String[] splited = updateCustomName.split(";");
			if (splited.length == 2) {
				String pathName = splited[0];
				String className = splited[1];
				state.getSharedLogger().log("PMTH", LogLevel.INFO,
						"Custom Module: path=" + pathName + ", class=" + className);
				File pathModule = new File(state.getUpdateRoot(), pathName);
				try {
					URLClassLoader classLoader = new URLClassLoader(new URL[] { pathModule.toURI().toURL() });
					Class<?> moduleClass = Class.forName(className, true, classLoader);
					if (FunctionsPublisher.class.isAssignableFrom(moduleClass)) {
						moduleClassName = className;
						FunctionsPublisher publisher = (FunctionsPublisher) moduleClass.newInstance();
						publisher.publishModuleFunctions(updaterFunctions);
						rightPathModule = pathModule;
						state.getSharedLogger().log("PMTH", LogLevel.INFO, "Custom module ready for use.");
					} else {
						state.getSharedLogger().log("PMTH", LogLevel.ERROR, "Malformed custom module.");
						failedToLoadCustomModule = true;
					}
				} catch (Exception e) {
					state.getSharedLogger().log("PMTH", LogLevel.ERROR, "Failed to load the custom module.");
					failedToLoadCustomModule = true;
				}
			}
		}

		if (failedToLoadCustomModule && !state.shouldBeHeadless()) {
			JOptionPane.showMessageDialog(null, translator.getTranslation("wizard.error.filemissing"),
					translator.getTranslation("wizard.error.title"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		new StandaloneUpdaterFunctionsModule(state.getSharedLogger()).publishModuleFunctions(updaterFunctions);

		if (!state.shouldBeHeadless()) {
			window = new UpdaterWindow(this);
			window.setVisible(true);
		} else {
			if (state.shouldShowEulaAnyways() && getEULAText() != null) {
				window = new LicenseOnlyWindow(this);
				window.setVisible(true);
			} else {
				startInstallHeadlessCallback();
			}
		}

	}

	public void startInstallHeadlessCallback() {
		ProductsDetectionThread detec = new ProductsDetectionThread(new HeadlessDetectionCallback(this), state,
				productToUpdateName, productTargetVersion, updaterFunctions, iterationsProperties, detectionFiles);
		detec.start();
	}

	public File getModulePath() {
		return rightPathModule;
	}

	public String getModuleClassName() {
		return moduleClassName;
	}

	public DetectionResult getSelectedInstallation() {
		return selectedInstallation;
	}

	public void setSelectedInstallation(DetectionResult selectedInstallation) {
		this.selectedInstallation = selectedInstallation;
	}

	public Properties getUpdatePropertiesFile() {
		return updateProperties;
	}

	public String getTargetVersion() {
		return productTargetVersion;
	}

	public String getTargetProductName() {
		return productToUpdateName;
	}

	public FunctionsGatherer getUpdaterFunctions() {
		return updaterFunctions;
	}

	public File getEulaFileReference() {
		return eulaFileRef;
	}

	public List<Properties> getDetectionFiles() {
		return detectionFiles;
	}

	public Properties getIterationsFile() {
		return iterationsProperties;
	}

	public String getEULAText() {
		return eula;
	}

	public String getDescriptionText() {
		return description;
	}

	public boolean shouldShowDescription() {
		return showDescription;
	}

	public long getReleaseDate() {
		return releaseDate;
	}

}
