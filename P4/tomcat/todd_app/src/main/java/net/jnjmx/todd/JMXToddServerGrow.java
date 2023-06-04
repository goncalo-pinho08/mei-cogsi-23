package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
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
			String server = "10.6.0.3";
			
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
			c.close();
			String hostaddress = "10.5.0.3";
			String command = "sleep 5 && echo -e \"todd;ToddSessionsPassive;0;Its back to normal\" | /usr/local/nagios/bin/send_nsca -H "+hostaddress+" -d \";\" -c /usr/local/nagios/etc/send_nsca.cfg";


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
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
			System.exit(2);
		}

	}

}
