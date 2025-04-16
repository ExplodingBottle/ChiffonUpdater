/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class AgentSession {

	private String cookie;
	private String origin;
	private AgentSessionState state;
	private URI backendURI;
	private UpdaterPathProvider pathProvider;
	private DetectionFilesManager detFilesManager;
	private LocalUpdatesManager localUpdatesManager;
	private List<ProductInformations> lastSearchResults;
	private CatalogManager catalogManager;
	private Map<ProductInformations, ActionResult> lastActionResults;
	private List<CatalogItem> catalog;
	private File currentUpdatePackage;
	private HistoryFileUtil historyFileUtil;
	private boolean allowKeyPersistence;
	private CustomizationInformationsManager customizationManager;

	public AgentSession(String cookie, String origin, AgentSessionState state) {
		this.cookie = cookie;
		this.origin = origin;
		this.state = state;
	}

	public CustomizationInformationsManager getCustomizationManager() {
		return customizationManager;
	}

	public void setCustomizationManager(CustomizationInformationsManager customizationManager) {
		this.customizationManager = customizationManager;
	}

	public CatalogManager getCatalogManager() {
		return catalogManager;
	}

	public void setCatalogManager(CatalogManager catalogManager) {
		this.catalogManager = catalogManager;
	}

	public boolean allowKeyPersistence() {
		return allowKeyPersistence;
	}

	public List<CatalogItem> getCatalogList() {
		return catalog;
	}

	public void setCatalogList(List<CatalogItem> catalog) {
		this.catalog = catalog;
	}

	public void setAllowKeyPersistence(boolean allowKeyPersistence) {
		this.allowKeyPersistence = allowKeyPersistence;
	}

	public HistoryFileUtil getHistoryFileUtil() {
		return historyFileUtil;
	}

	public void setHistoryFileUtil(HistoryFileUtil historyFileUtil) {
		this.historyFileUtil = historyFileUtil;
	}

	public URI getBackendURI() {
		return backendURI;
	}

	public Map<ProductInformations, ActionResult> getLastActionResults() {
		return lastActionResults;
	}

	public void setLastActionResults(Map<ProductInformations, ActionResult> lastActionResults) {
		this.lastActionResults = lastActionResults;
	}

	public File getCurrentUpdatePackage() {
		return currentUpdatePackage;
	}

	public void setCurrentUpdatePackage(File currentUpdatePackage) {
		this.currentUpdatePackage = currentUpdatePackage;
	}

	public AgentSessionState getState() {
		return state;
	}

	public void setBackendURI(URI backendURI) {
		this.backendURI = backendURI;
	}

	public void setState(AgentSessionState state) {
		this.state = state;
	}

	public String getCookie() {
		return cookie;
	}

	public UpdaterPathProvider getPathProvider() {
		return pathProvider;
	}

	public void setPathProvider(UpdaterPathProvider pathProvider) {
		this.pathProvider = pathProvider;
	}

	public List<ProductInformations> getLastSearchResults() {
		return lastSearchResults;
	}

	public void setLastSearchResults(List<ProductInformations> lastSearchResults) {
		this.lastSearchResults = lastSearchResults;
	}

	public String getOrigin() {
		return origin;
	}

	public DetectionFilesManager getDetFilesManager() {
		return detFilesManager;
	}

	public void setDetFilesManager(DetectionFilesManager detFilesManager) {
		this.detFilesManager = detFilesManager;
	}

	public LocalUpdatesManager getLocalUpdatesManager() {
		return localUpdatesManager;
	}

	public void setLocalUpdatesManager(LocalUpdatesManager localUpdatesManager) {
		this.localUpdatesManager = localUpdatesManager;
	}

}
