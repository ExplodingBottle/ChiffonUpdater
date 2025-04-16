/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;
import io.github.explodingbottle.chiffonupdater.tasks.GenerateExternalLibraryTask;
import io.github.explodingbottle.chiffonupdater.tasks.WebsiteFrontendConfigBuilder;

public class ToolkitWindow extends JFrame implements ActionListener, WindowListener {

	private static final long serialVersionUID = 7089779954228949835L;

	private JMenuBar menuBar;

	private JMenu projectMenu;
	private JMenu fileMenu;
	private JMenu sourceMenu;
	private JMenu webMenu;
	private JMenu toolsMenu;
	private JMenu interrogationMarkMenu;

	private JMenuItem leave;
	private JMenuItem open;
	private JMenu openRecent;
	private JMenuItem newProject;
	private JMenuItem saveProject;
	private JMenuItem saveAs;
	private JMenuItem close;
	private JMenuItem genIntegLib;
	private JMenuItem registerNewProduct;
	private JMenuItem buildAllPackages;
	private JMenuItem connectSource;
	private JMenuItem disconnectSource;

	private JMenuItem connectBackend;
	private JMenuItem disconnectBackend;
	private JMenuItem syncBackend;
	private JMenuItem createFrontendConfig;

	private JMenuItem settings;

	private JMenuItem about;

	private JSplitPane splitPane;
	private JSplitPane splitPane2;

	private File currentlyConnectedSource;
	private File currentlyConnectedBackend;

	private JLabel status;

	private ConsoleViewer console;
	private Editor editor;
	private ProjectManager manager;

	private boolean modified;
	private ChiffonUpdaterProject currentProject;
	private File correspondingProjectFile;

	private Image windowIcon;

	private GlobalLogger logger;

	private Map<JMenuItem, File> recentFilesMap;

	public ChiffonUpdaterProject getProject() {
		return currentProject;
	}

	public Image getWindowIcon() {
		return windowIcon;
	}

	public ToolkitWindow() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension size = toolkit.getScreenSize();
		Dimension twothird = new Dimension(Math.round((float) (size.getWidth() * (2.0 / 3.0))),
				Math.round((float) (size.getHeight() * (2.0 / 3.0))));
		setSize(twothird);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		controlUpdate();

		recentFilesMap = new HashMap<JMenuItem, File>();

		URL icon = this.getClass().getClassLoader().getResource("images/icon.png");
		if (icon != null) {
			windowIcon = new ImageIcon(icon).getImage();
			setIconImage(windowIcon);
		}

		console = new ConsoleViewer();
		logger = ToolkitMain.getGlobalLogger();
		logger.registerViewer(console);

		ToolkitMain.getPersistentStorage().persistentStorageLoad();

		Properties persSettingsProps = ToolkitMain.getPersistentStorage().getSettings();
		if ("true".equals(persSettingsProps.getProperty("source.connected"))) {
			String path = persSettingsProps.getProperty("source.path");
			if (path != null) {
				File src = new File(path);
				if (src.exists() && src.isDirectory()) {
					currentlyConnectedSource = src;
				}
			}
		}
		menuBar = new JMenuBar();

		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		projectMenu = new JMenu("Project");
		projectMenu.setMnemonic(KeyEvent.VK_P);
		sourceMenu = new JMenu("Source");
		sourceMenu.setMnemonic(KeyEvent.VK_S);
		webMenu = new JMenu("Website");
		webMenu.setMnemonic(KeyEvent.VK_E);
		toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		interrogationMarkMenu = new JMenu("Help");
		interrogationMarkMenu.setMnemonic(KeyEvent.VK_H);

		leave = new JMenuItem("Exit", KeyEvent.VK_X);
		leave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		leave.addActionListener(this);

		open = new JMenuItem("Open", KeyEvent.VK_O);
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		open.addActionListener(this);

		openRecent = new JMenu("Open recent...");
		openRecent.setMnemonic(KeyEvent.VK_R);
		openRecent.addActionListener(this);

		newProject = new JMenuItem("New...", KeyEvent.VK_N);
		newProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newProject.addActionListener(this);

		saveProject = new JMenuItem("Save", KeyEvent.VK_S);
		saveProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveProject.addActionListener(this);

		saveAs = new JMenuItem("Save as...", KeyEvent.VK_A);
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		saveAs.addActionListener(this);

		close = new JMenuItem("Close", KeyEvent.VK_C);
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		close.addActionListener(this);

		genIntegLib = new JMenuItem("Generate external library", KeyEvent.VK_G);
		genIntegLib.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		genIntegLib.addActionListener(this);

		buildAllPackages = new JMenuItem("Build all packages", KeyEvent.VK_A);
		buildAllPackages
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		buildAllPackages.addActionListener(this);

		registerNewProduct = new JMenuItem("Register new product", KeyEvent.VK_R);
		registerNewProduct.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		registerNewProduct.addActionListener(this);

		connectSource = new JMenuItem("Connect source folder", KeyEvent.VK_C);
		connectSource
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		connectSource.addActionListener(this);

		disconnectSource = new JMenuItem("Disconnect source folder", KeyEvent.VK_D);
		disconnectSource
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		disconnectSource.addActionListener(this);

		connectBackend = new JMenuItem("Connect backend folder", KeyEvent.VK_B);
		connectBackend
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		connectBackend.addActionListener(this);

		disconnectBackend = new JMenuItem("Disconnect backend folder", KeyEvent.VK_D);
		disconnectBackend
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		disconnectBackend.addActionListener(this);

		syncBackend = new JMenuItem("Synchronize backend files", KeyEvent.VK_Y);
		syncBackend
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		syncBackend.addActionListener(this);

		createFrontendConfig = new JMenuItem("Create frontend configuration file", KeyEvent.VK_F);
		createFrontendConfig
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		createFrontendConfig.addActionListener(this);

		settings = new JMenuItem("Settings", KeyEvent.VK_S);
		settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		settings.addActionListener(this);

		about = new JMenuItem("About", KeyEvent.VK_A);
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		about.addActionListener(this);

		addWindowListener(this);
		fileMenu.add(newProject);
		fileMenu.addSeparator();
		fileMenu.add(open);
		fileMenu.add(openRecent);
		fileMenu.add(saveProject);
		fileMenu.add(saveAs);
		fileMenu.add(close);
		fileMenu.addSeparator();
		fileMenu.add(leave);

		projectMenu.add(registerNewProduct);
		projectMenu.add(genIntegLib);
		projectMenu.add(buildAllPackages);

		sourceMenu.add(connectSource);
		sourceMenu.add(disconnectSource);

		webMenu.add(connectBackend);
		webMenu.add(disconnectBackend);
		webMenu.add(syncBackend);
		webMenu.add(createFrontendConfig);

		toolsMenu.add(settings);

		interrogationMarkMenu.add(about);

		menuBar.add(fileMenu);
		menuBar.add(projectMenu);
		menuBar.add(sourceMenu);
		menuBar.add(webMenu);
		menuBar.add(toolsMenu);
		menuBar.add(interrogationMarkMenu);

		setJMenuBar(menuBar);

		editor = new Editor();
		manager = new ProjectManager(editor, this);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, manager, editor);
		splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, console);

		splitPane.setOneTouchExpandable(true);
		splitPane2.setOneTouchExpandable(true);
		// projectViewer.setPreferredSize(new Dimension(20, 20));

		// editor.addTab("Main configuration", new MainProjectEditor());

		status = new JLabel("");
		status.setHorizontalAlignment(JLabel.LEFT);
		status.setBorder(new BevelBorder(BevelBorder.LOWERED));

		add(BorderLayout.CENTER, splitPane2);
		add(BorderLayout.SOUTH, status);
		updateMenuItems();
		updateSourceTexts();
		updateWebsiteItems();
		updateRecentList(null);
		setVisible(true);
	}

	public ProjectManager getProjectManager() {
		return manager;
	}

	public void generateSourceFolderStructure() {
		if ("true".equalsIgnoreCase(
				ToolkitMain.getPersistentStorage().getSettings().getProperty("source.create.manual"))) {
			return;
		}
		if (currentlyConnectedSource != null && currentProject != null) {
			for (ProductInfo info : currentProject.getProductsList()) {
				File sourceProductInfo = new File(currentlyConnectedSource, info.getProductName());
				if (!sourceProductInfo.exists()) {
					if (!sourceProductInfo.mkdir()) {
						logger.print("Couldn't create automatically folder for product " + sourceProductInfo.getName());
						continue;
					}
				}
				for (VersionInfo vInfo : info.getVersionsReference()) {
					File sourceVersionInfo = new File(sourceProductInfo, vInfo.getVersionName());
					if (!sourceVersionInfo.exists()) {
						if (!sourceVersionInfo.mkdir()) {
							logger.print("Couldn't create automatically folder for version " + vInfo.getVersionName()
									+ " of product " + sourceProductInfo.getName());
							continue;
						}
					}
					for (String feature : info.getFeaturesReference()) {
						File sourceFeature = new File(sourceVersionInfo, feature);
						if (!sourceFeature.exists()) {
							if (!sourceFeature.mkdir()) {
								logger.print("Couldn't create automatically folder for feature " + feature
										+ " of version " + vInfo.getVersionName() + " of product "
										+ sourceProductInfo.getName());
								continue;
							}
						}
					}
				}
			}
		}
	}

	public void updateRecentList(File newRecent) {
		openRecent.removeAll();
		recentFilesMap.clear();
		Properties settings = ToolkitMain.getPersistentStorage().getSettings();
		List<File> recentlyOpened = new ArrayList<File>();
		if (newRecent != null) {
			recentlyOpened.add(newRecent);
		}
		for (int i = 0; i < 5; i++) {
			String mruCur = settings.getProperty("file.mru" + i);
			if (mruCur != null) {
				File f = new File(mruCur);
				if (!recentlyOpened.contains(f)) {
					recentlyOpened.add(f);
				}
			}
		}
		for (int i = 0; i < 5; i++) {
			JMenuItem mruItem = new JMenuItem();
			if (i < recentlyOpened.size() && recentlyOpened.get(i) != null) {
				File f = recentlyOpened.get(i);
				settings.setProperty("file.mru" + i, f.getAbsolutePath());
				mruItem.setText(f.getAbsolutePath());
				mruItem.addActionListener(this);
				recentFilesMap.put(mruItem, f);
			} else {
				settings.remove("file.mru" + i);
				mruItem.setText("No recent entry...");
				mruItem.setEnabled(false);
			}
			openRecent.add(mruItem);
		}
		ToolkitMain.getPersistentStorage().persistentStorageSave();

	}

	public void controlUpdate() {
		if (currentProject == null) {
			setTitle("Chiffon Updater Toolkit");
		} else {
			String projName = null;
			if (correspondingProjectFile == null) {
				projName = "Unnamed";
			} else {
				projName = correspondingProjectFile.getName();
			}
			if (modified) {
				projName = "*" + projName;
			}
			setTitle("Chiffon Updater Toolkit - " + projName);
		}
		treeUpdate();
	}

	public void treeUpdate() {
		if (manager != null) {
			manager.updateFromProject(currentProject, correspondingProjectFile, modified);
		}
	}

	public void updateSourceTexts() {
		if (currentlyConnectedSource != null) {
			connectSource.setEnabled(false);
			disconnectSource.setEnabled(true);
			status.setText("Connected to source folder: " + currentlyConnectedSource.getAbsolutePath());
		} else {
			connectSource.setEnabled(true);
			disconnectSource.setEnabled(false);
			status.setText("Source folder not connected");
		}
	}

	public void updateWebsiteItems() {
		if (currentlyConnectedBackend != null) {
			connectBackend.setEnabled(false);
			disconnectBackend.setEnabled(true);
			syncBackend.setEnabled(true);
		} else {
			connectBackend.setEnabled(true);
			disconnectBackend.setEnabled(false);
			syncBackend.setEnabled(false);
		}

	}

	public void updateMenuItems() {
		if (currentProject == null) {
			saveAs.setEnabled(false);
			saveProject.setEnabled(false);
			close.setEnabled(false);
			projectMenu.setEnabled(false);
			webMenu.setEnabled(false);
		} else {
			saveAs.setEnabled(true);
			saveProject.setEnabled(true);
			close.setEnabled(true);
			projectMenu.setEnabled(true);
			webMenu.setEnabled(true);
		}
	}

	public void closeToolkit() {
		if (closeProject(true)) {
			setVisible(false);
			dispose();
		}

	}

	public void setIsWindowBusy(boolean isBusy) {
		if (isBusy) {
			// editor.clearEditor();
			fileMenu.setEnabled(false);
			projectMenu.setEnabled(false);
			webMenu.setEnabled(false);
			sourceMenu.setEnabled(false);
			interrogationMarkMenu.setEnabled(false);
			toolsMenu.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			fileMenu.setEnabled(true);
			sourceMenu.setEnabled(true);
			interrogationMarkMenu.setEnabled(true);
			toolsMenu.setEnabled(true);
			updateMenuItems();
			updateWebsiteItems();
			setCursor(Cursor.getDefaultCursor());
		}
	}

	public void eraseProjRef() {
		modified = false;
		currentProject = null;
		correspondingProjectFile = null;
		currentlyConnectedBackend = null;
		controlUpdate();
		updateMenuItems();
		updateWebsiteItems();
		editor.clearEditor();
	}

	public boolean closeProject(boolean showConfirm) {
		if (currentProject == null || !modified) {
			logger.print("Noting to save.");
			eraseProjRef();
			return true;
		}
		int result = JOptionPane.showConfirmDialog(this, "You have left your project unsaved. Do you want to save it ?",
				"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		switch (result) {
		case 0:
			logger.print("Will prompt a project save.");
			if (saveProject()) {
				if (showConfirm) {
					JOptionPane.showMessageDialog(this, "The project has been saved!", "Save notice",
							JOptionPane.INFORMATION_MESSAGE);
				}
				eraseProjRef();
				return true;
			} else {
				JOptionPane.showMessageDialog(this, "Failed to save the project.", "Save notice",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		case 1:
			logger.print("Project will be discarded.");
			eraseProjRef();
			return true;
		default:
			logger.print("Cancelled close operation.");
			return false;
		}
	}

	public boolean saveProjectInternal(File destination) {
		try (FileOutputStream output = new FileOutputStream(destination);
				HighCompressionGZOutputStream gzipOutput = new HighCompressionGZOutputStream(output);
				ObjectOutputStream obOutput = new ObjectOutputStream(gzipOutput)) {
			obOutput.writeObject(currentProject);
			logger.print("Project saved successfully.");
		} catch (IOException e) {
			logger.print("Failed to save the project.");
			logger.printThrowable(e);
			JOptionPane.showMessageDialog(this, "An error has occured while saving your project. Please try again.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		projectUpdateSourceFolderNames();
		generateSourceFolderStructure();
		projectUpdateOnDiskNames();
		modified = false;
		controlUpdate();
		return true;
	}

	public boolean saveProjectAs() {
		JFileChooser chooser = new JFileChooser();
		if (correspondingProjectFile != null) {
			chooser.setSelectedFile(correspondingProjectFile);
		}
		chooser.setFileFilter(new ProjectFileFilter());
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			if (!selected.getName().endsWith(".cupj")) {
				selected = new File(selected.getParentFile(), selected.getName() + ".cupj");
			}
			correspondingProjectFile = selected;
			if (saveProjectInternal(selected)) {
				updateRecentList(selected);
				return true;
			} else {
				correspondingProjectFile = null;
			}
		}
		return false;
	}

	public boolean saveProject() {
		if (correspondingProjectFile == null) {
			logger.print("Will prompt for file location because no file location recorded before.");
			return saveProjectAs();
		} else {
			return saveProjectInternal(correspondingProjectFile);
		}
	}

	public void projectUpdateOnDiskNames() {
		if (currentProject != null) {
			for (ProductInfo prodInf : currentProject.getProductsList()) {
				prodInf.updateOnDiskName();
				for (VersionInfo versInf : prodInf.getVersionsReference()) {
					versInf.updateOnDiskName();
				}
			}
		}
	}

	public void projectUpdateSourceFolderNames() {
		if (currentProject != null && currentlyConnectedSource != null) {
			for (ProductInfo prodInf : currentProject.getProductsList()) {
				File prodRealFolder = new File(currentlyConnectedSource, prodInf.getProductName());
				if (prodInf.getOnDiskName() != null && !prodInf.getProductName().equals(prodInf.getOnDiskName())) {
					try {
						Files.move(new File(currentlyConnectedSource, prodInf.getOnDiskName()).toPath(),
								prodRealFolder.toPath());
					} catch (IOException e) {
						logger.printThrowable(e);
						continue;
					}
				}
				for (VersionInfo versInf : prodInf.getVersionsReference()) {
					if (versInf.getOnDiskName() != null && !versInf.getVersionName().equals(versInf.getOnDiskName())) {
						try {
							Files.move(new File(prodRealFolder, versInf.getOnDiskName()).toPath(),
									new File(prodRealFolder, versInf.getVersionName()).toPath());
						} catch (IOException e) {
							logger.printThrowable(e);
							continue;
						}
					}
				}
			}
		}
	}

	private void openProjectInternal(File selected) {
		if (closeProject(false)) {
			try (FileInputStream reader = new FileInputStream(selected);
					GZIPInputStream gzipInput = new GZIPInputStream(reader);
					ObjectInputStream obReader = new ObjectInputStream(gzipInput)) {
				ChiffonUpdaterProject project = (ChiffonUpdaterProject) obReader.readObject();
				currentProject = project;
				correspondingProjectFile = selected;
				updateMenuItems();
				updateWebsiteItems();
				controlUpdate();
				projectUpdateOnDiskNames();
				generateSourceFolderStructure();
				logger.print("Opened the project successfully.");
			} catch (Exception e) {
				logger.print("Failed to open the project.");
				logger.printThrowable(e);
				JOptionPane.showMessageDialog(this,
						"An error has occured while opening your project. Please try again.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			updateRecentList(selected);
			if (!"true".equalsIgnoreCase(
					ToolkitMain.getPersistentStorage().getSettings().getProperty("source.create.manual"))) {
				updateMenuItems();
			}
		} else {
			logger.print("The project close didn't happen, won't open a new project.");
		}
	}

	public void openProject() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new ProjectFileFilter());
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			openProjectInternal(selected);
		} else {
			logger.print("User didn't approve the project opening operation.");
		}
	}

	public void createProject() {

		NewProjectDialog dialog = new NewProjectDialog(this);
		dialog.setVisible(true);

	}

	public File getCurrentlyConnectedBackend() {
		return currentlyConnectedBackend;
	}

	public void dialogEndSaveStatusDirtied() {
		modified = true;
		controlUpdate();
		updateMenuItems();
	}

	public void createdProjectCallback(ChiffonUpdaterProject project) {
		currentProject = project;
		dialogEndSaveStatusDirtied();
		logger.print("Project created callback called.");
	}

	private void disconnectSource() {
		int answer = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to disconnect the source folder?\n"
						+ "You will have to manually choose binaries unless you connect the folder again.",
				"Source folder disconnection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == 0) {
			if (!"true".equalsIgnoreCase(
					ToolkitMain.getPersistentStorage().getSettings().getProperty("source.create.manual"))) {
				if (JOptionPane.showConfirmDialog(this, "You are using automatic source folder modifications.\n"
						+ "Disconnecting the source folder and doing modifications will cause a desynchronization.\n"
						+ "Are you sure that you want to do this?", "Source folder disconnection",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != 0) {
					return;
				}
			}

			ToolkitMain.getPersistentStorage().getSettings().setProperty("source.connected", "false");
			ToolkitMain.getPersistentStorage().persistentStorageSave();
			currentlyConnectedSource = null;
			updateSourceTexts();
		}
	}

	public void connectBackend() {
		String backendLast = ToolkitMain.getPersistentStorage().getSettings().getProperty("backend.lastpath");
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (backendLast != null) {
			File backendLastFile = new File(backendLast);
			if (backendLastFile.exists() && backendLastFile.isDirectory()) {
				chooser.setSelectedFile(backendLastFile);
			}
		}
		int returnVal = chooser.showDialog(this, "Select backend folder");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			currentlyConnectedBackend = chooser.getSelectedFile();
			ToolkitMain.getPersistentStorage().getSettings().setProperty("backend.lastpath",
					currentlyConnectedBackend.getAbsolutePath());
			ToolkitMain.getPersistentStorage().persistentStorageSave();
			updateWebsiteItems();
			treeUpdate();
		}
	}

	public void disconnectBackend() {
		int answer = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to disconnect the backend folder ?\n"
						+ "You won't be able to generate files unless you reconnect the backend folder.",
				"Backend folder disconnection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == 0) {
			currentlyConnectedBackend = null;
			updateWebsiteItems();
			treeUpdate();
		}
	}

	private void connectSource() {
		SourceConnectionDialog scd = new SourceConnectionDialog(this);
		scd.setVisible(true);
	}

	public void updateSource(File ns) {
		ToolkitMain.getPersistentStorage().getSettings().setProperty("source.connected", "true");
		ToolkitMain.getPersistentStorage().getSettings().setProperty("source.path", ns.getAbsolutePath());
		ToolkitMain.getPersistentStorage().persistentStorageSave();
		currentlyConnectedSource = ns;
		updateSourceTexts();
	}

	private void buildAllPackages() {
		BulkProductBinariesDialog dialog = new BulkProductBinariesDialog(this, currentProject, false);
		dialog.setVisible(true);
	}

	private void generateWebConfigFile() {
		WebsiteFrontendConfigBuilder cfgBuilder = new WebsiteFrontendConfigBuilder(this,
				currentProject.getMainInfosRef().getWebsiteInfos(), logger);
		cfgBuilder.start();
	}

	private void syncBackendFiles() {
		BulkProductBinariesDialog dialog = new BulkProductBinariesDialog(this, currentProject, true);
		dialog.setVisible(true);
	}

	private void showAboutDialog() {
		AboutDialog dialog = new AboutDialog(this);
		dialog.setVisible(true);
	}

	private void showSettingsDialog() {
		ToolkitSettings dialog = new ToolkitSettings(this);
		dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == leave) {
			closeToolkit();
		} else if (e.getSource() == newProject) {
			createProject();
		} else if (e.getSource() == saveAs) {
			saveProjectAs();
		} else if (e.getSource() == saveProject) {
			saveProject();
		} else if (e.getSource() == close) {
			closeProject(false);
		} else if (e.getSource() == open) {
			openProject();
		} else if (e.getSource() == genIntegLib) {
			generateExternalLibrary();
		} else if (e.getSource() == registerNewProduct) {
			regNewProduct();
		} else if (e.getSource() == disconnectSource) {
			disconnectSource();
		} else if (e.getSource() == connectSource) {
			connectSource();
		} else if (e.getSource() == buildAllPackages) {
			buildAllPackages();
		} else if (e.getSource() == disconnectBackend) {
			disconnectBackend();
		} else if (e.getSource() == connectBackend) {
			connectBackend();
		} else if (e.getSource() == createFrontendConfig) {
			generateWebConfigFile();
		} else if (e.getSource() == syncBackend) {
			syncBackendFiles();
		} else if (e.getSource() == about) {
			showAboutDialog();
		} else if (e.getSource() == settings) {
			showSettingsDialog();
		} else {
			File relatedFile = recentFilesMap.get(e.getSource());
			if (relatedFile != null) {
				openProjectInternal(relatedFile);
			}
		}

	}

	public void regNewProduct() {
		NewProductDialog dialog = new NewProductDialog(this);
		dialog.setVisible(true);
	}

	public void generateExternalLibrary() {
		GenerateExternalLibraryTask task = new GenerateExternalLibraryTask(this);
		task.start();
	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		closeToolkit();
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

	public File getCurrentConnectedSource() {
		return currentlyConnectedSource;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		splitPane.setDividerLocation(0.27);
		splitPane2.setDividerLocation(0.79);
	}

	public void onDataChanged() {
		if (!modified) {
			modified = true;
		}
		controlUpdate();
	}

}
