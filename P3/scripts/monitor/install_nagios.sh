#!/bin/sh
#commands to install nagios on the machine
wget -O nagioscore.tar.gz https://github.com/NagiosEnterprises/nagioscore/archive/nagios-4.4.6.tar.gz
tar xzf nagioscore.tar.gz
cd nagioscore-nagios-4.4.6/
./configure --with-httpd-conf=/etc/apache2/sites-enabled
make all
useradd nagios
usermod -a -G nagios www-data
make install
make install-init
make install-config
make install-commandmode
make install-daemoninit
make install-webconf
a2enmod rewrite
a2enmod cgi
ufw allow Apache
ufw reload
#Creating a nagios user
 htpasswd -c -b /usr/local/nagios/etc/htpasswd.users nagiosadmin nagiosadmin
# Installing plugins
cd /tmp
wget --no-check-certificate -O nagios-plugins.tar.gz https://github.com/nagios-plugins/nagios-plugins/archive/release-2.3.3.tar.gz
tar xzf nagios-plugins.tar.gz
cd nagios-plugins-release-2.3.3/
./tools/setup
./configure
make
make install
#NRPE plugin
cd /tmp/
wget https://github.com/NagiosEnterprises/nrpe/releases/download/nrpe-4.0.2/nrpe-4.0.2.tar.gz
tar xzf nrpe-4.0.2.tar.gz
cd /tmp/nrpe-4.0.2/
./configure --enable-command-args --with-ssl-lib=/usr/lib/aarch64-linux-gnu/
make check_nrpe
make install-plugin