/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

public class ProgressFrame extends JFrame implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1222493763532336179L;

	private JPanel contentPanel;
	private Translator translator;
	private JButton bottomButton;
	private JButton acceptButton;
	private JButton declineButton;

	private JLabel agentIcon;
	private JLabel status;
	private JLabel topText;
	private JLabel currentTask;
	private JTextArea actionsLog;
	private JScrollPane actionsPane;
	private JProgressBar progressBar;
	private Runnable bottomButtonClick;
	private GraphicalStatusPhase currentStatus;

	private SpringLayout contentLayout;

	private JLabel printLicense;
	private boolean supportPrinting;

	private Map<String, ProductInformations> agreements;

	private List<ProductInformations> toUpdate;

	private ProductInformations prodInfosCurrent;

	private int agreementCounter;
	private int agreementMax;

	private Set<String> requiredDownloads;
	private Map<ProductInformations, ActionResult> actionResults;

	public void updateCurrentTaskText(String text) {
		currentTask.setText(text);
	}

	public void updateProgressBarValue(int val) {
		progressBar.setValue(val);
	}

	public void appendActionMessage(String message) {
		actionsLog.append(message);
		actionsLog.setCaretPosition(actionsLog.getText().length());
	}

	public void setTopText(String text) {
		topText.setText("<html><b>" + text + "</html></b>");
	}

	public void setBottomButtonText(String text) {
		bottomButton.setText(text);
	}

	public void setBottomButtonEnabled(boolean enabled) {
		bottomButton.setEnabled(enabled);
	}

	public void setBottomButtonClickRunnable(Runnable rb) {
		bottomButtonClick = rb;
	}

	public void setStatus(GraphicalStatusPhase status) {
		currentStatus = status;
	}

	public void clearActionLog() {
		actionsLog.setText("");
	}

	public void refreshDisplay() {
		switch (currentStatus) {
		case EULA:
			HybridInformations infs = prodInfosCurrent.searchUpdateInformation();
			if (infs == null) {
				return;
			}
			contentLayout.putConstraint(SpringLayout.SOUTH, actionsPane, -20, SpringLayout.SOUTH, contentPanel);
			topText.setText(
					translator.getTranslation("cua.eula.title", "" + (agreementCounter + 1), "" + agreementMax));
			status.setText(translator.getTranslation("cua.eula.details", "" + infs.getVersionName(),
					prodInfosCurrent.getProductName()));
			status.setVisible(true);
			progressBar.setVisible(false);
			currentTask.setVisible(false);
			actionsPane.setVisible(true);
			acceptButton.setVisible(true);
			declineButton.setVisible(true);
			printLicense.setVisible(true);
			break;
		case ACTION:
			contentLayout.putConstraint(SpringLayout.SOUTH, actionsPane, 130, SpringLayout.NORTH, status);
			status.setText(translator.getTranslation("cua.window.status"));
			status.setVisible(true);
			progressBar.setVisible(true);
			currentTask.setVisible(true);
			actionsPane.setVisible(true);
			acceptButton.setVisible(false);
			declineButton.setVisible(false);
			printLicense.setVisible(false);
			break;
		case END:
			status.setVisible(false);
			progressBar.setVisible(false);
			currentTask.setVisible(false);
			actionsPane.setVisible(false);
			acceptButton.setVisible(false);
			declineButton.setVisible(false);
			printLicense.setVisible(false);
			break;
		default:
			break;
		}
	}

	public ProgressFrame(String windowTitle, Map<String, ProductInformations> agreements, Set<String> requiredDownloads,
			Map<ProductInformations, ActionResult> actionResults, List<ProductInformations> toUpdate) {
		translator = AgentMain.getTranslator();

		this.requiredDownloads = requiredDownloads;
		this.actionResults = actionResults;
		this.agreements = agreements;
		this.toUpdate = toUpdate;

		supportPrinting = Desktop.getDesktop().isSupported(Action.PRINT);

		setTitle(windowTitle);

		actionsLog = new JTextArea();

		if (agreements != null && !agreements.isEmpty()) {
			currentStatus = GraphicalStatusPhase.EULA;
			agreementMax = agreements.size();
			for (String eula : agreements.keySet()) {
				actionsLog.setText(eula);
				actionsLog.setCaretPosition(0);
				prodInfosCurrent = agreements.get(eula);
				agreements.remove(eula);
				break;
			}
			bottomButtonClick = () -> {
				declineEulaCallback();
			};

		} else {
			currentStatus = GraphicalStatusPhase.ACTION;
		}

		ImageIcon agentIconToUse = AgentMain.getAgentIcon();
		if (AgentMain.getCurrentSession() != null && AgentMain.getCurrentSession().getCustomizationManager() != null) {
			agentIconToUse = AgentMain.getCurrentSession().getCustomizationManager().getIconToUse();
		}

		if (agentIconToUse != null) {
			setIconImage(agentIconToUse.getImage());
		}

		printLicense = new JLabel("<html><u>" + translator.getTranslation("cua.eula.print") + "</u></html>");
		if (Desktop.getDesktop().isSupported(Action.PRINT)) {
			printLicense.setForeground(Color.BLUE);
			printLicense.addMouseListener(this);
			printLicense.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			printLicense.setForeground(Color.GRAY);
		}

		setSize(640, 500);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		actionsLog.setEditable(false);
		actionsLog.setLineWrap(true);
		actionsLog.setWrapStyleWord(true);
		actionsLog.setBackground(getBackground());

		actionsPane = new JScrollPane(actionsLog);

		progressBar = new JProgressBar();
		progressBar.setMaximum(100);

		bottomButton = new JButton(translator.getTranslation("cua.window.cancel"));
		bottomButton.addActionListener(this);

		acceptButton = new JButton(translator.getTranslation("cua.eula.accept"));
		acceptButton.addActionListener(this);

		declineButton = new JButton(translator.getTranslation("cua.eula.decline"));
		declineButton.addActionListener(this);

		status = new JLabel();
		currentTask = new JLabel();

		topText = new JLabel();

		contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		contentLayout = new SpringLayout();
		contentPanel.setLayout(contentLayout);

		agentIcon = new JLabel(new ImageIcon(agentIconToUse.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));

		Container contentPane = getContentPane();

		layout.putConstraint(SpringLayout.NORTH, contentPanel, 20, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, contentPanel, 20, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, contentPanel, -20, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, contentPanel, -50, SpringLayout.SOUTH, contentPane);

		layout.putConstraint(SpringLayout.EAST, bottomButton, -20, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, bottomButton, -10, SpringLayout.SOUTH, contentPane);

		layout.putConstraint(SpringLayout.EAST, acceptButton, -20, SpringLayout.WEST, bottomButton);
		layout.putConstraint(SpringLayout.SOUTH, acceptButton, -10, SpringLayout.SOUTH, contentPane);

		layout.putConstraint(SpringLayout.EAST, declineButton, -20, SpringLayout.WEST, acceptButton);
		layout.putConstraint(SpringLayout.SOUTH, declineButton, -10, SpringLayout.SOUTH, contentPane);

		contentLayout.putConstraint(SpringLayout.NORTH, agentIcon, 10, SpringLayout.NORTH, contentPanel);
		contentLayout.putConstraint(SpringLayout.WEST, agentIcon, 10, SpringLayout.WEST, contentPanel);
		contentLayout.putConstraint(SpringLayout.EAST, agentIcon, 10 + 64, SpringLayout.WEST, contentPanel);
		contentLayout.putConstraint(SpringLayout.SOUTH, agentIcon, 10 + 64, SpringLayout.NORTH, contentPanel);

		contentLayout.putConstraint(SpringLayout.NORTH, topText, 0, SpringLayout.NORTH, agentIcon);
		contentLayout.putConstraint(SpringLayout.WEST, topText, 10, SpringLayout.EAST, agentIcon);
		contentLayout.putConstraint(SpringLayout.EAST, topText, -10, SpringLayout.EAST, contentPanel);
		contentLayout.putConstraint(SpringLayout.SOUTH, topText, 0, SpringLayout.SOUTH, agentIcon);

		contentLayout.putConstraint(SpringLayout.NORTH, status, 50, SpringLayout.SOUTH, agentIcon);
		contentLayout.putConstraint(SpringLayout.WEST, status, 10, SpringLayout.WEST, contentPanel);
		contentLayout.putConstraint(SpringLayout.EAST, status, -10, SpringLayout.EAST, contentPanel);
		contentLayout.putConstraint(SpringLayout.SOUTH, status, 50 + 20, SpringLayout.SOUTH, agentIcon);

		contentLayout.putConstraint(SpringLayout.NORTH, actionsPane, 10, SpringLayout.SOUTH, status);
		contentLayout.putConstraint(SpringLayout.WEST, actionsPane, 10, SpringLayout.WEST, contentPanel);
		contentLayout.putConstraint(SpringLayout.EAST, actionsPane, -10, SpringLayout.EAST, contentPanel);

		contentLayout.putConstraint(SpringLayout.NORTH, currentTask, 30, SpringLayout.SOUTH, actionsPane);
		contentLayout.putConstraint(SpringLayout.WEST, currentTask, 10, SpringLayout.WEST, contentPanel);
		contentLayout.putConstraint(SpringLayout.EAST, currentTask, -10, SpringLayout.EAST, contentPanel);
		contentLayout.putConstraint(SpringLayout.SOUTH, currentTask, 60, SpringLayout.SOUTH, actionsPane);

		contentLayout.putConstraint(SpringLayout.NORTH, progressBar, 10, SpringLayout.SOUTH, currentTask);
		contentLayout.putConstraint(SpringLayout.WEST, progressBar, 10, SpringLayout.WEST, contentPanel);
		contentLayout.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPanel);
		contentLayout.putConstraint(SpringLayout.SOUTH, progressBar, -30, SpringLayout.SOUTH, contentPanel);

		layout.putConstraint(SpringLayout.WEST, printLicense, 20, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, printLicense, -10, SpringLayout.SOUTH, contentPane);

		setAlwaysOnTop(true);

		contentPanel.add(agentIcon);
		contentPanel.add(actionsPane);
		contentPanel.add(status);
		contentPanel.add(currentTask);
		contentPanel.add(progressBar);
		contentPanel.add(topText);
		add(contentPanel);
		add(bottomButton);
		add(acceptButton);
		add(declineButton);
		add(printLicense);

		refreshDisplay();

	}

	public void declineEulaCallback() {
		for (ProductInformations info : toUpdate) {
			if (actionResults.get(info) == null) {
				actionResults.put(info, ActionResult.CANCELLED);
			}
		}
		bottomButtonClick = () -> {
			setVisible(false);
			dispose();
			AgentMain.getCurrentSession().setState(AgentSessionState.IDLE);
		};
		AgentMain.getCurrentSession().setLastActionResults(actionResults);

		HistoryFileUtil history = AgentMain.getCurrentSession().getHistoryFileUtil();
		List<ActionRecord> records = new ArrayList<ActionRecord>();
		for (ProductInformations prodInf : actionResults.keySet()) {
			ActionResult result = actionResults.get(prodInf);
			HybridInformations updateInfos = prodInf.searchUpdateInformation();
			ActionRecord record = new ActionRecord(prodInf.getProductName(), result, System.currentTimeMillis(), false,
					prodInf.getProductInstallationPath(), prodInf.getCurrentVersion(), updateInfos.getVersionName(),
					prodInf.getProductFeatures());
			records.add(record);
		}

		history.addHistoryItems(records);

		setTopText(translator.getTranslation("cua.cancelled.details"));
		setBottomButtonText(translator.getTranslation("cua.window.close"));
		setBottomButtonEnabled(true);
		setStatus(GraphicalStatusPhase.END);
		refreshDisplay();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bottomButton) {
			if (bottomButtonClick != null) {
				bottomButtonClick.run();
			}
		}
		if (e.getSource() == declineButton) {
			declineEulaCallback();
		}
		if (e.getSource() == acceptButton) {
			agreementCounter++;
			if (agreementCounter >= agreementMax) {
				new UpdatesInstallerThread(toUpdate, this, actionResults, requiredDownloads).start();
				return;
			}
			for (String eula : agreements.keySet()) {
				actionsLog.setText(eula);
				actionsLog.setCaretPosition(0);
				prodInfosCurrent = agreements.get(eula);
				agreements.remove(eula);
				break;
			}
			refreshDisplay();
		}
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
		if (e.getSource() == printLicense && supportPrinting) {
			AgentSession session = AgentMain.getCurrentSession();
			if (session == null) {
				JOptionPane.showMessageDialog(this, translator.getTranslation("cua.eula.print.failure"),
						translator.getTranslation("cua.eula.print.failure.title"), JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (session.getPathProvider() == null) {
				JOptionPane.showMessageDialog(this, translator.getTranslation("cua.eula.print.failure"),
						translator.getTranslation("cua.eula.print.failure.title"), JOptionPane.WARNING_MESSAGE);
				return;
			}

			File eulaPrint = new File(session.getPathProvider().getAccessibleFolder(), "eula_print.txt");
			try (BufferedWriter output = new BufferedWriter(new FileWriter(eulaPrint))) {
				output.write(actionsLog.getText());
			} catch (IOException exc) {
				JOptionPane.showMessageDialog(this, translator.getTranslation("cua.eula.print.failure"),
						translator.getTranslation("cua.eula.print.failure.title"), JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				Desktop.getDesktop().print(eulaPrint);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, translator.getTranslation("cua.eula.print.failure"),
						translator.getTranslation("cua.eula.print.failure.title"), JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
	}

}
