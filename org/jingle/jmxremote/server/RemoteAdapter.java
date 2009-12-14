/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.jingle.jmxremote.server;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.ModelMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.timer.Timer;

import org.jingle.jmxremote.common.JMXRemoteNotification;
import org.jingle.jmxremote.common.JMXRemoteUtil;
import org.jingle.jmxremote.common.MBeanInfoExt;
import org.jingle.jmxremote.common.RemoteAdapterMBean;
import org.jingle.jmxremote.common.RemoteMBeanServerSettings;

import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import edu.emory.mathcs.backport.java.util.concurrent.FutureTask;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

public class RemoteAdapter extends NotificationBroadcasterSupport implements DynamicMBean, RemoteAdapterMBean, MBeanRegistration, NotificationListener {

	private static final String PING_NOTIFICATION_TYPE = "org.jingle.jmxremote.server.timer.NotificationType.ping";
	private static MBeanInfo MBEANINFO = null;
	
	protected ObjectName name = null;
	protected MBeanServer server = null;
	protected Timer timer = null;
	protected Map mBeanMap = new HashMap();
	//protected Map<ObjectName, IRemoteDynamicMBean> prepareMBeanMap = new HashMap<ObjectName, IRemoteDynamicMBean>();
	protected RemoteMBeanServerSettings settings = new RemoteMBeanServerSettings();
	protected ThreadPoolExecutor threadPool = null;
	protected Map waitingMap = new HashMap();
	
	private ReentrantLock lock = new ReentrantLock();

	
	static {
		MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[] {
				new MBeanAttributeInfo("RemoteMBeans", Map.class.getName(), "remote mbeans", true, false, false),
				new OpenMBeanAttributeInfoSupport("Settings", "remote MBeanServer settings", RemoteMBeanServerSettings.OPENTYPE, true, false, false)};
		MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[] {new MBeanConstructorInfo("Constructor", "Constructor", new MBeanParameterInfo[] {new OpenMBeanParameterInfoSupport("settings", "remote MBeanServer settings", RemoteMBeanServerSettings.OPENTYPE)})};
		MBeanOperationInfo[] opers = new MBeanOperationInfo[] {
				new MBeanOperationInfo("createDynamicMBean", "create dynamic mbean", new MBeanParameterInfo[] {
						new MBeanParameterInfo("mBeanInfo", MBeanInfo.class.getName(), "MBeanInfo"),
						new OpenMBeanParameterInfoSupport("mBeanInfoExt", "MBeanInfo extention", MBeanInfoExt.OPENTYPE)
						}, ObjectInstance.class.getName(), MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("fireNotification", "fire notification", new MBeanParameterInfo[] {
						new MBeanParameterInfo("objectName", ObjectName.class.getName(), "target mbean name"),
						new MBeanParameterInfo("notification", Notification.class.getName(), "notification")
						}, Void.class.getName(), MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("ping", "ping remote mbean server", new MBeanParameterInfo[0], Void.class.getName(), MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("returnException", "Return an exception according to a request", new MBeanParameterInfo[] {
						new MBeanParameterInfo("objectName", ObjectName.class.getName(), "target mbean name"),
						new MBeanParameterInfo("sequence", Long.TYPE.getName(), "Request sequence"),
						new MBeanParameterInfo("throwable", Throwable.class.getName(), "exception"),
						new MBeanParameterInfo("prepare", Boolean.class.getName(), "Whether the target object is in prepare stage")
						}, Void.class.getName(), MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("returnResult", "Return a request result", new MBeanParameterInfo[] {
						new MBeanParameterInfo("objectName", ObjectName.class.getName(), "Target mbean name"),
						new MBeanParameterInfo("sequence", Long.TYPE.getName(), "Request sequence"),
						new MBeanParameterInfo("attr", Attribute.class.getName(), "Result attribute"),
						new MBeanParameterInfo("prepare", Boolean.class.getName(), "Whether the target object is in prepare stage")
						}, Void.class.getName(), MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("returnResults", "Return request results", new MBeanParameterInfo[] {
						new MBeanParameterInfo("objectName", ObjectName.class.getName(), "target mbean name"),
						new MBeanParameterInfo("sequence", Long.TYPE.getName(), "Request sequence"),
						new MBeanParameterInfo("attrs", Throwable.class.getName(), "Result attribute")
						}, Void.class.getName(), MBeanOperationInfo.ACTION),
				};
		MBEANINFO = new MBeanInfo(RemoteAdapter.class.getName(), "Remote Adapter", 
				attrs, constructors, opers, new MBeanNotificationInfo[0]);
	}
	
	class WaitingTask implements Callable {
		JMXRemoteNotification notif;
		ResultContainer entry;
		public WaitingTask(JMXRemoteNotification notif, ResultContainer entry) {
			this.notif = notif;
			this.entry = entry;
		}

		public Object call() throws Exception {
			Long sequence = new Long(notif.getSequenceNumber());
			synchronized(waitingMap) {
				waitingMap.put(new Long(sequence.longValue()), entry);
			}
			try {
				sendNotification(notif.toNotification());
				synchronized(entry) {
					if (entry.isFilled()) {
						return entry;
					} else {
					    entry.wait(settings.getResponseWaitTime());
						return entry;
					}
				}
			} catch (InterruptedException e) {
				throw new JMException("operation time out");
			} finally {
				synchronized (waitingMap) {
					waitingMap.remove(sequence);
				}
			}
		}
	}
	public RemoteAdapter(CompositeData settingsData) {
		this.settings.fromOpenData(settingsData);
		timer = new Timer();
		timer.stop();
		timer.addNotification(PING_NOTIFICATION_TYPE, null, null, new Date(), settings.getHeartBeatInterval());
		//timer.addNotification(BROADCAST_NOTIFICATION_TYPE, null, null, new Date(), adapterSettings.getBroadcastInterval());
//		RemoteMBeanServerSettings settings = mBeanInfo.getRemoteMBeanServerSettings();
		threadPool = new ThreadPoolExecutor(settings.getCoreThreadPoolSize(), 
				settings.getMaxThreadPoolSize(), 
				settings.getThreadKeepAliveTime(), 
				TimeUnit.MILLISECONDS, 
				(settings.getThreadWorkQueueSize() <= 0) ? new LinkedBlockingQueue() : new LinkedBlockingQueue(settings.getThreadWorkQueueSize()));

	}
	
	public ObjectInstance createDynamicMBean(MBeanInfo mBeanInfo, CompositeData mBeanInfoExtData, ObjectName name) throws JMException {
		MBeanInfoExt mBeanInfoExt = new MBeanInfoExt(mBeanInfoExtData);
		RemoteDynamicMBean inst = new RemoteDynamicMBean(mBeanInfo, mBeanInfoExt, this);
		IRemoteDynamicMBean mBeanInst = null;
		if (mBeanInfoExt.isModelMBean()) {
			mBeanInst = (IRemoteDynamicMBean) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {IRemoteDynamicMBean.class, ModelMBean.class}, new RemoteMBeanInvocationHandler(inst));
		} else if (mBeanInfoExt.isNotificationEmitter()) {
			mBeanInst = inst;
		} else if (mBeanInfoExt.isNotificationBroadcaster()) {
			mBeanInst = (IRemoteDynamicMBean) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {IRemoteDynamicMBean.class}, new RemoteMBeanInvocationHandler(inst));
		} else {
			mBeanInst = (IRemoteDynamicMBean) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {IRemoteDynamicMBean.class}, new RemoteMBeanInvocationHandler(inst));
		}

		if (mBeanInfoExt.isClassLoader()) {
			if (mBeanInfoExt.isModelMBean()) {
				mBeanInst = new RemoteClassLoaderWithModelMBean(this, (IRemoteDynamicMBean) mBeanInst);
			} else {
				mBeanInst = new RemoteClassLoader(this, (IRemoteDynamicMBean) mBeanInst);
			}
		}
		
		ObjectInstance ret = null;
		try {
			lock.lock();
			synchronized (mBeanMap) {
				mBeanMap.put(name, mBeanInst);
			}
			ret = server.registerMBean(mBeanInst, name);
			synchronized(mBeanMap) {
				mBeanMap.remove(name);
				mBeanMap.put(ret.getObjectName(), mBeanInst);
			}
			return ret;
		} finally {
			if (ret == null) {
				synchronized(mBeanMap) {
					mBeanMap.remove(name);
				}
			}
			lock.unlock();
		}
	}
	
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		this.server = server;
		this.name = name;
		return name;
	}

	public void postRegister(Boolean done) {
		if (Boolean.TRUE.equals(done)) {
			timer.addNotificationListener(this, new TimerNotificationFilter(), null);
			timer.start();
			try {
				server.addNotificationListener(JMXRemoteUtil.getMBeanServerDelegateObjectName(), this, null, null);
			} catch (InstanceNotFoundException e) {
				throw new RuntimeException("Can not add notification to MBeanServerDelegate");
			}
		}
	}

	public void preDeregister() throws Exception {
	}

	public void postDeregister() {
		threadPool.shutdown();
		timer.stop();
	}

	public void returnResult(ObjectName name, long sequence, Attribute attr, boolean prepare) {
		Object inst = null;
		synchronized(mBeanMap) {
		    inst = mBeanMap.get(name);
		}
		if (inst != null) {
		    setOperationResult(sequence, new OperationResult(attr));
		}
	}

	public void returnException(ObjectName name, long sequence, Throwable t, boolean prepare) {
		Object inst = null;
		synchronized(mBeanMap) {
			inst = mBeanMap.get(name);
		}
		if (inst != null)
		    setOperationResult(sequence, new OperationResult((Throwable)t));
	}

	public void returnResults(ObjectName name, long sequence, AttributeList attrs) {
		Object inst = null;
		synchronized(mBeanMap) {
			inst = mBeanMap.get(name);
		}
		if (inst != null)
		    setOperationResult(sequence, new OperationResult((AttributeList)attrs));
	}

	public void sendInternalNotification(JMXRemoteNotification notif) {
		this.sendNotification(notif.toNotification());
	}
	
	public void removeMBean(ObjectName name) {
		synchronized(mBeanMap) {
			mBeanMap.remove(name);
		}
	}

	public MBeanNotificationInfo[] getNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}

	public void fireNotification(ObjectName name, Notification notification) {
		IRemoteDynamicMBean inst = null;
		synchronized(mBeanMap) {
			inst = (IRemoteDynamicMBean) mBeanMap.get(name);
		}		
		if (inst != null) {
			inst.sendNotification(notification);
		}
	}

	public void ping() {
		this.settings.renew();
	}

	public void handleNotification(Notification notification, Object handback) {
		if (PING_NOTIFICATION_TYPE.equals(notification.getType())) {
			this.settings.tick();
			if (this.settings.isExpired()) {
				try {
					List objNameList = new ArrayList();
					synchronized(mBeanMap) {
						objNameList.addAll(this.mBeanMap.keySet());
					}
					
					for (int i = 0; i < objNameList.size(); i++) {
						ObjectName objName = (ObjectName) objNameList.get(i);
						try {
							server.unregisterMBean(objName);
						} catch (Exception e) {
						}
					}
					server.unregisterMBean(name);
				} catch (Exception e) {
				}
			}
		}
	}

	public Map getRemoteMBeans() {
		Map ret = new HashMap();
		synchronized(mBeanMap) {
			for (int i = 0; i < new ArrayList(mBeanMap.entrySet()).size(); i++) {
				Map.Entry entry = (Entry) new ArrayList(mBeanMap.entrySet()).get(i);
				ret.put(entry.getKey(), ((DynamicMBean) entry.getValue()).getMBeanInfo());
			}
		}
		return ret;
	}

	public CompositeData getSettings() {
		return settings.toOpenData();
	}

	public RemoteMBeanServerSettings getRemoteMBeanServerSettings() {
		return this.settings;
	}
	
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
			if (attribute.equals("Settings"))
				return this.getSettings();
			else if (attribute.equals("RemoteMBeans"))
				return this.getRemoteMBeans();
			else
				throw new AttributeNotFoundException(attribute);
	}

	public AttributeList getAttributes(String[] attributes) {
		AttributeList ret = new AttributeList();
		
		for (int i = 0; i < attributes.length; i++) {
			String attr = attributes[i];
			try {
				Object value = getAttribute(attr);
				ret.add(new Attribute(attr, value));
			} catch (Exception e) {
			}
		}
		return ret;
	}

	public MBeanInfo getMBeanInfo() {
		return MBEANINFO;
	}

	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		if (actionName.equals("createDynamicMBean")) {
			try {
				return this.createDynamicMBean((MBeanInfo)params[0], (CompositeData)params[1], (ObjectName)params[2]);
			} catch (JMException e) {
				throw new MBeanException(e, "Error when createDynamicMBean");
			}
		} else if (actionName.equals("fireNotification")) {
			this.fireNotification((ObjectName)params[0], (Notification)params[1]);
			return null;
		} else if (actionName.equals("ping")) {
			this.ping();
			return null;
		} else if (actionName.equals("returnException")) {
			this.returnException((ObjectName)params[0], ((Long)params[1]).longValue(), (Throwable)params[2], ((Boolean)params[3]).booleanValue());
			return null;
		} else if (actionName.equals("returnResult")) {
			this.returnResult((ObjectName)params[0], ((Long)params[1]).longValue(), (Attribute)params[2], ((Boolean)params[3]).booleanValue());
			return null;
		} else if (actionName.equals("returnResults")) {
			this.returnResults((ObjectName)params[0], ((Long)params[1]).longValue(), (AttributeList)params[2]);
			return null;
		}
		throw new MBeanException(null, "Can not find action: [" + actionName + "]");
	}

	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		throw new AttributeNotFoundException(attribute.getName());
	}

	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList ret = new AttributeList();
		
		for (int i = 0; i < attributes.size(); i++) {
			Object attr = attributes.get(i);
			try {
				setAttribute((Attribute)attr);
				ret.add((Attribute)attr);
			} catch (Exception e) {
			}
		}
		return ret;
	}
	
	public Object invokeRemote(JMXRemoteNotification notif, ResultContainer entry) throws Exception {
		FutureTask task = new FutureTask(new WaitingTask(notif, entry));
		threadPool.execute(task);
		try {
			task.get();
		} catch (Throwable e) {
			JMXRemoteUtil.throwJMException("Error when invoke remote operation", e);
		}
		if (entry.hasException()) {
		    throw entry.getException();
		}
		return entry.getResult();
	}
	
	private void setOperationResult(long sequence, OperationResult result) {
		ResultContainer entry = null;
		synchronized (waitingMap) {
			entry = (ResultContainer) waitingMap.get(new Long(sequence));
		}
		if (entry != null)
		    entry.setResult(result);
	}
}

