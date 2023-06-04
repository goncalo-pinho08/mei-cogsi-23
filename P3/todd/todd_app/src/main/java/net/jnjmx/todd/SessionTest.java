package net.jnjmx.todd;

import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;

public class SessionTest {

	public static void main(String[] args) throws Exception {
		System.err.println("Raw socket test");
		Socket s = new Socket("localhost", 3006);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		bw.write("HELLO");
		bw.newLine();
		bw.flush();
		System.out.println(br.readLine());
		bw.write("TOD");
		bw.newLine();
		bw.flush();
		System.out.println(br.readLine());
		bw.write("TOD");
		bw.newLine();
		bw.flush();
		System.out.println(br.readLine());
		bw.write("GOODBYE");
		bw.newLine();
		bw.flush();
		System.out.println(br.readLine());
		s.close();
		
		System.err.println("Client test");
		Client c = new Client();
		System.out.println(c.timeOfDay());
		c.close();
	}
}

