/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

public class DescriptionPage extends WizardHeaderPage {

	private static final long serialVersionUID = 7371037285934305362L;

	public DescriptionPage(String desc) {
		Translator translator = PackageMain.getTranslator();

		updateUpperText(translator.getTranslation("updatedesc.title"));
		SpringLayout layout = getSpringLayout();

		JTextArea area = new JTextArea(desc);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBackground(getBackground());

		JLabel updateDescriptionText = new JLabel(translator.getTranslation("updatedesc.text"));

		JScrollPane scroller = new JScrollPane(area);
		area.setEditable(false);

		layout.putConstraint(SpringLayout.NORTH, updateDescriptionText, 10, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.SOUTH, updateDescriptionText, 10 + 30, SpringLayout.SOUTH,
				returnHeaderPanel());
		layout.putConstraint(SpringLayout.EAST, updateDescriptionText, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, updateDescriptionText, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, scroller, 10, SpringLayout.SOUTH, updateDescriptionText);
		layout.putConstraint(SpringLayout.SOUTH, scroller, -10, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, scroller, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, scroller, 20, SpringLayout.WEST, this);

		add(updateDescriptionText);
		add(scroller);
	}

}
