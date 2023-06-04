package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

public class JMXToddServerGrow {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String server = "192.168.64.220";
			
			if (args.length >= 1) {
				server = args[0];
			}

			// Connect to a remote MBean Server
			JMXConnector c = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL(
							"service:jmx:rmi:///jndi/rmi://"+server+":10500/jmxrmi"));

			MBeanServerConnection mbs = c.getMBeanServerConnection();

			// Lets try to access the MBean net.jnjmx.todd.Server:
			ObjectName son = new ObjectName("todd:id=SessionPool");
			ObjectInstance ob=mbs.getObjectInstance(son);
			mbs.invoke(son, "grow", new Object[] {new Integer(2)} , new String[] {"int"}); //the first argument is the
			System.out.println("Grow method was invoked");						
			c.close();
			System.exit(0);
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
			System.exit(2);
		}

	}

}
