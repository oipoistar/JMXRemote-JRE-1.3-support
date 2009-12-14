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

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

public class RemoteModelMBeanInfo extends RemoteMBeanInfo implements ModelMBeanInfo {
	private static final long serialVersionUID = 1L;
	private ModelMBeanInfo modelMBeanInfo = null;
	public RemoteModelMBeanInfo(ModelMBeanInfo mbi, Object inst, RemoteMBeanServerSettings remoteMBeanServerSettings) {
		super(mbi.getClassName(), mbi.getDescription(), mbi.getAttributes(), mbi.getConstructors(), mbi.getOperations(), mbi.getNotifications(), inst, remoteMBeanServerSettings);
		this.modelMBeanInfo = mbi;
	}

	public ModelMBeanAttributeInfo getAttribute(String inName) throws MBeanException, RuntimeOperationsException {
		return modelMBeanInfo.getAttribute(inName);
	}
	public MBeanAttributeInfo[] getAttributes() {
		return modelMBeanInfo.getAttributes();
	}
	public String getClassName() {
		return modelMBeanInfo.getClassName();
	}
	public MBeanConstructorInfo[] getConstructors() {
		return modelMBeanInfo.getConstructors();
	}
	public String getDescription() {
		return modelMBeanInfo.getDescription();
	}
	public Descriptor getDescriptor(String inDescriptorName, String inDescriptorType) throws MBeanException, RuntimeOperationsException {
		return modelMBeanInfo.getDescriptor(inDescriptorName, inDescriptorType);
	}
	public Descriptor[] getDescriptors(String inDescriptorType) throws MBeanException, RuntimeOperationsException {
		return modelMBeanInfo.getDescriptors(inDescriptorType);
	}
	public Descriptor getMBeanDescriptor() throws MBeanException, RuntimeOperationsException {
		return modelMBeanInfo.getMBeanDescriptor();
	}
	public ModelMBeanNotificationInfo getNotification(String inName) throws MBeanException, RuntimeOperationsException {
		return modelMBeanInfo.getNotification(inName);
	}
	public MBeanNotificationInfo[] getNotifications() {
		return modelMBeanInfo.getNotifications();
	}
	public ModelMBeanOperationInfo getOperation(String inName) throws MBeanException, RuntimeOperationsException {
		return modelMBeanInfo.getOperation(inName);
	}
	public MBeanOperationInfo[] getOperations() {
		return modelMBeanInfo.getOperations();
	}
	public void setDescriptor(Descriptor inDescriptor, String inDescriptorType) throws MBeanException, RuntimeOperationsException {
		modelMBeanInfo.setDescriptor(inDescriptor, inDescriptorType);
	}
	public void setDescriptors(Descriptor[] inDescriptors) throws MBeanException, RuntimeOperationsException {
		modelMBeanInfo.setDescriptors(inDescriptors);
	}
	public void setMBeanDescriptor(Descriptor inDescriptor) throws MBeanException, RuntimeOperationsException {
		modelMBeanInfo.setMBeanDescriptor(inDescriptor);
	}
}
