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

import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.Notification;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

public interface RemoteAdapterMBean {
	/**
	 * Create dynamic mbean
	 * @param remoteMBeanInfo Remote mbeain info
	 * @param name Target object name
	 * @return Object instance
	 * @throws JMException
	 */
	public ObjectInstance createDynamicMBean(MBeanInfo remoteMBeanInfo, CompositeData mBeanInfoExt, ObjectName name) throws JMException;
	
	/**
	 * Return a request result
	 * @param name Target object name
	 * @param sequence The request sequence
	 * @param attr Result attribute
	 * @param prepare Whther the target object is in prepare stage
	 */
	public void returnResult(ObjectName name, long sequence, Attribute attr, boolean prepare);
	
	/**
	 * Return request results
	 * @param name Target object name
	 * @param sequence Request sequence
	 * @param attrs Results
	 */
	public void returnResults(ObjectName name, long sequence, AttributeList attrs);
	
	/**
	 * Return an exception according to a request
	 * @param name Taregt object name
	 * @param sequence Request sequence
	 * @param t The exception
	 * @param prepare Whether the target object is in prepare stage
	 */
	public void returnException(ObjectName name, long sequence, Throwable t, boolean prepare);
	
	/**
	 * Fire notification to target
	 * @param name Target object name
	 * @param notification The notification
	 */
	public void fireNotification(ObjectName name, Notification notification);
	
	/**
	 * Ping the target mbeans
	 * @param names Target MBean names
	 * @return The responsible MBean names
	 */
	public void ping();
	
	/**
	 * Get all remote mbeans info
	 * @return A map contains mbeans info
	 */
	public Map getRemoteMBeans();
	
	/**
	 * Get the remote adapter settings
	 * @return The remote adapter settings
	 */
	public CompositeData getSettings();
}
