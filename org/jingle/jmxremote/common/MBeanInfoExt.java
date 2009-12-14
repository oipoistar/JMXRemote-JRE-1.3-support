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

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class MBeanInfoExt {
	private boolean mBeanRegistration = false;
	private boolean notificationBroadcaster = false;
	private boolean notificationEmitter = false;
	private boolean modelMBean = false;
	private boolean classLoader = false;
	
	private static String FIELD_MBEANREGISTRATION = "mBeanRegistration";
	private static String FIELD_NOTIFICATIONBROADCASTER = "notificationBroadcaster";
	private static String FIELD_NOTIFICATIONEMITTER = "notificationEmitter";
	private static String FIELD_MODELMBEAN = "modelMBean";
	private static String FIELD_CLASSLOADER = "classLoader";
	public static CompositeType OPENTYPE;
	
	static {
		try {
			OPENTYPE = new CompositeType(MBeanInfoExt.class.getName(),  
										 "The type used to represent MBeanInfo extention", 
										 new String[] {FIELD_MBEANREGISTRATION, FIELD_NOTIFICATIONBROADCASTER, FIELD_NOTIFICATIONEMITTER, FIELD_MODELMBEAN, FIELD_CLASSLOADER}, 
										 new String[] {"whether implements MBeanRegistration", "whether implements NotificationBroadcaster", "whether implements NotificationEmitter", "whether implements ModelMBean", "whether it is a class loader"}, 
										 new OpenType[] {SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN});
		} catch (OpenDataException e) {
			throw new RuntimeException("Can not create CompositeType for MBeanInfo extention");
		}
	}
	
	/**
	 * Default constructor
	 */
	public MBeanInfoExt() {
	}
	
	/**
	 * Constructor
	 * @param data CompositeData
	 */
	public MBeanInfoExt(CompositeData data) {
		this();
		this.fromOpenData(data);
	}
	
	public boolean isMBeanRegistration() {
		return mBeanRegistration;
	}
	public void setMBeanRegistration(boolean mBeanRegistration) {
		this.mBeanRegistration = mBeanRegistration;
	}
	public boolean isNotificationBroadcaster() {
		return notificationBroadcaster;
	}
	public void setNotificationBroadcaster(boolean notificationBroadcaster) {
		this.notificationBroadcaster = notificationBroadcaster;
	}
	public boolean isNotificationEmitter() {
		return notificationEmitter;
	}
	public void setNotificationEmitter(boolean notificationEmitter) {
		this.notificationEmitter = notificationEmitter;
	}
	public boolean isModelMBean() {
		return modelMBean;
	}
	public void setModelMBean(boolean modelMBean) {
		this.modelMBean = modelMBean;
	}
	
	public boolean isClassLoader() {
		return classLoader;
	}

	public void setClassLoader(boolean classLoader) {
		this.classLoader = classLoader;
	}

	public CompositeData toOpenData() {
		Map dataMap = new HashMap();
		dataMap.put(FIELD_MBEANREGISTRATION, new Boolean(this.mBeanRegistration));
		dataMap.put(FIELD_NOTIFICATIONBROADCASTER, new Boolean(this.notificationBroadcaster));
		dataMap.put(FIELD_NOTIFICATIONEMITTER, new Boolean(this.notificationEmitter));
		dataMap.put(FIELD_MODELMBEAN, new Boolean(this.modelMBean));
		dataMap.put(FIELD_CLASSLOADER, new Boolean(this.classLoader));
		try {
			return new CompositeDataSupport(OPENTYPE, dataMap);
		} catch (OpenDataException e) {
			throw new RuntimeException("Error when create CompositeData for MBeanInfo extention");
		}
	}
	
	public void fromOpenData(CompositeData openData) {
		this.mBeanRegistration = ((Boolean) openData.get(FIELD_MBEANREGISTRATION)).booleanValue();
		this.notificationBroadcaster = ((Boolean) openData.get(FIELD_NOTIFICATIONBROADCASTER)).booleanValue();
		this.notificationEmitter = ((Boolean) openData.get(FIELD_NOTIFICATIONEMITTER)).booleanValue();
		this.modelMBean = ((Boolean) openData.get(FIELD_MODELMBEAN)).booleanValue();
		this.classLoader = ((Boolean)openData.get(FIELD_CLASSLOADER)).booleanValue();
	}
	
}
