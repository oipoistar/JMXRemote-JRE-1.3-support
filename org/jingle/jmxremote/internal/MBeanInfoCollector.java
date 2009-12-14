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
package org.jingle.jmxremote.client.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.modelmbean.ModelMBean;

import org.jingle.jmxremote.common.JMXRemoteUtil;
import org.jingle.jmxremote.common.MBeanInfoExt;
import org.jingle.jmxremote.common.OpKey;
import org.jingle.jmxremote.common.RemoteMBeanInfo;

/**
 * This class is used to collect MBeanInfo for a MBean instance.
 * @author jianlu
 *
 */
public class MBeanInfoCollector {
	protected Map attrMap = new HashMap();
	protected Map opMap = new HashMap();
	Object inst;
	Class mBeanClass;
	MBeanInfo mBeanInfo = null;
	MBeanInfoExt mBeanInfoExt = null;

	/**
	 * @param inst MBean instance
	 * @param remoteMBeanServerSettings RemoteMBeanServerSettings instance which is 
	 * used to specify some detailed parameters for the MBeanServer usage.
	 * @throws NotCompliantMBeanException If the instance is not a valid MBean.
	 */
	public MBeanInfoCollector(Object inst) throws NotCompliantMBeanException {
		this.inst = inst;
		findMBeanClass();
		constructMBeanInfo();
	}
	
	/**
	 * Get MBean instance.
	 * @return MBean instance
	 */
	public Object getInstance() {
		return inst;
	}
	
	/**
	 * Get the Management interface of the MBean instance 
	 * @return The managed interface of the MBean instance.
	 */
	public Class getMBeanClass() {
		return mBeanClass;
	}
	
	/**
	 * Get MBeanInfo of the MBean instance.
	 * @return MBeanInfo
	 */
	public MBeanInfo getMBeanInfo() {
		return mBeanInfo;
	}
	
	/**
	 * Get MBeanInfo extension of the MBean instance.
	 * @return MBeanInfo
	 */
	public MBeanInfoExt getMBeanInfoExt() {
		return mBeanInfoExt;
	}

	/**
	 * Get the attribute getter method.
	 * @param attr The attribute name.
	 * @return The attribute's getter method.
	 */
	public Method getAttributeReader(String attr) {
		MBeanAttribute mBeanAttribute =  (MBeanAttribute)attrMap.get(attr);
		if (mBeanAttribute != null)
			return mBeanAttribute.getGetter();
		else
			return null;
	}
	
	/**
	 * Get the attribute setter method.
	 * @param attr The attribute name.
	 * @return The attribute's setter method.
	 */
	public Method getAttributeWritter(String attr) {
		MBeanAttribute mBeanAttribute =  (MBeanAttribute)attrMap.get(attr);
		if (mBeanAttribute != null)
			return mBeanAttribute.getSetter();
		else
			return null;
	}

	/**
	 * Get the operation method.
	 * @param opName Operation name.
	 * @param signature Operation signature.
	 * @return The operation method.
	 */
	public Method getOperationMethod(String opName, String[] signature) {
		Method ret = (Method)opMap.get(new OpKey(opName, signature));
		if (ret == null) {
			if (opName.startsWith("get") && (signature.length == 0)) {
				String attrName = opName.substring(3);
				ret = getAttributeReader(attrName);
			} else if (opName.startsWith("set") && (signature.length == 1)) {
				String attrName = opName.substring(3);
				Method setter = getAttributeWritter(attrName);
				if ((setter != null) && (setter.getReturnType().getName().equals(signature[0])))
				    ret = setter;
			} else if (opName.startsWith("is") && (signature.length == 0)) {
				String attrName = opName.substring(2);
				ret = getAttributeReader(attrName);
			}
		}
		return ret;
	}
	
	void constructMBeanInfo() {
		mBeanInfoExt = new MBeanInfoExt();
		if (inst instanceof MBeanRegistration)
			mBeanInfoExt.setMBeanRegistration(true);
		if (inst instanceof NotificationBroadcaster)
			mBeanInfoExt.setNotificationBroadcaster(true);
		if (inst instanceof NotificationEmitter)
			mBeanInfoExt.setNotificationEmitter(true);
		if (inst instanceof ClassLoader)
			mBeanInfoExt.setClassLoader(true);
		
		if (mBeanClass == ModelMBean.class) {
			mBeanInfo = ((ModelMBean)inst).getMBeanInfo();
			mBeanInfoExt.setModelMBean(true);
		} else if (mBeanClass == DynamicMBean.class)
			mBeanInfo = ((DynamicMBean)inst).getMBeanInfo();
		else {
			MBeanAttributeInfo[] attrs = null;
			MBeanOperationInfo[] ops = null;
			MBeanConstructorInfo[] cons = new MBeanConstructorInfo[1];
			MBeanNotificationInfo[] notifications = null;
			
			Method[] methods = mBeanClass.getMethods();
			for (int i = 0; i < methods.length; i++){
				Method m = methods[i];
				if (filterMethod(m))
					break;
				String name = m.getName();
				Class type = m.getReturnType();
				MBeanAttribute attr = null;
				if ((name.startsWith("get")) && (m.getParameterTypes().length == 0)) {
					if (type != void.class) {
						name = name.substring(3);
						attr = (MBeanAttribute) attrMap.get(name);
						if (attr != null) {
							if ((attr.getType() != type) || attr.getGetter() != null) {
								opMap.put(new OpKey(m), m);
								attrToOp(attr);
								attr = null;
							} else
							    attr.setGetter(m);
						} else
						    attr = new MBeanAttribute(name, type, m, null);
					} else
						opMap.put(new OpKey(m), m);
				} else if ((name.startsWith("is")) && (m.getParameterTypes().length == 0)) {
					if (type == Boolean.TYPE) {
						name = name.substring(2);
						attr = (MBeanAttribute) attrMap.get(name);
						if (attr != null) {
							if ((attr.getType() != type) || attr.getGetter() != null) {
								opMap.put(new OpKey(m), m);
								attrToOp(attr);
								attr = null;
							} else 
								attr.setSetter(m);
						} else
						    attr = new MBeanAttribute(name, type, m, null);
					} else
						opMap.put(new OpKey(m), m);
				} else if ((name.startsWith("set")) && (m.getReturnType() == void.class)) {
					if (m.getParameterTypes().length == 1) {
						type = m.getParameterTypes()[0];
						name = name.substring(3);
						attr = (MBeanAttribute) attrMap.get(name);
						if (attr != null) {
							if (attr.getType() != type) {
								opMap.put(new OpKey(m), m);
								attrToOp(attr);
								attr = null;
							} else
							    attr.setSetter(m);
						} else
						    attr = new MBeanAttribute(name, type, null, m);
					} else
						opMap.put(new OpKey(m), m);
				} else {
					opMap.put(new OpKey(m), m);
				}
				if (attr != null)
					attrMap.put(name, attr);
				else
					attrMap.remove(name);
			}
			
			attrs = new MBeanAttributeInfo[attrMap.size()];
			int i = 0;
			Iterator iter = attrMap.values().iterator();
			while (iter.hasNext()) {
				try {
					MBeanAttribute it = (MBeanAttribute) iter.next();
					attrs[i++] = new MBeanAttributeInfo(it.getName(), null, it.getGetter(), it.getSetter());
				} catch (IntrospectionException e) {
					JMXRemoteUtil.throwJMException("Error when create MBeanAttributeInfo", e);
				}
			}
			
			ops = new MBeanOperationInfo[opMap.size()];
			i = 0;
			Iterator seciter = opMap.values().iterator();
			while(seciter.hasNext()) {
				Method m = (Method) seciter.next();
				ops[i++] = new MBeanOperationInfo(null, m);
			}
			
			cons[0] = new MBeanConstructorInfo("Constructor", null, new MBeanParameterInfo[] {new MBeanParameterInfo("mBeanInfo", RemoteMBeanInfo.class.getName(), "mBeanInfo")});
	
			if (mBeanInfoExt.isNotificationBroadcaster())
				notifications = ((NotificationBroadcaster)inst).getNotificationInfo();
			
			mBeanInfo =  new MBeanInfo(mBeanClass.getName(), "Remote Dynamic MBean", attrs, cons, ops, notifications);
		}
	}

	private void attrToOp(MBeanAttribute attr) {
		if (attr.getGetter() != null)
			opMap.put(new OpKey(attr.getGetter()), attr.getGetter());
		if (attr.getSetter() != null)
			opMap.put(new OpKey(attr.getSetter()), attr.getSetter());
	}

	void findMBeanClass() throws NotCompliantMBeanException {
		Class instClass = inst.getClass();
		if (ModelMBean.class.isAssignableFrom(instClass))
			mBeanClass = ModelMBean.class;
		else if (DynamicMBean.class.isAssignableFrom(instClass))
			mBeanClass = DynamicMBean.class;
		else
		    mBeanClass = getMBeanClass(instClass);
		if (mBeanClass == null)
			throw new NotCompliantMBeanException("Can not find MBean interface for the object [" + inst + "]");
	}

	private static Class getMBeanClass(Class clazz) {
		Class ret = null;
		String className = clazz.getName();
		Class[] interfaces = clazz.getInterfaces();
		Class superClass = clazz.getSuperclass();
		//check if there is an interface named XXXMBean
		String expectedClassName = className + "MBean";
		for (int i = 1; i <= interfaces.length; i++) {
			String interfaceName = interfaces[i - 1].getName();
			if (expectedClassName.equals(interfaceName)) 
				return interfaces[i - 1];
		}
		//check the superclass
		if (superClass != null) {
			ret = getMBeanClass(superClass);
		}
		return ret;
	}

	protected boolean filterMethod(Method m) {
		if (mBeanInfoExt.isMBeanRegistration()) {
			Method[] mtd = MBeanRegistration.class.getMethods();
			for (int i = 0; i < mtd.length; i++) {
				if (m.equals(mtd[i]))
					return true;
			}
		}
		if (mBeanInfoExt.isNotificationEmitter()) {
			Method[] mtd = NotificationEmitter.class.getMethods();
			for (int i = 0; i < mtd.length; i++) {
				if (m.equals(mtd[i]))
					return true;
			}
		}
		if (mBeanInfoExt.isNotificationBroadcaster()) {
			Method[] mtd = NotificationBroadcaster.class.getMethods();
			for (int i = 0; i < mtd.length; i++) {
				if (m.equals(mtd[i]))
					return true;
			}
		}
		return false;
	}
}

class MBeanAttribute {
	String name;
	Class type;
	Method getter = null;
	Method setter = null;
	
	public MBeanAttribute(String name, Class type, Method getter, Method setter) {
		this.name = name;
		this.type = type;
		this.getter = getter;
		this.setter = setter;
	}

	public Method getGetter() {
		return getter;
	}
	

	public void setGetter(Method getter) {
		this.getter = getter;
	}
	

	public String getName() {
		return name;
	}
	

	public void setName(String name) {
		this.name = name;
	}
	

	public Method getSetter() {
		return setter;
	}
	

	public void setSetter(Method setter) {
		this.setter = setter;
	}
	

	public Class getType() {
		return type;
	}
	

	public void setType(Class type) {
		this.type = type;
	}
}