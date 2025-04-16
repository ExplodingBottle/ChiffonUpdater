/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UpdatesInstallerPreThread extends Thread {

	private SharedLogger logger;
	private List<ProductInformations> toUpdate;
	private static final String CMPN = "UIPT";
	private Translator translator;

	public UpdatesInstallerPreThread(List<ProductInformations> toUpdate) {
		translator = AgentMain.getTranslator();
		this.toUpdate = toUpdate;
		logger = AgentMain.getSharedLogger();
	}

	public void run() {
		logger.log(CMPN, LogLevel.INFO, "Updates will be download and installed for " + toUpdate.size() + " products.");

		LocalUpdatesManager localUpdMgr = AgentMain.getCurrentSession().getLocalUpdatesManager();
		AgentMain.getCurrentSession().setLastActionResults(null);

		Map<ProductInformations, ActionResult> actionResults = new HashMap<ProductInformations, ActionResult>();
		Map<String, ProductInformations> agreementsHashByInfos = new HashMap<String, ProductInformations>();
		Map<String, ProductInformations> agreementsByInfos = new HashMap<String, ProductInformations>();
		Set<String> requiredDownloads = new HashSet<String>();

		for (int i = 0; i < toUpdate.size(); i++) {
			ProductInformations prod = toUpdate.get(i);

			HybridInformations updateInfos = prod.searchUpdateInformation();
			if (updateInfos == null) {
				logger.log(CMPN, LogLevel.WARNING,
						"Stange situation: no update informations in hybrid informations for " + prod.getProductName());
				continue;
			}
			String eulaHash = updateInfos.getEULAHash();
			if (eulaHash != null) {
				agreementsHashByInfos.put(eulaHash, prod);
			} else {
				requiredDownloads.add(updateInfos.getBinaryHash());
			}
		}

		for (String eulaHash : agreementsHashByInfos.keySet()) {
			ProductInformations infos = agreementsHashByInfos.get(eulaHash);
			List<ProductInformations> relatedProdInfos = new ArrayList<ProductInformations>();
			for (ProductInformations toCheck : toUpdate) {
				HybridInformations updateInfos = toCheck.searchUpdateInformation();
				if (eulaHash.equals(updateInfos.getEULAHash())) {
					relatedProdInfos.add(toCheck);
				}
			}

			if (infos == null) {
				continue;
			}
			String eulaText = localUpdMgr.getEulaByHash(eulaHash);
			if (eulaText == null) {
				logger.log(CMPN, LogLevel.WARNING,
						"Couldn't download EULA file for update of " + infos.getProductName());

				for (ProductInformations prod2 : relatedProdInfos) {
					actionResults.put(prod2, ActionResult.FAILED);
				}
				return;
			}
			agreementsByInfos.put(eulaText, infos);
			for (ProductInformations prod2 : relatedProdInfos) {
				HybridInformations updateInfos = prod2.searchUpdateInformation();
				requiredDownloads.add(updateInfos.getBinaryHash());
			}
		}

		boolean startImmediately = agreementsByInfos.isEmpty();

		ProgressFrame frame = new ProgressFrame(translator.getTranslation("cua.installing.title"), agreementsByInfos,
				requiredDownloads, actionResults, toUpdate);
		if (startImmediately) {
			new UpdatesInstallerThread(toUpdate, frame, actionResults, requiredDownloads).start();
		}
		frame.setVisible(true);
	}

}
