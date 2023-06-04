package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientApp {

	static Scanner input = new Scanner(System.in);

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("Todd ClientApp... The 'regular' TODD client application.");
		
		try {
			String server = "10.5.0.6";
			int sessions = 8;
			ArrayList<Client> clients=new ArrayList<>();

			if (args.length >= 1) {
				server = args[0];
				server.replace(":10500", " ").trim();
			}	

			if (args.length >= 2) {
				sessions = Integer.parseInt(args[1]);
			}	

			System.out.println("Connecting to TODD server at "+server+" ...");
			
			for (int i=0; i<sessions; ++i) {
				Client c=new Client(server);
				clients.add(c);
				c.timeOfDay();
			}

			System.out.println("Waiting 60 secs to receive notifications");
			Thread.sleep(60000);
			
			//Scanner input = new Scanner(System.in);
			//input.nextLine();

			//System.out.println("Press enter to exit");
			//System.console().readLine();

			for (int i=0; i<sessions; ++i) {
				clients.get(i).close();
			}			
			
			System.out.println("Exiting...");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
