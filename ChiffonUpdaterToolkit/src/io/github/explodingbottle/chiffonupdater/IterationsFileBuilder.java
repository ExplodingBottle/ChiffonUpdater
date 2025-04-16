/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.Properties;

import io.github.explodingbottle.chiffonupdater.project.ProductInfo;

public class IterationsFileBuilder {
	private ProductInfo product;

	public IterationsFileBuilder(ProductInfo product) {
		this.product = product;
	}

	public Properties buildIterationsProperties() {
		Properties props = new Properties();

		product.getVersionsReference().forEach((versInfo -> {
			props.setProperty(versInfo.getVersionName(), "" + versInfo.getIterationNumber());
		}));

		return props;
	}
}
