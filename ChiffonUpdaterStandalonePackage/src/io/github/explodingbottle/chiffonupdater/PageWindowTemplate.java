/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class PageWindowTemplate extends JFrame implements WindowListener, ActionListener, MouseListener {

	private static final long serialVersionUID = 5676255249106839015L;

	private JButton backButton;
	private JButton cancelButton;
	private JButton nextButton;

	private JPanel buttonsPanel;

	private JLabel aboutLabel;

	private SpringLayout layout;

	private List<WizardPage> pages;
	private int currentPage;
	private WizardPage currentDisplayedPage; // This shouldn't be modified

	private boolean onFinalPage;

	private Translator translator;

	public PageWindowTemplate() {
		translator = PackageMain.getTranslator();

		pages = new ArrayList<WizardPage>();
		currentPage = 0;

		ImageIcon imgWizardIconImage = PackageMain.getCustomizationEngine().getIconToDisplay();
		if (imgWizardIconImage != null) {
			setIconImage(imgWizardIconImage.getImage());
		}

		setSize(650, 500);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		layout = new SpringLayout();
		setLayout(layout);

		setResizable(false);

		aboutLabel = new JLabel("<html><u>" + translator.getTranslation("wizard.about") + "</u><html>");
		aboutLabel.setForeground(Color.BLUE);
		aboutLabel.addMouseListener(this);
		aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		backButton = new JButton(translator.getTranslation("wizard.back"));
		nextButton = new JButton(translator.getTranslation("wizard.next"));
		cancelButton = new JButton(translator.getTranslation("wizard.cancel"));

		addWindowListener(this);

		backButton.addActionListener(this);
		nextButton.addActionListener(this);
		cancelButton.addActionListener(this);

		buttonsPanel = new JPanel();
		buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		SpringLayout buttonsPanelLayout = new SpringLayout();
		buttonsPanel.setLayout(buttonsPanelLayout);

		buttonsPanel.setPreferredSize(new Dimension(650, 40));

		layout.putConstraint(SpringLayout.SOUTH, buttonsPanel, 0, SpringLayout.SOUTH, getContentPane());

		add(buttonsPanel);

		buttonsPanelLayout.putConstraint(SpringLayout.NORTH, cancelButton, 5, SpringLayout.NORTH, buttonsPanel);
		buttonsPanelLayout.putConstraint(SpringLayout.EAST, cancelButton, -20, SpringLayout.EAST, buttonsPanel);
		buttonsPanelLayout.putConstraint(SpringLayout.NORTH, nextButton, 5, SpringLayout.NORTH, buttonsPanel);
		buttonsPanelLayout.putConstraint(SpringLayout.EAST, nextButton, -20, SpringLayout.WEST, cancelButton);
		buttonsPanelLayout.putConstraint(SpringLayout.NORTH, backButton, 5, SpringLayout.NORTH, buttonsPanel);
		buttonsPanelLayout.putConstraint(SpringLayout.EAST, backButton, 0, SpringLayout.WEST, nextButton);
		buttonsPanelLayout.putConstraint(SpringLayout.NORTH, aboutLabel, 0, SpringLayout.NORTH, backButton);
		buttonsPanelLayout.putConstraint(SpringLayout.SOUTH, aboutLabel, 0, SpringLayout.SOUTH, backButton);
		buttonsPanelLayout.putConstraint(SpringLayout.WEST, aboutLabel, 5, SpringLayout.WEST, buttonsPanel);

		buttonsPanel.add(backButton);
		buttonsPanel.add(nextButton);
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(aboutLabel);

		setAlwaysOnTop(true);

		// updateCurrentPage();
	}

	public void relayoutCurrentDisplayPage() {

		layout.putConstraint(SpringLayout.WEST, currentDisplayedPage, 0, SpringLayout.WEST, getContentPane());
		layout.putConstraint(SpringLayout.NORTH, currentDisplayedPage, 0, SpringLayout.NORTH, getContentPane());
		layout.putConstraint(SpringLayout.EAST, currentDisplayedPage, 0, SpringLayout.EAST, getContentPane());
		layout.putConstraint(SpringLayout.SOUTH, currentDisplayedPage, 0, SpringLayout.NORTH, buttonsPanel);
	}

	public void updateCurrentPage() {
		if (currentDisplayedPage != null) {
			remove(currentDisplayedPage);
			layout.removeLayoutComponent(currentDisplayedPage);
		}
		currentDisplayedPage = pages.get(currentPage);
		relayoutCurrentDisplayPage();
		add(currentDisplayedPage);
		updatePageSelection();
		currentDisplayedPage.pageShownPriv(this);
		revalidate();
		repaint();

	}

	public void updatePageSelection() {
		UpdaterState state = PackageMain.returnUpdaterState();
		if (currentDisplayedPage != null) {
			backButton.setEnabled((currentPage > 0) && currentDisplayedPage.isAllowingPageMovements());
			nextButton.setEnabled((currentPage + 1 < pages.size()) && currentDisplayedPage.isAllowingPageMovements()
					&& currentDisplayedPage.isAllowingNext());
			cancelButton.setEnabled(!state.isUpdateCancelled() && currentDisplayedPage.isAllowingCancel());
		} else {
			backButton.setEnabled(currentPage > 0);
			nextButton.setEnabled(currentPage + 1 < pages.size());
			cancelButton.setEnabled(!state.isUpdateCancelled());
		}
	}

	public void cancel() {
		if (currentDisplayedPage != null) {
			if (!currentDisplayedPage.isAllowingCancel()) {
				return;
			}
		}
		UpdaterState state = PackageMain.returnUpdaterState();
		if (!state.isUpdateCancelled() && !onFinalPage) {
			if (state.canCancelFromCallback(this)) {
				if (!state.isUpdateCancelled() && !onFinalPage) {
					state.cancelUpdate();
					updatePageSelection();
					state.callCancelCallback(this);
				}
			}
		}
	}

	public void showEndWindow(WizardPage endPage) {
		if (onFinalPage)
			return;
		onFinalPage = true;
		if (currentDisplayedPage != null) {
			remove(currentDisplayedPage);
			layout.removeLayoutComponent(currentDisplayedPage);
		}
		currentDisplayedPage = endPage;
		relayoutCurrentDisplayPage();
		backButton.setEnabled(false);
		nextButton.setText(translator.getTranslation("wizard.finish"));
		nextButton.setEnabled(true);
		cancelButton.setEnabled(false);
		add(currentDisplayedPage);
		currentDisplayedPage.pageShownPriv(this);
		revalidate();
		repaint();
	}

	public void updateCancelledDisplay() {
		showEndWindow(new UpdateInterruptedPage());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == nextButton) {
			if (onFinalPage) {
				setVisible(false);
				dispose();
			} else {
				currentPage++;
				updateCurrentPage();
			}
		}
		if (e.getSource() == backButton) {
			currentPage--;
			updateCurrentPage();
		}
		if (e.getSource() == cancelButton) {
			cancel();
		}
	}

	public void addWizardPage(WizardPage page) {
		pages.add(page);
	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (onFinalPage) {
			setVisible(false);
			dispose();
		} else {
			cancel();
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == aboutLabel) {
			AboutDialog dialog = new AboutDialog(this, translator);
			dialog.setVisible(true);
		}
	}

}
