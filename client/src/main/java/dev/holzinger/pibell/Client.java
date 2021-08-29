// Copyright (C) 2021 Paul Holzinger
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package dev.holzinger.pibell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class Client implements Runnable {

	private String ip;
	private int port;
	private App app;
	private Socket server;
	private boolean reconnect;

	public Client(String address, App app) throws URISyntaxException {
		// WORKAROUND: add any scheme to make the resulting URI valid.
		URI uri = new URI("tcp://" + address); // may throw URISyntaxException
		if (uri.getHost() == null || uri.getPort() == -1) {
			throw new URISyntaxException(address, "Verbindung braucht host and port Teil");
		}

		this.ip = uri.getHost();
		this.port = uri.getPort();
		this.app = app;
	}

	/**
	 * run the client
	 */
	public void run() {
		while (true) {
			try (Socket server = new Socket(ip, port)) {
				this.server = server;
				BufferedReader empfang = new BufferedReader(new InputStreamReader(server.getInputStream()));
				app.writeSuccess("Erolgreich verbunden zu: " + server.getInetAddress().getHostAddress());
				// Successfully connected to the server, set reconnect to make sure we reconnect
				// in case we loose connection to the server
				reconnect = true;
				while (true) {
					String msg = empfang.readLine();
					if (msg != null) {
						if (msg.equals("1")) {
							app.createAlarm("Es hat geklingelt");
						} else {
							// we expect a fixed answer from the server, if get something else we should
							// exit
							app.writeError("Unbekannte Antwort vom Server");
							return;
						}
					} else {
						break;
					}
				}
			} catch (UnknownHostException ex) {
				app.writeError("Verbindungsaubau zum Server fehlgeschlagen: " + ex.getMessage());
				return;
			} catch (IOException ex) {
				app.writeError("Verbindung zum Server verloren: " + ex.getMessage());
				// exit if we should not reconnect
				if (!reconnect) {
					return;
				}
				app.writeLog("Versuche Verbindung zum server in 30 Sekunden wiederherzustellen");
				// sleep 30 seconds
				try {
					Thread.sleep(30000);
				} catch (InterruptedException iex) {
					app.writeLog("Unerwarteter Fehler während des wartens : " + iex.getMessage());
				}
			}
		}
	}

	/**
	 * close the connection to the server
	 */
	public void Close() {
		if (server != null) {
			try {
				reconnect = false;
				server.close();
				server = null;
			} catch (IOException e) {
				// ignore errors
			}
		}
	}

}
