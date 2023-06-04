#!/bin/sh
#installing nrpe
sudo apt-get update   
sudo apt-get install -y nano zip inetutils-ping unzip bzip2 gzip openjdk-11-jdk ufw autoconf automake openssl gcc libc6 libmcrypt-dev make libssl-dev wget bc gawk dc build-essential snmp libnet-snmp-perl gettext
cd /tmp
wget --no-check-certificate -O nrpe.tar.gz https://github.com/NagiosEnterprises/nrpe/archive/nrpe-4.1.0.tar.gz
tar xzf nrpe.tar.gz
cd /tmp/nrpe-nrpe-4.1.0/
sudo ./configure --enable-command-args --with-ssl-lib=/usr/lib/aarch64-linux-gnu/
sudo make all
sudo make install-groups-users
sudo make install
sudo make install-config
sudo sh -c "echo >> /etc/services"
sudo sh -c "sudo echo '# Nagios services' >> /etc/services"
sudo sh -c "sudo echo 'nrpe    5666/tcp' >> /etc/services"
sudo make install-init
sudo systemctl enable nrpe.service
sudo apt-get install -y ufw
sudo mkdir -p /etc/ufw/applications.d
sudo sh -c "echo '[NRPE]' > /etc/ufw/applications.d/nagios"
sudo sh -c "echo 'title=Nagios Remote Plugin Executor' >> /etc/ufw/applications.d/nagios"
sudo sh -c "echo 'description=Allows remote execution of Nagios plugins' >> /etc/ufw/applications.d/nagios"
sudo sh -c "echo 'ports=5666/tcp' >> /etc/ufw/applications.d/nagios"
sudo ufw allow NRPE
sudo ufw reload
sudo sh -c "sed -i 's/^dont_blame_nrpe=.*/dont_blame_nrpe=1/g' /usr/local/nagios/etc/nrpe.cfg"
sudo systemctl start nrpe.service
#installing nrpe plugins
cd /tmp
wget --no-check-certificate -O nagios-plugins.tar.gz https://github.com/nagios-plugins/nagios-plugins/archive/release-2.2.1.tar.gz
tar zxf nagios-plugins.tar.gz
cd /tmp/nagios-plugins-release-2.2.1/
sudo ./tools/setup
sudo ./configure --build=aarch64-unknown-linux-gnu
sudo make
sudo make install
sudo usermod -aG sudo nagios
sudo usermod -g sudo nagios
sudo sh -c "echo 'nagios ALL=NOPASSWD: ALL' >> /etc/sudoers"