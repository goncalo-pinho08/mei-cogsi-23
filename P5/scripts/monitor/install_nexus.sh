#!/bin/bash

cd /opt/
sudo wget -O latest-unix.tar.gz https://download.sonatype.com/nexus/3/latest-unix.tar.gz
sudo tar -xvzf /opt/latest-unix.tar.gz
sudo mv nexus-3* nexus
sudo mv sonatype-work nexusdata
sudo useradd --system --no-create-home nexus
sudo chown -R nexus:nexus /opt/nexus
sudo chown -R nexus:nexus /opt/nexusdata

sudo sed -i 's/sonatype-work/nexusdata/g' /opt/nexus/bin/nexus.vmoptions

sudo sed -i 's/#run_as_user=""/run_as_user="nexus"/g' /opt/nexus/bin/nexus.rc

sudo sed -i 's/application-host=127.0.0.1/application-host=0.0.0.0/g' /opt/nexus/etc/nexus-default.properties
sudo sed -i 's/application-port=8081/application-port=9081/g' /opt/nexus/etc/nexus-default.properties

echo "nexus ALL=(ALL) NOPASSWD: ALL" | sudo tee -a /etc/sudoers

sudo tee /etc/systemd/system/nexus.service >/dev/null <<EOL
[Unit]
Description=Nexus Service
After=syslog.target network.target

[Service]
Type=forking
LimitNOFILE=65536
ExecStart=/opt/nexus/bin/nexus start
ExecStop=/opt/nexus/bin/nexus stop
User=nexus
Group=nexus
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOL

sudo systemctl daemon-reload
sudo systemctl enable nexus
sudo systemctl start nexus