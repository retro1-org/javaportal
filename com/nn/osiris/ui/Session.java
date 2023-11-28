package com.nn.osiris.ui;

import org.w3c.dom.*;
//import java.io.*;

class Session
{
	String	host;
	int		port;
	String	name;
	String	group;
	// esoteric, mostly designed for NGN but can be specified
	// thru a NNIAC session startup.
	String	lesson;
	String	unit;
	String	rs_host;
	// for testing/debug of aw stub
	double local_os_failure_rate;
	// mtutor boot files and flag
	String mtdisk0;
	String mtdisk1;
	String mtboot;

	
	
	public void toXML(StringBuffer sb)
	{
		sb.append("<session host=\""+host+"\"");
		sb.append(" port=\""+port+"\"");
		/*
		
		if	(name != null)
			sb.append(" name=\""+name+"\"");
		if	(group != null)
			sb.append(" group=\""+group+"\"");
	*/
		if	(mtdisk0 != null && mtdisk0.length() > 0)
			sb.append(" mtdisk0=\""+mtdisk0+"\"");
		if	(mtdisk1 != null  && mtdisk1.length() > 0)
			sb.append(" mtdisk1=\""+mtdisk1+"\"");
		if	(mtboot != null  && mtboot.length() > 0)
			sb.append(" mtboot=\""+mtboot+"\"");
		sb.append("/>");
	}

	public Session()
	{
		host = PlatoConsts.default_host;
		port = PlatoConsts.default_port;
		name = group = null;
	}

	public Session(String host,int port,String name,String group)
	{
		this.host = host;
		this.port = port;
		this.name = name;
		this.group = group;
	}

	/**
	 * Create a session from a <session> element in
	 * an XML configuration file.
	 */
	public Session(Node session_node)
	{
	NamedNodeMap	nnm = session_node.getAttributes();
	Node	n;

		n = nnm.getNamedItem("host");
		if	(null == n)
			this.host = PlatoConsts.default_host;
		else
			this.host = n.getNodeValue();
		n = nnm.getNamedItem("port");
		if	(null == n)
			this.port = PlatoConsts.default_port;
		else
		{
			try
			{
				this.port = Integer.parseInt(n.getNodeValue());
			}
			catch (NumberFormatException e1)
			{
				this.port = PlatoConsts.default_port;
			}
		}
		n = nnm.getNamedItem("name");
		if	(null == n)
			this.name = "";
		else
			this.name = n.getNodeValue();
		
		n = nnm.getNamedItem("group");
		if	(null == n)
			this.group = "";
		else
			this.group = n.getNodeValue();

		n = nnm.getNamedItem("mtdisk0");
		if	(null == n)
			this.mtdisk0 = null;
		else
		{
			this.mtdisk0 = n.getNodeValue();
		}
		
		n = nnm.getNamedItem("mtdisk1");
		if	(null == n)
			this.mtdisk1 = null;
		else
		{
			this.mtdisk1 = n.getNodeValue();
		}
		
		n = nnm.getNamedItem("mtboot");
		if	(null == n)
			this.mtboot = null;
		else
		{
			this.mtboot = n.getNodeValue();
		}
		
		
	}

	/**
	 * Create a session from a java properties object.
	 */
	public Session(java.util.Properties p)
	{
		this.host = p.getProperty("network_host",PlatoConsts.default_host);
		this.port = Integer.parseInt(p.getProperty("network_host_port",Integer.toString(PlatoConsts.default_port)));
		this.name = p.getProperty("name");
		this.group = p.getProperty("group");
		this.lesson = p.getProperty("lesson");
		this.unit = p.getProperty("unit");
		this.mtdisk0 = p.getProperty("mtdisk0");
		this.mtdisk1 = p.getProperty("mtdisk1");
		this.mtboot = p.getProperty("mtboot");
		this.rs_host = p.getProperty("resource_server");
	}

	/**
	 * Copy this session to another session object.
	 */
	public void copyTo(Session x)
	{
		x.host = this.host;
		x.port = this.port;
		x.name = this.name;
		x.group = this.group;
		x.lesson = this.lesson;
		x.unit = this.unit;
		x.mtdisk0 = this.mtdisk0;
		x.mtdisk1 = this.mtdisk1;
		x.mtboot = this.mtboot;
	}
};