/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

/**
 * The result of the inter-feature applicability for a command.
 * 
 * @author ExplodingBottle
 *
 */
public enum InterFeatureApplicabilityCheckStatus {
	/**
	 * The command will be run.
	 */
	OK,
	/**
	 * The prerequisite check will be skipped, generally because it depends on
	 * another command.
	 */
	SKIP_CHECK,
	/**
	 * The command must be skipped.
	 **/
	SKIP,
	/**
	 * The command cannot run, abort the update.
	 */
	FAIL;

}
