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

import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import org.jingle.jmxremote.common.DoOperNotification;
import org.jingle.jmxremote.common.DoOperNotification_OLD;
import org.jingle.jmxremote.common.GetAttributeNotification;
import org.jingle.jmxremote.common.GetAttributesNotification;
import org.jingle.jmxremote.common.JMXRemoteNotification;
import org.jingle.jmxremote.common.JMXRemoteUtil;
import org.jingle.jmxremote.common.MBeanInfoExt;
import org.jingle.jmxremote.common.RegistrationNotification;
import org.jingle.jmxremote.common.RegistrationNotification_OLD;
import org.jingle.jmxremote.common.SetAttributeNotification;
import org.jingle.jmxremote.common.SetAttributeNotification_OLD;
import org.jingle.jmxremote.common.SetAttributesNotification;
import org.jingle.jmxremote.common.SetAttributesNotification_OLD;

public class RemoteDynamicMBean extends NotificationBroadcasterSupport implements IRemoteDynamicMBean {

	MBeanInfoAccessor mBeanAccessor = null;
	MBeanInfoExt mBeanInfoExt = null;
	ObjectName name = null;
	RemoteAdapter adapter;
	
	

	
	public RemoteDynamicMBean(MBeanInfo mBeanInfo, MBeanInfoExt mBeanInfoExt, RemoteAdapter adapter) throws JMException {
		this.mBeanAccessor = new MBeanInfoAccessor(mBeanInfo, mBeanInfoExt);
		this.adapter = adapter;
	}
	
	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attr) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		MBeanAttributeInfo attrInfo = mBeanAccessor.getAttribute(attr);
		if (attrInfo == null)
			throw new AttributeNotFoundException("Can not find attribute [" + attr + "]");
		if (!attrInfo.isReadable())
			throw new AttributeNotFoundException("Attribute [" + attr + "] is not writable");
		JMXRemoteNotification notif = new GetAttributeNotification(attr, name, adapter.getRemoteMBeanServerSettings().getClientId());
		ResultContainer entry = new ResultContainer(ResultContainer.GETTER_RESULT);
		try {
			return this.adapter.invokeRemote(notif, entry);
		} catch (AttributeNotFoundException e) {
			throw e;
		} catch (MBeanException e) {
			throw e;
		} catch (ReflectionException e) {
			throw e;		
		} catch (Throwable e) {
			JMXRemoteUtil.throwJMException("Error when getAttribute", e);
			//Never get here. Just for compilation
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute(Attribute attr) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
		String attrName = attr.getName();
		MBeanAttributeInfo attrInfo = mBeanAccessor.getAttribute(attrName);
		if (attrInfo == null)
			throw new AttributeNotFoundException("Can not find attribute [" + attrName + "]");
		if (!attrInfo.isWritable())
			throw new AttributeNotFoundException("Attribute [" + attrName + "] is not writable");
		JMXRemoteNotification notif = null;
		if (JMXRemoteUtil.isJDK15())
			notif = new SetAttributeNotification_OLD(attrName, attr.getValue(), name, adapter.getRemoteMBeanServerSettings().getClientId());
		else
			notif = new SetAttributeNotification(attrName, attr.getValue(), name, adapter.getRemoteMBeanServerSettings().getClientId());
		ResultContainer entry = new ResultContainer(ResultContainer.SETTER_RESULT);
		
		try {
			this.adapter.invokeRemote(notif, entry);
		} catch (AttributeNotFoundException e) {
			throw e;
		} catch (InvalidAttributeValueException e) {
			throw e;
		} catch (MBeanException e) {
			throw e;
		} catch (ReflectionException e) {
			throw e;		
		} catch (Throwable e) {
			e.printStackTrace();
			JMXRemoteUtil.throwJMException("Error when setAttribute", e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	public AttributeList getAttributes(String[] attrs) {
		List actualAttrs = new ArrayList();
		List attrInfos = new ArrayList();
		
		for(int i = 0; i < attrs.length; i++) {
			String attr = attrs[i];
			MBeanAttributeInfo attrInfo = mBeanAccessor.getAttribute(attr);
			if ((attrInfo != null) && (attrInfo.isReadable())) {
				actualAttrs.add(attr);
				attrInfos.add(attrInfo);
			}
		}
		JMXRemoteNotification notif = new GetAttributesNotification(
				(String[])actualAttrs.toArray(new String[0]),
				name,
				adapter.getRemoteMBeanServerSettings().getClientId().toString()
				);
		ResultContainer entry = new ResultContainer(ResultContainer.GETTERS_RESULT);
		
		try {
			return (AttributeList) this.adapter.invokeRemote(notif, entry);	
		} catch (Throwable e) {
			JMXRemoteUtil.throwJMException("Error when getAttributes", e);
			//Never get here. Just for compilation
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	public AttributeList setAttributes(AttributeList attrs) {
		AttributeList actualAttrs = new AttributeList();
		List attrInfos = new ArrayList();
		
		for(int i = 0; i < attrs.size(); i++) {
			Object attr = attrs.get(i);
			Attribute attribute = (Attribute)attr;
			MBeanAttributeInfo attrInfo = mBeanAccessor.getAttribute(attribute.getName());
			if ((attrInfo != null) && (attrInfo.isWritable())) {
				actualAttrs.add(attribute);
				attrInfos.add(attrInfo);
			}
		}
		JMXRemoteNotification notif = null;
		if (JMXRemoteUtil.isJDK15())
			notif = new SetAttributesNotification_OLD(actualAttrs, name, adapter.getRemoteMBeanServerSettings().getClientId());
		else
			notif = new SetAttributesNotification(actualAttrs, name, adapter.getRemoteMBeanServerSettings().getClientId());
		ResultContainer entry = new ResultContainer(ResultContainer.SETTERS_RESULT);
		try {
			return (AttributeList) this.adapter.invokeRemote(notif, entry);	
		} catch (Throwable e) {
			JMXRemoteUtil.throwJMException("Error when setAttributes", e);
			//Never get here. Just for compilation
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		MBeanOperationInfo opInfo = mBeanAccessor.getOperation(actionName, signature);
		if (opInfo == null)
			throw new JMRuntimeException("Can not find operation [" + actionName + "] with signature [" + signature + "]");
		JMXRemoteNotification notif = null; 
		if (JMXRemoteUtil.isJDK15())
			notif = new DoOperNotification_OLD(actionName, params, signature, name, adapter.getRemoteMBeanServerSettings().getClientId());
		else
			notif = new DoOperNotification(actionName, params, signature, name, adapter.getRemoteMBeanServerSettings().getClientId());
		ResultContainer entry = new ResultContainer(ResultContainer.OP_RESULT);
		
		try {
			return this.adapter.invokeRemote(notif, entry);
		} catch (MBeanException e) {
			throw e;
		} catch (ReflectionException e) {
			throw e;		
		} catch (Throwable e) {
			JMXRemoteUtil.throwJMException("Error when invoke", e);
			//Never get here. Just for compilation
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	public MBeanInfo getMBeanInfo() {
		return this.mBeanAccessor.getMBeanInfo();
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer mBeanServer, ObjectName name) throws Exception {
		if (mBeanAccessor.getMBeanInfoExt().isMBeanRegistration()) {
			JMXRemoteNotification notif = null;
			if (JMXRemoteUtil.isJDK15())
				notif = new RegistrationNotification_OLD(null, JMXRemoteNotification.PRE_REGISTER_OLD_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
			else
				notif = new RegistrationNotification(null, JMXRemoteNotification.PRE_REGISTER_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
			ResultContainer entry = new ResultContainer(ResultContainer.PRE_REGISTER_RESULT);
			ObjectName ret = (ObjectName) this.adapter.invokeRemote(notif, entry);
			this.name = ret;
			synchronized (this.adapter.mBeanMap) {
				this.adapter.mBeanMap.remove(name);
				this.adapter.mBeanMap.put(this.name, this);
			}
			
			return ret;
		} else {
			this.name = name;
			return name;
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean done) {
		if (mBeanAccessor.getMBeanInfoExt().isMBeanRegistration()) {
			JMXRemoteNotification notif = null;
			if (JMXRemoteUtil.isJDK15())
				notif = new RegistrationNotification_OLD(done, JMXRemoteNotification.POST_REGISTER_OLD_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
			else
				notif = new RegistrationNotification(done, JMXRemoteNotification.POST_REGISTER_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
			this.adapter.sendInternalNotification(notif);
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
		if (mBeanAccessor.getMBeanInfoExt().isMBeanRegistration()) {
			JMXRemoteNotification notif = null;
			if (JMXRemoteUtil.isJDK15())
				notif = new RegistrationNotification_OLD(null, JMXRemoteNotification.PRE_DEREGISTER_OLD_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
			else
				notif = new RegistrationNotification(null, JMXRemoteNotification.PRE_DEREGISTER_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
			adapter.sendInternalNotification(notif);
		}
	}
 
	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {
		try {
			if (mBeanAccessor.getMBeanInfoExt().isMBeanRegistration()) {
				JMXRemoteNotification notif = null;
				if (JMXRemoteUtil.isJDK15())
					notif = new RegistrationNotification_OLD(null, JMXRemoteNotification.POST_DEREGISTER_OLD_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
				else
					notif = new RegistrationNotification(null, JMXRemoteNotification.POST_DEREGISTER_TYPE, name, adapter.getRemoteMBeanServerSettings().getClientId());
				adapter.sendInternalNotification(notif);
			}
		} finally {
			adapter.removeMBean(name);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.management.NotificationBroadcasterSupport#getNotificationInfo()
	 */
	public MBeanNotificationInfo[] getNotificationInfo() {
		return mBeanAccessor.getMBeanInfo().getNotifications();
	}

	/* (non-Javadoc)
	 * @see javax.management.PersistentMBean#load()
	 */
	public void load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
		JMXRemoteNotification notif = new DoOperNotification("load", new Object[0], new String[0], name, adapter.getRemoteMBeanServerSettings().getClientId());
		ResultContainer entry = new ResultContainer(ResultContainer.OP_RESULT);
		
		try {
			this.adapter.invokeRemote(notif, entry);
		} catch (RuntimeOperationsException e) {
			throw e;
		} catch (InstanceNotFoundException e) {
			throw e;
		} catch (MBeanException e) {
			throw e;		
		} catch (Throwable e) {
			JMXRemoteUtil.throwJMException("Error when load", e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.management.PersistentMBean#store()
	 */
	public void store() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
		JMXRemoteNotification notif = new DoOperNotification("save", new Object[0], new String[0], name, adapter.getRemoteMBeanServerSettings().getClientId());
		ResultContainer entry = new ResultContainer(ResultContainer.OP_RESULT);
		
		try {
			this.adapter.invokeRemote(notif, entry);
		} catch (RuntimeOperationsException e) {
			throw e;
		} catch (InstanceNotFoundException e) {
			throw e;
		} catch (MBeanException e) {
			throw e;		
		} catch (Throwable e) {
			JMXRemoteUtil.throwJMException("Error when load", e);
		}
	}
}

