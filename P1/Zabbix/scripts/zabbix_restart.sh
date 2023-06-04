sudo su
mysql -u root -e "set global log_bin_trust_function_creators = 0";
exit
sudo systemctl restart zabbix-server zabbix-agent apache2
sudo systemctl enable zabbix-server zabbix-agent apache2