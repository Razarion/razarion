#!/bin/bash

SERVER_DIR="/home/game/razarion-server"

echo "Stopping razarion-server..."
sudo systemctl stop razarion-server

echo "Changing directory to $SERVER_DIR"
cd "$SERVER_DIR" || { echo "Directory not found!"; exit 1; }

echo "Renaming jar file..."
mv razarion-server-0.0.1-SNAPSHOT.jar razarion-server.jar

echo "Starting razarion-server..."
sudo systemctl start razarion-server

echo "Done."
