# pibell

pibell is a server and client application to get notifications when the doorbell rings.

# Installation

## Server

The following commands are tested on the Raspberry Pi OS.

### Install runtime dependencies
```
sudo apt install openjdk-11-jdk wiringpi
```

### Install build dependencies
```
sudo apt install maven git
```

### Clone the git repo
```
git clone https://github.com/Luap99/pibell
```

### Install the server
```
sudo ./pibell/install-server.sh
```
The script will compile the server and copy the jar to `/opt/pibell/server.jar`. It will also create a systemd unit `/etc/systemd/system/pibell.service` and enable this unit. You can check the status with `systemctl status pibell` and stop the service with `systemctl stop pibell`.

## Client

The client can be used on linux or windows.

### Install runtime dependencies
Debian based:
```
sudo apt install openjdk-11-jdk
```
Fedora:
```
sudo dnf install java-11-openjdk
```
For Windows you can install openjdk 11 from [here](https://jdk.java.net/java-se-ri/11).

### Compile the client
You can compile the client on the server if you are in the pibell directory use
```
mvn --projects client package
```
to create a jar for the client. The jar is placed in `./client/target/client-VERSION.jar`. You can copy this jar to your client system, it should work on linux and windows.

### Run the client
```
java -jar /path/to/client.jar <address>
```

# License
This program is licensed under GPL-3.0.

# Code dependencies
The server uses [pi4j v1.4](https://pi4j.com/1.4/) to read the GPIO pins licensed under Apache-2.0.

The client uses [javafx](https://openjfx.io/) v15.0.1 for the GUI licensed under GPL-2.0.
