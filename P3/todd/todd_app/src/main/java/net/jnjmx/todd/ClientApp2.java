package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Scanner;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.management.remote.*;


public class ClientApp2 {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Todd ClientApp2... Accessing JMX Beans (and the 'Uptime' property of TODD MBean 'Server')");

		try {
			String server = "10.5.0.6:10500";

			if (args.length >= 1) {
				server = args[0];
			}

			System.out.println("Connecting to JMX Agent (with running TODD server) at "+server+" ...");

			// Connect to a remote MBean Server
			JMXConnector c = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + server + "/jmxrmi"));
			
			MBeanServerConnection mbs = c.getMBeanServerConnection();

			/*
			 * MBeanServerConnection conn=new MBeanServerConnection();
			 * ManagementFactory.newPlatformMXBeanProxy(MBeanServerConnection
			 * connection, String mxbeanName, Class<T> mxbeanInterface)
			 */

			Set<ObjectInstance> mbeans = mbs.queryMBeans(null, null);

			for (ObjectInstance mbean : mbeans) {
				System.out.println(mbean.getClassName());
			}
			
			// Lets try to access the MBean net.jnjmx.todd.Server:
			ObjectName son = new ObjectName("todd:id=Server");
			ObjectInstance ob=mbs.getObjectInstance(son);
			
			String value=(String)mbs.getAttribute(son, "Name");
			
			System.out.println("Nane=" + value);			

			c.close();
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
		}
	}
}
