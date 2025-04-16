/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Component;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -7079332432546574708L;
	private ImageIcon projIcon;
	private ImageIcon prodIcon;
	private ImageIcon versIcon;
	private ImageIcon featureIcon;
	private ImageIcon websiteIcon;
	private ImageIcon customizationIcon;

	private ImageIcon rescaleImageIcon(ImageIcon ic) {
		return new ImageIcon(ic.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
	}

	public ProjectTreeCellRenderer() {

		URL projectIconURL = this.getClass().getClassLoader().getResource("images/project_icon.png");
		if (projectIconURL != null) {
			projIcon = rescaleImageIcon(new ImageIcon(projectIconURL));
		}

		URL productIconURL = this.getClass().getClassLoader().getResource("images/product_icon.png");
		if (productIconURL != null) {
			prodIcon = rescaleImageIcon(new ImageIcon(productIconURL));
		}

		URL versIconURL = this.getClass().getClassLoader().getResource("images/version_icon.png");
		if (versIconURL != null) {
			versIcon = rescaleImageIcon(new ImageIcon(versIconURL));
		}

		URL featureIconURL = this.getClass().getClassLoader().getResource("images/feature_icon.png");
		if (featureIconURL != null) {
			featureIcon = rescaleImageIcon(new ImageIcon(featureIconURL));
		}

		URL websiteIconURL = this.getClass().getClassLoader().getResource("images/website_icon.png");
		if (websiteIconURL != null) {
			websiteIcon = rescaleImageIcon(new ImageIcon(websiteIconURL));
		}

		URL customizationIconURL = this.getClass().getClassLoader().getResource("images/customization_icon.png");
		if (customizationIconURL != null) {
			customizationIcon = rescaleImageIcon(new ImageIcon(customizationIconURL));
		}

	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		Component defComp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (!(value instanceof DefaultMutableTreeNode)) {
			return defComp;
		}
		Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
		if (!(userObj instanceof ProjectTreeDisplayObject)) {
			return defComp;
		}

		ProjectTreeDisplayObject obj = (ProjectTreeDisplayObject) userObj;
		switch (obj.getDisplayType()) {
		case FEATURE:
			setIcon(featureIcon);
			break;
		case PRODUCT:
			setIcon(prodIcon);
			break;
		case PROJECT:
			setIcon(projIcon);
			break;
		case VERSION:
			setIcon(versIcon);
			break;
		case WEBSITE:
			setIcon(websiteIcon);
			break;
		case CUSTOMIZATION:
			setIcon(customizationIcon);
			break;
		default:
			setIcon(null);
		}
		setText(obj.getText());
		return defComp;
	}

}
