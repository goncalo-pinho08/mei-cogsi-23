#!/bin/bash
# Update package repositories
sudo apt update
# Install dependencies
sudo apt install -y python3 python3-pip sshpass
# Upgrade pip
sudo pip3 install --upgrade pip
#Install lxml dependencies
sudo apt-get install -y libxml2 libxml2-dev libxslt1-dev zlib1g-dev python3-lxml
# Install Ansible using pip
sudo pip3 install ansible
# Install community.general collection
ansible-galaxy collection install community.general
#install lxml
pip3 install lxml
# Create the /etc/ansible directory if it doesn't exist
sudo mkdir -p /etc/ansible
# Create the ansible.cfg file
sudo bash -c 'echo -e "[defaults]\nhost_key_checking = False" > /etc/ansible/ansible.cfg'

