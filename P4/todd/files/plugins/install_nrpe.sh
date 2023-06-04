#!/bin/sh
#installing nrpe
apt-get update
apt-get install -y arp-scan nano zip inetutils-ping unzip bzip2 gzip openjdk-8-jdk ufw autoconf automake openssl gcc libc6 libmcrypt-dev make libssl-dev wget bc gawk dc build-essential snmp libnet-snmp-perl gettext
cd /tmp
wget --no-check-certificate -O nrpe.tar.gz https://github.com/NagiosEnterprises/nrpe/archive/nrpe-4.1.0.tar.gz
tar xzf nrpe.tar.gz
cd /tmp/nrpe-nrpe-4.1.0/
./configure --enable-command-args --with-ssl-lib=/usr/lib/aarch64-linux-gnu/
make all
make install-groups-users
make install
make install-config
sh -c "echo >> /etc/services"
sh -c "echo '# Nagios services' >> /etc/services"
sh -c "echo 'nrpe    5666/tcp' >> /etc/services"
make install-init
systemctl enable nrpe.service
apt-get install -y ufw
mkdir -p /etc/ufw/applications.d
sh -c "echo '[NRPE]' > /etc/ufw/applications.d/nagios"
sh -c "echo 'title=Nagios Remote Plugin Executor' >> /etc/ufw/applications.d/nagios"
sh -c "echo 'description=Allows remote execution of Nagios plugins' >> /etc/ufw/applications.d/nagios"
sh -c "echo 'ports=5666/tcp' >> /etc/ufw/applications.d/nagios"
ufw allow NRPE
ufw reload
sed -i '/^allowed_hosts=/s/$/,10.5.0.3/' /usr/local/nagios/etc/nrpe.cfg
sed -i '' 's/^dont_blame_nrpe=.*/dont_blame_nrpe=1/g' /usr/local/nagios/etc/nrpe.cfg
systemctl start nrpe.service
#installing nrpe plugins
cd /tmp
wget --no-check-certificate -O nagios-plugins.tar.gz https://github.com/nagios-plugins/nagios-plugins/archive/release-2.2.1.tar.gz
tar zxf nagios-plugins.tar.gz
cd /tmp/nagios-plugins-release-2.2.1/
./tools/setup
./configure --build=aarch64-unknown-linux-gnu
make
make install
usermod -aG nagios
usermod -g nagios
sh -c "echo 'command[restart_todd]=/usr/local/nagios/libexec/restart_todd.sh' >> /usr/local/nagios/etc/nrpe.cfg"
sh -c "echo 'command[grow_todd]=java -cp /usr/local/todd/todd.jar net.jnjmx.todd.JMXToddServerGrow' >> /usr/local/nagios/etc/nrpe.cfg"
sh -c "echo 'nagios ALL=NOPASSWD: ALL' >> /etc/sudoers"
/usr/local/nagios/bin/nrpe -c /usr/local/nagios/etc/nrpe.cfg -d
