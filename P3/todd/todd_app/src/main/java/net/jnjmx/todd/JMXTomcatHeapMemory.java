package net.jnjmx.todd;

import javax.management.*;
import javax.management.monitor.GaugeMonitor;
import javax.management.monitor.Monitor;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Set;

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
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Todd JMXAvailableSessionsMonitor... Accessing JMX Beans (using JMX Notifications with TODD MBeans)");

		try {

			String server = "10.5.0.6:6000";

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
