
  
# COGSI P4 Network and System Simulation- Gonçalo Pinho - 1220257

This topic we are suposed to make a network and system simulation with GNS3.
The goal is to simulate a local netowrk with the components from previous exercises (Todd, Nagios and tomcat) and we should use docker containers.

# Notes
Due to the lack of support for arm architecture in GNS3, I had to search for a workaround that leaded to the tutorial of [grossmj](https://github.com/GNS3/gns3-gui/discussions/3261), in this tutorial he showed how to use GNS3 in Mac M1 with the GNS3 server that he created for the purpose. While attempting to use a workaround for the Mac M1, I encountered significant instability and a lack of support. Additionally, the docker images required for the project did not support arm architecture. The attempt of the use GNS3 will be documented in this essay.

As a result, I had to approach my professor to seek alternative evaluation methods. We ultimately created a docker container using Ubuntu that functions as a router with the help of iproute2.

# How to create a router using a container

Making use of everything of the Project 3, I had to make the following adaptations to send the packets though the router:

1. Create the router container with ubunto image, and check if the image in question has net.ipv4.conf.all.forwarding enabled and for that you can use the following command `sysctl net.ipv4.conf.all.forwarding`. The one we are using has this enabled by deafault.

2. Then we needed to create two networks one for the monitor and the other one for the applications and assign for each service the correspondent networks, note that the router has to be on both networks.

3. Define static IPs for every service.

4. If needed change the addresses on the files that require that change (P3 has this all explained).

5. Install iproute2 on each machine, you can do that by changing the already existing dockerfile.

6. Create the route exception for each service so that the nagios can access the applications and vice versa.

For that we created the following docker-compose.yml:

``` yaml

version: '3'

services:

nagios:

cap_add:

- NET_ADMIN

build: .

ports:

- "8080:80"

volumes:

- ./nagios:/opt/nagios/etc

command: /bin/sh -c "ip route add 10.6.0.0/16 via 10.5.0.5 && /usr/local/bin/start_nagios && tail -f /dev/null"

networks:

monitor:

ipv4_address: 10.5.0.3

todd:

build: todd

cap_add:

- NET_ADMIN

ports:

- "10500:10500"

- "3006:3006"

networks:

apps:

ipv4_address: 10.6.0.3

command: /bin/sh -c "ip route add 10.5.0.0/16 via 10.6.0.2 && /usr/local/nagios/bin/nrpe -c /usr/local/nagios/etc/nrpe.cfg -d && systemctl start ToddService && systemctl start ssh && tail -f /dev/null"

tomcat:

build: tomcat

cap_add:

- NET_ADMIN

ports:

- "8082:8080"

- "6000:6000"

networks:

apps:

ipv4_address: 10.6.0.4

command: /bin/sh -c "ip route add 10.5.0.0/16 via 10.6.0.2 && /usr/local/nagios/bin/nrpe -c /usr/local/nagios/etc/nrpe.cfg -d && systemctl start tomcat9 && systemctl start ssh && tail -f /dev/null"

router:

image: ubuntu:latest

command: tail -f /dev/null

cap_add:

- NET_ADMIN

networks:

monitor:

ipv4_address: 10.5.0.5

apps:

ipv4_address: 10.6.0.2

networks:

monitor:

driver: bridge

ipam:

config:

- subnet: 10.5.0.0/16

gateway: 10.5.0.1

apps:

driver: bridge

ipam:

config:

- subnet: 10.6.0.0/16

gateway: 10.6.0.1

```

Now if we stop the router container nagios wont be able to communicate with todd nor tomcat.

  

This part of the P4 was based on the following sources:

https://www.tecmint.com/setup-linux-as-router/

https://stackoverflow.com/questions/34110416/start-container-with-multiple-network-interfaces

https://stackoverflow.com/questions/27708376/why-am-i-getting-an-rtnetlink-operation-not-permitted-when-using-pipework-with-d


# GNS3 on Mac M1  

This part of the assignment I'm going to explain how to make GNS3 work on the Mac M1.

  

**Disclaimer:** This is not supported by GNS3, this was developed by the community and we can have different results.

  

# What is GNS3

![GNS3](https://external-preview.redd.it/KOsXqQEXFoN73ovbEzMeP5Dgy1fqqD3uBj8NjnVws2U.jpg?auto=webp&s=7a3f50b61714dd2b2e5d4cbb4e507a25a403f39b)

[GNS3](https://docs.gns3.com/docs/) is a free and open-source software that provides users with a platform to emulate, configure, test, and troubleshoot virtual and real networks. It enables users to create and run network topologies on their laptops, ranging from simple setups to complex ones. GNS3 is flexible, cost-effective, and valuable for training and education purposes. Its virtual environment simulates real-world network scenarios, making it an essential tool for networking professionals and students to gain practical experience in network design and management.

  

# How to run it in Mac M1

In [this](https://github.com/GNS3/gns3-gui/discussions/3261) github discussion we can see a tutorial in how to use GNS3 on Mac M1 and basically this was the one that I've followed.

  

The user that created the discussion has been updating his GNS3 server version to keep up with the GNS3 software version and you can see it [here](https://github.com/GNS3/gns3-gui/releases). In this case I've downloaded the v2.2.38.

  

First download from the gns3 [website](https://www.gns3.com/software/download) the software, note that this will force the MacOS to emulate the software since they don't support the ARM version of it, as it was said earlier you may experience loss of performance and a faster drainage of battery.

  

If you get a error on the first run click [here](https://gns3.com/community/discussions/install-error-macos-ventura).

  

Now you need to install VMWare fusion, I have a tutorial on P1 in how to download it if you have any doubts just go there and follow the tutorial.

  

After having the GNS3 software and VMware fusion download the virtual machine that correspond to your GNS3 version you can check your version if you open the GNS3 and click on GNS3>About.

  

After downloading it, create a custom virtual machine, the OS that you need to choose is Ubuntu ARM 64-bit then you want to use an existing virtual disk and from the ones that you've downloaded you pick the DISK1 and click on finish, as it is shown on the following images.

  

![Create a custom VM](https://user-images.githubusercontent.com/4752614/142552930-f49b20e0-568d-4394-a0c3-dd35044c7dcb.png)

Author of the image is [grossmj](https://github.com/grossmj).

![Choose OS](https://user-images.githubusercontent.com/4752614/142553094-a0a998e0-0d83-4c2b-b493-51165d27dffc.png)

Author of the image is [grossmj](https://github.com/grossmj).

![Pick a Virtual disk](https://user-images.githubusercontent.com/4752614/142553414-64e354b4-82f2-4221-bef1-2752f1b584ca.png)

Author of the image is [grossmj](https://github.com/grossmj).

![Selecting disk1](https://user-images.githubusercontent.com/4752614/142553834-2e25276a-8ad2-4602-9e27-53c2a94ec2cd.png)

Author of the image is [grossmj](https://github.com/grossmj).

![Summary and finish](https://user-images.githubusercontent.com/4752614/142554107-ef523274-8d98-4b80-b455-a0d1d37dbed6.png)

Author of the image is [grossmj](https://github.com/grossmj).

Then you go to the VM settings and you need to add a new hard drive to it, that's the DISK2 that you've downloaded.

![VM settings](https://user-images.githubusercontent.com/4752614/142554318-34e9a9fe-8eda-4125-b333-47819dfcfadb.png)

Author of the image is [grossmj](https://github.com/grossmj).

![Add a existing hard disk](https://user-images.githubusercontent.com/4752614/142554443-32283ff3-4b25-4602-9492-d544f4169729.png)

Author of the image is [grossmj](https://github.com/grossmj).

![Selecting disk2](https://user-images.githubusercontent.com/4752614/142554473-7d31dbcd-3dcd-4256-86fc-1a1c266ce4ff.png)

Author of the image is [grossmj](https://github.com/grossmj).

![Summary and apply](https://user-images.githubusercontent.com/4752614/142554737-33801b2e-8e0d-45dc-bbe8-5dd6c347cefc.png)

Author of the image is [grossmj](https://github.com/grossmj).

Then you can start the VM and you should see something like this.

![Server up and running](https://user-images.githubusercontent.com/4752614/142555198-de3402f9-26ee-41b2-96ad-7f6672615672.png)

Author of the image is [grossmj](https://github.com/grossmj).

  

The next step is oppening the GNS3 and and connecting to the server and to avoid problems create a new project. Then go to GNS3>Preferences>Server>Remote Server.

  

Then you should click on add insert a name, host and port and click on ok as you can see on the following image.

  

![Remote server details](https://i.imgur.com/jL8GxTI.png)

After that you should be connected to the server and now you can use GNS3.

The main problem here was due to the fact that I needed some specific images such as [webterm](https://hub.docker.com/r/gns3/webterm) that doesn't support arm architecture and I couldn't even access the machine. I tried to use VPS and it seemed to work fine. And sometimes I would get stuck on the loading when import my previous project.