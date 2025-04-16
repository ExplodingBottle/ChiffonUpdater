/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import io.github.explodingbottle.chiffonupdater.project.ProductFilesInfo;

public class FilesTableModel extends AbstractTableModel {

	private static final String[] HEADERS = { "File Path", "Hash" };
	private static final long serialVersionUID = -1343449212718827954L;

	private ProductFilesInfo infos;
	private ToolkitWindow handler;

	public FilesTableModel(ProductFilesInfo infos, ToolkitWindow handler) {
		this.infos = infos;
		this.handler = handler;
	}

	@Override
	public int getColumnCount() {
		return HEADERS.length;
	}

	@Override
	public int getRowCount() {
		return infos.getFileHashesListReference().size() + 1;
	}

	@Override
	public String getColumnName(int cnum) {
		return HEADERS[cnum];
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}

	@Override
	public Class<?> getColumnClass(int c) {
		return String.class;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		String val;
		if (col == 0) {
			val = ((String) value).replace("\\", "/");
			while (val.contains("//")) {
				val = val.replace("//", "/");
			}
			if (val.endsWith("/")) {
				val = val.substring(0, val.length() - 1);
			}
			for (FileInfos finfs : infos.getFileHashesListReference()) {
				if ((row < infos.getFileHashesListReference().size()
						&& finfs != infos.getFileHashesListReference().get(row))
						&& (finfs.getFilePath().startsWith(val + "/") || finfs.getFilePath().equals(val))) {
					return;
				}
			}
		} else {
			val = ((String) value).toLowerCase();
			try {
				new BigInteger(val, 16);
			} catch (NumberFormatException e) {
				return;
			}
		}
		if (row >= infos.getFileHashesListReference().size()) {
			if ("".equals(value))
				return;
			if (col == 0) {
				infos.getFileHashesListReference().add(new FileInfos((String) val, ""));
			} else {
				infos.getFileHashesListReference().add(new FileInfos("", (String) val));
			}
			fireTableRowsInserted(row + 1, row + 1);
			fireTableRowsUpdated(row, row + 1);
			handler.onDataChanged();
			return;
		}
		if (col == 0) {
			infos.getFileHashesListReference().get(row).setFilePath((String) val);
		}
		if (col == 1) {
			infos.getFileHashesListReference().get(row).setFileHash((String) val);
		}
		FileInfos infos2 = infos.getFileHashesListReference().get(row);
		if (infos2.getFileHash().trim().isEmpty() && infos2.getFilePath().trim().isEmpty()) {
			infos.getFileHashesListReference().remove(row);
			fireTableRowsDeleted(row, row);
		}
		handler.onDataChanged();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {

		if (arg0 >= infos.getFileHashesListReference().size()) {
			return "";
		}
		switch (arg1) {
		case 0:
			return infos.getFileHashesListReference().get(arg0).getFilePath();
		case 1:
			return infos.getFileHashesListReference().get(arg0).getFileHash();
		default:
			return null;
		}
	}

	public void deleteSelection(int[] rows) {
		List<Object> toDelete = new ArrayList<Object>();
		boolean isChanged = false;
		int min = -1;
		int max = -1;
		for (int row : rows) {
			if (row < infos.getFileHashesListReference().size()) {
				if (min == -1 || row < min) {
					min = row;
				}
				if (max == -1 || row > max) {
					max = row;
				}
				toDelete.add(infos.getFileHashesListReference().get(row));
				isChanged = true;
			}
		}
		if (isChanged) {
			toDelete.forEach(toDel -> {
				infos.getFileHashesListReference().remove(toDel);
			});
			fireTableRowsDeleted(min, max);
			handler.onDataChanged();
		}
	}

}
