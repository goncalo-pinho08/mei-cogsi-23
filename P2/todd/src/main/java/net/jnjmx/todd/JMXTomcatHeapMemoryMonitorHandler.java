package net.jnjmx.todd;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

        String hostaddress = "192.168.64.220";

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
