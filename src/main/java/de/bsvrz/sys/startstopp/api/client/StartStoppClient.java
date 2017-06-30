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

package de.bsvrz.sys.startstopp.api.client;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.client.ClientConfig;

import de.bsvrz.sys.funclib.operatingMessage.MessageIdFactory;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppstatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;
import de.bsvrz.sys.startstopp.api.server.ApiServer;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppStatusException;

public class StartStoppClient {

	private class StartStoppHostnameVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			if (!startStoppHostName.equals(hostname)) {
				return false;
			}

			String peerHost = session.getPeerHost();
			if (!startStoppHostName.equals(peerHost)) {
				return false;
			}

			return true;
		}
	}

	private StartStoppHostnameVerifier verifier = new StartStoppHostnameVerifier();
	private final String startStoppHostName;
	private final Client client;
	private int port;

	public StartStoppClient(String startStoppHostName, int port) throws StartStoppException {

		this.startStoppHostName = startStoppHostName;
		this.port = port;

		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(ApiServer.class.getResource("keystore.jks").toExternalForm());
		sslContextFactory.setKeyStorePassword("startstopp");
		sslContextFactory.setKeyManagerPassword("startstopp");
		try {
			sslContextFactory.start();
		} catch (Exception e) {
			throw new StartStoppException(e);
		}

		SSLContext sslContext = sslContextFactory.getSslContext();
		client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(verifier)
				.withConfig(new ClientConfig().register(Applikation.class)).build();
	}

	private Response createPostResponse(String string) {
		return createPostResponse(string, null);
	}

	private Response createPostResponse(String path, Object object) {
		Entity<?> entity = null;
		if (object != null) {
			entity = Entity.entity(object, MediaType.APPLICATION_JSON_TYPE);
		}
		Response response = client.target("https://" + startStoppHostName + ":" + port + "/ststapi/v1" + path)
				.request(MediaType.APPLICATION_JSON).post(entity);
		return response;
	}

	private Response createPutResponse(String path, Object object) {
		Entity<?> entity = null;
		if (object != null) {
			entity = Entity.entity(object, MediaType.APPLICATION_JSON_TYPE);
		}
		Response response = client.target("https://" + startStoppHostName + ":" + port + "/ststapi/v1" + path)
				.request(MediaType.APPLICATION_JSON).put(entity);
		return response;
	}

	private Response createGetResponse(String path) {
		Response response = client.target("https://" + startStoppHostName + ":" + port + "/ststapi/v1" + path)
				.request(MediaType.APPLICATION_JSON).get(Response.class);
		return response;
	}

	/* System-Funktionen. */

	public Startstoppstatus getStartStoppStatus() throws StartStoppException {
		Response response = null;
		try {
			response = createGetResponse("/system");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Startstoppstatus.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"SystemStatus konnte nicht abgerufen werden (Response: " + response.getStatus() + ")");
	}

	public void exitStartStopp() throws StartStoppException {
		Response response = null;
		try {
			response = createPostResponse("/system/exit");
			if (response.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException("Anforderung zum Beenden von StartStopp wurde nicht entgegengenommen (Response: "
				+ response.getStatus() + ")");
	}

	public void stoppStartStopp() throws StartStoppException {
		Response response = null;
		try {
			response = createPostResponse("/system/stopp");
			if (response.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Anforderung zum Beenden der StartStopp-Konfiguration wurde nicht entgegengenommen (Response: "
						+ response.getStatus() + ")");
	}

	public void restartStartStopp() throws StartStoppException {
		Response response = null;
		try {
			response = createPostResponse("/system/restart");
			if (response.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Anforderung zum Neustart der StartStopp-Konfiguration wurde nicht entgegengenommen (Response: "
						+ response.getStatus() + ")");
	}

	/* Skript-Funktionen. */

	public Startstoppskript getCurrentSkript() throws StartStoppException {
		Response response = null;
		try {
			response = createGetResponse("/skripte/current");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Startstoppskript.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException("Die aktuelle StartStopp-Konfiguration konnte nicht abgerufen werden (Response: "
				+ response.getStatus() + ")");
	}

	public Startstoppskript setCurrentSkript(Startstoppskript skript) throws StartStoppException {
		Response response = null;
		try {
			response = createPutResponse("/skripte/current", skript);
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Startstoppskript.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException("Die aktuelle StartStopp-Konfiguration konnte nicht gesetzt werden (Response: "
				+ response.getStatus() + ")");
	}

	public Startstoppskriptstatus getCurrentSkriptStatus() throws StartStoppException {
		Response response = null;
		try {
			response = createGetResponse("/skripte/current/status");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Startstoppskriptstatus.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Die Status der aktuellen StartStopp-Konfiguration konnte nicht abgerufen werden (Response: "
						+ response.getStatus() + ")");
	}

	/* Applikation-Funktionen. */

	public List<Applikation> getApplikationen() throws StartStoppException {
		Response response = null;
		try {
			response = createGetResponse("/applikationen");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(new GenericType<List<Applikation>>() {
				});
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Applikationen konnten nicht abgerufen werden (Response: " + response.getStatus() + ")");
	}

	public Applikation getApplikation(String inkarnationsName) throws StartStoppException {
		Response response = null;
		try {
			response = createGetResponse("/applikationen/" + inkarnationsName);
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException("Die Applikation \"" + inkarnationsName
				+ "\"konnte nicht abgerufen werden (Response: " + response.getStatus() + ")");
	}

	public Applikation starteApplikation(String inkarnationsName) throws StartStoppException {
		Response response = null;
		try {
			response = createPostResponse("/applikationen/" + inkarnationsName + "/start");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		if ((response != null) && (response.getStatus() == Response.Status.CONFLICT.getStatusCode())) {
			throw new StartStoppStatusException("Die Applikation \"" + inkarnationsName
					+ "\"konnte nicht gestartet werden (Response: " + response.getStatus() + ")",
					response.readEntity(Statusresponse.class));
		}
		throw new StartStoppException("Die Applikation \"" + inkarnationsName
				+ "\"konnte nicht gestartet werden (Response: " + response.getStatus() + ")");
	}

	public Applikation restarteApplikation(String inkarnationsName) throws StartStoppException {
		Response response = null;
		try {
			response = createPostResponse("/applikationen/" + inkarnationsName + "/restart");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		if ((response != null) && (response.getStatus() == Response.Status.CONFLICT.getStatusCode())) {
			throw new StartStoppStatusException("Die Applikation \"" + inkarnationsName
					+ "\"konnte nicht neu gestartet werden (Response: " + response.getStatus() + ")",
					response.readEntity(Statusresponse.class));
		}
		throw new StartStoppException("Die Applikation \"" + inkarnationsName
				+ "\"konnte nicht neu gestartet werden (Response: " + response.getStatus() + ")");
	}

	public Applikation stoppeApplikation(String inkarnationsName) throws StartStoppException {
		Response response = null;
		try {
			response = createPostResponse("/applikationen/" + inkarnationsName + "/stopp");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		if ((response != null) && (response.getStatus() == Response.Status.CONFLICT.getStatusCode())) {
			throw new StartStoppStatusException("Die Applikation \"" + inkarnationsName
					+ "\"konnte nicht gestoppt gestartet werden (Response: " + response.getStatus() + ")",
					response.readEntity(Statusresponse.class));
		}
		throw new StartStoppException("Die Applikation \"" + inkarnationsName
				+ "\"konnte nicht gestoppt werden (Response: " + response.getStatus() + ")");
	}

}
