/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.github.explodingbottle.chiffonupdater.project.MainProjectInfo;
import io.github.explodingbottle.chiffonupdater.project.ProductInfo;

public class ChiffonUpdaterProject implements Serializable {

	private static final long serialVersionUID = -5817084726639549988L;

	private MainProjectInfo mainInfos;
	private List<ProductInfo> products;

	public ChiffonUpdaterProject(MainProjectInfo mainInfos) {
		this.mainInfos = mainInfos;
		products = new ArrayList<ProductInfo>();
	}

	public MainProjectInfo getMainInfosRef() {
		return mainInfos;
	}

	public List<ProductInfo> getProductsList() {
		return products;
	}

}
