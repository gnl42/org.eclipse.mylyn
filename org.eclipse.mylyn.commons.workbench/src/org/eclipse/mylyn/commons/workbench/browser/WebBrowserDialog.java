/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.commons.workbench.browser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.internal.commons.workbench.CommonsWorkbenchPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Dialog that show the contents of an HTML page or the content of a URL in a dialog.
 * 
 * @author Shawn Minto
 * @author Steffen Pingel
 * @since 3.7
 */
public class WebBrowserDialog extends MessageDialog {

	private String text;

	private Browser browser;

	private Label statusLabel;

	private Text locationLabel;

	public WebBrowserDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
				defaultIndex);
		this.setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE);
	}

	public void setText(String text) {
		this.text = text;
		if (browser != null) {
			browser.setText(text);
		}
	}

	public String getText() {
		return text;
	}

	public boolean setUrl(String url, String postData, String[] headers) {
		return getBrowser().setUrl(url, postData, headers);
	}

	public static int openText(Shell parent, String title, String message, String text) {
		if (PlatformUiUtil.hasInternalBrowser()) {
			WebBrowserDialog dialog = new WebBrowserDialog(parent, title, null, message, NONE,
					new String[] { IDialogConstants.OK_LABEL }, 0);
			dialog.setText(text);
			return dialog.open();
		} else {
			File file = null;
			try {
				file = File.createTempFile("temp", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
				file.deleteOnExit();
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				try {
					writer.write(message);
				} finally {
					writer.close();
				}
			} catch (IOException e) {
				if (file != null) {
					file.delete();
				}
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, CommonsWorkbenchPlugin.ID_PLUGIN,
								"Unexpected error while displaying page", e), StatusManager.LOG); //$NON-NLS-1$
				return Window.CANCEL;
			}
			BrowserUtil.openUrl(file.toURI().toString(), IWorkbenchBrowserSupport.AS_EXTERNAL);
			return Window.OK;
		}
	}

	@Override
	public Control createCustomArea(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		locationLabel = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		locationLabel.setBackground(parent.getBackground());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(locationLabel);

		browser = new Browser(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);

		statusLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusLabel);

		browser.addStatusTextListener(new StatusTextListener() {
			public void changed(StatusTextEvent event) {
				statusLabel.setText((event.text != null) ? event.text : ""); //$NON-NLS-1$
			}
		});
		browser.addLocationListener(new LocationListener() {
			public void changing(LocationEvent event) {
				// ignore			
			}

			public void changed(LocationEvent event) {
				if (!event.top) {
					// ignore nested frames
					return;
				}
				locationLabel.setText(event.location != null ? event.location : ""); //$NON-NLS-1$
			}
		});

		if (text != null) {
			browser.setText(text);
		}

		Dialog.applyDialogFont(parent);
		return parent;
	}

	public Browser getBrowser() {
		return browser;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

}
