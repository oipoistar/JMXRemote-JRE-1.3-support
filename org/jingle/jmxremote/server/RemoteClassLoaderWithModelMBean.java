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
import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.NotificationListener;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;

public class RemoteClassLoaderWithModelMBean extends RemoteClassLoader implements ModelMBean {
	
	private ModelMBean delegate2 = null;
	
	public RemoteClassLoaderWithModelMBean(RemoteAdapter adapter, IRemoteDynamicMBean delegate) {
		super(adapter, delegate);
		this.delegate2 = (ModelMBean) delegate;
	}

	public void addAttributeChangeNotificationListener(
			NotificationListener listener, String attributeName, Object handback)
			throws MBeanException, RuntimeOperationsException,
			IllegalArgumentException {
		delegate2.addAttributeChangeNotificationListener(listener,
				attributeName, handback);
	}

	public void removeAttributeChangeNotificationListener(
			NotificationListener listener, String attributeName)
			throws MBeanException, RuntimeOperationsException,
			ListenerNotFoundException {
		delegate2.removeAttributeChangeNotificationListener(listener,
				attributeName);
	}

	public void sendAttributeChangeNotification(Attribute oldValue,
			Attribute newValue) throws MBeanException,
			RuntimeOperationsException {
		delegate2.sendAttributeChangeNotification(oldValue, newValue);
	}

	public void sendAttributeChangeNotification(
			AttributeChangeNotification notification) throws MBeanException,
			RuntimeOperationsException {
		delegate2.sendAttributeChangeNotification(notification);
	}

	public void sendNotification(String ntfyText) throws MBeanException,
			RuntimeOperationsException {
		delegate2.sendNotification(ntfyText);
	}

	public void setManagedResource(Object mr, String mr_type)
			throws MBeanException, RuntimeOperationsException,
			InstanceNotFoundException, InvalidTargetObjectTypeException {
		delegate2.setManagedResource(mr, mr_type);
	}

	public void setModelMBeanInfo(ModelMBeanInfo inModelMBeanInfo)
			throws MBeanException, RuntimeOperationsException {
		delegate2.setModelMBeanInfo(inModelMBeanInfo);
	}
}
