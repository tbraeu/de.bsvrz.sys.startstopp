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

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class RechnerTable extends Table<String> {

	private StartStoppSkript skript;
	private WindowBasedTextGUI gui;

	public RechnerTable(WindowBasedTextGUI gui, StartStoppSkript skript) {
		super("Name", "Host", "Port");
		this.skript = skript;
		this.gui = gui;

		setSelectAction(new Runnable() {
			@Override
			public void run() {
				int row = getSelectedRow();
				Rechner rechner = skript.getGlobal().getRechner().get(row);
				RechnerEditor editor = new RechnerEditor(skript, rechner);
				MessageDialogButton result = editor.showDialog(gui);
				if (result == MessageDialogButton.OK) {
					getTableModel().setCell(0, row, rechner.getName());
					getTableModel().setCell(1, row, rechner.getTcpAdresse());
					getTableModel().setCell(2, row, rechner.getPort());
				}
			}
		});

		this.skript = skript;
		for (Rechner rechner : skript.getGlobal().getRechner()) {
			getTableModel().addRow(rechner.getName(), rechner.getTcpAdresse(), rechner.getPort());
		}
	}

	@Override
	public Result handleKeyStroke(KeyStroke keyStroke) {
		// TODO Auto-generated method stub
		System.err.println("RechnerTable-Key: " + keyStroke);

		if (keyStroke.getKeyType() == KeyType.Character) {
			switch (keyStroke.getCharacter()) {
			case '+':
				int row = getSelectedRow() + 1;
				Rechner rechner = new Rechner();
				rechner.setName("Neuer Rechner");
				rechner.setTcpAdresse("");
				rechner.setPort("");
				RechnerEditor editor = new RechnerEditor(skript, rechner);
				MessageDialogButton result = editor.showDialog(gui);
				if (result == MessageDialogButton.OK) {
					skript.getGlobal().getRechner().add(row, rechner);
					getTableModel().insertRow(row,
							Arrays.asList(rechner.getName(), rechner.getTcpAdresse(), rechner.getPort()));
				}
				break;
			case '-':
				MessageDialogBuilder builder = new MessageDialogBuilder();
				builder.addButton(MessageDialogButton.Yes);
				builder.addButton(MessageDialogButton.No);
				builder.setTitle("Rechner löschen");
				builder.setText("Soll der ausgewählte Rechner wirklich gelöscht werden?");
				result = builder.build().showDialog(gui);
				if (result.equals(MessageDialogButton.Yes)) {
					int deleteRow = getSelectedRow();
					skript.getGlobal().getRechner().remove(deleteRow);
					getTableModel().removeRow(deleteRow);
				}
				break;
			default:
			}
		}

		return super.handleKeyStroke(keyStroke);
	}
}
