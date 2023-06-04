package net.jnjmx.todd;

import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Client {
	private BufferedReader br;
	private BufferedWriter bw;
	private Socket socket;

	public Client() throws IOException {
		this("localhost", Listener.TODDPORT);
	}

	public Client(String hostname) throws IOException {
		this(hostname, Listener.TODDPORT);
	}

	public Client(int port) throws IOException {
		this("localhost", port);
	}

	public Client(String hostname, int port) throws IOException {
		socket = new Socket(hostname, port);
		bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		bw.write("HELLO");
		bw.newLine();
		bw.flush();

		String response = br.readLine();
		if ((response == null) || (response.compareToIgnoreCase("OK") != 0)) {
			socket.close();
			throw new IOException("TODD greeting failed");
		}
	}

	public String timeOfDay() throws IOException {
		bw.write("TOD");
		bw.newLine();
		bw.flush();
		
		String response = br.readLine();
		if (response == null) {
			socket.close();
			throw new IOException("TODD timeOfDay failed");
		}

		return response;
	}
	
	public void close() throws IOException {
		bw.write("GOODBYE");
		bw.newLine();
		bw.flush();
		
		String response = br.readLine();
		if ((response == null) || (response.compareToIgnoreCase("OK") != 0)) {
			socket.close();
			throw new IOException("TODD disconnect failed");
		}
		socket.close();
	}
}