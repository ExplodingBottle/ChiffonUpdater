/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class CatalogManager {

	private URI catalogURI;
	private SharedLogger logger;

	private static final String CMPN = "CAMA";

	public CatalogManager(URI backendUri) {
		catalogURI = backendUri.resolve("catalog/");
		logger = AgentMain.getSharedLogger();
	}

	private String readString(DataInputStream dis) throws IOException {
		int stringSize = dis.readInt();
		StringBuilder bdr = new StringBuilder();
		for (int i = 0; i < stringSize; i++) {
			bdr.append(dis.readChar());
		}
		return bdr.toString();
	}

	public List<CatalogItem> loadCatalog() {
		List<CatalogItem> catalogItems = new ArrayList<CatalogItem>();

		logger.log(CMPN, LogLevel.INFO, "Will load the catalog.");
		boolean nextCatalog = true;
		for (int i = 0; nextCatalog; i++) {
			nextCatalog = false;
			URI currentCatFileURI = catalogURI.resolve("catinf" + i + ".chd");
			URL castURL = null;
			try {
				castURL = currentCatFileURI.toURL();
			} catch (MalformedURLException e) {
				logger.log(CMPN, LogLevel.ERROR, "Impossible to parse URL for catalog header " + i);
				return null;
			}
			URLConnection con = null;
			try {
				con = castURL.openConnection();
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.ERROR, "Couldn't open connection for catalog header " + i);
				return null;
			}
			try (DataInputStream dis = new DataInputStream(new GZIPInputStream(con.getInputStream()))) {
				byte header[] = new byte[5];
				int headerRead = dis.read(header, 0, header.length);
				while (headerRead != -1) {
					if (headerRead == header.length) {
						if ("CNTRY".equals(new String(header))) {
							long releaseDate = dis.readLong();
							String productName = readString(dis);
							String versionName = readString(dis);
							String description = readString(dis);
							String downloadFileName = readString(dis);
							catalogItems.add(new CatalogItem(productName, versionName, releaseDate, description,
									downloadFileName));
						} else if ("CNEXT".equals(new String(header))) {
							nextCatalog = true;
							break;
						} else {
							logger.log(CMPN, LogLevel.ERROR, "Catalog header " + i + " contained an invalid entry.");
							return null;
						}
					}
					headerRead = dis.read(header, 0, header.length);
				}

			} catch (Exception e) {
				logger.log(CMPN, LogLevel.ERROR, "Couldn't parse catalog header " + i);
				return null;
			}
			logger.log(CMPN, LogLevel.INFO, "Parsed catalog header " + i);

		}
		
		return catalogItems;

	}

}
