/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TooltipListDisplay extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -6618829147430054359L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		String text = (String) value;
		setToolTipText(text);
		setText(text);
		return this;
	}

}
