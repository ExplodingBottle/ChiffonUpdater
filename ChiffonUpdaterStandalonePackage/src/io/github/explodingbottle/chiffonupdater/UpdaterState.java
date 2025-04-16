/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

public class UpdaterState {

	private File updateRoot;
	private SharedLogger logger;
	private OperatingSystemDetector osDetector;
	private PropertiesLoader properties;
	private UpdaterPathProvider uPathProvider;
	private ProductsListManager productsListManager;
	private File customProductDir;
	private boolean headless;
	private boolean cancelled;
	private UpdateCancelledCallback cancelCallback;
	private boolean showEulaAnyways;
	private boolean eulaExplicitelyAccepted;

	public UpdaterState(File updateRoot, SharedLogger logger, OperatingSystemDetector osDetector,
			PropertiesLoader properties, UpdaterPathProvider uPathProvider, ProductsListManager productsListManager,
			File customProductDir, boolean headless, boolean showEulaAnyways, boolean eulaExplicitelyAccepted) {
		this.updateRoot = updateRoot;
		this.logger = logger;
		this.osDetector = osDetector;
		this.properties = properties;
		this.uPathProvider = uPathProvider;
		this.productsListManager = productsListManager;
		this.customProductDir = customProductDir;
		this.headless = headless;
		this.cancelled = false;
		this.showEulaAnyways = showEulaAnyways;
		this.eulaExplicitelyAccepted = eulaExplicitelyAccepted;
	}

	public void setCancelCallback(UpdateCancelledCallback cancelCallback) {
		this.cancelCallback = cancelCallback;
	}

	public void callCancelCallback(PageWindowTemplate window) {
		if (cancelCallback != null) {
			cancelCallback.onCancel(window);
		}
	}

	public boolean canCancelFromCallback(PageWindowTemplate window) {
		if (cancelCallback != null) {
			return cancelCallback.canCancel(window);
		}
		return false;
	}

	public void cancelUpdate() {
		cancelled = true;
	}

	public boolean isUpdateCancelled() {
		return cancelled;
	}

	public File getUpdateRoot() {
		return updateRoot;
	}

	public SharedLogger getSharedLogger() {
		return logger;
	}

	public OperatingSystemDetector getOperatingSystemDetector() {
		return osDetector;
	}

	public PropertiesLoader getProperties() {
		return properties;
	}

	public UpdaterPathProvider getUpdaterPathProvider() {
		return uPathProvider;
	}

	public ProductsListManager getProductsListManager() {
		return productsListManager;
	}

	public File getCustomProductDirectory() {
		return customProductDir;
	}

	public boolean shouldBeHeadless() {
		return headless;
	}

	public boolean shouldShowEulaAnyways() {
		return showEulaAnyways;
	}

	public boolean isEulaExplicitelyAccepted() {
		return eulaExplicitelyAccepted;
	}
}
