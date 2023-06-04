#!/bin/sh
apt-get update
apt-get install -y build-essential libssl-dev git
cd /tmp
git clone https://github.com/NagiosEnterprises/nsca.git
cd /tmp/nsca
./configure --build=arm-linux-gnueabihf
make all
make install
cp /tmp/nsca/src/send_nsca /usr/local/nagios/bin/
cp /tmp/nsca/sample-config/send_nsca.cfg /usr/local/nagios/etc/