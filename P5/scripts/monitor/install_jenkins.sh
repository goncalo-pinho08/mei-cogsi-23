#!/bin/bash
# Update package lists
sudo apt update
# Install Java
sudo apt install -y default-jdk arp-scan
# Add Jenkins repository key
wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | sudo apt-key add -
# Add Jenkins repository
echo deb https://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list
# Import the missing public key
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 5BA31D57EF5975CA
# Update package lists again
sudo apt update
# Install Jenkins
sudo apt install -y jenkins
# Enable Jenkins service to start on boot
sudo systemctl enable jenkins
# Start Jenkins service
sudo systemctl start jenkins
