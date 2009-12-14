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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import org.jingle.jmxremote.common.DoOperNotification;
import org.jingle.jmxremote.common.DoOperNotification_OLD;
import org.jingle.jmxremote.common.JMXRemoteNotification;
import org.jingle.jmxremote.common.JMXRemoteUtil;

public class RemoteClassLoader extends ClassLoader implements IRemoteDynamicMBean {
	
	private IRemoteDynamicMBean delegate = null;
	private RemoteAdapter adapter = null;
	private ObjectName name = null;

	
	public RemoteClassLoader(RemoteAdapter adapter, IRemoteDynamicMBean delegate) {
		this.adapter = adapter;
		this.delegate = delegate;
	}

	public void addNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws IllegalArgumentException {
		delegate.addNotificationListener(listener, filter, handback);
	}

	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		return delegate.getAttribute(attribute);
	}

	public AttributeList getAttributes(String[] attributes) {
		return delegate.getAttributes(attributes);
	}

	public MBeanInfo getMBeanInfo() {
		return delegate.getMBeanInfo();
	}

	public MBeanNotificationInfo[] getNotificationInfo() {
		return delegate.getNotificationInfo();
	}

	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		return delegate.invoke(actionName, params, signature);
	}

	public void load() throws MBeanException, RuntimeOperationsException,
			InstanceNotFoundException {
		delegate.load();
	}

	public void postDeregister() {
		delegate.postDeregister();
	}

	public void postRegister(Boolean registrationDone) {
		delegate.postRegister(registrationDone);
	}

	public void preDeregister() throws Exception {
		delegate.preDeregister();
	}

	public ObjectName preRegister(MBeanServer server, ObjectName name)
			throws Exception {
		this.name = delegate.preRegister(server, name);
		return this.name;
	}

	public void removeNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws ListenerNotFoundException {
		delegate.removeNotificationListener(listener, filter, handback);
	}

	public void removeNotificationListener(NotificationListener listener)
			throws ListenerNotFoundException {
		delegate.removeNotificationListener(listener);
	}

	public void sendNotification(Notification notification) {
		delegate.sendNotification(notification);
	}

	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		delegate.setAttribute(attribute);
	}

	public AttributeList setAttributes(AttributeList attributes) {
		return delegate.setAttributes(attributes);
	}

	public void store() throws MBeanException, RuntimeOperationsException,
			InstanceNotFoundException {
		delegate.store();
	}

	protected Class findClass(String name) throws ClassNotFoundException {
		byte[] b = loadClassData(name);
		if (b == null)
			throw new ClassNotFoundException(name);
		return defineClass(name, b, 0, b.length);
	}
	
    private byte[] loadClassData(String className) {
		JMXRemoteNotification notif = null; 
		if (JMXRemoteUtil.isJDK15())
			notif = new DoOperNotification_OLD("getResourceAsStream", new Object[] {className}, new String[] {String.class.getName()}, name, adapter.getRemoteMBeanServerSettings().getClientId());
		else
			notif = new DoOperNotification("getResourceAsStream", new Object[] {className}, new String[] {String.class.getName()}, name, adapter.getRemoteMBeanServerSettings().getClientId());

		ResultContainer entry = new ResultContainer(ResultContainer.OP_RESULT);
		
		try {
			byte[] ret = (byte[]) this.adapter.invokeRemote(notif, entry);
			return ret;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
    }
}
