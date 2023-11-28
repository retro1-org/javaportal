/*
 * JPortal.java
 *
 * Started 1999
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.awt.*;
//import java.awt.event.*;
import java.util.*;
//import java.io.*;
//import netscape.javascript.*;

/**
 * JPortal is the main class of the Java Portal application/applet.
 * It sets up environment information/configuration and creates
 * the main frame of the interface.
 *
 * @author John Hegarty
 */
public class PlatoAccess extends javax.swing.JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -11L;
	// Configuration file object
	private static 	Properties	settings;
	static PlatoAccess		global_jportal;
	static	int			frame_width = PlatoConsts.default_width;
	ScormInterface		scorminterface = null;

	PlatoFrame			frame = null;
	
	static String		current_dir = null;

	/** 
	 * Applet initialization function, invoked by the JVM
	 * when the portal is running as an applet.
	 */
	public void init()
	{
		initializeConstants(true);
/*
		try
		{
		Class	cls = Class.forName("com.nn.ui.ScormImplementer");

			scorminterface = (ScormInterface) cls.newInstance();
			scorminterface.init(this);
		}
		catch (NoClassDefFoundError e1)
		{
		}
		catch (java.lang.ClassNotFoundException e2)
		{
		}
		catch (Exception e3)
		{
		}
		finally
		{
			if	(null == scorminterface)
				scorminterface = new ScormInterface();
		}
*/
	}

	/**
	 * Applet startup function, creates the user interface
	 * when portal is running as an applet.
	 */
	
/*
	public void start()
	{
		settings = new Properties();

		// Override settings. (No lesson or unit defined here.)
	String	rsserver = getParameter("rsserver");
	String	applethost = getParameter("applethost");
	String	appletport = getParameter("appletport");
	String	novaname = getParameter("novaname");
	String	novagroup = getParameter("novagroup");

		if	(rsserver != null)
		{
			if	(PortalConsts.is_debugging) System.out.println("rsserver="+rsserver);
			settings.setProperty("resource_server",rsserver);
		}
		if	(getParameter("resource_prefix") != null)
			settings.setProperty("resource_prefix",getParameter("resource_prefix"));
		if	(applethost != null)
		{
			if	(PortalConsts.is_debugging) System.out.println("applet host="+applethost);
			settings.setProperty("network_host",applethost);
		}
		if	(appletport != null)
		{
			if	(PortalConsts.is_debugging) System.out.println("applet port="+appletport);
			settings.setProperty("network_host_port",appletport);
		}
		if	(novaname != null)
		{
			if	(PortalConsts.is_debugging) System.out.println("applet sname="+novaname);
			settings.setProperty("name",novaname);
		}
		if	(novagroup != null)
			settings.setProperty("group",novagroup);
		if	(getParameter("appletlesson") != null)
		{
			if	(PortalConsts.is_debugging) System.out.println("applet lesson="+getParameter("appletlesson"));
			settings.setProperty("lesson",getParameter("appletlesson"));
		}
		if	(scorminterface.getLesson() != null)
		{
			settings.setProperty("lesson",scorminterface.getLesson());
		}

		if	(getParameter("appletunit") != null)
			settings.setProperty("unit",getParameter("appletunit"));

		//Execute a job on the event-dispatching thread:
		//creating this applet's GUI.
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					PortalConstruct();
				}
			});
		} catch (Exception e) {
			System.err.println("createGUI didn't successfully complete");
		}

	}
*/
	
	/**
	 * Applet stop function, called when the browser wants
	 * portal to be stopped ... which depends on which browser is
	 * used
	 */
	public void stop()
	{
	}

	/**
	 * Final applet clean up function
	 */
	public void destroy()
	{
	}

	/**
	 * Called to setup some constants describing the environment
	 * portal is running in.
	 */
	static void initializeConstants(boolean is_applet)
	{
		current_dir = System.getProperty("user.dir");

		if	(is_applet)
		{
			PlatoConsts.is_applet = true;
			PlatoConsts.is_quicktime = false;
		// rest of this code is not good in applets
			return;
		}
		else
			PlatoConsts.is_applet = false;

		if	(PlatoConsts.is_debugging)
			System.getProperties().list(System.out);

		// check to see if we are on some sort of macintosh
		if (System.getProperty("mrj.version") != null)
		{
			PlatoConsts.is_macintosh = true;
			PlatoConsts.is_quicktime = true;
		}
		else
		{
			PlatoConsts.is_macintosh = false;
			if	(System.getProperty("os.name").startsWith("Wind"))
			{
				PlatoConsts.is_windows = true;
				if	(PlatoConsts.is_debugging)
					System.out.println("Detected Windoze platform...");
			}
		}
	}

	/**
	 * Constructor for portal class.
	 */
	public PlatoAccess()
	{
		global_jportal = this;
	}

/*
	public void CreateAppletUI()
	{
	LevelOnePanel	lop = new LevelOnePanel(null,frame_width);

		lop.startSession(new Session(settings));
		getContentPane().add(lop,BorderLayout.CENTER);
		this.pack();
	}
*/
	
  public void pack() {
    Container cp = getContentPane();
    Dimension d = cp.getLayout().preferredLayoutSize(cp);
    setSize((int)d.getWidth(),(int)d.getHeight());
  }

	/**
	 * Function that does actual work to create user interface,
	 * shared by application and applet code.
	 */
	public void PortalConstruct()
	{
		if	(PlatoConsts.is_debugging)
			System.out.println(java.lang.System.getProperty("java.version"));
	
		if (null == scorminterface)
			scorminterface = new ScormInterface();

		try
		{
/*
			if	(PortalConsts.is_applet)
			{
				CreateAppletUI();
			}
			else
*/
			{
				frame = new PlatoFrame(settings);
				frame.pack();
				frame.setVisible(true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets properties that need to be set very early on to affect the Mac GUI.
	 */
	static
	{
		try
		{
		// 1.3.1 growbox control
			System.setProperty("com.apple.mrj.application.growbox.intrudes","false");
		// 1.4 growbox control
			System.setProperty("apple.awt.showGrowBox","false");
		// 1.3.1 menu bar control factor
			System.setProperty("com.apple.macos.useScreenMenuBar", "true");

		//~~ note the following is supposed to set the application name in the mac menu,
		//~~ but it doesn't work, at least in 1.4.x
			System.setProperty("com.apple.mrj.application.apple.menu.about.name","Portal");
		}
		/*
		catch (java.security.AccessControlException e1)
		{
		// will splat here with applet runs - no big deal
		}
		*/
		catch (Exception e1)
		{
		// will splat here with applet runs - no big deal
		}

	}

	/**
	 * Sets system properties for proper look/operation under Mac OS X.
	 */
	static void macProperties()
	{
		// this is the JDK 1.4 one
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}


	/**
	 * First function invoked when running as an application,
	 * creates instance of portal class after initializing
	 * some variables.
	 */
	static public void main(String[] args) 
	{
		settings = new Properties();
		PlatoAccess.initializeConstants(false);

		if	(PlatoConsts.is_macintosh)
			macProperties();


		// Override settings with the application arguments.
		for (int i=0; i<args.length; i++)
		{
			if	(args[i].equals("-rs") && i+1 < args.length)
			{
				settings.setProperty("resource_server", args[i+1]);
				if	(PlatoConsts.is_debugging)
					System.out.println("Using resource server: "+settings.getProperty("resource_server"));
			}
			if	(args[i].equals("-host") && i+1 < args.length)
			{
				settings.setProperty("network_host", args[i+1]);
			}
			if	(args[i].equals("-port") && i+1 < args.length)
			{
				settings.setProperty("network_host_port", args[i+1]);
			}
			if	(args[i].equals("-name") && i+1 < args.length)
			{
				settings.setProperty("name", args[i+1]);
			}
			if	(args[i].equals("-group") && i+1 < args.length)
			{
				settings.setProperty("group", args[i+1]);
			}
			if	(args[i].equals("-lesson") && i+1 < args.length)
			{
				settings.setProperty("lesson", args[i+1]);
			}
			if	(args[i].equals("-unit") && i+1 < args.length)
			{
				settings.setProperty("unit", args[i+1]);
			}
			if	(args[i].equals("-config") && i+1 < args.length)
			{
				settings.setProperty("configfile", args[i+1]);
			}
			if	(args[i].equals("-winconfig") && i+1 < args.length)
			{
				settings.setProperty("winconfig",args[i+1]);
			}
			if	(args[i].equals("-localfail") && i+1 < args.length)
			{
				settings.setProperty("localfail",args[i+1]);
			}
			if	(args[i].equals("-mtdisk0") && i+1 < args.length)
			{
				settings.setProperty("mtdisk0",args[i+1]);
			}
			if	(args[i].equals("-mtdisk1") && i+1 < args.length)
			{
				settings.setProperty("mtdisk1",args[i+1]);
			}
			if	(args[i].equals("-mtboot") && i+1 < args.length)
			{
				settings.setProperty("mtboot",args[i+1]);
			}
			if	(args[i].equals("-width") && i+1 < args.length)
				frame_width = Integer.parseInt(args[i+1]);
		}

		PlatoAccess jportal = new PlatoAccess();

		jportal.PortalConstruct();
	}

	/**
	 * Converts boolean to string - dont use Boolean.toString because
	 * of JDK 1.4 limitation on that routine...
	 */
	static public String boolString(boolean b)
	{
		if	(b)
			return "true";
		else
			return "false";
	}

	/**
	 * Converts string to boolean, simpler than  Boolean.valueOf(XXX).booleanValue();
	 */
	static public boolean stringBool(String s)
	{
		return Boolean.valueOf(s).booleanValue();
	}
}
