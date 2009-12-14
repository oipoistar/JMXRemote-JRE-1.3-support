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
package org.jingle.jmxremote.client.spring;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

import org.jingle.jmxremote.client.MBeanServerImpl;
import org.jingle.jmxremote.common.RemoteMBeanServerSettings;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class is a plugin for Spring framework. It acts as a FactoryBean to generate
 * MBeanServer instance.
 * @author jianlu
 *
 */
public class MBeanServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
	MBeanServerConnection connection = null;
	MBeanServer server = null;
	RemoteMBeanServerSettings settings = null;
	
	/**
	 * Get RemoteMBeanServerSettings instance which is used to config the MBeanServerImpl.
	 * @return RemoteMBeanServerSettings instance.
	 */
	public RemoteMBeanServerSettings getSettings() {
		return settings;
	}
	
	/**
	 * Set RemoteMBeanServerSettings instance which is used to config the MBeanServerImpl.
	 * @param settings
	 */
	public void setSettings(RemoteMBeanServerSettings settings) {
		this.settings = settings;
	}
	
	/**
	 * Get MBeanServerConnection instance.
	 * @return MBeanServerConnection instance.
	 */
	public MBeanServerConnection getConnection() {
		return connection;
	}
	
	/**
	 * Set MBeanServerConnection instance
	 * @param connection The MBeanServerConnection instance.
	 */
	public void setConnection(MBeanServerConnection connection) {
		this.connection = connection;
	}
	

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		return server;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return (this.server != null ? this.server.getClass() : MBeanServer.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (connection == null)
			throw new IllegalArgumentException("connection can not be null");
		if (settings == null)
			settings = new RemoteMBeanServerSettings();
		server = new MBeanServerImpl(connection, settings);
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
	}
}
