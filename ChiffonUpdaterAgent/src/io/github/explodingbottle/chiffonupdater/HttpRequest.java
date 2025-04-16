/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.Map;

public class HttpRequest {
	private String url;
	private Map<String, String> parameters;
	private Map<String, String> httpParameters;

	public HttpRequest(String url, Map<String, String> parameters, Map<String, String> httpParameters) {
		this.url = url;
		this.parameters = parameters;
		this.httpParameters = httpParameters;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public Map<String, String> getHttpParameters() {
		return httpParameters;
	}

}
