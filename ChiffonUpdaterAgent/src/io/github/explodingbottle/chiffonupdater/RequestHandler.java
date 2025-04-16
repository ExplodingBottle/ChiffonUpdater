/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler {

	private Socket soc;
	private static final HttpResponse BAD_REQUEST_ERROR = new HttpResponse("HTTP/1.1", 400, "Bad Request", null, null);
	private static final HttpResponse UNAUTHORIZED_RESPONSE = new HttpResponse("HTTP/1.1", 401, "Unauthorized", null,
			null);
	private static final HttpResponse SERV_UNAVAIL_RESPONSE = new HttpResponse("HTTP/1.1", 503, "Service Unavailable",
			null, null);
	private static final HttpResponse TOO_MANY_REQS_RESPONSE = new HttpResponse("HTTP/1.1", 429, "Too Many Requests",
			null, null);
	private static final HttpResponse INTERNAL_SERVER_ERROR = new HttpResponse("HTTP/1.1", 500, "Internal Server Error",
			null, null);
	private SharedLogger logger;
	private static final String CMPN = "REQH";

	private static final int MAX_ENTRIES_PER_CATALOG_PAGE = 50;

	public RequestHandler(Socket soc) {
		this.soc = soc;
		logger = AgentMain.getSharedLogger();
	}

	private boolean isAllowed(HttpRequest req) {
		return AgentMain.getCurrentSession() != null
				&& AgentMain.getCurrentSession().getOrigin().equals(req.getHttpParameters().get("Origin"))
				&& AgentMain.getCurrentSession().getCookie().equals(req.getParameters().get("cookie"));
	}

	private void handleRequestAgentStatus(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null,
				AgentMain.getCurrentSession().getState().name().toCharArray()));
	}

	private void handleRequestAgentFeedConfiguration(HttpRequestProcessor reqProc, HttpRequest req) {

		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("backend")
				|| !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.UNCONFIGURED) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		session.setState(AgentSessionState.BUSY);
		logger.log(CMPN, LogLevel.INFO, "Agent is being configured.");
		URI uriBackend = null;
		try {
			uriBackend = new URI(parameters.get("backend"));
		} catch (URISyntaxException e) {
			logger.log(CMPN, LogLevel.WARNING, "Invalid URI.");
			session.setState(AgentSessionState.UNCONFIGURED);
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}

		session.setBackendURI(uriBackend);
		ConfigurationDownloadThread cdt = new ConfigurationDownloadThread();
		cdt.start();
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, null));
	}

	private void handleRequestAgentControlAccepted(HttpRequestProcessor reqProc, HttpRequest req) {
		String cookie = req.getParameters().get("cookie");
		String origin = req.getHttpParameters().get("Origin");
		if (origin == null || cookie == null) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (isAllowed(req)) {

			reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "true".toCharArray()));
			return;
		}
		AgentSession potSession = AgentMain.getPendingSessionsList().get(origin);
		if (potSession != null) {
			if (potSession.getCookie().equals(cookie)) {
				reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "wait".toCharArray()));
				return;
			}
		}
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "false".toCharArray()));
	}

	private void handleRequestAgentSearchProducts(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.IDLE) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		session.setState(AgentSessionState.BUSY);
		logger.log(CMPN, LogLevel.INFO, "Agent will now gather the informations.");
		ProductsSearcherThread searcherThread = new ProductsSearcherThread();
		searcherThread.start();
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, null));
	}

	private void handleRequestAgentPerformActions(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("action")
				|| !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		boolean rollback = false;
		if ("rollback".equalsIgnoreCase(parameters.get("action"))) {
			rollback = true;
		} else if (!"update".equalsIgnoreCase(parameters.get("action"))) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if ((!rollback && !parameters.containsKey("update_list"))
				|| ((rollback && !parameters.containsKey("rollback_list")))) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}

		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.IDLE) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		List<ProductInformations> updProds = new ArrayList<ProductInformations>();
		Map<ProductInformations, String> rollbackMap = new HashMap<ProductInformations, String>();
		if (session.getLastSearchResults() == null) {
			logger.log(CMPN, LogLevel.ERROR, "Impossible to launch an action when the search results are null.");
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		if (rollback) {
			String ids[] = parameters.get("rollback_list").split(";");
			for (String rollbackPair : ids) {
				String split[] = rollbackPair.split(":");
				if (split.length != 2) {
					logger.log(CMPN, LogLevel.WARNING,
							"Recieved a request containing an incorrect rollback id/version pair.");
					reqProc.pushResponse(BAD_REQUEST_ERROR);
					return;
				}
				int id;
				try {
					id = Integer.parseInt(split[0]);
				} catch (NumberFormatException e) {
					logger.log(CMPN, LogLevel.WARNING, "Recieved a request containing a malformed ID: " + split[0]);
					reqProc.pushResponse(BAD_REQUEST_ERROR);
					return;
				}

				ProductInformations fInfo = session.getLastSearchResults().get(id);
				if (fInfo != null) {
					boolean rollbackCompatible = false;
					for (HybridInformations hybInf : fInfo.getHybridInformations()) {
						if (hybInf.isInformationForUninstall() && hybInf.getVersionName().equals(split[1])) {
							rollbackCompatible = true;
							break;
						}
					}
					if (!rollbackCompatible) {
						logger.log(CMPN, LogLevel.WARNING, "Recieved a request containing an ID " + split[0]
								+ " referring to an incompatible entry.");
						reqProc.pushResponse(BAD_REQUEST_ERROR);
						return;
					}
					rollbackMap.put(fInfo, split[1]);
				} else {
					logger.log(CMPN, LogLevel.WARNING,
							"Recieved a request containing an ID " + split[0] + " referring to an unexisting entry.");
					reqProc.pushResponse(BAD_REQUEST_ERROR);
					return;
				}

			}
		} else {

			String ids[] = parameters.get("update_list").split(";");
			for (String curId : ids) {
				int id;
				try {
					id = Integer.parseInt(curId);
				} catch (NumberFormatException e) {
					logger.log(CMPN, LogLevel.WARNING, "Recieved a request containing a malformed ID: " + curId);
					reqProc.pushResponse(BAD_REQUEST_ERROR);
					return;
				}
				ProductInformations fInfo = session.getLastSearchResults().get(id);
				if (fInfo != null) {
					boolean updateCompatible = false;
					for (HybridInformations hybInf : fInfo.getHybridInformations()) {
						if (!hybInf.isInformationForUninstall()) {
							updateCompatible = true;
							break;
						}
					}
					if (!updateCompatible) {
						logger.log(CMPN, LogLevel.WARNING, "Recieved a request containing an ID " + curId
								+ " referring to an incompatible entry.");
						reqProc.pushResponse(BAD_REQUEST_ERROR);
						return;
					}
					updProds.add(fInfo);
				} else {
					logger.log(CMPN, LogLevel.WARNING,
							"Recieved a request containing an ID " + curId + " referring to an unexisting entry.");
					reqProc.pushResponse(BAD_REQUEST_ERROR);
					return;
				}
			}
		}

		session.setState(AgentSessionState.BUSY);
		logger.log(CMPN, LogLevel.INFO, "Agent will now perform the actions.");
		// Launching here the update
		if (rollback) {
			logger.log(CMPN, LogLevel.INFO, "Selected action is 'rollback'.");
			VersionRollbackThread vrt = new VersionRollbackThread(rollbackMap);
			vrt.start();
		} else {
			logger.log(CMPN, LogLevel.INFO, "Selected action is 'update'.");
			UpdatesInstallerPreThread uit = new UpdatesInstallerPreThread(updProds);
			uit.start();
		}
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, null));
	}

	private void printStringWithLength(StringBuilder b, String t) {
		b.append(t.length() + "\n" + t + "\n");
	}

	private void printListWithLength(StringBuilder b, List<String> l) {
		b.append(l.size() + "\n");
		for (String s : l) {
			b.append(s + "\n");
		}
	}

	private void handleRequestAgentKeyPersistence(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}

		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.IDLE) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		if (!session.allowKeyPersistence()) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		session.setState(AgentSessionState.BUSY);
		String key = PersistentAccessKeyUtil.createValidKeyForFolder(session.getPathProvider().getAccessibleFolder());
		session.setState(AgentSessionState.IDLE);
		if (key == null) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
		} else {
			reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, key.toCharArray()));
		}
	}

	private void handleRequestAgentActionResults(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.IDLE) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		List<ProductInformations> searchResults = session.getLastSearchResults();
		Map<ProductInformations, ActionResult> actionResults = session.getLastActionResults();
		if (searchResults == null || actionResults == null) {
			reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "null".toCharArray()));
			return;
		}
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < searchResults.size(); i++) {
			ActionResult res = actionResults.get(searchResults.get(i));
			if (res != null) {
				int resi = 0;
				if (res == ActionResult.CANCELLED) {
					resi = 1;
				}
				if (res == ActionResult.FAILED) {
					resi = -1;
				}
				strBuilder.append(i + "=" + resi + "\n");
			}
		}
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, strBuilder.toString().toCharArray()));
	}

	private void handleRequestAgentActionsHistory(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.IDLE) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}

		HistoryFileUtil historyFileUtil = session.getHistoryFileUtil();

		List<ActionRecord> history = historyFileUtil.getHistory();
		if (history == null) {
			reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "null".toCharArray()));
			return;
		}

		StringBuilder strBuilder = new StringBuilder();
		for (ActionRecord record : history) {
			ActionResult res = record.getStatus();
			int resi = 0;
			if (res == ActionResult.CANCELLED) {
				resi = 1;
			}
			if (res == ActionResult.FAILED) {
				resi = -1;
			}
			strBuilder.append(record.getProductName() + ";" + record.getActionDate() + ";" + record.getInstallVersion()
					+ ";" + record.getTargetVersion() + ";" + String.join(",", record.getFeatures()) + ";"
					+ record.wasDowngrade() + ";" + resi + "\n");
			strBuilder.append(record.getInstallLocation().getAbsolutePath() + "\n");
		}
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, strBuilder.toString().toCharArray()));
	}

	private void handleRequestAgentSearchResults(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("cookie")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.IDLE) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		List<ProductInformations> searchResults = session.getLastSearchResults();
		if (searchResults == null) {
			reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "null".toCharArray()));
			return;
		}
		StringBuilder strBuilder = new StringBuilder();
		for (ProductInformations infos : searchResults) {
			printStringWithLength(strBuilder, infos.getProductName());
			printStringWithLength(strBuilder, infos.getCurrentVersion());
			printStringWithLength(strBuilder, infos.getProductInstallationPath().getAbsolutePath());
			printListWithLength(strBuilder, infos.getProductFeatures());
			strBuilder.append(infos.getHybridInformations().size() + "\n");
			for (HybridInformations hybInf : infos.getHybridInformations()) {
				printStringWithLength(strBuilder, hybInf.getVersionName());
				strBuilder.append(hybInf.isInformationForUninstall() + "\n");
				strBuilder.append("" + hybInf.getUpdateInstallDate() + "\n");
				strBuilder.append("" + hybInf.getUpdateReleaseDate() + "\n");
				if (hybInf.getUpdateDescription() == null) {
					printStringWithLength(strBuilder, "");
				} else {
					printStringWithLength(strBuilder, hybInf.getUpdateDescription().replace("\n", "<br>"));
				}
			}
		}
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, strBuilder.toString().toCharArray()));
	}

	private boolean doesCatalogItemMatch(CatalogItem item, String toMatch) {
		String toSearchList[] = toMatch.split(" ");
		boolean considerAsMatch = true;
		for (String toSearch : toSearchList) {
			String toSearchLc = toSearch.toLowerCase();
			boolean foundOne = false;
			foundOne |= item.getProductName().toLowerCase().contains(toSearchLc);
			foundOne |= item.getProductVersion().toLowerCase().contains(toSearchLc);
			foundOne |= item.getProductDescription().toLowerCase().contains(toSearchLc);
			foundOne |= item.getDownloadFileName().toLowerCase().contains(toSearchLc);
			considerAsMatch &= foundOne;
		}
		return considerAsMatch;

	}

	private void handleRequestAgentFetchCatalog(HttpRequestProcessor reqProc, HttpRequest req) {
		Map<String, String> parameters = req.getParameters();
		if (!req.getHttpParameters().containsKey("Origin") || !parameters.containsKey("cookie")
				|| !parameters.containsKey("page") || !parameters.containsKey("sort")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (!isAllowed(req)) {
			reqProc.pushResponse(UNAUTHORIZED_RESPONSE);
			return;
		}
		AgentSession session = AgentMain.getCurrentSession();
		if (session.getState() != AgentSessionState.IDLE) {
			reqProc.pushResponse(INTERNAL_SERVER_ERROR);
			return;
		}
		String searchData = parameters.get("search");
		String descendantSort = parameters.get("descendantSort");
		int pageNum;
		try {
			pageNum = Integer.parseInt(parameters.get("page"));
		} catch (NumberFormatException e) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		String sortType = parameters.get("sort");
		if (!sortType.equalsIgnoreCase("name") && !sortType.equalsIgnoreCase("date")
				&& !sortType.equalsIgnoreCase("version")) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		List<CatalogItem> catalog = session.getCatalogList();
		List<CatalogItem> catalogFiltered = new ArrayList<CatalogItem>();
		if (catalog == null) {
			reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "null".toCharArray()));
			return;
		}
		StringBuilder strBuilder = new StringBuilder();
		for (CatalogItem infos : catalog) {
			if (searchData == null || doesCatalogItemMatch(infos, searchData)) {
				catalogFiltered.add(infos);
			}
		}

		catalogFiltered.sort(new Comparator<CatalogItem>() {

			@Override
			public int compare(CatalogItem o1, CatalogItem o2) {
				if (sortType.equalsIgnoreCase("name")) {
					return o1.getProductName().compareTo(o2.getProductName());
				}
				if (sortType.equalsIgnoreCase("date")) {
					if (o1.getReleaseDate() > o2.getReleaseDate()) {
						return 1;
					}
					if (o1.getReleaseDate() < o2.getReleaseDate()) {
						return -1;
					}
					return 0;
				}
				if (sortType.equalsIgnoreCase("version")) {
					return o1.getProductVersion().compareTo(o2.getProductVersion());
				}
				return 0;
			}
		});
		if ("true".equalsIgnoreCase(descendantSort)) {
			Collections.reverse(catalogFiltered);
		}

		int pagesMax = catalogFiltered.size() / MAX_ENTRIES_PER_CATALOG_PAGE;
		if (catalogFiltered.size() % MAX_ENTRIES_PER_CATALOG_PAGE > 0) {
			pagesMax++;
		}
		strBuilder.append(pagesMax + "\n");

		for (int i = MAX_ENTRIES_PER_CATALOG_PAGE * pageNum; i < Math.min(MAX_ENTRIES_PER_CATALOG_PAGE * (pageNum + 1),
				catalogFiltered.size()); i++) {
			CatalogItem infos = catalogFiltered.get(i);
			strBuilder.append(infos.getReleaseDate() + "\n");
			printStringWithLength(strBuilder, infos.getProductName());
			printStringWithLength(strBuilder, infos.getProductVersion());
			printStringWithLength(strBuilder, infos.getProductDescription().replace("\n", "<br>"));
			printStringWithLength(strBuilder, infos.getDownloadFileName());
		}
		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, strBuilder.toString().toCharArray()));
	}

	private void handleRequestAgentRequestControl(HttpRequestProcessor reqProc, HttpRequest req) {
		String origin = req.getHttpParameters().get("Origin");
		if (origin == null) {
			reqProc.pushResponse(BAD_REQUEST_ERROR);
			return;
		}
		if (AgentMain.getCurrentSession() != null
				&& AgentMain.getCurrentSession().getState() == AgentSessionState.BUSY) {
			reqProc.pushResponse(SERV_UNAVAIL_RESPONSE);
			return;
		}
		if (AgentMain.getPendingSessionsList().get(origin) != null) {
			reqProc.pushResponse(TOO_MANY_REQS_RESPONSE);
			logger.log(CMPN, LogLevel.WARNING, "Another connection from " + req.getHttpParameters().get("Origin"));
			return;
		}
		String key = req.getParameters().get("accessKey");
		SecureRandom secureRandom = new SecureRandom();
		String cookie = Integer.toHexString(secureRandom.nextInt());
		AgentSession session = new AgentSession(cookie, origin, AgentSessionState.PENDING_USER_ACCEPT);
		if (key != null && PersistentAccessKeyUtil.checkKeyValidity(key)) {
			session.setState(AgentSessionState.UNCONFIGURED);
			session.setAllowKeyPersistence(true);
			AgentMain.getAgentPresence().setCurrentConnection(session.getOrigin());
			AgentMain.getSharedLogger().log(CMPN, LogLevel.INFO, "Closing log file for session closure.");
			AgentMain.getSharedLogger().closeFileLogging();
			AgentMain.setCurrentSession(session);
		} else {
			AgentMain.getPendingSessionsList().put(req.getHttpParameters().get("Origin"), session);
			AgentMain.onNewPendingSession();
		}
		logger.log(CMPN, LogLevel.INFO, "Incoming connection from " + req.getHttpParameters().get("Origin"));

		reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, cookie.toCharArray()));
	}

	public void handleSocket() {

		try (HttpRequestProcessor reqProc = new HttpRequestProcessor(soc)) {
			HttpRequest req = reqProc.parseIncomingRequest();
			if (req == null) {
				reqProc.pushResponse(BAD_REQUEST_ERROR);
				return;
			} else {
				switch (req.getUrl().toLowerCase()) {
				case "/agent/status":
					handleRequestAgentStatus(reqProc, req);
					break;
				case "/agent/feed_configuration":
					handleRequestAgentFeedConfiguration(reqProc, req);
					break;
				case "/agent/key_persistence":
					handleRequestAgentKeyPersistence(reqProc, req);
					break;
				case "/agent/control_accepted":
					handleRequestAgentControlAccepted(reqProc, req);
					break;
				case "/agent/request_control":
					handleRequestAgentRequestControl(reqProc, req);
					break;
				case "/agent/search_products":
					handleRequestAgentSearchProducts(reqProc, req);
					break;
				case "/agent/search_results":
					handleRequestAgentSearchResults(reqProc, req);
					break;
				case "/agent/action_results":
					handleRequestAgentActionResults(reqProc, req);
					break;
				case "/agent/actions_history":
					handleRequestAgentActionsHistory(reqProc, req);
					break;
				case "/agent/perform_actions":
					handleRequestAgentPerformActions(reqProc, req);
					break;
				case "/agent/fetch_catalog":
					handleRequestAgentFetchCatalog(reqProc, req);
					break;
				case "/agent/ping":
					reqProc.pushResponse(new HttpResponse("HTTP/1.1", 200, "OK", null, "cuagent".toCharArray()));
					break;
				default:
					reqProc.pushResponse(BAD_REQUEST_ERROR);
					return;
				}
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "IOException while processing request");
		}
	}
}
