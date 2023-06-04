package net.jnjmx.todd;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;

public class JMXNotificationListener implements NotificationListener {
	
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
	}
}

