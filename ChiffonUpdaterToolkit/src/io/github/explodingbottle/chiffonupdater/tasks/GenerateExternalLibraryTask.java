/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;

import io.github.explodingbottle.chiffonupdater.ChiffonUpdaterProject;
import io.github.explodingbottle.chiffonupdater.GlobalLogger;
import io.github.explodingbottle.chiffonupdater.JarFileFilter;
import io.github.explodingbottle.chiffonupdater.ToolkitWindow;
import io.github.explodingbottle.chiffonupdater.binpack.Binpack;
import io.github.explodingbottle.chiffonupdater.binpack.BinpackProvider;

public class GenerateExternalLibraryTask extends ToolkitTask {

	public GenerateExternalLibraryTask(ToolkitWindow toolkitWindow) {
		super(toolkitWindow);
	}

	@Override
	public void runTask() {
		ToolkitWindow window = returnWindow();
		GlobalLogger logger = getLogger();
		ChiffonUpdaterProject project = window.getProject();
		BinpackProvider prov = new BinpackProvider();
		Binpack binpack = prov.getBinPack(returnWindow());
		if (binpack == null) {
			return;
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(new File("external.jar"));
		chooser.setFileFilter(new JarFileFilter());
		if (chooser.showDialog(window, "Generate external library") == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			if (!selected.getName().endsWith(".jar")) {
				selected = new File(selected.getParentFile(), selected.getName() + ".jar");
			}
			logger.print("File selected, will extract.");
			Properties properties = project.getMainInfosRef().createPropertiesFromInfos();
			try (FileInputStream fis = new FileInputStream(binpack.getExternalLibraryFile());
					ZipInputStream zipInput = new ZipInputStream(fis);
					FileOutputStream fos = new FileOutputStream(selected);
					ZipOutputStream zipOutput = new ZipOutputStream(fos)) {
				byte[] buffer = new byte[4096];

				ZipEntry entry = zipInput.getNextEntry();
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
				entry = new ZipEntry("provider.properties");
				zipOutput.putNextEntry(entry);
				properties.store(zipOutput, "Generated using the Chiffon Updater Toolkit");
				zipOutput.closeEntry();

			} catch (IOException e) {
				logger.printThrowable(e);
			}
		}
	}

}
