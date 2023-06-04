package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

public class ClientApp3 {

	/**
	 * The first two lines here create and register a GaugeMonitor MBean named
	 * todd:id=SessionPoolMonitor. The next seven lines set attributes that tell
	 * GaugeMonitor which attribute of which MBean should be monitored
	 * (ObservedAttribute or ObservedObject), how often (GranularityPeriod, in
	 * milliseconds), and whether or not to send a notification on high-threshold
	 * and low-threshold violations. Then we invoke the setThresholds() method, via
	 * the MBeanServer, to set the actual high and low threshold values. Finally, we
	 * make the server listen for session pool monitor notifications and start the
	 * gauge monitor.
	 */
	public static void configureMonitor1(MBeanServerConnection mbs) throws Exception {
		ObjectName spmon = new ObjectName("todd:id=SessionPoolMonitorRemote");

		Set<ObjectInstance> mbeans = mbs.queryMBeans(spmon, null);

		if (mbeans.isEmpty()) {
			mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);
		} else {
			// nothing to do...
		}

		AttributeList spmal = new AttributeList();
		spmal.add(new Attribute("ObservedObject", new ObjectName("todd:id=Server")));
		spmal.add(new Attribute("ObservedAttribute", "Connections"));
		spmal.add(new Attribute("GranularityPeriod", new Long(10000)));
		spmal.add(new Attribute("NotifyHigh", new Boolean(true)));
		spmal.add(new Attribute("NotifyLow", new Boolean(true)));
		mbs.setAttributes(spmon, spmal);

		mbs.invoke(spmon, "setThresholds", new Object[] { new Integer(2), new Integer(-1) },
				new String[] { "java.lang.Number", "java.lang.Number" });

		mbs.addNotificationListener(spmon, new JMXNotificationListener(), null, null);

		mbs.invoke(spmon, "start", new Object[] {}, new String[] {});
	}

	public static void configureMonitor2(MBeanServerConnection mbs) throws Exception {
		ObjectName spmon = new ObjectName("todd:id=SessionsMonitorRemote");

		Set<ObjectInstance> mbeans = mbs.queryMBeans(spmon, null);

		if (mbeans.isEmpty()) {
			mbs.createMBean("javax.management.monitor.CounterMonitor", spmon);
		} else {
			// nothing to do...
		}

		AttributeList spmal = new AttributeList();
		spmal.add(new Attribute("ObservedObject", new ObjectName("todd:id=Server")));
		spmal.add(new Attribute("ObservedAttribute", "Connections"));  // "Sessions"));
		spmal.add(new Attribute("GranularityPeriod", new Long(10000)));
		spmal.add(new Attribute("Notify", new Boolean(true)));
		spmal.add(new Attribute("InitThreshold", new Integer(1)));	
		spmal.add(new Attribute("Offset", new Integer(2)));	
		spmal.add(new Attribute("DifferenceMode", new Boolean(false)));
		mbs.setAttributes(spmon, spmal);

		mbs.addNotificationListener(spmon, new JMXNotificationListener(), null, null);

		mbs.invoke(spmon, "start", new Object[] {}, new String[] {});
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Todd ClientApp3... Accessing JMX Beans (using JMX Notifications with TODD MBeans)");

		try {

			String server = "10.5.0.6:10500";

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
			configureMonitor1(mbs);
			
			configureMonitor2(mbs);
			
			// mbs.addNotificationListener(new ObjectName("todd:id=SessionPool"), new
			// JMXNotificationListener(), null, null);
			// Thread.sleep(100000);
			
			System.out.println("Waiting 60 secs to receive notifications");
			Thread.sleep(60000);
			
			c.close();
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
		}
	}
}
