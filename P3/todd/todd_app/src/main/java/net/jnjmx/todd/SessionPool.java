package net.jnjmx.todd;

import java.util.HashSet;
import java.util.Set;

public class SessionPool implements SessionPoolMBean {
	private static final int POOLSIZE = 8;

	private Set sessions;
	private int size;

	public SessionPool() {
		this(POOLSIZE);
	}

	public SessionPool(int size) {
		this.size = size;
		sessions = new HashSet(this.size);
		fill();
	}
	
	public synchronized void grow(int increment) {
		for (int i = 0; i < increment; i++) {
			Session s = new Session(this);
			sessions.add(s);
		}
		size = size + increment;
	}
	
	public Integer getSize() {
		return new Integer(size);
	}
	
	public synchronized Integer getAvailableSessions() {
		return new Integer(sessions.size());
	}

	public synchronized void release(Session session) {
		sessions.add(session);
		notify();
	}

	public synchronized Session reserve() {
		while (sessions.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException x) {
				// see if the set is still empty
			}
		}
		Session s = (Session) sessions.iterator().next();
		sessions.remove(s);
		return s;
	}

	private void fill() {
		for (int i = 0; i < size; i++) {
			Session s = new Session(this);
			sessions.add(s);
		}
	}
}