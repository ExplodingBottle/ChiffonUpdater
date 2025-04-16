/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

/**
 * 
 * This function represents a single function that can be called from a
 * configuration file for detections.
 * 
 * @author ExplodingBottle
 *
 */
public interface DetectionFunction {

	/**
	 * Override this function with your code.
	 * 
	 * @param productRoot The root folder where we found the product.
	 * @param arguments   Optional arguments that may be passed to the function
	 * @return Optional return value that might or might not be used.
	 */
	public String runDetection(File productRoot, String[] arguments);

	/**
	 * This function will be called whenever a function is called.<br>
	 * It is used to determine what to display in the details text of the wizard.
	 * 
	 * @param arguments The arguments we are about to call this function with
	 * @return The user friendly text to show while processing the command.
	 */
	public String getUserFriendlyCommandDetail(String[] arguments);
}
