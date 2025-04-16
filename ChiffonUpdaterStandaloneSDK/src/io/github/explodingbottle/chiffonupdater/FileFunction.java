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
 * configuration file for tasks that implies a source file.
 * 
 * @author ExplodingBottle
 *
 */
public interface FileFunction {

	/**
	 * Override this function with your code that will be ran by the file function.
	 * 
	 * @param productRoot The root folder where we found the product.
	 * @param updateRoot  The root folder where are located the update files
	 * @param arguments   Optional arguments that may be passed to the function
	 * @return True if the operation succeed, else false.
	 */
	public boolean runOperation(File productRoot, File updateRoot, String[] arguments);

	/**
	 * Override this to run an operation that explains what to do when removing your
	 * update.
	 * 
	 * @param productRoot      The root folder where we found the product.
	 * @param backupFolder     The folder in which backup files will be placed.
	 * @param rollbackRegister The properties file containing a list of instructions
	 * @param arguments        The list of arguments that will be used on the
	 *                         original function.
	 * @return True if the backup operation succeed, else false.
	 */
	public boolean backupOperation(File productRoot, File backupFolder, RollbackRegister rollbackRegister,
			String[] arguments);

	/**
	 * Override this function with the code that will be used to check if the action
	 * will be done.
	 * 
	 * @param productRoot The root folder where we found the product.
	 * @param updateRoot  The root folder where are located the update files
	 * @param arguments   Optional arguments that may be passed to the function
	 * @return True if the operation succeed, else false.
	 */
	public boolean preActionCheck(File productRoot, File updateRoot, String[] arguments);

	/**
	 * This function will be called whenever a function is called.<br>
	 * It is used to determine what to display in the details text of the wizard.
	 * 
	 * @param arguments The arguments we are about to call this function with
	 * @return The user friendly text to show while processing the command.
	 */
	public String getUserFriendlyCommandDetail(String[] arguments);

}
