/*
 * PortalFrame.java
 *
 * Started 1999
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.print.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.lang.reflect.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * PortalFrame is a derived JFrame that drives the entire GUI of the
 * portal application.
 *
 * @author John Hegarty
 */
public class PortalFrame
	extends JFrame
	implements Printable, ActionListener, ONetworkListener,
		ComponentListener
{
	MacGlueInterface	mgi = null;		// macintosh-specific goo
	KeyBarDialog	key_bar_dialog;		// dialog to assist user with NovaNET fkeys
	NovaKeysDialog	nova_keys_dialog;	// dialog that displays help on NovaNET keys
	JTabbedPane		portal_pane;		// pane holding LevelOnePanels for connections
	LevelOnePanel	level_one_panel;
	boolean			qt_open;			// true if quicktime opened
	boolean			window_mapped;		// true if window has been mapped
	boolean			ended_session;		// true if we terminated our session
	JLabel			logo_label;			// our big product icon

	boolean			inhibit_tabs = false;

	Session			last_session = new Session();

	/* these variables are only for pre-1.4 java where we can't disable tab directly */
	FocusManager	oldFocusManager;	// Focus manager before redirecting Tab.
	FocusManager	ourFocusManager;	// Our focus manager that lets tab get to NovaNET

	/*********
	 * Configuration data
	 ********/
	Properties		startup_properties;
	Options			options = new Options();
	boolean			options_file_read;
	/* Last configuration file the user has opened */
	File	current_config_file;
	/* File with global options */
	File	options_file;


	/**
	 * Gets configuration information from the registry
	 * for a windows portal convert.
	 */
	
	/* does not work drs 2023/10/3
	 * 

	Session convertRegistryConfiguration(String spec)
	{
	// registry keys:
	//	HKEY_CURRENT_USER/Software/NovaNET Learning/Portal Configuration/
	//		subkeys are config names
	//		sub-subkeys are
	//			NetworkHostName, NetworkTcpPort, TerminalName, TerminalGroup
	//	use the REG.EXE program to extract these values
	//	reg.exe query "HKCU\Software\NovaNET Learning\Portal Configuration"
	//		to get session names
	//	reg.exe query "...\SESSION"
	//		to get all the session settings
		try
		{
		Process child = Runtime.getRuntime().exec("reg.exe query "+
			"\"HKLM\\Software\\NovaNET Learning\\Portal Configuration\\"+spec+"\"");

		BufferedReader	br = new BufferedReader(new InputStreamReader(child.getInputStream()));
		String	line;
		String	host=null,name=null,group=null;
		int		numport=-1;

			while ((line = br.readLine()) != null)
			{
				if	(line.indexOf("NetworkHostName") > -1)
				{
				// take string after REG_SZ
					host = line.substring(line.indexOf("REG_SZ")+6).trim();
				}
				if	(line.indexOf("NetworkTcpPort") > -1)
				{
				// take string after REG_DWORD
				String	port = line.substring(line.indexOf("REG_DWORD")+9).trim().substring(2);
					numport = Integer.parseInt(port,16);
				}
				if	(line.indexOf("TerminalName") > -1)
				{
				// take string after REG_SZ
					name = line.substring(line.indexOf("REG_SZ")+6).trim();
				}
				if	(line.indexOf("TerminalGroup") > -1)
				{
				// take string after REG_SZ
					group = line.substring(line.indexOf("REG_SZ")+6).trim();
				}
			}
			br.close();
			if	(null == host || numport < 1)
				return null;
			return new Session(host,numport,name,group);
		}
		catch (java.lang.Exception e1)
		{
			System.out.println("caught exception: "+e1);
			e1.printStackTrace();
		}
		return null;
	}
*/
	/**
	 * Returns the screen height.
	 */
	int getScreenHeight()
	{
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		return (int) dim.getHeight();
	}

	/**
	 * Returns true if tabbedpane should not be displayed in GUI.
	 */
	boolean noTabbing()
	{
		return inhibit_tabs;
	}

	void addPanel(String name,LevelOnePanel lop)
	{
		if	(noTabbing())
		{
			level_one_panel = lop;
			getContentPane().remove(logo_label);
			getContentPane().add(lop,BorderLayout.CENTER);
			getContentPane().repaint();
		}
		else
		{
			portal_pane.addTab(name,lop);
			adjustBounds();
		}
	}

	void removePanel(LevelOnePanel lop)
	{
		if	(noTabbing())
		{
			getContentPane().remove(lop);
			getContentPane().add(logo_label,BorderLayout.CENTER);
			getContentPane().repaint();
			level_one_panel = null;
		}
		else
		{
			portal_pane.remove(lop);
			adjustBounds();
		}
	}

	/**
	 * Determines the starting/default configuration file to use
	 * for this user.
	 */
	void findDefaultConfig()
	{
	String	user_config_fn = PortalConsts.user_config_file;
	String	global_config_fn = PortalConsts.global_config_file;

		if (PortalConsts.is_macintosh)
		{
		String	home = System.getProperty("user.home");
		String	libprefs = "/Library/Preferences/";

			user_config_fn = home+libprefs+PortalConsts.user_config_file;
			global_config_fn = libprefs+PortalConsts.global_config_file;
			options_file = new File(home+libprefs+PortalConsts.options_file);
		// check for global options file if user one doesn't exist.
			if	(!options_file.exists())
			{
			File	test = new File(libprefs+PortalConsts.options_file);

				if	(test.exists())
					options_file = test;
			}
		}
		else
		{
			options_file = new File(PortalConsts.options_file);

		String	user_prop= startup_properties.getProperty("configfile");

			if	(null != user_prop)
				user_config_fn = user_prop;
		}

	// user-specific config file
	File	uref = new File(user_config_fn);
	// global config file
	File	gref = new File(global_config_fn);

		// use user-specific preferences if it exists -- if not
		// use global one if possible
		if	(!uref.exists() && gref.exists())
			current_config_file = gref;
		else
			current_config_file = uref;
	}

	/**
	 * Gets icon from the jar and tells the frame to use it.
	 */
	private void setPortalIcon()
	{
	java.net.URL	imgURL = getClass().getResource("/com/nn/images/nnlogo.jpg");

		if	(null != imgURL)
			setIconImage(new ImageIcon(imgURL).getImage());
	}

	/**
	 * Changes window title with connection status.
	 */
	public void setStatus(final LevelOnePanel lop,final String status)
	{
		if	(!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					setStatus(lop,status);
				}
			});
		}
		else
		{
			if	(!options.disable_signon_display)
			{
			int	tab_index = findPanelIndex(lop);
				if	(tab_index > -1)
					portal_pane.setTitleAt(tab_index,status);
			}
		}
	}

	/**
	 * Invoked when connection is made to NovaNET host.
	 * NOTE: not invoked from swing thread, be careful.
	 */
	public void networkConnected(final LevelOnePanel lop)
	{
		setStatus(lop,"Not Signed On");
		ended_session = false;
	}


	/**
	 * Invoked when connection is never made to NovaNET host.
	 * NOTE: not invoked from swing thread, be careful.
	 */
	public void networkConnectFailed(final LevelOnePanel lop,final String s)
	{
		setStatus(lop,"Not Connected");
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				lop.endSession();
				removePanel(lop);

				JOptionPane.showMessageDialog(PortalFrame.this,s,"Connection Failed",
					JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	/**
	 * Invoked when connection is lost to NovaNET host.
	 * NOTE: not invoked from swing thread, be careful.
	 */
	public void networkDisconnected(final LevelOnePanel lop)
	{
		setStatus(lop,"Not Connected");
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				lop.endSession();
				removePanel(lop);

				if	(!ended_session)
				{
					JOptionPane.showMessageDialog(PortalFrame.this,
						"<html>"+
						"You are no longer connected to NovaNET, <br>"+
						"you must choose the Open Connection option <br>"+
						"in the File menu to continue working.</html>",
						"Connection Lost",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/**
	 * Method to create instance of MacOSX apple-event,apple-menu handlers using reflection
	 * for cross-platform compatibility.
	 */
	public void macOSXRegistration()
	{

		try
		{
		Class osxAdapter = Class.forName("com.nn.osiris.ui.MacGlue");

			mgi = (MacGlueInterface) osxAdapter.newInstance();
			mgi.init(this);
			if	(PortalConsts.is_debugging)
				System.out.println("Using EAWT extensions for finder; temp="+mgi.getTempFolder());
		}
		catch (NoClassDefFoundError e)
		{
		// This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
		// because OSXAdapter extends ApplicationAdapter in its def
			System.err.println("This version of Mac OS X does not support the Apple EAWT.");
			try
			{
			Class	cls = Class.forName("com.nn.osiris.ui.MacGlueMRJ");

				mgi = (MacGlueInterface) cls.newInstance();
				mgi.init(this);
				if	(PortalConsts.is_debugging)
					System.out.println("Using MRJ extensions for finder; temp="+mgi.getTempFolder());
			}
			catch (NoClassDefFoundError e2)
			{
				System.err.println("This version of Mac OS X does not support the Apple MRJ.");
			}
			catch (Exception e3)
			{
				System.err.println("This version of Mac OS X does not support the Apple MRJ.");
			}


		}
		catch (ClassNotFoundException e)
		{
		// This shouldn't be reached; if there's a problem with the OSXAdapter we should get the 
		// above NoClassDefFoundError first.
			System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
		}
		catch (Exception e)
		{
			System.err.println("Exception while loading the OSXAdapter:");
			e.printStackTrace();
		}
	}

	/**
	 * Saves the options object to disk.
	 */
	public void saveOptions()
	{
		if	(PortalConsts.is_debugging)
			System.out.println("saveoptions invoked");
		try
		{
		FileOutputStream	file = new FileOutputStream(options_file);
		StringBuffer	sb = new StringBuffer();

			sb.append("<?xml version=\"1.0\" standalone='yes'?>\n");
			sb.append("<portaloptions>\n");
			options.toXML(sb);
			sb.append("\n</portaloptions>");

			file.write(sb.toString().getBytes());

			file.close();
		}
		catch (FileNotFoundException fnfe)
		{
			JOptionPane.showMessageDialog(
				this,
				options_file.getAbsolutePath()+" can not be created or modified.",
				"File Not Found",
				JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ioe)
		{
			JOptionPane.showMessageDialog(
				this,
				"An error occurred while writing to "+options_file.getAbsolutePath()+".",
				"IO Exception",
				JOptionPane.ERROR_MESSAGE);
		}
	}


	private void selfUpdate()
	{
	// java.library.path up to first : is directory javaportal.jar sits in
	// java.class.path is path to jar on either system?
		try
		{
/* -- to determine version info of a binary... */
   Package p = this.getClass().getPackage();
   System.out.println("Hello Specification Version : " 
                         + p.getSpecificationVersion());
   System.out.println("Hello Implementation Version : " 
                         + p.getImplementationVersion());

		File	updatedir = new File(System.getProperty("user.dir"),"updates");
		File	orig = new File(updatedir,"javaportal.jar");
		File	nfile = new File(System.getProperty("java.class.path"));
			if	(PortalConsts.is_debugging)
			{
				System.out.println("from file: "+orig.getAbsolutePath());
				System.out.println("to file: "+nfile.getAbsolutePath());
			}
		byte	buffer[] = new byte[1024];
		// loop thru and copy data between the files
		FileInputStream	fis = new FileInputStream(orig);
		FileOutputStream	fos = new FileOutputStream(nfile);
		int				ret;

			while ((ret = fis.read(buffer)) > 0)
				fos.write(buffer,0,ret);

			fos.close();
			fis.close();

		}
		catch (Exception e1)
		{
			if	(PortalConsts.is_debugging)
			{
				System.out.println("selfupdate exception!"+e1.toString());
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Method to create menu hierarchy.
	 */
	private void createMenus()
	{
	JMenuItem				item;
	javax.swing.JMenuBar	jMenuBar1;
	javax.swing.JMenu		jMenuFile;
	javax.swing.JMenu		jMenuEdit;
	javax.swing.JMenu		jMenuSettings;
	javax.swing.JMenu		jMenuHelp;
	
		jMenuBar1 = new javax.swing.JMenuBar();

		jMenuFile = new javax.swing.JMenu("File");
		if	(!PortalConsts.is_macintosh)
			jMenuFile.setMnemonic('F');
		jMenuFile.add (item = new JMenuItem("Open Connection"));
		item.addActionListener(this);
		jMenuFile.add (item = new JMenuItem("Close Connection"));
		item.addActionListener(this);
		jMenuFile.addSeparator();
/* -- this is really useless in java, you dont wanna keep a job
	open forever if they happen to do this, and it's in the
	print dialog anyway.
		jMenuFile.add (item = new JMenuItem("Page Setup..."));
		item.addActionListener(this);
*/
		jMenuFile.add (item = new JMenuItem("Print Screen..."));
		item.addActionListener(this);

		if	(!PortalConsts.is_macintosh)
		{
			jMenuFile.addSeparator();
			jMenuFile.add (item = new JMenuItem("Exit"));
			item.addActionListener(this);
		}
		jMenuBar1.add(jMenuFile);

		jMenuEdit = new javax.swing.JMenu("Edit");
		if	(!PortalConsts.is_macintosh)
			jMenuEdit.setMnemonic('E');

		jMenuEdit.add (item = new JMenuItem("Copy"));
		if	(PortalConsts.is_macintosh)
		{
		// mac only since portal uses all the control keys
		// for novanet stuff under windows, but on macintosh
		// the command key is the special key and not used
		// by portal
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
 		item.addActionListener(this);

		jMenuEdit.add (item = new JMenuItem("Paste"));
		if	(PortalConsts.is_macintosh)
		{
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		item.addActionListener(this);
		jMenuBar1.add(jMenuEdit);

		jMenuSettings = new javax.swing.JMenu("Settings");
		if	(!PortalConsts.is_macintosh)
			jMenuSettings.setMnemonic('S');

		jMenuSettings.add (item = new JMenuItem("Communications..."));
		item.addActionListener(this);
		if	(!PortalConsts.is_macintosh)
		{
			jMenuSettings.add (item = new JMenuItem("Options..."));
			item.addActionListener(this);
		}
		jMenuSettings.addSeparator();
		jMenuSettings.add (item = new JMenuItem("Load Configuration..."));
		item.addActionListener(this);
		jMenuSettings.add (item = new JMenuItem("Save Configuration As..."));
		item.addActionListener(this);
		jMenuBar1.add(jMenuSettings);

		jMenuHelp = new javax.swing.JMenu("Help");
		if	(!PortalConsts.is_macintosh)
			jMenuHelp.setMnemonic('H');

		jMenuHelp.add (item = new JMenuItem("Keyboard..."));
		item.addActionListener(this);
		jMenuHelp.add (item = new JMenuItem("Technical Support..."));
		item.addActionListener(this);
		jMenuHelp.addSeparator();
		jMenuHelp.add (item = new JMenuItem("Function Key Bar"));
		item.addActionListener(this);
		jMenuHelp.addSeparator();
		jMenuHelp.add (item = new JMenuItem("About Multimedia..."));
		item.addActionListener(this);

		if	(!PortalConsts.is_macintosh)
		{
			jMenuHelp.add (item = new JMenuItem("About Portal..."));
			item.addActionListener(this);
		}
		jMenuBar1.add(jMenuHelp);
		
		setJMenuBar(jMenuBar1);
	}

	/**
	 * Method to restore natural focus manager, needed before
	 * displaying a dialog.
	 */
	private void ungrabTabKey()
	{
		if	(oldFocusManager != null)
			FocusManager.setCurrentManager(oldFocusManager);
	}

	/**
	 * Method to re-establish focus grab that lets tab reach
	 * novanet.
	 */
	private void regrabTabKey()
	{
		if	(ourFocusManager != null)
			FocusManager.setCurrentManager(ourFocusManager);
	}

	/**
	 * Prevents a component from getting the focus.
	 */
	private void compFocus(Component c)
	{
		try
		{
		// this method is 1.4+ only
			c.setFocusable(false);
		}
		catch (java.lang.NoSuchMethodError e1)
		{
		}
	}

	/**
	 * Prevents a component from hiding tab keypresses.
	 */
	private void compTab(Component c)
	{
		try
		{
		// this method is 1.4+ only
			c.setFocusTraversalKeysEnabled(false);
		}
		catch (java.lang.NoSuchMethodError e1)
		{
		}
	}

	/**
	 * Method to override default swing behavior of the tab key
	 * which cycles the focus around controls. Since tab is a key
	 * used by novanet, we instead send it off to novanet when pressed.
	 */
	private void grabTabKey()
	{
		try
		{
		// this method is 1.4+ only
			this.setFocusTraversalKeysEnabled(false);
			compTab(portal_pane);
			if	(PortalConsts.is_debugging)
				System.out.println("disabled tab via 1.4 method");
		}
		catch (java.lang.NoSuchMethodError e1)
		{
			// this is a security violation for an applet - later
			if	(PortalConsts.is_applet)
				return;

			if	(PortalConsts.is_debugging)
				System.out.println("disabled tab via swing focus manager");
			oldFocusManager = FocusManager.getCurrentManager();


			// Create and set to a FocusManager that sends <tab> and <shift+tab>
			// to the level one protocol panel.
			ourFocusManager = new DefaultFocusManager()
				{
					/**
					 * Override super class method to pass <tab> and <shift+tab> event
					 * to the level one protocol panel.
					 *
					 * @see	FocusManager#processKeyEvent.
					 */
					public void processKeyEvent(
						Component component,
						KeyEvent event)
					{
						if (null == currentPanel() || !currentPanel().hasFocus())
							return;

						// If it is not a key press, consume and ignore it.
						if (event.getID() != KeyEvent.KEY_PRESSED)
						{
							event.consume();
							return;
						}
						// If it is a key press, let's pass it to novaNET.
						else
						{
							if (!event.isConsumed() && currentPanel().isLegalNovanetKey(event))
							{
								event.consume();
								currentPanel().getParser().keyPressed(event);
							}
						}
					}
				};
			FocusManager.setCurrentManager(ourFocusManager);
		}
	}

	/**
	 * Returns the current level one panel.
	 */
	public LevelOnePanel currentPanel()
	{
		if	(noTabbing())
			return level_one_panel;

	LevelOnePanel	current = (LevelOnePanel) portal_pane.getSelectedComponent();

		if	(null == current && portal_pane.getTabCount() > 0)
			current = (LevelOnePanel) portal_pane.getComponentAt(0);
		return current;
	}

	/**
	 * Safe routine to send a key to novanet.
	 */
	public void SendKey(int novakey)
	{
		if	(currentPanel() != null && currentPanel().getParser() != null)
			currentPanel().getParser().SendKey(novakey);
	}

	/**
	 * Fixes up the window geometry with addition/removal of tabs.
	 */
	public void adjustBounds()
	{
	Container	cp = getContentPane();
	int			tcount = portal_pane.getTabCount();

		if	(PortalConsts.is_debugging)
			System.out.println("adjustbounds, tcount="+tcount);
		if	(tcount < 1)
		{
			if	(noTabbing())
				cp.remove(currentPanel());
			else
				cp.remove(portal_pane);
			if	(null != logo_label)
				cp.add(logo_label,BorderLayout.CENTER);
			cp.repaint();
		}
		else if (tcount == 1)
		{
			if	(null != logo_label)
				cp.remove(logo_label);
			cp.add(portal_pane,BorderLayout.CENTER);
		}
	}

	/**
	 * Invoked when control-tab is hit by the user. Causes a flip
	 * between NovaNET sessions if more than 1 is active.
	 */
	public void controlTab()
	{
	int	tc = portal_pane.getTabCount();

		if	(tc > 1)
		{
			portal_pane.setSelectedIndex((portal_pane.getSelectedIndex()+1)%tc);
		}
	}

	/**
	 * Constructor for frame.
	 */
	public PortalFrame(Properties startup_props)
	{
		super ("Portal");
		setPortalIcon();
		setResizable(false);
		this.startup_properties = startup_props;

		if	(getScreenHeight() <= 600)
			inhibit_tabs = true;

	// determine configuration file
		if	(!PortalConsts.is_applet)
			findDefaultConfig();

		if	(PortalConsts.is_macintosh)
			macOSXRegistration();

	// quicktime for java is not compatible with 64 bit java
		if	(System.getProperty("os.arch").startsWith("x86_64"))
			PortalConsts.is_quicktime = false;

	// initialize quicktime if we think it is present
		
		/*
		if	(PortalConsts.is_quicktime)
		{
			try
			{
				PortalConsts.is_quicktime = false;
				quicktime.QTSession.open();
				qt_open = true;
				PortalConsts.is_quicktime = true;
				if	(PortalConsts.is_debugging) System.out.println("Quicktime initialized");
			}
			catch (java.lang.ExceptionInInitializerError e1)
			{
				if	(PortalConsts.is_debugging) System.out.println("Quicktime exception_in_initializer_error");
			}
			catch (java.lang.Exception e2)
			{
				if	(PortalConsts.is_debugging) System.out.println("Quicktime exception");
			}
			catch (NoClassDefFoundError e3)
			{
				if	(PortalConsts.is_debugging) System.out.println("Quicktime no_class_def_found_error");
			}
		}
*/
//	CREATE MENUS AND MENUBAR
		createMenus();

//	CREATE CONTENT
		Container	cp = getContentPane();


//	Create lower layer
	java.net.URL	imgURL = getClass().getResource("/com/nn/images/offline.jpg");
	ImageIcon		offlineIcon;

		if	(null != imgURL)
		{
			offlineIcon = new ImageIcon(imgURL);
			logo_label = new JLabel(offlineIcon);
			logo_label.setOpaque(true);
			logo_label.setBackground(Color.white);
			logo_label.setPreferredSize(new Dimension(640,532));
			cp.add(logo_label,BorderLayout.CENTER);
		}
		else if (PortalConsts.is_debugging)
			System.out.println("could not read logo com/nn/images/offline.jpg");

//	Create tabbed pane
		portal_pane = new JTabbedPane();
		compFocus(portal_pane);

/*
	arrange to get tab keys in the portal panel - otherwise Swing would
	make tab unusable on Novanet.
*/
		grabTabKey();

		addWindowListener(new java.awt.event.WindowAdapter()
		{
			/**
			 * Invoked when window is closed, shuts things down.
			 */
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				thisWindowClosing(e);
			}

			/**
			 * Invoked when the window sees the light of day,
			 * grabs focus for the novanet panel.
			 */
			public void windowOpened(java.awt.event.WindowEvent e)
			{
				thisWindowOpened();
			}
		});
	}


	/** used to keep track of what image we are going to print for
		the callback routine **/
	private Image	printing_image;

	/**
	 * Renders any java image on the graphic object of a
	 * printer, scaling it to fit as needed.
	 */
	public void renderImage(Graphics pg,PageFormat pf,Image src)
	{
	int		srcWidth = src.getWidth(this);
	int		srcHeight = src.getHeight(this);
	int		dx1 = (int) pf.getImageableX();
	int		dy1 = (int) pf.getImageableY();
	double	width = pf.getImageableWidth();
	double	height = pf.getImageableHeight();
	double	scalew = srcWidth/width;
	double	scaleh = srcHeight/height;

		pg.translate(dx1,dy1);
		dx1 = dy1 = 0;
		// note: this case doesn't work on the macintosh - which
		// seems to be a bug in the AWT printing for mac.
		if	(scalew <= 1 && scaleh <= 1)
		{
		// it fits, so just draw it
			pg.drawImage(src,
				dx1,
				dy1,
				dx1+(int) width,
				dy1+(int) height,
				0,
				0,
				srcWidth,
				srcHeight,
				this);
		}
		else if (scalew > scaleh)
		{
		// width is worst fit
			pg.drawImage(src,
				dx1,dy1,
				(int)pf.getImageableWidth(),
				(int)(srcHeight/scalew),
				this);
		}
		else
		{
		// height is worst fit
			pg.drawImage(src,
				dx1,dy1,
				(int)(srcWidth/scaleh),
				(int)pf.getImageableHeight(),
				this);
		}
	}


	/**
	 * Invoked by awt as a callback when we print.
	 */
	public int print(Graphics g, PageFormat pf, int pi)
							  throws PrinterException
	{
		// we are just printing images that are 1 page...
		if (pi >= 1)
		{
			return Printable.NO_SUCH_PAGE;
		}
		renderImage(g,pf,printing_image);
		return Printable.PAGE_EXISTS;
	}

	/**
	 * Arranges for any java image to spew out of the
	 * printer tray.
	 */
	public void printImage(Image img,boolean is_dialog)
	{
	// Get a PrinterJob
	java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
		// Ask user for page format (e.g., portrait/landscape)
	boolean	not_cancelled = true;

		job.setPrintable(this);
		if	(is_dialog)
			not_cancelled = job.printDialog();

		if	(not_cancelled)
		{
			try
			{
				printing_image = img;
				job.print();
			}
			catch (java.lang.Exception e1)
			{
			}
		}
	}

	/**
	 * Invoked when user wants to do a print screen.
	 */
	public void doPrint()
	{
		printImage(currentPanel().getImage(),true);
	}

	/**
	 * Invoked when user selects about... menu item
	 */
	public void aboutBox()
	{
	JOptionPane.showMessageDialog(this,
		"Portal Version "+PortalConsts.short_version+", Copyright 2010 Pearson Education",
		"About Portal",
		JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Invoked when user selects about multimedia... menu item
	 */
	public void aboutMultimedia()
	{
	String	version = "unknown";

		try
		{
		FileReader	fr = new FileReader(new File("AW7","awinfo.ini"));
		BufferedReader	br = new BufferedReader(fr);
		String	l;

			for (;;)
			{
				l = br.readLine();
				if	(null == l)
					break;
				if	(l.startsWith("Version="))
				{
					version = l.substring(8);
					break;
				}
			}
			
		}
		catch (java.lang.Exception e1)
		{
		}

	JOptionPane.showMessageDialog(this,
		"Multimedia Version "+version,
		"About Multimedia",
		JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Invoked from protocol engine when user signs off novanet.
	 */
	public void signOut(LevelOnePanel lop)
	{
		setStatus(lop,"Not Signed On");

	// disconnect after signoff if user configured that option
		if	(options.disconnect_at_signoff)
		{
			lop.endSession();
			removePanel(lop);
		}
	}

	/**
	 * Invoked from protocol engine when user is logged into novanet,
	 * gives signon name in use.
	 */
	public void setTitle(LevelOnePanel lop,String title)
	{
		setStatus(lop,title);
	}

	/**
	 * Invoked when user selects copy menu item
	 */
	public void doCopy()
	{
		currentPanel().doCopy();
	}

	/**
	 * Invoked when user selects paste menu item
	 */
	public void doPaste()
	{
		try
		{
		Toolkit			tk = Toolkit.getDefaultToolkit();
		Clipboard		cp = tk.getSystemClipboard();
		Transferable	t = cp.getContents(null);

			if	(t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor))
			{
			String	s = (String) t.getTransferData(DataFlavor.stringFlavor);

				currentPanel().getParser().sendString(s);
			}
		}
		catch (java.lang.Exception e1)
		{
			System.out.println("paste error:"+e1);
		}
	}

	/**
	 * Displays dialog with most important portal configuration options.
	 */
	public void doCommunicationsDialog()
	{
		ungrabTabKey();

	CommunicationsDialog	communications = 
		new CommunicationsDialog(
				this,
				"Communications Configuration",
				true,					// modal
				last_session,
				options);
		communications.show();

		if	(!communications.isCancelled())
			communications.getValues().copyTo(last_session);

		regrabTabKey();
	}

	/**
	 * Opens a new connection to NovaNET using the
	 * passed configuration.
	 */
	public void doOpenConnection(Session session)
	{
		if	((noTabbing() && null != level_one_panel) || (portal_pane.getTabCount() > 0 && !options.multi_connect))
		{
			JOptionPane.showMessageDialog(
				this,
				"You already have a connection open.",
				"Connection open",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		session.copyTo(last_session);

	// don't try it if using default bogus host
		if	(session.host.equals(PortalConsts.default_host))
			return;

	LevelOnePanel	lop = new LevelOnePanel(this,JPortal.frame_width);
	double			rate = 0.0;

		lop.addNetworkListener(this);
		lop.addComponentListener(this);
		addPanel(session.host,lop);
		if	(startup_properties.getProperty("localfail") != null)
		{
			try
			{
				session.local_os_failure_rate = Double.parseDouble(startup_properties.getProperty("localfail"));
			}
			catch (java.lang.NumberFormatException e1)
			{
			}
		}
		lop.startSession(session);

		compTab(lop);

		this.pack();
	}

	/**
	 * Returns index in the JTabbedPane for the given level one panel.
	 */
	int findPanelIndex(LevelOnePanel lop)
	{
	int	cnt = portal_pane.getTabCount();

		for (int i=0;i<cnt; i++)
		{
			if	(lop == ((LevelOnePanel) portal_pane.getComponentAt(i)))
				return i;
		}

		return -1;
	}

	/**
	 * Closes the current connection to NovaNET
	 */
	public void doCloseConnection()
	{
		if	(null != currentPanel())
		{
			currentPanel().endSession();
			removePanel(currentPanel());
			ended_session = true;
		}
	}

	/**
	 * Macintosh preferences menu item callback
	 */
	public void doPreferences()
	{
		doOptionsDialog();
	}

	/**
	 * Brings up and drives the options dialog.
	 */
	public void doOptionsDialog()
	{
	String	title;

		ungrabTabKey();

		if	(PortalConsts.is_macintosh)
			title = "Preferences";
		else
			title = "Options";

	OptionsDialog	options_dialog = 
		new OptionsDialog(
				this,
				title,
				true,					// modal
				options);

		options_dialog.show();

	// options are always saved immediately
		if	(!options_dialog.isCancelled())
		{
			if	(PortalConsts.is_debugging)
				System.out.println("options updated from dialog");
			options_dialog.getValues().copyTo(options);
			saveOptions();
		}
		else if (PortalConsts.is_debugging)
			System.out.println("options dialog cancelled");

		options_dialog.dispose();

		regrabTabKey();
	}

	/**
	 * Loads a configuration based on a filename. Used for macintosh
	 * configuration file loading.
	 */
	public void loadConfig(String filename)
	{
		readConfigurationFile(new File(filename));
	}

	/**
	 * Writes the configuration settings to the user's preferences file.
	 */
	public void savePreferences(File prefsfile)
	{
		try
		{
		FileOutputStream	file = new FileOutputStream(prefsfile);
		StringBuffer	sb = new StringBuffer();

			sb.append("<?xml version=\"1.0\" standalone='yes'?>\n");
			sb.append("<configuration>\n");
			if	(portal_pane.getTabCount() > 0)
			{
				for (int i=0; i<portal_pane.getTabCount(); i++)
				{
				// write sessions to xml file
				LevelOnePanel	lop = (LevelOnePanel) portal_pane.getComponentAt(i);

					lop.session.toXML(sb);
				}
			}
			else
				last_session.toXML(sb);

			sb.append("\n</configuration>");

			file.write(sb.toString().getBytes());

			file.close();
			if	(mgi != null)
				mgi.associateConfigFile(prefsfile);
		}
		catch (FileNotFoundException fnfe)
		{
			JOptionPane.showMessageDialog(
				this,
				prefsfile.getAbsolutePath()+" can not be created or modified.",
				"File Not Found",
				JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ioe)
		{
			JOptionPane.showMessageDialog(
				this,
				"An error occurred while writing to "+prefsfile.getAbsolutePath()+".",
				"IO Exception",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Writes the configuration settings to the user's preferences file.
	 */
	public void savePreferences()
	{
		if	(options.lock_configuration)
		{
			JOptionPane.showMessageDialog(
				this,
				"The configuration file is locked and cannot be modified.",
				"Configuration locked",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		savePreferences(current_config_file);
	}

	/**
	 * Invoked when a swing control action callback happens
	 */
	public void actionPerformed (ActionEvent e)
	{
	String	cmd = e.getActionCommand();

		if	(PortalConsts.is_debugging)
	    	System.out.println ("actionPerformed: "+cmd);
		if	(cmd.equals("About Portal..."))
			aboutBox();
		if	(cmd.equals("About Multimedia..."))
		{
			aboutMultimedia();
		}

		if	(cmd.equals("Keyboard..."))
		{
			if	(nova_keys_dialog == null)
			{
				nova_keys_dialog = new NovaKeysDialog(this);
			}

			nova_keys_dialog.show();
		}

		if	(cmd.equals("Function Key Bar"))
		{
			if	(key_bar_dialog == null)
				key_bar_dialog = new KeyBarDialog(this);

			key_bar_dialog.show();
		}

		if	(cmd.equals("Technical Support..."))
		{
			JOptionPane.showMessageDialog(this,
				"If you are having problems installing, configuring, or using this software, please call Pearson Digital Learning Product Support at 1-888-977-7100",
				"Technical Support",
				JOptionPane.PLAIN_MESSAGE);
		}

		if	(cmd.equals("Page Setup..."))
		{
		// Get a PrinterJob
		java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
			// Ask user for page format (e.g., portrait/landscape)
		java.awt.print.PageFormat pf = job.pageDialog(job.defaultPage());
		}

		if	(cmd.equals("Print Screen..."))
		{
			doPrint();
		}

		if	(e.getActionCommand().equals("Open Connection"))
			doOpenConnection(last_session);

		if	(e.getActionCommand().equals("Close Connection"))
			doCloseConnection();

		if	(cmd.equals("Paste"))
			doPaste();

		if	(e.getActionCommand().equals("Copy"))
		{
			currentPanel().doCopy();
		}

		if	(cmd.equals("Quit") || cmd.equals("Exit"))
		{
//			currentPanel().endSession();

			//if	(qt_open)
			//	quicktime.QTSession.close();
			setVisible(false);
			dispose();
			if	(!PortalConsts.is_applet)
				System.exit(0);
		}

		if	(e.getActionCommand().equals("Communications..."))
		{
			doCommunicationsDialog();
		}

		if	(e.getActionCommand().equals("Options..."))
		{
			doOptionsDialog();
		}

		if	(e.getActionCommand().equals("Load Configuration..."))
		{
			doLoadConfiguration();
		}

		if	(e.getActionCommand().equals("Save Configuration As..."))
		{
			doSaveConfiguration();
		}
	}

	/**
	 * Reads an old properties configuration file
	 */
	void readOldConfiguration(File fref,boolean set_options)
	{
		if	(PortalConsts.is_debugging)
			System.out.println("readold="+fref);
		try
		{
		FileInputStream	file = new FileInputStream(fref);
		Properties	settings = new Properties();

			settings.load(file);
			file.close();
			if	(set_options)
			{
				options.readProperties(settings);
				if	(!fref.canWrite())
					options.lock_configuration = true;
			}

		Session	s = new Session(settings);

			doOpenConnection(s);
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Reads a new style XML configuration file.
	 */
	public void readXMLConfiguration(File fref)
	{
		if	(PortalConsts.is_debugging)
			System.out.println("readxmlconfig="+fref);
		try
		{
		DocumentBuilderFactory	dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder	db = dbf.newDocumentBuilder();
		Document	doc;

			doc = db.parse(fref);

		NodeList	nl = doc.getElementsByTagName("session");

			for (int i=0; i<nl.getLength(); i++)
			{
			Session	s = new Session(nl.item(i));

				doOpenConnection(s);
			}
		}
		catch (java.lang.Exception e1)
		{
		}
	}

	/**
	 * Reads options from new style config file.
	 */
	public void readXMLOptions(File fref)
	{
		if	(PortalConsts.is_debugging)
			System.out.println("readxmloptions="+fref);
		try
		{
		DocumentBuilderFactory	dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder	db = dbf.newDocumentBuilder();
		Document	doc;

			doc = db.parse(fref);

		NodeList	nl = doc.getElementsByTagName("options");

			for (int i=0; i<nl.getLength(); i++)
			{
				options.readNode(nl.item(i));
			}
		}
		catch (java.lang.Exception e1)
		{
		}
	}


	/**
	 * Reads a configuration file into session/options variables.
	 */
	void readConfigurationFile(File fref)
	{
	// need to determine if file is xml or not
		try
		{
		FileReader	fr = new FileReader(fref);
		BufferedReader	br = new BufferedReader(fr);
		String	l;

			l = br.readLine();
			br.close();
			if	(l.startsWith("<?xml"))
				readXMLConfiguration(fref);
			else
				readOldConfiguration(fref,!options_file_read && !options_file.exists());
		}
		catch (java.lang.Exception e1)
		{
		}
	}

	/**
	 * Read the portal options into memory.
	 */
	void readOptionsFile()
	{
	// if haven't read options yet, read them
		if	(!options_file_read)
		{
			options_file_read = true;
			if	(options_file.exists())
				readXMLOptions(options_file);
		}
	}

	/**
	 * Invoked when the user wants to load a configuration file.
	 */
	void doLoadConfiguration()
	{
	JFileChooser	fc = new JFileChooser();

		if	(mgi != null)
		{
			fc.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
				public boolean accept(File fref)
				{
					return mgi.isConfigFile(fref);
				}

				public String getDescription()
				{
					return "Portal File Filter";
				}
			});
		}

		if	(JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this))
		{
			readConfigurationFile(fc.getSelectedFile());
		}
	}

	/**
	 * Invoked when the user wants to save their configuration.
	 */
	void doSaveConfiguration()
	{
	JFileChooser	fc = new JFileChooser();

		if	(JFileChooser.APPROVE_OPTION == fc.showSaveDialog(this))
		{
			savePreferences(fc.getSelectedFile());
		}
	}

	/**
	 * Callback invoked when the window gets mapped.
	 */
	void thisWindowOpened()
	{
		window_mapped = true;
		initializeApplication();
	}

	/**
	 * Callback when a macintosh finder event to open a configuration file happens.
	 */
	void macOpenFile(String filename)
	{
		current_config_file = new File(filename);
		if	(window_mapped)
			readConfigurationFile(current_config_file);
	}

	/**
	 * Called when it is time to start things going.
	 */
	public void initializeApplication()
	{
		if	(!PortalConsts.is_applet)
			readOptionsFile();

		/*
		if	(startup_properties.getProperty("winconfig") != null)
		{
		Session	s = convertRegistryConfiguration(startup_properties.getProperty("winconfig"));

			if	(null != s)
			{
				if	(PortalConsts.is_debugging)
					System.out.println("opening converted winconfig: "+startup_properties.getProperty("winconfig"));
				doOpenConnection(s);
				return;
			}
		}
		*/

		// make command line options overrule everything else --
		if	(startup_properties.getProperty("network_host") != null)
		{
			if	(PortalConsts.is_debugging)
				System.out.println("configuring from command line");

		Session	s = new Session(startup_properties);

			doOpenConnection(s);
		}
		// if no configuration file, prompt user to create same
		else if	(PortalConsts.is_applet || !current_config_file.exists())
		{
			if	(PortalConsts.is_debugging)
				System.out.println("no config file: "+current_config_file);

			if	(startup_properties.getProperty("network_host") != null)
			{
				if	(PortalConsts.is_debugging)
					System.out.println("configuring from command line");

			Session	s = new Session(startup_properties);

				doOpenConnection(s);
			}
			else
			{
				JOptionPane.showMessageDialog(this,
					"<html>The Portal needs to be configured. When you click the OK<br>"+
					"button, you will be taken to the main configuration dialog.</html>",
					"Configuration",
					JOptionPane.WARNING_MESSAGE);
				doCommunicationsDialog();
				savePreferences();
				doOptionsDialog();
			}
		}
		else
			readConfigurationFile(current_config_file);
	}

	/**
	 * Callback invoked when user closes the portal window.
	 */
	void thisWindowClosing(java.awt.event.WindowEvent e)
	{
		if	(key_bar_dialog != null)
		{
			key_bar_dialog.dispose();
			key_bar_dialog = null;
		}
		if	(nova_keys_dialog != null)
		{
			nova_keys_dialog.dispose();
			nova_keys_dialog = null;
		}
//		if (null != jPortalPanel)
//			jPortalPanel.endSession();
//		if	(qt_open)
//			quicktime.QTSession.close();
		setVisible(false);
		dispose();
		System.exit(0);
	}

	/**
	 * Returns macintosh temp folder for current user.
	 */
	

	String getMacintoshTempFolder()
	{
		if	(mgi != null)
			return mgi.getTempFolder();

		return System.getProperty("java.io.tmpdir");
	}


	/**
	 * Opens specified URL.
	 */
	public void openURL(String urlspec)
		throws java.io.IOException
	{
		if	(mgi != null)
			mgi.openURL(urlspec);
		else
			BrowserLauncher.openURL(urlspec);
	}

	/**
	 * Callback when LevelOnePanel is hidden.
	 */
	public void componentHidden(ComponentEvent e)
	{
	}

	/**
	 * Callback when LevelOnePanel is moved.
	 */
	public void componentMoved(ComponentEvent e)
	{
	}

	/**
	 * Callback when LevelOnePanel is resized.
	 */
	public void componentResized(ComponentEvent e)
	{
	}

	/**
	 * Callback when LevelOnePanel is made visible,
	 * which is a good time to obtain the focus for it.
	 * Note: this doesn't get called ever basically.
	 */
	public void componentShown(ComponentEvent e)
	{
		if	(PortalConsts.is_debugging)
			System.out.println("requesting focus on "+e.getComponent());
		e.getComponent().requestFocus();
	}
}
