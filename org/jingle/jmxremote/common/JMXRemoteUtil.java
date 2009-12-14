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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import javax.management.JMRuntimeException;
import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.SimpleType;

public class JMXRemoteUtil {
	static Method BYTEARRAYTOBASE64 = null;
	static Method BASE64TOBYTEARRAY = null;
	static boolean JDK15 = false;
	private static ObjectName MBEANSERVERDELEGATE_OBJECT_NAME = null;
	
	static {
		try {
			Class clazz = Class.forName("java.util.prefs.Base64");
			BYTEARRAYTOBASE64 = clazz.getDeclaredMethod("byteArrayToBase64", new Class[] {byte[].class});
			BASE64TOBYTEARRAY = clazz.getDeclaredMethod("base64ToByteArray", new Class[] {String.class});
			BYTEARRAYTOBASE64.setAccessible(true);
			BASE64TOBYTEARRAY.setAccessible(true);
		} catch (Exception e) {
		}
		
		try {
			MBEANSERVERDELEGATE_OBJECT_NAME = new ObjectName("JMImplementation:type=MBeanServerDelegate");
		} catch (Exception e) {
			throw new RuntimeException("Can not create MBeanServerDelegate object name");
		}
		
		try {
			ArrayType.class.getConstructor(new Class[] {SimpleType.class, boolean.class});
		} catch (Exception e) {
			JDK15 = true;
		}
	}
	
	public static ObjectName getMBeanServerDelegateObjectName() {
		return MBEANSERVERDELEGATE_OBJECT_NAME;
	}

	public static void throwJMException(String message, Throwable cause) throws JMRuntimeException {
		if (cause instanceof RuntimeException)
			throw (RuntimeException)cause;
		if (cause instanceof Error)
			throw (Error) cause;
		JMRuntimeException ret = new JMRuntimeException(message);
		//(ret).initCause(cause);
		throw ret;
	}
	
	public static byte[] serialize(Object obj) {
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Error when serialize object");
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static String serialize2Str(Object obj) {
		try {
			return (String) BYTEARRAYTOBASE64.invoke(null, new Object[] {serialize(obj)});
		} catch (Exception e) {
			throw new RuntimeException("Error when serialize object to string");
		}
	}

	public static Object deserialize(byte[] bytes) {
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bis);
			return ois.readObject();
		} catch (IOException e) {
			throw new RuntimeException("Error when deserialize object");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Error when deserialize object");
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static Object deserializeFromStr(String str) {
		try {
			return deserialize((byte[]) BASE64TOBYTEARRAY.invoke(null, new Object[] {str}));
		} catch (Exception e) {
			throw new RuntimeException("Error when deserialize object from string");
		}
	}
	
	public static boolean isJDK15() {
		return JDK15;
	}
}
