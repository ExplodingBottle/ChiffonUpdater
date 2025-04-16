/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;

public class UpdateFileBuilder {
	private ProductInfo product;
	private VersionInfo targetVersion;

	public UpdateFileBuilder(ProductInfo product, VersionInfo targetVersion) {
		this.product = product;
		this.targetVersion = targetVersion;
	}

	private List<FileInfos> buildFullFileInfosList(List<FileInfos> partialList) {
		List<FileInfos> builtFileList = new ArrayList<FileInfos>();

		ArrayList<String> dirs = new ArrayList<String>();
		for (FileInfos finfo : partialList) {
			String cutStrings[] = finfo.getFilePath().split("/");
			int max = finfo.getFileHash().isEmpty() ? cutStrings.length : cutStrings.length - 1;
			String built = "";
			for (int i = 0; i < max; i++) {
				built += (i != 0 ? "/" : "") + cutStrings[i];
				if (!dirs.contains(built)) {
					dirs.add(built);
				}
			}
			if (!finfo.getFileHash().isEmpty()) {
				builtFileList.add(finfo);
			}
		}
		for (String dir : dirs) {
			builtFileList.add(new FileInfos(dir, ""));
		}
		return builtFileList;
	}

	public UpdateDirectives buildUpdateDirectives() {
		Properties updateProperties = new Properties();
		updateProperties.setProperty("update.productname", product.getProductName());
		updateProperties.setProperty("update.targetversion", targetVersion.getVersionName());
		updateProperties.setProperty("update.releasedate", "" + targetVersion.getReleaseDate());
		boolean canShowDesc = targetVersion.getShowVersionDescription();
		if (targetVersion.getVersionDescription() != null && !targetVersion.getVersionDescription().isEmpty()) {
			updateProperties.setProperty("update.description", targetVersion.getVersionDescription());
		} else {
			canShowDesc = false;
		}
		updateProperties.setProperty("update.show.description", canShowDesc ? "1" : "0");

		if (targetVersion.getCustomPrerequisiteCommands() != null
				&& !targetVersion.getCustomPrerequisiteCommands().isEmpty()) {
			int i = 0;
			for (String cmd : targetVersion.getCustomPrerequisiteCommands().replace("\r", "").split("\n")) {
				updateProperties.setProperty("update.prerequisites." + i, cmd);
				i++;
			}
		}
		if (!product.getUninstallerName().trim().isEmpty() && !product.doNotCreateUninstaller()) {
			updateProperties.setProperty("update.uninstaller.name", product.getUninstallerName());
		}
		if (targetVersion.getCustomEulaPath() != null && !targetVersion.getCustomEulaPath().isEmpty()) {
			updateProperties.setProperty("update.eula", "eula.txt");
		}
		if (targetVersion.getCustomModulePath() != null && !targetVersion.getCustomModulePath().isEmpty()
				&& targetVersion.getCustomModulePublisher() != null
				&& !targetVersion.getCustomModulePublisher().isEmpty()) {
			updateProperties.setProperty("update.custom", "cumodule.jar;" + targetVersion.getCustomModulePublisher());
		}
		List<VersionInfo> canUpdVersions = new ArrayList<VersionInfo>();
		product.getVersionsReference().forEach(vers -> {
			if ((vers.getIterationNumber() < targetVersion.getIterationNumber()) && vers != targetVersion) {
				canUpdVersions.add(vers);
			}
		});

		long[] fileIter = { 0 };
		long[] sectIter = { 0 };
		Map<String, String> mappedFiles = new HashMap<String, String>();
		Map<String, String> mappedFilesFeatures = new HashMap<String, String>();

		// TODO: BUG BUG BUG
		// Happens because if the same file
		canUpdVersions.forEach(version -> {

			List<FileInfosComp> filesRemoved = new ArrayList<FileInfosComp>();
			List<FileInfosComp> filesAdded = new ArrayList<FileInfosComp>();
			List<FileInfosComp> filesChanged = new ArrayList<FileInfosComp>();

			targetVersion.filesForFeatureMap().forEach((feature, tvfIndirect) -> {
				/*
				 * System.out.println("Comparing " + version.getVersionName() + " -> " +
				 * targetVersion.getVersionName() + " on feature " + feature);
				 */
				List<FileInfos> targetVersionFiles = buildFullFileInfosList(tvfIndirect.getFileHashesListReference());
				List<FileInfos> testVersionFiles = buildFullFileInfosList(
						version.filesForFeatureMap().get(feature).getFileHashesListReference());

				testVersionFiles.forEach(fileInfosTest -> {
					String differentHash = null;
					boolean foundExact = false;
					boolean foundDifferent = false;
					for (FileInfos fileInfosTarget : targetVersionFiles) {
						if (fileInfosTest.getFileHash().equals(fileInfosTarget.getFileHash())
								&& fileInfosTest.getFilePath().equals(fileInfosTarget.getFilePath())) {
							foundExact = true;
							break;
						}
						if (!fileInfosTest.getFileHash().equals(fileInfosTarget.getFileHash())
								&& fileInfosTest.getFilePath().equals(fileInfosTarget.getFilePath())) {
							foundDifferent = true;
							differentHash = fileInfosTarget.getFileHash();
							break;
						}
					}
					if (foundDifferent) {
						// System.out.println(fileInfosTest.getFilePath() + " modified !");
						filesChanged.add(
								new FileInfosComp(new FileInfos(fileInfosTest.getFilePath(), differentHash), feature));
					}
					if (!foundDifferent && !foundExact) {
						// System.out.println(fileInfosTest.getFilePath() + " removed !");
						filesRemoved.add(new FileInfosComp(fileInfosTest, feature));
					}
				});
				for (FileInfos fileInfosTarget : targetVersionFiles) {
					boolean foundAdded = false;
					for (FileInfos fileInfosTest : testVersionFiles) {
						if (fileInfosTarget.getFilePath().equals(fileInfosTest.getFilePath())) {
							foundAdded = true;
							break;
						}
					}
					if (!foundAdded) {
						// System.out.println(fileInfosTarget.getFilePath() + " added !");
						filesAdded.add(new FileInfosComp(fileInfosTarget, feature));
					}
				}

			});
			FolderTreeComparator cmptr = new FolderTreeComparator(false);
			FolderTreeComparator cmptr2 = new FolderTreeComparator(true);
			filesRemoved.sort(cmptr2);
			filesAdded.sort(cmptr);
			filesChanged.sort(cmptr);
			for (FileInfosComp cFileAdded : filesAdded) {
				if (cFileAdded.getInfos().getFileHash().isEmpty()) {
					updateProperties.setProperty(
							"update." + cFileAdded.getFeature() + "." + version.getVersionName() + "." + sectIter[0],
							"FolderCreate;" + cFileAdded.getInfos().getFilePath());
				} else {
					String bFile = mappedFiles.get(cFileAdded.getInfos().getFileHash());
					if (bFile == null) {
						bFile = "data" + fileIter[0] + ".upd";
						mappedFiles.put(cFileAdded.getInfos().getFileHash(), bFile);
						mappedFilesFeatures.put(cFileAdded.getInfos().getFileHash(), cFileAdded.getFeature());
						fileIter[0]++;
					}
					updateProperties.setProperty(
							"update." + cFileAdded.getFeature() + "." + version.getVersionName() + "." + sectIter[0],
							"FileCopy;" + bFile + ";" + cFileAdded.getInfos().getFilePath());
				}
				sectIter[0]++;
			}
			for (FileInfosComp cFileChanged : filesChanged) {
				String bFile = mappedFiles.get(cFileChanged.getInfos().getFileHash());
				if (bFile == null) {
					bFile = "data" + fileIter[0] + ".upd";
					mappedFiles.put(cFileChanged.getInfos().getFileHash(), bFile);
					mappedFilesFeatures.put(cFileChanged.getInfos().getFileHash(), cFileChanged.getFeature());
					fileIter[0]++;
				}
				updateProperties.setProperty(
						"update." + cFileChanged.getFeature() + "." + version.getVersionName() + "." + sectIter[0],
						"FileCopy;" + bFile + ";" + cFileChanged.getInfos().getFilePath());
				sectIter[0]++;
			}
			for (FileInfosComp cFileRemoved : filesRemoved) {
				updateProperties.setProperty(
						"update." + cFileRemoved.getFeature() + "." + version.getVersionName() + "." + sectIter[0],
						"FileDelete;" + cFileRemoved.getInfos().getFilePath());
				sectIter[0]++;
			}
			for (String feature : targetVersion.getParent().getFeaturesReference()) {
				String cmmd = targetVersion.customActionCommandsByFeatureMap().get(feature);
				if (cmmd != null) {
					String splitedCmds[] = cmmd.replace("\r", "").split("\n");
					for (String cCmd : splitedCmds) {
						updateProperties.setProperty(
								"update." + feature + "." + version.getVersionName() + "." + sectIter[0], cCmd);
						sectIter[0]++;
					}
				}
			}

		});
		return new UpdateDirectives(updateProperties, mappedFiles, mappedFilesFeatures);
	}
}
