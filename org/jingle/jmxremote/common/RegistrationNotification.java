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

import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Notification of MBean register and deregister
 * @author jianlu
 *
 */
public class RegistrationNotification extends JMXRemoteNotification implements IRegistrationNotification {
	private static final long serialVersionUID = 1L;
	private static CompositeType TYPE = null;
	
	static {
		try {
			TYPE = new CompositeType("org.jingle.jmxremote.notification.REGISTER_TYPE", "org.jingle.jmxremote.notification.REGISTER_TYPE", 
					new String[] {"clientId", "payload"}, 
					new String[] {"client id", "payload"},
					new OpenType[] {SimpleType.STRING, new ArrayType(1, SimpleType.BYTE)}
			);
		} catch (OpenDataException e) {
			throw new RuntimeException("Can not create CompositeType for RegistrationNotification");
		}
	}
	
	private Object payload;
	
	/**
	 * Constructor
	 * @param type Registration notification tyep
	 * @param source Source MBean object name
	 */
	public RegistrationNotification(String type, ObjectName source) {
		super(type, source);
	}
	/**
	 * Constructor
	 * @param payload Notification payload
	 * @param type Notification type
	 * @param source Source MBean object name
	 * @param clientId Notification target client ID
	 */
	public RegistrationNotification(Object payload, String type, ObjectName source, String clientId) {
		super(type, source, clientId);
		this.payload = payload;
	}
	
	/**
	 * Get notification payload
	 * @return Notification payload
	 */
	public Object getPayload() {
		return payload;
	}
	
	public CompositeData toOpenData() {
		Map map = new HashMap();
		map.put("clientId", clientId);
		map.put("payload", JMXRemoteUtil.serialize(payload));
		try {
			return new CompositeDataSupport(TYPE, map);
		} catch (OpenDataException e) {
			throw new RuntimeException("Error when create CompositeData for RegistrationNotification");
		}
	}

	protected void fromOpenData(CompositeData openData) {
		this.clientId = (String) openData.get("clientId");
		this.payload = JMXRemoteUtil.deserialize((byte[]) openData.get("payload"));
	}
}
