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

public class JMXAvailableSessionsMonitor {

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
	public static void configureMonitor(MBeanServerConnection mbs) throws Exception {
		// Get the actual value of the Size
		ObjectName son = new ObjectName("todd:id=SessionPool");
		ObjectInstance ob=mbs.getObjectInstance(son);
			
		Integer size=(Integer)mbs.getAttribute(son, "Size");
		Integer lowValue=(int)(0.2*size);
		//System.out.println("Size=" + size);		
		
		ObjectName spmon = new ObjectName("todd:id=AvailableSessionsMonitor");

		Set<ObjectInstance> mbeans = mbs.queryMBeans(spmon, null);

		if (mbeans.isEmpty()) {
			mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);
		} else {
			// nothing to do...
		}			

		AttributeList spmal = new AttributeList();
		spmal.add(new Attribute("ObservedObject", new ObjectName("todd:id=SessionPool")));
		spmal.add(new Attribute("ObservedAttribute", "AvailableSessions"));
		spmal.add(new Attribute("GranularityPeriod", 1000L));  // For each second
		spmal.add(new Attribute("NotifyHigh", false)); //this way we only get notifications when the value goes below the threshold
		spmal.add(new Attribute("NotifyLow", true));
		mbs.setAttributes(spmon, spmal);

		mbs.invoke(spmon, "setThresholds", new Object[] { lowValue, lowValue },
				new String[] { "java.lang.Number", "java.lang.Number" });

		mbs.addNotificationListener(spmon, new JMXAvailableSessionsMonitorHandler(), null, null); //creadted a listener that will handle the notifications
		
		mbs.invoke(spmon, "start", new Object[] {}, new String[] {});
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Todd JMXAvailableSessionsMonitor... Accessing JMX Beans (using JMX Notifications with TODD MBeans)");

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
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
		}
	}
}
