package com.nn.osiris.ui;

import org.w3c.dom.*;
//import java.io.*;
import java.util.*;

class Options
{
	// true if multiple connections are allowed
	boolean	multi_connect;
	// true if we disconnect from socket upon novanet signoff
	boolean	disconnect_at_signoff;
	// true if we don't display novanet signon in window
	boolean	disable_signon_display;
	// true if configuration settings are locked
	boolean	lock_configuration;

	public Options()
	{
		multi_connect = false;
		disconnect_at_signoff = false;
		disable_signon_display = false;
		lock_configuration = false;
	}

	public void copyTo(Options x)
	{
		x.multi_connect = this.multi_connect;
		x.disconnect_at_signoff = this.disconnect_at_signoff;
		x.disable_signon_display = this.disable_signon_display;
		x.lock_configuration = this.lock_configuration;
	}

	public void toXML(StringBuffer sb)
	{
		sb.append("<options multi_connect=\""+JPortal.boolString(multi_connect)+"\"");
		sb.append(" disconnect_at_signoff=\""+JPortal.boolString(disconnect_at_signoff)+"\"");
		sb.append(" disable_signon_display=\""+JPortal.boolString(disable_signon_display)+"\"");
		sb.append(" lock_configuration=\""+JPortal.boolString(lock_configuration)+"\"");
		sb.append("/>\n");
	}

	public void readProperties(Properties p)
	{
		if	(PortalConsts.is_debugging)
			System.out.println("options set from properties");
		this.disconnect_at_signoff = JPortal.stringBool(p.getProperty("disconnect_signoff"));
		this.disable_signon_display = JPortal.stringBool(p.getProperty("disable_name"));
	}

	public void readNode(Node options_node)
	{
	NamedNodeMap	nnm = options_node.getAttributes();
	Node	n;

		if	(PortalConsts.is_debugging)
			System.out.println("options set from XML");
		n = nnm.getNamedItem("multi_connect");
		if	(null != n)
		{
			this.multi_connect = JPortal.stringBool(n.getNodeValue());
			if	(PortalConsts.is_debugging)
				System.out.println("multi-connect="+this.multi_connect);
		}
		n = nnm.getNamedItem("disconnect_at_signoff");
		if	(null != n)
			this.disconnect_at_signoff = JPortal.stringBool(n.getNodeValue());
		n = nnm.getNamedItem("disable_signon_display");
		if	(null != n)
			this.disable_signon_display = JPortal.stringBool(n.getNodeValue());
		n = nnm.getNamedItem("lock_configuration");
		if	(null != n)
			this.lock_configuration = JPortal.stringBool(n.getNodeValue());
	}
}