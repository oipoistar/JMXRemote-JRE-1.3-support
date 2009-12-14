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

import java.util.Date;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicLong;

/**
 * Internal notification base class
 * @author jianlu
 *
 */
public abstract class JMXRemoteNotification extends Notification implements IJMXRemoteNotification {
	private static final long serialVersionUID = 1L;

	static AtomicLong sequence = new AtomicLong(0);
	
	public static final String BROADCAST_CLIENT_ID = "BOARDCAST";
	
	public static final String GET_ATTR_TYPE = "org.jingle.jmxremote.notification.GET_ATTR_TYPE";
	public static final String GET_ATTRS_TYPE = "org.jingle.jmxremote.notification.GET_ATTRS_TYPE";
	public static final String SET_ATTR_TYPE = "org.jingle.jmxremote.notification.SET_ATTR_TYPE";
	public static final String SET_ATTR_OLD_TYPE = "org.jingle.jmxremote.notification.SET_ATTR_OLD_TYPE";
	public static final String SET_ATTRS_TYPE = "org.jingle.jmxremote.notification.SET_ATTRS_TYPE";
	public static final String SET_ATTRS_OLD_TYPE = "org.jingle.jmxremote.notification.SET_ATTRS_OLD_TYPE";
	public static final String DO_OPER_TYPE = "org.jingle.jmxremote.notification.DO_OPER_TYPE";
	public static final String DO_OPER_OLD_TYPE = "org.jingle.jmxremote.notification.DO_OPER_OLD_TYPE";
	public static final String PRE_REGISTER_TYPE = "org.jingle.jmxremote.notification.PRE_REGISTER_TYPE";
	public static final String PRE_REGISTER_OLD_TYPE = "org.jingle.jmxremote.notification.PRE_REGISTER_OLD_TYPE";
	public static final String POST_REGISTER_TYPE = "org.jingle.jmxremote.notification.POST_REGISTER_TYPE";
	public static final String POST_REGISTER_OLD_TYPE = "org.jingle.jmxremote.notification.POST_OLD_REGISTER_TYPE";
	public static final String PRE_DEREGISTER_TYPE = "org.jingle.jmxremote.notification.PRE_DEREGISTER_TYPE";
	public static final String PRE_DEREGISTER_OLD_TYPE = "org.jingle.jmxremote.notification.PRE_DEREGISTER_OLD_TYPE";
	public static final String POST_DEREGISTER_TYPE = "org.jingle.jmxremote.notification.POST_DEREGISTER_TYPE";
	public static final String POST_DEREGISTER_OLD_TYPE = "org.jingle.jmxremote.notification.POST_DEREGISTER_OLD_TYPE";
	
	public static final String NOTIFICATION_HANDBACK = "org.jingle.jmxremote.notification.handback";
	
	protected String clientId;
	
	public static boolean isValidJMXRemoteNotificationType(String type) {
		if (GET_ATTR_TYPE.equals(type) ||
			GET_ATTRS_TYPE.equals(type) ||
			SET_ATTR_TYPE.equals(type) ||
			SET_ATTR_OLD_TYPE.equals(type) ||
			SET_ATTRS_TYPE.equals(type) ||
			SET_ATTRS_OLD_TYPE.equals(type) ||
			DO_OPER_TYPE.equals(type) ||
			DO_OPER_OLD_TYPE.equals(type) ||
			PRE_REGISTER_TYPE.equals(type) ||
			PRE_REGISTER_OLD_TYPE.equals(type) ||
			POST_REGISTER_TYPE.equals(type) ||
			POST_REGISTER_OLD_TYPE.equals(type) ||
			PRE_DEREGISTER_TYPE.equals(type) ||
			PRE_DEREGISTER_OLD_TYPE.equals(type) ||
			POST_DEREGISTER_TYPE.equals(type) ||
			POST_DEREGISTER_OLD_TYPE.equals(type)
		)
			return true;
		return false;
	}
	
	/**
	 * Default Constructor
	 */
	protected JMXRemoteNotification(String type, ObjectName source) {
		super(type, source, sequence.addAndGet(1), new Date().getTime());
	}
	/**
	 * Constructor
	 * @param type Notification type
	 * @param source Source MBean object name
	 * @param clientId Notification target ID
	 */
	protected JMXRemoteNotification(String type, ObjectName source, String clientId) {
		super(type, source, sequence.addAndGet(1), new Date().getTime());
		this.clientId = clientId;
	}

	/**
	 * Get notification source
	 * @return Notification source. 
	 */
	public Object getSource() {
		return ((Object)source);
	}

	/**
	 * Get notification target client ID
	 * @return Notification target ID
	 */
	public String getClientId() {
		return clientId;
	}
	
	protected abstract CompositeData toOpenData();
	
	protected abstract void fromOpenData(CompositeData openData);
	
	public Notification toNotification() {
		Notification ret = new Notification(this.getType(), this.getSource(), this.getSequenceNumber());
		ret.setUserData(toOpenData());
		return ret;
	}
	
	public static JMXRemoteNotification fromNotification(Notification notif) {
		JMXRemoteNotification ret = null;
		String type = notif.getType();
		ObjectName source = (ObjectName) notif.getSource();
		if (GET_ATTR_TYPE.equals(type))
			ret = new GetAttributeNotification(source);
		else if (GET_ATTRS_TYPE.equals(type))
			ret = new GetAttributesNotification(source);
		else if (SET_ATTR_TYPE.equals(type))
			ret = new SetAttributeNotification(source);
		else if (SET_ATTR_OLD_TYPE.equals(type))
			ret = new SetAttributeNotification_OLD(source);
		else if (SET_ATTRS_TYPE.equals(type))
			ret = new SetAttributesNotification(source);
		else if (SET_ATTRS_OLD_TYPE.equals(type))
			ret = new SetAttributesNotification_OLD(source);
		else if (DO_OPER_TYPE.equals(type))
			ret = new DoOperNotification(source);
		else if (DO_OPER_OLD_TYPE.equals(type))
			ret = new DoOperNotification_OLD(source);
		else if (PRE_REGISTER_TYPE.equals(type))
			ret = new RegistrationNotification(PRE_REGISTER_TYPE, source);
		else if (PRE_REGISTER_OLD_TYPE.equals(type))
			ret = new RegistrationNotification_OLD(PRE_REGISTER_OLD_TYPE, source);
		else if (POST_REGISTER_TYPE.equals(type))
			ret = new RegistrationNotification(POST_REGISTER_TYPE, source);
		else if (POST_REGISTER_OLD_TYPE.equals(type))
			ret = new RegistrationNotification_OLD(POST_REGISTER_OLD_TYPE, source);
		else if (PRE_DEREGISTER_TYPE.equals(type))
			ret = new RegistrationNotification(PRE_DEREGISTER_TYPE, source);
		else if (PRE_DEREGISTER_OLD_TYPE.equals(type))
			ret = new RegistrationNotification_OLD(PRE_DEREGISTER_OLD_TYPE, source);
		else if (POST_DEREGISTER_TYPE.equals(type))
			ret = new RegistrationNotification(POST_DEREGISTER_TYPE, source);
		else if (POST_DEREGISTER_OLD_TYPE.equals(type))
			ret = new RegistrationNotification_OLD(POST_DEREGISTER_OLD_TYPE, source);
		if (ret != null) {
			ret.fromOpenData((CompositeData) notif.getUserData());
			ret.setSequenceNumber(notif.getSequenceNumber());
		}
		return ret;
	}
}
