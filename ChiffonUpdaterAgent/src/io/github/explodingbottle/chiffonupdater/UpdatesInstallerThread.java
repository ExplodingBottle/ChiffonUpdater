/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class UpdatesInstallerThread extends Thread {

	private SharedLogger logger;
	private List<ProductInformations> toUpdate;
	private static final String CMPN = "UITH";
	private Translator translator;
	private boolean cancelPending;

	private Map<ProductInformations, ActionResult> actionResults;
	private Set<String> requiredDownloads;

	private ProgressFrame progressFrame;

	public UpdatesInstallerThread(List<ProductInformations> toUpdate, ProgressFrame progressFrame,
			Map<ProductInformations, ActionResult> actionResults, Set<String> requiredDownloads) {
		translator = AgentMain.getTranslator();
		this.progressFrame = progressFrame;
		this.requiredDownloads = requiredDownloads;
		this.toUpdate = toUpdate;
		this.actionResults = actionResults;
		logger = AgentMain.getSharedLogger();
	}

	public void showEndPage(ProgressFrame frame, String endText) {
		frame.setBottomButtonClickRunnable(() -> {
			frame.setVisible(false);
			frame.dispose();
			AgentMain.getCurrentSession().setState(AgentSessionState.IDLE);
		});
		frame.setTopText(endText);
		frame.setBottomButtonText(translator.getTranslation("cua.window.close"));
		frame.setBottomButtonEnabled(true);
		frame.setStatus(GraphicalStatusPhase.END);
		frame.refreshDisplay();
	}

	public void run() {

		LocalUpdatesManager localUpdMgr = AgentMain.getCurrentSession().getLocalUpdatesManager();
		CustomizationInformationsManager custoMgr = AgentMain.getCurrentSession().getCustomizationManager();

		progressFrame.setTopText(translator.getTranslation("cua.installing.details"));
		progressFrame.setStatus(GraphicalStatusPhase.ACTION);
		progressFrame.refreshDisplay();
		progressFrame.clearActionLog();

		progressFrame.setBottomButtonClickRunnable(() -> {
			progressFrame.setBottomButtonEnabled(false);
			cancelPending = true;
		});

		Map<ProductInformations, File> readyUpdates = new HashMap<ProductInformations, File>();

		int kcur = 0;
		for (String toDownload : requiredDownloads) {
			progressFrame.updateProgressBarValue((kcur * 100) / requiredDownloads.size());
			kcur++;
			List<ProductInformations> relatedProdInfos = new ArrayList<ProductInformations>();
			for (ProductInformations toCheck : toUpdate) {
				HybridInformations updateInfos = toCheck.searchUpdateInformation();
				if (toDownload.equals(updateInfos.getBinaryHash())) {
					relatedProdInfos.add(toCheck);
				}
			}

			ProductInformations prod = relatedProdInfos.get(0);
			HybridInformations updateInfos = prod.searchUpdateInformation();
			if (cancelPending) {
				for (ProductInformations prod2 : relatedProdInfos) {
					actionResults.put(prod2, ActionResult.CANCELLED);
				}
				continue;
			}

			progressFrame.updateCurrentTaskText(translator.getTranslation("cua.status.downloading",
					updateInfos.getVersionName(), prod.getProductName()));
			progressFrame.appendActionMessage(
					translator.getTranslation("cua.downloading", updateInfos.getVersionName(), prod.getProductName()));
			File readyFolder = localUpdMgr.getPreparedUpdateFolder(updateInfos.getBinaryHash());
			if (readyFolder != null) {

				if (!custoMgr.copyCustomizationsToExtractedFolder(readyFolder)) {
					logger.log(CMPN, LogLevel.WARNING, "Customization informations for product " + prod.getProductName()
							+ " failed to be deployed.");
				}

				for (ProductInformations prod2 : relatedProdInfos) {
					readyUpdates.put(prod2, readyFolder);
				}
				progressFrame.appendActionMessage(" " + translator.getTranslation("cua.done") + "\n");
				logger.log(CMPN, LogLevel.INFO,
						"Product update for " + prod.getProductName() + " has been downloaded and extracted.");
			} else {
				for (ProductInformations prod2 : relatedProdInfos) {
					actionResults.put(prod2, ActionResult.FAILED);
				}
				progressFrame.appendActionMessage(" " + translator.getTranslation("cua.failed") + "\n");
				logger.log(CMPN, LogLevel.WARNING, "Failed download for update of product " + prod.getProductName());
			}
		}

		if (cancelPending) {
			showEndPage(progressFrame, translator.getTranslation("cua.cancelled.details"));
			AgentMain.getCurrentSession().setLastActionResults(actionResults);
			return;
		}
		progressFrame.updateCurrentTaskText("");
		progressFrame.updateProgressBarValue(0);

		for (int i = 0; i < readyUpdates.size(); i++) {
			progressFrame.updateProgressBarValue((i * 100) / toUpdate.size());
			ProductInformations prod = toUpdate.get(i);
			if (cancelPending) {
				actionResults.put(prod, ActionResult.CANCELLED);
				continue;
			}
			File updFolder = readyUpdates.get(prod);
			if (updFolder == null) {
				continue;
			}
			HybridInformations updateInfos = prod.searchUpdateInformation();
			// No need to check if updateInfos is null, it won't be.
			progressFrame.updateCurrentTaskText(translator.getTranslation("cua.status.installing",
					updateInfos.getVersionName(), prod.getProductName()));
			progressFrame.appendActionMessage(
					translator.getTranslation("cua.installing", updateInfos.getVersionName(), prod.getProductName()));

			progressFrame.setAlwaysOnTop(false);
			if (!JarJumpUtil.jumpToOther(AgentMain.getCurrentSession().getCurrentUpdatePackage(), updFolder,
					prod.getProductInstallationPath().getAbsolutePath(), "true", "true", null, null, null, "true")) {
				progressFrame.setAlwaysOnTop(true);
				logger.log(CMPN, LogLevel.WARNING, "Failed to launch the update for product "
						+ prod.getProductInstallationPath().getAbsolutePath() + ".");
				actionResults.put(prod, ActionResult.FAILED);
				progressFrame.appendActionMessage(" " + translator.getTranslation("cua.failed") + "\n");
				continue;
			}
			progressFrame.setAlwaysOnTop(true);
			// Checking if the update has been applied.

			List<Properties> detectionFiles = AgentMain.getCurrentSession().getDetFilesManager()
					.loadDetectionPropertiesList();
			if (detectionFiles == null) {
				logger.log(CMPN, LogLevel.WARNING,
						"Failed to prepare detection files list to check if update succeed.");
				continue;
			}

			ProductsListManager manager = new ProductsListManager(
					new File(AgentMain.getCurrentSession().getPathProvider().getAccessibleFolder(),
							HardcodedValues.returnProductsReglistName()),
					logger);

			ProductDetector detector = new ProductDetector(logger, manager, detectionFiles,
					AgentMain.getSharedFunctions(), prod.getProductInstallationPath());
			List<DetectionResult> results = detector.detectProducts();
			if (results.size() == 1) {
				if (results.get(0).getVersion().equals(updateInfos.getVersionName())) {
					actionResults.put(prod, ActionResult.SUCCEED);
					progressFrame.appendActionMessage(" " + translator.getTranslation("cua.done"));
				} else {
					actionResults.put(prod, ActionResult.FAILED);
					progressFrame.appendActionMessage(" " + translator.getTranslation("cua.failed"));
				}
			}
			progressFrame.appendActionMessage("\n");
		}
		AgentMain.getCurrentSession().setLastActionResults(actionResults);

		boolean errorOccured = false;
		for (ActionResult r : actionResults.values()) {
			if (r == ActionResult.FAILED) {
				errorOccured = true;
				break;
			}
		}
		HistoryFileUtil history = AgentMain.getCurrentSession().getHistoryFileUtil();
		List<ActionRecord> records = new ArrayList<ActionRecord>();
		for (ProductInformations prodInf : actionResults.keySet()) {
			ActionResult result = actionResults.get(prodInf);
			HybridInformations updateInfos = prodInf.searchUpdateInformation();
			ActionRecord record = new ActionRecord(prodInf.getProductName(), result, System.currentTimeMillis(), false,
					prodInf.getProductInstallationPath(), prodInf.getCurrentVersion(), updateInfos.getVersionName(),
					prodInf.getProductFeatures());
			records.add(record);
		}

		if (!history.addHistoryItems(records)) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to save the history with the new records.");
		}

		if (cancelPending) {
			showEndPage(progressFrame, translator.getTranslation("cua.cancelled.details"));
			return;
		}

		if (errorOccured) {
			showEndPage(progressFrame, translator.getTranslation("cua.finishedwe.details"));
		} else {
			showEndPage(progressFrame, translator.getTranslation("cua.finished.details"));
		}

	}

}
