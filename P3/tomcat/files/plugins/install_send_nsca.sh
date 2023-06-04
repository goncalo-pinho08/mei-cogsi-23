#!/bin/sh
sudo apt-get update
sudo apt-get install -y build-essential libssl-dev
sudo apt-get install git
cd /tmp
git clone https://github.com/NagiosEnterprises/nsca.git
cd /tmp/nsca
sudo ./configure --build=arm-linux-gnueabihf
sudo make install
sudo make all
sudo cp /tmp/nsca/src/send_nsca /usr/local/nagios/bin/
sudo cp /tmp/nsca/sample-config/send_nsca.cfg /usr/local/nagios/etc/