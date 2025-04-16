/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

/**
 * 
 * Your updater custom module must contain a class that implements this
 * interface.
 * 
 * @author ExplodingBottle
 *
 */
public interface FunctionsPublisher {

	/**
	 * This function is called by the update package internal code to register your
	 * functions.
	 * 
	 * @param gatherer Use the function {@code registerFunction()} to register your
	 *                 functions.
	 * 
	 * @see FunctionsGatherer
	 */
	public void publishModuleFunctions(FunctionsGatherer gatherer);
}
