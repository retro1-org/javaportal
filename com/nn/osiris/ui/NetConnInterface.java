/*
 * NetConnInterface.java
 *
 * Started 2005
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.net.*;
import java.io.*;

class NetConnInterface
{
	Socket	socket;

	public void connect(String host,int port)
		throws java.lang.Exception
	{
		try
		{
			socket = new Socket(host, port);
		}
		catch (java.lang.Exception woof)
		{
			System.out.println("net problem: " + woof);
			throw woof;
		}
	}

	public InputStream getInputStream()
		throws java.io.IOException
	{
		return socket.getInputStream();
	}

	public OutputStream getOutputStream()
		throws java.io.IOException
	{
		return socket.getOutputStream();
	}

	public void close()
	{
		try
		{
			if	(null != socket)
				socket.close();
		}
		catch (java.io.IOException e1)
		{
		}
	}
}
