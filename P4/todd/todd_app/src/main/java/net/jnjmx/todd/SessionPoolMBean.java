package net.jnjmx.todd;

public interface SessionPoolMBean {
	void grow(int increment);
	
	Integer getAvailableSessions();
	Integer getSize();
}
