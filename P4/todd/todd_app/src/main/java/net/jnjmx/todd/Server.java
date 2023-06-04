package net.jnjmx.todd;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.monitor.MonitorNotification;
import javax.management.relation.InvalidRoleInfoException;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
import javax.management.relation.RoleResult;

public class Server implements ServerMBean, NotificationListener {
	private MBeanServer mbs;

	private SessionPool sessions;
	private SortedSet connectionQueue;
	private Listener listener;
	private boolean active;

	private long tzero;
	private int connections;

	private String name="";

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name=name;
	}

	public static void main(String[] args) throws Exception {
		try {

			// MBeanServer mbs = MBeanServerFactory.createMBeanServer();
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			// The portion of the name to the left of the colon is the domain,
			// which is an arbitrary string that is opaque to the MBeanServer
			// but may have meaning to one or more management applications.
			// On the right are the key properties, a set of name/value pairs that help
			// distinguish one MBean from another. Together they must form a unique name,
			// within a given MBeanServer, for the associated MBean.
			Server server = new Server(mbs);
			ObjectName son = new ObjectName("todd:id=Server");
			mbs.registerMBean(server, son);
			
			// configure the gauge monitor 
			configureMonitor1(mbs);

			// ObjectName rson = new ObjectName("todd:id=RelationService");
			// mbs.createMBean(
			// "javax.management.relation.RelationService",
			// rson,
			// new Object[] { new Boolean(true)},
			// new String[] { "boolean" });
			//
			// createRelationType(mbs, rson);

			// configureMonitor(mbs, rson);

			// ObjectName httpon = new ObjectName("adapters:id=Http");
			// mbs.createMBean("com.tivoli.jmx.http_pa.Listener", httpon);
			// mbs.invoke(httpon, "startListener", new Object[] {}, new String[] {});

			mbs.invoke(son, "start", new Object[] {}, new String[] {});

			while (server.isActive()) {
				Connection k = server.waitForConnection();
				server.activateSession(k);

				// (ATB)
				server.internalSetLastClient(k.getClientAddress());
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public Server(MBeanServer mbs) throws Exception {
		this.mbs = mbs;

		connectionQueue = new TreeSet();
		connections = 0;

		sessions = new SessionPool();
		ObjectName spon = new ObjectName("todd:id=SessionPool");
		mbs.registerMBean(sessions, spon);

		active = true;
		tzero = System.currentTimeMillis();
	}

	public void activateSession(Connection k) {
		Session s;
		synchronized (sessions) {
			while ((s = sessions.reserve()) == null) {
				try {
					sessions.wait();
				} catch (InterruptedException x) {
					// see if a session is available
				}
			}
		}
		s.activate(k);
		connections++;
	}

	/**
		The first two lines here create and register a GaugeMonitor MBean named todd:id=SessionPoolMonitor. 
		The next seven lines set attributes that tell GaugeMonitor which attribute of which MBean should be monitored 
		(ObservedAttribute or ObservedObject), how often (GranularityPeriod, in milliseconds), and whether or not 
		to send a notification on high-threshold and low-threshold violations. 
		Then we invoke the setThresholds() method, via the MBeanServer, to set the actual high and low threshold values. 
		Finally, we make the server listen for session pool monitor notifications and start the gauge monitor.
	*/
	public static void configureMonitor1(MBeanServer mbs) throws Exception {
		ObjectName spmon = new ObjectName("todd:id=SessionPoolMonitor");
		mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);

		AttributeList spmal = new AttributeList();
		spmal.add(new Attribute("ObservedObject", new ObjectName("todd:id=SessionPool")));
		spmal.add(new Attribute("ObservedAttribute", "AvailableSessions"));
		spmal.add(new Attribute("GranularityPeriod", new Long(10000)));
		spmal.add(new Attribute("NotifyHigh", new Boolean(true)));
		spmal.add(new Attribute("NotifyLow", new Boolean(true)));
		mbs.setAttributes(spmon, spmal);

		mbs.invoke(spmon, "setThresholds", new Object[] { new Integer(1), new Integer(0) },
				new String[] { "java.lang.Number", "java.lang.Number" });

		mbs.addNotificationListener(spmon, new ObjectName("todd:id=Server"), null, new Object());

		mbs.invoke(spmon, "start", new Object[] {}, new String[] {});
	}

	public static void configureMonitor(MBeanServer mbs, ObjectName rson)
			throws MalformedObjectNameException, ReflectionException, InstanceAlreadyExistsException,
			MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {

		ObjectName spmon = new ObjectName("todd:id=SessionPoolMonitor");
		mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);

		AttributeList spmal = new AttributeList();
		spmal.add(new Attribute("ObservedObject", new ObjectName("todd:id=SessionPool")));
		spmal.add(new Attribute("ObservedAttribute", "AvailableSessions"));
		spmal.add(new Attribute("GranularityPeriod", new Long(10000)));
		spmal.add(new Attribute("NotifyHigh", new Boolean(true)));
		spmal.add(new Attribute("NotifyLow", new Boolean(true)));
		mbs.setAttributes(spmon, spmal);

		mbs.invoke(spmon, "setThresholds", new Object[] { new Integer(1), new Integer(0) },
				new String[] { "java.lang.Number", "java.lang.Number" });

		RoleList sprl = new RoleList();
		ArrayList poolval = new ArrayList();
		poolval.add(new ObjectName("todd:id=SessionPool"));
		Role poolrole = new Role("pool", poolval);

		ArrayList gaugeval = new ArrayList();
		gaugeval.add(spmon);
		Role gaugerole = new Role("gauges", gaugeval);

		ArrayList timerval = new ArrayList();
		Role timerrole = new Role("timer", timerval);

		sprl.add(poolrole);
		sprl.add(gaugerole);
		sprl.add(timerrole);

		mbs.invoke(rson, "createRelation", new Object[] { "todd.sessionpool", "SessionPool", sprl },
				new String[] { "java.lang.String", "java.lang.String", "javax.management.relation.RoleList" });

		RoleResult rr = (RoleResult) mbs.invoke(rson, "getAllRoles", new Object[] { "todd.sessionpool" },
				new String[] { "java.lang.String" });

		mbs.addNotificationListener(spmon, new ObjectName("todd:id=Server"), null, new Object());

		mbs.invoke(spmon, "start", new Object[] {}, new String[] {});
	}

	public static void createRelationType(MBeanServer mbs, ObjectName rson) throws MalformedObjectNameException,
			MBeanException, MBeanRegistrationException, NotCompliantMBeanException, ClassNotFoundException,
			InvalidRoleInfoException, InstanceAlreadyExistsException, InstanceNotFoundException, ReflectionException {
		RoleInfo[] sproles = new RoleInfo[3];
		sproles[0] = new RoleInfo("pool", "net.jnjmx.todd.SessionPool");
		sproles[1] = new RoleInfo("gauges", "javax.management.monitor.GaugeMonitor", false, false, 1, 2, null);
		sproles[2] = new RoleInfo("timer", "javax.management.timer.Timer", false, false, 0, 1, null);
		mbs.invoke(rson, "createRelationType", new Object[] { "SessionPool", sproles },
				new String[] { "java.lang.String", "[Ljavax.management.relation.RoleInfo;" });
	}

	public Integer getConnections() {
		return new Integer(connections);
	}

	public Integer getSessions() {
		int as = sessions.getAvailableSessions().intValue();
		int sz = sessions.getSize().intValue();
		return new Integer(sz - as);
	}

	public Long getUptime() {
		return new Long(System.currentTimeMillis() - tzero);
	}

	public void handleNotification(Notification n, Object hb) {
		String type = n.getType();
		if (type.compareTo(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED) == 0) {
			stop();
		} else if (type.compareTo(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED) == 0) {
			if (isActive() == false)
				start();
		}
	}

	public boolean isActive() {
		return active;
	}

	public void shutdown() {
		System.exit(0);
	}

	public void start() {
		listener = new Listener(connectionQueue);
		listener.start();
	}

	public void stop() {
		listener.stopListening();
	}

	public Connection waitForConnection() {
		Connection k;
		synchronized (connectionQueue) {
			while (connectionQueue.isEmpty()) {
				try {
					connectionQueue.wait();
				} catch (InterruptedException x) {
					// see if the queue is still empty
				}
			}
			k = (Connection) connectionQueue.first();
			connectionQueue.remove(k);
		}
		return k;
	}

	// (ATB)
	private String lastClient = "";

	private void internalSetLastClient(String last) {
		lastClient = last;
	}

	@Override
	public String getLastClient() {
		return lastClient;
	}
}