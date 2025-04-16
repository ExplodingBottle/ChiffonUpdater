/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class JarJumpUtil {
	public static boolean jumpToOther(File recent, File updateFolder, String productDir, String noConsole,
			String noGraphics, String uninstVers, String noVersCheck, String custLogFile, String noLogging,
			String eulaAccept) {
		List<String> unbuiltCommand = new ArrayList<String>();

		File javaHome = new File(System.getProperty("java.home"));
		File javaBin = new File(javaHome, "bin");
		File javaProgram = new File(javaBin, "java");
		File javaProgramWindows = new File(javaBin, "java.exe");

		String mainCmd = null;
		if (javaProgram.exists()) {
			mainCmd = javaProgram.getAbsolutePath();
		}
		if (javaProgramWindows.exists()) {
			mainCmd = javaProgramWindows.getAbsolutePath();
		}
		if (mainCmd == null) {
			return false;
		}

		unbuiltCommand.add("java");
		unbuiltCommand.add("-Dcupackage.updatedir=" + updateFolder.getAbsolutePath());
		if (productDir != null) {
			unbuiltCommand.add("-Dcupackage.productdir=" + productDir);
		}
		if (noConsole != null) {
			unbuiltCommand.add("-Dcupackage.noconsole=" + noConsole);
		}
		if (noGraphics != null) {
			unbuiltCommand.add("-Dcupackage.nographics=" + noGraphics);
		}
		if (uninstVers != null) {
			unbuiltCommand.add("-Dcupackage.uninstallversion=" + uninstVers);
		}
		if (noVersCheck != null) {
			unbuiltCommand.add("-Dcupackage.noverscheck=" + noVersCheck);
		}
		if (custLogFile != null) {
			unbuiltCommand.add("-Dcupackage.customlog=" + custLogFile);
		}
		if (noLogging != null) {
			unbuiltCommand.add("-Dcupackage.nologging=" + noLogging);
		}
		if (eulaAccept != null) {
			unbuiltCommand.add("-Dcupackage.accepteula=" + eulaAccept);
		}
		unbuiltCommand.add("-jar");
		unbuiltCommand.add(recent.getAbsolutePath());
		try {
			ProcessBuilder builder = new ProcessBuilder(unbuiltCommand);
			builder.inheritIO();
			Process proc = builder.start();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {

			}
			return proc.exitValue() == 0;
		} catch (IOException e) {

		}

		return false;
	}

	public static boolean jumpToOther(File recent, File updateFolder, String productDir, String noConsole,
			String noGraphics, String uninstVers, String custLogFile, String noLogging, String eulaAccept) {
		return jumpToOther(recent, updateFolder, productDir, noConsole, noGraphics, uninstVers, "true", custLogFile,
				noLogging, eulaAccept);
	}
}
