/**
 * PROJECT	Portal panel
 * FILE		LevelOneNetwork.java
 *
 *			(c) copyright 1999
 *			NCS NovaNET Learning
 *			
 * @author	J Hegarty
 */

package com.nn.osiris.ui;

import java.net.*;
import java.io.*;
import javax.swing.*;
//import java.awt.*;
//import java.util.*;
 
/**
 * This class manages the connection to the host.
 */
public class LevelOneNetwork extends Thread
{
	/** The panel that handles the connection this object handles data for */
	LevelOnePanel	panel;
	/** Circular buffer for holding incoming network data. */
	public com.nn.osiris.ui.CircularBuffer ocbuffer = new com.nn.osiris.ui.CircularBuffer(16384);
	
	/** Number of attempts we make to connect to the host
		this was gt one for NGN where we might have gotten
		running before NNIAC, for normal use 1 is plenty of
		attempts */
	static final int MAX_CONNECTION_ATTEMPTS = 1;
	static final int CONNECTION_RETRY_MSECONDS = 1000;

	/** The remote host we are connecting to, if any */	
	String	host;
	/** The remote port we are connecting to, if any */
	int		port;
	/** True if we need to open a connection */
	boolean	do_connect=false;
	/** True if we are in the process of connecting to a host */
	boolean	in_connect=false;
	/** The socket with the connection to the remote host. */
	NetConnInterface nci;
	/** The state of the network connection. */
	private boolean is_network_alive=false;
	/** The network input stream. */
	private InputStream in;
	/** The network output stream. */
	private OutputStream out;
	/** Circular buffer for holding outgoing network data. */
	private CircularBuffer out_buffer = new CircularBuffer(4096);
	/** Circular buffer for holding outgoing network data delays. */
	private CircularBuffer out_delay = new CircularBuffer(4096);

	/** Thread that writes network data at a metered rate. */
	private NetWriter nw_handle;

	/**
	 * Registered network listener. This should be used by the level one
	 * classes only. That is why only one is listener is needed.
	 */
	private ONetworkListener network_listener;

	/**
	 * Constructs a level one network.
	 */
	public LevelOneNetwork(LevelOnePanel panel)
	{
		this.panel = panel;
	}

	/**
	 * Returns true if a connection is being established.
	 */
	public boolean isConnecting()
	{
		return in_connect;
	}

	/**
	 * Creates TCP connection to specified host and port.
	 *
	 * @param	host	The host to connect to.
	 * @param	port	The port to connect to.
	 * @throws	UnknownHostException
	 *					Unable to find novaNET host.
	 * @throws	IOException
	 *					Unable to connect to host and port.
	 */
	public void connect(String host,int port)
	{
		this.host = host;
		this.port = port;

		if	(SwingUtilities.isEventDispatchThread())
		{
			do_connect = true;
			return;
		}

		do_connect = false;

	boolean	load_error = false;

		try
		{
		Class<?> netclass = Class.forName("com.nn.osiris.ui.NetConnModern");

			nci = (NetConnInterface) netclass.getDeclaredConstructor().newInstance();
			if	(PlatoConsts.is_debugging)	System.out.println("Using JRE1.5+ NetConnInterface");
		}
		catch (UnsupportedClassVersionError e)
		{
			load_error = true;
		}
		catch (ClassNotFoundException e2)
		{
			load_error = true;
		}
		catch (InstantiationException e3)
		{
			load_error = true;
		}
		catch (IllegalAccessException e4)
		{
			load_error = true;
		}
		catch (Exception notpossible)
		{
			if	(PlatoConsts.is_debugging)	System.out.println("ERROR: unable to load netconninterface");
			load_error = true;
		}
		
		if	(load_error)
		{
			try
			{
			Class<?> netclass = Class.forName("com.nn.osiris.ui.NetConnInterface");

				nci = (NetConnInterface) netclass.getDeclaredConstructor().newInstance();
				if	(PlatoConsts.is_debugging)	System.out.println("Using JRE<1.5 NetConnInterface");
			}
			catch (Exception notpossible)
			{
				if	(PlatoConsts.is_debugging)	System.out.println("ERROR: unable to load netconninterface");
			}
		}

		// Make several attempts to establish the connection when a
		// connection attempt fails.
		for (int num_tries = 0;
			num_tries < MAX_CONNECTION_ATTEMPTS;
			num_tries++)
		{
			try
			{
				nci.connect(host,port);
				in = nci.getInputStream();
				out = nci.getOutputStream();
				setNetworkAlive(true);
				break;
			}
			catch (UnknownHostException exception)
			{
				notifyConnectFailed("Host name lookup failed.");
			}
			catch (IOException exception)
			{
				if	(PlatoConsts.is_debugging)
				{
					System.out.println("Connection attempt #"+(num_tries+1)+
						" to host: "+host+" port: "+port+" failed.");	//!! DEBUG
				}
				
				// If we have failed all of our attempts, give up.
				if (num_tries+1 == MAX_CONNECTION_ATTEMPTS && null != network_listener)
				{
					notifyConnectFailed("Unable to connect to host.");
				}
				
				// Wait for a second between attempts.
				if	(num_tries+1 < MAX_CONNECTION_ATTEMPTS)
				{
					try
					{
						Thread.sleep(CONNECTION_RETRY_MSECONDS);	
					}
					catch (Exception e)
					{
					}
				}
			}
			catch (java.lang.Exception generic)
			{
				notifyConnectFailed("Other exception: "+generic.toString());
			}
		}
	}

	/**
	 * Notifies listener that connect didn't happen.
	 */
	void notifyConnectFailed(String s)
	{
		if (PlatoConsts.is_debugging) System.out.println("notifyconnectfailed: "+s);
		if	(null != network_listener)
		{
		ONetworkListener	nl = network_listener;

			network_listener = null;
			nl.networkConnectFailed(panel,s);
		}
	}

	/**
	 * Notifies listener that network closed.
	 */
	void notifyDisconnect()
	{
		if	(null != network_listener)
		{
		ONetworkListener	nl = network_listener;

			network_listener = null;
			nl.networkDisconnected(panel);
		}
	}

	/**
	 * Set state of network, up or down. Notifies listener if any.
	 */
	void setNetworkAlive(boolean state)
	{
	boolean	old_state = is_network_alive;

		is_network_alive = state;
		if	(PlatoConsts.is_debugging)	System.out.println("setnetworkalive="+state);

		if	(old_state != state && null != network_listener)
		{
			if	(state)
				network_listener.networkConnected(panel);
			else
				notifyDisconnect();
		}
	}

	/**
	 * Disconnect from the network.
	 */
	public void disconnect()
	{
		setNetworkAlive(false);

		if (null != nci)
		{
			nci.close();
			nci = null;
		}

		if (null != nw_handle)
		{
			try
			{
				nw_handle.join();
			}
			catch (java.lang.InterruptedException e2)
			{
			}
		}

		in = null;
		out = null;
		nw_handle = null;
	}

	/**
	 * Thread main loop.  Waits for data on the socket and places it into a
	 * circular buffer for later processing via the network worker thread.
	 * The data is accumulated rather than processed one byte at a time
	 * because the protocol interpreter operates with greater efficiency on
	 * larger packets.  In JAVA this makes a real difference in the
	 * performance of the program.
	 */
	public void run()
	{
		if	(do_connect)
		{
			in_connect = true;
			connect(host,port);
			in_connect = false;
		}

		if	(is_network_alive)
		{
			nw_handle = new NetWriter();
			nw_handle.start();
		}

		while (is_network_alive)
		{
		int	limit;
		int result;

			try
			{
				// Determine data that can be read into circular buffer at
				// one read.
				limit = ocbuffer.EnqueueBufferLimit();

				result = in.read(
					ocbuffer.buffer,
					ocbuffer.GetEnqueueBuffer(),
					limit);

				if (result < 0)
					break;
				ocbuffer.EnqueuedBytes(result);
			}
			catch (IOException e)
			{
				break;
			}	
		}
		
		if	(PlatoConsts.is_debugging)	System.out.println("Network connection died");

		setNetworkAlive(false);
	}

	/**
	 * Write a buffer to network destination with specified delay.
	 *
	 * @param	buffer	Buffer of content to be written.
	 * @param	offset	Offset from the buffer to start writing.
	 * @param	length	Length of the content to be written.
	 * @param	delay	Delay before writing.
	 */
	public void write(
		byte[] buffer,
		int offset,
		int length,
		int delay)
	{
		if (out_buffer != null)
		{
			if (length != out_buffer.Enqueue(buffer, offset, length))
			{
				if	(PlatoConsts.is_debugging)	System.out.println("ERROR: network write failed, buffer overflow");
			}

			out_delay.EnqueueZeroes(length-1);

			if (delay > 255)
				delay = 255;
			out_delay.Enqueue(delay);
		}
	}

	/**
	 * Write a buffer to network destination with default delay.
	 *
	 * @param	buffer	Buffer of content to be written.
	 * @param	offset	Offset from the buffer to start writing.
	 * @param	length	Length of the content to be written.
	 */
	public void write(
		byte[] buffer,
		int offset,
		int length)
	{
		// Default 50 keys/sec limit.
		write(buffer, offset, length, 20*length);
	}

	/**
	 * Add network listener. This should only be called by the level one
	 * classes.
	 *
	 * @param	listener	The network listener to be registered.
	 */
	public void addNetworkListener(ONetworkListener listener)
	{
		network_listener = listener;
	}

	/**
	 * Final cleanup for network object.
	 */
	void dispose()
	{
		network_listener = null;
		ocbuffer = null;
		out_buffer = null;
		out_delay = null;
	}
	
	/**
	 * Represent writing network data to novanet network.
	 */
	private class NetWriter extends Thread
	{
		/**
		 * Thread main loop.  Sends out network data with speed controls to
		 * avoid overloading novanet network.
		 */
		public void run()
		{
			while (is_network_alive)
			{
			int	xmit_length = 0;
			int delay = 0;

				while (is_network_alive)
				{
					delay = out_delay.Dequeue();
					if (-1 == delay)
					{
						if (xmit_length > 0)
							break;
						else
						{
							// Sit and wait if no data ready to go.
							delayThread(25);
							continue;
						}
					}
					xmit_length++;
					if (delay > 0)
						break;
				}

				while (xmit_length > 0)
				{
					int	max_xmit = out_buffer.DequeueBufferLimit();

					if (max_xmit > xmit_length)
						max_xmit = xmit_length;
					try
					{
						out.write(
							out_buffer.buffer,
							out_buffer.GetDequeueBuffer(),
							max_xmit);
					}
					catch (IOException e)
					{
						if	(PlatoConsts.is_debugging)	System.out.println("network thread: write failed");
					}
					xmit_length -= max_xmit;
					out_buffer.DequeuedBytes(max_xmit);
				}

				if (delay > 0)
					delayThread(delay);
			}
		}

		/**
		 * Delay thread.
		 *
		 * @param	milliseconds	Number of milliseconds to delay.
		 */
		public void delayThread(int milliseconds)
		{
			try
			{
				Thread.sleep(milliseconds);
			}
			catch (java.lang.InterruptedException e)
			{
			}
		}
	}
}
