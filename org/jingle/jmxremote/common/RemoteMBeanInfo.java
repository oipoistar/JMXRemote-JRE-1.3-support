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
package org.jingle.jmxremote.common;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.modelmbean.ModelMBean;

/**
 * This class represents remote MBean info
 * @author jianlu
 *
 */
public class RemoteMBeanInfo extends MBeanInfo {
	private static final long serialVersionUID = 1L;
	private boolean mBeanRegistration = false;
	private boolean notifiactionBroadcaster = false;
	private boolean notificationEmitter = false;
	private boolean modelMBean = false;
	private int lifeTimeCounter = 0;
	private RemoteMBeanServerSettings remoteMBeanServerSettings;
	
	/**
	 * Constructor
	 * @param className MBean interface name
	 * @param description MBean description
	 * @param attributes MBean attributes info
	 * @param constructors MBean constructors info
	 * @param operations MBean operations info
	 * @param notifications MBean notifications info
	 * @param inst MBean instance
	 * @param remoteMBeanServerSettings Remote MBean server settings
	 */
	public RemoteMBeanInfo(String className, String description, MBeanAttributeInfo[] attributes, MBeanConstructorInfo[] constructors, MBeanOperationInfo[] operations, MBeanNotificationInfo[] notifications, Object inst, RemoteMBeanServerSettings remoteMBeanServerSettings) {
		super(className, description, attributes, constructors, operations, notifications);
		if (inst instanceof ModelMBean)
			modelMBean = true;
		if (inst instanceof MBeanRegistration)
			mBeanRegistration = true;
		if (inst instanceof NotificationBroadcaster)
			notifiactionBroadcaster = true;
		if (inst instanceof NotificationEmitter)
			notificationEmitter = true;
		this.remoteMBeanServerSettings = remoteMBeanServerSettings;
	}
	
	/**
	 * Constructor
	 * @param mBeanInfo MBeanInfo instance
	 * @param inst MBean instance
	 * @param remoteMBeanServerSettings Remote MBean server settings
	 */
	public RemoteMBeanInfo(MBeanInfo mBeanInfo, Object inst, RemoteMBeanServerSettings remoteMBeanServerSettings) {
		this(mBeanInfo.getClassName(), mBeanInfo.getDescription(), mBeanInfo.getAttributes(), mBeanInfo.getConstructors(), mBeanInfo.getOperations(), mBeanInfo.getNotifications(), inst, remoteMBeanServerSettings);
	}
	
	/**
	 * Whether the MBean implements NotificationBroadcastre interface
	 * @return true or false
	 */
	public boolean isNotifiactionBroadcaster() {
		return notifiactionBroadcaster;
	}
	
	/**
	 * Whether the MBean implements NotificationEmitter interface
	 * @return true or false.
	 */
	public boolean isNotificationEmitter() {
		return notificationEmitter;
	}

	/**
	 * Whether the MBean implements MBeanRegistration interface
	 * @return true ot false
	 */
	public boolean isMBeanRegistration() {
		return mBeanRegistration;
	}
	
	/**
	 * Whether the MBean implements ModelMBean interface
	 * @return true ot false
	 */
	public boolean isModelMBean() {
		return modelMBean;
	}

	/**
	 * Time tick.
	 */
	public void tick() {
		lifeTimeCounter++;
	}
	
	/**
	 * Renew the time tick count
	 *
	 */
	public void renew() {
		lifeTimeCounter = 0;
	}
	
	/**
	 * Is the MBean life time expired
	 * @return true or false
	 */
	public boolean isExpired() {
		return lifeTimeCounter >= remoteMBeanServerSettings.getMaxHeartBeatCount();
	}

	/**
	 * Get life time tick count
	 * @return Life time tick count
	 */
	public int getLifeTimeCounter() {
		return lifeTimeCounter;
	}

	/**
	 * Get remote MBean server settings
	 * @return Remote MBean server settings
	 */
	public RemoteMBeanServerSettings getRemoteMBeanServerSettings() {
		return remoteMBeanServerSettings;
	}
}
