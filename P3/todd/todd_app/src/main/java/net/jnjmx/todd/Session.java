package net.jnjmx.todd;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Session implements Runnable {
	private MessageListener msgListener;
	private Connection connection;
	private SessionPool pool;
	private boolean active;

	public Session(SessionPool pool) {
		this.msgListener = MessageAdapter.READY;
		this.active = false;
		this.pool = pool;
	}

	public void setMessageListener(MessageListener ml) {
		msgListener = ml;
	}

	public Thread activate(Connection c) {
		connection = c;
		active = true;

		Thread t = new Thread(this);
		t.start();
		return t;
	}

	public void activateAndWait(Connection c) {
		try {
			activate(c).join();
		} catch (InterruptedException x) {
		}
	}

	public void terminate() {
		active = false;
		try {
			connection.close();
			pool.release(this);
		} catch (IOException x) {
			System.err.println(x.toString());
		}
	}

	public void run() {
		try {
			while (active) {
				Message msg = connection.receiveMsg();
				msg.dispatch(msgListener, new MessageEvent(this, connection));
			}
		} catch (Exception x) {
			System.err.println(x.toString());
			active = false;
		}
	}
}