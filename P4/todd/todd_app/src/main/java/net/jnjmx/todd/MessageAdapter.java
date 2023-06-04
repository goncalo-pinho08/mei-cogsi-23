package net.jnjmx.todd;

import java.util.Calendar;
import java.net.Socket;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

public abstract class MessageAdapter implements MessageListener {
	public static MessageAdapter READY = new MessageAdapter() {
		public void helloMsg(MessageEvent e) {
			sendResponse(e.getConnection(), "OK");
			((Session) e.getSource()).setMessageListener(ESTABLISHED);
		}
	};
	public static MessageAdapter ESTABLISHED = new MessageAdapter() {
		public void todMsg(MessageEvent e) {
			String tod = Calendar.getInstance().getTime().toString();
			sendResponse(e.getConnection(), tod);
		}
	};

	public void helloMsg(MessageEvent e) {
		sendResponse(e.getConnection(), "ERROR");
		((Session) e.getSource()).setMessageListener(READY);
		((Session) e.getSource()).terminate();
	}

	public void todMsg(MessageEvent e) {
		sendResponse(e.getConnection(), "ERROR");
		((Session) e.getSource()).setMessageListener(READY);
		((Session) e.getSource()).terminate();
	}

	public void goodbyeMsg(MessageEvent e) {
		sendResponse(e.getConnection(), "OK");
		((Session) e.getSource()).setMessageListener(READY);
		((Session) e.getSource()).terminate();
	}

	protected void sendResponse(Connection c, String resp) {
		try {
			c.sendMsg(resp);
		} catch (Exception x) {
			System.err.println(x.toString());
		}
	}
}