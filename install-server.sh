#!/bin/bash

set -e

# cd into the script directory in case the script is executed from a different directory
cd "$(dirname "$0")"


NAME=pibell

mkdir -p /opt/$NAME

# compile the server code
mvn --projects server package

# move server jar into dir
mv ./server/target/server*.jar /opt/$NAME/server.jar

mvn clean

# create a systemd unit to autostart the program
cat > /etc/systemd/system/$NAME.service << EOF
[Unit]
Description=$NAME service
Wants=network-online.target
After=network-online.target


[Service]
Type=simple
Restart=on-failure
User=pi
ProtectSystem=full
PrivateTmp=true
ExecStart=java -jar /opt/$NAME/server.jar

[Install]
WantedBy=default.target

EOF

# enable and start the unit
systemctl daemon-reload
systemctl enable --now $NAME.service
