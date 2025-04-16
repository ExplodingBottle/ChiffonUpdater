/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UninstallVersionSelectorPage extends WizardPage implements ListSelectionListener {

	private static final long serialVersionUID = -3986972473112487298L;

	private JList<String> versionsSelector;
	private Map<String, VersionRollbackTargetInformation> versionsData;
	private JLabel versInfos;
	private JTextArea versDescArea;
	private JScrollPane versDescScroller;

	public UninstallVersionSelectorPage(List<VersionRollbackTargetInformation> versions) {
		versionsData = new HashMap<String, VersionRollbackTargetInformation>();

		JLabel upperText = new JLabel(PackageMain.getTranslator().getTranslation("uninstall.versselect.message"),
				SwingConstants.CENTER);

		versInfos = new JLabel();

		versionsSelector = new JList<String>();
		versionsSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		versDescArea = new JTextArea();
		versDescArea.setEditable(false);
		versDescScroller = new JScrollPane(versDescArea);

		DefaultListModel<String> listModel = new DefaultListModel<String>();
		versions.forEach(vers -> {
			String tadd;
			if (vers.getState() == VersionRollbackState.CAN_ROLLBACK) {
				tadd = vers.getTargetVersion();
			} else {
				tadd = PackageMain.getTranslator().getTranslation("uninstall.rollback.impossible",
						vers.getTargetVersion());
			}

			listModel.addElement(tadd);
			versionsData.put(vers.getTargetVersion(), vers);
		});

		versionsSelector.setModel(listModel);

		SpringLayout layout = getSpringLayout();

		JScrollPane scrolling = new JScrollPane(versionsSelector);

		layout.putConstraint(SpringLayout.NORTH, upperText, 20, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, upperText, 20 + 50, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, upperText, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, upperText, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, scrolling, 10, SpringLayout.SOUTH, upperText);
		layout.putConstraint(SpringLayout.SOUTH, scrolling, -100, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, scrolling, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, scrolling, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, versInfos, 10, SpringLayout.SOUTH, scrolling);
		layout.putConstraint(SpringLayout.SOUTH, versInfos, 10 + 20, SpringLayout.SOUTH, scrolling);
		layout.putConstraint(SpringLayout.EAST, versInfos, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, versInfos, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, versDescScroller, 10, SpringLayout.SOUTH, versInfos);
		layout.putConstraint(SpringLayout.SOUTH, versDescScroller, -10, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, versDescScroller, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, versDescScroller, 20, SpringLayout.WEST, this);

		add(upperText);
		add(scrolling);
		add(versInfos);
		add(versDescScroller);
	}

	public void pageShown() {
		versionsSelector.addListSelectionListener(this);
		versionsSelector.setSelectedIndex(0);
	}

	public String getSelectedVersion() {
		return versionsSelector.getSelectedValue();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (versionsSelector.getSelectedValue() == null) {
			setAllowNext(false);
			return;
		}

		VersionRollbackTargetInformation descript = versionsData.get(versionsSelector.getSelectedValue());
		if (descript == null) {
			setAllowNext(false);
			return;
		}

		DateFormat df = DateFormat.getDateInstance();

		boolean allInformationsNull = true;

		String versDescription = descript.getDescription();
		if (versDescription != null && !versDescription.isEmpty()) {
			allInformationsNull = false;
		}
		String formattedRelDate = PackageMain.getTranslator().getTranslation("uninstall.rollback.unkdate");
		String formattedInsDate = formattedRelDate;
		if (descript.getReleaseDate() != null) {
			formattedRelDate = df.format(new Date(descript.getReleaseDate()));
			allInformationsNull = false;
		}
		if (descript.getReleaseDate() != null) {
			formattedInsDate = df.format(new Date(descript.getInstallDate()));
			allInformationsNull = false;
		}
		if (allInformationsNull) {
			versInfos.setText(PackageMain.getTranslator().getTranslation("uninstall.rollback.noinfos"));
			versDescScroller.setVisible(false);
		} else {
			if (versDescription != null && !versDescription.isEmpty()) {
				versInfos.setText(PackageMain.getTranslator().getTranslation("uninstall.rollback.description",
						formattedRelDate, formattedInsDate));
				versDescArea.setText(versDescription);
				versDescScroller.setVisible(true);
			} else {
				versInfos.setText(PackageMain.getTranslator().getTranslation("uninstall.rollback.datesonly",
						formattedRelDate, formattedInsDate));
				versDescScroller.setVisible(false);
			}

		}
		if (descript.getState() != VersionRollbackState.CAN_ROLLBACK) {
			setAllowNext(false);
			return;
		}

		setAllowNext(true);

	}

}
