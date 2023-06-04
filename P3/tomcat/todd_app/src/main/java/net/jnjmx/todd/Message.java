package net.jnjmx.todd;

import java.util.HashMap;

public final class Message {
	private static final class Type {
		public static final String HELLO = "HELLO";
		public static final String GOODBYE = "GOODBYE";
		public static final String TOD = "TOD";
	}

	private static interface Dispatcher {
		void dispatch(MessageListener listener, MessageEvent event);
	}
	
	private static final HashMap messages = new HashMap();

	public static final Message HELLO =
		new Message(Message.Type.HELLO, new Dispatcher() {
			public void dispatch(MessageListener listener, MessageEvent event) {
				listener.helloMsg(event);
			}
		});
	public static final Message GOODBYE =
		new Message(Message.Type.GOODBYE, new Dispatcher() {
			public void dispatch(MessageListener listener, MessageEvent event) {
				listener.goodbyeMsg(event);
			}
		});
	public static final Message TOD =
		new Message(Message.Type.TOD, new Dispatcher() {
			public void dispatch(MessageListener listener, MessageEvent event) {
				listener.todMsg(event);
			}
		});

	public static Message forType(String type) {
		return (Message) messages.get(type);
	}

	private final Dispatcher dispatcher;
	private final String type;

	private Message(String type, Dispatcher dispatcher) {
		this.type = type;
		this.dispatcher = dispatcher;
		messages.put(type, this);
	}

	public void dispatch(MessageListener listener, MessageEvent event) {
		dispatcher.dispatch(listener, event);
	}

}