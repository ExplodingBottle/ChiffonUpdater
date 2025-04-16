/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.github.explodingbottle.chiffonupdater.project.ProductFilesInfo;
import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;

public class ProductEditor extends ComponentEditor
		implements DocumentListener, ActionListener, ListSelectionListener, ItemListener {

	private static final long serialVersionUID = 9180179048792484429L;

	private ProductInfo infos;

	private JLabel descriptionHelp;

	private JTextField productName;
	private JTextField featureAddField;
	private JList<String> features;
	private DefaultListModel<String> featuresListModel;
	private JButton addFeature;
	private JButton removeFeature;
	private JTextField uninstallerNameField;

	private JCheckBox doNotCreateUninstaller;

	private JLabel untoleratedName;

	public ProductEditor(ToolkitWindow handler, ProductInfo infos) {
		super(handler);
		SpringLayout spring = new SpringLayout();
		setLayout(spring);

		this.infos = infos;

		descriptionHelp = new JLabel("<html>" + "<h3>Product</h3><br>"
				+ "This editor allows you to register a product by giving the name and the list of features.<br>"
				+ "A feature corresponds to a set of files that are inside of the product folder.<br>"
				+ "<u>Please note that features edition is not supported by the automatic source folder modifications.</u><br>"
				+ "</html>");

		spring.putConstraint(SpringLayout.NORTH, descriptionHelp, 10, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.SOUTH, descriptionHelp, 10 + 120, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.EAST, descriptionHelp, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, descriptionHelp, 10, SpringLayout.WEST, this);

		JLabel pName = new JLabel("Product Name:");
		JLabel featureAdd = new JLabel("Features:");
		JLabel uName = new JLabel("Uninstaller Filename:");

		addFeature = new JButton("Add");
		addFeature.setToolTipText("Adds the specified feature.");
		removeFeature = new JButton("Remove");
		removeFeature.setToolTipText("Removes the specified feature.");

		addFeature.addActionListener(this);
		removeFeature.addActionListener(this);

		productName = new JTextField(infos.getProductName());
		productName.setToolTipText("The name of the product.");
		featureAddField = new JTextField("");
		featureAddField.setToolTipText("Insert here the name of the feature you want to add or delete.");

		doNotCreateUninstaller = new JCheckBox("Do not create an uninstaller");
		doNotCreateUninstaller.setToolTipText(
				"Prevent the package from copying itself as an uninstaller in the folder of the product after updating.");
		doNotCreateUninstaller.setSelected(infos.doNotCreateUninstaller());
		doNotCreateUninstaller.addItemListener(this);

		untoleratedName = new JLabel();
		untoleratedName.setForeground(Color.RED);
		untoleratedName.setVisible(false);

		uninstallerNameField = new JTextField(infos.getUninstallerName());
		uninstallerNameField
				.setToolTipText("The name of the uninstaller file that will be copied in the product root folder.");

		productName.getDocument().addDocumentListener(this);
		featureAddField.getDocument().addDocumentListener(this);
		uninstallerNameField.getDocument().addDocumentListener(this);

		featuresListModel = new DefaultListModel<String>();
		features = new JList<String>(featuresListModel);
		features.addListSelectionListener(this);
		features.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if (!"true".equals(
				ToolkitMain.getPersistentStorage().getSettings().getProperty("editor.settings.advanced.show"))) {
			uName.setVisible(false);
			uninstallerNameField.setVisible(false);
			doNotCreateUninstaller.setVisible(false);

		}

		JScrollPane featuresPane = new JScrollPane(features);

		spring.putConstraint(SpringLayout.NORTH, uName, 10, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.SOUTH, uName, 10 + 20, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.WEST, uName, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, uName, 10 + 125, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, uninstallerNameField, 0, SpringLayout.NORTH, uName);
		spring.putConstraint(SpringLayout.SOUTH, uninstallerNameField, 0, SpringLayout.SOUTH, uName);
		spring.putConstraint(SpringLayout.WEST, uninstallerNameField, 10, SpringLayout.EAST, uName);
		spring.putConstraint(SpringLayout.EAST, uninstallerNameField, 150, SpringLayout.WEST, uninstallerNameField);

		spring.putConstraint(SpringLayout.NORTH, doNotCreateUninstaller, 0, SpringLayout.NORTH, uName);
		spring.putConstraint(SpringLayout.SOUTH, doNotCreateUninstaller, 0, SpringLayout.SOUTH, uName);
		spring.putConstraint(SpringLayout.WEST, doNotCreateUninstaller, 10, SpringLayout.EAST, uninstallerNameField);
		spring.putConstraint(SpringLayout.EAST, doNotCreateUninstaller, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, pName, 10, SpringLayout.SOUTH, uName);
		spring.putConstraint(SpringLayout.SOUTH, pName, 10 + 20, SpringLayout.SOUTH, uName);
		spring.putConstraint(SpringLayout.WEST, pName, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, pName, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, untoleratedName, 10, SpringLayout.SOUTH, pName);
		spring.putConstraint(SpringLayout.SOUTH, untoleratedName, 10 + 20, SpringLayout.SOUTH, pName);
		spring.putConstraint(SpringLayout.WEST, untoleratedName, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, untoleratedName, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, featureAdd, 10, SpringLayout.SOUTH, untoleratedName);
		spring.putConstraint(SpringLayout.SOUTH, featureAdd, 10 + 20, SpringLayout.SOUTH, untoleratedName);
		spring.putConstraint(SpringLayout.WEST, featureAdd, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, featureAdd, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, productName, 0, SpringLayout.NORTH, pName);
		spring.putConstraint(SpringLayout.SOUTH, productName, 0, SpringLayout.SOUTH, pName);
		spring.putConstraint(SpringLayout.WEST, productName, 10, SpringLayout.EAST, pName);
		spring.putConstraint(SpringLayout.EAST, productName, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, featureAddField, 0, SpringLayout.NORTH, featureAdd);
		spring.putConstraint(SpringLayout.SOUTH, featureAddField, 0, SpringLayout.SOUTH, featureAdd);
		spring.putConstraint(SpringLayout.WEST, featureAddField, 10, SpringLayout.EAST, featureAdd);
		spring.putConstraint(SpringLayout.EAST, featureAddField, 10 + 200, SpringLayout.EAST, featureAdd);

		spring.putConstraint(SpringLayout.NORTH, addFeature, 0, SpringLayout.NORTH, featureAddField);
		spring.putConstraint(SpringLayout.SOUTH, addFeature, 0, SpringLayout.SOUTH, featureAddField);
		spring.putConstraint(SpringLayout.WEST, addFeature, 10, SpringLayout.EAST, featureAddField);
		spring.putConstraint(SpringLayout.EAST, addFeature, 10 + 100, SpringLayout.EAST, featureAddField);

		spring.putConstraint(SpringLayout.NORTH, removeFeature, 0, SpringLayout.NORTH, addFeature);
		spring.putConstraint(SpringLayout.SOUTH, removeFeature, 0, SpringLayout.SOUTH, addFeature);
		spring.putConstraint(SpringLayout.WEST, removeFeature, 10, SpringLayout.EAST, addFeature);
		spring.putConstraint(SpringLayout.EAST, removeFeature, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, featuresPane, 10, SpringLayout.SOUTH, featureAdd);
		spring.putConstraint(SpringLayout.SOUTH, featuresPane, -20, SpringLayout.SOUTH, this);
		spring.putConstraint(SpringLayout.WEST, featuresPane, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, featuresPane, -10, SpringLayout.EAST, this);

		add(descriptionHelp);
		add(pName);
		add(productName);
		add(featureAdd);
		add(featureAddField);
		add(addFeature);
		add(removeFeature);
		add(featuresPane);
		add(uName);
		add(uninstallerNameField);
		add(untoleratedName);
		add(doNotCreateUninstaller);

		setPreferredSize(new Dimension(550, 350));

		updateFeatureAddButtons();
		updateList();

		updateUninstallTextAndBox();

	}

	public void updateFeatureAddButtons() {
		if (featureAddField.getText().trim().isEmpty()) {
			addFeature.setEnabled(false);
			removeFeature.setEnabled(false);
			return;
		}
		if (infos.getFeaturesReference().contains(featureAddField.getText())) {
			addFeature.setEnabled(false);
			removeFeature.setEnabled(true);
		} else {
			addFeature.setEnabled(true);
			removeFeature.setEnabled(false);
		}
	}

	public void docUpdAll(DocumentEvent e) {
		if (e.getDocument() == productName.getDocument()) {
			List<String> prodNames = new ArrayList<String>();
			for (ProductInfo prod : infos.getParent().getProductsList()) {
				if (prod != infos) {
					prodNames.add(prod.getProductName().toLowerCase());
				}
			}
			boolean visible = false;
			if (prodNames.contains(productName.getText().toLowerCase())) {
				String name = null;
				for (int i = 1; name == null; i++) {
					String tName = productName.getText() + "_" + i;
					if (!prodNames.contains(tName.toLowerCase())) {
						name = tName;
						untoleratedName.setText("Conflict with an existing product. Name will be " + tName);
						visible = true;
					}
				}
				infos.setProductName(name);
			} else {
				infos.setProductName(productName.getText());
			}

			untoleratedName.setVisible(visible);
			markAsModified();
		}
		if (e.getDocument() == uninstallerNameField.getDocument()) {
			infos.setUninstallerName(uninstallerNameField.getText());
			updateUninstallTextAndBox();
			markAsModified();
		}
		if (e.getDocument() == featureAddField.getDocument()) {
			updateFeatureAddButtons();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		docUpdAll(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		docUpdAll(e);

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		docUpdAll(e);

	}

	public void updateList() {
		featuresListModel.removeAllElements();
		infos.getFeaturesReference().forEach(feature -> {
			featuresListModel.addElement(feature);
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == removeFeature) {
			boolean mustAsk = false;
			for (VersionInfo vers : infos.getVersionsReference()) {
				ProductFilesInfo infos = vers.filesForFeatureMap().get(featureAddField.getText());
				if (infos.getFileHashesListReference().size() > 0) {
					mustAsk = true;
					break;

				}
			}
			if (mustAsk) {
				if (JOptionPane.showConfirmDialog(returnHandler(),
						"There are data contained into this feature. Are you sure you want to remove it ?",
						"Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != 0) {
					return;
				}
			}
			infos.getFeaturesReference().remove(featureAddField.getText());
			for (VersionInfo vers : infos.getVersionsReference()) {
				vers.filesForFeatureMap().remove(featureAddField.getText());
			}
			updateList();
			markAsModified();
		}
		if (e.getSource() == addFeature) {
			infos.getFeaturesReference().add(featureAddField.getText());
			for (VersionInfo vers : infos.getVersionsReference()) {
				vers.filesForFeatureMap().put(featureAddField.getText(), new ProductFilesInfo(vers));
			}
			updateList();
			markAsModified();
		}
		updateFeatureAddButtons();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		featureAddField.setText(features.getSelectedValue());
	}

	public void updateUninstallTextAndBox() {
		if (doNotCreateUninstaller.isEnabled() && !uninstallerNameField.getText().trim().isEmpty())
			uninstallerNameField.setEnabled(!doNotCreateUninstaller.isSelected());

		if (uninstallerNameField.getText().trim().isEmpty()) {
			doNotCreateUninstaller.setEnabled(false);
			doNotCreateUninstaller.setSelected(true);
		} else if (uninstallerNameField.isEnabled()) {
			doNotCreateUninstaller.setEnabled(true);
			doNotCreateUninstaller.setSelected(false);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == doNotCreateUninstaller) {
			updateUninstallTextAndBox();
			infos.setDoNotCreateUninstaller(doNotCreateUninstaller.isSelected());
			markAsModified();
		}
	}

}
