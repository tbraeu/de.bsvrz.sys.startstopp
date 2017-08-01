/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp
 * Copyright (C) 2007-2017 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.sys.startstopp.console.ui.editor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt.Option;

public class InkarnationEditor extends DialogWindow implements WindowListener {

	private MessageDialogButton result;
	private TextBox nameField;
	private Inkarnation inkarnation;
	private TextBox applikationField;

	public InkarnationEditor(Inkarnation inkarnation) {
		super("StartStopp - Editor: Inkarnation: ");

		this.inkarnation = inkarnation;
		
		setHints(Arrays.asList(Window.Hint.CENTERED));
		setCloseWindowWithEscape(true);
		addWindowListener(this);
		
		initUI();
	}
	
	private void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		Button okButton = new Button("OK", new Runnable() {


			@Override
			public void run() {
//				rechner.setName(nameField.getText());
//				rechner.setTcpAdresse(addresseField.getText());
//				rechner.setPort(portField.getText());
				result = MessageDialogButton.OK;
				close();
			}
		});
		buttonPanel.addComponent(okButton);
		Button cancelButton = new Button("Abbrechen", new Runnable() {

			@Override
			public void run() {
				close();
			}
		});
		buttonPanel.addComponent(cancelButton);

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(2).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Inkarnationsname:"));
		nameField = new TextBox();
		nameField.setText(inkarnation.getInkarnationsName());
		mainPanel.addComponent(nameField, GridLayout.createHorizontallyFilledLayoutData(1));
		
		mainPanel.addComponent(new Label("Applikation:"));
		applikationField = new TextBox("");
		applikationField.setText(inkarnation.getApplikation());
		mainPanel.addComponent(applikationField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Parameter:"));
		Button parameterButton = new Button("Bearbeiten");
		mainPanel.addComponent(parameterButton, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Typ:"));
		ComboBox<StartArt.Option> startArtOption = new ComboBox<StartArt.Option>(StartArt.Option.values());
		// TODO korrekten Offset bestimmen
		startArtOption.setSelectedIndex(0);
		mainPanel.addComponent(startArtOption, GridLayout.createHorizontallyFilledLayoutData(1));

		
		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
		
		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	@Override
	public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
		// TODO Auto-generated method stub
		
	}

}
