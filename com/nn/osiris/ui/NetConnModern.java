package com.nn.osiris.ui;

import java.net.*;

/**
 * Network connection interface derived for JRE 1.5+
 */
class NetConnModern extends NetConnInterface
{
	public void connect(String host,int port)
		throws java.lang.Exception
	{
	ProxySelector	ps = ProxySelector.getDefault();
	java.util.List<Proxy>	plist;
	Proxy	proxy = null;

//	ask the system if there is a proxy we should use
		plist = ps.select(new URI("socket://"+host+":"+port));

//	if there is, select the first socks proxy
		if	(plist != null && !plist.isEmpty())
		{
			for (int pi=0;pi<plist.size();pi++)
			{
				proxy = plist.get(pi);
				if	(PlatoConsts.is_debugging) System.out.println("evaluating proxy: "+proxy.toString());
				if	(proxy.type() == Proxy.Type.SOCKS)
					break;
			}
		}
		if (PlatoConsts.is_debugging) System.out.println("using proxy: "+proxy.toString());

//	if we found a proxy to use, connect using it
		if	(null != proxy)
		{
			socket = new Socket(proxy);

		InetSocketAddress dest = new InetSocketAddress(host, port);

			socket.connect(dest);
		}
//	otherwise just connect like always
		else
		{
			socket = new Socket(host,port);
		}
	}
}
