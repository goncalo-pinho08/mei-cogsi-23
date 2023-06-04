# COGSI P3 - Gonçalo Pinho - 1220257

The objective of the third project is to substitute virtual machines with docker technology. The task at hand involves the development of P1 and P2 using docker as the primary platform.

# What's docker?
![Docker](https://www.cloudsavvyit.com/p/uploads/2021/04/075c8694.jpeg?height=200p&trim=2,2,2,2)
[Docker](https://docs.docker.com/get-started/overview/) is a popular tool among developers and system administrators as it allows for the quick launch of applications without affecting the system. Containers encapsulate everything required to run an application, and Docker builds these containers using a Dockerfile to create images that define the software available. Anyone can launch an application with an image created through Docker.
![How containarization with docker works](https://www.docker.com/wp-content/uploads/2021/11/docker-containerized-appliction-blue-border_2.png)
Containers are an abstraction at the app layer that packages code and dependencies together. Multiple containers can run on the same machine and share the OS kernel with other containers, each running as isolated processes in user space. Containers take up less space than VMs (container images are typically tens of MBs in size), can handle more applications and require fewer VMs and Operating systems.

**Commands needed in this project:**
This is a small list of commands that we will use along the project.
 - List all docker containers: `docker ps`
 - Stop, Start or restart container `docker <start-stop-restart> <container-id>`
 - Enter on container termial `docker exec -it <container-id> bin/bash`

# Create a monitoring container with nagios

When it comes to setting up a monitoring container with Nagios, one has the choice to either create a personalized image or utilize an existing one from Docker Hub. In my particular case, I decided to opt for the latter option since the pre-existing image had everything necessary to launch the project. However, as I was using a Mac M1, I encountered issues with the JasonRivers image not functioning correctly, and thus had to search for an image compatible with the arm64 architecture. Fortunately, I came across an image developed by Tronyx that was based on the JasonRivers docker image and included Nagios as well as a range of plugins such as NRPE, NCPA, NSCA, and NagiosTV. This approach saved me a considerable amount of time as I didn't need to create a customized image from scratch.

To ensure effective monitoring of other containers, it is essential to prepare the orchestration tool in advance that can initiate all containers within the same network. Docker Compose is an ideal solution for this purpose, and I have chosen to employ it in this particular scenario.
[Compose](https://docs.docker.com/compose/) is a powerful tool designed to facilitate the development and operation of multi-container Docker applications. Its functionality is based on a YAML file, which serves as a configuration file for all the application's services. With just a single command, Compose creates and starts all the services in your configuration, making deployment effortless.

Whether you're working in production, staging, development, testing, or other environments, Compose is built to work seamlessly across all platforms, including CI workflows. Additionally, Compose offers a range of commands for managing the complete lifecycle of your application, including starting, stopping, and rebuilding services, monitoring the status of running services, streaming the log output of services, and executing a one-off command on a service.

The image had all plugins needed for the nagios monitoring but it didn't have the sendEmail command so I needed to create a Dockerfile that based on the Tronyx nagios image that would copy the shell script previously built by me and run it to install the command.

```
FROM tronyx/nagios:latest
COPY scripts/monitor/install_send_email.sh /tmp/install_send_email.sh
RUN chmod +x /tmp/install_send_email.sh && \
/tmp/install_send_email.sh && \
rm -rf /tmp/install_send_email.sh
```

Then, as i've said previously I needed to create a file named `docker-compose.yml` and do the following:
```yaml
version: '3'
services:
	nagios:
		build: .
		ports:
			- "8080:80"
		volumes:
			- ./nagios:/opt/nagios/etc
		networks:
		 cogsi:
		  ipv4_address: 10.5.0.5

networks:
	cogsi:
		driver: bridge
			ipam:
			 config:
			  - subnet: 10.5.0.0/16
			    gateway: 10.5.0.1
```

This is a Docker Compose file written in version 3, which is used to define and run multi-container Docker applications. In this file, a service named "nagios" is defined after building the previously shown Dockerfile that will be used to run the Nagios monitoring tool. The service is also configured to expose the container port 80 on the host's port 8080, which means that the Nagios web interface will be accessible on the host machine's IP address at port 8080.

Furthermore, a volume is defined to persist Nagios configuration files on the host machine in the "./nagios" directory, and a network named "cogsi" is created with a subnet of 10.5.0.0/16 and a gateway of 10.5.0.1. The "nagios" service is then attached to this network with a static IPv4 address of 10.5.0.5. The network driver used is "bridge", which is the default driver for Docker networks. This configuration allows multiple containers to communicate with each other and share resources on the same network.

Launching the docker-compose file is a straightforward process. Once you navigate to the directory that you have your docker-compose file, you can initiate the command `docker compose up` from the terminal. Once the process is complete, you can access Nagios by entering `localhost:8080/nagios` in your browser. You will be prompted to enter a username and password, which in this case are:
-   Username: nagiosadmin
-   Password: nagios

With these credentials, you can access Nagios, which will be ready for use immediately.

After nagios is up and running you should have the the nagios config and first of all you need to go to objects>commands.cfg and edit the send email command to the one used on the previous projects and go to the contacts.cfg and change the contact email to the one you want.

After changing that you need to restart the docker container.

# Creating a monitored container with todd server

In this portion of the project, I replicated previous work done in P2 with the added advantage of utilizing Docker. This made the process easier compared to the previous iteration, as I did not have to manually edit addresses upon container restarts. The setup of the nagios was extensively documented in the P2 readme, so on this one I will only document what I've done to make it work on docker.
To begin, I utilized the Dockerfile provided by the professor and made the necessary modifications to tailor it to my particular solution. These adaptations allowed me to streamline the replication process with minimal effort.

```
FROM  "ubuntu:18.04"
LABEL __copyright__="(C) Guido Draheim, licensed under the EUPL" \
__version__="1.5.7106"
RUN apt-get update -y && \
apt-get install -y git && \
apt-get install -y openjdk-8-jdk-headless python3 sudo openssh-server
COPY files/docker/systemctl3.py /usr/bin/systemctl
RUN test -L /bin/systemctl || ln -sf /usr/bin/systemctl /bin/systemctl
RUN systemctl start ssh
RUN systemctl enable ssh
RUN mkdir -p /usr/local
WORKDIR /usr/local
#copy todd_app from the host machine to the container
COPY ./todd_app /usr/local/todd
WORKDIR /usr/local/todd
RUN ./gradlew clean build
RUN cp ./build/libs/todd-1.0.1.jar ./todd.jar
ADD files-todd-service/ToddService.service /etc/systemd/system/ToddService.service
RUN chmod +x+r /etc/systemd/system/ToddService.service
ADD files-todd-service/ToddService.sh /usr/local/todd/ToddService.sh
RUN chmod +x /usr/local/todd/ToddService.sh
#INSTALL NRPE
COPY files/plugins/install_nrpe.sh /tmp/install_nrpe.sh
RUN chmod +x /tmp/install_nrpe.sh && \
/tmp/install_nrpe.sh && \
rm -rf /tmp/install_nrpe.sh
#INSTALL SEND_NSCA
COPY files/plugins/install_send_nsca.sh /tmp/install_send_nsca.sh
RUN chmod +x /tmp/install_send_nsca.sh && \
/tmp/install_send_nsca.sh && \
rm -rf /tmp/install_send_nsca.sh
COPY files/scripts/restart_todd.sh /usr/local/nagios/libexec/restart_todd.sh
RUN systemctl enable ToddService
CMD ["/usr/bin/systemctl"]
```

The given Dockerfile is used to create a Docker container image that includes an application todd. The Docker image is based on the Ubuntu 18.04 operating system and includes software packages such as Git, OpenJDK 8, Python 3, and SSH server. The Dockerfile also installs Nagios Remote Plugin Executor (NRPE) and NSCA (Nagios Service Check Acceptor) for monitoring purposes.

The Dockerfile starts by updating the system and installing necessary software packages. It then copies a Python script that emulates the "systemctl" command, which is not available in the Docker container by default. The Dockerfile also copies the "todd" application files from the host machine to the container and builds the application using Gradle.

The Dockerfile sets up the "ToddService" systemd service and copies a service file and a service script to the appropriate directories. It also installs NRPE and NSCA by copying installation scripts and running them. Finally, it copies a script that restarts the "todd" application and enables the "ToddService" systemd service.

When the Docker container is started, the default command is set to run the "systemctl" command, which allows starting and stopping services in the container.

I have created a service in the docker-compose.yml file for the todd container, which includes the following configuration:

```yaml
todd:
build: todd
	ports:
	 - "10500:10500"
	 - "3006:3006"
	networks:
	 cogsi:
	  ipv4_address: 10.5.0.6
	command: /bin/sh -c "/usr/local/nagios/bin/nrpe -c /usr/local/nagios/etc/nrpe.cfg -d && systemctl start ToddService && systemctl start ssh && tail -f /dev/null"
```

The purpose of this command is to enable the nrpe without manual entry to the container. Since the container command has been altered, I needed to initiate the other necessary services as well. Moreover, I had to modify the nagios dockerfile to include todd and enable the usage of the jar to facilitate the monitoring of the machine via jmx.

```
FROM tronyx/nagios:latest
RUN mkdir -p /usr/local
RUN apt-get update -y && \
apt-get install -y git && \
apt-get install -y openjdk-8-jdk-headless sudo
#copy todd_app from the host machine to the container
COPY ./todd/todd_app /usr/local/todd
WORKDIR /usr/local/todd
RUN ./gradlew clean build
RUN cp ./build/libs/todd-1.0.1.jar /opt/nagios/libexec/todd.jar
COPY scripts/monitor/install_send_email.sh /tmp/install_send_email.sh
RUN chmod +x /tmp/install_send_email.sh && \
/tmp/install_send_email.sh && \
rm -rf /tmp/install_send_email.sh
```

The install_send_email.sh was already used on P1 and P2. 
Since I have the nagios volume, before running the compose file I've created the host.cfg for the todd container:
```
# Define a host for the local machine
define host {
use linux-server ; Name of host template to use
host_name todd ; Name of the host to monitor
alias Tomcat Server
address 10.5.0.6
notifications_enabled 1
contact_groups admins
check_command check-host-alive
}

define service {
use local-service
host_name todd
service_description ToddSessionsPassive
check_command check_dummy!0 "Available Sessions OK"
passive_checks_enabled 1
active_checks_enabled 0
contact_groups admins
notifications_enabled 1
event_handler_enabled 1
event_handler check_nrpe!grow_todd
}

define service{
use local-service ; Name of service template to use
host_name todd
service_description Todd-Server
check_command check_todd_server
contact_groups admins
notifications_enabled 1
event_handler_enabled 1
event_handler check_nrpe!restart_todd
}

define service{
use local-service ; Name of service template to use
host_name todd
service_description Todd-Number-Sessions
check_command check_todd_sessions
contact_groups admins
notifications_enabled 1
}

# Define a service to check if SSH is running on the host
define service {
use generic-service ; Name of service template to use
host_name todd ; Name of the host to monitor
service_description Check SSH ; A friendly name for the service
check_command check_ssh ; Name of the Nagios plugin to use
contact_groups admins
notifications_enabled 1
}

#check number of users
define service {
use local-service ; Name of service template to use
host_name todd
service_description Check Number Users
check_command check_nrpe!check_users
contact_groups admins
notifications_enabled 1
}

#check CPU load
define service{
use generic-service
host_name todd
service_description CPU Load
check_command check_nrpe!check_load
}

define service{
use generic-service
host_name todd
service_description Zombie Process
check_command check_nrpe!check_zombie_procs
}

define service{
use generic-service
host_name todd
service_description Total Processes
check_command check_nrpe!check_total_procs
}
```

Remember to add the commands that I already used on P1 and P2 and also remember to add the host config to the nagios.cfg.

After that I use docker `compose up`  to run the container you should test, and for that go to the nrpe container and run `./gradlew runMonitor`, and go to the nagios container and run `./gradlew runClient`. You should see the service ToddSessionsPassive going to critical by a passive check sent from the todd server.


# Creating a monitored container with tomcat

This container's configuration closely resembles that of the todd container, so if you notice any missing elements in this explanation, you can refer to the todd container's documentation for further details.

To create the tomcat container, I followed an approach suggested by our professor that involved incorporating `systemctl` within the container, allowing to reproduce the previous work. I obtained the necessary materials from Moodle and customized them to suit our specific requirements.

```
FROM  "ubuntu:18.04"
LABEL __copyright__="(C) Guido Draheim, licensed under the EUPL" \
__version__="1.5.7106"
EXPOSE 80
EXPOSE 8080
RUN apt-get update
RUN apt-get install -y tomcat9 python3 openssh-server sudo
COPY files/docker/systemctl3.py /usr/bin/systemctl
RUN test -L /bin/systemctl || ln -sf /usr/bin/systemctl /bin/systemctl
#tomcat had no permitiion to write to /var/lib/tomcat9
RUN chown -R tomcat:tomcat /var/lib/tomcat9 
RUN systemctl enable tomcat9
RUN systemctl start ssh
RUN systemctl enable ssh
RUN mkdir -p /usr/local
WORKDIR /usr/local
#copy todd_app from the host machine to the container
COPY ./todd_app /usr/local/todd
WORKDIR /usr/local/todd
#INSTALL NRPE
COPY files/plugins/install_nrpe.sh /tmp/install_nrpe.sh
RUN chmod +x /tmp/install_nrpe.sh && \
/tmp/install_nrpe.sh && \
rm -rf /tmp/install_nrpe.sh
#INSTALL SEND_NSCA
COPY files/plugins/install_send_nsca.sh /tmp/install_send_nsca.sh
RUN chmod +x /tmp/install_send_nsca.sh && \
/tmp/install_send_nsca.sh && \
rm -rf /tmp/install_send_nsca.sh
COPY files/scripts/restart_tomcat.sh /usr/local/nagios/libexec/restart_tomcat.sh
RUN sh -c 'echo "CATALINA_OPTS=\"-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6000 -Dcom.sun.management.jmxremote.rmi.port=6000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=10.5.0.7\"" > /usr/share/tomcat9/bin/setenv.sh' && chmod 776 /usr/share/tomcat9/bin/setenv.sh
CMD ["/usr/bin/systemctl"]
```

In the previous dockerfile I installed nrpe and send_nsca with the script that were already used for the todd image, I also copied todd app to the image since I used it to implement the tomcat monitor. At the end of the dockerfile I created a file `setenv.sh` that enables jmx monitoring to the tomcat.

With the dockerfile ready was created a service for tomcat in docker compose:
```yaml
tomcat:
	build: tomcat
	ports:
	  - "8082:8080"
	  - "6000:6000"
	networks:
	  cogsi:
	  ipv4_address: 10.5.0.7
	command: /bin/sh -c "/usr/local/nagios/bin/nrpe -c /usr/local/nagios/etc/nrpe.cfg -d && systemctl start tomcat9 && systemctl start ssh && tail -f /dev/null"
```

After having the new service together with nagios I've created a host.cgf to monitor the tomcat container:

```
# Define a host for the local machine
define host {
use linux-server ; Name of host template to use
host_name tomcat ; Name of the host to monitor
alias Tomcat Server
address 10.5.0.7
notifications_enabled 1
contact_groups admins
check_command check-host-alive
}

define service {
use local-service
host_name tomcat
service_description TomcatPassive
check_command check_dummy!0 "Heap Memory Usage is Stable"
passive_checks_enabled 1
active_checks_enabled 0
contact_groups admins
notifications_enabled 1
event_handler_enabled 1
event_handler check_nrpe!restart_tomcat
}

define service {
use local-service
host_name tomcat
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
host_name tomcat ; Name of the host to monitor
service_description Check SSH ; A friendly name for the service
check_command check_ssh ; Name of the Nagios plugin to use
contact_groups admins
notifications_enabled 1
}

#check number of users
define service {
use local-service ; Name of service template to use
host_name tomcat
service_description Check Number Users
check_command check_nrpe!check_users
contact_groups admins
notifications_enabled 1
}

#check CPU load
define service{
use generic-service
host_name tomcat
service_description CPU Load
check_command check_nrpe!check_load
contact_groups admins
notifications_enabled 1
}

define service{
use generic-service
host_name tomcat
service_description Zombie Process
check_command check_nrpe!check_zombie_procs
contact_groups admins
notifications_enabled 1
}

define service{
use generic-service
host_name tomcat
service_description Total Processes
check_command check_nrpe!check_total_procs
contact_groups admins
notifications_enabled 1
}
```
Don't forget to add this new host to the nagios.cfg.

And with this you should have all the project running on containers now!


# Linux namespaces

Docker strongly rely on linux namespaces to provide container environments, because namespaces isolate Linux processes into their own little system environments. This makes it possible to run a whole range of applications on a single real Linux machine and ensure no two of them can interfere with each other, without having to resort to using virtual machines.

The defenition of namespaces can be: Linux namespaces wrap a global system resource in an abstraction that makes it appear to the processes within the namespace that they have their own isolated instance of the global resource.  Changes to the global resource are visible to other processes that are members of the namespace, but are invisible to other processes. One use of namespaces is to implement containers like we said earlier.


Here are some of the most commonly used namespaces:

1.  Process Isolation (PID Namespace): This namespace isolates the process ID number space, allowing each process to have its unique set of process IDs. It is useful for managing and tracking processes within a container.
2.  Network Interfaces (Net Namespace): This namespace isolates the network stack, including network interfaces, routing tables, and iptables rules. It is useful for running multiple network environments on a single host.
3.  Unix Timesharing System (UTS Namespace): This namespace isolates the hostname and domain name, allowing each process to have its unique hostname. It is useful for running multiple instances of a service on a single host.
4.  User Namespace: This namespace isolates the user and group ID number spaces, allowing each container to have its unique set of users and groups. It is useful for providing containerized applications with different levels of privilege.
5.  Mount (MNT Namespace): This namespace isolates the mount points, allowing each container to have its unique set of mounted file systems. It is useful for providing containerized applications with their own file systems.
6.  Interprocess Communication (IPC): This namespace isolates the System V IPC and POSIX message queues. It is useful for running multiple instances of an application that relies on IPC.
7.  Cgroups: Cgroups are not a namespace but rather a feature that allows the allocation of system resources such as CPU, memory, and I/O to different groups of processes. It is useful for resource management and control in a containerized environment.

Sources to conduct this project 3:

 - https://www.docker.com 
 - https://hub.docker.com
 - https://resources.infosecinstitute.com/topic/how-docker-primitives-secure-container-environments/#:~:text=Docker%20makes%20heavy%20use%20of,Kernel%20are%20namespaces%20and%20cgroups.
 - https://man7.org/linux/man-pages/man7/namespaces.7.html 
 - The lectures recorded by the professor.
