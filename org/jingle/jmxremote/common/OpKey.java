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

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * The key of operation.
 * @author jianlu
 *
 */
public class OpKey {
	String opName;
	String[] signature;
	
	/**
	 * Constructor
	 * @param opName Operation name
	 * @param signature Operation signature
	 */
	public OpKey(String opName, String[] signature) {
		this.opName = opName;
		this.signature = signature;
	}

	/**
	 * Constructor
	 * @param m Operation method
	 */
	public OpKey(Method m) {
		this.opName = m.getName();
		Class[] paramTypes = m.getParameterTypes();
		this.signature = new String[paramTypes.length];
		int i = 0;
		
		for (int f =0; f < paramTypes.length; f++) {
			Class param = paramTypes[f];
			this.signature[i++] = param.getName();
		}
	}

	/**
	 * Constructor
	 * @param info MBeanOperationInfo instance
	 */
	public OpKey(MBeanOperationInfo info) {
		this.opName = info.getName();
		MBeanParameterInfo[] params = info.getSignature();
		this.signature = new String[params.length];
		int i = 0;
		for (int f = 0; f < params.length; f++) {
			MBeanParameterInfo param = params[f];
			this.signature[i++] = param.getType();
		}
	}

	/**
	 * Get operation name
	 * @return Operation name
	 */
	public String getOpName() {
		return opName;
	}
	

	/**
	 * Get operation signature
	 * @return Operation signature
	 */
	public String[] getSignature() {
		return signature;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof OpKey) {
			OpKey opKey = (OpKey)obj;
			if ((opName.equals(opKey.getOpName())) && (Arrays.equals(signature, opKey.getSignature())))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return opName.hashCode();
	}
}

