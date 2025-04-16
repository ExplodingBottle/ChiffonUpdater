/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class WizardPage extends JPanel {

	private static final long serialVersionUID = -2760517127864382291L;
	private SpringLayout layout;

	private boolean allowPageMovements;
	private boolean allowCancel;
	private boolean allowNext;

	private PageWindowTemplate windowReference;

	public WizardPage() {
		allowPageMovements = true;
		allowCancel = true;
		allowNext = true;
		layout = new SpringLayout();
		setLayout(layout);
	}

	public SpringLayout getSpringLayout() {
		return layout;
	}

	public PageWindowTemplate getParentingWindow() {
		return windowReference;
	}

	void pageShownPriv(PageWindowTemplate window) {
		windowReference = window;
		pageShown();
	}

	public void pageShown() {

	}

	public final boolean isAllowingPageMovements() {
		return allowPageMovements;
	}

	public final boolean isAllowingCancel() {
		return allowCancel;
	}

	public final boolean isAllowingNext() {
		return allowNext;
	}

	public final void setAllowNext(boolean allow) {
		allowNext = allow;
		windowReference.updatePageSelection();
	}

	public final void setAllowPageMovements(boolean allow) {
		allowPageMovements = allow;
		windowReference.updatePageSelection();
	}

	public final void setAllowCancel(boolean allow) {
		allowCancel = allow;
		windowReference.updatePageSelection();

	}

}
