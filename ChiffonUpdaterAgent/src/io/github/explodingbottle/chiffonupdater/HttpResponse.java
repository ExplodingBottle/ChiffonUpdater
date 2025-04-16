/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.Map;

public class HttpResponse {
	private String httpVersion;
	private int code;
	private String message;
	private Map<String, String> httpParams;
	private char[] content;

	public HttpResponse(String httpVersion, int code, String message, Map<String, String> httpParams, char content[]) {
		this.httpVersion = httpVersion;
		this.code = code;
		this.httpParams = httpParams;
		this.content = content;
		this.message = message;
	}

	public char[] getContent() {
		return content;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Map<String, String> getHttpParams() {
		return httpParams;
	}
}
