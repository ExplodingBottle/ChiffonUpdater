/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * The goal of this class is to register every functions made available by all
 * the modules. <br>
 * A single instance of this {@code FunctionsGatherer} is created and used. <br>
 * It is then given to every modules in order to let them register themselves.
 * 
 * @author ExplodingBottle
 *
 */
final public class FunctionsGatherer {

	private Map<String, DetectionFunction> detectionFunctions;
	private Map<String, FileFunction> fileFunctions;
	private List<InterFeatureApplicabilityComputer> ifaComputers;

	FunctionsGatherer() {
		detectionFunctions = new HashMap<String, DetectionFunction>();
		fileFunctions = new HashMap<String, FileFunction>();
		ifaComputers = new ArrayList<InterFeatureApplicabilityComputer>();
	}

	/**
	 * This function is called by every module to register their detection
	 * functions.
	 * 
	 * @param functionName   The name that will be used in the configuration file in
	 *                       the detection section.
	 * @param customFunction A class wrapping the function that will be called.
	 */
	public void registerDetectionFunction(String functionName, DetectionFunction customFunction) {
		detectionFunctions.put(functionName, customFunction);
	}

	/**
	 * This function is called by every module to register their detection
	 * functions.
	 * 
	 * @param functionName   The name that will be used in the configuration file in
	 *                       the detection section.
	 * @param customFunction A class wrapping the function that will be called.
	 */
	public void registerFileFunction(String functionName, FileFunction customFunction) {
		fileFunctions.put(functionName, customFunction);
	}

	/**
	 * This function is called by every module to register their inter-feature
	 * applicability computer. This function can be called multiple times.
	 * 
	 * @param computer The new computer you want to register.
	 */
	public void registerInterFeatureApplicabilityComputer(InterFeatureApplicabilityComputer computer) {
		ifaComputers.add(computer);
	}

	// These are not supposed to be seen
	// START
	String runDetectionFunction(String functionName, File productRoot, String[] arguments) {
		DetectionFunction function = detectionFunctions.get(functionName);
		if (function != null) {
			return function.runDetection(productRoot, arguments);
		}
		return null;
	}

	InterFeatureApplicabilityCheckStatus computeInterFeatureApplicability(String command, List<String> otherCommands) {
		if (ifaComputers.isEmpty()) {
			return InterFeatureApplicabilityCheckStatus.OK;
		}
		List<InterFeatureApplicabilityCheckResult> results = new ArrayList<InterFeatureApplicabilityCheckResult>();
		ifaComputers.forEach(computer -> {
			results.add(computer.computerInterFeatureApplicability(command, otherCommands));
		});
		results.sort(new Comparator<InterFeatureApplicabilityCheckResult>() {

			@Override
			public int compare(InterFeatureApplicabilityCheckResult arg0, InterFeatureApplicabilityCheckResult arg1) {
				long t0 = arg0.getPriority();
				long t1 = arg1.getPriority();
				if (t0 < t1) {
					return -1;
				}
				if (t0 > t1) {
					return 1;
				}
				return 0;
			}
		});
		return results.get(results.size() - 1).getStatus();
	}

	boolean runFileFunction(String functionName, File productRoot, File updateRoot, String[] arguments) {
		FileFunction function = fileFunctions.get(functionName);
		if (function != null) {
			return function.runOperation(productRoot, updateRoot, arguments);
		}
		return false;
	}

	boolean runFileFunctionBackup(String functionName, File productRoot, File backupFolder,
			RollbackRegister rollbackRegister, String[] arguments) {
		FileFunction function = fileFunctions.get(functionName);
		if (function != null) {
			return function.backupOperation(productRoot, backupFolder, rollbackRegister, arguments);
		}
		return false;
	}

	String getFileFunctionUserFriendlyDetails(String functionName, String[] arguments) {
		FileFunction function = fileFunctions.get(functionName);
		if (function != null) {
			return function.getUserFriendlyCommandDetail(arguments);
		}
		return null;
	}

	String getDetectionFunctionUserFriendlyDetails(String functionName, String[] arguments) {
		DetectionFunction function = detectionFunctions.get(functionName);
		if (function != null) {
			return function.getUserFriendlyCommandDetail(arguments);
		}
		return null;
	}

	boolean runFileFunctionPreCheck(String functionName, File productRoot, File updateRoot, String[] arguments) {
		FileFunction function = fileFunctions.get(functionName);
		if (function != null) {
			return function.preActionCheck(productRoot, updateRoot, arguments);
		}
		return false;
	}

	boolean isDetectionFunctionPresent(String functionName) {
		return detectionFunctions.get(functionName) != null;
	}

	boolean isFileFunctionPresent(String functionName) {
		return fileFunctions.get(functionName) != null;
	}
	// STOP

}
