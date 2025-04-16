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

public class VersionRollbackThread extends Thread {

	private Map<ProductInformations, String> rollbackMap;
	private SharedLogger logger;
	private Translator translator;

	private boolean cancelPending;

	private static final String CMPN = "VRTH";

	public VersionRollbackThread(Map<ProductInformations, String> rollbackMap) {
		this.rollbackMap = rollbackMap;
		logger = AgentMain.getSharedLogger();
		translator = AgentMain.getTranslator();
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
		logger.log(CMPN, LogLevel.INFO, rollbackMap.size() + " products will be rolled back to an anterior version.");

		Map<ProductInformations, ActionResult> actionResults = new HashMap<ProductInformations, ActionResult>();

		ProgressFrame pf = new ProgressFrame(translator.getTranslation("cua.rollback.title"), null, null, null, null);
		pf.setTopText(translator.getTranslation("cua.rollback.details"));
		pf.setVisible(true);

		pf.setBottomButtonClickRunnable(() -> {
			pf.setBottomButtonEnabled(false);
			cancelPending = true;
		});

		int kcur = 0;
		for (ProductInformations infos : rollbackMap.keySet()) {

			if (cancelPending) {
				actionResults.put(infos, ActionResult.CANCELLED);
				continue;
			}

			pf.updateProgressBarValue((kcur * 100) / rollbackMap.size());
			kcur++;
			String targetVersion = rollbackMap.get(infos);
			HybridInformations rollbackInfos = null;
			for (HybridInformations info : infos.getHybridInformations()) {
				if (info.isInformationForUninstall() && info.getVersionName().equals(targetVersion)) {
					rollbackInfos = info;
					break;
				}
			}
			if (rollbackInfos == null) {
				logger.log(CMPN, LogLevel.WARNING,
						"Stange situation: no rollback informations in hybrid informations for "
								+ infos.getProductName());
				continue;
			}

			pf.updateCurrentTaskText(translator.getTranslation("cua.status.rollback", infos.getProductName(),
					rollbackInfos.getVersionName()));
			pf.appendActionMessage(
					translator.getTranslation("cua.rollback", infos.getProductName(), rollbackInfos.getVersionName()));

			pf.setAlwaysOnTop(false);
			if (!JarJumpUtil.jumpToOther(AgentMain.getCurrentSession().getCurrentUpdatePackage(),
					infos.getProductInstallationPath(), null, "true", "true", targetVersion, null, null, null)) {
				pf.setAlwaysOnTop(true);
				logger.log(CMPN, LogLevel.WARNING, "Failed to launch the rollback for product "
						+ infos.getProductInstallationPath().getAbsolutePath() + ".");
				actionResults.put(infos, ActionResult.FAILED);
				pf.appendActionMessage(" " + translator.getTranslation("cua.failed"));
				continue;
			}
			pf.setAlwaysOnTop(true);

			// Checking if the rollback has been applied.

			List<Properties> detectionFiles = AgentMain.getCurrentSession().getDetFilesManager()
					.loadDetectionPropertiesList();
			if (detectionFiles == null) {
				logger.log(CMPN, LogLevel.WARNING,
						"Failed to prepare detection files list to check if rollback succeed.");
				continue;
			}

			ProductsListManager manager = new ProductsListManager(
					new File(AgentMain.getCurrentSession().getPathProvider().getAccessibleFolder(),
							HardcodedValues.returnProductsReglistName()),
					logger);

			ProductDetector detector = new ProductDetector(logger, manager, detectionFiles,
					AgentMain.getSharedFunctions(), infos.getProductInstallationPath());
			List<DetectionResult> results = detector.detectProducts();
			if (results.size() == 1) {
				if (results.get(0).getVersion().equals(rollbackInfos.getVersionName())) {
					actionResults.put(infos, ActionResult.SUCCEED);
					pf.appendActionMessage(" " + translator.getTranslation("cua.done"));
				} else {
					actionResults.put(infos, ActionResult.FAILED);
					pf.appendActionMessage(" " + translator.getTranslation("cua.failed"));
				}
			}
			pf.appendActionMessage("\n");

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
			String targetVersion = rollbackMap.get(prodInf);
			ActionResult result = actionResults.get(prodInf);
			HybridInformations rollbackInfos = null;
			for (HybridInformations info : prodInf.getHybridInformations()) {
				if (info.isInformationForUninstall() && info.getVersionName().equals(targetVersion)) {
					rollbackInfos = info;
					break;
				}
			}
			ActionRecord record = new ActionRecord(prodInf.getProductName(), result, System.currentTimeMillis(), true,
					prodInf.getProductInstallationPath(), prodInf.getCurrentVersion(), rollbackInfos.getVersionName(),
					prodInf.getProductFeatures());
			records.add(record);
		}

		if (!history.addHistoryItems(records)) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to save the history with the new records.");
		}
		if (cancelPending) {
			showEndPage(pf, translator.getTranslation("cua.cancelled.details"));
			return;
		}

		if (errorOccured) {
			showEndPage(pf, translator.getTranslation("cua.finishedwe.details"));
		} else {
			showEndPage(pf, translator.getTranslation("cua.finished.details"));
		}

	}

}
