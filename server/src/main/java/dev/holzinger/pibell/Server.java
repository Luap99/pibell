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

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Server {

	private String ip;
	private int port;
	private ArrayList<Socket> clients = new ArrayList<Socket>();

	private static void usage() {
		System.err.println("pibell [LISTEN_ADDRESS:PORT]");
	}

	private static void errorAndExit(String message) {
		System.err.println("Error: " + message);
		usage();
		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			errorAndExit("Zu viele argumente");
		}

		// defaults
		String listenIP = "0.0.0.0";
		int listenPort = 12321;

		if (args.length == 1) {
			if (args[0].equals("--help") || args[0].equals("-h")) {
				// user wants help print usage and exit
				usage();
				return;
			}

			try {
				// WORKAROUND: add any scheme to make the resulting URI valid.
				URI uri = new URI("tcp://" + args[0]); // may throw URISyntaxException
				if (uri.getHost() == null || uri.getPort() == -1) {
					throw new URISyntaxException(args[0], "Verbindung braucht host and port Teil");
				}

				listenIP = uri.getHost();
				listenPort = uri.getPort();

			} catch (URISyntaxException ex) {
				errorAndExit(ex.getMessage());
			}
		}
		Server server = new Server(listenIP, listenPort);
		server.start();
	}

	private Server(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	private void start() {

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #02 as an input pin with its internal pull down resistor
		// enabled
		final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02,
				PinPullResistance.PULL_DOWN);

		// set shutdown state for this input pin
		myButton.setShutdownOptions(true);

		// create and register gpio pin listener
		myButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				// only use the HIGH state
				// we only care when the bell is pressed not when it is released
				if (event.getState() == PinState.HIGH) {
					System.out.println("Klingel festgestellt");
					// send message to all clients
					broadcast();
				}
			}

		});

		InetAddress listenAddress = null;
		try {
			listenAddress = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			errorAndExit("Ungültige IP Adresse: "+ e.getMessage());
		}
		try (ServerSocket server = new ServerSocket(port, 0, listenAddress)) {
			System.out.println("Server gestartet auf " + ip + ":" + port);
			while (true) {
				Socket client = server.accept();
				// make sure the tcp connection stays alive because we do not send for a long time
				client.setKeepAlive(true);
				clients.add(client);
				System.err.println("Neuer Client verbunden von " + client.getInetAddress().getHostAddress());
			}

		} catch (BindException e) {
			// starting the server socket failed -> e.g. port in use or a non local ip
			errorAndExit("Server konnte nicht gestartet werden: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Unerwarteter IO Fehler");
			e.printStackTrace();
		}
	}

	private void broadcast() {
		System.out.println("Verbundene clients: " + clients.size());
		// NOTE: Do not use foreach otherwise we cannot remove the client inside the
		// loop.
		for (Iterator<Socket> it = clients.iterator(); it.hasNext();) {
			Socket client = it.next();
			try {
				// NOTE: Do not use a PrintWriter here.
				// PrintWriter never throws a IOException
				OutputStream senden = client.getOutputStream();
				// send "1\n"
				senden.write(49);
				senden.write(10);
				senden.flush();
			} catch (IOException ex) {
				System.err.println("Verbindung zu Client " + client.getInetAddress().getHostAddress()+ " getrennt");
				// the client is most likely disconnected lets remove it
				it.remove();
				// ex.printStackTrace();
			}
		}
	}

}