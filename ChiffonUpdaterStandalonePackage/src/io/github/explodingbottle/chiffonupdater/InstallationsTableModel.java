/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

public class InstallationsTableModel extends AbstractTableModel {

	private String[] headers;
	private static final long serialVersionUID = -1343449212718827954L;
	private List<DetectionResult> products;
	private DetectionResult selectedProduct;
	private JLabel textUpd;
	private UpdaterThread thread;
	private Translator translator;

	public InstallationsTableModel(List<DetectionResult> products, JLabel textUpd, UpdaterThread thread) {
		translator = PackageMain.getTranslator();
		this.products = products;
		this.textUpd = textUpd;
		this.thread = thread;
		if (products.size() > 0) {
			selectedProduct = products.get(0);
		}
		headers = new String[] { translator.getTranslation("detinst.update"), translator.getTranslation("detinst.name"),
				translator.getTranslation("detinst.features"), translator.getTranslation("detinst.version"),
				translator.getTranslation("detinst.path") };
	}

	@Override
	public int getColumnCount() {
		return headers.length;
	}

	@Override
	public int getRowCount() {
		return products.size();
	}

	@Override
	public String getColumnName(int cnum) {
		return headers[cnum];
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 0;
	}

	@Override
	public Class<?> getColumnClass(int c) {
		if (c == 0) {
			return Boolean.class;
		}
		return String.class;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 0) {
			selectedProduct = products.get(row);
			fireTableDataChanged();
		}
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		DetectionResult curRes = products.get(arg0);
		switch (arg1) {
		case 0:
			if (products.get(arg0) == selectedProduct) {
				textUpd.setText(translator.getTranslation("detinst.prodsel", selectedProduct.getProductName(),
						String.join(", ", selectedProduct.getProductFeatures()), selectedProduct.getVersion(),
						selectedProduct.getRootProductFolder().getAbsolutePath()));
				thread.setSelectedInstallation(selectedProduct);
				return true;
			}
			return false;
		case 1:
			return curRes.getProductName();
		case 2:
			return String.join(", ", curRes.getProductFeatures());
		case 3:
			return curRes.getVersion();
		case 4:
			return curRes.getRootProductFolder().getAbsolutePath();
		default:
			return null;
		}
	}

}
