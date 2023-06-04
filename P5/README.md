
# P5 Integration - 1220257 Gonçalo Pinho

![Devops cycle](https://www.tibco.com/sites/tibco/files/media_entity/2021-04/Dev-ops-01.svg)

The objective of this assignment is to integrate various tools and technologies, including Ansible, Jenkins, and Maven/Nexus, for a DevOps scenario simulated using Vagrant.

The assignment also requires the implementation of monitoring using Nagios, JMX, and NRPE, incorporating both passive and active checking modes. An essential concept in this context is the utilization of infrastructure as code.

  

However, please note that certain components cannot be implemented due to limitations on the MAC M1 platform. For example, the GNS3 and the simulation of a router using Docker cannot be included. Additionally, static IP addresses and significant network settings cannot be modified through Vagrant on this platform.

  

Nonetheless, all other aspects of the project have been successfully implemented.

  
  

# Preparing infrastructure with vagrant

  
  

As in previous projects (P1 and P2), I utilized a Vagrantfile to create virtual machines. During the initialization of each machine, I incorporated shell script files to provision and install the desired dependencies.

  

There are four machines in total:

  

- "monitor": This machine includes Nagios and Nexus.

- "jenkins": This machine includes Ansible and Jenkins.

- "server_one": This machine includes Tomcat and Todd.

- "server_two": This machine also includes Tomcat and Todd.

  

Below, you will find the complete Vagrantfile, which encompasses the creation of these machines.

``` ruby

Vagrant.configure("2") do |config|

# monitor machine with nagios installed

config.vm.define "monitor"  do |monitor|

#define a status mac address to be used by the monitor machine

monitor.vm.base_mac = "080027E0E0E0"

monitor.vm.hostname = "monitor"

monitor.vm.box = "starboard/ubuntu-arm64-20.04.5"

monitor.vm.box_version = "20221120.20.40.0"

monitor.vm.box_download_insecure = true

monitor.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true  #Allowing to get information of the virtual machine SSH key

v.gui = true  #In Mac computer with Apple Silicon processors we need to have GUI enabled otherwise it doesn't work.

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"  #Hypervisor network driver that is optimized to provide high performance, high throughput, and minimal latency

end

monitor.vm.provision "shell", path: "scripts/monitor/install_nexus.sh"

monitor.vm.provision "shell", path: "scripts/monitor/install_send_email.sh"

monitor.vm.provision "shell", path: "scripts/monitor/install_nagios.sh"

monitor.vm.provision "shell", path: "scripts/monitor/install_nsca.sh"

# Redirect port http 80 to 8080 on host to access nagios web interface

monitor.vm.network "forwarded_port", guest: 80, host: 8080

#redirect the port 9081 to 9081 on host to access nexus web interface

monitor.vm.network "forwarded_port", guest: 9081, host: 9081

# share ansible folder with the host

#monitor.vm.synced_folder "shared/monitor/ansible", "/etc/ansible/"

# share nagios files with the host

#monitor.vm.synced_folder "shared/monitor/nagios", "/usr/local/nagios/etc/"

end

  

config.vm.define "jenkins"  do |jenkins|

jenkins.vm.base_mac = "080027E0E2E4"

jenkins.vm.hostname = "jenkins"

jenkins.vm.box = "starboard/ubuntu-arm64-20.04.5"

jenkins.vm.box_version = "20221120.20.40.0"

jenkins.vm.box_download_insecure = true

jenkins.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true  #Allowing to get information of the virtual machine SSH key

v.gui = true  #In Mac computer with Apple Silicon processors we need to have GUI enabled otherwise it doesn't work.

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"  #Hypervisor network driver that is optimized to provide high performance, high throughput, and minimal latency

end

  

jenkins.vm.provision "shell", path: "scripts/monitor/install_ansible.sh"

jenkins.vm.provision "shell", path: "scripts/monitor/install_jenkins.sh"

  

# Redirect port 8080 to 8080 on host to access jenkins web interface

jenkins.vm.network "forwarded_port", guest: 8080, host: 8081

end

  

config.vm.define "server_one"  do |server_one|

server_one.vm.base_mac = "080027E0E0E2"

server_one.vm.hostname = "server"

server_one.vm.box = "starboard/ubuntu-arm64-20.04.5"

server_one.vm.box_version = "20221120.20.40.0"

server_one.vm.box_download_insecure = true

server_one.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true  #Allowing to get information of the virtual machine SSH key

v.gui = true  #In Mac computer with Apple Silicon processors we need to have GUI enabled otherwise it doesn't work.

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"  #Hypervisor network driver that is optimized to provide high performance, high throughput, and minimal latency

end

  

server_one.vm.provision "shell", path: "scripts/server/install_nrpe.sh"

server_one.vm.provision "shell", path: "scripts/server/install_send_nsca.sh"

server_one.vm.provision "shell", path: "scripts/server/install_tomcat.sh"

end

  

config.vm.define "server_two"  do |server_two|

server_two.vm.base_mac = "080027E0E0E4"

server_two.vm.hostname = "server"

server_two.vm.box = "starboard/ubuntu-arm64-20.04.5"

server_two.vm.box_version = "20221120.20.40.0"

server_two.vm.box_download_insecure = true

server_two.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true  #Allowing to get information of the virtual machine SSH key

v.gui = true  #In Mac computer with Apple Silicon processors we need to have GUI enabled otherwise it doesn't work.

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"  #Hypervisor network driver that is optimized to provide high performance, high throughput, and minimal latency

end

server_two.vm.provision "shell", path: "scripts/server/install_nrpe.sh"

server_two.vm.provision "shell", path: "scripts/server/install_send_nsca.sh"

server_two.vm.provision "shell", path: "scripts/server/install_tomcat.sh"

end

end

```

  

All provisioned shell scripts are on the repository in execption of nexus, ansible and jenkins every other was already used on previous projects. I'll explain only the ones that added on this assigment.

  

Shell script to install ansible:

```bash

#!/bin/bash

# Update package repositories

sudo  apt  update

# Install dependencies

sudo  apt  install  -y  python3  python3-pip  sshpass

# Upgrade pip

sudo  pip3  install  --upgrade  pip

#Install lxml dependencies

sudo  apt-get  install  -y  libxml2  libxml2-dev  libxslt1-dev  zlib1g-dev  python3-lxml

# Install Ansible using pip

sudo  pip3  install  ansible

# Install community.general collection

ansible-galaxy  collection  install  community.general

pip3  install  lxml

# Create the /etc/ansible directory if it doesn't exist

sudo  mkdir  -p  /etc/ansible

# Create the ansible.cfg file

sudo  bash  -c  'echo -e "[defaults]\nhost_key_checking = False" > /etc/ansible/ansible.cfg'

```

  

The mentioned script installs Ansible and its dependencies, including the community.general library, which enables the use of the community-built module called "maven_artifact." Additionally, it installs the lxml library to facilitate artifact downloads from the repository. The script also generates the ansible.cfg file and disables host_key_checking, as SSH keys are not being utilized.

  

Shell script to install jenkins:

```bash

#!/bin/bash

# Update package lists

sudo  apt  update

# Install Java

sudo  apt  install  -y  default-jdk

# Add Jenkins repository key

wget  -q  -O  -  https://pkg.jenkins.io/debian/jenkins.io.key | sudo  apt-key  add  -

# Add Jenkins repository

echo  deb  https://pkg.jenkins.io/debian-stable  binary/ | sudo  tee  /etc/apt/sources.list.d/jenkins.list

# Import the missing public key

sudo  apt-key  adv  --keyserver  keyserver.ubuntu.com  --recv-keys  5BA31D57EF5975CA

# Update package lists again

sudo  apt  update

# Install Jenkins

sudo  apt  install  -y  jenkins

# Enable Jenkins service to start on boot

sudo  systemctl  enable  jenkins

# Start Jenkins service

sudo  systemctl  start  jenkins

```

  

This shell script updates package lists, installs Java and Jenkins, and configures Jenkins to start automatically on boot.

  

Shell script to install nexus:

```bash

cd  /opt/

sudo  wget  -O  latest-unix.tar.gz  https://download.sonatype.com/nexus/3/latest-unix.tar.gz

sudo  tar  -xvzf  /opt/latest-unix.tar.gz

sudo  mv  nexus-3*  nexus

sudo  mv  sonatype-work  nexusdata

sudo  useradd  --system  --no-create-home  nexus

sudo  chown  -R  nexus:nexus  /opt/nexus

sudo  chown  -R  nexus:nexus  /opt/nexusdata

  

sudo  sed  -i  's/sonatype-work/nexusdata/g'  /opt/nexus/bin/nexus.vmoptions

sudo  sed  -i  's/#run_as_user=""/run_as_user="nexus"/g'  /opt/nexus/bin/nexus.rc

sudo  sed  -i  's/application-host=127.0.0.1/application-host=0.0.0.0/g'  /opt/nexus/etc/nexus-default.properties

sudo  sed  -i  's/application-port=8081/application-port=9081/g'  /opt/nexus/etc/nexus-default.properties

echo  "nexus ALL=(ALL) NOPASSWD: ALL" | sudo  tee  -a  /etc/sudoers

sudo  tee  /etc/systemd/system/nexus.service >/dev/null <<EOL

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

  

sudo  systemctl  daemon-reload

sudo  systemctl  enable  nexus

sudo  systemctl  start  nexus

```

  

This shell script downloads and installs the Nexus repository manager in the /opt/ directory. It sets up the necessary configurations, creates a dedicated user, and starts the Nexus service.

  

# Preparing the repository in nexus

[Nexus](https://www.sonatype.com/products/sonatype-nexus-repository?_bt=652685393747&_bk=nexus%20repo&_bm=p&_bn=g&_bg=146861071568&utm_term=nexus%20repo&utm_campaign=INT%20%7C%20EMEASW%20%7C%20IMEMA%20T1%20%7C%20Brand&utm_source=google&utm_medium=cpc&hsa_tgt=kwd-1625672627969&hsa_grp=146861071568&hsa_src=g&hsa_net=adwords&hsa_mt=p&hsa_ver=3&hsa_ad=652685393747&hsa_acc=2665806879&hsa_kw=nexus%20repo&hsa_cam=18331480783&gclid=CjwKCAjwpayjBhAnEiwA-7ena2I1e3cqC5NQ_wDTEm763ZoJNFGihnGaTHPG-2d36GoOCp2zAoodaxoC-BkQAvD_BwE) is an artifact repository manager, commonly known as Nexus Repository, It's a powerful tool used in software development and deployment workflows to manage and store software artifacts.

  

An artifact repository is a centralized storage location where software artifacts are stored, managed, and distributed. Artifacts can include compiled binaries, libraries, packages, container images, configuration files, and other files related to software development.

  

Assuming you've used my shell script to install nexus and you have it up and running, we can access it via localhost:9081, the username is admin and the password you get it by `cat /opt/nexusdata/nexus3/admin.password` then you will be prompted to change the password to one of your own.

  

Then you will be logged in and you can go to the Settings(top left corner)>Repositories>Create repository, then choose maven2(hosted) type the name you want and to avoid problems use the Mixed version policy and scroll down and click on the Create repository button.

  

Now you have successfuly created a repository for your artifact.

  

# Preparing Jenkins

  

[Jenkins](https://www.jenkins.io) is an open-source automation server that helps automate various stages of the software development lifecycle. It is primarily used for continuous integration (CI) and continuous delivery (CD) processes. Jenkins allows developers to automate repetitive tasks, build, test, and deploy applications more efficiently.

  

Once you have Jenkins installed, navigate to localhost:8081. You'll be prompted to enter the admin password. To retrieve the password, execute the following command: `sudo cat /var/lib/jenkins/secrets/initialAdminPassword`. Copy the password displayed and proceed to install all recommended plugins.

  

Next, create an account and access "Manage Jenkins" on the left side. Navigate to "Credentials" > "System" > "Global credentials" and click on the "Add credentials" button. Provide your Git credentials, as they are necessary for running the pipeline. For simplicity, assign a meaningful ID to the credentials.

  

# Automating the proccess of deploying the todd application and enabling jmx on nagios

  
  

As per the requirements, we are required to deploy using Ansible. Ansible is a powerful open-source automation tool designed to simplify the management and configuration of IT infrastructure. It provides a consistent and repeatable approach to tasks such as server provisioning, application deployment, network configuration, and system management. While Ansible offers a range of base modules for automation, we can also leverage community-built modules to enhance our project. Playbooks, which are organized units of scripts, can be utilized to execute sets of tasks for server configuration management with Ansible.

  

To accommodate the use of Nexus artifact, we needed to install a module that enables downloading artifacts from the repository. We employed the Maven artifact module, which fulfills this requirement. To install the module, execute the following command: `ansible-galaxy collection install community.general`

  

Once installed, you will have access to the required module. Additionally, it is crucial to ensure that the servers intended for deploying the Todd application have the Python3-lxml package installed, as it is a dependency for that module. However, we will ensure that this dependency is installed through the playbook.

  

As our pipeline is based on the application being deployed, most of the work is centered around the Todd repository available at [my Todd repository](https://bitbucket.org/goncalo-pinho/todd-1220257/src/master/). From this point forward, I will explain the details of the process.

  

So the first thing i've made was adapt the gradle task so that I can provide de nexus address via argument, and the result was:

```yaml

uploadArchives {

def nexusAddress = project.hasProperty('nexusAddress') ? project.property('nexusAddress') : "172.16.244.142"

repositories {

mavenDeployer {

repository(url: "http://"+"${nexusAddress}:9081/repository/todd") {

authentication(userName: "admin", password: "admin")

}

snapshotRepository(url: "http://"+"${nexusAddress}:9081/repository/todd") {

authentication(userName: "admin", password: "admin")

}

} }}

```

  

The previous code implementation offers the flexibility to provide the Nexus address as an argumen. This is particularly useful because the machine addresses are constantly changing, and by utilizing this approach, I can avoid editing the build.gradle for each address change.

  

To facilitate the deployment process, I created an inventory file that includes the hosts and relevant variables. The contents of the inventory file are as follows:

  

```

# BEGIN ANSIBLE MANAGED BLOCK - DO NOT EDIT THIS BLOCK

[monitor]

monitor ansible_host=172.16.244.145

[servers]

server1 ansible_host=172.16.244.130

server2 ansible_host=172.16.244.142

# END ANSIBLE MANAGED BLOCK - DO NOT EDIT THIS BLOCK

#host for localhost

[servers:vars]

ansible_user=vagrant

ansible_password=vagrant

[monitor:vars]

ansible_user=vagrant

ansible_password=vagrant

```

I have two "groups" the monitor that is the nagios and nexus machine and i have the servers that are the machines that we want to deploy todd and enable jmx in tomcat9. The variables are the credentials to use sudo in the playbook.

In this inventory file, there are two groups: "monitor," which represents the Nagios and Nexus machine, and "servers," which includes the machines where we want to deploy Todd and enable JMX in Tomcat 9. The variables provided are the credentials used for executing sudo in the playbook.

 
Following the inventory setup, I created several necessary templates, namely:

- nrpe.cfg

- nsca.cfg

- toddService.sh

- host.cfg

  

Templates serve as files that can be modified before being copied to a remote machine. Let's take the example of the host.cfg template, which is used to monitor the machine where Todd is deployed. By utilizing the template, we can dynamically change the IP address and the machine's name based on the inventory and specific Ansible properties. The values within the template vary depending on the server we are deploying to.

  

The host template we mentioned earlier has the following structure:

```

# Define a host for the local machine

define host {

use linux-server ; Name of host template to use

host_name {{ inventory_hostname }} ; Name of the host to monitor

alias {{ inventory_hostname }} ; Alias of the host to monitor

address {{ ansible_default_ipv4.address }}

notifications_enabled 1

contact_groups admins

check_command check-host-alive

}

define service {

use local-service

host_name {{ inventory_hostname }}

service_description ToddSessionsPassive

check_command check_dummy!0 "Available Sessions OK"

passive_checks_enabled 1

active_checks_enabled 0

contact_groups admins

notifications_enabled 1

event_handler_enabled 1

event_handler grow_todd

}

define service {

use local-service

host_name {{ inventory_hostname }}

service_description TomcatPassive

check_command check_dummy!0 "Heap Memory Usage is Stable"

passive_checks_enabled 1

active_checks_enabled 0

contact_groups admins

notifications_enabled 1

event_handler_enabled 1

event_handler check_nrpe!restart_tomcat

}

define service{

use local-service ; Name of service template to use

host_name {{ inventory_hostname }}

service_description Todd-Server

check_command check_todd_server!{{ ansible_default_ipv4.address }}

contact_groups admins

notifications_enabled 1

event_handler_enabled 1

event_handler check_nrpe!restart_todd

}

define service{

use local-service ; Name of service template to use

host_name {{ inventory_hostname }}

service_description Todd-Number-Sessions

check_command check_todd_sessions!{{ ansible_default_ipv4.address }}

contact_groups admins

notifications_enabled 1

#event_handler check_nrpe!grow_todd

}

# Define a service to check HTTP on the remote server.

define service {

use local-service

host_name {{ inventory_hostname }}

service_description Check HTTP

check_command check_http! -p 8080

contact_groups admins

notifications_enabled 1

event_handler_enabled 1

event_handler check_nrpe!restart_tomcat

}

# Define a service to check if SSH is running on the host

define service {

use generic-service ; Name of service template to use

host_name {{ inventory_hostname }} ; Name of the host to monitor

service_description Check SSH ; A friendly name for the service

check_command check_ssh ; Name of the Nagios plugin to use

contact_groups admins

notifications_enabled 1

}

#check number of users

define service {

use local-service ; Name of service template to use

host_name {{ inventory_hostname }}

service_description Check Number Users

check_command check_nrpe!check_users

contact_groups admins

notifications_enabled 1

}

#check CPU load

define service{

use generic-service

host_name {{ inventory_hostname }}

service_description CPU Load

check_command check_nrpe!check_load

}

define service{

use generic-service

host_name {{ inventory_hostname }}

service_description Free Space

check_command check_nrpe!check_disk!/dev/nvme0n1p2

}

define service{

use generic-service

host_name {{ inventory_hostname }}

service_description Zombie Process

check_command check_nrpe!check_zombie_procs

}

define service{

use generic-service

host_name {{ inventory_hostname }}

service_description Total Processes

check_command check_nrpe!check_total_procs

}

```

  
  

In the host.cfg template, we utilized the variable `{{ ansible_default_ipv4.address }}`, which is replaced with the server's address, and `{{ inventory_hostname }}`, which is replaced with the server's name. This allows for dynamic configuration based on the specific server.

  

We followed the same approach in the remaining templates, as demonstrated in the nrpe.cfg template:

```

#############################################################################

#

# Sample NRPE Config File

#

# Notes:

#

# This is a sample configuration file for the NRPE daemon. It needs to be

# located on the remote host that is running the NRPE daemon, not the host

# from which the check_nrpe client is being executed.

#

#############################################################################

# LOG FACILITY

# The syslog facility that should be used for logging purposes.

log_facility=daemon

# LOG FILE

# If a log file is specified in this option, nrpe will write to

# that file instead of using syslog.

#log_file=/usr/local/nagios/var/nrpe.log

# DEBUGGING OPTION

# This option determines whether or not debugging messages are logged to the

# syslog facility.

# Values: 0=debugging off, 1=debugging on

debug=0

# PID FILE

# The name of the file in which the NRPE daemon should write it's process ID

# number. The file is only written if the NRPE daemon is started by the root

# user and is running in standalone mode.

pid_file=/usr/local/nagios/var/nrpe.pid

# PORT NUMBER

# Port number we should wait for connections on.

# NOTE: This must be a non-privileged port (i.e. > 1024).

# NOTE: This option is ignored if NRPE is running under either inetd or xinetd

server_port=5666

# SERVER ADDRESS

# Address that nrpe should bind to in case there are more than one interface

# and you do not want nrpe to bind on all interfaces.

# NOTE: This option is ignored if NRPE is running under either inetd or xinetd

#server_address=127.0.0.1

# LISTEN QUEUE SIZE

# Listen queue size (backlog) for serving incoming connections.

# You may want to increase this value under high load.

#listen_queue_size=5

# NRPE USER

# This determines the effective user that the NRPE daemon should run as.

# You can either supply a username or a UID.

#

# NOTE: This option is ignored if NRPE is running under either inetd or xinetd

nrpe_user=nagios

# NRPE GROUP

# This determines the effective group that the NRPE daemon should run as.

# You can either supply a group name or a GID.

#

# NOTE: This option is ignored if NRPE is running under either inetd or xinetd

nrpe_group=nagios

# ALLOWED HOST ADDRESSES

# This is an optional comma-delimited list of IP address or hostnames

# that are allowed to talk to the NRPE daemon. Network addresses with a bit mask

# (i.e. 192.168.1.0/24) are also supported. Hostname wildcards are not currently

# supported.

#

# Note: The daemon only does rudimentary checking of the client's IP

# address. I would highly recommend adding entries in your /etc/hosts.allow

# file to allow only the specified host to connect to the port

# you are running this daemon on.

#

# NOTE: This option is ignored if NRPE is running under either inetd or xinetd

allowed_hosts=127.0.0.1,{{ hostvars['monitor']['ansible_host'] }}

# COMMAND ARGUMENT PROCESSING

# This option determines whether or not the NRPE daemon will allow clients

# to specify arguments to commands that are executed. This option only works

# if the daemon was configured with the --enable-command-args configure script

# option.

#

# *** ENABLING THIS OPTION IS A SECURITY RISK! ***

# Read the SECURITY file for information on some of the security implications

# of enabling this variable.

#

# Values: 0=do not allow arguments, 1=allow command arguments

dont_blame_nrpe=1

# BASH COMMAND SUBSTITUTION

# This option determines whether or not the NRPE daemon will allow clients

# to specify arguments that contain bash command substitutions of the form

# $(...). This option only works if the daemon was configured with both

# the --enable-command-args and --enable-bash-command-substitution configure

# script options.

#

# *** ENABLING THIS OPTION IS A HIGH SECURITY RISK! ***

# Read the SECURITY file for information on some of the security implications

# of enabling this variable.

#

# Values: 0=do not allow bash command substitutions,

# 1=allow bash command substitutions

allow_bash_command_substitution=0

# COMMAND PREFIX

# This option allows you to prefix all commands with a user-defined string.

# A space is automatically added between the specified prefix string and the

# command line from the command definition.

#

# *** THIS EXAMPLE MAY POSE A POTENTIAL SECURITY RISK, SO USE WITH CAUTION! ***

# Usage scenario:

# Execute restricted commmands using sudo. For this to work, you need to add

# the nagios user to your /etc/sudoers. An example entry for allowing

# execution of the plugins from might be:

#

# nagios ALL=(ALL) NOPASSWD: /usr/lib/nagios/plugins/

#

# This lets the nagios user run all commands in that directory (and only them)

# without asking for a password. If you do this, make sure you don't give

# random users write access to that directory or its contents!

# command_prefix=/usr/bin/sudo

# MAX COMMANDS

# This specifies how many children processes may be spawned at any one

# time, essentially limiting the fork()s that occur.

# Default (0) is set to unlimited

# max_commands=0

# COMMAND TIMEOUT

# This specifies the maximum number of seconds that the NRPE daemon will

# allow plugins to finish executing before killing them off.

command_timeout=60

# CONNECTION TIMEOUT

# This specifies the maximum number of seconds that the NRPE daemon will

# wait for a connection to be established before exiting. This is sometimes

# seen where a network problem stops the SSL being established even though

# all network sessions are connected. This causes the nrpe daemons to

# accumulate, eating system resources. Do not set this too low.

connection_timeout=300

# WEAK RANDOM SEED OPTION

# This directive allows you to use SSL even if your system does not have

# a /dev/random or /dev/urandom (on purpose or because the necessary patches

# were not applied). The random number generator will be seeded from a file

# which is either a file pointed to by the environment valiable $RANDFILE

# or $HOME/.rnd. If neither exists, the pseudo random number generator will

# be initialized and a warning will be issued.

# Values: 0=only seed from /dev/[u]random, 1=also seed from weak randomness

#allow_weak_random_seed=1

# SSL/TLS OPTIONS

# These directives allow you to specify how to use SSL/TLS.

# SSL VERSION

# This can be any of: SSLv2 (only use SSLv2), SSLv2+ (use any version),

# SSLv3 (only use SSLv3), SSLv3+ (use SSLv3 or above), TLSv1 (only use

# TLSv1), TLSv1+ (use TLSv1 or above), TLSv1.1 (only use TLSv1.1),

# TLSv1.1+ (use TLSv1.1 or above), TLSv1.2 (only use TLSv1.2),

# TLSv1.2+ (use TLSv1.2 or above)

# If an "or above" version is used, the best will be negotiated. So if both

# ends are able to do TLSv1.2 and use specify SSLv2, you will get TLSv1.2.

# If you are using openssl 1.1.0 or above, the SSLv2 options are not available.

#ssl_version=SSLv2+

# SSL USE ADH

# This is for backward compatibility and is DEPRECATED. Set to 1 to enable

# ADH or 2 to require ADH. 1 is currently the default but will be changed

# in a later version.

#ssl_use_adh=1

# SSL CIPHER LIST

# This lists which ciphers can be used. For backward compatibility, this

# defaults to 'ssl_cipher_list=ALL:!MD5:@STRENGTH' for < OpenSSL 1.1.0,

# and 'ssl_cipher_list=ALL:!MD5:@STRENGTH:@SECLEVEL=0' for OpenSSL 1.1.0 and

# greater.

#ssl_cipher_list=ALL:!MD5:@STRENGTH

#ssl_cipher_list=ALL:!MD5:@STRENGTH:@SECLEVEL=0

#ssl_cipher_list=ALL:!aNULL:!eNULL:!SSLv2:!LOW:!EXP:!RC4:!MD5:@STRENGTH

# SSL Certificate and Private Key Files

#ssl_cacert_file=/etc/ssl/servercerts/ca-cert.pem

#ssl_cert_file=/etc/ssl/servercerts/nagios-cert.pem

#ssl_privatekey_file=/etc/ssl/servercerts/nagios-key.pem

# SSL USE CLIENT CERTS

# This options determines client certificate usage.

# Values: 0 = Don't ask for or require client certificates (default)

# 1 = Ask for client certificates

# 2 = Require client certificates

#ssl_client_certs=0

# SSL LOGGING

# This option determines which SSL messages are send to syslog. OR values

# together to specify multiple options.

# Values: 0x00 (0) = No additional logging (default)

# 0x01 (1) = Log startup SSL/TLS parameters

# 0x02 (2) = Log remote IP address

# 0x04 (4) = Log SSL/TLS version of connections

# 0x08 (8) = Log which cipher is being used for the connection

# 0x10 (16) = Log if client has a certificate

# 0x20 (32) = Log details of client's certificate if it has one

# -1 or 0xff or 0x2f = All of the above

#ssl_logging=0x00

# NASTY METACHARACTERS

# This option allows you to override the list of characters that cannot

# be passed to the NRPE daemon.

# nasty_metachars=|`&><'\\[]{};\r\n

# This option allows you to enable or disable logging error messages to the syslog facilities.

# If this option is not set, the error messages will be logged.

disable_syslog=0

# COMMAND DEFINITIONS

# Command definitions that this daemon will run. Definitions

# are in the following format:

#

# command[<command_name>]=<command_line>

#

# When the daemon receives a request to return the results of <command_name>

# it will execute the command specified by the <command_line> argument.

#

# Unlike Nagios, the command line cannot contain macros - it must be

# typed exactly as it should be executed.

#

# Note: Any plugins that are used in the command lines must reside

# on the machine that this daemon is running on! The examples below

# assume that you have plugins installed in a /usr/local/nagios/libexec

# directory. Also note that you will have to modify the definitions below

# to match the argument format the plugins expect. Remember, these are

# examples only!

# The following examples use hardcoded command arguments...

# This is by far the most secure method of using NRPE

command[check_users]=/usr/local/nagios/libexec/check_users -w 5 -c 10

command[check_load]=/usr/local/nagios/libexec/check_load -r -w .15,.10,.05 -c .30,.25,.20

command[check_disk]=/usr/local/nagios/libexec/check_disk -w 20% -c 10% -p /dev/nvme0n1p2

command[check_zombie_procs]=/usr/local/nagios/libexec/check_procs -w 5 -c 10 -s Z

command[check_total_procs]=/usr/local/nagios/libexec/check_procs -w 220 -c 300

command[restart_tomcat]=/usr/local/nagios/etc/scripts/restart_tomcat.sh

command[restart_todd]=/usr/local/nagios/etc/scripts/restart_todd.sh

command[grow_todd]=java -cp /usr/local/todd/todd.jar net.jnjmx.todd.JMXToddServerGrow {{ ansible_default_ipv4.address }}

# The following examples allow user-supplied arguments and can

# only be used if the NRPE daemon was compiled with support for

# command arguments *AND* the dont_blame_nrpe directive in this

# config file is set to '1'. This poses a potential security risk, so

# make sure you read the SECURITY file before doing this.

### MISC SYSTEM METRICS ###

#command[check_users]=/usr/local/nagios/libexec/check_users $ARG1$

#command[check_load]=/usr/local/nagios/libexec/check_load $ARG1$

#command[check_disk]=/usr/local/nagios/libexec/check_disk $ARG1$

#command[check_swap]=/usr/local/nagios/libexec/check_swap $ARG1$

#command[check_cpu_stats]=/usr/local/nagios/libexec/check_cpu_stats.sh $ARG1$

#command[check_mem]=/usr/local/nagios/libexec/custom_check_mem -n $ARG1$

### GENERIC SERVICES ###

#command[check_init_service]=sudo /usr/local/nagios/libexec/check_init_service $ARG1$

#command[check_services]=/usr/local/nagios/libexec/check_services -p $ARG1$

### SYSTEM UPDATES ###

#command[check_yum]=/usr/local/nagios/libexec/check_yum

#command[check_apt]=/usr/local/nagios/libexec/check_apt

### PROCESSES ###

#command[check_all_procs]=/usr/local/nagios/libexec/custom_check_procs

#command[check_procs]=/usr/local/nagios/libexec/check_procs $ARG1$

### OPEN FILES ###

#command[check_open_files]=/usr/local/nagios/libexec/check_open_files.pl $ARG1$

### NETWORK CONNECTIONS ###

#command[check_netstat]=/usr/local/nagios/libexec/check_netstat.pl -p $ARG1$ $ARG2$

### ASTERISK ###

#command[check_asterisk]=/usr/local/nagios/libexec/check_asterisk.pl $ARG1$

#command[check_sip]=/usr/local/nagios/libexec/check_sip $ARG1$

#command[check_asterisk_sip_peers]=sudo /usr/local/nagios/libexec/check_asterisk_sip_peers.sh $ARG1$

#command[check_asterisk_version]=/usr/local/nagios/libexec/nagisk.pl -c version

#command[check_asterisk_peers]=/usr/local/nagios/libexec/nagisk.pl -c peers

#command[check_asterisk_channels]=/usr/local/nagios/libexec/nagisk.pl -c channels

#command[check_asterisk_zaptel]=/usr/local/nagios/libexec/nagisk.pl -c zaptel

#command[check_asterisk_span]=/usr/local/nagios/libexec/nagisk.pl -c span -s 1

# INCLUDE CONFIG FILE

# This directive allows you to include definitions from an external config file.

#include=<somefile.cfg>

# INCLUDE CONFIG DIRECTORY

# This directive allows you to include definitions from config files (with a

# .cfg extension) in one or more directories (with recursion).

#include_dir=<somedirectory>

#include_dir=<someotherdirectory>

# KEEP ENVIRONMENT VARIABLES

# This directive allows you to retain specific variables from the environment

# when starting the NRPE daemon.

#keep_environment_vars=NRPE_MULTILINESUPPORT,NRPE_PROGRAMVERSION

```

  

nsca.cgf template:

```

####################################################

# Sample NSCA Daemon Config File

# Written by: Ethan Galstad (nagios@nagios.org)

#

# Last Modified: 11-23-2007

####################################################

# LOG FACILITY

# The syslog facility that should be used for logging purposes.

log_facility=daemon

# Check Result directory. If passed, skip command pipe and submit

# directly into the checkresult directory. Requires Nagios 3+

# For best results, mount dir on ramdisk.

#check_result_path=/usr/local/nagios/var/checkresults

# PID FILE

# The name of the file in which the NSCA daemon should write it's process ID

# number. The file is only written if the NSCA daemon is started by the root

# user as a single- or multi-process daemon.

pid_file=/var/run/nsca.pid

# PORT NUMBER

# Port number we should wait for connections on.

# This must be a non-privileged port (i.e. > 1024).

server_port=5667

# SERVER ADDRESS

# Address that NSCA has to bind to in case there are

# more as one interface and we do not want NSCA to bind

# (thus listen) on all interfaces.

#server_address={{ ansible_default_ipv4.address }}

# NSCA USER

# This determines the effective user that the NSCA daemon should run as.

# You can either supply a username or a UID.

#

# NOTE: This option is ignored if NSCA is running under either inetd or xinetd

nsca_user=nagios

# NSCA GROUP

# This determines the effective group that the NSCA daemon should run as.

# You can either supply a group name or a GID.

#

# NOTE: This option is ignored if NSCA is running under either inetd or xinetd

nsca_group=nagios

# NSCA CHROOT

# If specified, determines a directory into which the nsca daemon

# will perform a chroot(2) operation before dropping its privileges.

# for the security conscious this can add a layer of protection in

# the event that the nagios daemon is compromised.

#

# NOTE: if you specify this option, the command file will be opened

# relative to this directory.

#nsca_chroot=/var/run/nagios/rw

# DEBUGGING OPTION

# This option determines whether or not debugging

# messages are logged to the syslog facility.

# Values: 0 = debugging off, 1 = debugging on

debug=0

# COMMAND FILE

# This is the location of the Nagios command file that the daemon

# should write all service check results that it receives.

command_file=/usr/local/nagios/var/rw/nagios.cmd

# ALTERNATE DUMP FILE

# This is used to specify an alternate file the daemon should

# write service check results to in the event the command file

# does not exist. It is important to note that the command file

# is implemented as a named pipe and only exists when Nagios is

# running. You may want to modify the startup script for Nagios

# to dump the contents of this file into the command file after

# it starts Nagios. Or you may simply choose to ignore any

# check results received while Nagios was not running...

alternate_dump_file=/usr/local/nagios/var/rw/nsca.dump

# AGGREGATED WRITES OPTION

# This option determines whether or not the nsca daemon will

# aggregate writes to the external command file for client

# connections that contain multiple check results. If you

# are queueing service check results on remote hosts and

# sending them to the nsca daemon in bulk, you will probably

# want to enable bulk writes, as this will be a bit more

# efficient.

# Values: 0 = do not aggregate writes, 1 = aggregate writes

aggregate_writes=0

# APPEND TO FILE OPTION

# This option determines whether or not the nsca daemon will

# will open the external command file for writing or appending.

# This option should almost *always* be set to 0!

# Values: 0 = open file for writing, 1 = open file for appending

append_to_file=0

# MAX PACKET AGE OPTION

# This option is used by the nsca daemon to determine when client

# data is too old to be valid. Keeping this value as small as

# possible is recommended, as it helps prevent the possibility of

# "replay" attacks. This value needs to be at least as long as

# the time it takes your clients to send their data to the server.

# Values are in seconds. The max packet age cannot exceed 15

# minutes (900 seconds). If this variable is set to zero (0), no

# packets will be rejected based on their age.

max_packet_age=30

# DECRYPTION PASSWORD

# This is the password/passphrase that should be used to decrypt the

# incoming packets. Note that all clients must encrypt the packets

# they send using the same password!

# IMPORTANT: You don't want all the users on this system to be able

# to read the password you specify here, so make sure to set

# restrictive permissions on this config file!

#password=

# DECRYPTION METHOD

# This option determines the method by which the nsca daemon will

# decrypt the packets it receives from the clients. The decryption

# method you choose will be a balance between security and performance,

# as strong encryption methods consume more processor resources.

# You should evaluate your security needs when choosing a decryption

# method.

#

# Note: The decryption method you specify here must match the

# encryption method the nsca clients use (as specified in

# the send_nsca.cfg file)!!

# Values:

#

# 0 = None (Do NOT use this option)

# 1 = Simple XOR (No security, just obfuscation, but very fast)

#

# 2 = DES

# 3 = 3DES (Triple DES)

# 4 = CAST-128

# 5 = CAST-256

# 6 = xTEA

# 7 = 3WAY

# 8 = BLOWFISH

# 9 = TWOFISH

# 10 = LOKI97

# 11 = RC2

# 12 = ARCFOUR

#

# 14 = RIJNDAEL-128

# 15 = RIJNDAEL-192

# 16 = RIJNDAEL-256

#

# 19 = WAKE

# 20 = SERPENT

#

# 22 = ENIGMA (Unix crypt)

# 23 = GOST

# 24 = SAFER64

# 25 = SAFER128

# 26 = SAFER+

#

decryption_method=1

```

  

toddService.sh template:

```

#!/bin/sh

SERVICE_NAME=ToddService

PATH_TO_JAR=/usr/local/todd/todd.jar

PID_PATH_NAME=/tmp/ToddService-pid

case $1 in

start)

echo "Starting $SERVICE_NAME ..." if [ ! -f $PID_PATH_NAME ]; then

nohup java -cp $PATH_TO_JAR -Dcom.sun.management.jmxremote.port=10500 -Dcom.sun.management.jmxremote.rmi.port=10500 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname={{ ansible_default_ipv4.address }} -Dcom.sun.management.jmxremote.host=0.0.0.0 net.jnjmx.todd.Server 2>> /dev/null >> /dev/null &

echo $! > $PID_PATH_NAME

echo "$SERVICE_NAME started ..." else

echo "$SERVICE_NAME is already running ..." fi

;; stop)

if [ -f $PID_PATH_NAME ]; then

PID=$(cat $PID_PATH_NAME);

echo "$SERVICE_NAME stoping ..." kill $PID;

echo "$SERVICE_NAME stopped ..." rm $PID_PATH_NAME

else

echo "$SERVICE_NAME is not running ..." fi

;; restart)

if [ -f $PID_PATH_NAME ]; then

PID=$(cat $PID_PATH_NAME);

echo "$SERVICE_NAME stopping ...";

kill $PID;

echo "$SERVICE_NAME stopped ...";

rm $PID_PATH_NAME

echo "$SERVICE_NAME starting ..." nohup -cp $PATH_TO_JAR -Dcom.sun.management.jmxremote.port=10500 -Dcom.sun.management.jmxremote.rmi.port=10500 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname={{ ansible_default_ipv4.address }} -Dcom.sun.management.jmxremote.host=0.0.0.0 net.jnjmx.todd.Server 2>> /dev/null >> /dev/null &

echo $! > $PID_PATH_NAME

echo "$SERVICE_NAME started ..." else

echo "$SERVICE_NAME is not running ..." fi

;;esac

```

  

After completing all the necessary preparations, we began the construction of the Ansible playbook.

  

I have all of this on the same playbook but i will break this in three parts so its easier to explain.

  

First part of the playbook:

```yaml

- name: Tomcat tasks

hosts: servers

become: true

tasks:

- name: Ensure that java11 is installed

become: true

apt:

name: openjdk-11-jdk

state: present

- name: Ensure that tomcat9 is installed

become: true

apt:

name: tomcat9

state: present

- name: Check if setenv.sh file exists

stat:

path: /usr/share/tomcat9/bin/setenv.sh

register: setenv_file

- name: Create setenv.sh file

vars:

jmx_port: 6000

tomcat_address: "{{ ansible_default_ipv4.address }}"

become: true

copy:

dest: /usr/share/tomcat9/bin/setenv.sh

content: |

#!/bin/bash

CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port={{ jmx_port }} -Dcom.sun.management.jmxremote.rmi.port={{ jmx_port }} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname={{ tomcat_address }}"

mode: '0755'

register: create_setenv_file

- name: Restarting tomcat9 to apply changes

become: true

shell: systemctl restart tomcat9

when: create_setenv_file.changed

```

  

In this part of the playbook, the focus is on enabling JMX for Tomcat 9. The tasks are executed on the target servers specified by the "servers" group. Here's a breakdown of the tasks:

  

1. The playbook ensures that Java 11 (openjdk-11-jdk) and Tomcat 9 (tomcat9) are installed on the server using the `apt` module.

2. It checks if the `setenv.sh` file exists on the server by using the `stat` module and registers the result in the variable `setenv_file`.

3. The playbook creates the `setenv.sh` file using the `copy` module. Inside the file, the JMX-related configuration is added, including the JMX port (`jmx_port`) and the server's IP address (`tomcat_address`). The `mode` option sets the file permissions to '0755'. The result is registered in the variable `create_setenv_file`.

4. If the `setenv.sh` file was changed or created, the playbook restarts Tomcat 9 using the `systemctl restart tomcat9` command.

  

This part of the playbook ensures that JMX is enabled for Tomcat 9 by installing the necessary dependencies, creating the `setenv.sh` file with the appropriate configurations, and restarting Tomcat 9 to apply the changes.

  

The second part is about preparing and deploying the todd application, and creating the monitoring host files on nagios objects on the monitor server. For that we have the second part of the playbook:

```yaml

- name: todd

hosts: servers

become: true

vars:

ansible_python_interpreter: /usr/bin/python3

tasks:

- name: Ensure that lxml is installed

apt:

name: python3-lxml

state: present

- name: Ensure that arp-scan is installed

apt:

name: arp-scan

state: present

- name: Get monitor address

shell: arp-scan --interface=eth0 --localnet | awk '/08:00:27:e0:e0:e0/ {print $1}'

register: monitor_address

- name: Create folder

file:

path: /usr/local/todd

state: directory

- name: Create folder

file:

path: /usr/local/todd/scripts

state: directory

- name: Transfer ToddService.sh and update file

template:

src: ../boilerplates/ToddService_template.j2

dest: /usr/local/todd/scripts/ToddService.sh

- name: Copy ToddService.service to systemd directory

template:

src: ../../scripts/ToddService.service

dest: /etc/systemd/system/ToddService.service

- name: Set permissions for ToddService.service

file:

path: /etc/systemd/system/ToddService.service

mode: "0755"

- name: Set permissions for ToddService.sh

file:

path: /usr/local/todd/scripts/ToddService.sh

mode: "0755"

- name: Copy shell script to restart tomcat

template:

src: ../../scripts/restart_tomcat.sh

dest: /usr/local/nagios/libexec/restart_tomcat.sh

mode: 'u+x,g+x,o+x'

- name: Copy shell script to restart todd

template:

src: ../../scripts/restart_todd.sh

dest: /usr/local/nagios/libexec/restart_todd.sh

mode: 'u+x,g+x,o+x'

- name: Replace nrpe.cfg for the one with the needed scripts

template:

src: ../boilerplates/nrpe_template.j2

dest: /usr/local/nagios/etc/nrpe.cfg

- name: Download an artifact from a private repository requiring authentication

community.general.maven_artifact:

group_id: pt.isep.cogsi

artifact_id: cogsi

repository_url: 'http://{{ monitor_address.stdout }}:9081/repository/todd/'

username: admin

password: admin

dest: /usr/local/todd/todd.jar

- name: Change permissions for todd.jar

command: chmod 777 /usr/local/todd/todd.jar

- name: Reload daemon

shell: systemctl daemon-reload

- name: Start ToddService service

service:

name: ToddService

state: started

- name: Enable ToddService service

service:

name: ToddService

enabled: true

- name: Restart nrpe service

service:

name: nrpe

state: restarted

- name: Create a host file to monitor the server

- name: Restart ToddService service

service:

name: ToddService

state: restarted

template:

src: ../boilerplates/hostmonitor_template.j2 # Replace with the path to your template file

dest: /usr/local/nagios/etc/objects/{{ inventory_hostname }}.cfg # Replace with the destination path on the control node

delegate_to: monitor

- name: Add line to the end of the file

lineinfile:

path: /usr/local/nagios/etc/nagios.cfg

line: "cfg_file=/usr/local/nagios/etc/objects/{{ inventory_hostname }}.cfg"

insertafter: EOF

delegate_to: monitor

- name: Copy nagios commands

template:

src: ../files/commands.cfg

dest: /usr/local/nagios/etc/objects/commands.cfg

delegate_to: monitor

```

  

The previous ansible playbook snipped does the following:

  

1. Ensure Dependencies:

- Check if the required package "lxml" is installed to download the artifact from the Nexus repository.

- Check if the package "arp-scan" is installed for retrieving the monitor address.

2. Directory Setup:

- Create a directory "/usr/local/todd" to deploy the "todd" application.

- Create a nested directory "/usr/local/todd/scripts" for storing scripts.

3. Script Deployment:

- Transfer the "ToddService.sh" script template to the server and update it with necessary details.

- Copy the "ToddService.service" script to the appropriate systemd directory.

- Set permissions (755) for the "ToddService.service" file.

- Set permissions (755) for the "ToddService.sh" script.

4. Additional Shell Scripts:

- Copy shell scripts for restarting Tomcat and Todd to the Nagios libexec directory.

- Set executable permissions (u+x,g+x,o+x) for the shell scripts.

5. Configuration File Update:

- Replace the existing "nrpe.cfg" file with a template version containing the required scripts.

6. Artifact Download:

- Download an artifact from the Nexus repository using authentication.

- Save the artifact as "todd.jar" in the designated location.

- Set permissions (777) to enable execution of the "todd.jar" file.

7. Service and System Configuration:

- Reload the systemd daemon to apply the changes.

- Start the "ToddService" service.

- Enable auto-start of the "ToddService" service.

- Restart the "nrpe" service.

8. Monitoring Configuration:

- Create a host configuration file using the template and include server-specific information.

- Copy the host configuration file to the appropriate location on the monitor machine.

- Add a line to the "nagios.cfg" file to include the newly created host configuration file.

  

And the last part of the playbook it more directed to the monitor that will also ensure that jdk 11 is installed and then deploy the todd artifact that will allow nagios to monitor the application in various servers. After the deploy the permissions of the todd.jar were changed to allow execution by everyone and then re replace the nsca.cfg with a template and finally restart nagios service to apply changes.

  

```yaml

- name: Changes on monitor

hosts: monitor

become: true

tasks:

- name: Ensure that java11 is installed

apt:

name: python3-lxml

state: present

- name: Download an artifact from a private repository requiring authentication

community.general.maven_artifact:

group_id: pt.isep.cogsi

artifact_id: cogsi

repository_url: 'http://{{ ansible_default_ipv4.address }}:9081/repository/todd'

username: admin

password: admin

dest: /usr/local/nagios/libexec/todd.jar

- name: Change permissions for todd.jar

command: chmod 777 /usr/local/nagios/libexec/todd.jar

- name: Replace nsca.cfg

template:

src: ../boilerplates/nsca_template.j2

dest: /usr/local/nagios/etc/nsca.cfg

- name: Restart nagios service

service:

name: nagios

state: restarted

```

  

Now to make all of this work we need to build a Jenkinsfile, that is the file that the pipeline will search on the repository that has the various stages of the pipeline.

In my case I have the following Jenkinsfile:

```yaml

pipeline {

agent any

stages {

stage('Checkout') {

steps {

echo 'Checking out...'

git credentialsId: 'git-credentials', url: 'https://goncalo-pinho@bitbucket.org/goncalo-pinho/todd-1220257.git'

}

}

stage('Build') {

steps {

echo 'Building...'

sh './gradlew clean build'

}

}

stage('Jenkins Archiving') {

steps {

echo 'Jenkins Archiving...'

archiveArtifacts 'build/libs/*'

}

}

stage('Nexus Archiving') {

steps {

echo 'Nexus Archiving...'

sh "./gradlew uploadArchives -PnexusAddress='${env.NEXUS_ADDRESS}'"

}

}

stage('User Confirmation') {

steps {

input(message: 'Do you want to deploy this version?', ok: 'Proceed')

}

}

stage('Deploy') {

steps {

echo 'Deploying...'

sh 'ansible-playbook -i ansible/hosts ansible/playbooks/deployplaybook.yml'

}

}

}

}

```

  

In the Jenkinsfile, we have the following stages defined:

  

1. Checkout:

- This stage downloads the contents of a specified repository.

2. Build:

- This stage builds the application and generates the necessary artifacts.

3. Jenkins Archiving:

- In this stage, we archive the generated artifact on Jenkins for future reference.

4. Nexus Archiving:

- This stage involves using the Gradle task explained earlier to archive the artifact on Nexus, the repository manager.

5. User Confirmation:

- As per the assignment requirements, this stage allows the user to manually decide whether to proceed with the deployment in this part of the pipeline. They have the option to choose between discarding the deployment or continuing with it.

6. Deploy:

- If the user chooses to proceed with the deployment during the manual confirmation stage, the previously explained Ansible playbook will be executed. This playbook handles all the necessary steps we discussed earlier, including the installation of dependencies, configuration changes, artifact download, and service restarts.

  

These stages provide a clear progression of the pipeline.

  

To set up the Jenkins pipeline, follow these steps:

  

1. Access the Jenkins webpage (with my configs localhost:8081).

2. Click on the "New Item" button.

3. Choose the pipeline option and provide a name for it.

4. Scroll down to the pipeline definition section.

5. You have two options:

- For debugging and testing purposes in the initial stages, manually copy and paste the pipeline code into the input. This is recommended.

- For automation, select the pipeline script from the source code management (SCM) by configuring the necessary credentials mentioned earlier and providing the repository URL (HTTPS).

  

Important things if you are running this project:

- (MAC M1/M2 users) Since you can't define static address you need to manually change the address on the pipeline and on the hosts file.

- All my shell scripts that I use on vagrant are ready for arm architecture so if you use this on non arm architectures you will get errors.


# Alternative

In this part of the assignment, we are suposed to explore alternative approaches for implementing continuous deployment and configuration management topics within the exercise. We can consider various options, such as utilizing different methods in the pipeline, employing alternative artifact repositories, leveraging containers to package the application, or exploring alternative tools instead of Ansible.

Instead of implementing, on the alternative we are going to suggest different tools to replace the ones that we've used.

Lets start with the Jenkins alternative, such as [GitLab CI](https://docs.gitlab.com/ee/ci/) is a web-based DevOps platform that automates software development stages, including continuous integration and continuous deployment. It uses a configuration file, `.gitlab-ci.yml`, to define a pipeline with different stages and jobs. Each job represents a specific task, such as building, testing, or deploying code. The pipeline is triggered automatically or manually, running jobs in the specified order, either in parallel or sequentially.

Now lets see some advantages of using each one:

**Pros of Jenkins:**

-   **Free, Open Source, and Easy Installation:** Jenkins is available as a free and open-source tool, making it accessible to a wide range of users. It is also relatively easy to install and set up, allowing for quick adoption.
-   **Abundance of Plugins:** Jenkins has a vast ecosystem of plugins, offering extensive customization and integration options. These plugins enable users to extend Jenkins' functionality and tailor it to their specific needs.
-   **Supportive Community:** Jenkins has a large and active community of users and developers who contribute to its development and provide support. This community-driven approach ensures ongoing improvements, frequent updates, and assistance when encountering issues.

**Pros of GitLab CI:**

-   **High Availability Deployments:** GitLab CI/CD is built directly into the GitLab platform, making installation and configuration seamless. It is widely used for automating deployments, enabling organizations to streamline the process and achieve high availability.
-   **Milestone Setting:** GitLab provides milestone setting capabilities, allowing users to track and manage issues, improvements, and requests in a repository. Milestones can be assigned to specific issues or combined with requests within a project or even across multiple projects within a group.
-   **Issue Tracking & Issue Shuffling:** GitLab CI/CD excels in issue tracking and management, making it a preferred choice for many open-source projects. It enables efficient testing of pull requests and branches simultaneously, with testing outcomes conveniently displayed on the user interface. GitLab's user-friendly interface simplifies monitoring and enhances usability compared to Jenkins.

It's also worth noting that GilabCI is free until a certain point, if you want to implement this in a larger scale you need to pay.
 
 A alternative for Ansible is Chef. [Chef](https://www.chef.io) is an open-source configuration management and automation tool used for managing and deploying infrastructure and applications. It provides a framework for defining infrastructure as code, allowing developers and system administrators to automate the configuration, deployment, and management of systems at scale.
 
 While they share similarities, such as automating infrastructure configuration, there are significant differences between them:

1.  **Architecture:** Chef uses a master-client architecture with a central server and client agents running on each node. Ansible, on the other hand, operates with a server-only architecture, relying on SSH connections to configure client systems without requiring agents.
    
2.  **Configuration Files:** Chef uses "cookbooks" to define configurations, while Ansible uses "playbooks." Playbooks in Ansible are easier to create and understand but may have limitations in handling complex configuration tasks.
    
3.  **Source of Truth:** Ansible considers the deployed playbooks as the source of truth, making it suitable for source control systems. Chef relies on its server as the source of truth and requires consistent and identical uploaded cookbooks.
    
4.  **Management:** Chef employs a client-pull model, where clients fetch configurations from the server, using Ruby DSL. Ansible uses a server-push model, pushing configurations to nodes using YAML, a more administrator-friendly language.
    

In summary, Ansible offers simpler setup, easy-to-manage YAML-based configurations, and a server-only architecture without requiring client agents. Chef, on the other hand, provides a more developer-oriented Ruby DSL language and a master-client architecture. Ultimately, the choice between Ansible and Chef depends on the specific requirements and preferences of the project or organization.


# Sources

 - https://www.fosstechnix.com/how-to-install-nexus-repository-on-ubuntu/
 - https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html
 - https://www.jenkins.io/doc/book/installing/linux/
 - https://docs.ansible.com/ansible/latest/playbook_guide/playbooks_intro.html
 - https://www.lambdatest.com/blog/jenkins-vs-gitlab-ci-battle-of-ci-cd-tools/
 - https://www.simplilearn.com/ansible-vs-chef-differences-article
