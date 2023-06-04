#!/bin/sh
apt-get update
apt-get install -y build-essential libssl-dev
apt-get install -y git
cd /tmp
git clone https://github.com/NagiosEnterprises/nsca.git
cd /tmp/nsca
./configure --build=arm-linux-gnueabihf
make install
#cp /tmp/nsca/src/nsca /usr/local/nagios/bin/
#cp /tmp/nsca/sample-config/nsca.cfg /usr/local/nagios/etc/
#/usr/local/nagios/bin/nsca -c /usr/local/nagios/etc/nsca.cfg