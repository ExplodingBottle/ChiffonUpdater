/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;

public class ConsoleViewer extends JPanel implements ActionListener {

	private JTextArea console;
	private JButton clear;

	private static final long serialVersionUID = -7266270507672812504L;

	public void appendToConsole(String text) {
		console.append(text);
	}

	public ConsoleViewer() {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		console = new JTextArea();

		console.setEditable(false);

		JScrollPane scroller = new JScrollPane(console);

		JToolBar consoleToolbar = new JToolBar();
		consoleToolbar.setFloatable(false);
		clear = new JButton("Clear console");
		clear.setMnemonic(KeyEvent.VK_L);

		clear.addActionListener(this);
		consoleToolbar.add(clear);

		layout.putConstraint(SpringLayout.EAST, consoleToolbar, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, consoleToolbar, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, consoleToolbar, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, consoleToolbar, 30, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.EAST, scroller, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, scroller, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, scroller, 0, SpringLayout.SOUTH, consoleToolbar);
		layout.putConstraint(SpringLayout.SOUTH, scroller, 0, SpringLayout.SOUTH, this);

		add(consoleToolbar);
		add(scroller);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == clear) {
			console.setText("");
		}

	}

}
