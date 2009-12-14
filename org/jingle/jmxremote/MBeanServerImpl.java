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
package org.jingle.jmxremote.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.emory.mathcs.backport.java.util.concurrent.locks.*;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMRuntimeException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;
import javax.management.openmbean.CompositeData;

import org.jingle.jmxremote.client.internal.MBeanInfoCollector;
import org.jingle.jmxremote.client.internal.RemoteNotificationListener;
import org.jingle.jmxremote.common.IDoOperNotification;
import org.jingle.jmxremote.common.IGetAttributeNotification;
import org.jingle.jmxremote.common.IGetAttributesNotification;
import org.jingle.jmxremote.common.IRegistrationNotification;
import org.jingle.jmxremote.common.ISetAttributeNotification;
import org.jingle.jmxremote.common.ISetAttributesNotification;
import org.jingle.jmxremote.common.JMXRemoteNotification;
import org.jingle.jmxremote.common.JMXRemoteNotificationFilter;
import org.jingle.jmxremote.common.JMXRemoteUtil;
import org.jingle.jmxremote.common.MBeanInfoExt;
import org.jingle.jmxremote.common.RemoteAdapterMBean;
import org.jingle.jmxremote.common.RemoteMBeanServerSettings;

import com.eaio.uuid.UUID;

/**
 * This class is a delegation of remote MBean Server. It can be used to do most
 * of the MBeanServer operations except instantiate, deserialize and classloader
 * related operation. The most advantage of this implementation is that it can 
 * register a local MBean to the remote MBeanServer.
 * 
 * @author jianlu
 *
 */
public class MBeanServerImpl implements MBeanServer, NotificationListener {
	private MBeanServerConnection connection = null;
	private Map mBeanMap = new HashMap();
	//Map<ObjectName, MBeanInfoCollector> prepareMBeanMap = new HashMap<ObjectName, MBeanInfoCollector>();
	private boolean delegate = true;
	private RemoteMBeanServerSettings remoteMBeanServerSettings;
	private Thread timer = null;
	private ObjectName adapterName = null;
	private edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock lock = new ReentrantLock();
	private RemoteAdapterMBean adapter = null;
	//private static final String TIMER_NOTIFICATION_TYPE = "org.jingle.jmxremote.client.timer.NotificationType";

	/**
	 * Constructor
	 * @param connection MBeanServerConnection instance
	 */
	public MBeanServerImpl(MBeanServerConnection connection) {
		this(connection, new RemoteMBeanServerSettings());
	}
	
	/**
	 * Constructor
	 * @param connection MBeanServerConnection instance
	 * @param remoteMBeanServerSettings RemoteMBeanServerSettings instance to specify
	 * some detailed parameters for this MBeanServer usage.
	 */
	public MBeanServerImpl(MBeanServerConnection connection, RemoteMBeanServerSettings remoteMBeanServerSettings) {
		if ((connection == null) || (remoteMBeanServerSettings == null))
			throw new IllegalArgumentException();
		this.connection = connection;
		this.remoteMBeanServerSettings = (RemoteMBeanServerSettings) remoteMBeanServerSettings.clone();
		this.remoteMBeanServerSettings.setClientId(new UUID().toString());
		this.remoteMBeanServerSettings.setTick(0);
		adapterName = this.remoteMBeanServerSettings.getObjectName();
		if (connection instanceof MBeanServer)
			delegate = false;
		if (delegate) {
			try {
			    createAdapter();
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when create the adapter", e);
			}
			timer = new Thread() {
				public void run() {
					while(true) {
						invokePing();
						synchronized(this) {
							try {
								this.wait(MBeanServerImpl.this.remoteMBeanServerSettings.getHeartBeatInterval());
							} catch (InterruptedException e) {
							}
						}
					}
				}
			};
			timer.setDaemon(true);
			timer.start();
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void addNotificationListener(ObjectName arg0,
			NotificationListener arg1, NotificationFilter arg2, Object arg3)
			throws InstanceNotFoundException {
		if (delegate)
			try {
				connection.addNotificationListener(arg0, arg1, arg2, arg3);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when addNotificationListener", e);
			}
		else
			((MBeanServer) connection).addNotificationListener(arg0, arg1,
					arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void addNotificationListener(ObjectName arg0, ObjectName arg1,
			NotificationFilter arg2, Object arg3)
			throws InstanceNotFoundException {
		if (delegate)
			try {
				connection.addNotificationListener(arg0, arg1, arg2, arg3);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when addNotificationListener", e);
			}
		else
			((MBeanServer) connection).addNotificationListener(arg0, arg1,
					arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName)
	 */
	public ObjectInstance createMBean(String arg0, ObjectName arg1)
			throws ReflectionException, InstanceAlreadyExistsException,
			MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException {
		if (delegate)
			try {
				return connection.createMBean(arg0, arg1);
			} catch (ReflectionException e) {
				throw e;
			} catch (InstanceAlreadyExistsException e) {
				throw e;
			} catch (MBeanRegistrationException e) {
				throw e;
			} catch (MBeanException e) {
				throw e;
			} catch (NotCompliantMBeanException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when createMBean", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).createMBean(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName)
	 */
	public ObjectInstance createMBean(String arg0, ObjectName arg1,
			ObjectName arg2) throws ReflectionException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			MBeanException, NotCompliantMBeanException,
			InstanceNotFoundException {
		if (delegate)
			try {
				return connection.createMBean(arg0, arg1, arg2);
			} catch (ReflectionException e) {
				throw e;
			} catch (InstanceAlreadyExistsException e) {
				throw e;
			} catch (MBeanRegistrationException e) {
				throw e;
			} catch (MBeanException e) {
				throw e;
			} catch (NotCompliantMBeanException e) {
				throw e;
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when createMBean", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).createMBean(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	public ObjectInstance createMBean(String arg0, ObjectName arg1,
			ObjectName arg2, Object[] arg3, String[] arg4)
			throws ReflectionException, InstanceAlreadyExistsException,
			MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, InstanceNotFoundException {
		if (delegate)
			try {
				return connection.createMBean(arg0, arg1, arg2, arg3, arg4);
			} catch (ReflectionException e) {
				throw e;
			} catch (InstanceAlreadyExistsException e) {
				throw e;
			} catch (MBeanRegistrationException e) {
				throw e;
			} catch (MBeanException e) {
				throw e;
			} catch (NotCompliantMBeanException e) {
				throw e;
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when createMBean", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).createMBean(arg0, arg1, arg2,
					arg3, arg4);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	public ObjectInstance createMBean(String arg0, ObjectName arg1,
			Object[] arg2, String[] arg3) throws ReflectionException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			MBeanException, NotCompliantMBeanException {
		if (delegate)
			try {
				return connection.createMBean(arg0, arg1, arg2, arg3);
			} catch (ReflectionException e) {
				throw e;
			} catch (InstanceAlreadyExistsException e) {
				throw e;
			} catch (MBeanRegistrationException e) {
				throw e;
			} catch (MBeanException e) {
				throw e;
			} catch (NotCompliantMBeanException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when createMBean", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).createMBean(arg0, arg1, arg2,
					arg3);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName, java.lang.String)
	 */
	public Object getAttribute(ObjectName arg0, String arg1)
			throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException {
		if (delegate)
			try {
				return connection.getAttribute(arg0, arg1);
			} catch (ReflectionException e) {
				throw e;
			} catch (MBeanException e) {
				throw e;
			} catch (AttributeNotFoundException e) {
				throw e;
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when getAttribute", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).getAttribute(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName, java.lang.String[])
	 */
	public AttributeList getAttributes(ObjectName arg0, String[] arg1)
			throws InstanceNotFoundException, ReflectionException {
		if (delegate)
			try {
				return connection.getAttributes(arg0, arg1);
			} catch (ReflectionException e) {
				throw e;
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when getAttributes", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).getAttributes(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#getDefaultDomain()
	 */
	public String getDefaultDomain() {
		if (delegate)
			try {
				return connection.getDefaultDomain();
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when getDefaultDomain", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).getDefaultDomain();
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#getDomains()
	 */
	public String[] getDomains() {
		if (delegate)
			try {
				return connection.getDomains();
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when getDomains", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).getDomains();
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#getMBeanCount()
	 */
	public Integer getMBeanCount() {
		if (delegate)
			try {
				return connection.getMBeanCount();
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when getMBeanCount", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).getMBeanCount();
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
	 */
	public MBeanInfo getMBeanInfo(ObjectName arg0)
			throws InstanceNotFoundException, IntrospectionException,
			ReflectionException {
		if (delegate)
			try {
				return connection.getMBeanInfo(arg0);
			} catch (ReflectionException e) {
				throw e;
			} catch (IntrospectionException e) {
				throw e;
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when getMBeanInfo", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).getMBeanInfo(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
	 */
	public ObjectInstance getObjectInstance(ObjectName arg0)
			throws InstanceNotFoundException {
		if (delegate)
			try {
				return connection.getObjectInstance(arg0);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when getObjectInstance", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).getObjectInstance(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(ObjectName arg0, String arg1, Object[] arg2,
			String[] arg3) throws InstanceNotFoundException, MBeanException,
			ReflectionException {
		if (delegate)
			try {
				return connection.invoke(arg0, arg1, arg2, arg3);
			} catch (ReflectionException e) {
				throw e;
			} catch (MBeanException e) {
				throw e;
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when invoke", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).invoke(arg0, arg1, arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#isInstanceOf(javax.management.ObjectName, java.lang.String)
	 */
	public boolean isInstanceOf(ObjectName arg0, String arg1)
			throws InstanceNotFoundException {
		if (delegate)
			try {
				return connection.isInstanceOf(arg0, arg1);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when isInstanceOf", e);
				//never go to here. It is just for compilation
				return false;
			}
		else
			return ((MBeanServer) connection).isInstanceOf(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#isRegistered(javax.management.ObjectName)
	 */
	public boolean isRegistered(ObjectName arg0) {
		if (delegate)
			try {
				return connection.isRegistered(arg0);
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when isRegistered", e);
				//never go to here. It is just for compilation
				return false;
			}
		else
			return ((MBeanServer) connection).isRegistered(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
	 */

	public Set queryMBeans(ObjectName arg0, QueryExp arg1) {
		if (delegate)
			try {
				return connection.queryMBeans(arg0, arg1);
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when queryMBeans", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).queryMBeans(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName, javax.management.QueryExp)
	 */
	
	public Set queryNames(ObjectName arg0, QueryExp arg1) {
		if (delegate)
			try {
				return connection.queryNames(arg0, arg1);
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when queryNames", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).queryNames(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener)
	 */
	public void removeNotificationListener(ObjectName arg0,
			NotificationListener arg1) throws InstanceNotFoundException,
			ListenerNotFoundException {
		if (delegate)
			try {
				connection.removeNotificationListener(arg0, arg1);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (ListenerNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when removeNotificationListener", e);
			}
		else {
			((MBeanServer) connection).removeNotificationListener(arg0, arg1);
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void removeNotificationListener(ObjectName arg0,
			NotificationListener arg1, NotificationFilter arg2, Object arg3)
			throws InstanceNotFoundException, ListenerNotFoundException {
		if (delegate)
			try {
				connection.removeNotificationListener(arg0, arg1, arg2, arg3);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (ListenerNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when removeNotificationListener", e);
			}
		else
			((MBeanServer) connection).removeNotificationListener(arg0, arg1,
					arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName)
	 */
	public void removeNotificationListener(ObjectName arg0, ObjectName arg1)
			throws InstanceNotFoundException, ListenerNotFoundException {
		if (delegate)
			try {
				connection.removeNotificationListener(arg0, arg1);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (ListenerNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when removeNotificationListener", e);
			}
		else
			((MBeanServer) connection).removeNotificationListener(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void removeNotificationListener(ObjectName arg0, ObjectName arg1,
			NotificationFilter arg2, Object arg3)
			throws InstanceNotFoundException, ListenerNotFoundException {
		if (delegate)
			try {
				connection.removeNotificationListener(arg0, arg1, arg2, arg3);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (ListenerNotFoundException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when removeNotificationListener", e);
			}
		else
			((MBeanServer) connection).removeNotificationListener(arg0, arg1,
					arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#setAttribute(javax.management.ObjectName, javax.management.Attribute)
	 */
	public void setAttribute(ObjectName arg0, Attribute arg1)
			throws InstanceNotFoundException, AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
		if (delegate)
			try {
				connection.setAttribute(arg0, arg1);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (AttributeNotFoundException e) {
				throw e;
			} catch (InvalidAttributeValueException e) {
				throw e;
			} catch (MBeanException e) {
				throw e;
			} catch (ReflectionException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when setAttribute", e);
			}
		else
			((MBeanServer) connection).setAttribute(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName, javax.management.AttributeList)
	 */
	public AttributeList setAttributes(ObjectName arg0, AttributeList arg1)
			throws InstanceNotFoundException, ReflectionException {
		if (delegate)
			try {
				return connection.setAttributes(arg0, arg1);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (ReflectionException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when setAttributes", e);
				//never go to here. It is just for compilation
				return null;
			}
		else
			return ((MBeanServer) connection).setAttributes(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServerConnection#unregisterMBean(javax.management.ObjectName)
	 */
	public void unregisterMBean(ObjectName arg0)
			throws InstanceNotFoundException, MBeanRegistrationException {
		if (delegate)
			try {
				connection.unregisterMBean(arg0);
			} catch (InstanceNotFoundException e) {
				throw e;
			} catch (MBeanRegistrationException e) {
				throw e;
			} catch (Throwable e) {
				JMXRemoteUtil.throwJMException("Error when unregisterMBean", e);
			}
		else
			((MBeanServer) connection).unregisterMBean(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#instantiate(java.lang.String)
	 */
	public Object instantiate(String arg0) throws ReflectionException,
			MBeanException {
		if (delegate)
			throw new UnsupportedOperationException("instantiate is not supported");
		else
			return ((MBeanServer) connection).instantiate(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#instantiate(java.lang.String, javax.management.ObjectName)
	 */
	public Object instantiate(String arg0, ObjectName arg1)
			throws ReflectionException, MBeanException,
			InstanceNotFoundException {
		if (delegate)
			throw new UnsupportedOperationException("instantiate is not supported");
		else
			return ((MBeanServer) connection).instantiate(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#instantiate(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object instantiate(String arg0, Object[] arg1, String[] arg2)
			throws ReflectionException, MBeanException {
		if (delegate)
			throw new UnsupportedOperationException("instantiate is not supported");
		else
			return ((MBeanServer) connection).instantiate(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#instantiate(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	public Object instantiate(String arg0, ObjectName arg1, Object[] arg2,
			String[] arg3) throws ReflectionException, MBeanException,
			InstanceNotFoundException {
		if (delegate)
			throw new UnsupportedOperationException("instantiate is not supported");
		else
			return ((MBeanServer) connection).instantiate(arg0, arg1, arg2,
					arg3);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#deserialize(javax.management.ObjectName, byte[])
	 */
	
	public ObjectInputStream deserialize(ObjectName arg0, byte[] arg1)
			throws InstanceNotFoundException, OperationsException {
		if (delegate)
			throw new UnsupportedOperationException("instantiate is not supported");
		else
			return ((MBeanServer) connection).deserialize(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#deserialize(java.lang.String, byte[])
	 */
	
	public ObjectInputStream deserialize(String arg0, byte[] arg1)
			throws OperationsException, ReflectionException {
		if (delegate)
			throw new UnsupportedOperationException("deserialize is not supported");
		else
			return ((MBeanServer) connection).deserialize(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#deserialize(java.lang.String, javax.management.ObjectName, byte[])
	 */
	
	public ObjectInputStream deserialize(String arg0, ObjectName arg1,
			byte[] arg2) throws InstanceNotFoundException, OperationsException,
			ReflectionException {
		if (delegate)
			throw new UnsupportedOperationException("deserialize is not supported");
		else
			return ((MBeanServer) connection).deserialize(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#getClassLoaderFor(javax.management.ObjectName)
	 */
	public ClassLoader getClassLoaderFor(ObjectName arg0)
			throws InstanceNotFoundException {
		if (delegate)
			throw new UnsupportedOperationException("getClassLoaderFor is not supported");
		else
			return ((MBeanServer) connection).getClassLoaderFor(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#getClassLoader(javax.management.ObjectName)
	 */
	public ClassLoader getClassLoader(ObjectName arg0)
			throws InstanceNotFoundException {
		if (delegate)
			throw new UnsupportedOperationException("getClassLoader is not supported");
		else
			return ((MBeanServer) connection).getClassLoader(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#getClassLoaderRepository()
	 */
	public ClassLoaderRepository getClassLoaderRepository() {
		if (delegate)
			throw new UnsupportedOperationException("getClassLoaderRepository is not supported");
		else
			return ((MBeanServer) connection).getClassLoaderRepository();
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanServer#registerMBean(java.lang.Object, javax.management.ObjectName)
	 */
	public synchronized ObjectInstance registerMBean(Object inst, ObjectName name)
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		if (delegate) {
			ObjectInstance ret = null;
			if (inst == null)
				throw new NullPointerException();
			MBeanInfoCollector collector = new MBeanInfoCollector(inst);
			try {
				lock.lock();
				synchronized (mBeanMap) {
					mBeanMap.put(name, collector);
				}
				try {
					ret = adapter.createDynamicMBean(collector.getMBeanInfo(), collector.getMBeanInfoExt().toOpenData(), name);
					synchronized (mBeanMap) {
						mBeanMap.remove(name);
						mBeanMap.put(ret.getObjectName(), collector);
					}
					if (collector.getMBeanInfoExt().isNotificationBroadcaster()) {
						((NotificationBroadcaster)inst).addNotificationListener(new RemoteNotificationListener(this), null, ret.getObjectName());
					}
				} finally {
					if (ret == null)
					synchronized (mBeanMap) {
						mBeanMap.remove(name);
					}
				}
			} catch (MBeanRegistrationException e) {
				throw e;	
			} catch (MBeanException e) {
				Throwable t = e.getCause();
				if (t != null) {
					if (t instanceof InstanceAlreadyExistsException)
						throw (InstanceAlreadyExistsException)t;
					else if (t instanceof MBeanRegistrationException)
						throw (MBeanRegistrationException)t;
					else if (t instanceof NotCompliantMBeanException)
						throw (NotCompliantMBeanException)t;
				}
				JMXRemoteUtil.throwJMException("Error when registerMBean", t);
			} catch (Throwable t) {
				JMXRemoteUtil.throwJMException("Error when registerMBean", t);
			} finally {
				lock.unlock();
			}
			return ret;
		} else
			return ((MBeanServer) connection).registerMBean(inst, name);
	}

	protected void createAdapter() throws InstanceAlreadyExistsException,
			MBeanRegistrationException, NotCompliantMBeanException {
		try {
			this.getObjectInstance(adapterName);
		} catch (Exception e) {
			try {
				this.createMBean("org.jingle.jmxremote.server.RemoteAdapter", adapterName, new Object[] {this.remoteMBeanServerSettings.toOpenData()}, new String[] {CompositeData.class.getName()});
				adapter = (RemoteAdapterMBean) MBeanServerInvocationHandler.newProxyInstance(connection, adapterName, RemoteAdapterMBean.class, false);
				this.addNotificationListener(adapterName, this, new JMXRemoteNotificationFilter(this.remoteMBeanServerSettings.getClientId()), JMXRemoteNotification.NOTIFICATION_HANDBACK);
			} catch (InstanceAlreadyExistsException e1) {
				throw e1;
			} catch (MBeanRegistrationException e1) {
				throw e1;
			} catch (NotCompliantMBeanException e1) {
				throw e1;
			} catch (Throwable t) {
				// @ TODO remove system.outs
				System.out.println("ATTENTION");
				t.printStackTrace();
				JMXRemoteUtil.throwJMException("Error when create adapter", t);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
	public void handleNotification(Notification notification, Object handback) {
		try {
			String type = notification.getType();
			if (JMXRemoteNotification.SET_ATTR_TYPE.equals(type) || JMXRemoteNotification.SET_ATTR_OLD_TYPE.equals(type)) {
				invokeSetter((ISetAttributeNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.GET_ATTR_TYPE.equals(type)) {
				invokeGetter((IGetAttributeNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.DO_OPER_TYPE.equals(type) || JMXRemoteNotification.DO_OPER_OLD_TYPE.equals(type)) {
				invokeOper((IDoOperNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.SET_ATTRS_TYPE.equals(type) || JMXRemoteNotification.SET_ATTRS_OLD_TYPE.equals(type)) {
				invokeSetters((ISetAttributesNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.GET_ATTRS_TYPE.equals(type)) {
				invokeGetters((IGetAttributesNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.PRE_REGISTER_TYPE.equals(type) || JMXRemoteNotification.PRE_REGISTER_OLD_TYPE.equals(type)) {
				invokePreRegister((IRegistrationNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.POST_REGISTER_TYPE.equals(type) || JMXRemoteNotification.POST_REGISTER_OLD_TYPE.equals(type)) {
				invokePostRegister((IRegistrationNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.PRE_DEREGISTER_TYPE.equals(type) || JMXRemoteNotification.PRE_DEREGISTER_OLD_TYPE.equals(type)) {
				invokePreDeregister((IRegistrationNotification)JMXRemoteNotification.fromNotification(notification));
			} else if (JMXRemoteNotification.POST_DEREGISTER_TYPE.equals(type) || JMXRemoteNotification.POST_DEREGISTER_OLD_TYPE.equals(type)) {
				invokePostDeregister((IRegistrationNotification)JMXRemoteNotification.fromNotification(notification));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Get the remote adapter name.
	 * @return Remote adapter name.
	 */
	public ObjectName getAdapterName() {
		return adapterName;
	}
	
	protected void invokeSetter(ISetAttributeNotification setAttrNotif) {
		ObjectName name = (ObjectName) setAttrNotif.getSource();
		String attr = setAttrNotif.getAttr();
		Object value = setAttrNotif.getValue();
		long sequence = setAttrNotif.getSequenceNumber();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		try {
			if (collector != null) {
				Object instance = collector.getInstance();
				if (instance instanceof DynamicMBean) {
					((DynamicMBean)instance).setAttribute(new Attribute(attr, value));
					try {
						adapter.returnResult(name, sequence, new Attribute(attr, value), false);
					} catch (Exception e) {
						throw new ReflectionException(e);
					}
				} else {
					Method setter = collector.getAttributeWritter(attr);
					if (setter != null) {
						try {
	    					setter.invoke(instance, new Object[] {value});
						} catch (InvocationTargetException e) {
							wrapMethodException(setter, e);
						} catch (IllegalArgumentException e) {
							throw new InvalidAttributeValueException(e.toString());
						} catch (Exception e) {
							throw new ReflectionException(e);
						}
						try {
							adapter.returnResult(name, sequence, new Attribute(attr, value), false);
						} catch (Exception e) {
							throw new ReflectionException(e);
						}
					} else {
						throw new AttributeNotFoundException("Attribute [" + attr + "] is not writable");
					}
				}
			} else {
				throw new AttributeNotFoundException("Can not find MBean [" + name + "]");
			}
		} catch (Throwable t) {
			try {
				Throwable exception = null;
				if (t instanceof MBeanException)
					exception = t;
				if (t instanceof ReflectionException)
					exception = t;
				else if (t instanceof InvalidAttributeValueException)
					exception = t;
				else if (t instanceof AttributeNotFoundException)
					exception = t;
				else if (t instanceof Exception)
					exception = new ReflectionException((Exception)t);
				else
					exception = new JMRuntimeException(t.toString());
				adapter.returnException(name, sequence, exception, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void invokeSetters(ISetAttributesNotification setAttrsNotif) {
		ObjectName name = (ObjectName) setAttrsNotif.getSource();
		AttributeList attrs = setAttrsNotif.getAttrs();
		long sequence = setAttrsNotif.getSequenceNumber();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		try {
			if (collector != null) {
				Object instance = collector.getInstance();
				AttributeList result = null;
				if (instance instanceof DynamicMBean) {
					result = ((DynamicMBean)instance).setAttributes(attrs);
				} else {
					result = new AttributeList();
					
					for (int i = 0; i < attrs.size(); i++) {
						Object element = attrs.get(i);
						Attribute attr = (Attribute)element;
						Method setter = collector.getAttributeWritter(attr.getName());
						if (setter != null) {
							try {
								setter.invoke(instance, new Object[] {attr.getValue()});
								result.add(new Attribute(attr.getName(), attr.getValue()));
							} catch (Throwable e) {
							}
						}
					}
				}
				adapter.returnResults(name, sequence, result);
			} else {
				throw new JMRuntimeException("Can not find MBean [" + name + "]");
			}
		} catch (Throwable t) {
			try {
				Throwable exception = null;
				if (t instanceof JMRuntimeException) {
					exception = t;
				} else
				    exception  = new JMRuntimeException(t.toString());
				adapter.returnException(name, sequence, exception, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void invokeGetter(IGetAttributeNotification getAttrNotif) {
		ObjectName name = (ObjectName) getAttrNotif.getSource();
		String attr = getAttrNotif.getAttr();
		long sequence = getAttrNotif.getSequenceNumber();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		try {
			if (collector != null) {
				Object instance = collector.getInstance();
				Object value = null;
				if (instance instanceof DynamicMBean) {
					value = ((DynamicMBean)instance).getAttribute(attr);
					try {
						adapter.returnResult(name, sequence, new Attribute(attr, value), false);
					} catch (Exception e) {
						throw new ReflectionException(e);
					}
				} else {
					Method getter = collector.getAttributeReader(attr);
					if (getter != null) {
						try {
						    value = getter.invoke(instance, new Object[0]);
						} catch (InvocationTargetException e) {
							wrapMethodException(getter, e);
						} catch (Exception e) {
							throw new ReflectionException(e);
						}
						try {
							adapter.returnResult(name, sequence, new Attribute(attr, value), false);
						} catch (Exception e) {
							throw new ReflectionException(e);
						}
					} else {
						throw new AttributeNotFoundException("Attribute [" + attr + "] is not readable");
					}
				}
			} else {
				throw new AttributeNotFoundException("Can not find MBean [" + name + "]");
			}
		} catch (Throwable t) {
			try {
				Throwable exception = null;
				if (t instanceof MBeanException)
					exception = t;
				if (t instanceof ReflectionException)
					exception = t;
				else if (t instanceof AttributeNotFoundException)
					exception = t;
				else if (t instanceof Exception)
					exception = new ReflectionException((Exception)t);
				else
					exception = new JMRuntimeException(t.toString());
				adapter.returnException(name, sequence, exception, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void invokeGetters(IGetAttributesNotification getAttrsNotif) {
		ObjectName name = (ObjectName) getAttrsNotif.getSource();
		String[] attrs = getAttrsNotif.getAttrs();
		long sequence = getAttrsNotif.getSequenceNumber();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		try {
			if (collector != null) {
				Object instance = collector.getInstance();
				AttributeList result = null;
				if (instance instanceof DynamicMBean) {
					result = ((DynamicMBean)instance).getAttributes(attrs);
				} else {
					result = new AttributeList();
					
					for (int i =0; i < attrs.length; i++) {
						String attr = attrs[i];
						Method getter = collector.getAttributeReader(attr);
						Object value = null;
						if (getter != null) {
							try {
							    value = getter.invoke(instance, new Object[0]);
								result.add(new Attribute(attr, value));
							} catch (Throwable e) {
							}
						}
					}
				}
				adapter.returnResults(name, sequence, result);
			} else {
				throw new JMRuntimeException("Can not find MBean [" + name + "]");
			}
		} catch (Throwable t) {
			try {
				Throwable exception = null;
				if (t instanceof JMRuntimeException) {
					exception = t;
				} else
				    exception  = new JMRuntimeException(t.toString());
				adapter.returnException(name, sequence, exception, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void invokeOper(IDoOperNotification doOperNotif) {
		ObjectName name = (ObjectName) doOperNotif.getSource();
		String oper = doOperNotif.getOpName();
		long sequence = doOperNotif.getSequenceNumber();
		Object[] params = doOperNotif.getParams();
		String[] signature = doOperNotif.getSignature();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		try {
			if (collector != null) {
				Object instance = collector.getInstance();
				Object value = null;
				if ("getResourceAsStream".equals(oper) && (signature.length == 1) && String.class.getName().equals(signature[0])) {
					if (collector.getMBeanInfoExt().isClassLoader()) {
						value = loadClassData((ClassLoader)collector.getInstance(), (String) params[0]);
						try {
							adapter.returnResult(name, sequence, new Attribute(oper, value), false);
						} catch (Exception e) {
							throw new ReflectionException(e);
						}
					} 
				}
				if (instance instanceof DynamicMBean) {
					value = ((DynamicMBean)instance).invoke(oper, params, signature);
					try {
						adapter.returnResult(name, sequence, new Attribute(oper, value), false);
					} catch (Exception e) {
						throw new ReflectionException(e);
					}
				} else {
					Method m = collector.getOperationMethod(oper, signature);
					if (m != null) {
						try {
						    value = m.invoke(instance, params);
						} catch (InvocationTargetException e) {
							wrapMethodException(m, e);
						} catch (Exception e) {
							throw new ReflectionException(e);
						}
						try {
							adapter.returnResult(name, sequence, new Attribute(oper, value), false);
						} catch (Exception e) {
							throw new ReflectionException(e);
						}
					} else {
						throw new JMRuntimeException("Can not find operation [" + oper + "]");
					}
				}
			} else {
				throw new JMRuntimeException("Can not find MBean [" + name + "]");
			}
		} catch (Throwable t) {
			try {
				Throwable exception = null;
				if (t instanceof MBeanException)
					exception = t;
				if (t instanceof ReflectionException)
					exception = t;
				else if (t instanceof Exception)
					exception = new ReflectionException((Exception)t);
				else
					exception = new JMRuntimeException(t.toString());
				adapter.returnException(name, sequence, exception, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
    private byte[] loadClassData(ClassLoader loader, String className) {
    	InputStream is = null;
    	ByteArrayOutputStream bos = null;
    	try {
	    	String path = className.replace('.', '/') + ".class";
	    	is = loader.getResourceAsStream(path);
	    	if (is != null) {
		    	bos = new ByteArrayOutputStream();
		    	byte[] bytes = new byte[1024];
		    	int length = 0;
		    	while ((length = is.read(bytes)) >= 0) {
		    		bos.write(bytes, 0, length);
		    	}
		    	return bos.toByteArray();
	    	} else
	    		return null;
    	} catch (Exception e) {
    		return null;
    	} finally {
    		if (is != null) {
    			try {
					is.close();
				} catch (IOException e) {
				}
    		}
    		if (bos != null) {
    			try {
    				bos.close();
				} catch (IOException e) {
				}
    		}
    	}
    }

	protected void invokePreRegister(IRegistrationNotification regNotif) {
		ObjectName name = (ObjectName) regNotif.getSource();
		long sequence = regNotif.getSequenceNumber();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		try {
			if ((collector != null) && (collector.getInstance() instanceof MBeanRegistration)) {
				MBeanRegistration instance = (MBeanRegistration)collector.getInstance();
				ObjectName result = null;
				result = instance.preRegister(this, name);
				synchronized (mBeanMap) {
					mBeanMap.remove(name);
					mBeanMap.put(result, collector);
				}
				adapter.returnResult(name, sequence, new Attribute("", result), true);
			} else {
				throw new JMRuntimeException("Can not find MBean [" + name + "]. Or the MBean does not implement MBeanRegistration");
			}
		} catch (Throwable t) {
			try {
				Throwable exception = null;
				if (t instanceof Exception)
					exception = t;
				else
					exception = new JMRuntimeException(t.toString());
				adapter.returnException(name, sequence, exception, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void invokePostRegister(IRegistrationNotification regNotif) {
		ObjectName name = (ObjectName) regNotif.getSource();
		Object done = regNotif.getPayload();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		if ((collector != null) && (collector.getInstance() instanceof MBeanRegistration)) {
			MBeanRegistration instance = (MBeanRegistration)collector.getInstance();
			instance.postRegister((Boolean) done);
		} else {
			throw new JMRuntimeException("Can not find MBean [" + name + "]. Or the MBean does not implement MBeanRegistration");
		}
	}

	protected void invokePreDeregister(IRegistrationNotification regNotif) {
		ObjectName name = (ObjectName) regNotif.getSource();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		if ((collector != null) && (collector.getMBeanInfoExt().isMBeanRegistration())) {
			MBeanRegistration instance = (MBeanRegistration)collector.getInstance();
			try {
			    instance.preDeregister();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new JMRuntimeException("Can not find MBean [" + name + "]. Or the MBean does not implement MBeanRegistration");
		}
	}

	protected void invokePostDeregister(IRegistrationNotification regNotif) {
		ObjectName name = (ObjectName) regNotif.getSource();
		MBeanInfoCollector collector = null;
		synchronized (mBeanMap) {
			collector = (MBeanInfoCollector) mBeanMap.get(name);
		}
		if ((collector != null) && (collector.getInstance() instanceof MBeanRegistration)) {
			try {
				MBeanRegistration instance = (MBeanRegistration)collector.getInstance();
				instance.postDeregister();
			} finally {
				synchronized (mBeanMap) {
					mBeanMap.remove(name);
				}
			}
		} else {
			throw new JMRuntimeException("Can not find MBean [" + name + "]. Or the MBean does not implement MBeanRegistration");
		}
	}

	protected void invokePing() {
		this.remoteMBeanServerSettings.tick();
		try {
			adapter.ping();
			this.remoteMBeanServerSettings.renew();
		} catch (Exception e) {
		}
		if (this.remoteMBeanServerSettings.isExpired()) {
			List mBeanInfoCollectorList = new ArrayList();
			synchronized (mBeanMap) {
				mBeanInfoCollectorList.addAll(mBeanMap.values());
			}
			
			for (int i = 0; i < mBeanInfoCollectorList.size(); i++) {
				MBeanInfoCollector collector = (MBeanInfoCollector) mBeanInfoCollectorList.get(i);
				MBeanInfoExt mBeanInfoExt = collector.getMBeanInfoExt();
				if (mBeanInfoExt.isMBeanRegistration()) {
					MBeanRegistration inst = (MBeanRegistration)collector.getInstance();
					try {
						inst.preDeregister();
					} catch (Exception e) {
					}
					try {
						inst.postDeregister();
					} catch (Exception e) {
					}
				}
			}
			synchronized (mBeanMap) {
				mBeanMap.clear();
			}
		}
	}

	protected void wrapMethodException(Method m, InvocationTargetException exception) throws MBeanException, ReflectionException {
		Class[] exTypes = m.getExceptionTypes();
		Throwable cause = exception.getTargetException();
		if ((cause == null) || !(cause instanceof Exception))
			throw new ReflectionException(exception);
		Class causeType = cause.getClass();
		
		for (int i = 0; i < exTypes.length; i++) {
			Class exType = exTypes[i];
			if (causeType == exType)
				throw new MBeanException((Exception)cause);
		}
		if (cause instanceof RuntimeException)
			throw new MBeanException((Exception)cause);
		throw new ReflectionException(exception);
	}
}

