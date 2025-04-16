/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ComponentEditor extends JPanel {

	private static final long serialVersionUID = -4115873384678741782L;

	private ToolkitWindow handler;
	private JScrollPane pane;

	public ComponentEditor(ToolkitWindow handler) {
		pane = new JScrollPane(this);
		this.handler = handler;
	}

	public JScrollPane returnPane() {
		return pane;
	}

	public ToolkitWindow returnHandler() {
		return handler;
	}

	public void markAsModified() {
		if (handler != null) {
			handler.onDataChanged();
		}
	}

}
