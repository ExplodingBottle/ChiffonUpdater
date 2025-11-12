/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestProcessor implements Closeable {

	private SharedLogger logger;
	private static final String CMPN = "HTRP";
	private BufferedReader reader;
	private BufferedWriter writer;

	public HttpRequestProcessor(Socket req) throws IOException {
		logger = AgentMain.getSharedLogger();
		writer = new BufferedWriter(new OutputStreamWriter(req.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
	}

	public boolean pushResponse(HttpResponse response) {
		try {
			writer.write(response.getHttpVersion() + " " + response.getCode() + " " + response.getMessage() + "\n");
			List<String> toWrite = new ArrayList<String>();
			if (response.getHttpParams() != null) {
				response.getHttpParams().forEach((k, v) -> {
					toWrite.add(k + ": " + v + "\n");
				});
			}
			toWrite.add("Connection: close\n");
			toWrite.add("Access-Control-Allow-Origin: *\n");
			toWrite.add("Content-Type: text/plain\n");
			for (String line : toWrite) {
				writer.write(line);
			}
			writer.write("\n");
			if (response.getContent() != null) {
				writer.write(response.getContent());
			}
			writer.flush();
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to acquire the output stream.");
			return false;
		}
		return true;
	}

	public HttpRequest parseIncomingRequest() {
		try {
			String line = reader.readLine();
			boolean firstLine = true;
			Map<String, String> parameters = new HashMap<String, String>();
			Map<String, String> httpParameters = new HashMap<String, String>();
			String pureUrl = null;
			while (line != null) {
				if (firstLine) {
					String split[] = line.split(" ");
					if (split.length == 3) {
						String method = split[0];
						String uri = split[1];
						String httpVersion = split[2];
						if (method.equalsIgnoreCase("GET") && httpVersion.toUpperCase().startsWith("HTTP/")) {
							String splitUrl[] = uri.split("\\?");
							pureUrl = splitUrl[0];
							if (splitUrl.length > 1) {
								String parametersSplit[] = splitUrl[1].split("&");
								for (String param : parametersSplit) {
									String pSplit[] = param.split("=");
									if (pSplit.length >= 2) {
										String recSplit = "";
										for (int i = 1; i < pSplit.length; i++) {
											recSplit += pSplit[i];
										}
										parameters.put(URLDecoder.decode(pSplit[0], "UTF-8"),
												URLDecoder.decode(recSplit, "UTF-8"));
									}
								}
							}
						} else {
							return null;
						}
					} else {
						return null;
					}
					firstLine = false;
				} else {
					if (!line.isEmpty()) {
						String split[] = line.split(":");
						String regenerated = "";
						if (split.length >= 2) {
							for (int i = 1; i < split.length; i++) {
								regenerated += split[i] + (i == split.length - 1 ? "" : ":");
							}
							httpParameters.put(split[0].trim(), regenerated.trim());
						}
					} else {
						break;
					}
				}
				firstLine = false;
				line = reader.readLine();
			}
			if (firstLine) {
				return null;
			}
			return new HttpRequest(pureUrl, parameters, httpParameters);
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to acquire the input stream.");
			return null;
		}

	}

	@Override
	public void close() throws IOException {
		reader.close();
		writer.close();
	}

}
