/**
 * PROJECT		Portal panel
 *				(c) copyright 1999
 *				NCS NovaNET Learning
 *			
 * @author		J Hegarty
 */

package com.nn.osiris.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.Vector;
import javax.swing.*;
import java.awt.datatransfer.*;
//import java.awt.print.*;

/**
 * This class represents the panel that displays the level one protocol.
 */
public class LevelOnePanel
	extends JPanel
	implements MouseListener, ActionListener, MouseMotionListener, ClipboardOwner
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -151L;

	/** Level one panel height. */
	public static final int PANEL_HEIGHT = 512;

	/** The frame that holds this panel. */
	private Frame parent_frame;
	/** The level one parser. */
	private LevelOneParser level_one_parser;
	/** List of registered network listeners. */
	private Vector<ONetworkListener> network_listeners = new Vector<ONetworkListener>();
	/** The level one network to use for connecting to novaNET. */
	private LevelOneNetwork level_one_network;
	/** Network timer. */
	private javax.swing.Timer network_timer;
	/** Width of the panel. */
	private int panel_width;
	/** Offscreen image for doing our own double buffering. */
	Image offscreen;
	/** Version of java this is running on. */
	private String java_version = System.getProperty("java.version");
	/** Vector to keep track of threads that need cleanup */
	Vector<LevelOneNetwork> thread_list = new Vector<LevelOneNetwork>();

	/** Variables holding information about the current screen selection */
	Rectangle lastMark = null;
    Rectangle currentRect = null;
    Rectangle rectToDraw = null;
    Rectangle previousRectDrawn = new Rectangle();
	boolean	isMarkingArea;

	Session		session;
	ImageIcon	offlineIcon;
	ConnectDialog	connectDialog;


	/**
	 * Gets the image to display when the user is not connected
	 * to NovaNET. Product branding!
	 */
	void getOfflineImage()
	{
	java.net.URL	imgURL = getClass().getResource("/com/nn/images/offline.jpg");

		if	(null != imgURL)
			offlineIcon = new ImageIcon(imgURL);
	}

	/**
	 * Construct objects and threads for the level one panel.
	 *
	 * @param	parent_frame	The frame which holds this panel.
	 * @param	width			The width of this panel.
	 */
	public LevelOnePanel(
		Frame parent_frame,
		int width)
  	{
  		super(false);	// Don't use double buffering.

  		this.parent_frame = parent_frame;
		panel_width = width;

		// Get icon to display when not connected
		getOfflineImage();

		// Setup our dimensions as we want them.
		Dimension panel_dim = new Dimension(panel_width, PANEL_HEIGHT);

		setMinimumSize(panel_dim);
		setMaximumSize(panel_dim);
		setPreferredSize(panel_dim);
		setSize(panel_dim);
		// No need of a layout manager since we are using the panel just to
		// mirror our offscreen image.
  		setLayout(null);
		setBackground(Color.black);

		offscreen = new BufferedImage(
			panel_width,
			PANEL_HEIGHT,
			BufferedImage.TYPE_INT_RGB);

		// Get ourselves an appropriate protocol interpreter depending on
		// the java version.
		createProtocolInterpreter();

		addKeyListener(new KeyAdapter()
		{
			/**
			 * Override super class method to pass legal novaNET keys to
			 * novaNET and let others continue to be processed by other
			 * controls.
			 *
			 * @see	KeyApdapter#keyPressed
			 */
			public void keyPressed(KeyEvent event)
			{
				// If it is a legal novaNET key, send it to novaNET and
				// mark it consumed. 
				if (!event.isConsumed() && isLegalNovanetKey(event))
				{
					level_one_parser.keyPressed(event);
					event.consume();
				}
			}
		});

		addMouseListener(this);
		addMouseMotionListener(this);

		// Create a timer to process network data. This is done because
		// all drawing must be done by the GUI thread, hence, we do not draw
		// from the thread that listens for network data.
		network_timer = new Timer(5, this);
	}

	void closeConnectDialog()
	{
		if	(connectDialog != null && !SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					closeConnectDialog();
				}
			});
		}
		else
		{
			if	(connectDialog != null)
			{
				connectDialog.dispose();
				connectDialog = null;
			}
		}
	}

	/**
	 * Start a novaNET session (either for NGN or non-NGN) with specified
	 * signon name and group and a specific lesson and its arguments to run.
	 * Although for non-NGN session, it does not make sense to specify lesson
	 * name, unit name, and arguments, do it this way to unify start session.
	 *
	 * @param	host		The host for the novaNET session.
	 * @param	port		The port of the host to connect to.
	 * @param	rs_host			Resource server host name.
	 * @param	resource_prefix	Resource prefix for getting a resource from
	 *							the resource server. It is the meta resource
	 *							process ID.
	 * @param	name		The signon name for auto signon.
	 * @param	group		The group name for auto signon.
	 * @param	lesson		The lesson to run for auto signon.
	 * @param	unit		The unit within the lesson to run for auto signon.
	 * @param	arguments	The arguments (as they are) for the lesson to run.
	 * @param	is_for_ngn	Session is for NGN.
	 * @return	true		Session successfully started.
	 *			false		Session not started due to network error.
	 */
	public boolean startSession(Session session)
	{
		this.session = session;
		// Things look different when connected:
		repaint();
		level_one_parser.clearScreen();
		// kill any previous session and clean up
		endSession();
		
		if (this.session.mtutor != null)
		{
			if (PortalConsts.is_debugging) System.out.println("--- mtutor boot file : " + this.session.mtutor );
		}
		
		// Create a level one network to be used for this session.
		level_one_network = new LevelOneNetwork(this);

		// Register a network listener so we propagate event to all network
		// listeners registered to this panel.
		level_one_network.addNetworkListener(new ONetworkListener()
		{
			/**
			 * Override interface method to propagate connected event to all
			 * registered listeners.
			 */
			public void networkConnected(final LevelOnePanel lop)
			{
				closeConnectDialog();
				for (int index = 0;
					index < network_listeners.size();
					index++)
				{
					((ONetworkListener)
						network_listeners.get(index)).networkConnected(lop);
				}
			}

			/**
			 * Override interface method to propagate disconnencted event to
			 * all registered listeners.
			 */
			public void networkDisconnected(final LevelOnePanel lop)
			{
				closeConnectDialog();
				for (int index = 0;
					index < network_listeners.size();
					index++)
				{
					((ONetworkListener)
						network_listeners.get(index)).networkDisconnected(lop);
				}
			}

			/**
			 * Override interface method to propagate never connected event to
			 * all registered listeners.
			 */
			public void networkConnectFailed(final LevelOnePanel lop,final String s)
			{
				closeConnectDialog();
				for (int index = 0;
					index < network_listeners.size();
					index++)
				{
					((ONetworkListener)
						network_listeners.get(index)).networkConnectFailed(lop,s);
				}
			}
		});

		try
		{
			connectDialog = new ConnectDialog(parent_frame,"Network","Opening connection to host...");
			connectDialog.setVisible(true);

			// Connect to remote host.
			level_one_network.connect(session.host, session.port);

			// Start the thread that listens to the network.
			level_one_network.start();
			
			// Start timer to get network data processed.
			network_timer.start();
			
			// Tell protocol interpreter about the network to use.
			level_one_parser.setNetwork(level_one_network);
			level_one_parser.setResourceServer(session.rs_host, null);
			level_one_parser.setLocalFailureRate(session.local_os_failure_rate);

			level_one_parser.setUser(
				session.name,
				session.group,
				session.lesson,
				session.unit,
				null,
				null);
			// Since it always requires user to press enter to get it started,
			// let us do it for the user.
			if	(null == session.lesson)
				level_one_parser.sendString("\n");
			
			
			if (this.session.mtutor != null)
			{
				level_one_parser.cpu = new PZ80Cpu();
				level_one_parser.cpu.Init(level_one_parser);
				level_one_parser.z80 = level_one_parser.cpu.z80;			// shortcut
				
				level_one_parser.center_x = (PortalConsts.default_width -512) / 2;

				level_one_parser.cpu.BootMtutor(this.session.mtutor);
			}
					

			return true;
		}
		catch (Exception exception)
		{
			//++ Maybe want to deal with it more rigorously.
			return false;
		}
	}

	/**
	 * Invoked via novanet l1p to inform user that
	 * their session is dying.
	 */
	public void closeSession(String message)
	{
	StringBuffer	sb = new StringBuffer();
	int				j=0;

		// remove ends of lines which
		// are difficult to display:
		for (j=0; j<message.length(); j++)
		{
		char	c = message.charAt(j);

			if	(c == '\\')
			{
				sb.append(' ');
				j++;
			}
			else
				sb.append(c);
		}

		JOptionPane.showMessageDialog(parent_frame,
			sb.toString(),
			"Host Session Closed",
			JOptionPane.PLAIN_MESSAGE);
		endSession();
	}

	/**
	 * End the novaNET session.
	 */
	public void endSession()
	{
		closeConnectDialog();
		// erase backing store on disconnect
		level_one_parser.clearScreen();
		// Take the network away from protocol interpreter.
		level_one_parser.setNetwork(null);
		level_one_parser.setResourceServer(null, null);
		level_one_parser.setUser(null, null, null, null, null, null);

		// Disassociate meta resource process since it is per session.
		level_one_parser.setProcessID(null);

		// Stop the network data processor timer.
		network_timer.stop();
		
		// If the network exists, stop the thread that listens to the network.
		if (null != level_one_network)
		{
			level_one_network.disconnect();
			level_one_network.dispose();

			if	(level_one_network.isConnecting())
			{
				if	(PortalConsts.is_debugging)	System.out.println("adding thread to collection list");
				thread_list.add(level_one_network);
			}
			else
			{
				// Wait for thread to exit.
				try
				{
					level_one_network.join(500);
				}
				catch (java.lang.InterruptedException e1)
				{
				}
			}

			// Allow thread to get garbage collected.
			level_one_network = null;
			// Things look different when not connected:
			repaint();
		}
	}

	/**
	 * Returns image that reflects what the user currently sees.
	 */
	Image getImage()
	{
		if	(null == level_one_network && null != offlineIcon)
			return offlineIcon.getImage();

		return offscreen;
	}

	/**
	 * Paint level one content in the panel. This function is invoked when
	 * a part of our window is exposed.
	 *
	 * @param	graphics	The graphics component to paint it on.
	 */
	boolean setresize = false,setsize = false;
	boolean	requestfocus = false;

    public void paintComponent(Graphics graphics)
    {
		if	(!requestfocus)
		{
			requestFocus();
			requestfocus = true;
		}
/*
	double panelw = getSize().getWidth();
	double panelh = getSize().getHeight();

		if	(panelw < panel_width || panelh < PANEL_HEIGHT)
		{
			if	(!setsize)
			{
				setSize(panel_width,PANss	ssEL_HEIGHT);
				setsize = true;
			}
		}
		else if (!setresize)
		{
			parent_frame.setResizable(false);
			setresize = true;
		}
*/

		// When not connected, the panel contains different
		// content indicating that.
		if	(null == level_one_network)
		{
			graphics.setColor(Color.white);
			graphics.fillRect(0,0,panel_width,PANEL_HEIGHT);
			if	(null == offlineIcon)
			{
				graphics.setColor(Color.blue);
				for (int i=0; i<PANEL_HEIGHT; i+=5)
					graphics.drawLine(0,i,panel_width,i);
				graphics.setColor(Color.black);
				graphics.drawString("You are not connected to the host",
					(panel_width-200)>>1,266);
			}
			else
			{
			int	w = offlineIcon.getIconWidth();
			int	h = offlineIcon.getIconHeight();

				offlineIcon.paintIcon(this,graphics,(panel_width-w)>>1,(PANEL_HEIGHT-h)>>1);
			}
			return;
		}
		// Draw from the backing store.
		graphics.drawImage(offscreen, 0, 0, this);

		// Draw cursor
		level_one_parser.ShowCursor(graphics);

		// Draw selection
        if (currentRect != null)
		{
			//Last marked rect user has seen:
			lastMark = currentRect;
            //Draw a rectangle on top of the image.
            graphics.setXORMode(Color.white); //Color of line varies
                                       //depending on image colors
            graphics.drawRect(rectToDraw.x, rectToDraw.y, 
                       rectToDraw.width - 1, rectToDraw.height - 1);

        }
    }

	/**
	 * Called when novanet protocol engine thinks it's time
	 * to stop drawing over current screen with a mark!
	 */
	public void clearMark()
	{
		if	(!isMarkingArea)
			currentRect = null;
	}

	/**
	 * Invoked by JVM when we lose clipboard ownership.
	 */
	public void lostOwnership(Clipboard c,Transferable t)
	{
	}

	/**
	 * Add a network listener to the level one panel.
	 *
	 * @param	listener	The network listener to be registered.
	 */
	public void addNetworkListener(ONetworkListener listener)
	{
		network_listeners.add(listener);
	}

	/**
	 * Get the level one parser underneath the panel.
	 *
	 * @return	The level one parser we are using.
	 */
	public LevelOneParser getParser()
	{
		return level_one_parser;
	}

	/**
	 * Override interface method. This is called via the timer every few ms.
	 *
	 * @see	ActionListener#actionPerformed
	 */
	public void actionPerformed(ActionEvent event)
	{
		// Check on threads left laying around to join with.
		if	(!thread_list.isEmpty())
		{
		Thread	t = (Thread) thread_list.get(0);

			if	(!t.isAlive())
			{
				if	(PortalConsts.is_debugging)	System.out.println("thread not alive, joining it");

				try
				{
					t.join(5);
					thread_list.remove(t);
				}
				catch (Exception e1)
				{
				}
			}
		}

		// process network data that has arrived
		this.processData();
	}

	/**
	 * Processes NovaNET network data.
	 */
	synchronized void processData()
	{
		// If level one parser doesn't exist yet, just exit.
		if (null == level_one_parser)
			return;

		// We process network data with a low thread priority. This has been
		// done because when quicktime loads movies from a URL, the portal
		// CPU consumption goes to 100%. When the web server is on the same
		// machine it can never process our request to load the URL.  Lowering
		// the priority eliminates this problem.
		int	oldpriority = Thread.currentThread().getPriority();

		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

		if (level_one_network.ocbuffer.BytesQueued() > 0)
		{
			int	result = level_one_parser.ParseStream(
				level_one_network.ocbuffer.buffer,
				level_one_network.ocbuffer.GetDequeueBuffer(),
				level_one_network.ocbuffer.DequeueBufferLimit());

			if	(null != level_one_network)
				level_one_network.ocbuffer.DequeuedBytes(result);
		}
		else
			level_one_parser.ParseStream(null, 0, 0);

		Thread.currentThread().setPriority(oldpriority);
	}

	/**
	 * Called from protocol engine when a display delay
	 * is requested by novanet. Delays next processing of
	 * network data by specified time. Used by TUTOR lessons
	 * to perform smooth animations.
	 */
	void startDelay(int ms)
	{
		network_timer.setInitialDelay(ms);
		network_timer.restart();
	}

    void updateSize(MouseEvent e)
	{
        int x = e.getX();
        int y = e.getY();
		if	(currentRect != null)
		{
			currentRect.setSize(x - currentRect.x,
								y - currentRect.y);
			updateDrawableRect(getWidth(), getHeight());
			Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
			repaint(totalRepaint.x, totalRepaint.y,
					totalRepaint.width, totalRepaint.height);
		}
    }

	/**
	 * Invoked when user decides to copy some text to the clipboard.
	 */
	public void doCopy()
	{
	int	x1,y1,x2,y2;

		if	(null == lastMark)
			return;
		x1=lastMark.x; y1=lastMark.y;
		x2=x1+lastMark.width;y2=y1+lastMark.height;

	StringBuffer	sb = getParser().clipToMemory(y1>>4,y2>>4,x1>>3,x2>>3);
/*
      SecurityManager sm = System.getSecurityManager();
		if (sm != null)
		{
			try
			{
			   //sm.checkSystemClipboardAccess();
			}
			catch (Exception e) {e.printStackTrace();}
		}
*/
	Toolkit tk = Toolkit.getDefaultToolkit();
	Clipboard cp = tk.getSystemClipboard();

		cp.setContents(new java.awt.datatransfer.StringSelection(sb.toString()), this);
	}

    private void updateDrawableRect(int compWidth, int compHeight) {
        int x = currentRect.x;
        int y = currentRect.y;
        int width = currentRect.width;
        int height = currentRect.height;

        //Make the width and height positive, if necessary.
        if (width < 0) {
            width = 0 - width;
            x = x - width + 1; 
            if (x < 0) {
                width += x; 
                x = 0;
            }
        }
        if (height < 0) {
            height = 0 - height;
            y = y - height + 1; 
            if (y < 0) {
                height += y; 
                y = 0;
            }
        }

        //The rectangle shouldn't extend past the drawing area.
        if ((x + width) > compWidth) {
            width = compWidth - x;
        }
        if ((y + height) > compHeight) {
            height = compHeight - y;
        }
      
        //Update rectToDraw after saving old value.
        if (rectToDraw != null) {
            previousRectDrawn.setBounds(
                        rectToDraw.x, rectToDraw.y, 
                        rectToDraw.width, rectToDraw.height);
            rectToDraw.setBounds(x, y, width, height);
        } else {
            rectToDraw = new Rectangle(x, y, width, height);
        }
    }


	public void mouseDragged(MouseEvent e)
	{
		if	(isMarkingArea)
			updateSize(e);
	}

	public void mouseMoved(MouseEvent e)
	{
	}

	/**
	 * Override interface method.
	 *
	 * @see	MouseListener#mouseClicked
	 */
	public void mouseClicked(MouseEvent e)
	{
	}

	/**
	 * Override interface method.
	 *
	 * @see	MouseListener#mouseExited
	 */
	public void mouseExited(MouseEvent e)
	{
	}
	
	/**
	 * Override interface method.
	 *
	 * @see	MouseListener#mouseEntered
	 */
	public void mouseEntered(MouseEvent e)
	{
	}
	
	/**
	 * Override interface method.
	 *
	 * @see	MouseListener#mousePressed
	 */
	public void mousePressed(MouseEvent e)
	{
		// The user has pressed level one panel, set the focus to that so the
		// key inputs will be sent to novaNET.
		requestFocus();

	int	button = getButton(e);

		if	(button == 3 || (button == 1 && e.isShiftDown()))
		{
			isMarkingArea = true;
			int x = e.getX();
			int y = e.getY();
			currentRect = new Rectangle(x, y, 0, 0);
			updateDrawableRect(getWidth(), getHeight());
			repaint();
		}
		else if	(button == 1)
		{
		// Pass the click off to the protocol interpreter.
			level_one_parser.TouchProcess(
				e.getClickCount(),
				e.getX(),
				e.getY(),
				0);
		}
	}
	
	/**
	 * Override interface method.
	 *
	 * @see	MouseListener#mouseReleased
	 */
	public void mouseReleased(MouseEvent e)
	{
		if	(getButton(e) == 3)
		{
			isMarkingArea = false;
			updateSize(e);
		}
	}

	/**
	 * JDK 1.3 + 1.4 safe way to determine which
	 * button was pressed.
	 */
	static public int getButton(MouseEvent e)
	{
	int	ret = 0;

		try
		{
		// this method/constants are 1.4+
			if	(e.getButton() == MouseEvent.BUTTON1)
				ret = 1;
			else if (e.getButton() == MouseEvent.BUTTON3)
				ret = 3;
		}
		catch (java.lang.NoSuchMethodError e1)
		{
			if	((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0)
				ret = 1;
			if	((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0)
				ret = 3;
		}

		return ret;
	}

	/**
	 * Create an appropriate protocol interpreter. We have two protocol
	 * interpreters. The advanced protocol interpreter will take advantage
	 * of Graphics2D feature. The basic protocol interpreter will work on
	 * any version of java environment.
	 */
	private void createProtocolInterpreter()
	{
	// First create an object for doing JMF operations
	JMFInterface	jmf_player = null;

		try
		{
		// Grab the constructor we need through reflection.
		Class<?>	cls = Class.forName("com.nn.osiris.ui.JMFImplementer");

			jmf_player = (JMFInterface)	cls.getDeclaredConstructor().newInstance();

			if	(PortalConsts.is_debugging)	System.out.println("Loaded Java Media Framework");
		}
		catch (NoClassDefFoundError e1)
		{
		}
		catch (Exception e2)
		{
		}

		if	(null == jmf_player)
		{
			if	(PortalConsts.is_debugging)	System.out.println("Warning: No Java Media Framework");
			jmf_player = new JMFInterface();
		}

	// Second create an object for doing QuickTime operations
	QuickTimeInterface	quicktime_player = null;

		if	(PortalConsts.is_quicktime)
		{
		// decide what to go for based on quicktime.app.view.QTFactory class existance
		String	our_qt = "com.nn.osiris.ui.QuickTimeImplementer";

			try
			{
			Class<?>	cls = Class.forName("quicktime.app.view.QTFactory");
	
			}
			catch (NoClassDefFoundError e1)
			{
				our_qt = "com.nn.osiris.ui.QuickTimeImplementerOld";
			}
			catch (Exception e2)
			{
				our_qt = "com.nn.osiris.ui.QuickTimeImplementerOld";
			}

			if	(PortalConsts.is_debugging)	System.out.println("Quicktime via class: "+our_qt);

			try
			{
			// Grab the constructor we need through reflection.
			Class<?>	cls = Class.forName(our_qt);

				quicktime_player = (QuickTimeInterface)
					cls.getDeclaredConstructor().newInstance();

				if	(PortalConsts.is_debugging)	System.out.println("Loaded Quicktime Framework");
			}
			catch (NoClassDefFoundError e1)
			{
				System.out.println("doh"+e1);
				e1.printStackTrace();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}

		if	(null == quicktime_player)
		{
			if	(PortalConsts.is_debugging)	System.out.println("Warning: No Quicktime Framework");
			quicktime_player = new QuickTimeInterface();
		}

		// If java version is not 1.1x, we assume it is at least version
		// 1.2 and above. We can use more advanced protocol interpreter
		// that supports Graphics2D.
		if (!java_version.startsWith("1.1"))
		{
			try
			{
/* -- old code without reflection
				level_one_parser = new LevelOneParser2(
					parent_frame,
					this,
					this.getGraphics(),
					this.getGraphics(),
					offscreen,
					offscreen.getGraphics(),
					level_one_network,
					panel_width);
*/
				// Setup parameter class list to get the advanced
				// protocol interpreter constructor we need. Using
				// reflection because some platforms may not support
				// Graphics2D, such as mac os.
				Class[] parameter_classes = {Frame.class, Container.class,
					Graphics.class, Graphics.class, Image.class,
					Graphics.class, LevelOneNetwork.class, int.class};

				// Grab the constructor we need through inflection.
				Constructor<?> parser_constructor = Class.forName(
					"com.nn.osiris.ui.LevelOneParser2").getConstructor(
						parameter_classes);

				Object[] arguments = {parent_frame, this, 
					this.getGraphics(), this.getGraphics(), offscreen,
					offscreen.getGraphics(), level_one_network,
					Integer.valueOf(panel_width)};
				
				level_one_parser = (LevelOneParser)
					parser_constructor.newInstance(arguments);

				if	(PortalConsts.is_debugging)	System.out.println("Using advanced protocol interpreter!");
			}
			catch (ClassNotFoundException exception)
			{
				if	(PortalConsts.is_debugging)	System.out.println("advanced protocol interpreter class not found!");
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}				

		// If we still don't have a level one parser, we need to get one.
		if (null == level_one_parser)
		{
			level_one_parser = new LevelOneParser(
				parent_frame,
				this,
				this.getGraphics(),
				this.getGraphics(),
				offscreen,
				offscreen.getGraphics(),
				level_one_network,
				panel_width);

			if	(PortalConsts.is_debugging)	System.out.println("Using basic protocol interpreter.");
		}

		jmf_player.setEngine(level_one_parser);
		quicktime_player.setEngine(level_one_parser);
		level_one_parser.setJMFPlayer(jmf_player);
		level_one_parser.setQuickTimePlayer(quicktime_player);

	// create an object to do printing for the portal
	PrintInterface	pi;

		if	(PortalConsts.is_macintosh)
			pi = new PrintInterfaceMac();
		else
			pi = new PrintInterface();

		pi.setEngine(level_one_parser);
		level_one_parser.setPrintInterface(pi);
	}

	/**
	 * Key event is a legal novaNET key.
	 *
	 * @return	true	A legal novaNET key.
	 *			false	Not a legal novaNET key.
	 */
	boolean isLegalNovanetKey(KeyEvent event)
	{
		// alt/meta are used for menu/system functions
		// note that the macintosh command key is META
		if	(event.isAltDown() || event.isMetaDown())
			return false;
		// control tab for flipping tabs
		else if (KeyEvent.VK_TAB == event.getKeyCode() && event.isControlDown())
		{
			if	(PortalConsts.is_debugging)	System.out.println("flip tab focus");
			if (null != parent_frame && parent_frame instanceof PortalFrame)
				((PortalFrame)parent_frame).controlTab();
			return false;
		}
		// these were used for connection hotkeys with > 1 connection
		else if (KeyEvent.VK_0 <= event.getKeyCode() &&
				KeyEvent.VK_9 >= event.getKeyCode() &&
				0 != event.getModifiersEx() &&
			 	!event.isShiftDown())
		{
			return false;
		}
		else
			return true;
/*
		return !(event.isAltDown() ||
			(KeyEvent.VK_TAB == event.getKeyCode() && event.isControlDown()) ||
			(KeyEvent.VK_0 <= event.getKeyCode() &&
				KeyEvent.VK_9 >= event.getKeyCode() &&
				0 != event.getModifiers() &&
			 	!event.isShiftDown()));
*/
	}
}
