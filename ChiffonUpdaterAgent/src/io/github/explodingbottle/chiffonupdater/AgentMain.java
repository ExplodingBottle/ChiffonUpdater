/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class AgentMain {

	private static SharedLogger logger;
	private static Translator translator;
	private static FunctionsGatherer sharedFunctions;
	private static AgentSession currentSession;
	private static Map<String, AgentSession> pendingSessions;
	private static WebsiteAccessPopup websiteAccessPopup;
	private static OperatingSystemDetector sysDetec;
	private static ImageIcon agentIcon;
	private static AgentPresence presence;
	private static AgentServer server;
	private static String licenseText;

	private static final String CMPN = "AGMN";

	public static AgentSession getCurrentSession() {
		return currentSession;
	}

	public static ImageIcon getAgentIcon() {
		return agentIcon;
	}

	public static AgentPresence getAgentPresence() {
		return presence;
	}

	public static AgentServer getAgentServer() {
		return server;
	}

	public static Map<String, AgentSession> getPendingSessionsList() {
		return pendingSessions;
	}

	public static String getLicenseText() {
		return licenseText;
	}

	public static void onNewPendingSession() {
		if (websiteAccessPopup == null) {
			websiteAccessPopup = new WebsiteAccessPopup();
			websiteAccessPopup.handleTask();
		}
	}

	public static OperatingSystemDetector getOperatingSystemDetector() {
		return sysDetec;
	}

	public static void clearCurrentWebsiteAccessPopup() {
		websiteAccessPopup = null;
	}

	public static void setCurrentSession(AgentSession currentSession) {
		AgentMain.currentSession = currentSession;
	}

	public static SharedLogger getSharedLogger() {
		return logger;
	}

	public static Translator getTranslator() {
		return translator;
	}

	public static FunctionsGatherer getSharedFunctions() {
		return sharedFunctions;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {

		}

		logger = new SharedLogger(null, true);
		pendingSessions = new HashMap<String, AgentSession>();
		logger.log(CMPN, LogLevel.INFO, "Logger ready.");
		sysDetec = new OperatingSystemDetector();
		sysDetec.detectOperatingSystem();
		translator = new Translator(new ModularTranslatorConfiguration("translations/${lang}.properties"),
				AgentMain.class.getClassLoader());
		URL iconURL = AgentMain.class.getClassLoader().getResource("images/icon.png");
		if (iconURL != null) {
			agentIcon = new ImageIcon(iconURL);
		}

		InputStream licenseStream = AgentMain.class.getClassLoader().getResourceAsStream("licenses/mit_license.txt");
		if (licenseStream == null) {
			logger.log(CMPN, LogLevel.ERROR, "Couldn't open license.");
			return;
		}
		char[] cbuff = new char[4096];
		StringBuilder licBldr = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(licenseStream))) {
			int read = br.read(cbuff, 0, cbuff.length);
			while (read != -1) {
				licBldr.append(cbuff, 0, read);
				read = br.read(cbuff, 0, cbuff.length);
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.ERROR, "Couldn't read license.");
			return;
		}
		licenseText = licBldr.toString();

		sharedFunctions = new FunctionsGatherer();
		new StandaloneUpdaterFunctionsModule(logger).publishModuleFunctions(sharedFunctions);

		presence = new AgentPresence();
		presence.manifestPresence();

		server = new AgentServer(() -> {
			AgentMain.getSharedLogger().log(CMPN, LogLevel.INFO, "Closing log file because the agent is leaving.");
			AgentMain.getSharedLogger().closeFileLogging();
			presence.permanentlyUnmanifest();
		});
		server.start();

		// Hacky
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JOptionPane.showMessageDialog(frame, translator.getTranslation("cua.open.text"),
				translator.getTranslation("cua.open.title"), JOptionPane.INFORMATION_MESSAGE);
		frame.dispose();
	}

}
