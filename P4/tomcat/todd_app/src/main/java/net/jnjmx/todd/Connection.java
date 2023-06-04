package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Connection implements Comparable {
	private Socket socket;
	private long timestamp;

	public Connection(long timestamp, Socket socket) {
		this.timestamp = timestamp;
		this.socket = socket;
	}

	public Message receiveMsg() throws IOException {
		BufferedReader rdr =
			new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		String msg = rdr.readLine();
		return Message.forType(msg);
	}

	public void sendMsg(String msg) throws IOException {
		BufferedWriter wtr =
			new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
		wtr.write(msg);
		wtr.write("\r\n");
		wtr.flush();
	}

	public void close() throws IOException {
		socket.close();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int compareTo(Object connection) {
		if (!(connection instanceof Connection)) {
			throw new ClassCastException("Not a Connection");
		}

		return (int) (timestamp - ((Connection) connection).getTimestamp());
	}
	
	//(ATB)
	public String getClientAddress() {
		if (socket!=null) {
			return socket.getInetAddress().getHostAddress();
		}
		else return "";
	}
}