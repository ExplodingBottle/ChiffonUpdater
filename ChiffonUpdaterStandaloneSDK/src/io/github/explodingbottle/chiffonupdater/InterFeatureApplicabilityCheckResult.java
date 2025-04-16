/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

/**
 * 
 * This class represents a result returned by the computer, which contains a
 * status and a priority.
 * 
 * @author ExplodingBottle
 *
 */
public class InterFeatureApplicabilityCheckResult {

	public static final long DEFAULT_PRIORITY = 0;

	private InterFeatureApplicabilityCheckStatus status;
	private long priority;

	/**
	 * Creates a result
	 * 
	 * @param status   The status of the result
	 * @param priority The priority of the result
	 */
	public InterFeatureApplicabilityCheckResult(InterFeatureApplicabilityCheckStatus status, long priority) {
		this.status = status;
		this.priority = priority;
	}

	InterFeatureApplicabilityCheckStatus getStatus() {
		return status;
	}

	long getPriority() {
		return priority;
	}

}
