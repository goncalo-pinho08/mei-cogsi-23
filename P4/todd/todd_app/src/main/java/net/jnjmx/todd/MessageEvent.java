package net.jnjmx.todd;

import java.util.EventObject;
import java.net.Socket;

public class MessageEvent extends EventObject {
	private final Connection connection;
	public MessageEvent(Object source, Connection connection) {
		super(source);
		this.connection = connection;
	}

	public Connection getConnection() {
		return this.connection;
	}
}

