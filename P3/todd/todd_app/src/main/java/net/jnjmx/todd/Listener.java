package net.jnjmx.todd;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.SortedSet;

public class Listener extends Thread {
	public static final int TODDPORT = 3006;

	private static final int TIMEOUT = 30000;

	private SortedSet queue;
	private boolean listening;
	private int port;

	public Listener(SortedSet queue) {
		this(queue, TODDPORT);
	}

	public Listener(SortedSet queue, int port) {
		this.queue = queue;
		this.port = port;
		listening = true;
	}

	public boolean isListening() {
		return listening;
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			ss.setSoTimeout(TIMEOUT);
			listening = true;
			while (listening) {
				try {
					Socket s = ss.accept();
					Connection k = new Connection(System.currentTimeMillis(), s);
					synchronized (queue) {
						queue.add(k);
						queue.notify();
					}
				} catch (InterruptedIOException x) {
					System.err.print(".");
					// just keep waiting
				}
			}
			ss.close();
		} catch (IOException x) {
			System.err.println(x.toString());
			listening = false;
		}
	}
	
	public void stopListening() {
		listening = false;
	}
}
