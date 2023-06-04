package net.jnjmx.todd;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

public class JMXToddAvailableSessionsStatus {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String server = "192.168.64.215";
			
			if (args.length >= 1) {
				server = args[0];
			}

			// Connect to a remote MBean Server
			JMXConnector c = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL(
							"service:jmx:rmi:///jndi/rmi://"+server+":10500/jmxrmi"));

			MBeanServerConnection mbs = c.getMBeanServerConnection();

			// Lets try to access the MBean net.jnjmx.todd.SessionPool:
			ObjectName son = new ObjectName("todd:id=SessionPool");
			ObjectInstance ob=mbs.getObjectInstance(son);

			Integer sessions=(Integer)mbs.getAttribute(son, "AvailableSessions");
			
			System.out.println("AvailableSessions=" + sessions);	
			
			Integer size=(Integer)mbs.getAttribute(son, "Size");
			
			System.out.println("Size=" + size);	

			if ((sessions) < 0.2*size) {
				c.close();
			
				System.out.println("Critical: AvailableSessions below 20%!");
				System.exit(2);				
			}
			    	
			System.out.println("OK, Avaliable Sessions above 20%");						
			
			c.close();
			
			System.exit(0);
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
			System.exit(2);
		}

	}

}
