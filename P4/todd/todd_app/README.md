TODD JMX Demonstration Application
========================

This gradle project is now configured to work with a local installation of the Nexus Artifact Repository Manager.

To upload the jar into the local Nexus type:

 	./gradlew uploadArchives

# 1. Requirements

Java JDK

Gradle

# 2. Build the Jar

Type:

	./gradlew jar

# 3. Run the Application

## Run the Server (JMX local)

Type:

	./gradlew runServer

## Execute the Client App

Type:

	./gradlew runClient

## Run the Server (JMX remote activated)

Type:

	./gradlew runServerRemote

## Execute the Client App that Access JMX remotely

Type:

	./gradlew runClient2

## Execute the Client App that Demonstrates JMX Notifications remotely

Type:

	./gradlew runClient3

# 4. IDE Support

You should be able to open the project in Eclipse and Netbeans.
