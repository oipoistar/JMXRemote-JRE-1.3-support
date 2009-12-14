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
 * Notification of do operation
 * @author jianlu
 *
 */
public class DoOperNotification_OLD extends JMXRemoteNotification implements IDoOperNotification {
	private static final long serialVersionUID = 1L;
	private static CompositeType TYPE = null;
	
	static {
		try {
			TYPE = new CompositeType(DO_OPER_TYPE, DO_OPER_TYPE, 
					new String[] {"clientId", "opName", "params", "signature"}, 
					new String[] {"client id", "operation name", "operation parameters", "operation signature"},
					new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, new ArrayType(1, SimpleType.STRING)}
			);
		} catch (OpenDataException e) {
			throw new RuntimeException("Can not create CompositeType for DoOperNotification");
		}
	}
	
	private String opName;
	private Object[] params;
	private String[] signature;
	
	/**
	 * Constructor
	 * @param source Source MBean object name
	 */
	public DoOperNotification_OLD(ObjectName source) {
		super(DO_OPER_OLD_TYPE, source);
	}
	
	/**
	 * Constructor
	 * @param opName Operation name
	 * @param params Operation parameters
	 * @param signature Operation signatures
	 * @param source Source MBean object name
	 * @param clientId Notification target client ID
	 */
	public DoOperNotification_OLD(String opName, Object[] params, String[] signature, ObjectName source, String clientId) {
		super(DO_OPER_OLD_TYPE, source, clientId);
		this.opName = opName;
		this.params = params;
		this.signature = signature;
	}
	
	/**
	 * Get operation name
	 * @return operation name
	 */
	public String getOpName() {
		return opName;
	}
	
	/**
	 * Get operation parameters
	 * @return Operation parameters
	 */
	public Object[] getParams() {
		return params;
	}
	
	/**
	 * Get operation signatures
	 * @return Operation signatures
	 */
	public String[] getSignature() {
		return signature;
	}


	public CompositeData toOpenData() {
		Map map = new HashMap();
		map.put("clientId", clientId);
		map.put("opName", opName);
		map.put("params", JMXRemoteUtil.serialize2Str(params));
		map.put("signature", signature);
		try {
			return new CompositeDataSupport(TYPE, map);
		} catch (OpenDataException e) {
			throw new RuntimeException("Error when create CompositeData for DoOperNotification");
		}
	}

	public void fromOpenData(CompositeData openData) {
		this.clientId = (String) openData.get("clientId");
		this.opName = (String) openData.get("opName");
		this.params = (Object[]) JMXRemoteUtil.deserializeFromStr((String) openData.get("params"));
		this.signature = (String[]) openData.get("signature");
	}
}
