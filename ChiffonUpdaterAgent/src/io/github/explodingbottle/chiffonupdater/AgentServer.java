/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class AgentServer extends Thread {

	private SharedLogger logger;
	private static final String CMPN = "AGSE";
	private static final int DEFAULT_PORT = 17458;
	private static final int MAX_RETRY_ITERATIONS = 20;
	private Runnable endCallback;

	public AgentServer(Runnable endCallback) {
		logger = AgentMain.getSharedLogger();
		this.endCallback = endCallback;
	}

	public void run() {
		ServerSocket soc = null;
		try {
			for (int i = 0; i < MAX_RETRY_ITERATIONS && soc == null; i++) {
				try {
					soc = new ServerSocket(DEFAULT_PORT + i, 10, InetAddress.getLoopbackAddress());
					soc.setSoTimeout(1000);
				} catch (BindException e) {
					logger.log(CMPN, LogLevel.WARNING, "Failed to bind on port " + (DEFAULT_PORT + i));
				}
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "IOException when finding a port");
		}
		if (soc != null) {
			logger.log(CMPN, LogLevel.INFO, "Bound on port " + soc.getLocalPort());
			while (!interrupted()) {
				Socket aSoc;
				try {
					aSoc = soc.accept();
				} catch (SocketTimeoutException e) {
					continue;
				} catch (IOException e) {
					logger.log(CMPN, LogLevel.WARNING, "IOException while accepting socket");
					continue;
				}
				RequestHandler handler = new RequestHandler(aSoc);
				handler.handleSocket();
				try {
					aSoc.close();
				} catch (IOException e) {
					logger.log(CMPN, LogLevel.WARNING, "IOException while closing socket of " + aSoc.getPort());
				}
			}
		}
		try {
			soc.close();
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "IOException while closing server socket.");
		}
		if (endCallback != null) {
			endCallback.run();
		}
	}

}
