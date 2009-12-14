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
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;


/**
 * This class represents the remote MBean server settings
 * @author jianlu
 *
 */
public class RemoteMBeanServerSettings implements Cloneable {
	private long heartBeatInterval = 5000;
	private int maxHeartBeatCount = 5;
	private long responseWaitTime = 5000;
	private int coreThreadPoolSize = 2;
	private int maxThreadPoolSize = 5;
	private long threadKeepAliveTime = 10000;
	private int threadWorkQueueSize = 10;
	private int tick = 0;
	private String clientId = null;
	private String host = null;
	private String user = null;
	private String pid = null;
	
	private static String FIELD_HEART_BEAT_INTERVAL = "heartBeatInterval";
	private static String FIELD_MAX_HEART_BEAT_COUNT = "maxHeartBeatCount";
	private static String FIELD_RESPONSE_WAIT_TIME = "responseWaitTime";
	private static String FIELD_CORE_THREAD_POOL_SIZE = "coreThreadPoolSize";
	private static String FIELD_MAX_THREAD_POOL_SIZE = "maxThreadPoolSize";
	private static String FIELD_THREAD_KEEP_ALIVE_TIME = "threadKeepAliveTime";
	private static String FIELD_THREAD_WORK_QUEUE_SIZE = "threadWorkQueueSize";
	private static String FIELD_TICK = "tick";
	private static String FIELD_CLIENT_ID = "clientId";
	private static String FIELD_HOST = "host";
	private static String FIELD_USER = "user";
	private static String FIELD_PID = "pid";
	
	
	public static CompositeType OPENTYPE;
	private static String USER = null;
	private static String PID = null;
	private static String HOST = null;
	
	static {
		try {
			OPENTYPE = new CompositeType(RemoteMBeanServerSettings.class.getName(),  
										 "The type used to represent the settings for remote MBeanServer", 
										 new String[] {FIELD_HEART_BEAT_INTERVAL, FIELD_MAX_HEART_BEAT_COUNT, FIELD_RESPONSE_WAIT_TIME, FIELD_CORE_THREAD_POOL_SIZE, FIELD_MAX_THREAD_POOL_SIZE, FIELD_THREAD_KEEP_ALIVE_TIME, FIELD_THREAD_WORK_QUEUE_SIZE, FIELD_TICK, FIELD_CLIENT_ID, FIELD_HOST, FIELD_USER, FIELD_PID}, 
										 new String[] {"heart beat interval", "max heart beat count", "max response wait time", "the core size of the thread pool", "the max size of the thread pool", "the thread keep alife time", "the thread pool queue size", "tick", "client id", "remote host", "remote user", "remote process id"}, 
										 new OpenType[] {SimpleType.LONG, SimpleType.INTEGER, SimpleType.LONG, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.LONG, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING});
		} catch (OpenDataException e) {
			throw new RuntimeException("Can not create CompositeType for MBeanInfo extention");
		}
		
		USER = "dummyUser";
		HOST = "127.0.0.1";
		PID = "12345";
	}
	
	/**
	 * Default constructor
	 */
	public RemoteMBeanServerSettings() {
		this.user = USER;
		this.host = HOST;
		this.pid = PID;
	}
	
	/**
	 * Get heart beat interval
	 * @return Heart beat interval
	 */
	public long getHeartBeatInterval() {
		return heartBeatInterval;
	}

	/**
	 * Set heart beat interval
	 * @param heartBeatInterval Heart beat interval
	 */
	public void setHeartBeatInterval(long heartBeatInterval) {
		this.heartBeatInterval = heartBeatInterval;
	}

	/**
	 * Get core thread pool size
	 * @return Core thread pool size
	 */
	public int getCoreThreadPoolSize() {
		return coreThreadPoolSize;
	}
	
	/**
	 * Set core thread pool size
	 * @param coreThreadPoolSize Core thread pool size
	 */
	public void setCoreThreadPoolSize(int coreThreadPoolSize) {
		if (coreThreadPoolSize <= 0)
			throw new IllegalArgumentException();
		this.coreThreadPoolSize = coreThreadPoolSize;
		if (coreThreadPoolSize > maxThreadPoolSize)
			maxThreadPoolSize = coreThreadPoolSize;
	}
	
	/**
	 * Get the max life time tick property
	 * @return The max life time tick property
	 */
	public int getMaxHeartBeatCount() {
		return maxHeartBeatCount;
	}
	
	/**
	 * Set the max life time tick property
	 * @param maxHeartBeatCount The max life time tick property
	 */
	public void setMaxHeartBeatCount(int maxHeartBeatCount) {
		if (maxHeartBeatCount <= 0)
			throw new IllegalArgumentException();
		this.maxHeartBeatCount = maxHeartBeatCount;
	}
	
	/**
	 * Get max thread pool size
	 * @return Max thread pool size
	 */
	public int getMaxThreadPoolSize() {
		return maxThreadPoolSize;
	}
	
	/**
	 * Set max thread pool size
	 * @param maxThreadPoolSize Max thread pool size
	 */
	public void setMaxThreadPoolSize(int maxThreadPoolSize) {
		if (maxThreadPoolSize <= 0) 
			throw new IllegalArgumentException();
		if (coreThreadPoolSize > maxThreadPoolSize)
			this.maxThreadPoolSize = coreThreadPoolSize;
		else
		    this.maxThreadPoolSize = maxThreadPoolSize;
	}
	
	/**
	 * Get the max wait time for each remote request (in millian seconds)
	 * @return The max wait time for each remote request
	 */
	public long getResponseWaitTime() {
		return responseWaitTime;
	}
	
	/**
	 * Set the max wait time for each remote request (in millian seconds)
	 * @param responseWaitTime The max wait time for each remote request
	 */
	public void setResponseWaitTime(long responseWaitTime) {
		if (responseWaitTime <= 0)
			throw new IllegalArgumentException();
		this.responseWaitTime = responseWaitTime;
	}
	
	/**
	 * Get thread keep alive time (in millian seconds)
	 * @return Thread keep alive time
	 */
	public long getThreadKeepAliveTime() {
		return threadKeepAliveTime;
	}
	
	/**
	 * Set thread keep alive time (in millian seconds)
	 * @param threadKeepAliveTime Thread keep alive time
	 */
	public void setThreadKeepAliveTime(long threadKeepAliveTime) {
		if (threadKeepAliveTime <= 0)
			throw new IllegalArgumentException();
		this.threadKeepAliveTime = threadKeepAliveTime;
	}

	/**
	 * Get thread work queue size
	 * @return Thread work queue size
	 */
	public int getThreadWorkQueueSize() {
		return threadWorkQueueSize;
	}
	

	/**
	 * Set thread work queue size
	 * @param threadWorkQueueSize Thread work queue size
	 */
	public void setThreadWorkQueueSize(int threadWorkQueueSize) {
		this.threadWorkQueueSize = threadWorkQueueSize;
	}

	/**
	 * Get unique client id
	 * @return Client ID
	 */
	public String getClientId() {
		return clientId;
	}

	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public String getPid() {
		return pid;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void tick() {
		tick++;
	}
	
	public void renew() {
		tick = 0;
	}
	
	/**
	 * Is life time expired
	 * @return true or false
	 */
	public boolean isExpired() {
		return tick >= maxHeartBeatCount;
	}

	public CompositeData toOpenData() {
		Map dataMap = new HashMap();
		dataMap.put(FIELD_HEART_BEAT_INTERVAL, new Long(heartBeatInterval));
		dataMap.put(FIELD_MAX_HEART_BEAT_COUNT, new Integer(this.maxHeartBeatCount));
		dataMap.put(FIELD_RESPONSE_WAIT_TIME, new Long(this.responseWaitTime));
		dataMap.put(FIELD_CORE_THREAD_POOL_SIZE, new Integer(this.coreThreadPoolSize));
		dataMap.put(FIELD_MAX_THREAD_POOL_SIZE, new Integer(this.maxThreadPoolSize));
		dataMap.put(FIELD_THREAD_KEEP_ALIVE_TIME, new Long(this.threadKeepAliveTime));
		dataMap.put(FIELD_THREAD_WORK_QUEUE_SIZE, new Integer(this.threadWorkQueueSize));
		dataMap.put(FIELD_TICK, new Integer(this.tick));
		dataMap.put(FIELD_CLIENT_ID, this.clientId);
		dataMap.put(FIELD_HOST, this.host);
		dataMap.put(FIELD_USER, this.user);
		dataMap.put(FIELD_PID, this.pid);
		try {
			return new CompositeDataSupport(OPENTYPE, dataMap);
		} catch (OpenDataException e) {
			throw new RuntimeException("Error when create CompositeData for remote MBeanServer setting");
		}
	}
	
	public void fromOpenData(CompositeData openData) {
		this.heartBeatInterval = ((Long) openData.get(FIELD_HEART_BEAT_INTERVAL)).longValue();
		this.maxHeartBeatCount = ((Integer) openData.get(FIELD_MAX_HEART_BEAT_COUNT)).intValue();
		this.responseWaitTime = ((Long) openData.get(FIELD_RESPONSE_WAIT_TIME)).longValue();
		this.coreThreadPoolSize = ((Integer) openData.get(FIELD_CORE_THREAD_POOL_SIZE)).intValue();
		this.maxThreadPoolSize = ((Integer) openData.get(FIELD_MAX_THREAD_POOL_SIZE)).intValue();
		this.threadKeepAliveTime = ((Long) openData.get(FIELD_THREAD_KEEP_ALIVE_TIME)).longValue();
		this.threadWorkQueueSize = ((Integer) openData.get(FIELD_THREAD_WORK_QUEUE_SIZE)).intValue();
		this.tick = ((Integer) openData.get(FIELD_TICK)).intValue();
		this.clientId = (String) openData.get(FIELD_CLIENT_ID);
		this.host = (String) openData.get(FIELD_HOST);
		this.user = (String) openData.get(FIELD_USER);
		this.pid = (String) openData.get(FIELD_PID);
	}

	public ObjectName getObjectName() {
		try {
			return new ObjectName("system:name=adapter,type=org.jingle.jmxremote.server.RemoteAdapter,clientId=" + clientId);
		} catch (Exception e) {
			throw new RuntimeException("Can not create adapter object name");
		}
	}
	
	public Object clone() {
		RemoteMBeanServerSettings ret = null;
		try {
			ret = (RemoteMBeanServerSettings) super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return ret;
	}
}
