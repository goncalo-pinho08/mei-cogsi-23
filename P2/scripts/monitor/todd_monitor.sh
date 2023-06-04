#!/bin/sh
echo "Script to define the addresses of the machines"
cp -a /vagrant/todd /home/vagrant/mei-isep-todd-aa22bb1bcc52
cd /home/vagrant/mei-isep-todd-aa22bb1bcc52/
sudo apt-get install -y arp-scan
export MONITOR_ADDRESS=$(ip a | awk '/inet / && !/127.0.0.1/ {gsub(/\/.*/, "", $2); print $2}')
export SERVER_ADDRESS=$(sudo arp-scan --interface=eth0 --localnet | awk '/08:00:27:e0:e0:e2/ {print $1}')
echo $MONITOR_ADDRESS
echo $SERVER_ADDRESS
sudo sed -i "/address/c\address $SERVER_ADDRESS" /usr/local/nagios/etc/objects/server.cfg
sudo sed -i "/server_address=/c\server_address=$MONITOR_ADDRESS" /usr/local/nagios/etc/nsca.cfg 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddServerStatus.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddAvailableSessionsStatus.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:10500\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitor.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:600\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXTomcatHeapMemory.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp.java
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:10500\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp2.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:10500\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp3.java
sudo sed -i "/String hostaddress/c\                    String hostaddress = \"$MONITOR_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitorHandler.java 
sudo sed -i "/String hostaddress/c\                    String hostaddress = \"$MONITOR_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXTomcatHeapMemoryMonitorHandler.java  
sudo sed -i "s/args = \['[^']*:10500'\]/args = \['$SERVER_ADDRESS:10500'\]/g" /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
sudo sed -i "s/args = \['[^']*:6000'\]/args = \['$SERVER_ADDRESS:10500'\]/g" /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
sudo sed -i "s/args = \['[^']*'\]/args = \['$SERVER_ADDRESS:10500'\]/g" /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
sudo ./gradlew jar
sudo cp /home/vagrant/mei-isep-todd-aa22bb1bcc52/build/libs/mei-isep-todd-aa22bb1bcc52-1.0.1.jar /usr/local/nagios/libexec/todd-1.0.1.jar
sudo chmod a+x+r /usr/local/nagios/libexec/todd-1.0.1.jar
sudo systemctl restart nagios.service