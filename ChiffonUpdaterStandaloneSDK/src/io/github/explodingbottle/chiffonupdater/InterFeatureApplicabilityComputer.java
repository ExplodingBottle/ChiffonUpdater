/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;

/**
 * This interface represents a computer that is called whenever there is a need
 * to know if a command is applicable or not depending on the other commands.
 * This is called inter-feature applicability.
 * 
 * @author ExplodingBottle
 *
 */
public interface InterFeatureApplicabilityComputer {

	/**
	 * This function is called to computer the inter-feature applicability of a
	 * command.
	 * 
	 * @param command       The command you have to check the applicability
	 * @param otherCommands The other commands that will help you compute
	 * @return The result of the applicability test
	 */
	public InterFeatureApplicabilityCheckResult computerInterFeatureApplicability(String command,
			List<String> otherCommands);

}
