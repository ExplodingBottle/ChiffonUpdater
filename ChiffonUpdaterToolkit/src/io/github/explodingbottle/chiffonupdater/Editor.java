/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class Editor extends JPanel {

	private static final long serialVersionUID = -796063479295100047L;

	private SpringLayout layout;
	private ComponentEditor editor;

	public Editor() {
		layout = new SpringLayout();
		setLayout(layout);
	}

	public void clearEditor() {
		if (editor != null) {
			JScrollPane editorPane = editor.returnPane();
			layout.removeLayoutComponent(editorPane);
			remove(editorPane);
			editor = null;
			revalidate();
			repaint();
		}
	}

	public void setEditor(ComponentEditor editor) {
		clearEditor();
		this.editor = editor;
		JScrollPane editorPane = editor.returnPane();
		layout.putConstraint(SpringLayout.NORTH, editorPane, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, editorPane, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, editorPane, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, editorPane, 0, SpringLayout.SOUTH, this);
		add(editorPane);
		revalidate();
		repaint();
	}

}
