/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ProductsSearcherThread extends Thread {

	private SharedLogger logger;
	private static final String CMPN = "IGTH";

	public ProductsSearcherThread() {
		logger = AgentMain.getSharedLogger();
	}

	public void run() {
		AgentSession currentSession = AgentMain.getCurrentSession();
		currentSession.setLastSearchResults(null);
		List<Properties> detectionFiles = currentSession.getDetFilesManager().loadDetectionPropertiesList();
		if (detectionFiles == null) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to prepare detection files list.");
			currentSession.setState(AgentSessionState.IDLE);
			return;
		}

		ProductsListManager manager = new ProductsListManager(
				new File(currentSession.getPathProvider().getAccessibleFolder(),
						HardcodedValues.returnProductsReglistName()),
				logger);

		ProductDetector detector = new ProductDetector(logger, manager, detectionFiles, AgentMain.getSharedFunctions(),
				null);
		List<DetectionResult> results = detector.detectProducts();

		LocalUpdatesManager localUpdMgr = currentSession.getLocalUpdatesManager();
		Properties prodsList = localUpdMgr.fetchProductsInformationsList();
		if (prodsList == null) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to download the product informations list.");
			currentSession.setState(AgentSessionState.IDLE);
			return;
		}

		if (!localUpdMgr.runCleanupMaintenanceTask(results)) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to run the local updates manager maintenance task.");
		}

		Set<String> requiredInformationHashes = new HashSet<String>();
		Map<String, Properties> loadedInformations = new HashMap<String, Properties>();
		for (DetectionResult result : results) {
			String hash = prodsList.getProperty(result.getProductName());
			if (hash == null) {
				logger.log(CMPN, LogLevel.WARNING, "Product " + result.getRootProductFolder().getAbsolutePath()
						+ " identified but no informations available for it.");
				continue;
			}
			requiredInformationHashes.add(hash);
		}
		for (String infosFileHash : requiredInformationHashes) {
			Properties props = localUpdMgr.getProductInformationsByHash(infosFileHash);
			if (props == null) {
				logger.log(CMPN, LogLevel.ERROR,
						"Failed to download the product informations file with hash " + infosFileHash + ".");
				currentSession.setState(AgentSessionState.IDLE);
				return;
			}
			if (!props.containsKey("update.productname")) {
				logger.log(CMPN, LogLevel.WARNING,
						"Skipping incorrect product informations file with hash " + infosFileHash + ".");
				continue;
			}
			loadedInformations.put(props.getProperty("update.productname"), props);
		}

		// Main infos & update infos
		List<ProductInformations> productInfos = new ArrayList<ProductInformations>();
		for (DetectionResult result : results) {
			Properties relatedProperties = loadedInformations.get(result.getProductName());
			if (relatedProperties == null) {
				logger.log(CMPN, LogLevel.WARNING, "Product named " + result.getProductName()
						+ " doesn't have a matching product informations file.");
				continue;
			}
			ProductInformations infos = new ProductInformations(result.getProductName(), result.getProductFeatures(),
					result.getRootProductFolder(), result.getVersion());
			Long releaseDateNum = null;
			try {
				String updateRelaseDateProp = relatedProperties.getProperty("update.releasedate");
				if (updateRelaseDateProp != null) {
					releaseDateNum = Long.parseLong(updateRelaseDateProp);
				}
			} catch (NumberFormatException e) {
				logger.log(CMPN, LogLevel.WARNING, "Failed to parse update release date.");
			}

			if (!result.getVersion().equals(relatedProperties.getProperty("update.versionname"))) {
				HybridInformations hybInfos = new HybridInformations(
						relatedProperties.getProperty("update.versionname"),
						relatedProperties.getProperty("update.description"), releaseDateNum, null, false,
						relatedProperties.getProperty("update.binhash"),
						relatedProperties.getProperty("update.eulahash"));
				infos.getHybridInformations().add(hybInfos);
			}
			productInfos.add(infos);
		}

		// Uninstall infos
		logger.log(CMPN, LogLevel.INFO, "Retrieving rollback informations of products.");
		for (ProductInformations infos : productInfos) {
			File databaseFolder = new File(infos.getProductInstallationPath(),
					HardcodedValues.returnProductSpecificUpdateFolderName());
			if (!databaseFolder.exists() || !databaseFolder.isDirectory()) {
				continue;
			}
			VersionsRollbackEnumerator enumerator = new VersionsRollbackEnumerator(logger, databaseFolder);
			ProductRollbackInformations rollbackInfos = enumerator.enumerateRollbackVersions();
			if (rollbackInfos.getState() != RollbackEnvironmentState.ROLLBACK_READY) {
				continue;
			}
			List<VersionRollbackTargetInformation> versInfos = rollbackInfos.getVersions();
			for (VersionRollbackTargetInformation curVersInfos : versInfos) {
				HybridInformations hybInfos = new HybridInformations(curVersInfos.getTargetVersion(),
						curVersInfos.getDescription(), curVersInfos.getReleaseDate(), curVersInfos.getInstallDate(),
						true, null, null);
				infos.getHybridInformations().add(hybInfos);
			}
		}
		currentSession.setLastSearchResults(productInfos);

		// Catalog infos
		logger.log(CMPN, LogLevel.INFO, "Retrieving product versions catalog.");
		List<CatalogItem> catalogList = currentSession.getCatalogManager().loadCatalog();
		if (catalogList == null) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to retrieve the catalog.");
			currentSession.setCatalogList(new ArrayList<CatalogItem>());
		} else {
			currentSession.setCatalogList(catalogList);
		}

		currentSession.setState(AgentSessionState.IDLE);
		return;
	}

}
