/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.nio.file.Paths;
import java.util.List;

public class PackageIfaComputer implements InterFeatureApplicabilityComputer {

	private static final int CMD_CPY = 0;
	private static final int CMD_DEL = 1;
	private static final int CMD_FCR = 2;

	private InterFeatureApplicabilityCheckStatus processFileCopy(String currCmd[], String otherCmd[]) {
		if (otherCmd[0].equals(StandalonePackageCommandNames.FILE_COPY_COMMAND) && otherCmd.length == 3) {

			if (Paths.get(otherCmd[2]).equals(Paths.get(currCmd[2]))) {
				if (!Paths.get(otherCmd[1]).equals(Paths.get(currCmd[1]))) {
					return InterFeatureApplicabilityCheckStatus.FAIL;
				} else {
					return InterFeatureApplicabilityCheckStatus.SKIP;
				}
			}
		}
		if (otherCmd[0].equals(StandalonePackageCommandNames.FOLDER_CREATE_COMMAND) && otherCmd.length == 2) {
			if (FileStartsWith.filePathStartsWith(currCmd[2], otherCmd[1])) {
				return InterFeatureApplicabilityCheckStatus.SKIP_CHECK;
			}
		}
		return InterFeatureApplicabilityCheckStatus.OK;
	}

	private InterFeatureApplicabilityCheckStatus processFileDelete(String currCmd[], String otherCmd[]) {
		if ((otherCmd[0].equals(StandalonePackageCommandNames.FILE_DELETE_COMMAND)) && otherCmd.length == 2) {
			if (Paths.get(otherCmd[1]).equals(Paths.get(currCmd[1]))) {
				return InterFeatureApplicabilityCheckStatus.SKIP;
			}
		}
		if (otherCmd[0].equals(StandalonePackageCommandNames.FILE_COPY_COMMAND) && otherCmd.length == 3) {
			if (FileStartsWith.filePathStartsWith(otherCmd[2], currCmd[1])) {
				return InterFeatureApplicabilityCheckStatus.SKIP;
			}
		}
		if (otherCmd[0].equals(StandalonePackageCommandNames.FOLDER_CREATE_COMMAND) && otherCmd.length == 2) {
			if (FileStartsWith.filePathStartsWith(otherCmd[1], currCmd[1])) {
				return InterFeatureApplicabilityCheckStatus.SKIP;
			}
			if (FileStartsWith.filePathStartsWith(currCmd[1], otherCmd[1])) {
				return InterFeatureApplicabilityCheckStatus.SKIP_CHECK;
			}
		}
		return InterFeatureApplicabilityCheckStatus.OK;
	}

	private InterFeatureApplicabilityCheckStatus processFolderCreate(String currCmd[], String otherCmd[]) {
		if (otherCmd[0].equals(StandalonePackageCommandNames.FOLDER_CREATE_COMMAND) && otherCmd.length == 2) {
			if (Paths.get(otherCmd[1]).equals(Paths.get(currCmd[1]))) {
				return InterFeatureApplicabilityCheckStatus.SKIP;
			}
			if (FileStartsWith.filePathStartsWith(currCmd[1], otherCmd[1])) {
				return InterFeatureApplicabilityCheckStatus.SKIP_CHECK;
			}
		}
		return InterFeatureApplicabilityCheckStatus.OK;
	}

	private InterFeatureApplicabilityCheckStatus returnNewStatus(InterFeatureApplicabilityCheckStatus current,
			InterFeatureApplicabilityCheckStatus newStatus) {
		switch (current) {
		case OK:
			return newStatus;
		case SKIP_CHECK:
			if (newStatus == InterFeatureApplicabilityCheckStatus.SKIP
					|| newStatus == InterFeatureApplicabilityCheckStatus.FAIL) {
				return newStatus;
			}
			return current;
		case SKIP:
			if (newStatus == InterFeatureApplicabilityCheckStatus.FAIL) {
				return newStatus;
			}
			return current;
		case FAIL:
			return current;
		}
		return current;
	}

	private InterFeatureApplicabilityCheckStatus processOtherCommands(List<String> otherCommands, String currCmd[],
			int detectedCmd) {
		InterFeatureApplicabilityCheckStatus result = InterFeatureApplicabilityCheckStatus.OK;
		for (String command : otherCommands) {
			String checkCommandSplit[] = command.split(";");
			if (detectedCmd == CMD_CPY) {
				result = returnNewStatus(result, processFileCopy(currCmd, checkCommandSplit));
			}
			if (detectedCmd == CMD_DEL) {
				result = returnNewStatus(result, processFileDelete(currCmd, checkCommandSplit));
			}
			if (detectedCmd == CMD_FCR) {
				result = returnNewStatus(result, processFolderCreate(currCmd, checkCommandSplit));
			}
		}
		return result;
	}

	@Override
	public InterFeatureApplicabilityCheckResult computerInterFeatureApplicability(String command,
			List<String> otherCommands) {

		InterFeatureApplicabilityCheckStatus resStatus = InterFeatureApplicabilityCheckStatus.OK;
		String checkCommandSplit[] = command.split(";");
		if (checkCommandSplit.length > 0) {
			if (checkCommandSplit[0].equals(StandalonePackageCommandNames.FILE_COPY_COMMAND)
					&& checkCommandSplit.length == 3) {
				resStatus = processOtherCommands(otherCommands, checkCommandSplit, CMD_CPY);
			}
			if (checkCommandSplit[0].equals(StandalonePackageCommandNames.FILE_DELETE_COMMAND)
					&& checkCommandSplit.length == 2) {
				resStatus = processOtherCommands(otherCommands, checkCommandSplit, CMD_DEL);
			}
			if (checkCommandSplit[0].equals(StandalonePackageCommandNames.FOLDER_CREATE_COMMAND)
					&& checkCommandSplit.length == 2) {
				resStatus = processOtherCommands(otherCommands, checkCommandSplit, CMD_FCR);

			}
		}

		return new InterFeatureApplicabilityCheckResult(resStatus,
				InterFeatureApplicabilityCheckResult.DEFAULT_PRIORITY);
	}

}
