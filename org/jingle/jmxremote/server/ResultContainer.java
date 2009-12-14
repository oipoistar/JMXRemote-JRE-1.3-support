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
import javax.management.JMRuntimeException;

public class ResultContainer {
	public static final int GETTER_RESULT = 1;
	public static final int SETTER_RESULT = 2;
	public static final int GETTERS_RESULT = 3;
	public static final int SETTERS_RESULT = 4;
	public static final int OP_RESULT = 5;
	public static final int PRE_REGISTER_RESULT = 6;
	public static final int ADD_LISTENER_RESULT = 8;
	public static final int REMOVE_LISTENER_RESULT = 9;

	int type;
	OperationResult result = null;
	boolean filled = false;
	
	public ResultContainer(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isFilled() {
		return filled;
	}
	
	public boolean hasException() throws InterruptedException {
		synchronized(this) {
			if (!filled)
				throw new InterruptedException();
			return result.hasException();
		}
	}
	
	public void setResult(OperationResult result) {
		synchronized(this) {
			filled = true;
			this.result = result;
			this.notifyAll();
		}
	}
	
	public Object getResult() throws InterruptedException {
		synchronized (this) {
			if (!filled)
				throw new InterruptedException();
			if (result.hasException())
				return null;
			switch(type) {
			    case GETTER_RESULT:
				    return ((Attribute)(result.getAttributes().get(0))).getValue();
			    case GETTERS_RESULT:
					return result.getAttributes();
			    case SETTER_RESULT:
				    return result.getAttributes().get(0);
			    case SETTERS_RESULT:
				    return result.getAttributes();
			    case OP_RESULT:
				    return ((Attribute)(result.getAttributes().get(0))).getValue();
			    case PRE_REGISTER_RESULT:
				    return ((Attribute)(result.getAttributes().get(0))).getValue();
			    case ADD_LISTENER_RESULT:
				    return null;
			    case REMOVE_LISTENER_RESULT:
				    return null;
				default:
					return null;
			}
		}
	}
	
	public Exception getException() throws InterruptedException {
		synchronized(this) {
			if (!filled)
				throw new InterruptedException();
			if (result.hasException()) {
				Throwable t = result.getException();
				if (t instanceof Exception)
					return (Exception)t;
				else
					return new JMRuntimeException(t.getMessage());
			}
			return null;
		}
	}
}

