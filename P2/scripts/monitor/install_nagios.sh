#!/bin/sh
#commands to install nagios on the machine
wget -O nagioscore.tar.gz https://github.com/NagiosEnterprises/nagioscore/archive/nagios-4.4.6.tar.gz
tar xzf nagioscore.tar.gz
cd nagioscore-nagios-4.4.6/
sudo ./configure --with-httpd-conf=/etc/apache2/sites-enabled
sudo make all
sudo useradd nagios
sudo usermod -a -G nagios www-data
sudo make install
sudo make install-init
sudo make install-config
sudo make install-commandmode
sudo make install-daemoninit
sudo make install-webconf
sudo a2enmod rewrite
sudo a2enmod cgi
sudo ufw allow Apache
sudo ufw reload
#Creating a nagios user
sudo htpasswd -c -b /usr/local/nagios/etc/htpasswd.users nagiosadmin nagiosadmin
#Restarting nagios to apply the changes
sudo systemctl restart apache2.service
sudo systemctl start nagios.service
# Installing plugins
cd /tmp
wget --no-check-certificate -O nagios-plugins.tar.gz https://github.com/nagios-plugins/nagios-plugins/archive/release-2.3.3.tar.gz
tar xzf nagios-plugins.tar.gz
cd nagios-plugins-release-2.3.3/
sudo ./tools/setup
sudo ./configure
sudo make
sudo make install
#NRPE plugin
cd /tmp/
wget https://github.com/NagiosEnterprises/nrpe/releases/download/nrpe-4.0.2/nrpe-4.0.2.tar.gz
tar xzf nrpe-4.0.2.tar.gz
cd /tmp/nrpe-4.0.2/
sudo ./configure --enable-command-args --with-ssl-lib=/usr/lib/aarch64-linux-gnu/
sudo make check_nrpe
sudo make install-plugin