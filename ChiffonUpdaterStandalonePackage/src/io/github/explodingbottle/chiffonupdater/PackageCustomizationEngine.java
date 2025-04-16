/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import javax.swing.ImageIcon;

public class PackageCustomizationEngine {

	private PropertiesLoader loader;
	private Translator translator;
	private SharedLogger logger;

	private String titleToDisplay;

	private ImageIcon customIcon;
	private ImageIcon customBanner;

	private static final String CMPN = "PCEN";

	public PackageCustomizationEngine(PropertiesLoader loader, Translator translator, SharedLogger logger) {
		this.loader = loader;
		this.translator = translator;
		this.logger = logger;
	}

	public String getTitleToDisplay() {
		return titleToDisplay;
	}

	public ImageIcon getIconToDisplay() {
		return customIcon;
	}

	public ImageIcon getBannerToDisplay() {
		return customBanner;
	}

	public boolean copyCustomizationToUninstallDatabase(File target) {
		File targetProps = new File(target, "customization.properties");
		Properties custo = loader.getProperties("customization.properties");
		if (custo == null) {
			return true;
		}

		String bannerName = custo.getProperty("package.customization.banner");
		String iconName = custo.getProperty("package.customization.icon");

		if (bannerName != null) {
			try {
				Files.copy(new File(loader.getPropertiesRootFolder(), bannerName).toPath(),
						new File(target, bannerName).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to copy the custom banner file.");
				return false;
			}
		}
		if (iconName != null) {
			try {
				Files.copy(new File(loader.getPropertiesRootFolder(), iconName).toPath(),
						new File(target, iconName).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to copy the custom icon file.");
				return false;
			}
		}

		try (FileOutputStream fos = new FileOutputStream(targetProps)) {
			if (custo != null) {
				custo.store(fos, "Copy of the customization file\r\nDO NOT EDIT!");
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to save the customization file.");
			return false;
		}
		return true;
	}

	public void initEngine() {
		Properties custo = loader.getProperties("customization.properties");
		titleToDisplay = translator.getTranslation("wizard.about.product.title");
		customBanner = PackageMain.getImageResourcesLoader().getLoadedResource("wizard.png");
		customIcon = PackageMain.getImageResourcesLoader().getLoadedResource("wizard_icon.png");
		if (custo != null) {
			String custoTitle = custo.getProperty("package.customization.title");
			if (custoTitle != null) {
				titleToDisplay = translator.getTranslation("wizard.about.product.poweredby", custoTitle);
			}
			String bannerName = custo.getProperty("package.customization.banner");
			String iconName = custo.getProperty("package.customization.icon");
			if (bannerName != null) {
				try {
					customBanner = new ImageIcon(
							new File(loader.getPropertiesRootFolder(), bannerName).toURI().toURL());
				} catch (MalformedURLException e) {
					logger.log(CMPN, LogLevel.ERROR, "Couldn't parse URL of the custom banner.");
				}
			}
			if (iconName != null) {
				try {
					customIcon = new ImageIcon(new File(loader.getPropertiesRootFolder(), iconName).toURI().toURL());
				} catch (MalformedURLException e) {
					logger.log(CMPN, LogLevel.ERROR, "Couldn't parse URL of the custom banner.");
				}
			}
		}
		logger.log(CMPN, LogLevel.INFO, "Customization engine is ready.");
	}

}
