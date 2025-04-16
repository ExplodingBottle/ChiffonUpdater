/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LocationSelectionPage extends WizardHeaderPage implements DetectionDoneCallback {

	private static final long serialVersionUID = -4819372512530802934L;

	private JLabel searchingImage;
	private JLabel loadingText;
	private JLabel pickupText;
	private JLabel programText;

	private JTable detectedPrograms;
	private JScrollPane detectedProgramsScroller;

	private boolean detectionAlreadyDone;

	private UpdaterThread updThread;

	private Translator translator;

	public LocationSelectionPage(UpdaterThread updThread) {
		translator = PackageMain.getTranslator();

		updateUpperText(translator.getTranslation("detection.title"));

		ImageIcon detectionGif = PackageMain.getImageResourcesLoader().getLoadedResource("detection.gif");

		SpringLayout layout = getSpringLayout();

		if (detectionGif != null) {
			searchingImage = new JLabel(detectionGif);
		} else {
			searchingImage = new JLabel();
		}

		this.updThread = updThread;

		detectedPrograms = new JTable(new Object[][] {}, new Object[] {});
		detectedPrograms.setBackground(getBackground());
		detectedProgramsScroller = new JScrollPane(detectedPrograms);
		detectedPrograms.setFillsViewportHeight(true);

		layout.putConstraint(SpringLayout.NORTH, searchingImage, 50, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.SOUTH, searchingImage, 50 + 128, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.EAST, searchingImage, -230, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, searchingImage, 230, SpringLayout.WEST, this);

		loadingText = new JLabel(translator.getTranslation("detection.dettext"), SwingConstants.CENTER);

		pickupText = new JLabel(translator.getTranslation("detection.pickup"));

		programText = new JLabel();

		layout.putConstraint(SpringLayout.NORTH, pickupText, 10, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.SOUTH, pickupText, 10 + 40, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.EAST, pickupText, -10, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, pickupText, 10, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, loadingText, 50, SpringLayout.SOUTH, searchingImage);
		layout.putConstraint(SpringLayout.EAST, loadingText, -50, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, loadingText, 50, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, detectedProgramsScroller, 10, SpringLayout.SOUTH, pickupText);
		layout.putConstraint(SpringLayout.SOUTH, detectedProgramsScroller, 10 + 150, SpringLayout.SOUTH, pickupText);
		layout.putConstraint(SpringLayout.EAST, detectedProgramsScroller, -10, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, detectedProgramsScroller, 10, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, programText, 10, SpringLayout.SOUTH, detectedProgramsScroller);
		layout.putConstraint(SpringLayout.SOUTH, programText, -10, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, programText, -10, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, programText, 10, SpringLayout.WEST, this);

		detectedProgramsScroller.setVisible(false);
		pickupText.setVisible(false);
		programText.setVisible(false);

		add(searchingImage);
		add(loadingText);
		add(programText);
		add(pickupText);
		add(detectedProgramsScroller);

	}

	public void pageShown() {
		if (!detectionAlreadyDone) {
			setAllowPageMovements(false);
			setAllowCancel(false);
			ProductsDetectionThread thread = new ProductsDetectionThread(this, PackageMain.returnUpdaterState(),
					updThread.getTargetProductName(), updThread.getTargetVersion(), updThread.getUpdaterFunctions(),
					updThread.getIterationsFile(), updThread.getDetectionFiles());
			thread.start();
		}
	}

	@Override
	public void calllbackOnDetectionDone(List<DetectionResult> results) {
		SwingUtilities.invokeLater(() -> {
			if (results.size() == 0) {
				getParentingWindow().showEndWindow(new UpdateNoInstallationsPage());
				return;
			}
			detectionAlreadyDone = true;
			setAllowPageMovements(true);
			setAllowCancel(true);
			searchingImage.setVisible(false);
			loadingText.setVisible(false);
			pickupText.setVisible(true);
			programText.setVisible(true);

			InstallationsTableModel dtm = new InstallationsTableModel(results, programText, updThread);
			detectedPrograms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			detectedPrograms.setDefaultRenderer(String.class, new TooltipListDisplay());

			detectedPrograms.setModel(dtm);

			detectedPrograms.getColumnModel().getColumn(0).setPreferredWidth(20);
			detectedPrograms.getColumnModel().getColumn(1).setPreferredWidth(50);
			detectedPrograms.getColumnModel().getColumn(2).setPreferredWidth(50);
			detectedPrograms.getColumnModel().getColumn(3).setPreferredWidth(50);
			detectedPrograms.getColumnModel().getColumn(4).setPreferredWidth(300);
			detectedPrograms.setAutoCreateRowSorter(true);

			detectedProgramsScroller.setVisible(true);

			updateUpperText(translator.getTranslation("detection.select.title"));
		});

	}

}
