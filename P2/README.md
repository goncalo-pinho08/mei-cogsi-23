

# COGSI P2 - Gonçalo Pinho - 1220257

  

For this second part of the project (P2) that is Application Monitoring , for which is mandatory the use of JMX and [Nagios](https://www.nagios.org/) . It is worth noting that this project is being carried out on a Macbook Air M1 with an Apple Silicon Processor, 256GB of storage, and 16GB of RAM, which may result in some differences in certain aspects.

  

# What is JMX?

Java Management Extensions (JMX) framework has been widely adopted as a user-friendly infrastructure solution to help manage both remote and local Java applications. Since JMX introduced the concept of MBeans, it helped to revolutionize Java application management and bring real-time management tools into the picture.JMX is a public specification and many vendors of commonly used monitoring products support it.

![JMX Architecture](https://www.devopsschool.com/blog/wp-content/uploads/2021/05/what-is-jmx-architecture.png)

As we can see on the previous image, JMX architecture follows a three-layered approach:
 - **Instrumentation layer:** MBeans registered with the JMX agent through
   which resources are managed;
 - **JMX agent layer:** the core component (MbeanServer) which maintains registry of managed MBeans and provides an interface to access them; 
 - **Remote management layer:** usually client side tool like JConsole;

  

# Before starting notes


Before commencing the actual project, I made the decision to clean up the code. This was necessary as each virtual machine (VM) had too many provision commands, making it difficult to understand. To address this issue, I opted to provision ".sh" files instead of inserting the commands directly. The command used for this purpose was as follows:

`server.vm.provision "shell", path: "scripts/server/install_nrpe.sh"` 

In addition, I had prior experience with the project and knew that I couldn't afford to have the IP address change each time I restarted the machine. To tackle this challenge, I devised a solution based on the topic created by fellow student Vicente Oliveira (ID: 1220286). I defined a static MAC address and utilized arp-scanner to find the IP address of the machines using the MAC address. I then set two environment variables, namely MONITOR_ADDRESS and SERVER_ADDRESS, in each VM. These variables would be instrumental in adapting the project implementation, as I will explain further.

However, it's worth mentioning that this task was time-consuming and the commands differed depending on the macOS version being used. In my case, I am currently on Ventura 13.3.1.

# Preparing the Todd application on both machines
In order to monitor the Todd server, some preparations needed to be made on the server machine. To simplify this process, I created a shell script, as defining static IP addresses on Mac M1 was not possible and required a workaround for testing purposes.


**Server Machine:**
On the server machine, the shell script I created copied the Todd project to the machine and installed arp-scan. It also assigned the values of environment variables to the IP addresses of both machines. Additionally, I used the 'sed' command to change the IP address in relevant files. Finally, I built the application and set it up as a service using the code provided by the professor. This allowed for smooth implementation of the Todd project on the server machine.

```
#!/bin/sh
echo  "Script to define the addresses of the machines"
cp -a  /vagrant/todd  /home/vagrant/mei-isep-todd-aa22bb1bcc52
cd  /home/vagrant/mei-isep-todd-aa22bb1bcc52/
sudo apt-get  install  -y  arp-scan
export  MONITOR_ADDRESS=$(sudo arp-scan --interface=eth0 --localnet | awk '/08:00:27:e0:e0:e0/ {print $1}')
export  SERVER_ADDRESS=$(ip a | awk '/inet / && !/127.0.0.1/ {gsub(/\/.*/, "", $2); print $2}')
echo  $MONITOR_ADDRESS
echo  $SERVER_ADDRESS
sudo sed  -i  "s/^allowed_hosts=.*/allowed_hosts=127.0.0.1,$MONITOR_ADDRESS/g"  /usr/local/nagios/etc/nrpe.cfg
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddServerStatus.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddAvailableSessionsStatus.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS:10500\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitor.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS:10500\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp2.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS:10500\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp3.java
sudo sed  -i  "/String hostaddress/c\ String hostaddress = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitorHandler.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS:6000\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXTomcatHeapMemory.java
sudo sed  -i  "/String hostaddress/c\ String hostaddress = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXTomcatHeapMemoryMonitorHandler.java
sudo sed  -i  "s/args = \['[^']*:10500'\]/args = \['$SERVER_ADDRESS:10500'\]/g"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
sudo sed  -i  "s/args = \['[^']*:6000'\]/args = \['$SERVER_ADDRESS:6000'\]/g"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
sudo sed  -i  "s/args = \['[^']*'\]/args = \['$SERVER_ADDRESS:10500'\]/g"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
./gradlew jar
sudo mkdir  /usr/local/todd
sudo cp  ./build/libs/mei-isep-todd-aa22bb1bcc52-1.0.1.jar  /usr/local/todd/todd.jar
sudo cp  /vagrant/files/ToddService.service  /etc/systemd/system/ToddService.service
sudo cp  /vagrant/files/ToddService.sh  /usr/local/todd/ToddService.sh
#not working and i dont know why will change manually
#sudo sed -i "s/-Djava.rmi.server.hostname=[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+/-Djava.rmi.server.hostname=$SERVER_ADDRESS/g" /vagrant/files/ToddService.sh
sudo chmod  +x+r  /etc/systemd/system/ToddService.service
sudo chmod  +x  /usr/local/todd/ToddService.sh
sudo systemctl  start  ToddService
sudo systemctl  enable  ToddService
sudo systemctl  restart  nrpe.service
```

** Monitor machine**
The same approach was followed for the monitor machine, as demonstrated in the following code:
```
#!/bin/sh
echo  "Script to define the addresses of the machines"
cp -a  /vagrant/todd  /home/vagrant/mei-isep-todd-aa22bb1bcc52
cd  /home/vagrant/mei-isep-todd-aa22bb1bcc52/
sudo apt-get  install  -y  arp-scan
export  MONITOR_ADDRESS=$(ip a | awk '/inet / && !/127.0.0.1/ {gsub(/\/.*/, "", $2); print $2}')
export  SERVER_ADDRESS=$(sudo arp-scan --interface=eth0 --localnet | awk '/08:00:27:e0:e0:e2/ {print $1}')
echo  $MONITOR_ADDRESS
echo  $SERVER_ADDRESS
sudo sed  -i  "/address/c\address $SERVER_ADDRESS"  /usr/local/nagios/etc/objects/server.cfg
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddServerStatus.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXToddAvailableSessionsStatus.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS:10500\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitor.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS:10500\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp2.java
sudo sed  -i  "/String server/c\ String server = \"$SERVER_ADDRESS:10500\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/ClientApp3.java
sudo sed  -i  "/String hostaddress/c\ String hostaddress = \"$SERVER_ADDRESS\";" /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitorHandler.java
sudo sed  -i  "s/args = \['[^']*:10500'\]/args = \['$SERVER_ADDRESS:10500'\]/g"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
sudo sed  -i  "s/args = \['[^']*'\]/args = \['$SERVER_ADDRESS'\]/g"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/build.gradle
sudo ./gradlew  jar
sudo cp  /home/vagrant/mei-isep-todd-aa22bb1bcc52/build/libs/mei-isep-todd-aa22bb1bcc52-1.0.1.jar  /usr/local/nagios/libexec/todd-1.0.1.jar
sudo chmod  a+x+r  /usr/local/nagios/libexec/todd-1.0.1.jar
sudo systemctl  restart  nagios.service
```
Note: Both .sh files are in its final version meaning it has all project required changes.

In order to execute the .sh files at the end of the machine startup, a workaround was required as the arp-scan needed both machines to be operational. One approach was to copy the files to the virtual machines (VMs) and then execute the .sh files. If using the provided folder structure and being inside the machine, the following commands can be executed:

** On the monitor machine **
```
cp /vagrant/scripts/monitor/todd_monitor.sh /home/vagrant/todd_monitor.sh
source todd_monitor.sh
```
** On the server machine **
```
cp /vagrant/scripts/server/todd_server.sh /home/vagrant/todd_server.sh
source todd_server.sh
```

After running these commands, there was one file, ToddServer.sh, that couldn't be changed. Attempts to modify it were unsuccessful. To work around this, the following steps were taken on the server machine:

``sudo nano /usr/local/todd/ToddService.sh``

Then, the occurrences of "-Djava.rmi.server.hostname=" in the file were located and changed to the appropriate IP address (server machine address). Finally, the ToddService was restarted with the command:

After that just restart the ToddService by:
``sudo systemctl restart ToddService.service ``


# Check if the Todd server is up
In this task, we utilized a similar approach to a previous project (P1), but instead of using NRPE for checking the status of the Todd Server, we employed JMX. By leveraging MBServer, we were able to remotely monitor the status of the Todd Server. However, in order to achieve this, some preparation was required to configure the Todd application accordingly. 

```java
public  class  JMXToddServerStatus {

/**
* @param  args
*/

public  static  void  main(String[] args) {
try {
String  server = "192.168.64.215";
if (args.length >= 1) {
server = args[0];
}
// Connect to a remote MBean Server
JMXConnector  c = javax.management.remote.JMXConnectorFactory
.connect(new  JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+server+":10500/jmxrmi"));

  

MBeanServerConnection  mbs = c.getMBeanServerConnection();
// Lets try to access the MBean net.jnjmx.todd.Server:
ObjectName  son = new  ObjectName("todd:id=Server");
ObjectInstance  ob=mbs.getObjectInstance(son);
System.out.println("Todd server is UP");
c.close();
System.exit(0);
} catch (Exception  ex) {
System.out.println("Error: unable to connect to MBean Server");
System.exit(2);
}
}
}
```

The existing code allows us to obtain an object and monitor it. Based on whether we are able to access the object or not, the code will exit with a specific code that is understood by Nagios. Specifically, an exit code of 0 will indicate an "OK" status, while an exit code of 2 will indicate a "CRITICAL" status. It's important to note that this code was already included in the project and serves the purpose of providing appropriate output for Nagios based on the monitoring results.

After that we can create a command on nagios to check the todd server, and for that we've added the following to the commands.cfg:
```
define command{
command_name check_todd_server
command_line java -cp /usr/local/nagios/libexec/todd-1.0.1.jar net.jnjmx.todd.JMXToddServerStatus
}
```

The command "java -cp /usr/local/nagios/libexec/todd-1.0.1.jar net.jnjmx.todd.JMXToddServerStatus" is used to execute a Java class from the "todd-1.0.1.jar" JAR file with the main class "net.jnjmx.todd.JMXToddServerStatus".

This Java class is responsible for checking the status of the Todd Server using JMX (Java Management Extensions). It communicates with the Todd Server via JMX to monitor the overall status of the server.

The output of this command will provide information about the status of the Todd Server, which will be used to determine the exit code for Nagios, indicating the overall status of the Todd Server (OK, CRITICAL, etc.) based on the monitoring results.

After obtaining the necessary command to monitor the Todd Server, the next step is to add a service definition in the Nagios host configuration file (host.cfg). This service definition includes the use of the "local-service" template, the hostname of the server being monitored, a service description for Todd-Server, the check command to be used (check_todd_server), the contact groups to be notified (admins), and notifications enabled (1).

```
define service{
	use local-service
	host_name server
	service_description Todd-Server
	check_command check_todd_server
	contact_groups admins
	notifications_enabled 1
}
```

Once the service definition is added, Nagios needs to be restarted to apply the changes. After restarting, the status of the Todd-Server service should be shown as OK if the monitoring is set up correctly. With the Todd Server now being monitored, the next step is to add an event handler that will attempt to restart the server if it goes down.

For this purpose, a shell script is created that attempts to restart the ToddService using the "sudo service ToddService restart" command. If the restart is successful, a message stating "ToddService Restarted Successfully" is displayed and the script exits with a status code of 0. Otherwise, if the restart fails, a message stating "Can't Restart ToddService" is displayed and the script exits with a status code of 2.

```
if sudo service  ToddService  restart; then
echo  "ToddService Restarted Successfuly"
exit  0
else
echo  "Can't Restart ToddService"
exit  2
fi
```


To enable remote execution of this script, a command definition is added to the NRPE (Nagios Remote Plugin Executor) configuration file (nrpe.cfg), specifying the location of the restart script.

```
command[restart_todd]=/usr/local/nagios/etc/scripts/restart_todd.sh
```

After adding the command definition, NRPE needs to be restarted to apply the changes. Then, the previously created service definition in the host.cfg file needs to be edited to enable event handling and specify the event handler command as "check_nrpe!restart_todd". Finally, Nagios needs to be restarted to apply the changes.

```
event_handler_enabled 1
event_handler check_nrpe!restart_todd
```

To test the event handling, the ToddService on the server machine can be intentionally stopped. If everything is set up correctly, Nagios should detect the service as down, trigger the event handler to attempt a restart using the remote script, and display the appropriate status for the Todd-Server service.

# Check available sessions

This will be the same as the last check, but this time there's no event handler. So for that we need to create a java class to use MBeans to check Todd server number of sessions and depending on the result will use the nagios known outputs.

``` java
public  class  JMXToddAvailableSessionsStatus { 
/**
* @param  args
*/
public  static  void  main(String[] args) {
try {
String  server = "192.168.64.215";
if (args.length >= 1) {
server = args[0];
}

  

// Connect to a remote MBean Server
JMXConnector  c = javax.management.remote.JMXConnectorFactory
.connect(new  JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+server+":10500/jmxrmi"));

MBeanServerConnection  mbs = c.getMBeanServerConnection();

// Lets try to access the MBean net.jnjmx.todd.SessionPool:
ObjectName  son = new  ObjectName("todd:id=SessionPool");
ObjectInstance  ob=mbs.getObjectInstance(son);
Integer  sessions=(Integer)mbs.getAttribute(son, "AvailableSessions");
System.out.println("AvailableSessions=" + sessions);
Integer  size=(Integer)mbs.getAttribute(son, "Size");
System.out.println("Size=" + size);
if ((sessions) < 0.2*size) {
c.close();
System.out.println("Critical: AvailableSessions below 20%!");
System.exit(2);
}
System.out.println("OK, Avaliable Sessions above 20%");
c.close();
System.exit(0);
} catch (Exception  ex) {
System.out.println("Error: unable to connect to MBean Server");
System.exit(2);
}
}
}
```


This Java code serves as a basic JMX (Java Management Extensions) client that establishes a connection to a remote MBean (Managed Bean) server using RMI (Remote Method Invocation) to retrieve the values of two attributes from an MBean named "net.jnjmx.todd.SessionPool". The attributes in question, assumed to be of type Integer, are "AvailableSessions" and "Size".

The code begins by attempting to establish a connection with the ToddServer. Following this, it utilizes the ObjectName class to create an object name for the MBean "net.jnjmx.todd.SessionPool" with an ID of "SessionPool". It then retrieves the ObjectInstance for this MBean using the getObjectInstance() method, although this ObjectInstance is not utilized in the subsequent code.

Next, the code retrieves the values of the "AvailableSessions" and "Size" attributes from the MBean using the getAttribute() method of the MBeanServerConnection object, and stores them in Integer variables labeled "sessions" and "size", respectively. These attribute values are then printed to the console using System.out.println().

Subsequently, the code checks if the "AvailableSessions" attribute is less than 20% of the "Size" attribute. If this condition is met, a critical error message is printed to the console, the JMXConnector is closed, and the code exits with a status code of 2. If the condition is not met, an OK message is printed to the console, the JMXConnector is closed, and the code exits with a status code of 0.

In the event that any exceptions occur during the code's execution, an error message is printed to the console, and the code exits with a status code of 2.

To monitor this code, a command needs to be added to Nagios, along with the creation of a service in the host config. The following command can be added to the commands file:
```bash
define command{
command_name check_todd_sessions
command_line java -cp /usr/local/nagios/libexec/todd-1.0.1.jar net.jnjmx.todd.JMXToddAvailableSessionsStatus
}
```
Following this, the service can be created as follows:
```
define service{
use local-service ; Name of service template to use
host_name server
service_description Todd-Number-Sessions
check_command check_todd_sessions
contact_groups admins
notifications_enabled 1
}
```

After restarting Nagios, the number of sessions of the Todd Server can be viewed in the monitoring system.

# JMX Notifications TODD

The task given was to create a JMX Agent Application that can utilize JMX notifications to respond to "events" occurring in a monitored resource. Specifically, we were required to develop an Agent to monitor the SessionPool and trigger the "grow" method if the "Available sessions" attribute falls below 20%.

For that, changed the provided code for the following:

JMXAvailableSessionsMonitor.java

```java
public  static  void  configureMonitor(MBeanServerConnection  mbs) throws  Exception {
// Get the actual value of the Size
ObjectName  son = new  ObjectName("todd:id=SessionPool");
ObjectInstance  ob=mbs.getObjectInstance(son);
Integer  size=(Integer)mbs.getAttribute(son, "Size");
Integer  lowValue=(int)(0.2*size);
//System.out.println("Size=" + size);
ObjectName  spmon = new  ObjectName("todd:id=AvailableSessionsMonitor");

Set<ObjectInstance> mbeans = mbs.queryMBeans(spmon, null);

if (mbeans.isEmpty()) {
mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);
} else {
// nothing to do...
}

  

AttributeList  spmal = new  AttributeList();
spmal.add(new  Attribute("ObservedObject", new  ObjectName("todd:id=SessionPool")));
spmal.add(new  Attribute("ObservedAttribute", "AvailableSessions"));
spmal.add(new  Attribute("GranularityPeriod", 1000L)); // For each second
spmal.add(new  Attribute("NotifyHigh", false)); //this way we only get notifications when the value goes below the threshold
spmal.add(new  Attribute("NotifyLow", true));
mbs.setAttributes(spmon, spmal);

mbs.invoke(spmon, "setThresholds", new  Object[] { lowValue, lowValue },
new  String[] { "java.lang.Number", "java.lang.Number" });
mbs.addNotificationListener(spmon, new  JMXAvailableSessionsMonitorHandler(), null, null); //creadted a listener that will handle the notifications
mbs.invoke(spmon, "start", new  Object[] {}, new  String[] {});
}

/**
* @param  args
* @throws  IOException
*/
public  static  void  main(String[] args) throws  IOException {

System.out.println("Todd JMXAvailableSessionsMonitor... Accessing JMX Beans (using JMX Notifications with TODD MBeans)");

try {
String  server = "192.168.64.215:10500";
if (args.length >= 1) {
server = args[0];
}
System.out.println("Connecting to TODD server at "+server+" ...");
// Connect to a remote MBean Server
JMXConnector  c = javax.management.remote.JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + server + "/jmxrmi"));
MBeanServerConnection  mbs = c.getMBeanServerConnection();
System.out.println("Setting up notification handlers...");
// Set a Notification Handler
configureMonitor(mbs);
// mbs.addNotificationListener(new ObjectName("todd:id=SessionPool"), new
// JMXNotificationListener(), null, null);
// Thread.sleep(100000);
System.out.print("Monitoring");
while (true) {
Thread.sleep(5000);
System.out.print(".");
}
// c.close();
} catch (Exception  ex) {
System.out.println("Error: unable to connect to MBean Server");
}
}
}
```

The provided code implements a monitor that generates a notification when the value of "available sessions" in the SessionPool falls below 20%. This notification serves as an event trigger, which can be captured and processed by a JMX Agent Application to take appropriate action, such as invoking the "grow" method to increase the number of available sessions in the SessionPool.

Here's the notification handler:
```java
public  class  JMXAvailableSessionsMonitorHandler  implements  NotificationListener {
@Override
public  void  handleNotification(Notification  notification, Object  handback) {
System.out.println("Received Notification");
System.out.println("======================================");
System.out.println("Timestamp: " + notification.getTimeStamp());
System.out.println("Type: " + notification.getType());
System.out.println("Sequence Number: " + notification.getSequenceNumber());
System.out.println("Message: " + notification.getMessage());
System.out.println("User Data: " + notification.getUserData());
System.out.println("Source: " + notification.getSource());
System.out.println("======================================");

// Lets restart the monitoring
// But before lets read again the observed value...
String  server = "192.168.64.215:10500";
JMXConnector  c;
try {
c = javax.management.remote.JMXConnectorFactory.connect(new  JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + server + "/jmxrmi"));
MBeanServerConnection  mbs = c.getMBeanServerConnection();
ObjectName  son = new  ObjectName("todd:id=SessionPool"); // The name of the MBean
mbs.invoke(son, "grow", new  Integer[] {new  Integer(4)}, new  String[] {"int"}); // The name of the operation and its signature
System.out.println("Grow Invoked");
c.close();
} catch (IOException  e) {
// TODO Auto-generated catch block
e.printStackTrace();
} catch (MalformedObjectNameException  e) {
// TODO Auto-generated catch block
e.printStackTrace();
} catch (InstanceNotFoundException  e) {
// TODO Auto-generated catch block
e.printStackTrace();
} catch (MBeanException  e) {
// TODO Auto-generated catch block
e.printStackTrace();
} catch (ReflectionException  e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
}
}
```

After this we need to add a build.gradle task to run the monitor:

```
task runMonitor(type:JavaExec, dependsOn: classes) {
main = 'net.jnjmx.todd.JMXAvailableSessionsMonitor'
if (project.hasProperty("appArgs")) {
args Eval.me(appArgs)
}
else {
args = ['192.168.64.215:10500']
}
classpath = sourceSets.main.runtimeClasspath
}
```

If we rebuild the application and run the command ``./gradlew runMonitor``it will start the monitor and when the available sessions is below 20% will trigger a notification. (To test this on the other machine run ./gradlew runClient)

Now that we have the monitor and the notification when the notification is triggered i need to execute the grow method, for that im going to use NSCA.

![NSCA](https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/images/nsca.png)

NSCA is a component of the Nagios monitoring system that allows external applications to send monitoring results to Nagios for processing. It uses a secure communication protocol to transmit monitoring data from external applications to Nagios, making it suitable for integrating custom or third-party monitoring scripts with Nagios.
As we can see on the previous image, this works the other way around of NRPE (used on P1). We will use send_nsca to execute a command on nagios machine that will execute the grow method.


When attempting to install NSCA using the command `sudo apt-get install nsca`, version 2.9.2 is installed by default. However, after conducting research and tests, it was found that this version did not work. As a solution, version 2.10.2 was installed from [Github](https://github.com/NagiosEnterprises/nsca).

To install version 2.10.2, the following steps were taken on the monitoring system:

1.  Install git: `sudo apt-get install git`
2.  Clone the NSCA repository from Github: `git clone https://github.com/NagiosEnterprises/nsca.git`
3.  Navigate to the NSCA directory: `cd /tmp/nsca`
4.  Configure the installation for your system: `sudo ./configure --build=arm-linux-gnueabihf`
5.  Compile and install NSCA: `sudo make install`
6.  Copy the NSCA binary to the Nagios bin directory: `sudo cp /tmp/nsca/src/nsca /usr/local/nagios/bin/`
7.  Copy the sample NSCA configuration file to the Nagios etc directory: `sudo cp /tmp/nsca/sample-config/nsca.cfg /usr/local/nagios/etc/`
8.  Start NSCA: `/usr/local/nagios/bin/nsca -c /usr/local/nagios/etc/nsca.cfg`

After installing NSCA, some configurations needed to be changed in the `nsca.cfg` file. These include adding the Nagios machine's address to the `server_address` line, changing `debug` from 0 to 1 for easier debugging, and verifying that the `command_file` path is correct.

Additionally, the `nagios.cfg` file on the server should be checked to ensure that passive checks and external commands are allowed.
On the nsca client (on the todd server machine):

1.  Install git: `sudo apt-get install git`
2.  Clone the NSCA repository from Github: `git clone https://github.com/NagiosEnterprises/nsca.git`
3.  Navigate to the NSCA directory: `cd /tmp/nsca`
4.  Configure the installation for your system: `sudo ./configure --build=arm-linux-gnueabihf`
5.  Compile and install NSCA: `sudo make install`
6.  Copy the send_nsca binary to the Nagios bin directory: `sudo cp /tmp/nsca/src/send_nsca /usr/local/nagios/bin/`
7.  Copy the sample send_nsca configuration file to the Nagios etc directory: `sudo cp /tmp/nsca/sample-config/send_nsca.cfg /usr/local/nagios/etc/`

after this if you didn't add any password to test just:
```
sudo echo -e "server;exemple;2;KnockKnock" | /usr/local/nagios/bin/send_nsca -H 192.168.64.217 -d ";" -c /usr/local/nagios/etc/send_nsca.cfg
```
Now that we have NSCA working, we should be able to send the passive check when the session hits below 20% and when the service is critical have a event handler that grows the application max session size. For that we need to:

Create a service:
```
define service {
use local-service
host_name server
service_description ToddSessionsPassive
check_command check_dummy!0 "Available Sessions OK"
passive_checks_enabled 1
active_checks_enabled 0
contact_groups admins
notifications_enabled 1
event_handler_enabled 1
event_handler grow_todd
}
```

That check_command check_dummy is a default nagios command that wont make nagios use pooling to check it's state it only works by passive_checks.

Now create the grow_todd command:
```
define command{
	command_name grow_todd
	command_line java -cp /usr/local/nagios/libexec/todd-1.0.1.jar net.jnjmx.todd.JMXToddServerGrow
}
```

Now create the class that executes the method grow on todd application:
```java
public  class  JMXToddServerGrow {
/**
* @param  args
*/
public  static  void  main(String[] args) {
try {
String  server = "192.168.64.220";
if (args.length >= 1) {
server = args[0];
}
// Connect to a remote MBean Server
JMXConnector  c = javax.management.remote.JMXConnectorFactory
.connect(new  JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+server+":10500/jmxrmi"));
MBeanServerConnection  mbs = c.getMBeanServerConnection();
// Lets try to access the MBean net.jnjmx.todd.Server:
ObjectName  son = new  ObjectName("todd:id=SessionPool");
ObjectInstance  ob=mbs.getObjectInstance(son);
mbs.invoke(son, "grow", new  Object[] {new  Integer(2)} , new  String[] {"int"}); //the first argument is the
System.out.println("Grow method was invoked");
c.close();
System.exit(0);
} catch (Exception  ex) {
System.out.println("Error: unable to connect to MBean Server");
System.exit(2);
}
}
}
```

Now on the notification handler that we have created earlier we need to add the following:
```java
String  hostaddress = "192.168.64.217";

String  command = "echo -e \"server;ToddSessionsPassive;2;Session Number is below 20\" | /usr/local/nagios/bin/send_nsca -H "+hostaddress+" -d \";\" -c /usr/local/nagios/etc/send_nsca.cfg";
try {
// Execute the command
Process  process = Runtime.getRuntime().exec(new  String[]{"bash", "-c", command});
// Wait for the command to finish and print the output
BufferedReader  reader = new  BufferedReader(new  InputStreamReader(process.getInputStream()));
String  line;
while ((line = reader.readLine()) != null) {
System.out.println(line);
}
// Check the exit status of the command
int  exitCode = process.waitFor();
if (exitCode != 0) {
System.err.println("Command failed with exit code " + exitCode);
}
} catch (IOException | InterruptedException  e) {
e.printStackTrace();
}
```

If you are using mac you need to add more things to the todd installer shell script:
```bash
sudo sed  -i  "/String hostaddress/c\ String hostaddress = \"$SERVER_ADDRESS\";"  /home/vagrant/mei-isep-todd-aa22bb1bcc52/src/main/java/net/jnjmx/todd/JMXAvailableSessionsMonitorHandler.java
```

Having all of this you should rebuild the application (on both machines) and restart nagios.
Every time that you restart nagios you need to give all users access to that nagios file so that you can receive passive checks:
```sudo chmod 777 /usr/local/nagios/var/rw/nagios.cmd```




# JMX Notification Tomcat

For this part of the project i've followed [this tutorial](https://geekflare.com/enable-jmx-tomcat-to-monitor-administer/).

First we need to go to the tomcat9 instalation directory and create a file named setenv.sh, and its content should be:
```
CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6000 -Dcom.sun.management.jmxremote.rmi.port=6000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=<tomcat-address>"
```

then you should give permissions to the file:
```
sudo chmod 775 setenv.sh
```

Then restart tomcat and you should be able to go to the jconsole and insert <machine-address>:6000 and then you can see the tomcat info.

For the next step ill be using the todd application to make the JMX notification but you can create a new application (and that is how it is suposed to be done).

First create a monitor:

```java  
public class JMXTomcatHeapMemory {  
  
   public static void configureMonitor(MBeanServerConnection mbs) throws Exception {  
      MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();  
  System.out.println("Heap memory usage: used = " + heapMemoryUsage);  
  ObjectName memoryBean = new ObjectName("java.lang:type=Memory");  
  
  GaugeMonitor memoryMonitor = new GaugeMonitor();  
  
  memoryMonitor.addObservedObject(memoryBean);  
  memoryMonitor.setObservedAttribute("HeapMemoryUsage");  
  memoryMonitor.setGranularityPeriod(1000);  
  memoryMonitor.setNotifyHigh(true);  
  memoryMonitor.setNotifyLow(false);  
  
  // Set high and low threshold values  
  memoryMonitor.setThresholds(new Long(1073741824L), new Long(104857600L));  
  
  MBeanServer server = ManagementFactory.getPlatformMBeanServer();  
  server.registerMBean(memoryMonitor, new ObjectName("com.example:type=MemoryMonitor"));  
  
  server.addNotificationListener(new ObjectName("com.example:type=MemoryMonitor"),  
 new JMXTomcatHeapMemoryMonitorHandler(), null, null);  
  
  memoryMonitor.start();  
  
  }  
  
   /**  
 * @param args  
  * @throws IOException  
 */  public static void main(String[] args) throws IOException {  
  
      System.out.println("Todd JMXAvailableSessionsMonitor... Accessing JMX Beans (using JMX Notifications with TODD MBeans)");  
  
 try {  
  
         String server = "192.168.64.217:6000";  
  
 if (args.length >= 1) {  
            server = args[0];  
  }  
  
         System.out.println("Connecting to TODD server at "+server+" ...");  
  
  // Connect to a remote MBean Server  
  JMXConnector c = javax.management.remote.JMXConnectorFactory  
               .connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + server + "/jmxrmi"));  
  
  MBeanServerConnection mbs = c.getMBeanServerConnection();  
  
  System.out.println("Setting up notification handlers...");  
  
  // Set a Notification Handler  
  configureMonitor(mbs);  
  
  System.out.print("Monitoring");  
 while (true) {  
            Thread.sleep(5000);  
  System.out.print(".");  
  }  
  
         // c.close();  
  } catch (Exception ex) {  
         System.out.println(ex);  
  System.out.println("Error: unable to connect to MBean Server");  
  }  
   }  
  
}
```

This code sets up a JMX monitor to monitor the heap memory usage of a Java Virtual Machine (JVM).

The `configureMonitor()` method creates a `GaugeMonitor` object and sets its observed object and attribute to the `MemoryMXBean` and "HeapMemoryUsage" respectively. It then sets the granularity period to 1000 milliseconds, meaning that the monitor will check the memory usage every second. It also sets the high threshold to 1073741824 bytes (which is 1 GB) and the low threshold to 104857600 bytes (which is 100 MB), and sets the high notification to true and low notification to false, which means that the monitor will only notify when the memory usage goes above the high threshold.

The `main()` method connects to a remote JMX server, retrieves the `MBeanServerConnection`, and calls the `configureMonitor()` method to set up the JMX monitor. It then enters an infinite loop, printing a dot every 5 seconds to show that it is still running.

When the monitored heap memory usage exceeds the high threshold, the `JMXTomcatHeapMemoryMonitorHandler` class is notified, which can be implemented to handle the notification in a customized way.

Then i've created the Monitor handler to send the notification:
```java
public class JMXTomcatHeapMemoryMonitorHandler implements NotificationListener {  
  
    @Override  
  public void handleNotification(Notification notification, Object handback) {  
        System.out.println("Received Notification");  
  System.out.println("======================================");  
  System.out.println("Timestamp: " + notification.getTimeStamp());  
  System.out.println("Type: " + notification.getType());  
  System.out.println("Sequence Number: " + notification.getSequenceNumber());  
  System.out.println("Message: " + notification.getMessage());  
  System.out.println("User Data: " + notification.getUserData());  
  System.out.println("Source: " + notification.getSource());  
  System.out.println("======================================");  
  
  String hostaddress = "192.168.64.217";  
  
  String command = "echo -e \"server;TomcatPassive;2;Heap Memory usage is high\" | /usr/local/nagios/bin/send_nsca -H "+hostaddress+" -d \";\" -c /usr/local/nagios/etc/send_nsca.cfg";  
  
 try {  
            // Execute the command  
  Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});  
  
  // Wait for the command to finish and print the output  
  BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));  
  String line;  
 while ((line = reader.readLine()) != null) {  
                System.out.println(line);  
  }  
  
            // Check the exit status of the command  
  int exitCode = process.waitFor();  
 if (exitCode != 0) {  
                System.err.println("Command failed with exit code " + exitCode);  
  }  
        } catch (IOException | InterruptedException e) {  
            e.printStackTrace();  
  }  
    }  
}
```

This code defines a Java class called "JMXTomcatHeapMemoryMonitorHandler" that implements the "NotificationListener" interface. This means that it can handle notifications that are sent by other parts of the code.

When this class receives a notification, it prints out various details about the notification, such as its timestamp, type, sequence number, message, user data, and source. It then constructs a command that will be executed using the "Runtime.getRuntime().exec()" method. This command sends a message to a Nagios monitoring system that alerts it when the heap memory usage is high.

The command is constructed using a string that contains the message to be sent, the IP address of the Nagios server, and the location of the configuration file for the "send_nsca" command. The command is executed using the "Runtime.getRuntime().exec()" method, which creates a new process that runs the command.

The output of the command is then read and printed to the console. If the command exits with a non-zero exit code, an error message is printed. Any exceptions that occur during the execution of the command are also caught and printed to the console.

Having everything ready to simplify testing I created a task on gradlew to run the monitor (like we did for todd):
```groovy
task runMonitorTomcat(type:JavaExec, dependsOn: classes) {  
  main = 'net.jnjmx.todd.JMXTomcatHeapMemory'  
  if (project.hasProperty("appArgs")) {  
        args Eval.me(appArgs)  
    }  
    else {  
        args = ['192.168.64.217:6000']  
    }  
    classpath = sourceSets.main.runtimeClasspath  
}
```

This code simply runs the Class JMXTomcatHeapMemory.

To integrate this with nagios I need to create a service on my host configuration:
```
define service {
use local-service
host_name server
service_description TomcatPassive
check_command check_dummy!0 "Heap Memory Usage is Stable"
passive_checks_enabled 1
active_checks_enabled 0
contact_groups admins
notifications_enabled 1
event_handler_enabled 1
event_handler check_nrpe!restart_tomcat
}
```

When we get the passive check from the JMX notification the event handler is restarting the tomcat so that way the Heap Memory Usage will decrease.

To test it just run `./gradlew runMonitorTomcat`on the server.

# JMX Alternative

In the world of Java application monitoring, the Java Management Extensions (JMX) is a popular technology. However, there exist some alternatives to JMX such as Jolokia.

Jolokia is a plugin agent that facilitates remote access to JMX, allowing for JMX metrics to be exposed for Java applications. This enables third-party agents to query the metrics via HTTP requests, whether it is through POST or GET requests. Essentially, Jolokia enables developers to access the rich set of JMX metrics in a simple and efficient manner, which was not possible previously.

With Jolokia, developers can choose to monitor their Java applications using various tools, including Nagios. Additionally, Jolokia provides a RESTful API that is easy to use, enabling developers to customize their monitoring tools as per their requirements.

In conclusion, while there are several JMX alternatives available, Jolokia stands out as a powerful plugin agent that enables remote access to JMX metrics. By simplifying the monitoring of Java applications, Jolokia provides developers with a powerful tool to improve the performance of their applications.

Sources to conduct this project 2:
https://www.baeldung.com/java-management-extensions
https://geekflare.com/enable-jmx-tomcat-to-monitor-administer/
https://medium.com/revianlabs/jolokia-overview-and-installation-9ed9ac564546#:~:text=Jolokia%20is%20a%20JMX%2DHTTP,and%20fine%20grained%20security%20policies. 
https://www.ittsystems.com/best-jmx-monitoring-tools/ 
https://jolokia.org
https://jolokia.org/features-nb.html#:~:text=Jolokia%20is%20an%20HTTP%2FJSON,response%20payload%20represented%20in%20JSON. 
The lectures recorded by the professor.