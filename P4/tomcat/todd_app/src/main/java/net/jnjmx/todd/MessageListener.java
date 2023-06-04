package net.jnjmx.todd;

import java.util.EventListener;

public interface MessageListener extends EventListener {
	void goodbyeMsg(MessageEvent e);
	void helloMsg(MessageEvent e);
	void todMsg(MessageEvent e);
}

