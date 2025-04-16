/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;

public class DetectionFileBuilder {

	private ProductInfo product;

	public DetectionFileBuilder(ProductInfo product) {
		this.product = product;
	}

	public Properties buildDetectionProperties() {
		Properties detectionProps = new Properties();
		detectionProps.setProperty("detection.productname", product.getProductName());
		detectionProps.setProperty("detection.features", String.join(";", product.getFeaturesReference()));

		Map<VersionInfo, String> sectionsPerInfo = new HashMap<VersionInfo, String>();

		int lastSectionNum = 0;

		Map<String, Map<String, Map<VersionInfo, String>>> filesMap = new HashMap<String, Map<String, Map<VersionInfo, String>>>();
		product.getFeaturesReference().forEach(feature -> {
			filesMap.put(feature, new HashMap<String, Map<VersionInfo, String>>());
		});

		for (VersionInfo info : product.getVersionsReference()) {
			String sectionName = "det" + lastSectionNum;
			sectionsPerInfo.put(info, sectionName);
			detectionProps.setProperty("detection.section." + sectionName, info.getVersionName());
			lastSectionNum++;
			info.filesForFeatureMap().forEach((feature, files) -> {
				Map<String, Map<VersionInfo, String>> featureMap = filesMap.get(feature);
				files.getFileHashesListReference().forEach(fileInfos -> {
					if (featureMap.containsKey(fileInfos.getFilePath())) {
						featureMap.get(fileInfos.getFilePath()).put(info, fileInfos.getFileHash());
					} else {
						Map<VersionInfo, String> versions = new HashMap<VersionInfo, String>();
						versions.put(info, fileInfos.getFileHash());
						featureMap.put(fileInfos.getFilePath(), versions);
					}
				});
			});
		}

		sectionsPerInfo.forEach((version, sectionName) -> {
			detectionProps.setProperty(sectionName + ".reportingfile", version.getReportingFileName());
		});

		long[] sectIter = { 0 };

		filesMap.forEach((feature, files) -> {
			files.forEach((fileInfos, versions) -> {
				for (VersionInfo versionInfo : product.getVersionsReference()) {
					String sectionName = sectionsPerInfo.get(versionInfo);
					if (versions.containsKey(versionInfo)) {
						if (versions.get(versionInfo).isEmpty()) {
							detectionProps.setProperty(sectionName + "." + feature + "." + sectIter[0],
									"true;FileExists;" + fileInfos + ";folder");
						} else {
							detectionProps.setProperty(sectionName + "." + feature + "." + sectIter[0],
									versions.get(versionInfo) + ";FileHash;" + fileInfos);
						}
					} else {
						detectionProps.setProperty(sectionName + "." + feature + "." + sectIter[0],
								"false;FileExists;" + fileInfos);
					}
					sectIter[0]++;
				}

			});
		});
		product.getVersionsReference().forEach(vers -> {
			product.getFeaturesReference().forEach(feature -> {
				String cmmd = vers.customDetectionCommandsByFeatureMap().get(feature);
				if (cmmd != null) {
					String splitedCmds[] = cmmd.replace("\r", "").split("\n");
					String sectionName = sectionsPerInfo.get(vers);
					for (String cCmd : splitedCmds) {
						detectionProps.setProperty(sectionName + "." + feature + "." + sectIter[0], cCmd);
						sectIter[0]++;
					}
				}
			});
		});

		return detectionProps;
	}

}
