/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;

import io.github.explodingbottle.chiffonupdater.binpack.Binpack;
import io.github.explodingbottle.chiffonupdater.binpack.BinpackProvider;
import io.github.explodingbottle.chiffonupdater.project.CustomizationInfos;
import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;
import io.github.explodingbottle.chiffonupdater.tasks.ToolkitTask;

public class StandalonePackageBuilder extends ToolkitTask {

	private ProductInfo targetProduct;
	private VersionInfo targetVersion;
	private Map<String, File> files;
	private File eulaFile;
	private File moduleFile;
	private Binpack binpack;
	private File targetFolder;
	private PackageGenerationReport report;
	private File targetZipFile;
	private File realTarget;
	private ToolkitWindow toolkitWindow;

	public StandalonePackageBuilder(ToolkitWindow toolkitWindow, ProductInfo targetProduct, VersionInfo targetVersion,
			Map<String, File> files, File eulaFile, File moduleFile, Binpack binpack, File targetFolder,
			PackageGenerationReport report, File targetZipFile) {
		super(toolkitWindow);
		this.targetProduct = targetProduct;
		this.targetVersion = targetVersion;
		this.files = files;
		this.eulaFile = eulaFile;
		this.moduleFile = moduleFile;
		this.binpack = binpack;
		this.targetFolder = targetFolder;
		this.report = report;
		this.targetZipFile = targetZipFile;
		this.toolkitWindow = toolkitWindow;
	}

	private static final String sanitizeString(String source) {
		return source.replace(".", "_");
	}

	public File setTargetAndGetFile() {
		realTarget = new File(targetFolder, sanitizeString(targetProduct.getProductName()) + "_"
				+ sanitizeString(targetVersion.getVersionName()) + "");
		return realTarget;
	}

	private static final boolean compareInputStreamAndProperties(InputStream toCompare, Properties comparer)
			throws IOException {
		Properties tcProps = new Properties();
		tcProps.load(toCompare);
		return tcProps.equals(comparer);
	}

	private static final boolean compareInputAndHash(InputStream toCompare, String hash) throws IOException {
		return hash.equalsIgnoreCase(
				new HashComputer(toCompare, new SharedLoggerAdapter(ToolkitMain.getGlobalLogger())).computeHash());
	}

	private boolean stringNotNullAndNotEmpty(String s) {
		return s != null && !s.trim().isEmpty();
	}

	@Override
	public void runTask() {
		Binpack selectedBinpack = binpack;
		if (binpack == null && targetZipFile == null) {
			BinpackProvider provider = new BinpackProvider();
			selectedBinpack = provider.getBinPack(returnWindow());
		}
		if (selectedBinpack != null || targetZipFile != null) {
			int returnVal = JFileChooser.APPROVE_OPTION;
			File targetFile = null;
			if (realTarget != null) {
				targetFile = realTarget;
			} else {
				if (report == null && targetZipFile == null) {
					if (targetFolder != null) {
						targetFile = new File(targetFolder, sanitizeString(targetProduct.getProductName()) + "_"
								+ sanitizeString(targetVersion.getVersionName()) + ".jar");
					} else {
						JFileChooser chooser = new JFileChooser();
						chooser.setSelectedFile(new File(sanitizeString(targetProduct.getProductName()) + "_"
								+ sanitizeString(targetVersion.getVersionName()) + ".jar"));
						returnVal = chooser.showDialog(returnWindow(), "Select package");
						targetFile = chooser.getSelectedFile();
					}
				}
			}

			File packageIconSourceFile = null;
			File packageBannerSourceFile = null;

			CustomizationInfos custInfos = targetProduct.getParent().getMainInfosRef().getCustomizationInfos();
			if (stringNotNullAndNotEmpty(custInfos.getCustomPackageIconPath())) {
				packageIconSourceFile = new File(custInfos.getCustomPackageIconPath());
				if (toolkitWindow.getCurrentConnectedSource() != null) {
					packageIconSourceFile = new File(toolkitWindow.getCurrentConnectedSource(),
							custInfos.getCustomPackageIconPath());
				}
			}
			if (stringNotNullAndNotEmpty(custInfos.getCustomPackageBannerPath())) {
				packageBannerSourceFile = new File(custInfos.getCustomPackageBannerPath());
				if (toolkitWindow.getCurrentConnectedSource() != null) {
					packageBannerSourceFile = new File(toolkitWindow.getCurrentConnectedSource(),
							custInfos.getCustomPackageBannerPath());
				}
			}

			DetectionFileBuilder builder = new DetectionFileBuilder(targetProduct);
			IterationsFileBuilder builder2 = new IterationsFileBuilder(targetProduct);
			UpdateFileBuilder builder3 = new UpdateFileBuilder(targetProduct, targetVersion);
			Properties providerProperties = targetProduct.getParent().getMainInfosRef().createPropertiesFromInfos();
			Properties iterationsProperties = builder2.buildIterationsProperties();
			Properties detectionProperties = builder.buildDetectionProperties();
			UpdateDirectives updateDirectives = builder3.buildUpdateDirectives();

			Properties customizationProperties = null;
			if (stringNotNullAndNotEmpty(custInfos.getCustomPackageIconPath())
					|| stringNotNullAndNotEmpty(custInfos.getCustomPackageBannerPath())
					|| stringNotNullAndNotEmpty(custInfos.getCustomPackageTitle())) {
				customizationProperties = new Properties();
				if (stringNotNullAndNotEmpty(custInfos.getCustomPackageIconPath())) {
					customizationProperties.setProperty("package.customization.icon", "package_custom_icon.png");
				}
				if (stringNotNullAndNotEmpty(custInfos.getCustomPackageBannerPath())) {
					customizationProperties.setProperty("package.customization.banner", "package_custom_banner.png");
				}
				if (stringNotNullAndNotEmpty(custInfos.getCustomPackageTitle())) {
					customizationProperties.setProperty("package.customization.title",
							custInfos.getCustomPackageTitle());
				}
			}

			boolean changeRegistered = true;
			if (targetZipFile != null) {
				changeRegistered = false;
				targetFile = targetZipFile;
				if (!targetFile.exists()) {
					changeRegistered = true;
				} else {
					try (FileInputStream fis = new FileInputStream(targetFile);
							ZipInputStream zipInput = new ZipInputStream(fis);) {

						String eulaHash = "";
						String cuModulehash = "";
						String pIcoHash = "";
						String bBnrHash = "";
						if (eulaFile != null) {
							eulaHash = new HashComputer(eulaFile,
									new SharedLoggerAdapter(ToolkitMain.getGlobalLogger())).computeHash();
						}
						if (moduleFile != null) {
							cuModulehash = new HashComputer(moduleFile,
									new SharedLoggerAdapter(ToolkitMain.getGlobalLogger())).computeHash();
						}
						if (targetZipFile == null) {
							if (packageIconSourceFile != null) {
								pIcoHash = new HashComputer(packageIconSourceFile,
										new SharedLoggerAdapter(ToolkitMain.getGlobalLogger())).computeHash();
							}
							if (packageBannerSourceFile != null) {
								bBnrHash = new HashComputer(packageBannerSourceFile,
										new SharedLoggerAdapter(ToolkitMain.getGlobalLogger())).computeHash();
							}
						}
						Map<String, String> updateFilesHashes = new HashMap<String, String>();
						List<String> requiredFiles = new ArrayList<String>();
						files.forEach((insHash, realFile) -> {
							String targetArchivePath = updateDirectives.getFileNamesMap().get(insHash);
							String targetFeature = updateDirectives.getFileFeaturesMap().get(insHash);
							if (targetFeature != null) {
								if (targetArchivePath != null) {
									requiredFiles.add(targetArchivePath);
									updateFilesHashes.put(targetArchivePath, insHash);
								}
							}

						});
						ZipEntry entry = zipInput.getNextEntry();
						List<String> itemFiles = new ArrayList<String>();
						while (entry != null) {
							itemFiles.add(entry.getName().toLowerCase());
							String entryNameLc = entry.getName().toLowerCase();
							switch (entryNameLc) {
							case "provider.properties":
								changeRegistered |= !compareInputStreamAndProperties(zipInput, providerProperties);
								break;
							case "detection.properties":
								changeRegistered |= !compareInputStreamAndProperties(zipInput, detectionProperties);
								break;
							case "update.properties":
								changeRegistered |= !compareInputStreamAndProperties(zipInput,
										updateDirectives.getUpdateProperties());
								break;
							case "customization.properties":
								if (targetZipFile == null) {
									if (customizationProperties != null) {
										changeRegistered |= !compareInputStreamAndProperties(zipInput,
												customizationProperties);
									} else {
										changeRegistered = true;
									}
									break;
								}
							case "iterations.properties":
								changeRegistered |= !compareInputStreamAndProperties(zipInput, iterationsProperties);
								break;
							case "eula.txt":
								if (eulaFile != null) {
									changeRegistered |= !compareInputAndHash(zipInput, eulaHash);
								} else {
									changeRegistered = true;
								}
								break;
							case "cumodule.jar":
								if (eulaFile != null) {
									changeRegistered |= !compareInputAndHash(zipInput, cuModulehash);
								} else {
									changeRegistered = true;
								}
								break;
							case "package_custom_icon.png":
								if (targetZipFile == null) {
									if (packageIconSourceFile != null) {
										changeRegistered |= !compareInputAndHash(zipInput, pIcoHash);
									} else {
										changeRegistered = true;
									}
									break;
								}
							case "package_custom_banner.png":
								if (targetZipFile == null) {
									if (packageBannerSourceFile != null) {
										changeRegistered |= !compareInputAndHash(zipInput, bBnrHash);
									} else {
										changeRegistered = true;
									}
									break;
								}
							default:
								if (!updateFilesHashes.containsKey(entryNameLc)) {
									changeRegistered = true;
									break;
								}
								changeRegistered |= !compareInputAndHash(zipInput, updateFilesHashes.get(entryNameLc));
							}
							zipInput.closeEntry();
							entry = zipInput.getNextEntry();
						}
						requiredFiles.add("provider.properties");
						requiredFiles.add("detection.properties");
						requiredFiles.add("update.properties");
						requiredFiles.add("iterations.properties");
						if (eulaFile != null) {
							requiredFiles.add("eula.txt");
						}
						if (moduleFile != null) {
							requiredFiles.add("cumodule.jar");
						}
						if (targetZipFile == null) {
							if (packageIconSourceFile != null) {
								requiredFiles.add("package_custom_icon.png");
							}
							if (packageBannerSourceFile != null) {
								requiredFiles.add("package_custom_banner.png");
							}
							if (customizationProperties != null) {
								requiredFiles.add("customization.properties");
							}
						}

						changeRegistered |= !itemFiles.containsAll(requiredFiles);
					} catch (IOException e) {
						getLogger().printThrowable(e);
						return;
					}
				}

			}

			if (!changeRegistered) {
				ToolkitMain.getGlobalLogger().print("Package for " + targetProduct.getProductName() + " with version "
						+ targetVersion.getVersionName() + " doesn't require refresh.");
			} else {
				ToolkitMain.getGlobalLogger().print("Package for " + targetProduct.getProductName() + " with version "
						+ targetVersion.getVersionName() + " requires refresh.");
			}
			if (report != null) {
				report.setPackageChanged(changeRegistered);
			}

			if (returnVal == JFileChooser.APPROVE_OPTION && changeRegistered) {

				try (FileOutputStream fos = new FileOutputStream(targetFile);
						ZipOutputStream zipOutput = new ZipOutputStream(fos);
						FileInputStream fis = targetZipFile == null
								? new FileInputStream(selectedBinpack.getSelfExtractFile())
								: null;
						ZipInputStream zipInput = targetZipFile == null ? new ZipInputStream(fis) : null;) {

					zipOutput.setLevel(Deflater.BEST_COMPRESSION);

					byte[] buffer = new byte[4096];

					ZipEntry entry = null;

					String updateFileFoldersPrefix = targetZipFile == null ? "update/" : "";

					if (targetZipFile == null) {
						entry = zipInput.getNextEntry();
						while (entry != null) {
							zipOutput.putNextEntry(entry);
							int length = zipInput.read(buffer, 0, buffer.length);
							while (length != -1) {
								zipOutput.write(buffer, 0, length);
								length = zipInput.read(buffer, 0, buffer.length);
							}
							zipOutput.closeEntry();
							entry = zipInput.getNextEntry();
						}
					}

					entry = new ZipEntry(updateFileFoldersPrefix + "provider.properties");
					zipOutput.putNextEntry(entry);
					providerProperties.store(zipOutput, "Generated using the Chiffon Updater Toolkit");
					zipOutput.closeEntry();

					entry = new ZipEntry(updateFileFoldersPrefix + "detection.properties");
					zipOutput.putNextEntry(entry);
					detectionProperties.store(zipOutput, "Generated using the Chiffon Updater Toolkit");
					zipOutput.closeEntry();

					entry = new ZipEntry(updateFileFoldersPrefix + "update.properties");
					zipOutput.putNextEntry(entry);
					updateDirectives.getUpdateProperties().store(zipOutput,
							"Generated using the Chiffon Updater Toolkit");
					zipOutput.closeEntry();

					entry = new ZipEntry(updateFileFoldersPrefix + "iterations.properties");
					zipOutput.putNextEntry(entry);
					iterationsProperties.store(zipOutput, "Generated using the Chiffon Updater Toolkit");
					zipOutput.closeEntry();

					if (targetZipFile == null) {

						if (customizationProperties != null) {
							entry = new ZipEntry(updateFileFoldersPrefix + "customization.properties");
							zipOutput.putNextEntry(entry);
							customizationProperties.store(zipOutput, "Generated using the Chiffon Updater Toolkit");
							zipOutput.closeEntry();
						}
						if (packageBannerSourceFile != null) {
							entry = new ZipEntry(updateFileFoldersPrefix + "package_custom_banner.png");
							zipOutput.putNextEntry(entry);
							if (!WebsiteBackendSyncTask.writeToZipOutputStream(getLogger(), packageBannerSourceFile,
									zipOutput)) {
								if (report != null) {
									report.setPackageCreationFailed(true);
								}
								return;
							}
							zipOutput.closeEntry();
						}
						if (packageIconSourceFile != null) {
							entry = new ZipEntry(updateFileFoldersPrefix + "package_custom_icon.png");
							zipOutput.putNextEntry(entry);
							if (!WebsiteBackendSyncTask.writeToZipOutputStream(getLogger(), packageIconSourceFile,
									zipOutput)) {
								if (report != null) {
									report.setPackageCreationFailed(true);
								}
								return;
							}
							zipOutput.closeEntry();
						}

						entry = new ZipEntry(updateFileFoldersPrefix + "update.jar");
						zipOutput.putNextEntry(entry);
						if (!WebsiteBackendSyncTask.writeToZipOutputStream(getLogger(),
								selectedBinpack.getStandalonePackageFile(), zipOutput)) {
							if (report != null) {
								report.setPackageCreationFailed(true);
							}
							return;
						}
						zipOutput.closeEntry();
					}

					if (eulaFile != null) {
						entry = new ZipEntry(updateFileFoldersPrefix + "eula.txt");
						zipOutput.putNextEntry(entry);
						if (!WebsiteBackendSyncTask.writeToZipOutputStream(getLogger(), eulaFile, zipOutput)) {
							if (report != null) {
								report.setPackageCreationFailed(true);
							}
							return;
						}
						zipOutput.closeEntry();
					}

					if (moduleFile != null) {
						entry = new ZipEntry(updateFileFoldersPrefix + "cumodule.jar");
						zipOutput.putNextEntry(entry);
						if (!WebsiteBackendSyncTask.writeToZipOutputStream(getLogger(), moduleFile, zipOutput)) {
							if (report != null) {
								report.setPackageCreationFailed(true);
							}
							return;
						}
						zipOutput.closeEntry();
					}
					if (targetZipFile == null) {
						entry = new ZipEntry(updateFileFoldersPrefix + "pversions.properties");
						zipOutput.putNextEntry(entry);
						if (!WebsiteBackendSyncTask.writeToZipOutputStream(getLogger(),
								selectedBinpack.getPackageVersionsFile(), zipOutput)) {
							if (report != null) {
								report.setPackageCreationFailed(true);
							}
							return;
						}
						zipOutput.closeEntry();

						Properties autoExtractProperties = new Properties();
						autoExtractProperties.setProperty("content", "update");
						autoExtractProperties.setProperty("mainJar", "update.jar");

						entry = new ZipEntry("extract.properties");
						zipOutput.putNextEntry(entry);
						autoExtractProperties.store(zipOutput, "Generated using the Chiffon Updater Toolkit");
						zipOutput.closeEntry();
					}
					byte buffer2[] = new byte[4096];
					files.forEach((insHash, realFile) -> {
						String targetArchivePath = updateDirectives.getFileNamesMap().get(insHash);
						if (targetArchivePath == null)
							return;
						try (FileInputStream fis2 = new FileInputStream(realFile)) {
							ZipEntry entry2 = new ZipEntry(updateFileFoldersPrefix + targetArchivePath);
							zipOutput.putNextEntry(entry2);
							int length = fis2.read(buffer2, 0, buffer2.length);
							while (length != -1) {
								zipOutput.write(buffer2, 0, length);
								length = fis2.read(buffer2, 0, buffer2.length);
							}
						} catch (IOException e) {
							getLogger().printThrowable(e);

							if (report != null) {
								report.setPackageCreationFailed(true);
							}
						}
						try {
							zipOutput.closeEntry();
						} catch (IOException e) {
							getLogger().printThrowable(e);
						}
					});

				} catch (IOException e) {

					if (report != null) {
						report.setPackageCreationFailed(true);
					}
					getLogger().printThrowable(e);
					return;
				}
				ToolkitMain.getGlobalLogger().print("Finished building package for " + targetProduct.getProductName()
						+ " with version " + targetVersion.getVersionName());

			}
		}
	}

}
