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

import java.util.HashMap;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jingle.jmxremote.common.MBeanInfoExt;
import org.jingle.jmxremote.common.OpKey;

/**
 * This class is used to access MBeanInfo
 * @author jianlu
 *
 */
public class MBeanInfoAccessor {
	MBeanInfo mBeanInfo;
	MBeanInfoExt mBeanInfoExt;
	Map attrMap = new HashMap();
	Map opMap = new HashMap();

	/**
	 * Constructor
	 * @param mBeanInfo MBeanInfo instance
	 * @throws JMException if there is any exception
	 */
	public MBeanInfoAccessor(MBeanInfo mBeanInfo, MBeanInfoExt mBeanInfoExt) throws JMException {
	    this.mBeanInfo = mBeanInfo;
	    this.mBeanInfoExt = mBeanInfoExt;
		if (mBeanInfoExt.isModelMBean())
			reviseModelMBeanInfo();
		constructMaps();
	}
	
	/**
	 * Get MBeanInfo
	 * @return MBeanInfo
	 */
	public MBeanInfo getMBeanInfo() {
		return mBeanInfo;
	}
	
	/**
	 * Get MBeanInfo extention
	 * @return MBeanInfoExt
	 */
	public MBeanInfoExt getMBeanInfoExt() {
		return this.mBeanInfoExt;
	}
	
	/**
	 * Get mbean attribute info
	 * @param attr Attribute name
	 * @return MBean attribute info
	 */
	public MBeanAttributeInfo getAttribute(String attr) {
		return (MBeanAttributeInfo) attrMap.get(attr);
	}

	/**
	 * Get mbean operation info
	 * @param opName Operation name
	 * @param signature Operation signature
	 * @return MBean operation info
	 */
	public MBeanOperationInfo getOperation(String opName, String[] signature) {
		return (MBeanOperationInfo) opMap.get(new OpKey(opName, signature));
	}
	
	protected void constructMaps() {
		MBeanAttributeInfo[] attrs = mBeanInfo.getAttributes();
		for (int i = 0; i < attrs.length; i++) {
			MBeanAttributeInfo attr = attrs[i];
			attrMap.put(attr.getName(), attr);
		}

		MBeanOperationInfo[] ops = mBeanInfo.getOperations();
		for (int i = 0; i < ops.length; i++) {
			MBeanOperationInfo op = ops[i];
			opMap.put(new OpKey(op), op);
		}
		for (int i = 0; i < attrs.length; i++) {
			MBeanAttributeInfo attr = attrs[i];
			if (attr.isIs()) {
				String opName = "is" + attr.getName();
				opMap.put(new OpKey(opName, new String[0]), 
						new MBeanOperationInfo(opName, null, new MBeanParameterInfo[0], attr.getType(), MBeanOperationInfo.INFO));
			} else if (attr.isReadable()) {
				String opName = "get" + attr.getName();
				opMap.put(new OpKey(opName, new String[0]), 
						new MBeanOperationInfo(opName, null, new MBeanParameterInfo[0], attr.getType(), MBeanOperationInfo.INFO));
			}
			if (attr.isWritable()) {
				String opName = "set" + attr.getName();
				opMap.put(new OpKey(opName, new String[] {attr.getType()}), 
						new MBeanOperationInfo(opName, null, new MBeanParameterInfo[] {new MBeanParameterInfo(null, attr.getType(), null)}, void.class.getName(), MBeanOperationInfo.ACTION));
			}
		}
    }
	
	protected void reviseModelMBeanInfo() throws JMException {
		if (!(mBeanInfo instanceof ModelMBeanInfo))
			throw new JMException("ModelMBeanInfo required");
		String[] types = new String[] {"mbean", "attribute", "operation", "constructor", "notification"};
		
		for (int i = 0; i < types.length; i++) {
			String type = types[i];
			Descriptor[] descs = ((ModelMBeanInfo)mBeanInfo).getDescriptors(type);
			
			for (int f =0; f < descs.length; f++) {
				Descriptor desc = descs[f];
				desc.removeField("log");
				desc.removeField("logfile");
			}
		}
	}
}
