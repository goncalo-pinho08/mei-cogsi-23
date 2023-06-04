
# COGSI P1 - Gonçalo Pinho - 1220257

  
![Nagios](https://www.zend.com/sites/default/files/image/2019-09/logo-nagios.jpg)
The first part of the project (P1) consists in Monitoring Networks and Systems, and to achieve that was recommended the use of [Nagios](https://www.nagios.org). Nagios is an open-source software application used for monitoring infrastructure components. Nagios allows users to monitor various aspects of their systems, including network services, host resources such as CPU usage, memory utilization and disk space. It can also monitor network devices like routers, switches, and firewalls, as well as application protocols and services.

This project is being executed on a Macbook Air M1(Apple Silicon Processor) 256GB of storage and 16GB of RAM, so It might differ in some aspects.




## Vagrant and Hypervisor

**Vagrant installation**

  

[Vagrant](https://www.vagrantup.com) is a open-source software for building and maintaining portable and virtual software development environments.

Before installing Vagrant in MacOS we need to have [Homebrew](https://brew.sh) installed.

Assuming that homebrew is already installed, we just need to execute the following commands to install Vagrant:

```

brew install vagrant #Installs vagrant

brew install vagrant-manager # Installs a manager that allows you to manage the VM directly from the menu bar.

```

  

To check if vagrant is installed correctly just try ```vagrant -v``` and it should print the vagrant version, that in my case is **2.3.4**.

  

For the hypervisor we could have used the following:

  

-  **VirtualBox** - It works perfectly in windows and Intel based Macs but the ARM version of it doesn't work well with vagrant.

-  **Parallels** - The community says that it works well with vagrant in Apple Silicon based Macs but it's payed. (Haven't tried myself)

-  **VMWare Fusion** - Works well with vagrant and it works in Intel based Macs and in Apple Silicon Macs and it has a free version! (This is what I am using)

  

There must be other options but these three where the ones that I've searched about.

  

**VMWare Installation**

  

1. Access [VMWare Fusion 13](https://customerconnect.vmware.com/evalcenter?p=fusion-player-personal-13).

2. Create your account.

3. Download the .dmg file.

4. Install it and insert your Key.

  

After having VMWare Installation you need some plugins for vagrant to make it work with the picked hypervisor, you can do it by executing the following commands:

```

brew install --cask vagrant-vmware-utility # instala o utilitário do vmware

brew install --cask vagrant-vmware-desktop # instala lib vmware para desktop

```

  

To try if it's working go to a directory of your own and create a Vagrantfile and use the following code snippet:

```ruby

Vagrant.configure("2") do |config|

config.vm.box = "starboard/ubuntu-arm64-20.04.5"

config.vm.box_version = "20221120.20.40.0"

config.vm.box_download_insecure = true

config.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true

v.gui = true

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"

end

end

```

**Note:** in this case we are using a ubunto box but if you want another type of box you need to find it in [Vagrantup](https://app.vagrantup.com/boxes/search) that is the website that has all the vagrant boxes just make sure that the box is built for VMWare and the OS inside is the ARM version.

  

In the Vagrantfile directory run ```vagrant up``` to run the virtual machine that you have created.

After the machine is running run ```vagrant ssh``` to establish a SSH connection with the machine and give you access to the shell.

To leave the machine, just type ```exit``` in the terminal and then ```vagrant halt``` to shutdown the machine.

  

Installation Source:

  

- [Youtube Video](https://www.youtube.com/watch?v=KvuXMMVkY1I)

- Moodle topic created by the student Vicente Oliveira - 1220286

  

# Creating the monitor machine

  

Having everything ready we need to create a Vagrantfile with the instructions to create a new box that executes some terminal commands and we can archieve this by following the [documentation of Vagrant](https://developer.hashicorp.com/vagrant/docs). And after some research the following code was accomplished:

```ruby

Vagrant.configure("2") do |config|

# monitor machine with nagios installed

config.vm.define "monitor"  do |monitor|

monitor.vm.hostname = "nagios"

monitor.vm.box = "starboard/ubuntu-arm64-20.04.5"

monitor.vm.box_version = "20221120.20.40.0"

monitor.vm.box_download_insecure = true

monitor.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true  #Allowing to get information of the virtual machine SSH key

v.gui = true  #In Mac computer with Apple Silicon processors we need to have GUI enabled otherwise it doesn't work.

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"  #Hypervisor network driver that is optimized to provide high performance, high throughput, and minimal latency

end

#commands to install nagios on the machine

monitor.vm.provision "shell", inline: <<-SHELL

sudo apt-get update

sudo apt-get install -y autoconf gcc libc6 make wget unzip apache2 php

cd /tmp

wget  -O nagioscore.tar.gz https://github.com/NagiosEnterprises/nagioscore/archive/nagios-4.4.6.tar.gz

tar xzf nagioscore.tar.gz

cd nagioscore-nagios-4.4.6/

sudo ./configure --with-httpd-conf=/etc/apache2/sites-enabled

sudo make all

sudo useradd nagios

sudo usermod -a  -G nagios www-data

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

sudo htpasswd -c  -b /usr/local/nagios/etc/htpasswd.users nagiosadmin nagiosadmin

#Restarting nagios to apply the changes

sudo systemctl restart apache2.service

sudo systemctl start nagios.service

# Installing plugins

sudo apt-get install -y autoconf gcc libc6 libmcrypt-dev make libssl-dev wget bc gawk dc build-essential snmp libnet-snmp-perl gettext iputils-ping

cd /tmp

wget  --no-check-certificate  -O nagios-plugins.tar.gz https://github.com/nagios-plugins/nagios-plugins/archive/release-2.3.3.tar.gz

tar xzf nagios-plugins.tar.gz

cd nagios-plugins-release-2.3.3/

sudo ./tools/setup

sudo ./configure

sudo make

sudo make install

#Restarting nagios again to apply changes

sudo systemctl restart nagios.service

SHELL

# Redirect port http 80 to 8080 on host to access nagios web interface

monitor.vm.network "forwarded_port", guest: 80, host: 8080

end

end

```

The comments on the code make it easy to understand what is happening. As we stated before, after having the Vagrantfile ready we execute ```vagrant up``` and the machine will start and then we go to a browser and access ```localhost:8080/nagios``` and you will be prompted a username and a password those are:

  

- username: nagiosadmin

- password: nagiosadmin

  

After that you will will be redirected to the nagios main page. Knowing that nagios is working we should proceed to the next step that is sharing the nagios configuration files with the host machine to be easier to configure it. On the Vagrant code snippet we already have shared command indicating the path of the host machine and the path of the virtual machine but is commented because on the first run we still don't have the config files and would generate errors, so you should run the VM with that commented and then remove then you can uncomment the following line:

```config.vm.synced_folder "nagios/etc", "/usr/local/nagios/etc"```

But that command doesn't copy the content of the folder to the host machine we need to make it ourselves. First we should create the directory ```nagios/etc``` at the same level of the Vagrantfile then access the machine with the command ```vagrant ssh monitor```.

Since the virtual machine shares a folder named `Vagrant`, that is the location of the Vagrantfile, with host machine we can copy the nagios config directory to the host directory with the following command: ```sudo cp -r /usr/local/nagios/etc/* vagrant/nagios/etc/``` and after that you should restart the machine with ```vagrant reload``` and then we can see that we have all the config files of nagios that if we edit will reflect on the nagios webpage (After reloading nagios service).

  

## Basics of monitoring a remote host

  

Having nagios installed in one VM and Tomcat in another, we can start monitoring the Tomcat machine to understand the basics of the monitoring software.

In the shared folders of the nagios machine we can see that we have a folder named `Objects` that has the configuration files that define objects that Nagios monitors, such as hosts, services, and commands. These objects can be defined using simple syntax, and they are used to configure the monitoring system.

These are the most used files:

  

-  **commands.cfg:** This file defines the commands that Nagios can use to monitor various services and hosts, for example check the website status by performing a HTTP request.

-  **contacts.cfg:** This file defines the contacts or individuals who should receive alerts from Nagios when there are issues detected.

-  **hosts.cfg:** This file defines the specific services that Nagios can monitor on hosts.

  

Summarising, it's where we configure everything to monitor what we want.

  

For a simple host monitor config we created the `server.cfg`, that has the following configurations:

```

# Define a host for the local machine

define host {

use linux-server ; Name of host template to use

host_name server ; Name of the host to monitor

alias Tomcat Server ; A friendly name for the host

address 192.168.64.162 ; IP address of the host

contacts nagiosadmin

}

  

# Define a service to check HTTP on the remote server.

# Disable notifications for this service by default, as not all users may have HTTP enabled.

define service {

use local-service ; Name of service template to use

host_name server

service_description HTTP

check_command check_http! -p 8080

contact_groups admins #the group that will get contacted if something happens

notifications_enabled 1

}

  

# Define a service to check the load on the local machine.

define service {

use local-service ; Name of service template to use

host_name localhost

service_description Current Load

check_command check_local_load!5.0,4.0,3.0!10.0,6.0,4.0

contact_groups admins #the group that will get contacted if something happens

notifications_enabled 1

}

  

# Define a service to check if SSH is running on the host

define service {

use generic-service ; Name of service template to use

host_name server ; Name of the host to monitor

service_description SSH ; A friendly name for the service

check_command check_ssh ; Name of the Nagios plugin to use

contact_groups admins #the group that will get contacted if something happens

notifications_enabled 1

}

  

define service {

use local-service ; Name of service template to use

host_name server

service_description Current Users

check_command check_local_users!20!50

contact_groups admins #the group that will get contacted if something happens

notifications_enabled 1

}

  

# Define a service to check the number of currently running procs

# on the local machine. Warning if > 250 processes, critical if

# > 400 processes.

define service {

use local-service ; Name of service template to use

host_name server

service_description Total Processes

check_command check_local_procs!250!400!RSZDT

contact_groups admins #the group that will get contacted if something happens

notifications_enabled 1

}

  

# Define a service to check the disk space of the root partition

# on the local machine. Warning if < 20% free, critical if

# < 10% free space on partition.

define service {

use local-service ; Name of service template to use

host_name localhost

service_description Root Partition

check_command check_local_disk!20%!10%!/

contact_groups admins #the group that will get contacted if something happens

notifications_enabled 1

}

```

  

We can see that on the code we have a host, that has the machine details (name, alias and IP address) and some services that are the commands we want to execute against the host to check something, for instance, a service to check HTTP on the remote server.

  

After creating the new file we need to import it on the `nagios.cfg` so that Nagios can run the new host services defined on that file, and we can archive that by adding the following to the config:

  

```

# Definitions for monitoring the remote server

cfg_file=/usr/local/nagios/etc/objects/name_of_file.cfg

```

Then we should reload Nagios service with `sudo systemctl restart nagios.service`.

  

Then we open nagios on the browser and go to **Hosts** and we should see something like the next image.

![Nagios hosts](https://i.imgur.com/wP9CzTl.png)

  

We have the new host that is still pending because we are waiting for the services to execute, we can get more detailed information when we click on the host name, like we can see on the next image.

![Host services](https://i.imgur.com/HQGbkpp.png)

  

We also can get notified if something happens with the service, as we can see on the config that was previously shown the contact group that will be contacted is named `admins`. We can find the contact groups and the contact on the file `contacts.cfg` that on this case has the following aspect:

  

```

define contact {

contact_name nagiosadmin ; Short name of user

use generic-contact ; Inherit default values from generic-contact template (defined above)

alias Nagios Admin ; Full name of user

email 1220257@isep.ipp.pt

service_notification_period 24x7

host_notification_period 24x7

service_notification_options w,u,c,r,f,s

host_notification_options d,u,r,f,s

service_notification_commands notify-service-by-email

}

  

define contactgroup {

contactgroup_name admins

alias Nagios Administrators

members nagiosadmin

}

```

  

In the previous configuration we can see that we have a single user (we can have more) and we have a contactgroup (we can have more contact groups).

In this case we can see that the contact can be notified at all time (27x7 time period that is also defined in a .cfg file) for both the hosts and services. and we can see that he will be notified about different type of notifications depending if it comes from a host or a service. The labels that exist are the following:

  

-  `w`: Notify on WARNING service states;

-  `u`: Notify on UNKNOWN service states;

-  `c`: Notify on CRITICAL service states;

-  `r`: Notify on service RECOVERY (OK states);

-  `f`: Notify when the service starts and stops FLAPPING;

-  `n (none)`: Do not notify the contact on any type of service notifications;

  

Having the contacts, the contact group defined and the host configured to send notifications we also have to configure Nagios so that he can send emails.

In this case was used a SMTP server [SendInBlue](https://www.sendinblue.com/) that has a free tier with a considerable amount of free emails per month.

To send emails we use `sendEmail` that is a lightweight, completely command line based, SMTP email agent, to install it we added to vagrant provision the following commands:

```

#commands to install sendEmail

wget http://caspian.dotconf.net/menu/Software/SendEmail/sendEmail-v1.56.tar.gz

tar xzf sendEmail-v1.56.tar.gz

sudo cp -a sendEmail-v1.56/sendEmail /usr/local/bin

sudo chmod +x /usr/local/bin/sendEmail

```

To save up some time just execute the commands manually, but if you need to create the machine on another host the sendEmail will be automatically installed.

  

Having sendEmail installed, just type ```sendEmail``` on the command line and you should see the version and the commands that you can execute. To test if you can send emails with your SMTP API use the following command but replacing the credentials with yours:

```sendEmail -f <from_email> -t <to_email> -u subject -m testing -s <smtp:port> -o tls=no -xu <user> -xp <password>```

  

If everything goes well you will receive a email (Check on the Spam folder).

  

Now you should go to the `commands.cfg` and replace the commands **notify_host_by_email** and **notify_service_by_email** with the following:

```

define command {

command_name notify-host-by-email

command_line /usr/bin/printf "%b" "***** Nagios *****\n\nNotification Type: $NOTIFICATIONTYPE$\nHost: $HOSTNAME$\nState: $HOSTSTATE$\nAddress: $HOSTADDRESS$\nInfo: $HOSTOUTPUT$\n\nDate/Time: $LONGDATETIME$\n" | /usr/local/bin/sendEmail -f <from_email> -t $CONTACTEMAIL$ -u "** $NOTIFICATIONTYPE$ Host Alert: $HOSTNAME$ is $HOSTSTATE$ **" -s <smtp:port> -o tls=no -xu <user> -xp <password>

}

  

define command {

command_name notify-service-by-email

command_line /usr/bin/printf "%b" " Nagios \n\nNotification Type: $NOTIFICATIONTYPE$\n\nService: $SERVICEDESC$\nHost: $HOSTALIAS$\nAddress: $HOSTADDRESS$\nState: $SERVICESTATE$\n\nDate/Time: $LONGDATETIME$\n\nAdditional Info:\n\n$SERVICEOUTPUT$\n" | /usr/local/bin/sendEmail -f <from_email> -t $CONTACTEMAIL$ -s <smtp:port> -u " Nagios Service Alert: $HOSTALIAS$/$SERVICEDESC$ is $SERVICESTATE$ " -o tls=no -xu <user> -xp <password>

}

```

  

Now you should go to your host file and add to the services that you want to get notified about and add the contact group and enable the notifications, and should look something like this:

```

define service {

use generic-service ; Name of service template to use

host_name server ; Name of the host to monitor

service_description SSH ; A friendly name for the service

check_command check_ssh ; Name of the Nagios plugin to use

contact_groups admins #contact group that will be notified

notifications_enabled 1

}

```

And to assume the changes reload nagios.

By now we should have everything configured to receive emails from nagios. Now we should test and for that we are going to test the SSH service:

  

1. Turn of the ssh service on the server with the command `sudo systemctl stop ssh`

2. Force check the service by clicking on Hosts>Magnifying glass close to the host name>SSH>Re-schedule the next check of this service>Commit

Then if you go back to the service list you will notice that the state of the SSH changed, you should wait until it gets a HARD Critical state and then you will receive an email.

  

## Nagios NRPE

NRPE allows Nagios to execute plugins on remote Linux/Unix hosts and gather performance data and status information about the remote host.

  

The NRPE agent can be used to monitor a variety of metrics on the remote host, such as CPU usage, disk space, memory usage, and network traffic.

  

It provides a secure and efficient method of monitoring remote hosts and can help to ensure that critical systems are running smoothly.

  

The NRPE agent is installed on the remote host that needs to be monitored, and the Nagios server is configured to communicate with the NRPE agent. The NRPE agent listens for incoming requests from the Nagios server and executes the requested plugins, returning the results to the Nagios server. The next image clearly illustrates was was described before.

  

![NRPE Architecture](https://www.bujarra.com/wp-content/uploads/2017/05/Nagios-NRPE-00.jpg)

  

Overall, NRPE is a valuable tool for system administrators and IT professionals who need to monitor remote hosts and ensure that their systems are performing optimally. All information was found in the [NRPE Documentation](https://assets.nagios.com/downloads/nagioscore/docs/nrpe/NRPE.pdf).

  

**Installing NRPE on the server machine**

  

The following commands were added to the server machine provision to install NRPE:

```

#install nrpe

sudo apt-get install -y autoconf automake gcc libc6 libmcrypt-dev make libssl-dev wget openssl

cd /tmp

wget --no-check-certificate -O nrpe.tar.gz https://github.com/NagiosEnterprises/nrpe/archive/nrpe-4.1.0.tar.gz

tar xzf nrpe.tar.gz

cd /tmp/nrpe-nrpe-4.1.0/

sudo ./configure --enable-command-args --with-ssl-lib=/usr/lib/aarch64-linux-gnu/

sudo make all

sudo make install-groups-users

sudo make install

sudo make install-config

sudo sh -c "echo >> /etc/services"

sudo sh -c "sudo echo '# Nagios services' >> /etc/services"

sudo sh -c "sudo echo 'nrpe 5666/tcp' >> /etc/services"

sudo make install-init

sudo systemctl enable nrpe.service

sudo apt-get install -y ufw

sudo mkdir -p /etc/ufw/applications.d

sudo sh -c "echo '[NRPE]' > /etc/ufw/applications.d/nagios"

sudo sh -c "echo 'title=Nagios Remote Plugin Executor' >> /etc/ufw/applications.d/nagios"

sudo sh -c "echo 'description=Allows remote execution of Nagios plugins' >> /etc/ufw/applications.d/nagios"

sudo sh -c "echo 'ports=5666/tcp' >> /etc/ufw/applications.d/nagios"

sudo ufw allow NRPE

sudo ufw reload

sudo sh -c "sed -i 's/^dont_blame_nrpe=.*/dont_blame_nrpe=1/g' /usr/local/nagios/etc/nrpe.cfg"

sudo systemctl start nrpe.service

sudo apt-get install -y autoconf gcc libc6 libmcrypt-dev make libssl-dev wget bc gawk dc build-essential snmp libnet-snmp-perl gettext

cd /tmp

wget --no-check-certificate -O nagios-plugins.tar.gz https://github.com/nagios-plugins/nagios-plugins/archive/release-2.2.1.tar.gz

tar zxf nagios-plugins.tar.gz

cd /tmp/nagios-plugins-release-2.2.1/

sudo ./tools/setup

sudo ./configure --build=aarch64-unknown-linux-gnu

sudo make

sudo make install

```

  

After that was also added the following command to create a synced folder: ```server.vm.synced_folder "server/nagios/etc", "/usr/local/nagios/etc"``` (The earlier section of the document provided an explanation on how to handle synced folders.)

  

To verify the successful installation, utilize the following command:

```/usr/local/nagios/libexec/check_nrpe -H localhost```

The output should be the NRPE version.

  

Locate the IP address of the monitoring VM and execute the following command on the server VM, substituting "nagios_ip" with the actual IP address:

```sudo sh -c "sed -i '/^allowed_hosts=/s/$/,<nagios_ip>/' /usr/local/nagios/etc/nrpe.cfg"```

  

To apply the updates, restart the NRPE service.

  

**Install NRPE plugin on monitor**

The following commands were added to the monitor machine provision to install NRPE plugin:

```

#NRPE plugin

cd /tmp/

wget https://github.com/NagiosEnterprises/nrpe/releases/download/nrpe-4.0.2/nrpe-4.0.2.tar.gz

tar xzf nrpe-4.0.2.tar.gz

cd /tmp/nrpe-4.0.2/

sudo ./configure --enable-command-args --with-ssl-lib=/usr/lib/aarch64-linux-gnu/

sudo make check_nrpe

sudo make install-plugin

#Restarting nagios again to apply changes

sudo systemctl restart nagios.service

```

  

To ensure successful communication with the server, execute the following command:

```/usr/local/nagios/libexec/check_nrpe -H <server_ip>```

The output should display the version of NRPE.

  

**Executing commands remotely**

  

To execute commands remotely, you must first review the commands specified in the `/usr/local/nagios/etc/nrpe.cfg` file. By default, it includes the following commands:

```

command[check_users]=/usr/local/nagios/libexec/check_users -w 5 -c 10

command[check_load]=/usr/local/nagios/libexec/check_load -r -w .15,.10,.05 -c .30,.25,.20

command[check_disk]=/usr/local/nagios/libexec/check_disk -w 20% -c 10% -p /dev/nvme0n1p2

command[check_zombie_procs]=/usr/local/nagios/libexec/check_procs -w 5 -c 10 -s Z

command[check_total_procs]=/usr/local/nagios/libexec/check_procs -w 220 -c 300

```

  

Note that for the check_disk command, you must update it with the appropriate disk that you are using.

  

Subsequently, on the monitoring machine, you will need to create a service for each command that you intend to use. For instance:

  

```

#check CPU load

define service{

use generic-service

host_name server

service_description CPU Load

check_command check_nrpe!check_load

}

```

  

This plugin is installed on the remote host that is being monitored and communicates with the Nagios monitoring system to provide real-time updates on the status of the CPU load.

  

The check_load command checks the current CPU load on the remote host and compares it to the warning and critical thresholds defined in the NRPE configuration file. If the CPU load exceeds the defined thresholds, the check_load command sends an alert to the Nagios monitoring system, which can then take appropriate action to mitigate the issue.

  

**Event Handlers**

  

Nagios event handlers are scripts or commands that are executed automatically by Nagios when a host or service event occurs, such as a host or service failure.

  

These event handlers can be used to take action to correct the problem or notify administrators. For example, an event handler can be configured to restart a failed service or send an email notification to an administrator. Nagios event handlers are an important tool for maintaining system availability and can help reduce downtime by automating corrective actions.

  

In this case we want to restart tomcat that is on the server VM if the HTTP service is down, and for that we needed to create a script to restart tomcat, like the following:

  

```

if sudo service tomcat9 restart; then

echo "Tomcat Restarted Successfuly"

exit 0

else

echo "Can't Restart Tomcat"

exit 2

fi

```

  

The previous script demonstrates the usage of exit codes 0 and 2; however, there are other exit codes available. Among these, exit codes 0, 1, and 2 are the most commonly used. Here is a brief description of each:

- Exit code 0: OK - This indicates that the service or host check has returned a "normal" status, indicating that everything is working correctly.

- Exit code 1: WARNING - This indicates that the service or host check has returned a "warning" status, indicating that there may be a problem or an impending issue that requires attention.

- Exit code 2: CRITICAL - This indicates that the service or host check has returned a "critical" status, indicating that there is a problem or outage that requires immediate attention.

These exit codes are used by Nagios to determine the status of a service or host and to trigger notifications or event handlers. When a service or host check is executed, Nagios will use the exit code returned by the plugin to determine the state of the check, and will take appropriate action based on the state (e.g. notify contacts, run event handlers, etc.).

  

It's worth noting that Nagios also supports additional exit codes for more specific states (e.g. "unknown" or "pending"), as well as custom exit codes defined by plugins.

  

The script will be stored in `usr/local/nagios/etc/restart_tomcat.sh` on the **server VM**. To execute this script, we must first create a command in `nrpe.cfg`. For example:

```

command[restart_tomcat]=/usr/local/nagios/etc/scripts/restart_tomcat.sh

```

  

Before testing this command, you need to grant permission to the nagios group to execute `sudo systemctl restart nagios.service` as root. To do this, follow these steps:

  

1. Run `sudo visudo`

2. Edit the file and add `nagios ALL=(ALL) NOPASSWD:ALL`

  

Once this is done, you can test the command's functionality by executing the following command: `sudo -u nagios /usr/local/nagios/libexec/check_nrpe -H localhost -c restart_tomcat`

  

Observe the output to determine if the operation was successful or not.

  

In order to make necessary changes, it is required to modify the HTTP service on the **monitor VM**. Specifically, you will need to edit the event handler in accordance with the given instructions. By doing so, you will be able to enhance the functionality of the HTTP service and ensure that it meets the requirements of your system.

  

```

define service {

use local-service

host_name server

service_description Check HTTP

check_command check_http! -p 8080

contact_groups admins

notifications_enabled 1

event_handler_enabled 1

event_handler check_nrpe!restart_tomcat

}

```

  

In the given configuration, an event handler has been defined for the `Check HTTP` service. The event handler is triggered when a critical state is detected by the monitoring system. In this case, the event handler command is `check_nrpe!restart_tomcat`, which invokes the NRPE plugin to execute the `restart_tomcat` command. This command is defined in the NRPE configuration file and is responsible for restarting the Tomcat server. By using an event handler in this way, the monitoring system can automatically take corrective action when critical issues are detected, thereby reducing the risk of service downtime and improving the overall reliability of the system.

  

## Zabbix

  

Zabbix is a powerful open-source monitoring software that allows to monitor and track the performance of IT infrastructures, servers, and network devices. It can monitor different aspects of IT environments, such as system resources, network connectivity, and application performance.

  

With Zabbix, is possible to set up triggers and alerts to notify when a specific metric exceeds a certain threshold. Zabbix provides a web interface that allows to view graphs and reports of monitored resources, and it supports a wide range of third-party integrations.

  

The main components of Zabbix include:

  

1. Zabbix Server: The Zabbix server is the core component of the Zabbix system. It collects data from different sources, stores it in a database, and analyzes the data to generate alerts and reports. The server can be configured to collect data from different sources such as network devices, servers, applications, and cloud services.

2. Zabbix Agent: The Zabbix agent is a lightweight software that runs on the monitored device and collects data locally. It sends the collected data to the Zabbix server for processing and analysis. The agent can collect various metrics such as CPU usage, memory usage, disk usage, and network traffic.

3. Zabbix Proxy: The Zabbix proxy is an optional component that can be used to collect data from remote locations or distributed networks. The proxy collects data from the monitored devices and sends it to the Zabbix server. This helps to reduce the load on the network and the Zabbix server.

4. Zabbix Frontend: The Zabbix frontend is the web-based user interface that provides access to the monitoring system. It allows users to configure the system, view alerts, generate reports, and perform other administrative tasks. The frontend is accessible from any device with a web browser.

5. Zabbix API: The Zabbix API is an interface that allows external applications to communicate with the Zabbix system. It can be used to automate tasks, integrate with other systems, and create custom applications.

  

In summary, Zabbix provides a comprehensive set of components that can be used to monitor and manage IT infrastructure. The Zabbix server, agent, proxy, frontend, and API work together to provide a powerful monitoring solution that can be customized to meet the specific needs of any organization.

  

Information found in the Zabbix official [documentation](https://www.zabbix.com/documentation/current/en/manual/introduction/about).

  

**Installing Zabbix**

  

Installing Zabbix is a straightforward process that begins by accessing the installation documentation on the official website [found here](https://www.zabbix.com/download?zabbix=6.0&os_distribution=ubuntu_arm64&os_version=20.04&components=server_frontend_agent&db=mysql&ws=apache). Within this documentation, one can easily select the desired version of the software (in this case, the 6.0 LTS), as well as the specific components needed such as the server, frontend, and agent. Additionally, one can specify the preferred operating system distribution (ubuntu arm64), the version of the OS (20.04), the database (mysql), and the webserver (apache).

  

In this case, I adhered to the concept of Infrastructure as Code and leveraged Vagrant to automate the process of launching virtual machines. I have provisioned two machines - Zabbix and server - and the corresponding Vagrantfile is outlined below:

```ruby

Vagrant.configure("2") do |config|

# zabbix machine with nagios installed

config.vm.define "zabbix"  do |zabbix|

zabbix.vm.hostname = "zabbix"

zabbix.vm.box = "starboard/ubuntu-arm64-20.04.5"

zabbix.vm.box_version = "20221120.20.40.0"

zabbix.vm.box_download_insecure = true

zabbix.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true  #Allowing to get information of the virtual machine SSH key

v.gui = true  #In Mac computer with Apple Silicon processors we need to have GUI enabled otherwise it doesn't work.

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"  #Hypervisor network driver that is optimized to provide high performance, high throughput, and minimal latency

end

# Redirect port http 80 to 8080 on host to access nagios web interface

zabbix.vm.network "forwarded_port", guest: 80, host: 8080

zabbix.vm.provision "shell", path: "./scripts/zabbix_installer.sh", run: "once"

zabbix.vm.provision "shell", path: "./scripts/zabbix_restart.sh", run: "always"

end

config.vm.define "server"  do |server|

server.vm.hostname = "server"

server.vm.box = "starboard/ubuntu-arm64-20.04.5"

server.vm.box_version = "20221120.20.40.0"

server.vm.box_download_insecure = true

server.vm.provider "vmware_desktop"  do |v|

v.ssh_info_public = true  #Allowing to get information of the virtual machine SSH key

v.gui = true  #In Mac computer with Apple Silicon processors we need to have GUI enabled otherwise it doesn't work.

v.linked_clone = false

v.vmx["ethernet0.virtualdev"] = "vmxnet3"  #Hypervisor network driver that is optimized to provide high performance, high throughput, and minimal latency

end

server.vm.provision "shell", path: "./scripts/tomcat_installer.sh", run: "once"

end

end

```

  

The scripts you see that the provision is running are the following:

**zabbix_installer.sh**

```bash

cd  /tmp

sudo  wget  https://repo.zabbix.com/zabbix/6.0/ubuntu-arm64/pool/main/z/zabbix-release/zabbix-release_6.0-5+ubuntu20.04_all.deb

sudo  dpkg  -i  zabbix-release_6.0-5+ubuntu20.04_all.deb

sudo  apt  update

sudo  apt  install  -y  zabbix-server-mysql  zabbix-frontend-php  zabbix-apache-conf  zabbix-sql-scripts  zabbix-agent

sudo  apt-get  -y  install  mysql-server

sudo  sed  -i  "s/# DBPassword=/DBPassword=password/g"  /etc/zabbix/zabbix_server.conf

sudo  su

mysql  -u  root  -e  "create database zabbix character set utf8mb4 collate utf8mb4_bin";

mysql  -u  root  -e  "create user zabbix@localhost identified by 'password'";

mysql  -u  root  -e  "grant all privileges on zabbix.* to zabbix@localhost";

mysql  -u  root  -e  "set global log_bin_trust_function_creators = 1";

sudo  zcat  -v  -f  /usr/share/zabbix-sql-scripts/mysql/server.sql.gz | mysql  --default-character-set=utf8mb4  -uzabbix  -ppassword  zabbix  --verbose

exit

```

**zabbix_restart.sh**

```bash

sudo  su

mysql  -u  root  -e  "set global log_bin_trust_function_creators = 0";

exit

sudo  systemctl  restart  zabbix-server  zabbix-agent  apache2

sudo  systemctl  enable  zabbix-server  zabbix-agent  apache2

```

**tomcat_installer**

```bash

sudo  apt-get  update

sudo  apt-get  -y  install  tomcat9

```

  

Upon executing the Vagrantfile, access to the Zabbix installation can be attained by navigating to localhost:8080/zabbix. During the installation process, you will be prompted to enter the database password, which, in this case, is 'password'. Once the installation is complete, you will be directed to a login page that requires the following default credentials:

  

- User: Admin

- Password: zabbix

  

To monitor the Tomcat server, please refer to [this tutorial](https://techexpert.tips/zabbix/monitoring-tomcat-zabbix/), which provides comprehensive step-by-step instructions.

  

## Prometheus

Prometheus is a popular open-source monitoring and alerting solution used to collect and record metrics from various systems and services. It was designed to handle large-scale, dynamic environments and provides a powerful query language, a web-based interface, and a range of integrations. Prometheus uses a pull-based model to scrape data from target systems, allowing it to work with a variety of technologies and architectures. It is commonly used in conjunction with Grafana, a visualization platform, to create rich dashboards and reports.

  

This was not used since I was having problems running it in the mac M1.

Information gathered from the prometheus official [documentation](https://prometheus.io/docs/introduction/overview/).

  

## Icinga

  

Icinga is an open source network and system monitoring application that allows users to monitor the availability of their IT infrastructure, detect and diagnose problems, and take corrective actions in real time.

  

Icinga was originally developed as a fork of the Nagios monitoring system, providing additional features and extensions such as an improved web interface, improved scalability, and extensibility.

  

Icinga allows users to monitor various components of their IT infrastructure such as servers, switches, routers, applications and services and receive alerts and notifications in case of any downtime or performance issues. Icinga also allows users to define and configure monitoring policies and thresholds, generate reports and track performance trends over time.

  

Overall, Icinga is a powerful and flexible monitoring tool that organizations can use to ensure the reliability, availability and performance of their IT systems and applications.

  

This was not used since I was having problems running it in the mac M1.

Information gathered from Icinga official [documentation](https://icinga.com/docs/icinga-2/latest/doc/01-about/#:~:text=Icinga%20is%20a%20monitoring%20system,complex%20environments%20across%20multiple%20locations.).