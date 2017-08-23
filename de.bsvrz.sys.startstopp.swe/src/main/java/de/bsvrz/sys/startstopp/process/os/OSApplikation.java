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

package de.bsvrz.sys.startstopp.process.os;

import java.util.concurrent.Executors;

import org.jutils.jprocesses.model.ProcessInfo;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;
import de.muspellheim.events.Event;

/**
 * Systemprozess einer Inkarnation.
 * 
 * @author gysi
 *
 */
public class OSApplikation {

	public static final int STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC = 5;

	static final Debug LOGGER = Debug.getLogger();
	
	public final Event<OSApplikationStatus> onStatusChange = new Event<>();
	ProcessInfo processInfo = null;
	Process process;
	private String programm;
	private String inkarnation;
	private OSApplikationStatus status = OSApplikationStatus.UNDEFINED;
	private String startFehler;
	AusgabeVerarbeitung ausgabeUmlenkung;
	int lastExitCode;
	private String programmArgumente;

	private StringBuilder ausgaben = new StringBuilder(1024);

	/**
	 * fügt der gesicherten Prozessausgabe eine Zeile hinzu
	 * 
	 * @param meldung
	 */
	public void addProzessAusgabe(String meldung) {
		if (ausgaben.length() > 0) {
			ausgaben.append('\n');
		}
		ausgaben.append(meldung);
	}

	/**
	 * liefert den zugewiesenen Inkarnationsname.
	 * 
	 * @return der Name
	 */
	public String getInkarnationsName() {
		return inkarnation;
	}

	/**
	 * Gibt den letzten Exit-Code des Prozesses zur&uuml;ck.
	 * 
	 * @return letzter Exit-Code
	 */
	public int getLastExitCode() {
		return lastExitCode;
	}

	/**
	 * Gibt die Pid des Prozesses zur&uuml;ck.
	 * 
	 * @return die Pid des Prozesses, <code>null</code> wenn der Prozess nicht
	 *         gefunden werden konnte
	 */
	public Integer getPid() {
		if (processInfo != null) {
			return Integer.parseInt(processInfo.getPid());
		}
		return null;
	}

	/**
	 * Gibt das auszuf&uuml;rende Programm des Prozesses zur&uuml;ck.
	 * 
	 * @return auszuf&uuml;rendes Programm
	 */
	public String getProgramm() {
		return programm;
	}

	/**
	 * Gibt die Programmargumente zur&uuml;ck.
	 * 
	 * @return die Programmargumente
	 */
	public String getProgrammArgumente() {
		return programmArgumente;
	}

	/**
	 * Gibt die gespeicherten Standardausgaben und Standardfehlerausgaben des
	 * Prozesses zur&uuml;ck.
	 * 
	 * @return Standardausgaben und Standardfehlerausgaben
	 */

	public String getProzessAusgabe() {
		return ausgaben.toString();
	}

	/**
	 * Gibt Informationen zum Startfehler des Prozesses zur&uuml;ck.
	 * 
	 * @return Startfehler-Informationen
	 */
	public String getStartFehler() {
		return startFehler;
	}

	/**
	 * Gibt den aktuellen Status des Prozesses zur&uuml;ck.
	 * 
	 * @return {@link OSApplikationStatus}
	 */
	public OSApplikationStatus getStatus() {
		return status;
	}

	/**
	 * Beendet den Prozess (harte Variante).
	 * <p>
	 * Der Prozess wird &uuml;ber <code>Process.destroyForcibly</code> beendet.
	 * </p>
	 */
	public void kill() {
		process.destroyForcibly();
	}

	void prozessBeendet(int exitCode) {
		lastExitCode = exitCode;
		setStatus(OSApplikationStatus.GESTOPPT);
	}

	void prozessGestartet() {
		setStatus(OSApplikationStatus.GESTARTET);
	}

	void prozessStartFehler(String string) {
		setStatus(OSApplikationStatus.STARTFEHLER);
		startFehler = string;
	}

	/**
	 * Setzt den Inkarnationsnamen des Prozesses.
	 * 
	 * @param command
	 *            der Name
	 */
	public void setInkarnationsName(String command) {
		inkarnation = command;
	}

	/**
	 * Setzt das auszuf&uuml;rende Programm des Prozesses.
	 * 
	 * @param command
	 *            auszuf&uuml;rendes Programm
	 */
	public void setProgramm(String command) {
		this.programm = command;
	}

	/**
	 * Setzt die Programmargumente.
	 * 
	 * @param args
	 *            Programmargumente (Kommandozeile)
	 */
	public void setProgrammArgumente(String args) {
		this.programmArgumente = args;

	}

	private void setStatus(OSApplikationStatus status) {
		this.status = status;
		onStatusChange.send(status);
	}

	/**
	 * Startet den Prozess.
	 */
	public void start() {
		if (getProgramm() == null || getProgramm().length() == 0) {
			throw new IllegalStateException("Kein Programm versorgt");
		}

		if (getInkarnationsName() == null || getInkarnationsName().length() == 0) {
			throw new IllegalStateException("Kein Inkarnationsname versorgt");
		}

		Ueberwacher ueberwacher = new Ueberwacher(this);
		Executors.newSingleThreadExecutor(new NamingThreadFactory(getInkarnationsName())).submit(ueberwacher);
	}

	/**
	 * Terminiert den Prozess (weiche Variante).
	 * <p>
	 * Unter Linux wird der Prozess &uuml;ber <code>Process.destroy</code> beendet
	 * (Signal 15).
	 * </p>
	 * <p>
	 * Unter Windows wird ein CTRl-C-EVENT an den Konsolen-Prozess gesendet.
	 * </p>
	 */
	public void terminate() {
		if (OSTools.isWindows()) {
			int terminateWindowsProzess = OSTools.terminateWindowsProzess(getPid());
			if (terminateWindowsProzess != 0) {
				LOGGER.warning("Fehler beim Terminieren der Inkarnation '" + getInkarnationsName() + "': "
						+ terminateWindowsProzess);
			}
		} else {
			process.destroy();
		}
	}

	/**
	 * gibt an, ob ein Prozess terminiert werden kann oder ob nur kill möglich ist.
	 * 
	 * @return den Status
	 */
	public boolean terminateSupported() {
		// TODO Eventuell unter Java 9 anpassen
		return !OSTools.isWindows();
	}
}