/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;

public interface DetectionDoneCallback {

	public void calllbackOnDetectionDone(List<DetectionResult> results);

}
