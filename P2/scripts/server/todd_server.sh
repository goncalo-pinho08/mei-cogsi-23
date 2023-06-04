#!/bin/sh
echo "Script to define the addresses of the machines"
cp -a /vagrant/todd /home/vagrant/mei-isep-todd-aa22bb1bcc52
cd /home/vagrant/mei-isep-todd-aa22bb1bcc52/
sudo apt-get install -y arp-scan
export MONITOR_ADDRESS=$(sudo arp-scan --interface=eth0 --localnet | awk '/08:00:27:e0:e0:e0/ {print $1}')
export SERVER_ADDRESS=$(ip a | awk '/inet / && !/127.0.0.1/ {gsub(/\/.*/, "", $2); print $2}')
echo $MONITOR_ADDRESS
echo $SERVER_ADDRESS
sudo sed -i "s/^allowed_hosts=.*/allowed_hosts=127.0.0.1,$MONITOR_ADDRESS/g" /usr/local/nagios/etc/nrpe.cfg
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddServerStatus.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddAvailableSessionsStatus.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:10500\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitor.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:6000\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXTomcatHeapMemory.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp.java
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:10500\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp2.java 
sudo sed -i "/String server/c\                    String server = \"$SERVER_ADDRESS:10500\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp3.java 
sudo sed -i "/String hostaddress/c\                    String hostaddress = \"$MONITOR_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitorHandler.java 
sudo sed -i "/String hostaddress/c\                    String hostaddress = \"$MONITOR_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXTomcatHeapMemoryMonitorHandler.java 
#sudo sed -i "s/args = \['[^']*:10500'\]/args = \['$SERVER_ADDRESS:10500'\]/g" /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
#sudo sed -i "s/args = \['[^']*:6000'\]/args = \['$SERVER_ADDRESS:10500'\]/g" /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
#sudo sed -i "s/args = \['[^']*'\]/args = \['$SERVER_ADDRESS:10500'\]/g" /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
./gradlew jar
sudo mkdir /usr/local/todd
sudo cp ./build/libs/mei-isep-todd-aa22bb1bcc52-1.0.1.jar /usr/local/todd/todd.jar
sudo cp /vagrant/files/ToddService.service /etc/systemd/system/ToddService.service
sudo cp /vagrant/files/ToddService.sh /usr/local/todd/ToddService.sh
#not working and i dont know why will change manually
#sudo sed -i "s/-Djava.rmi.server.hostname=[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+/-Djava.rmi.server.hostname=$SERVER_ADDRESS/g" /vagrant/files/ToddService.sh
sudo chmod +x+r /etc/systemd/system/ToddService.service
sudo chmod +x /usr/local/todd/ToddService.sh
sudo systemctl start ToddService
sudo systemctl enable ToddService
sudo systemctl restart nrpe.service