cd /tmp
sudo wget https://repo.zabbix.com/zabbix/6.0/ubuntu-arm64/pool/main/z/zabbix-release/zabbix-release_6.0-5+ubuntu20.04_all.deb
sudo dpkg -i zabbix-release_6.0-5+ubuntu20.04_all.deb
sudo apt update
sudo apt install -y zabbix-server-mysql zabbix-frontend-php zabbix-apache-conf zabbix-sql-scripts zabbix-agent
sudo apt-get -y install mysql-server
sudo sed -i "s/# DBPassword=/DBPassword=password/g" /etc/zabbix/zabbix_server.conf
sudo su
mysql -u root -e "create database zabbix character set utf8mb4 collate utf8mb4_bin";
mysql -u root -e "create user zabbix@localhost identified by 'password'";
mysql -u root -e "grant all privileges on zabbix.* to zabbix@localhost";
mysql -u root -e "set global log_bin_trust_function_creators = 1";
sudo zcat -v -f /usr/share/zabbix-sql-scripts/mysql/server.sql.gz | mysql --default-character-set=utf8mb4 -uzabbix -ppassword zabbix --verbose
exit