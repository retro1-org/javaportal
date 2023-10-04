/**
 * PROJECT		Portal panel
 *				(c) copyright 1999
 *				NCS NovaNET Learning
 *			
 * @author		J Hegarty
 */

package com.nn.osiris.ui;

//~~ This file is big.  It contains roughly converted C++ code, that is ok, but
//~~ it should be explained here in the file header comment.
//~~ Many of the methods in this file do not have descriptions of their 
//~~ parameters in their header comment.



import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Font.*;
import java.io.*;
import java.util.*;



/**
 * Level one protocol parser. Decodes the level one terminal protocol.
 */
public class LevelOneParser implements java.awt.event.ActionListener
{
	/**
	 * Constructor.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	LevelOneParser(
		Frame the_frame,
		Container a,
		Graphics bg,
		Graphics g1,
		Image i,
		Graphics g2,
		LevelOneNetwork n,
		int width)
	{
		parent_frame = the_frame;
  		levelone_container = a;
  		levelone_base_graphics = bg;
  		levelone_graphics = g1;
  		levelone_offscreen_image = i;
  		levelone_offscreen = g2;
  		levelone_network = n;
		
		if	(PortalConsts.is_applet)
		{
			original_dir = "";
			current_dir = "";
			disable_file_operations = true;
		}
		else
		{
		String	home_dir;

			original_dir = System.getProperty("user.dir");
			current_dir = original_dir;
			home_dir = System.getProperty("user.home");
			tmp_dir = System.getProperty("java.io.tmpdir");

		File	test1 = new File(home_dir,"Downloads");

			if	(test1.isDirectory())
				download_dir = test1.getAbsolutePath();
			else
			{
				test1 = new File(home_dir,"Download");
				if	(test1.isDirectory())
					download_dir = test1.getAbsolutePath();
				else
					download_dir = home_dir;
			}
		}

		if	(PortalConsts.is_debugging)	System.out.println("cwd: "+current_dir+" dl: "+download_dir);
		
  		image_slots = new Image[IMAGE_SLOTS];
	
   		trap_slots = new ByteArrayOutputStream[TRAP_SLOTS];
  		m2m3 = new byte[384*CHARHEIGHT];
  		cmd_state = 0;
  		top_state = 0;
  		coord_num[0] = 0;
  		coord_cnt[0] = 0;
  		post_action_i[0] = -1;
  		data_num[0] = 0;
  		stack_pointer = 0;
  		data_pnt = 0;
  		data_proc = TTYDataMode;
		
  		font_height = 16;
		
  		int stype = 116;
  		
  		
  		initEngine(
  		   width,	// Terminal width.
  		   512,		// Terminal height.
  		   stype,	// Terminal subtype.
  		   1,		// Color flag.
  		   14);		// Colors supported
  	}

	/**
	 * Constructor for copied protocol engines (used by -trap- machinery).
	 *
	 * @param
	 */
	LevelOneParser(LevelOneParser base)
	{
		parent_frame = base.parent_frame;
		levelone_container = base.levelone_container;
		levelone_base_graphics = base.levelone_base_graphics;
		levelone_graphics = base.levelone_graphics;
		levelone_offscreen_image = base.levelone_offscreen_image;
		levelone_offscreen = base.levelone_offscreen;
		levelone_network = null;
		
		is_signed_on = true;
		playback = true;
		local_fnt = base.local_fnt;
		local_font = base.local_font;
		local_font_metrics = base.local_font_metrics;
		local_font_descent = base.local_font_descent;
		font_height = base.font_height;
		disable_file_operations = true;
		original_dir = base.original_dir;
		current_dir = base.current_dir;
		localdata_dir = base.localdata_dir;
		image_slots = base.image_slots;
		jmf_player = base.jmf_player;
		quicktime_player = base.quicktime_player;
		clip_rect = base.clip_rect;
		sys_clip_rect = base.sys_clip_rect;
		clip_text = base.clip_text;

		is_quick_text_on = false;
		quick_text_length = 0;
		
		cmd_state = base.cmd_state;
		top_state = base.top_state;
		coord_num[0] = base.coord_num[0];
		coord_cnt[0] = base.coord_cnt[0];
		data_num[0] = base.data_num[0];
		post_action_i[0] = base.post_action_i[0];
		post_action_j[0] = base.post_action_j[0];
		stack_pointer = 0;
		data_pnt = 0;
		data_proc = L1PPDataMode;
		
		current_x = base.current_x;
		current_y = base.current_y;
		text_charset = 0;
		text_dir = base.text_dir;
		text_axis = base.text_axis;
		text_size = base.text_size;
		text_margin = base.text_margin;
		mem_addr = base.mem_addr;
		sys_x = base.sys_x;
		sys_y = base.sys_y;
		fg_color = base.fg_color;
		bg_color = base.bg_color;
		m2m3 = base.m2m3;
		is_word_coord_mode = base.is_word_coord_mode;
		do_palette_hold = base.do_palette_hold;
		cmd_pending = false;
		mem_conv = 0;
		load_x = base.load_x;
		load_y = base.load_y;
		old_x = base.old_x;
		old_y = base.old_y;
		seq_coord = base.seq_coord;
		text_style = base.text_style;
		colorAvail = base.colorAvail;
		wrap_x = base.wrap_x;
		wrap_y = base.wrap_y;
		style_pattern = base.style_pattern;
		style_thickness = base.style_thickness;
		style_cap = base.style_cap;
		style_dash = base.style_dash;
		style_join = base.style_join;
		style_fill = base.style_fill;
		do_rule_override = true;
		text_right = base.text_right;
		terminalWidth = base.terminalWidth;
		terminalHeight = base.terminalHeight;
//		terminalColors = base.terminalColors;
	}

	/**
	 * Get lstatus for the session.
	 *
	 * @return	The lstatus for the session.
	 */
	public long getLstatus()
	{
		return lstatus;
	}

	/**
	 * Set lstatus for the session.
	 *
	 * @param	The lstatus to set to.
	 */
	public void setLstatus(long lstatus)
	{
		this.lstatus = lstatus;
	}

	/**
	 * Get ldone for the session.
	 *
	 * @return	The ldone for the session.
	 */
	public int getLdone()
	{
		return ldone;
	}

	/**
	 * Set ldone for the session.
	 *
	 * @param	The ldone to set to.
	 */
	public void setLdone(int ldone)
	{
		this.ldone = ldone;
	}

	/**
	 * Get lscore for the session.
	 *
	 * @return	The lscore for the session.
	 */
	public int getLscore()
	{
		return lscore;
	}

	/**
	 * Set lscore for the session.
	 *
	 * @param	The lscore to set to.
	 */
	public void setLscore(int lscore)
	{
		this.lscore = lscore;
	}

	/**
	 * Get restart lesson name for the session.
	 *
	 * @return	The restart lesson name.
	 */
	public long getRstartl()
	{
		return rstartl;
	}

	/**
	 * Set restart lesson name for the session.
	 *
	 * @param	The restart lesson name to set to.
	 */
	public void setRstartl(long rstartl)
	{
		this.rstartl = rstartl;
	}

	/**
	 * Get restart unit name for the session.
	 *
	 * @return	The restart unit name.
	 */
	public long getRstartu()
	{
		return rstartu;
	}

	/**
	 * Set restart unit name for the session.
	 *
	 * @param	The restart unit name to set to.
	 */
	public void setRstartu(long rstartu)
	{
		this.rstartu = rstartu;
	}

	/**
	 * Get student variables for the session.
	 *
	 * @return	The student variables for the session.
	 */
	public long[] getStudentVariables()
	{
		return student_variables;
	}

	/**
	 * Set student variables for the session.
	 *
	 * @param	The student variables to set to.
	 */
	public void setStudentVariables(long[] student_variables)
	{
		this.student_variables = student_variables;
	}
	
	/**
	 * Set meta resource process ID for the session. Meta resource process ID
	 * needs to be set before starting a session when it is running under
	 * NGN context.
	 *
	 * @param	process_id	Meta resource process ID for the session.
	 */
	public void setProcessID(String process_id)
	{
		this.process_id = process_id;
	}

	/** Draw directly to container. */
	protected static final boolean is_direct_draw = false;
	/** The dimensions of the novanet system font. */
	private static final int CHARWIDTH = 8;
	private static final int CHARHEIGHT = 16;
	/** The number of image slots. */
	private static final int IMAGE_SLOTS = 255;
	/** The number of trap slots. */
	protected static final int TRAP_SLOTS = 48;

	/** No trap in progress trap state constant. */
	private static final int TRNONE = 0;
	/** Trapping to a slot trap state constant. */
	private static final int TRACTIVE = 1;
	/** Error during trip constant. */
	private static final int TRFAILED = 2;
	
	/** Plotting modes. */
	private static final int SCWRITE = 0;
	private static final int SCERASE = 1;
	private static final int SCREWRITE = 2;
	private static final int SCINVERSE = 3;
	private static final int SCXOR = 4;
	/** Plotting axis. */
	private static final int HORIZONTAL = 0;
	private static final int VERTICAL = 1;
	/** Plotting direction. */
	private static final int FORWARD = 0;
	private static final int REVERSE = 1;

	/** Patterns & dash styles. */
	protected static final int system_patterns [] =
	{
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// Pattern 0
		0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	// Pattern 1
		0xee, 0xbb, 0xee, 0xbb, 0xee, 0xbb, 0xee, 0xbb,	// Pattern 2..
		0xaa, 0x55, 0xaa, 0x55, 0xaa, 0x55, 0xaa, 0x55,
		0x22, 0x00, 0x88, 0x00, 0x22, 0x00, 0x88, 0x00,
		0x88, 0x22, 0x88, 0x22, 0x88, 0x22, 0x88, 0x22,
		0x08, 0x00, 0x80, 0x00, 0x08, 0x00, 0x80, 0x00,
		0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x80, 0x00,
		0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01,
		0xe0, 0x70, 0x38, 0x1c, 0x0e, 0x07, 0x83, 0xc1,
		0x88, 0x44, 0x22, 0x00, 0x88, 0x44, 0x22, 0x00,
		0x44, 0x22, 0x11, 0x08, 0x44, 0x22, 0x11, 0x88,
		0x3e, 0x1f, 0x8f, 0xc7, 0xe3, 0xf1, 0xf8, 0x7c,
		0x10, 0x20, 0x40, 0x80, 0x01, 0x02, 0x04, 0x08,
		0x1c, 0x38, 0x70, 0xe0, 0xc1, 0x83, 0x07, 0x0e,
		0x22, 0x44, 0x88, 0x00, 0x22, 0x44, 0x88, 0x00,
		0x11, 0x22, 0x44, 0x88, 0x11, 0x22, 0x44, 0x88,
		0x7c, 0xf8, 0xf1, 0xe3, 0xc7, 0x8f, 0x1f, 0x3e,
		0xe0, 0x00, 0x00, 0x00, 0x0e, 0x00, 0x00, 0x00,
		0xff, 0x00, 0x00, 0x00, 0xff, 0x00, 0x00, 0x00,
		0x80, 0x80, 0x80, 0x08, 0x08, 0x08, 0x00, 0x00,
		0x88, 0x88, 0x88, 0x88, 0x88, 0x88, 0x88, 0x88,
		0xff, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
		0xff, 0x88, 0x88, 0x88, 0xff, 0x88, 0x88, 0x88,
		0x24, 0x18, 0x18, 0x24, 0x42, 0x81, 0x81, 0x42,
		0x44, 0xaa, 0x11, 0xaa, 0x44, 0xaa, 0x11, 0xaa,
		0x08, 0x08, 0xff, 0x80, 0x80, 0x80, 0xff, 0x08,
		0x88, 0x88, 0x8f, 0x88, 0x88, 0x88, 0xf8, 0x88,
		0x80, 0xe3, 0x14, 0x08, 0x08, 0x3e, 0x41, 0x80,
		0x04, 0x02, 0x00, 0x20, 0x40, 0x80, 0x00, 0x08,
		0x04, 0x02, 0x01, 0x81, 0x42, 0x24, 0x18, 0x08,
		0xa0, 0x40, 0x00, 0x00, 0x0a, 0x04, 0x00, 0x00	// Pattern 31
	};

	protected boolean do_repaint;

	protected int[] user_patterns = new int[32*8];

	protected float[][] user_dashes = new float[8][];
	protected static final float[][] system_dashes =
	{
		{32000, 0},	// Dash 0 (solid line)
		{64, 12},	// Dash 1
		{32, 16},
		{16, 16},
		{8, 8},
		{4, 4},
		{2, 2},
		{32, 8, 8, 8}	// Dash 7
	};

	/** The lstatus for the session. */
	private long lstatus;
	/** The lscore for the session. */
	private int lscore;
	/** The ldone for the session. */
	private int ldone;
	/** The restart lesson name for the session. */
	private long rstartl;
	/** The restart unit name for the session. */
	private long rstartu;
	/** The student variables for the session. */
	private long[] student_variables;	

	/** The frame contains the parser. */
	Frame parent_frame;
	/** The container object. */
	Container levelone_container;
	/** Graphics object we draw to. For drawing text to screen (unclipped). */
	private Graphics levelone_base_graphics;
	/** Graphics object we draw to. For drawing text to screen (clipped). */
	protected Graphics levelone_graphics;
	/** The backing store for the graphics object we draw to. */
	private Image levelone_offscreen_image;
	protected Graphics levelone_offscreen;
	/** The network interface. */
	private LevelOneNetwork levelone_network;
	/** True to use resource server. */
	boolean using_resource_server;
	/** Resource server to use during this session. */
	private String rs_host;
	/** Meta resource process ID for the session. It has the form of Mxxxx-x. */
	private String process_id;

	/** Kermit server handle. */
	private LevelOneKermit kermit_object;
	/** Dialog informing user of kermit. */
	private KermitDialog kermit_dialog;
	/** Is signed onto system. */
	private boolean is_signed_on;
	/** Playback of data stream. */
	private boolean playback;
	/** End parsing. */
	private boolean end_parse;

	/** Local font family index. */
	private int local_ff;
	/** Local font size. */
	private int local_fs;
	/** Local font bold. */
	private boolean	local_fb;
	/** Local font italic. */
	private boolean	local_fi;

	/** FNT local font. */
	private WindowsFNT local_fnt;
	/** Java local font. */
	private Font local_font;
	/** Metrics of java local font. */
	private FontMetrics local_font_metrics;
	/** Descent of local font. */
	private int local_font_descent;
	/** Height of current font. */
	private int font_height;

	/** Observer that ignores everything. */
	private ImageObserver nil_observer = null;

	/** prevent local file operations. */
	private boolean disable_file_operations;

	/** Original current working directory (-local os-). */
	private String original_dir;
	/** Current working directory (-local os-). */
	private String current_dir;
	/** OS temporary directory */
	private String tmp_dir;
	private String download_dir;
	/** Saved current directory from IAC switch */
	private String iac_saved_dir;
	/** Local executed proc, if one */
	Process child_process;
	/** Error rate for -local os- */
	private double local_os_failure_rate = 0.0;
	/** Local data directory (-local os-). */
	private String localdata_dir;
	/** Command string parse point (-local os-). */
	private int local_os_cmdp;
	/** Options (-local os-). */
	private int local_os_options;
	/** Entries for local os */
	private int local_os_entries;
	/** Entry size (cyber words) for local os */
	private int local_os_entrysize;
	/** File we are spinning for kermit upload */
	private FileOutputStream local_os_fos;

	/** Last image get/copy/load result (-image-). */
	int imerror;
	/** Handles of memory-based images (-image-). */
	private Image[]	image_slots;
	/** Used for quick picture display (-image- quicktime). */
	private Image image_info_image;
	private String image_info_filename;

	/** -sound- variables. */
	/** URL that will be played (-sound-). */
	private String sound_url;
	/** File that will be played (-sound-). */
	private File sound_file;
	/** JMF player used to play sound (-sound-). */
	JMFInterface	jmf_player;
	/** Quicktime player used to play movies (-image-) stills (-image), & sound (-sound-). */
	QuickTimeInterface	quicktime_player;


	/** -print- variables */
	PrintInterface	print_interface;


	/** Sequence in Test mode data. */
	private int testseq;
	/** Passes made. */
	private int testpass;

	/** Mouse cursor style. */
	private int mouse_cursor_style;

	/** Cursor is active. */		
	private boolean	cursor_on;
	/** Cursor is flashing. */
	private boolean	cursor_flashing;
	/** Style of the cursor. */
	private int cursor_style;
	/** X location of cursor last displayed. */
	private int cursor_x;
	/** Y location of cursor last displayed. */
	private int cursor_y;

	/** Clipping information. */
	private Rectangle clip_rect;
	private Rectangle sys_clip_rect;

	/** Auto signon is on. */
	private boolean	autosignon;
	/** Auto signon name. */
	private String autoname;
	/** Auto signon group. */
	private String autogroup;
	/** Auto signon password. */
	private String autopassword;
	/** Auto signon lesson */
	private String auto_lesson;
	private String auto_unit;
	/** Lesson arguments (as they are) for the auto lesson. */
	private long[] auto_lesson_arguments;
	/** Auto signon bypass codeword */
	private String auto_bypass;

	/** Text contents of screen, for clipboard copy */
	private byte[] clip_text = new byte[128*32];	// 32 lines up to 128 chars wide

	/** Quick text plotting accumulation is on. */
	private boolean is_quick_text_on;
	/** Quick text plotting data. */
	static final int QUICK_LENGTH = 64;
	private byte[] quick_text_data = new byte[QUICK_LENGTH];
	/** Quick text plotting length (bytes). */
	private int quick_text_length;
	/** X coordinate of quick text plotting start. */
	private int quick_text_x;
	/** Y coordinate of quick text plotting start. */
	private int quick_text_y;

	/** trap-associated variables. */
	/** Current trap buffer number. */
	private int trap_buffer;
	/** Last trap result. */
	private int trap_status;
	/** Do display while trapping. */
	private boolean	do_display_while_trap;
	/** Trap slots. */
	protected ByteArrayOutputStream[] trap_slots;

	/*
	 * Level one protocol state variables.
	 */
	/** Current command data. */
	private byte[] data = new byte[1500];
	/** Command parse state. */
	private int cmd_state;
	/** Top protocol mode. */
	private int top_state;
	/** Coorindates expected. */
	private int[] coord_num = new int[3];
	/** Coordinates received. */
	private int[] coord_cnt = new int[3];
	/** Date byte expected. */
	private int[] data_num = new int[3];
	/** High level post-data fns. */
	private int[] post_action_i = new int[3];
	/** Low level post-data fns. */
	private int[] post_action_j = new int[3];
	/** Protocol stack pointer. */
	private int stack_pointer;
	/** Index into data array. */
	private int data_pnt;
	/** Highest protocol interpreter (protocolP function index). */
	private int data_proc;

	/*
	 * Level one graphics state.
	 */
	/** Current screen mode of terminal. */
	protected int screen_mode;
	/** Current x location. */
	private int current_x;
	/** Current y location. */
	private int current_y;
	/** Current charset working in. */
	private byte text_charset;
	/** Text plotting direction. */
	private byte text_dir;
	/** Text plotting axis. */
	private byte text_axis;
	/** Text size. */
	private byte text_size;
	/** Text margin. */
	private int text_margin;
	/** Memory address. */
	private int mem_addr;
	/** Terminal subtype. */
	private int sub_type;
	/** X coordinate for centering. */
	protected int center_x;
	/** Y coordinate for centering. */
	protected int center_y;
	/** System sent centering. */
	private int sys_x;
	/** System sent centering. */
	private int sys_y;
	/** Touch is enabled. */
	private boolean	is_touch_enabled;
	/** Foreground color. */
	private Color fg_color;
	/** Background color. */
	private Color bg_color;

	/*
	 * For putting together charsets.
	 */
	private int[] build_char = new int[8];
	/** Converted bitmap. */
	private byte[] char_bitmap = new byte[CHARHEIGHT];
	/** Loaded characters. */
	private byte[] m2m3;
	/** Counter of words received. */
	private int build_count;
	/** Upline enhancements. */
	private boolean	up_enhance;
	/** In word coordinate mode. */
	private boolean	is_word_coord_mode;
	/** Do palette hold. */
	private boolean	do_palette_hold;

	/*
	 * Engine support variables.
	 */
	/** Escape pending flag. */
	private boolean	cmd_pending;
	/** Flow control is on. */
	private boolean	is_flow_control_on;
	/** Flow control is available. */
	private boolean	is_flow_control_avail;
	/** Char conversions for ldm. */
	private int mem_conv;

	/*
	 * Data Constuction Support variables.
	 */
	/** X coordinate of load. */
	private int load_x;
	/** Y coordinate of load. */
	private int load_y;
	/** Previous x coordinate. */
	private int old_x;
	/** Previous y coordinate. */
	private int old_y;
	/** Coordinate sequence. */
	private boolean	seq_coord;
	/** First coordinate in line mode flag. */
	private boolean	first_line;
	/** Reference to polygon. */
	protected java.awt.Polygon polygon;
	/** Lightweight signon key supression */
	private boolean lw_suppress;
	/** Lightweight signoff data */
	private long signout_data[] = new long[1+64+64+150];
	private int signout_data_class;
	private int signout_count;
	/** Note/term comment/dump data */
	private long buffered_data[];
	private int buffered_count;
	/** Text style bits. */
	private int text_style;
	/** Color is avaiable. */
	private boolean	colorAvail;
	/** Current max X coordinate. */
	private int wrap_x;
	/** Current max Y coordinate. */
	private int wrap_y;
	/** Pen pattern in effect. */
	protected int style_pattern;
	/** Pen thickness. */
	protected int style_thickness;
	/** Pen cap style. */
	protected int style_cap;
	/** Pen dash style. */
	protected int style_dash;
	/** Pen join style. */
	protected int style_join;
	/** Fill flag. */
	protected int style_fill;
	
	/** File name buffer. */
	StringBuffer filename = new StringBuffer(512);
	/** Filename sum check. */
	private int filename_sum_check;
	/** Inhibit output features. */
	private boolean	do_inhibit_output;
	/** Inhibit input features. */
	private boolean	do_inhibit_input;
	/** Trap pattern override kludge. */
	private boolean	do_rule_override;
	/** Device selected with xout. */
	private int ext_device;
	/** End of scroll line. */
	private int text_right;
	/** Text editing key mode. */
	private boolean	is_text_key_mode;
	/** Multi-click is enabled. */
	private boolean	is_multi_clickable;
	/** Cursor type. */
	private int cursor_type;
	/** Kermit metering. */
	private int kerm_delay;
	/** Width of virtual screen. */
	private int terminalWidth;
	/** Height of virtual screen. */
	private int terminalHeight;
	/** Colors of screen. */
	//private int terminalColors;

	/** Buffer for transmitting keys back to system. */
	private byte[] transmit_buffer = new byte[12];

	/** Define names of ASCII control characters. */
	private static final int NUL = 0;
	private static final int SOH = 1;
	private static final int STX = 2;
	private static final int ETX = 3;
	private static final int EOT = 4;
	private static final int ENQ = 5;
	private static final int ACK = 6;
	private static final int BEL = 7;
	private static final int BS = 8;
	private static final int HT = 9;
	private static final int LF = 10;
	private static final int VT = 11;
	private static final int FF = 12;
	private static final int CR = 13;
	private static final int SO = 14;
	private static final int SI = 15;
	private static final int DLE = 16;
	private static final int DC1 = 17;
	private static final int DC2 = 18;
	private static final int DC3 = 19;
	private static final int DC4 = 20;
	private static final int NAK = 21;
	private static final int SYN = 22;
	private static final int ETB = 23;
	private static final int CAN = 24;
	private static final int EM = 25;
	private static final int SUB = 26;
	private static final int ESC = 27;
	private static final int FS = 28;
	private static final int GS = 29;
	private static final int RS = 30;
	private static final int US = 31;
	private static final int SP = 32;

	private static final int XON = DC1;
	private static final int XOFF = DC3;

	private static final int TTYDataMode = 0;
	private static final int KermDataMode = 1;
	private static final int TrapDataMode = 2;
	private static final int TestDataMode = 3;
	private static final int L1PPDataMode = 4;

	/**
	 * Define states when a command is being send from the host. The names of
	 * the form DATAcd give the number of coordiates and databytes expected
	 * with this command. Starting with CMD are the intermediate states for the
	 * command sequences, with the attached character naming the character
	 * received after the ESC, if any.
	 */
	private static final int ICMD = 0;
	private static final int DATA20 = 1;
	private static final int DATA23 = 2;
	private static final int DATA10 = 3;
	private static final int DATA00 = 4;
	private static final int DATA01 = 5;
	private static final int DATA02 = 6;
	private static final int DATA03 = 7;
	private static final int DATA04 = 8;
	private static final int DATA06 = 9;
	private static final int DATA07 = 10;
	private static final int DATA09 = 11;
	private static final int DATA010 = 12;
	private static final int DATA012 = 13;
	private static final int DATA013 = 14;
	private static final int DATA015 = 15;
	private static final int DATA018 = 16;
	private static final int DATA024 = 17;
	private static final int DATA027 = 18;
	private static final int CMD = 19;
	private static final int CMDi = 20;
	private static final int CMDj = 21;
	private static final int CMDx = 22;
	private static final int CMDq = 23;
	private static final int CMDv = 24;
	private static final int CMDk = 25;
	private static final int CMDextra = 26;

	/** Names of the top level states for the level one protocol engiine. */
	private static final int sPoint = 0;
	private static final int sLine = 1;
	private static final int sLoadmem = 2;
	private static final int sAlpha = 3;
	private static final int sBlock = 4;
	private static final int sUser5 = 5;
	private static final int sUser6 = 6;
	private static final int sUser7 = 7;
	private static final int sEllipArc = 8;
	private static final int sCircArc = 9;
	private static final int sCircle = 10;
	private static final int sEllipse = 11;
	private static final int sBox = 12;
	private static final int sExt = 13;
	private static final int sLoadFile = 14;
	private static final int sLocalData = 15;
	private static final int sExt2 = 16;
	private static final int sLightWeight = 17;
	private static final int sBufferData = 18;

	/** Data formats for the top level L1 states. */
	private static final byte exTop[] =
	{
		DATA10, DATA10, DATA03, DATA01, DATA20,
		DATA03, DATA03, DATA03, DATA012, DATA09,
		DATA03, DATA06, DATA23, DATA03, DATA01,
		DATA03, DATA01, DATA010, DATA010
	};

	/**
	 * Top-level function to decode L1P.
	 *
	 * @param state Current top-level state of the L1P.
	 * @param c Current data byte.
	 */
	private final void protocolP(
		int state,
		int c)
	{
		switch (state)
		{
			case TTYDataMode:
				TTYData(c);
				break;
			case KermDataMode:
				KermData(c);
				break;
			case TrapDataMode:
				TrapData(c);
				break;
			case TestDataMode:
				TestData(c);
				break;
			case L1PPDataMode:
				L1PPData(c);
				break;
		}
	}

	/**
	 * Procedures for the top level protocol states.
	 *
	 * @param state Selects the function to invoke.
	 */
	private final void topP(int state)
	{
		switch (state)
		{
			case sPoint:
				PointData();
				break;
			case sLine:
				LineData();
				break;
			case sLoadmem:
				LoadMemData();
				break;
			case sAlpha:
				AlphaData();
				break;
			case sBlock:
				BlockData();
				break;
			case sUser5:
				UserData5();
				break;
			case sUser6:
				UserData6();
				break;
			case sUser7:
				UserData7();
				break;
			case sEllipArc:
				EllipseArcData();
				break;
			case sCircArc:
				CircArcData();
				break;
			case sCircle:
				CircleData();
				break;
			case sEllipse:
				EllipseData();
				break;
			case sBox:
				BoxData();
				break;
			case sExt:
				ExternalData();
				break;
			case sLoadFile:
				FileNameData();
				break;
			case sLocalData:
				LocalDataInit();
				break;
			case sExt2:
				ExtData();
				break;
			case sLightWeight:
				lightWeightData();
				break;
			case sBufferData:
				bufferData();
				break;
		}
	}

	/**
	 * Number of coordinates associated with each of the data pending states.
	 */
	private static final byte exCoords[] =
		{0, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	/**
	 * Number of data bytes associated with each of the data pending
	 * states.
	 */
	private static final byte exBytes[] =
		{0, 0, 3, 0, 0, 1, 2, 3, 4, 6, 7, 9, 10, 12, 13, 15, 18, 24, 27};

	/** Next state for CMD state by character. */
	private static final byte CMDs [] =
	{
		/*00*/ICMD, ICMD, DATA00, DATA00, ICMD, ICMD, ICMD, ICMD,
		/*08*/ICMD, ICMD, ICMD, ICMD, DATA00, ICMD, ICMD, ICMD, 
		/*10*/ICMD, DATA00, DATA00, DATA00, DATA00, ICMD, ICMD, ICMD, 
		/*18*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*20*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*28*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*30*/ICMD, ICMD, DATA10, ICMD, DATA00, DATA00, DATA00, DATA00, 
		/*38*/DATA03, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*40*/DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, 
		/*48*/DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, 
		/*50*/DATA00, DATA03, DATA03, DATA00, DATA00, DATA00, DATA00, DATA03, 
		/*58*/ICMD, DATA03, DATA00, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*60*/ICMD, DATA04, DATA04, DATA02, DATA00, DATA00, DATA00, DATA00, 
		/*68*/DATA00, CMDi, CMDj, CMDk, DATA02, DATA02, DATA06, ICMD, 
		/*70*/ICMD, CMDq, ICMD, DATA00, ICMD, ICMD, CMDv, ICMD, 
		/*78*/CMDx, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD
	};

	/** Next state for CMDi state by character, offset by 0x30. */
	private static final byte CMDis [] = 
	{
		/*30*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*38*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*40*/ICMD, DATA01, DATA03, DATA01, DATA01, DATA01, DATA01, DATA013, 
		/*48*/DATA02, ICMD, DATA03, DATA00, ICMD, ICMD, ICMD, ICMD, 
		/*50*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*58*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*60*/ICMD, DATA03, DATA03, DATA03, DATA03, DATA00, DATA00, DATA00, 
		/*68*/DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, 
		/*70*/DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, 
		/*78*/DATA00, DATA00, DATA00, ICMD, ICMD, ICMD, ICMD, ICMD
	};

	/** Next state for CMDj state by character, offset by 0x30. */
	private static final byte CMDjs [] = 
	{
		/*30*/ICMD, DATA00, DATA00, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*38*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*40*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*48*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*50*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*58*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*60*/ICMD, DATA015, DATA027, DATA03, DATA024, DATA03, DATA00, DATA03, 
		/*68*/DATA00, DATA00, DATA00, DATA09, ICMD, ICMD, ICMD, ICMD, 
		/*70*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*78*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD
	};

	/** Next state for CMDx state by character, offset by 0x30. */
	private static final byte CMDxs [] = 
	{
		/*30*/DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, 
		/*38*/DATA00, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*40*/ICMD, DATA00, DATA00, DATA03, DATA01, DATA01, DATA03, DATA01, 
		/*48*/DATA00, DATA00, ICMD, DATA00, DATA00, DATA00, DATA20, DATA00, 
		/*50*/DATA00, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*58*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*60*/DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA09, DATA00, 
		/*68*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*70*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*78*/DATA03, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, ICMD
	};

	/** Next state for CMDq state by character, offset by 0x30. */
	private static final byte CMDqs [] = 
	{
		/*30*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*38*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*40*/ICMD, DATA00, DATA00, DATA00, DATA00, ICMD, ICMD, ICMD, 
		/*48*/ICMD, ICMD, ICMD, DATA00, DATA012, DATA018, DATA03, DATA06, 
		/*50*/DATA06, DATA015, DATA015, DATA012, DATA03, DATA00, ICMD, ICMD, 
		/*58*/ICMD, ICMD, DATA00, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*60*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, DATA00, 
		/*68*/DATA00, DATA03, DATA015, DATA00, DATA03, DATA03, DATA09, DATA03, 
		/*70*/DATA04, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, DATA00, 
		/*78*/DATA03, DATA03, DATA20, DATA01, ICMD, ICMD, ICMD, ICMD
	};

	/** Next state for CMDk state by character, offset by 0x30. */
	private static final byte CMDks [] = 
	{
		/*30*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*38*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*40*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*48*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*50*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*58*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*60*/ICMD, DATA03, DATA03, DATA04, DATA04, DATA01, DATA00, DATA00, 
		/*68*/DATA00, DATA00, DATA00, DATA00, DATA00, ICMD, ICMD, ICMD, 
		/*70*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*78*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD
	};

	/** Next state for CMDv state by character, offset by 0x30. */
	private static final byte CMDvs [] = 
	{
		/*30*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*38*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*40*/ICMD, DATA00, DATA00, DATA012, DATA00, DATA00, DATA00, DATA00, 
		/*48*/DATA00, DATA03, DATA00, DATA00, DATA00, DATA00, DATA00, DATA06, 
		/*50*/DATA06, DATA00, DATA00, DATA00, DATA03, DATA00, DATA00, DATA00, 
		/*58*/DATA00, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*60*/ICMD, DATA06, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*68*/ICMD, ICMD, ICMD, ICMD, ICMD, DATA00, ICMD, ICMD, 
		/*70*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, 
		/*78*/ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD, ICMD
	};

	/** Array for indexing the above state tables. */
	private static final byte stateTables[][] = {
		CMDs, CMDis, CMDjs, CMDxs, CMDqs, CMDvs, CMDks};

	/**
	 * Action procedures for CMD state by character.
	 *
	 * @param selector Selects the function to invoke.
	 */
	private final void CMDp(int selector)
	{
		switch (selector)
		{
			case 2:
				selectPlatoMode();
				break;
			case 3:
				selectTTYMode();
				break;
			case 0x0c:
				clearScreen();
				break;
			case 0x11:
				selectInverseMode();
				break;
			case 0x12:
				selectWriteMode();
				break;
			case 0x13:
				selectEraseMode();
				break;
			case 0x14:
				selectRewriteMode();
				break;
			case 0x32:
				LoadCoord();
				break;
			case 0x34:
				selectWordCoordMode();
				break;
			case 0x35:
				StandardCoords();
				break;
			case 0x36:
				PaletteClear();
				break;
			case 0x37:
				StyleDefaults();
				break;
			case 0x38:
				TimedDelay();
				break;
			case 0x40:
				SuperScript();
				break;
			case 0x41:
				SubScript();
				break;
			case 0x42:
				selectM0();
				break;
			case 0x43:
				selectM1();
				break;
			case 0x44:
				selectM2();
				break;
			case 0x45:
				selectM3();
				break;
			case 0x46:
				selectM4();
				break;
			case 0x47:
				selectM5();
				break;
			case 0x48:
				selectM6();
				break;
			case 0x49:
				selectM7();
				break;
			case 0x4a:
				selectHoriz();
				break;
			case 0x4b:
				selectVert();
				break;
			case 0x4c:
				selectForward();
				break;
			case 0x4d:
				selectReverse();
				break;
			case 0x4e:
				selectSize0();
				break;
			case 0x4f:
				selectSize2();
				break;
			case 0x50:
				LoadMemConvert();
				break;
			case 0x51:
				SSFProc();
				break;
			case 0x52:
				ExtProc();
				break;
			case 0x53:
				LoadMem();
				break;
			case 0x54:
				Mode5();
				break;
			case 0x55:
				Mode6();
				break;
			case 0x56:
				Mode7();
				break;
			case 0x57:
				LoadMemAddr();
				break;
			case 0x59:
				LoadEcho();
				break;
			case 0x5a:
				setMargin();
				break;
			case 0x61:
//				if	(PortalConsts.is_debugging)
//					System.out.println("----------                                                          setForeColor()");
				setForeColor();
				break;
			case 0x62:
//				if	(PortalConsts.is_debugging)
//					System.out.println("----------                                                          setBackColor()");
				setBackColor();
				break;
			case 0x63:
				Paint();
				break;
			case 0x64:
				selectInverseMode();
				break;
			case 0x65:
				selectWriteMode();
				break;
			case 0x66:
				selectEraseMode();
				break;
			case 0x67:
				selectRewriteMode();
				break;
			case 0x68:
				selectExclusiveOrMode();
				break;
			case 0x6c:
				setForeSlot();
				break;
			case 0x6d:
				setBackSlot();
				break;
			case 0x6e:
				LoadSlot();
				break;
			case 0x73:
				LoadCharSlot();
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in cmdp tables: "+selector);
		}
	}

	/**
	 * Action procedures for CMDi state by character, offset by 0x30.
	 *
	 * @param selector Selects the function to invoke.
	 */
	private final void CMDip(int selector)
	{
		switch (selector)
		{
			case 0x41:
				SetPenPat();
				break;
			case 0x42:
				SetPenThick();
				break;
			case 0x43:
				SetPenCap();
				break;
			case 0x44:
				SetPenDash();
				break;
			case 0x45:
				SetPenJoin();
				break;
			case 0x46:
				SetPenFill();
				break;
			case 0x47:
				LoadUserPat();
				break;
			case 0x48:
				LoadUserDash();
				break;
			case 0x4a:
				SetMouseCursor();
				break;
			case 0x4b:
				CloseMsg();
				break;
			case 0x61:
				TextStyleOr();
				break;
			case 0x62:
				TextStyleXor();
				break;
			case 0x63:
				TextStyleSet();
				break;
			case 0x64:
				TextStyleAnd();
				break;
			case 0x65:
				TextStyleC1();
				break;
			case 0x66:
				TextStyleS1();
				break;
			case 0x67:
				TextStyleC2();
				break;
			case 0x68:
				TextStyleS2();
				break;
			case 0x69:
				TextStyleC3();
				break;
			case 0x6a:
				TextStyleS3();
				break;
			case 0x6b:
				TextStyleC4();
				break;
			case 0x6c:
				TextStyleS4();
				break;
			case 0x6d:
				TextStyleC5();
				break;
			case 0x6e:
				TextStyleS5();
				break;
			case 0x6f:
				TextStyleC6();
				break;
			case 0x70:
				TextStyleS6();
				break;
			case 0x71:
				TextStyleC7();
				break;
			case 0x72:
				TextStyleS7();
				break;
			case 0x73:
				TextStyleC8();
				break;
			case 0x74:
				TextStyleS8();
				break;
			case 0x75:
				TextStyleC9();
				break;
			case 0x76:
				TextStyleS9();
				break;
			case 0x77:
				TextStyleC10();
				break;
			case 0x78:
				TextStyleS10();
				break;
			case 0x79:
				TextStyleC11();
				break;
			case 0x7a:
				TextStyleS11();
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in cmdip tables: "+selector);
		}
	}
	
	/**
	 * Action procedures for CMDj state by character.
	 *
	 * @param selector Selects the function to invoke.
	 */
	private final void CMDjp(int selector)
	{
		switch (selector)
		{
			case 0x31:
				ImageSaveAll();
				break;
			case 0x32:
				ImageGetAll();
				break;
			case 0x61:
				ImageSave();
				break;
			case 0x62:
				ImageGet();
				break;
			case 0x63:
				ImageInfo();
				break;
			case 0x64:
				ImageCopy();
				break;
			case 0x65:
				ImageDelete();
				break;
			case 0x66:
				ImageEcho();
				break;
			case 0x67:
				ImageLoad();
				break;
			case 0x68:
				ImagePause();
				break;
			case 0x69:
				ImageResume();
				break;
			case 0x6a:
				ImageStop();
				break;
			case 0x6b:
	//			ImageDrag(); todo
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in cmdjp tables: "+selector);
		}
	}

	/**
	 * Action procedures for CMDk state by character.
	 *
	 * @param selector Selects the function to invoke.
	 */
	private final void CMDkp(int selector)
	{
		switch (selector)
		{
			case 0x61:
				SoundOne();
				break;
			case 0x62:
				SoundTwo();
				break;
			case 0x63:
				SoundThree();
				break;
			case 0x64:
				SoundFour();
				break;
			case 0x65:
				SoundFive();
				break;
			case 0x66:
				SoundSix();
				break;
			case 0x67:
				SoundSeven();
				break;
			case 0x68:
				SoundEight();
				break;
			case 0x69:
				SoundNine();
				break;
			case 0x6a:
				SoundTen();
				break;
			case 0x6b:
				SoundEleven();
				break;
			case 0x6c:
				SoundTwelve();
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in cmdkp tables: "+selector);
		}
	}

	/**
	 * Action procedures for CMDx state by character.
	 *
	 * @param selector Selects the function to invoke.
	 */
	private final void CMDxp(int selector)
	{
		switch (selector)
		{
			case 0x30:
				lightWeightBuffer();
				break;
			case 0x31:
				lightWeightNote();
				break;
			case 0x32:
				lightWeightComment();
				break;
			case 0x33:
				lightWeightDump();
				break;
			case 0x34: // -score-/*lscore*
			case 0x35: // -status-/*lstatus*
			case 0x36: // -lesson-/-end-/*ldone*
			case 0x37: // -restart-/*rstartl*/*rstartu*
				signout_data = new long[1+64];
				signout_data_class = selector;
				if (PortalConsts.is_debugging)
				{ System.out.println("lw data dump coming.."+Integer.toString(selector,16));
					if (selector == 0x34)
						System.out.println("score changed");
					else if (selector == 0x35)
						System.out.println("status changed");
					else if (selector == 0x36)
						System.out.println("ldone changed");
					else
						System.out.println("restart done");
				}
				break;
			case 0x38: // signout
				signout_data = new long[1+64+64+150];
				signout_data_class = selector;
				if (PortalConsts.is_debugging) System.out.println("signout data coming.."+Integer.toString(selector,16));
				break;
			case 0x41:
				CursorOn();
				break;
			case 0x42:
				CursorOff();
				break;
			case 0x43:
				SetTextMargin();
				break;
			case 0x44:
				ScrollDelete();
				break;
			case 0x45:
				ScrollInsert();
				break;
			case 0x46:
				Scroll();
				break;
			case 0x47:
				SetCursorStyle();
				break;
			case 0x48:
				TextFlashOn();
				break;
			case 0x49:
				TextFlashOff();
				break;
			case 0x4b:
				TextEditKeys();
				break;
			case 0x4c:
				TextEditCursor();
				break;
			case 0x4d:
				TextEditClear();
				break;
			case 0x4e:
				TextEditWindow();
				break;
			case 0x4f:
				MltClkDisable();
				break;
			case 0x50:
				MltClkEnable();
				break;
			case 0x60:
				LocalPrint();
				break;
			case 0x61:
				LocalExecute();
				break;
			case 0x62:
				InhibitOutput();
				break;
			case 0x63:
				AllowOutput();
				break;
			case 0x64:
				InhibitInput();
				break;
			case 0x65:
				AllowInput();
				break;
			case 0x66:
				LocalDataInit();
				break;
			case 0x67:
				LocalCompute();
				break;
			case 0x78:
				SetUplineRate();
				break;
			case 0x79:
				KermitExit();
				break;
			case 0x7a:
				KermitInit();
				break;
			case 0x7b:
				TestInit();
				break;
			case 0x7c:
				AutoLogin();
				break;
			case 0x7d:
				lightWeightLogin();
				break;
			case 0x7e:
				lightWeightLogout();
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in cmdxp tables: "+selector);
		}
	}

	/**
	 * Action procedures for CMDq state by character.
	 *
	 * @param selector Selects the function to invoke.
	 */
	private final void CMDqp(int selector)
	{
		switch (selector)
		{
			case 0x41:
				EllipticalArcInit();
				break;
			case 0x42:
				CircularArcInit();
				break;
			case 0x43:
				CircleInit();
				break;
			case 0x44:
				EllipseInit();
				break;
			case 0x4b:
				BoxInit();
				break;
			case 0x4c:
				StretchEndline();
				break;
			case 0x4d:
				StretchMidline();
				break;
			case 0x4e:
				StretchCircle();
				break;
			case 0x4f:
				StretchEllipseX();
				break;
			case 0x50:
				StretchEllipseY();
				break;
			case 0x51:
				StretchBox();
				break;
			case 0x52:
				StretchThick();
				break;
			case 0x53:
				StretchFill();
				break;
			case 0x54:
				FontSelect();
				break;
			case 0x55:
				FontChecksum();
				break;
			case 0x5a:
				StretchStp();
				break;
			case 0x67:
				ExtMode();
				break;
			case 0x68:
				TrapDragStop();
				break;
			case 0x69:
				FilenameSumChk();
				break;
			case 0x6a:
				TrapDrag();
				break;
			case 0x6b:
				TrapTst();
				break;
			case 0x6c:
				TrapWrite();
				break;
			case 0x6d:
				TrapRead();
				break;
			case 0x6e:
				TrapDisplay();
				break;
			case 0x6f:
				TrapDelete();
				break;
			case 0x70:
				TrapSave();
				break;
			case 0x71:
				TrapEnd();
				break;
			case 0x72:
				FilenameClear();
				break;
			case 0x73:
				FilenameLoad();
				break;
			case 0x74:
				AsyncAlert();
				break;
			case 0x75:
				ExtdataInit();
				break;
			case 0x76:
				TerminalReport();
				break;
			case 0x77:
				ExtensionSelect();
				break;
			case 0x78:
				SetXOffset();
				break;
			case 0x79:
				SetYOffset();
				break;
			case 0x7a:
				SetClipping();
				break;
			case 0x7b:
	//			ExtensionSelect2(); todo
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in cmdqp tables: "+selector);
		}
	}

	/**
	 * Action procedures for CMDv state by character.
	 *
	 * @param selector Selects the function to invoke.
	 */
	private final void CMDvp(int selector)
	{
		switch (selector)
		{
			case 0x41:
				printScreen();
				break;
			case 0x42:
				PrintFile();
				break;
			case 0x43:
				CopyFile();
				break;
			case 0x44:
				POpenDoc();
				break;
			case 0x45:
				POpenPage();
				break;
			case 0x46:
				PClosePage();
				break;
			case 0x47:
				PCloseDoc();
				break;
			case 0x48:
				PTextFont();
				break;
			case 0x49:
				PTextSize();
				break;
			case 0x4a:
				PDrawText();
				break;
			case 0x4b:
				PTextWidth();
				break;
			case 0x4c:
				PFontAscent();
				break;
			case 0x4d:
				PFontDescent();
				break;
			case 0x4e:
				PFontLeading();
				break;
			case 0x4f:
				PMoveTo();
				break;
			case 0x50:
				PLineTo();
				break;
			case 0x51:
				PPage();
				break;
			case 0x52:
				PPageResolution();
				break;
			case 0x53:
				PGetPen();
				break;
			case 0x54:
				PTextStyle();
				break;
			case 0x55:
				PError();
				break;
			case 0x56:
				PCancel();
				break;
			case 0x57:
				TermInfo2();
				break;
			case 0x58:
				PCheck();
				break;
			case 0x61:
				LocalOS();
				break;
			case 0x6d:
				LocalFTP();
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in cmdvp tables: "+selector);
		}
	}

	/**
	 * Array for indexing action procedures.
	 *
//~~ Parameter descriptions please.	 
	 * @param
	 * @param
	 */
	private final void procTables(
		int i, 
		int j)
	{
		switch (i)
		{
			case 0:
				CMDp(j);
				break;
			case 1:
				CMDip(j);
				break;
			case 2:
				CMDjp(j);
				break;
			case 3:
				CMDxp(j);
				break;
			case 4:
				CMDqp(j);
				break;
			case 5:
				CMDvp(j);
				break;
			case 6:
				CMDkp(j);
				break;
			case 7:	// commands with data-dependent data length (user dash)
				switch (j)
				{
					case 0:
						LoadedUserDash();
						break;
				}
				break;
			case -1:		// used to ignore commands
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("Error in proctables: "+i);
		}
	}

	/**
	 * Set the level one network for the parser.
	 *
	 * @param	network	The network to set to.
	 */
	public void setNetwork(LevelOneNetwork network)
	{
		levelone_network = network;
	}

	/**
	 * Set the user signon and group for the auto signon novaNET session.
	 *
	 * @param	name		The signon name.
	 * @param	group		The signon group.
	 * @param	lesson		The lesson name to run.
	 * @param	unit		The unit name to run.
	 * @param	arguments	The arguments (as they are) for the lesson to run.
	 * @param	bypass		The bypass codeword from the router.
	 */
	public void setUser(
		String name,
		String group,
		String lesson,
		String unit,
		long[] arguments,
		String bypass)
	{
		autoname = name;
		autogroup = group;
		auto_lesson = lesson;
		auto_unit = unit;
		auto_lesson_arguments = arguments;
		auto_bypass = bypass;

		// If it is for NGN and has group or name specified, send to signal
		// want to be able to go into the router directly.
		if (null != lesson && (group != null || name != null))
		{
			if	(PortalConsts.is_debugging) System.out.println("NGN USM 126 startup");
			SendUsm(126);
		}

		// If scorm object has data, use it
		else if	(JPortal.global_jportal.scorminterface.getLesson() != null)
		{
		String	suspend_data;
		long[]	suspend_longs=null;
		String	arg1,arg2;

			autogroup = "scorm";
			autoname = "student";
			auto_lesson = JPortal.global_jportal.scorminterface.getLesson();
			suspend_data = JPortal.global_jportal.scorminterface.getSuspendData();
			if	(null != suspend_data)
				suspend_longs = stringToCybers(suspend_data);
// don't support unit jumpouts
//			auto_unit = null
			auto_bypass = null;
			arg1 = JPortal.global_jportal.scorminterface.getArg1();
			arg2 = JPortal.global_jportal.scorminterface.getArg2();
			if (arg2 != null && arg1 != null)
			{
				auto_lesson_arguments = new long[2];
				auto_lesson_arguments[0] = Long.parseLong(arg1,8) & 077777777777777777777L;
				auto_lesson_arguments[1] = Long.parseLong(arg2,8) & 077777777777777777777L;
			}
			else if (arg1 != null)
			{
				auto_lesson_arguments = new long[1];
				auto_lesson_arguments[0] = Long.parseLong(arg1,8) & 077777777777777777777L;
			}
			else
				auto_lesson_arguments = null;
			if	(suspend_longs != null && suspend_longs.length >= 3)
			{
				lstatus = suspend_longs[0];
				rstartl = suspend_longs[1];
				rstartu = suspend_longs[2];
				if	(suspend_longs.length >= 153)
				{
					if	(null == student_variables)
						student_variables = new long[150];
					for (int i=0;i<150;i++)
						student_variables[i] = suspend_longs[3+i];
				}
			}
			SendUsm(126);
		}

		if	(name != null && name.length() > 0)
			autosignon = true;
	}

	/**
	 * Sends in appropriate touch notification; called by mouse event handler.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	public final void TouchProcess(
		int clicks,
		int x,
		int y,
		int mods)
	{
		if (is_touch_enabled)
		{
			y = unxlatY(y);
			x = unxlatX(x);
			
			x -= sys_x;
			y -= sys_y;
			
			if (x >= 0 && y >= 0 && x <= wrap_x && y <= wrap_y)
			{
				if (up_enhance)
				{
					if (is_multi_clickable)
						SendPixRes(clicks,x,y,mods);
					else
						SendPixRes(1,x,y,mods);
				}
				else
					SendTouch(x>>5,y>>5);
			}
		}
	}

	/**
	 * Plot a trap buffer.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	protected void trapPlot(
		int buffer,
		int offx,
		int offy,
		int mode)
	{
		if (buffer >= 0 && buffer < TRAP_SLOTS)
		{
			if (null != trap_slots[buffer])
			{
				LevelOneParser	trapengine = new LevelOneParser(this);
				byte[] 	trap_buffer;
				
				trapengine.center_x = center_x+offx;
				trapengine.center_y = center_y+offy;
				trapengine.screen_mode = mode;
				
				trap_buffer = trap_slots[buffer].toByteArray();
				trapengine.ParseStream(trap_buffer,0,trap_buffer.length);
			}
		}
	}

	/**
	 * Called when user presses shift-stop.
	 */
	private final void ShiftStop()
	{
		// stop any in progress kermit transfer
		KermitExit();
		// end any trap in progress
		TrapEndInternal();
		// cancel any in progress print operation
		PCancel();
		// kill any playing sound
		SoundSix();
		// kill any quicktime activity
		quicktime_player.dispose();
		// reset directory
		current_dir = original_dir;
		// reset mouse cursor
		mouse_cursor_style = 0;
	}
	
	/**
	 * Initializes protocol engine for a port.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	private void initEngine(
		int width,
		int height,
		int sub,
		int colorflag,
		int ncolors)
	{
		is_flow_control_avail = true;
		terminalWidth = (width > 511 ? width : 512);
		terminalHeight = (height > 511 ? height : 512);
		sub_type = sub;
		colorAvail = true;
//		terminalColors = 0;
		initTTY();
	}
	
	int m_pending = 0;
	
	/**
	 * Process data through protocol engine.
	 *
	 * @param
	 * @param
	 * @param
	 */
	public final int ParseStream(
		byte[] buffer,
		int offset,
		int length)
	{
	int i;
	int c;
	int b; 
	boolean old_cursor = cursor_on;

		do_repaint = false;
		end_parse = false;

		for (i = 0; i < length;)
		{
			b = buffer[offset + (i++)] & 0xff;
			c = b & 0x7f;
		
			// the following is needed only for Cybis/Cyber1.
			// it is based on the following code from PTerm:
			/*
			 * 
int PtermHostConnection::AssembleAsciiWord (void)
{
    int i;
    
    for (;;)
    {
        i = dtReado (m_fet);
        if (i == -1)
        {
            if (dtConnected (m_fet))
            {
                return C_NODATA;
            }
            m_connActive = false;
            dtClose (m_fet, TRUE);
            m_fet = NULL;
            return C_DISCONNECT;
        }
        else if (m_pending == 0 && i == 0377)
        {
            // 0377 is used by Telnet to introduce commands (IAC).
            // We recognize only IAC IAC for now.
            // Note that the check has to be made before the sign
            // bit is stripped off.
            m_pending = 0377;
            continue;
        }

        i &= 0177;
        if (i == 033)
        {
            m_pending = 033;
            continue;
        }
        if (m_pending == 033)
        {
            m_pending = 0;
            return (033 << 8) + i;
        }
        else
        {
            m_pending = 0;
            if (i == 0)
            {
                // NUL is for -delay-
                i = 1 << 19;
            }
            return i;
        }
    }
}
			 * 
			 */
			
			if (sub_type == 16)  // needed only for Cybis/Cyber1.
			{
				if (m_pending == 0 && b == 0xff)
				{
					m_pending = 0xff;
					continue;
				}
		        if (c == 0x1b)
		        {
		            m_pending = 0x1b;
		            continue;
		        }				
		        if (m_pending == 0x1b)
		        {
			        protocolP(data_proc, 0x1b);
		            m_pending = 0;
		        }			
		        else
		        {
		        	 m_pending = 0;
		        }
		        protocolP(data_proc,c);
			}
			else
			{
				protocolP(data_proc,c);
			}
			
			// The protocol interpreter can set the end parse flag
			// when it wants to stop process traffic.  It usually
			// does this for operations that are expensive, to avoid
			// keeping events from getting processed for a long time.
			if (end_parse && !playback)
				break;
		}

		FlushText();
		
		MonitorMultiMedia();

		if	(old_cursor != cursor_on || cursor_on)
			repaintCursor();
		
		if (do_repaint)
		{
			levelone_container.repaint();
		}
		
		return i;
	}
	
	/**
	 * Protocol interpreter for TTY mode operation.
	 *
	 * @param
	 */ 
	private final void TTYData(int c)
	{
		if ( cmd_pending && ESC != c)
		{
			if ( c == STX && 0 == data_pnt)
			{
				cmd_pending = false;
				data_proc = L1PPDataMode;
				initPlatoMode();
			}
			else
			{
				if (data_pnt < data.length)
					data[data_pnt++] =(byte) c;
				else
					cmd_pending = false;
			}
		}
		else
		{
			if ( c < 0x20)
			{
				FlushText();
				switch ( c)
				{
					case BEL:
						Beep();
						break;
					case BS:
						if (current_x >= 8)
							current_x -= 8;
						break;
					case HT:
						current_x += 8;
						if (current_x > wrap_x)
							current_x = wrap_x - 7;
						break;
					case LF:
						XYAdjust( 0, -CHARHEIGHT);
						break;
					case VT:
						current_y += 16;
						break;
					case CR:
						current_x = 0;
						break;
					case ESC:
						cmd_pending = true;
						data_pnt = 0;
						break;
				}
			}
			else
			{
				if (is_quick_text_on)
				{
					quick_text_data[quick_text_length++] = (byte) c;
					if (quick_text_length == quick_text_data.length)
						is_quick_text_on = false;
					XYAdjust(8,0);
				}
				else
				{
					FlushText();
					data[0] = (byte) c;
					AlphaData();
				}
			}
		}
	}

	/**
	 * Protocol interpreter for Kermit mode operation.
	 *
	 * @param
	 */
	private final void KermData(int c)
	{
		if ( c == SOH || c == ESC)
		{
			data_pnt = 1;
			data[0] = (byte) c;
		}
		else
		{
			if (data_pnt < data.length)
				data[data_pnt++] = (byte) c;
			
			// check for ESC x y abort sequence
			if (data[0] == ESC &&
				data[1] == 'x' &&
				data[2] == 'y' &&
				data_pnt > 2)
			{
				KermitExit();
			}
			// check for ESC ETX enter TTY mode command
			else if ( data[0] == ESC && data[1] == ETX && data_pnt > 2)
			{
				KermitExit();
				selectTTYMode();
			}
			// pass completed packets to kermit server
			else if ( c == CR && data[0] == SOH)
			{
				if (null != kermit_object)
				{
					if (kermit_object.processPacket(data,data_pnt))
						KermitExit();
				}
			}
		}
	}

	/**
	 * Protocol interpreter while trap save is in effect.
	 *
	 * @param
	 */
	
	private final void TrapData(int c)
	{
		// if trap buffer is deleted, return to normal processing
		if (null == trap_slots[trap_buffer])
		{
			trap_status = TRNONE;
			data_proc = L1PPDataMode;
		}
		// save data only up to the first error
		if (TRACTIVE == trap_status)
			trap_slots[trap_buffer].write(c);

		// if displaying trap data, pass to engine
		if (do_display_while_trap)
			L1PPData( c);
		// otherwise need to check for trap end here
		else
		{
			if (c == 'q' && data[0] == 'q' && data[1] == ESC)
				TrapEnd();
			else
			{
				data[1] = data[0];
				data[0] = (byte) c;
			}
		}
	}

	/**
	 * Protocol interpreter while data test mode is in effect.
	 *
	 * @param
	 */
	void TestData(int c)
	{
		if (ESC == c)
		{
			data_proc = L1PPDataMode;
			L1PPData(c);
		}
		else if (c != testseq)
		{
			if	(PortalConsts.is_debugging)	System.out.println("test mode error; pass="+testpass);
			SendUsm(1);
			testseq = c;
		}

		testseq = (c == 127 ? 32 : c+1);
		if (32 == testseq)
		{
			testpass++;
			if	(PortalConsts.is_debugging)	System.out.println("test mode ok; pass="+testpass);
		}
	}
	
	/**
	 * Protocol interpreter for L1 mode operation.
	 *
	 * @param
	 */
	private final void L1PPData(int c)
	{
		int i;
		int postCmdFni;
		int postCmdFnj;
		
	
		
		//if	(PortalConsts.is_debugging)	System.out.println("Data="+ " 0x" + String.format("%x", c) + " : " + c + " : " + (char)c);
		
		if (c >= 0x20 && is_quick_text_on)
		{
			quick_text_data[quick_text_length++] = (byte) c;
			XYAdjust( CHARWIDTH, 0);
			
			if (quick_text_length == quick_text_data.length)
				FlushText();

			return;
		}
		else
		{
			is_quick_text_on = false;
			if (quick_text_length > 0)
				FlushText();
		}

		// parse commands
		if (cmd_pending)
		{
			if (c == ESC)
				cmd_state = CMD;
			else
			{
				i = c;
				if (cmd_state != CMD)
					i -= 0x30;
				// if char lies outside table, cancel command
				if (i < 0 || c > 0x7f)
				{
					post_action_i[stack_pointer] = -1;
					cmd_pending = false;
					return;
				}
				postCmdFni = cmd_state-CMD;
				postCmdFnj = c;
				
				cmd_state = stateTables[cmd_state-CMD][i];
				
				if (cmd_state == ICMD)
				{
					post_action_i[stack_pointer] = -1;
					cmd_pending = false;
					return;
				}
				// If done with command interpretation, clear flag and setup
				// data wait.
				if (cmd_state < CMD)
				{
					cmd_pending = false;
					InitData(cmd_state);
					post_action_i[stack_pointer] = postCmdFni;
					post_action_j[stack_pointer] = postCmdFnj;
				}
			}
		}
		// Process control characters.
		else if (c < 0x20)
		{
			if (top_state == sExt2 && (c == LF || c == CR || c == FF))
			{
				data[0] = (byte) c;
				ExtData();
			}
			else
			{
				switch (c)
				{
					case BS:
						XYAdjust(-CHARWIDTH, 0);
						break;
					case HT:
						XYAdjust(CHARWIDTH, 0);
						break;
					case LF:
						XYAdjust(0, -font_height);
						break;
					case VT:
						XYAdjust(0, font_height);
						break;
					case FF:
						FormFeed();
						break;
					case CR:
						CReturn();
						break;
					case EM:
						InitTop (sBlock);
						break;
					case ESC:
						// don't allow stack overflow
						if (stack_pointer >= 2)
							stack_pointer = 0;
						stack_pointer++;
						cmd_pending = true;
						cmd_state = CMD;
						break;
					case FS:
						InitTop (sPoint);
						break;
					case GS:
						first_line = true;
						InitTop (sLine);
						break;
					case US:
						InitTop (sAlpha);
						break;
				}
			}
		}
		// Process data for command.
		else if (coord_cnt[stack_pointer] < coord_num[stack_pointer])
		{
			if ( is_word_coord_mode)
			// word coords
			{
				data[data_pnt++] = (byte) c;
				if (data_pnt == 6)
				{
					old_x = load_x;
					old_y = load_y;
					load_x = ExtractWord (0);
					load_y = ExtractWord (3);
					coord_cnt[stack_pointer]++;
					data_pnt = 0;
				}
			}
			// old style coords
			else
			{
				switch ( (c >> 5) & 3)
				{
					case 0x01:
		  				if ( seq_coord)
							load_x = ( load_x & 0x1f) | ( (c & 0x1f) << 5);
						else
						{
							load_y = ( load_y & 0x1f) | ( (c & 0x1f) << 5);
							seq_coord = true;
						}
						break;
					case 0x02:
						load_x = ( load_x & 0x03e0) | (c & 0x1f);
						seq_coord = false;
						coord_cnt[stack_pointer]++;
						if (coord_cnt[stack_pointer] < coord_num[stack_pointer])
						{
							old_x = load_x;
							old_y = load_y;
						}
						break;
					case 0x03:
						load_y = ( load_y & 0x03e0) | (c & 0x1f);
						seq_coord = true;
				}
			}
		}
		else if (data_num[stack_pointer] != 0)
		{
			if (data_pnt > 99)
				return;

			data[data_pnt++] = (byte) c;
			data_num[stack_pointer]--;
		}

		if (cmd_pending)
			return;

		if (coord_num[stack_pointer] == coord_cnt[stack_pointer] &&
			data_num[stack_pointer] == 0)
		{
			// pop the stack if command completed
			if (stack_pointer > 0)
			{
				stack_pointer--;
				data_pnt = 0;
				procTables (post_action_i[stack_pointer+1],post_action_j[stack_pointer+1]);
			}
			else
			{
				InitData (exTop[top_state]);
				topP (top_state);
			}
		}
	}

	/**
	 * Initialize new top level L1 mode.
	 *
	 * @param
	 */
	private final void InitTop (int topmode)
	{
		checkPoly();
		stack_pointer = 0;
		top_state = topmode;
		InitData (exTop[top_state]);
	}

	/**
	 * Initialize for L1 data accumulation.
	 *
	 * @param
	 */
	private final void InitData (int datamode)
	{
		coord_num[stack_pointer] = exCoords[datamode];
		data_num[stack_pointer] = exBytes[datamode];
		coord_cnt[stack_pointer] = 0;
		seq_coord = false;
		data_pnt = 0;
	}

	/**
	 * Extract a data word from the data array.
	 *
	 * @param
	 */
	private final int ExtractWord (int firstbyte)
	{
		int work = (data[firstbyte] & 0x3f) |
			((data[firstbyte+1] & 0x3f) << 6) |
			((data[firstbyte+2] & 0x3f) << 12);
		
		// if the sign bit is set, sign-extend:
		if ((work & 0400000) != 0)
			work |= 0xfffc0000;
		
		return work;
	}

	/**
	 * Extract a data word from the data array.
	 *
	 * @param
	 */
	private final int ExtractLWord (int firstbyte)
	{
		return (data[firstbyte] & 0x3f) |
			((data[firstbyte+1] & 0x3f) << 6) |
			((data[firstbyte+2] & 0x3f) << 12);
	}

	/**
	 * Extract a long from the data array.
	 *
	 * @return	The long from the data array.
	 */
	private final long extractLong()
	{
		long result;

		result = ((long) data[0] & 0x3f);
		result |= ((long) data[1] & 0x3f) << 6;
		result |= ((long) data[2] & 0x3f) << 12;
		result |= ((long) data[3] & 0x3f) << 18;
		result |= ((long) data[4] & 0x3f) << 24;
		result |= ((long) data[5] & 0x3f) << 30;
		result |= ((long) data[6] & 0x3f) << 36;
		result |= ((long) data[7] & 0x3f) << 42;
		result |= ((long) data[8] & 0x3f) << 48;
		result |= ((long) data[9] & 0x3f) << 54;

		return result;
	}

	/**
	 * Extract red color component.
	 *
	 * @param
	 */
	
	private final int ExtractRed (int firstbyte)
	{
		return ((data[firstbyte+3] & 0x3f) << 2) |
			((data[firstbyte+2] & 0x30) >> 4);
	}
	
	/**
	 * Extract green color component.
	 *
	 * @param
	 */
	private final int ExtractGreen (int firstbyte)
	{
		return ((data[firstbyte+2] & 0x0f) << 4) |
			((data[firstbyte+1] & 0x3c) >> 2);
	}

	/**
	 * Extract blue color component.
	 * 
	 * @param
	 */
	private final int ExtractBlue (int firstbyte)
	{
		return ((data[firstbyte+1] & 0x03) << 6) |
			(data[firstbyte] & 0x3f);
	}

	/**
	 * Extract slot.
	 *
	 * @param
	 */
	private final int ExtractSlot (int firstbyte)
	{
		return ((data[firstbyte+1] & 0x3f) << 6) |
			(data[firstbyte] & 0x3f);
	}

	/**
	 * Adjust the current x and y.
	 *
	 * @param
	 * @param
	 */
	private final void XYAdjust (
		int deltaX,
		int deltaY)
	{
		if (text_size != 0)
		{
			deltaX += deltaX;
			deltaY += deltaY;
		}

		switch ((text_axis<<1) + text_dir)
		{
			case 0x00:
				current_x += deltaX;
				current_y += deltaY;
				break;
			case 0x01:
				current_x -= deltaX;
				current_y += deltaY;
				break;
			case 0x02:
				current_y += deltaX;
				current_x += deltaY;
				break;
			case 0x03:
				current_y -= deltaX;  
				current_x += deltaY;
				break;
		}

		if (current_x < 0)
		{
			is_quick_text_on = false;
			current_x += wrap_x+1;
		} 
		else if (current_x > wrap_x)
		{
			is_quick_text_on = false;
			if (data_proc != TTYDataMode)
				current_x -= wrap_x+1;
		}
	
		if (current_y < 0)
		{
			is_quick_text_on = false;
			current_y += wrap_y+1;
		}
		else if (current_y > wrap_y)
		{
			is_quick_text_on = false;
			current_y -= wrap_y+1;
		}
	}

	/**
	 * Set current_x/current_y to top of page.
	 */
	private final void FormFeed()
	{
		int sizeX = CHARWIDTH;
		int sizeY = CHARHEIGHT;
		
		if (text_size != 0)
		{
			sizeX <<= 1;
			sizeY <<= 1;
		}
		
		switch ( (text_axis<<1) + text_dir)
		{
			case 0x00:
				current_x = 0;
				current_y = 496;
				break;
			case 0x01:
				current_x = wrap_x + 1 - sizeX;
				current_y = 496;
				break;
			case 0x02:
				current_y = 0;
				current_x = sizeY - 1;
				break;
			case 0x03:
				current_y = wrap_y + 1 - sizeX;
				current_x = sizeY - 1;
				break;
		}
	}

	/**
	 * Set current_x/current_y to margin on next line.
	 */
	private final void CReturn()
	{
		int size = font_height;
		
		if (text_size != 0)
			size = 32;
		
		switch ((text_axis<<1) + text_dir)
		{
			case 0x00: 
			case 0x01:
				current_x = text_margin;
				current_y -= size;
				break;
			case 0x02:
			case 0x03:
				current_y = text_margin;
				current_x += size;
				break;
		}
	}

	/**
	 * send *value* back to plato with usm bits set.
	 *
	 * @param
	 */
	final void SendUsm (int value)
	{
		// if sending a terminal reset in, do local reinitialization as well
		if (2 == value)
		{
			KermitExit();
			initTTY();
			InitTop(sAlpha);
			data_proc = L1PPDataMode;
			is_signed_on = true;
			initPlatoMode();
		}
		if	(126 == value)
		{
			lw_suppress = true;
			if	(PortalConsts.is_debugging)	System.out.println("requesting LW login");
		}

		SendEsc ( (value & 0x7f) + 0x0380);
	}

	/**
	 * Random routine to set a bit for char conversion.
	 *
	 * @param
	 * @param
	 */ 
	private final void SetBit ( int byteNum, int bitNum)
	{
		int bitVal;
		
		byteNum = 15 - byteNum;
		bitNum = 7 - bitNum;
		bitVal = ( 1 << bitNum);
		char_bitmap [ byteNum ] |= (bitVal & 0xff);
	}
	
	/**
	 * Send course grid touch back to plato.
	 *
	 * @param
	 * @param
	 */
	private final void SendTouch ( int touchX, int touchY)
	{
	   SendEsc ( (touchY & 0x0f) + ((touchX & 0x0f) << 4) + 0x0100);
	}

	/**
	 *
	 * SendExt()
	 *
	 * send external key back to plato 
	 *
	 */

	private final void SendExt ( int value)
	{
		SendEsc ( (value & 0xff) | 0x0200);
	}

	/**
	 *
	 * SendEcho()
	 *
	 * send *value* back to plato with echo bits set 
	 *
	 */
	final void SendEcho ( int value)
	{
		SendEsc ( (value & 0x7f) | 0x0080);
	}

	/**
	 *
	 * SendEsc()
	 *
	 * send *value* back to plato packaged as an escape sequence
	 *
	 */

	private final void SendEsc ( int value)
	{
		if (playback)
			return;

		transmit_buffer[0] = 0x1b;
		transmit_buffer[1] = (byte) ((value & 0x3f) | 0x40);
		transmit_buffer[2] = (byte) (((value & 0x3c0) >> 6) | 0x60);

		if	(null != levelone_network)
			levelone_network.write (transmit_buffer,0, 3);
	}

	/**
	 *
	 * SendPixRes ( clicks, xLoc, yLoc)
	 *
	 * (xLoc, yLoc) back as a high-res touch key
	 *
	 */
	final void SendPixRes ( int clicks, int xLoc, int yLoc,int mods)
	{
		if (playback)
			return;

		transmit_buffer[0] = 0x1b;
		transmit_buffer[1] = 0x1e;
		transmit_buffer[2] = (byte) (0x60 | (clicks & 3) | ((mods & 7) << 2));

		transmit_buffer[3] = (byte) (0x40 | (xLoc & 0x3f));
		transmit_buffer[4] = (byte) (0x40 | ((xLoc >> 6) & 0x3f));
		transmit_buffer[5] = (byte) (0x40 | ((xLoc >> 12) & 0x3f));

		transmit_buffer[6] = (byte) (0x40 | (yLoc & 0x3f));
		transmit_buffer[7] = (byte) (0x40 | ((yLoc >> 6) & 0x3f));
		transmit_buffer[8] = (byte) (0x40 | ((yLoc >> 12) & 0x3f));

		if	(null != levelone_network)
			levelone_network.write (transmit_buffer,0, 9);
	}

	/**
	 *
	 * Sends in 60 bit data key.
	 *
	 */

	private final void SendDataKey(long data_value)
	{
		if	(playback)
			return;

		transmit_buffer[0] = 0x1b;
		transmit_buffer[1] = 0x2c;
		transmit_buffer[2] = (byte) (0x40 | (data_value & 0x3f));
		transmit_buffer[3] = (byte) (0x40 | ((data_value >> 6) & 0x3f));
		transmit_buffer[4] = (byte) (0x40 | ((data_value >> 12) & 0x3f));
		transmit_buffer[5] = (byte) (0x40 | ((data_value >> 18) & 0x3f));
		transmit_buffer[6] = (byte) (0x40 | ((data_value >> 24) & 0x3f));
		transmit_buffer[7] = (byte) (0x40 | ((data_value >> 30) & 0x3f));
		transmit_buffer[8] = (byte) (0x40 | ((data_value >> 36) & 0x3f));
		transmit_buffer[9] = (byte) (0x40 | ((data_value >> 42) & 0x3f));
		transmit_buffer[10] = (byte) (0x40 | ((data_value >> 48) & 0x3f));
		transmit_buffer[11] = (byte) (0x40 | ((data_value >> 54) & 0x3f));

		if	(null != levelone_network)
			levelone_network.write (transmit_buffer,0, 12,0);
	}

	/**
	 * Delete the passed trap buffer.
	 *
	 * @param
	 */
	private final void DeleteTrapBuffer (int num)
	{
		trap_slots[num] = null;                    
	}

	/**
	 * Flip a character from plato format to a decent bitmap format
	 */
	private final void CharFlip()
	{
	int i, wordCount, bitCount, tempWord;
		
		for ( i = 0; i < 16; i ++)
			char_bitmap [ i ] = 0;
		
		for ( wordCount = 0; wordCount < 8; wordCount ++)
		{
			tempWord = build_char [ wordCount ];
			for ( bitCount = 0; bitCount < 16; bitCount ++)
			{
				if ((tempWord & 0x01) != 0)
					SetBit ( bitCount, wordCount);
				tempWord = tempWord >> 1;
			}
		}
	}

	/**
	 * Selects PLATO mode operation.
	 */
//~~ Why does this funciton do nothing, yet exist?	 
	private final void selectPlatoMode()
	{
	}

	/**
	 * Selects TTY mode operation, which changes the top level protocol routine
	 * and does other state initializations.
	 */
	 
	private final void selectTTYMode()
	{
		PCancel();
		KermitExit();
		if (null != parent_frame && parent_frame instanceof PortalFrame)
			((PortalFrame) parent_frame).signOut((LevelOnePanel)levelone_container);
		is_signed_on = false;
		initTTY();
	}

	/**
	 * initialize TTY mode.
	 */ 
	private void initTTY()
	{
		int i;

		initTTYMode();

		imerror = 0;
		is_quick_text_on = false;
		quick_text_length = 0;
		trap_status = 0;

		data_pnt = 0;
		data_proc = TTYDataMode;
	 
		screen_mode = SCREWRITE;
		current_x = 0;
		current_y = 480;
		text_charset = 0;
		text_dir = FORWARD;
		text_axis = HORIZONTAL;
		text_size = 0;
		text_margin = 0;
		mem_addr = 0;
		is_touch_enabled = false;

		if (colorAvail)
		{
			fg_color = Color.white;
			bg_color = Color.black;
		}
		else
		{
			fg_color = Color.black;
			bg_color = Color.white;
		}

		build_count = 0;

		up_enhance = false;
		is_word_coord_mode = false;
		do_palette_hold = false;

		cmd_pending = false;

		is_flow_control_on = false;
		mem_conv = 0;

		text_style = 0;
		ClearStyles();

		do_inhibit_output = false;
		do_inhibit_input = false;
		do_rule_override = false;
		ext_device = 0;
		text_right = wrap_x;
		is_text_key_mode = false;
		is_multi_clickable = false;
	 	cursor_type = 0;
		kerm_delay = 0;
		cmd_pending = false;
		build_count = 0;

		for (i = 0; i < TRAP_SLOTS; i++)
			DeleteTrapBuffer (i);

		ClipSet (sys_x, sys_y, wrap_x+sys_x, wrap_y+sys_y, true);
	}


	/**
	 *
	 * Clear clipboard view of screen to default.
	 *
	 */
	void clipClearScreen()
	{
		for (int i=0; i < 32*(terminalWidth>>3); i++)
			clip_text[i] = ' ';
	}

	void clipPlotBlock(int x1,int y1,int x2,int y2)
	{
	int	line1,line2;
	int	col1,col2;
	int	i,j;

		if	(Math.min(x1,x2) < 0 || Math.min(y1,y2) < 0 || 
			Math.max(x1,x2) >= terminalWidth || Math.max(y1,y2) >= terminalHeight)
		{
			return;
		}

		line1 = 31 - (Math.max(y1,y2) >> 4);
		line2 = 31 - (Math.min(y1,y2) >> 4);

		col1 = Math.min(x1,x2) >> 3;
		col2 = Math.max(x1,x2) >> 3;

		for (i=line1; i<=line2; i++)
			for (j=col1; j<=col2; j++)
				clip_text[i*(terminalWidth>>3)+j] = ' ';
	}

	void clipPlotChar(int data,int x1,int y1)
	{
	int		row,col;
	int		index;

		if	(x1 < 0 || x1 >= terminalWidth || y1 < 0 || y1 >= terminalHeight)
			return;

		col = x1 >> 3;
		row = 31 - (y1 >> 4);

		index = col + row*(terminalWidth>>3);
		if	(!(data == '_' && clip_text[index] != ' '))
			clip_text[index] = (byte) data;
	}

	/**
	 * Renders plotted strings on the clipboard 'image' of the screen.
	 */
	void clipFlushText()
	{
	int		x,y,dx,value,i;

		if	(text_size != 0)
			dx = 16;
		else
			dx = 8;

		x = quick_text_x+center_x;
		y = quick_text_y+center_y;

		for (i=0; i<quick_text_length; i++)
		{
			value = quick_text_data[i];

			if	(text_charset == 1)
				switch(value)
				{
					case 0x21:
						value = '/';
						break;
					case 0x23:
						value = '~';
						break;
					case 0x4a:
						value = '|';
						break;
				}

			if	(screen_mode == SCERASE || screen_mode == SCINVERSE) 
				clipPlotChar(' ',x,y);
			else
				clipPlotChar(value,x,y);

			x += dx;
		}
	}

	/**
	 * Used by the copy code to fetch data from the clipboard 'image'
	 * and transfer it to the system clipboard.
	 */
	public StringBuffer clipToMemory(int sline,int eline,int scol,int ecol)
	{
	StringBuffer	out = new StringBuffer();
	int				lchar,i,line;

		eline = Math.min(eline,31);
		ecol = Math.min(ecol,(terminalWidth>>3)-1);

		if	(sline < 0 || scol < 0 || sline > eline || scol > ecol)
			return out;

		for (line=sline; line <= eline; line++)
		{
			lchar = -1 + scol;
			// determine where the line ends
			for (i=scol; i<=ecol; i++)
				if	(clip_text[i+line*(terminalWidth>>3)] != ' ')
					lchar = i;
			// now copy it to the stringbuffer
			for (i=scol; i<=lchar; i++)
				out.append((char) clip_text[i+line*(terminalWidth>>3)]);

			out.append('\n');
		}

		return out;
	}

	/**
	 * Clear window to background color and do required state changes.
	 */
	final void clearScreen()
	{
		clipClearScreen();
		is_text_key_mode = false;
		text_style = 0;
		is_multi_clickable = false;
		cursor_on = cursor_flashing = false;
		DropLocalFont();
		quicktime_player.dispose();
		checkPoly();
		ClearStyles();
		modeClipColor(false,false,false);
		((LevelOnePanel)levelone_container).clearMark();
		LocalOSWatchReset();

		if (is_direct_draw)
		{
			levelone_graphics.setClip(
				xlatX(0),
				xlatY(511),
				xlatX(terminalWidth),
				xlatY(0)+1);
			levelone_graphics.setColor (bg_color);
			levelone_graphics.fillRect(
				xlatX(0),
				xlatY(511),
				xlatX(terminalWidth),
				xlatY(0)+1);
		}
		else
			do_repaint = true;

		levelone_offscreen.setClip
		(
			xlatX(0),xlatY(511),xlatX(terminalWidth),xlatY(0)+1
		);


		levelone_offscreen.setColor (bg_color);
		levelone_offscreen.fillRect
		(
			xlatX(0),xlatY(511),xlatX(terminalWidth),xlatY(0)+1
		);
	}

	/**
	 * Select mode inverse plotting.
	 */
	private final void selectInverseMode()
	{
		checkPoly();
		screen_mode = SCINVERSE;
	}

	/**
	 *
	 * Select mode write plotting.
	 *
	 */
	private final void selectWriteMode()
	{
		checkPoly();
		screen_mode = SCWRITE;
	}

	/**
	 *
	 * Select mode erase plotting.
	 *
	 */
	private final void selectEraseMode()
	{
		checkPoly();
		screen_mode = SCERASE; 
	}

	/**
	 * Select mode rewrite plotting.
	 */
	private final void selectRewriteMode()
	{
		checkPoly();
		screen_mode = SCREWRITE;
	}

	/**
	 *
	 * Load current X,Y from coordinates received from host.
	 *
	 */

	private final void LoadCoord()
	{
		checkPoly();
		current_x = load_x;
		current_y = load_y;
	}

	/**
	 *
	 * Increase the screen position along the perpendicular axis by 5 pixels.
	 *
	 */

	private final void SuperScript()
	{
		XYAdjust(0, 5);
	}

	/**
	 *
	 * Decrease the screen position along the perpendicular axis by 5 pixels.
	 *
	 */

	private final void SubScript()
	{
		XYAdjust(0, -5);
	}

	/**
	 * Select character memory M0.
	 */
	private final void selectM0()
	{
		text_charset = 0;
	}

	/**
	 * Select character memory M1.
	 */
	private final void selectM1()
	{
		text_charset = 1;
	}

	/**
	 * Select character memory M2.
	 */
	private final void selectM2()
	{
		text_charset = 2;
	}

	/**
	 * Select character memory M3.
	 */
	private final void selectM3()
	{
		text_charset = 3;
	}

	/**
	 * Select character memory M4.
	 */
	private final void selectM4()
	{
		text_charset = 4;
	}

	/**
	 * Select character memory M5.
	 */
	private final void selectM5()
	{
		text_charset = 5;
	}

	/**
	 * Select character memory M6.
	 */
	private final void selectM6()
	{
		text_charset = 6;
	}

	/**
	 * Select character memory M7.
	 */
	private final void selectM7()
	{
		text_charset = 7;
	}

	/**
	 * Select horizontal text plotting.
	 */
	private final void selectHoriz()
	{
		text_axis = HORIZONTAL;
	}

	/**
	 * Select vertical text plotting.
	 */
	private final void selectVert()
	{
		text_axis = VERTICAL;
	}

	/**
	 * Select forward text plotting.
	 */
	private final void selectForward()
	{
		text_dir = FORWARD;
	}

	/**
	 * Select reverse text plotting.
	 */
	private final void selectReverse()
	{
		text_dir = REVERSE;
	}

	/**
	 * Select normal size text plotting.
	 */
	private final void selectSize0()
	{
		text_size = 0;
	}

	/**
	 * Select double size text plotting.
	 */
	private final void selectSize2()
	{
		text_size = 2;
	}

	/**
	 *
	 * LoadMemConvert()
	 *
	 * Setup load memory with character convert by address.
	 *
	 */
	 
	private final void LoadMemConvert()
	{
		mem_conv = 1;
		InitTop (sLoadmem);
	}

	/**
	 *
	 * SSFProc()
	 *
	 * Process the select special function command.
	 *
	 */

	void SSFProc()
	{
	int word,ssftype;

		word = ExtractWord ( 0);
		ssftype = (word >> 10) & 0x1f;

		switch(ssftype)
		{
			// Slide projector functions (ignored).
			case 0:
				break;
			// Set interrupt mask (touch enable/disable).
			case 1:
				if ((word & 0x20) != 0)
				{
					if (!is_touch_enabled)
						is_touch_enabled = true;
				}
				else if (is_touch_enabled)
				{
					is_touch_enabled = false;
				}
				break;
			// Select input/output.
			default:
				if ((word & 0x0200) == 0)
				{
					ext_device = ssftype;
	//				if ( 0 == ( word & 0x0100))
	//					ExtData ( word & 0xff);
				}
				break;
		}
	}

	/**
	 *
	 * ExtProc()
	 *
	 * Process the external data command.
	 *
	 */

	void ExtProc()
	{
//~~ Is this commented out c++ code needed?		
	/*
		win->ExtData ( ExtractWord(0) >> 8);
		win->ExtData ( ExtractWord(0) & 0xff);
	*/
	}

	/**
	 *
	 * LoadMem()
	 *
	 * Setup for load memory without character convert by address.
	 *
	 */

	void LoadMem()
	{
		mem_conv = 0;
		InitTop (sLoadmem);
	}

	/**
	 *
	 * Mode5()
	 *
	 * Initialize for user data mode 5.
	 *
	 */

	void Mode5()
	{
		InitTop (sUser5);
	}

	/**
	 *
	 * Mode6()
	 *
	 * Initialize for user data mode 6.
	 *
	 */

	void Mode6()
	{
		InitTop (sUser6);
	}

	/**
	 *
	 * Mode7()
	 *
	 * Initialize for user data mode 7.
	 *
	 */

	void Mode7()
	{
		InitTop (sUser7);
	}

	/**
	 *
	 * LoadMemAddr()
	 *
	 * Process the load memory address command.
	 *
	 */

	void LoadMemAddr()
	{
		mem_addr = ExtractWord ( 0);
		build_count = 0;
	}

	/**
	 * Tells protocol engine the object to play sound with using JMF.
	 */
	void setJMFPlayer(JMFInterface player)
	{
		this.jmf_player = player;
	}

	/**
	 * Tells protocol engine the object to do multimedia with using QuickTime.
	 */
	void setQuickTimePlayer(QuickTimeInterface player)
	{
		this.quicktime_player = player;
	}

	/**
	 * Tells protocol engine the object to do printing with.
	 */
	void setPrintInterface(PrintInterface print_interface)
	{
		this.print_interface = print_interface;
	}

	/**
	 *
	 * LoadEcho()
	 *
	 * Process the load echo command.
	 *
	 */
	private final void LoadEcho()
	{
	int word;

		if (playback)
			return;

		word = ExtractWord ( 0);

		if	(PortalConsts.is_debugging)
			System.out.println("LoadEcho("+word+")");
		switch ( word)
		{
	// terminal type (12 == ascii)
			case 0x70:
				SendEcho ( 12);
				break;
	// terminal subtype
			case 0x71:
				is_signed_on = true;
				
				if (levelone_network.port > 7000)
					sub_type = 16;
				SendEcho ( sub_type);
				break;
	// resident load file
			case 0x72:
				SendEcho ( 0);
				break;
	// terminal configuration -- 0x40 if touch device present
			case 0x73:
				SendEcho ( 0x40);
				break;
	// send backout code and drop dtr
			case 0x7a:
				SendUsm ( 0x7f);
				break;
	// sound audible alarm at the terminal
			case 0x7b:
				Beep();
				break;
	// enable flow control if supported
			case 0x52:
				if	(is_flow_control_avail)
				{
					is_flow_control_on = true;
					SendEcho ( 0x53);
				}
				else
					SendEcho ( 0x52);
				break;
	// osid operating system id (0=dos,1=mac,2=unix,3=win,4=os2,5=nt,6=win95,7=java/misc,8=java/win,9=java/mac)
			case 0x54:
				if	(PortalConsts.is_macintosh)
					SendEcho (9);
				else if (PortalConsts.is_windows)
					SendEcho(8);
				else
					SendEcho(7);
				break;
	// catchup echo -- wait for display synchronization
			case 0x48:
				SendEcho ( 0x48);
				break;
			default:
				SendEcho ( word);
	   	}
	}

	/**
	 * Set the text margin to the current location.
	 */
	private final void setMargin()
	{
		if (text_axis == HORIZONTAL)
			text_margin = current_x;
		else
	   		text_margin = current_y;
	}

	/**
	 * Set the foreground color.
	 */
	private final void setForeColor()
	{
		checkPoly();
		if (colorAvail)
		{
			fg_color = new Color (
				ExtractRed (0),
				ExtractGreen (0),
				ExtractBlue (0));
		}
	}

	/**
	 * Set the background color.
	 */
	private final void setBackColor()
	{
		checkPoly();
		if (colorAvail)
		{
			bg_color = new Color (
				ExtractRed (0),
				ExtractGreen (0),
				ExtractBlue (0));
		}
	}

	/**
	 *
	 * Paint()
	 *
	 * Process paint command.
	 *
	 */

	void Paint()
	{
	//	todo
	}

	/**
	 *
	 * Enter XOR plotting mode.
	 *
	 */

	private final void selectExclusiveOrMode()
	{
		checkPoly();
		screen_mode = SCXOR;
	}

	/**
	 *
	 * TextStyleOr()
	 *
	 * Logical OR of text style word with word.
	 *
	 */

	void TextStyleOr()
	{
		text_style |= ExtractWord ( 0);
	}

	/**
	 *
	 * TextStyleXOr()
	 *
	 * Logical XOR of text style word with word.
	 *
	 */

	void TextStyleXor()
	{
		text_style ^= ExtractWord ( 0);
	}

	/**
	 *
	 * TextStyleSet()
	 *
	 * Set text style word to word.
	 *
	 */

	void TextStyleSet()
	{
		text_style = ExtractWord ( 0);
	}

	/**
	 *
	 * TextStyleAnd()
	 *
	 * Logical AND of text style word with word.
	 *
	 */

	void TextStyleAnd()
	{
		text_style &= ~ExtractWord ( 0);
	}

	/**
	 *
	 * TextStyleC1()
	 *
	 * Clear bit one of text style word.
	 *
	 */

	void TextStyleC1()
	{
		text_style &= 0xfffe;
	}

	/**
	 *
	 * TextStyleS1()
	 *
	 * Set bit one of text style word.
	 *
	 */

	void TextStyleS1()
	{
		text_style |= 0x0001;
	}

	/**
	 *
	 * TextStyleC2()
	 *
	 * Clear bit two of text style word.
	 *
	 */

	void TextStyleC2()
	{
		text_style &= 0xfffd;
	}

	/**
	 *
	 * TextStyleS2()
	 *
	 * Set bit two of text style word.
	 *
	 */

	void TextStyleS2()
	{
		text_style |= 0x0002;
	}

	/**
	 *
	 * TextStyleC3()
	 *
	 * Clear bit three of text style word.
	 *
	 */

	void TextStyleC3()
	{
		text_style &= 0xfffb;
	}

	/**
	 *
	 * TextStyleS3()
	 *
	 * Set bit three of text style word.
	 *
	 */

	void TextStyleS3()
	{
		text_style |= 0x0004;
	}

	/**
	 *
	 * TextStyleC4()
	 *
	 * Clear bit four of text style word.
	 *
	 */

	void TextStyleC4()
	{
		text_style &= 0xfff7;
	}

	/**
	 *
	 * TextStyleS4()
	 *
	 * Set bit four of text style word.
	 *
	 */

	void TextStyleS4()
	{
		text_style |= 0x0008;
	}

	/**
	 *
	 * TextStyleC5()
	 *
	 * Clear bit five of text style word.
	 *
	 */

	void TextStyleC5()
	{
		text_style &= 0xffef;
	}

	/**
	 *
	 * TextStyleS5()
	 *
	 * Set bit five of text style word.
	 *
	 */

	void TextStyleS5()
	{
		text_style |= 0x0010;
	}

	/**
	 *
	 * TextStyleC6()
	 *
	 * Clear bit six of text style word.
	 *
	 */

	void TextStyleC6()
	{
		text_style &= 0xff1f;
	}

	/**
	 *
	 * TextStyleS6()
	 *
	 * Set bit six of text style word.
	 *
	 */

	void TextStyleS6()
	{
		text_style |= 0x0020;
	}

	/**
	 *
	 * TextStyleC7()
	 *
	 * Clear bit seven of text style word.
	 *
	 */

	void TextStyleC7()
	{
		text_style &= 0xff3f;
	}

	/**
	 *
	 * TextStyleS7()
	 *
	 * Set bit seven of text style word.
	 *
	 */

	void TextStyleS7()
	{
		text_style |= 0x0040;
	}

	/**
	 *
	 * TextStyleC8()
	 *
	 * Clear bit eight of text style word.
	 *
	 */

	void TextStyleC8()
	{
		text_style &= 0xff7f;
	}

	/**
	 *
	 * TextStyleS8()
	 *
	 * Set bit eight of text style word.
	 *
	 */

	void TextStyleS8()
	{
		text_style |= 0x0080;
	}

	/**
	 *
	 * TextStyleC9()
	 *
	 * Clear bit nine of text style word.
	 *
	 */

	void TextStyleC9()
	{
		text_style &= 0xfeff;
	}

	/**
	 *
	 * TextStyleS9()
	 *
	 * Set bit nine of text style word.
	 *
	 */

	void TextStyleS9()
	{
		text_style |= 0x0100;
	}

	/**
	 *
	 * TextStyleC10()
	 *
	 * Clear bit ten of text style word.
	 *
	 */

	void TextStyleC10()
	{
		text_style &= 0xfdff;
	}

	/**
	 *
	 * TextStyleS10()
	 *
	 * Set bit ten of text style word.
	 *
	 */

	void TextStyleS10()
	{
		text_style |= 0x0200;
	}

	/**
	 *
	 * TextStyleC11()
	 *
	 * Clear bit eleven of text style word.
	 *
	 */

	void TextStyleC11()
	{
		text_style &= 0xfbff;
	}

	/**
	 *
	 * TextStyleS11()
	 *
	 * Set bit eleven of text style word.
	 *
	 */

	void TextStyleS11()
	{
		text_style |= 0x0400;
	}

	/**
	 *
	 * Set the pen pattern.
	 *
	 */

	void SetPenPat()
	{
		checkPoly();
		
		style_pattern = data[0] & 0x3f;
	}

	/**
	 *
	 * Set the pen thickness.
	 *
	 */

	void SetPenThick()
	{
	int value;

		checkPoly();
		value = ExtractWord ( 0);
		
		if (value > 0)
		{
			if (0 != ( value & 1))
				value--;

			style_thickness = value;
		}
	}

	/**
	 *
	 * Set the cap style.
	 *
	 */

	void SetPenCap()
	{    
		checkPoly();
		style_cap = data[0] & 0x3;
	}

	/**
	 *
	 * Set the dash style.
	 *
	 */

	void SetPenDash()
	{   
		checkPoly();
		style_dash = data[0] & 0xf;
	}

	/**
	 *
	 * Set the join style.
	 *
	 */

	void SetPenJoin()
	{
		checkPoly();
		style_join = data[0] & 3;
	}

	/**
	 *
	 * Set the polygon fill bit.
	 *
	 */

	void SetPenFill()
	{
		checkPoly();
		style_fill = data[0] & 0x1;
	}

	/**
	 *
	 * Load a user-defined pen pattern.
	 *
	 */

	void LoadUserPat()
	{
	int pattern;
	int slot;

		slot = data[0] & 0x3f;
		if (slot > 31 && slot < 64)
		{
			slot -= 32;
			pattern = ExtractWord (1);
			user_patterns[8*slot+1] = ((pattern >> 8) & 0xff);
			user_patterns[8*slot] = (pattern & 0xff);
			pattern = ExtractWord (4);
			user_patterns[8*slot+3] = ((pattern >> 8) & 0xff);
			user_patterns[8*slot+2] = (pattern & 0xff);
			pattern = ExtractWord (7);
			user_patterns[8*slot+5] = ((pattern >> 8) & 0xff);
			user_patterns[8*slot+4] = (pattern & 0xff);
			pattern = ExtractWord (10);
			user_patterns[8*slot+7] = ((pattern >> 8) & 0xff);
			user_patterns[8*slot+6] = (pattern & 0xff);
		}
	}

	/**
	 *
	 * Init to load user-defined dash definition.
	 *
	 */

	void LoadUserDash()
	{
		data[0] &= 0x3f;
		data[1] &= 0x3f;

		stack_pointer++;
		data_num[stack_pointer] = 3*data[1];
		data_pnt = 2;
		post_action_i[stack_pointer] = CMDextra-CMD;
		post_action_j[stack_pointer] = 0;
	}

	/**
	 *
	 * Done accumlating user-defined dash definition.
	 *
	 */

	void LoadedUserDash()
	{
	//	data[0] = slot
	//	data[1] = number segments
	int slot = data[0]-8;

		if (slot >= 0 && slot <= 7)
		{
		int i;

			user_dashes[slot] = new float[data[1]];

			for (i=0; i < 8 && i < data[1]; i++)
				user_dashes[slot][i] = (float) ExtractWord(2+3*i);
		}
	}

	/**
	 *
	 * Returns true if stringbuffer describes a quicktime movie format.
	 *
	 */
	private final boolean IsMovieFile(StringBuffer x)
	{
		String test = x.toString();

		if (test.endsWith(".mov") || test.endsWith(".wav"))
			return true;

		return false;
	}

	/**
	 *
	 * Returns true if stringbuffer describes a jpeg file.
	 *
	 */

	private final boolean IsJPEGFile(StringBuffer x)
	{
	String test = x.toString();

		if (test.endsWith(".jpg"))
			return true;

		return false;
	}

	/**
	 *
	 * Returns true if stringbuffer describes a quicktime still picture format.
	 *
	 */
	private final boolean IsPictureFile(StringBuffer x)
	{
		String test = x.toString();

		if (IsJPEGFile(x) || test.endsWith(".gif"))
			return true;

		return false;
	}

	/**
	 * Make rectangle flipped.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	Rectangle MakeRectFlipped(
		int x1,
		int y1,
		int x2,
		int y2,
		int width,
		int height)
	{
		int topx,topy;

		// Limit coordinates to size of object.
		if (y1 >= height)
			y1 = height-1;
		if (y2 >= height)
			y2 = height-1;
		if (x1 >= width)
			x1 = width-1;
		if (x2 >= width)
			x2 = width-1;

	// system uses lower left origin; flip to upper left
		y1 = height-1-y1;
		y2 = height-1-y2;

	// get top left corner coordinates
		if (x1 < x2)
			topx = x1;
		else
			topx = x2;
		if (y1 < y2)
			topy = y1;
		else
			topy = y2;

		return new Rectangle(topx,topy,abs(x1-x2)+1,abs(y1-y2)+1);
	}

	/**
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	Rectangle MakeRectLegal(
		int x1,
		int y1,
		int x2,
		int y2)
	{
		int topx,topy;

		if (x1 < x2)
			topx = x1;
		else
			topx = x2;
		if (y1 < y2)
			topy = y1;
		else
			topy = y2;

		return new Rectangle(topx,topy,abs(x1-x2)+1,abs(y1-y2)+1);
	}

	/**
	 * Make rectangle.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	Rectangle MakeRect(
		int x1,
		int y1,
		int x2,
		int y2)
	{
	int topx,topy;

		x1 = xlatX(x1+center_x);
		y1 = xlatY(y1+center_y);
		x2 = xlatX(x2+center_x);
		y2 = xlatY(y2+center_y);
		if (x1 < x2)
			topx = x1;
		else
			topx = x2;
		if (y1 < y2)
			topy = y1;
		else
			topy = y2;

		return new Rectangle(topx,topy,abs(x1-x2)+1,abs(y1-y2)+1);
	}

	/**
		-image- command echo codes:

		0	Success
		1	File operations disabled
		2	No filename in buffer
		3	Can't open file
		4	File not in format readable by -image- command
		5	Slot ID out of range
		6	Terminal has insufficient memory for operation
		7	ID buffer is empty
		8	Filename filename_sum_check error
		9	Program doesn't support this file format
		10	Subrectangle or coordinate out of range
		11	File not found
		12	Load path
		13	Too many files open
		14	Access denied
		15	Disk error (hard)
		16	Disk full
	 */
	/**
	 *
	 * Save a bitmap image.
	 *
	 */
	void ImageSave()
	{
		if (!playback)
		{
	// if destination is disk file, check fileops & verify filename filename_sum_check
			if (((ExtractWord(12)>>8) & 3) == 0)
			{
				if (disable_file_operations)
				{
					SendEcho(1);
				}
				else if (filename.length()==0)
				{
					SendEcho(2);
				}
				else if (!VerifyFilenameSumChk())
				{
					SendEcho(8);
				}
				else
				{
					if	(PortalConsts.is_debugging)	System.out.println("imagesave to file:" + filename.toString());
					SendEcho(9);
				}	
			}
			else
			{
			int slot = (ExtractWord(12)&255)-1;

				if	(PortalConsts.is_debugging)	System.out.println("imagesave to mem:" + slot);
				if (slot < 0 || slot >= IMAGE_SLOTS)
					SendEcho(5);
				else
				{
				Rectangle	rect = MakeRect(ExtractWord(0),ExtractWord(3),ExtractWord(6),ExtractWord(9));

	// create image to store graphic in
					image_slots[slot] = levelone_container.createImage (rect.width,rect.height);

	// copy graphic from the backing store to the image
					image_slots[slot].getGraphics().drawImage(
						levelone_offscreen_image,
						0,0,rect.width,rect.height,
						rect.x,rect.y,rect.x+rect.width,rect.y+rect.height,
						null);

	// tell system it worked
					SendEcho(0);
				}
			}
		}
	}

	/**
	 *
	 * Monitors quicktime media and sends status updates to novanet.
	 *
	 */
	private final void MonitorMultiMedia()
	{
		if	(null != quicktime_player)
			quicktime_player.idle();

		if	(null != child_process)
		{
			try
			{
			int	ret = child_process.exitValue();

				if	(PortalConsts.is_debugging)	System.out.println("child done, ret="+ret);
				if	(ret == 0)
					LocalExecuteResult(0);
				else
					LocalExecuteResult(1);
				child_process = null;
			}
			catch (Exception e3)
			{
			}
		}
	}


	/**
	 *
	 * Display still image using built-in java support.
	 *
	 */
//~~ Again, this function and the next are very similar and could share more code.
	private final Image ImageGetFileJava (String url)
	{
		try
		{
		Image			file_image = java.awt.Toolkit.getDefaultToolkit().getImage(new java.net.URL(url));
		MediaTracker	tracker = new MediaTracker(levelone_container);
		
			tracker.addImage(file_image,1);
			tracker.waitForID(1);
			if (!tracker.isErrorAny())
				return file_image;
		}
		catch (java.lang.InterruptedException e)
		{
		}
		catch (java.net.MalformedURLException e2)
		{
		}

		imerror = 4;
		return null;
	}

	private final Image ImageGetFileJava (File file_handle)
	{
		try
		{
		Image			file_image = java.awt.Toolkit.getDefaultToolkit().getImage(file_handle.getCanonicalPath());
		MediaTracker	tracker = new MediaTracker(levelone_container);
		
			tracker.addImage(file_image,1);
			tracker.waitForID(1);
			if (!tracker.isErrorAny())
				return file_image;
		}
		catch (java.lang.InterruptedException e)
		{
		}
		catch (java.io.IOException e2)
		{
		}

		imerror = 4;
		return null;
	}

	/**
	 *
	 * Renders an image on the display.
	 *
	 */

	final void ImageRenderBase(
		Image image_handle,
		Rectangle dest_rect,
		Rectangle source_rect)
	{
		modeClipColor(false,false,false);

	// render image on screen
		if (is_direct_draw)
		{
			levelone_base_graphics.drawImage(
				image_handle,
				dest_rect.x,dest_rect.y,dest_rect.x+dest_rect.width,dest_rect.y+dest_rect.height,
				source_rect.x,source_rect.y,source_rect.x+source_rect.width,source_rect.y+source_rect.height,
				levelone_container);
		}
		else
			do_repaint = true;

	// render image on backing store
		levelone_offscreen.drawImage(
			image_handle,
			dest_rect.x,dest_rect.y,dest_rect.x+dest_rect.width,dest_rect.y+dest_rect.height,
			source_rect.x,source_rect.y,source_rect.x+source_rect.width,source_rect.y+source_rect.height,
			null);
	}

	private final void ImageRender(Image image_handle)
	{
	Rectangle	dest_rect = MakeRect(ExtractWord(12),ExtractWord(15),ExtractWord(18),ExtractWord(21));
	Rectangle	source_rect = MakeRectFlipped(ExtractWord(0),ExtractWord(3),ExtractWord(6),ExtractWord(9),
					image_handle.getWidth(nil_observer),image_handle.getHeight(nil_observer));

		ImageRenderBase(image_handle,dest_rect,source_rect);
		imerror = 0;
	}

	/**
	 *
	 * Display a bitmap image.
	 *
	 */

	private final void ImageGet()
	{
		end_parse = true;

		if (((ExtractWord(24)>>8) & 3) == 0)		// from disk file
		{
	// controls if palettes should be loaded from image;
	// this now controls whether a quicktime movie
	// is displayed with a movie controller.
		int loadpal = (ExtractWord(24)>>11)&1;

			if (disable_file_operations && !using_resource_server)
			{
				imerror = 1;
			}
			else if (filename.length()==0)
			{
				imerror = 2;
			}
			else if (!VerifyFilenameSumChk())
			{
				imerror = 8;
			}
			else if (using_resource_server)
			{
			String file_url = CreateURL();
			Image	file_image = null;

				if	(PortalConsts.is_debugging) System.out.println("image get;"+file_url);

	// only do still image stuff if file isn't an obvious movie file
				if (!IsMovieFile(filename))
				{
					if (null != image_info_image && image_info_filename.equals(filename.toString()))
						file_image = image_info_image;
					else if (IsJPEGFile(filename))
						file_image = ImageGetFileJava(file_url);
					if (null == file_image)
						file_image = quicktime_player.ImageGetFile(file_url);

	// release cached image
					image_info_image = null;
				}

				if (null != file_image)
					ImageRender(file_image);
				else
				{
					quicktime_player.ImageGetFileQuicktimeMovie(
						MakeRect(ExtractWord(12),ExtractWord(15),ExtractWord(18),ExtractWord(21)),
						file_url,loadpal);
				}
			}
			else
			{
			File file_handle = CreateFile();
			Image	file_image = null;

				if	(PortalConsts.is_debugging)	System.out.println("image get;"+filename.toString());

				if (!file_handle.exists())
				{
					imerror = 11;
					return;
				}

				if (!IsMovieFile(filename))
				{
					if (null != image_info_image && image_info_filename.equals(filename.toString()))
						file_image = image_info_image;
					else if (IsJPEGFile(filename))
						file_image = ImageGetFileJava(file_handle);
					if (null == file_image)
						file_image = quicktime_player.ImageGetFile(file_handle);

	// release any cached image
					image_info_image = null;
				}

				if (null != file_image)
					ImageRender(file_image);
				else
				{
					quicktime_player.ImageGetFileQuicktimeMovie(
						MakeRect(ExtractWord(12),ExtractWord(15),ExtractWord(18),ExtractWord(21)),
						file_handle,loadpal);
				}
			}
		}
		else
		{
		int slot = (ExtractWord(24)&255)-1;

	// 	System.out.println("image get;mem,"+slot);
	// invalid slot
			if (slot < 0 || slot >= IMAGE_SLOTS)
				imerror = 5;
	// check for slot with no content
			else if (null == image_slots[slot])
				imerror = 7;
	// all ok, do it
			else
			{
				imerror = 0;
				ImageRender(image_slots[slot]);
			}
		}
	}

	/**
	 *
	 * Get image information.
	 *
	 */
	private final void ImageInfo()
	{
	int ret;

	// if source is disk file, verify filename sumchk
		if (((ExtractWord(0)>>8) & 3) == 0)
		{
			if (disable_file_operations && !using_resource_server)
			{
				SendPixRes(0,0777777,1,0);
			}
			else if (0 == filename.length())
			{
				SendPixRes(0,0777777,2,0);
			}
			else if (!VerifyFilenameSumChk())
			{
				SendPixRes(0,0777777,8,0);
			}
			else if (using_resource_server)
			{
			String 		file_url = CreateURL();
			Image		file_image = null;
			int height = 3;	// error
			int width = 0777777;	// can't open file

				if	(PortalConsts.is_debugging)	System.out.println("image info;"+file_url);

				if (!IsMovieFile(filename))
				{
					if (IsJPEGFile(filename))
						file_image = ImageGetFileJava(file_url);
					if (null == file_image)
						file_image = quicktime_player.ImageGetFile(file_url);

					if (null != file_image)
					{
						image_info_image = file_image;
						image_info_filename = filename.toString();
					}
				}

				if (null == file_image)
				{
				Dimension	d = quicktime_player.ImageInfoFileMovie(file_url);

					if (null != d)
					{
						width = d.width;
						height = d.height;
					}
				}
				else
				{
					width = file_image.getWidth(nil_observer);
					height = file_image.getHeight(nil_observer);
				}
				
				SendPixRes(0,width,height,0);
			}
			else
			{
			File 		file_handle = CreateFile();
			Image			file_image = null;
			int height = 3;			// can't open file error
			int width = 0777777;	// error flag

				if (!file_handle.exists())
				{
					SendPixRes(0,0777777,11,0);
					return;
				}

				if (!IsMovieFile(filename))
				{
					if (IsJPEGFile(filename))
						file_image = ImageGetFileJava(file_handle);
					if (null == file_image)
						file_image = quicktime_player.ImageGetFile(file_handle);
				}

				if (null == file_image)
				{
				Dimension	d = quicktime_player.ImageInfoFileMovie(file_handle);

					if (null != d)
					{
						width = d.width;
						height = d.height;
					}
				}
				else
				{
					width = file_image.getWidth(nil_observer);
					height = file_image.getHeight(nil_observer);
				}
				
				SendPixRes(0,width,height,0);
			}
		}
		else
		{
		int slot = (ExtractWord(0)&255)-1;

			if (slot < 0)		// get window size
			{
				SendPixRes(0,terminalWidth,terminalHeight,0);
			}
			else if (null == image_slots[slot])	// slot with no image
			{
				SendPixRes(0,0777777,7,0);
			}
			else
			{
				SendPixRes(0,image_slots[slot].getWidth(null),image_slots[slot].getHeight(null),0);
			}
		}

	}

	/**
	 *
	 * Copy bitmap image.
	 *
	 */

	private final void ImageCopy()
	{
	Rectangle		source_rect = MakeRect(ExtractWord(0),ExtractWord(3),ExtractWord(6),ExtractWord(9));
	Rectangle		dest_rect = MakeRect(ExtractWord(12),ExtractWord(15),ExtractWord(18),ExtractWord(21));

	// java doesn't deal with the source and destination images overlapping, so .. make
	// another copy of the source rect..
	Image			si = levelone_container.createImage(source_rect.width,source_rect.height);
	Graphics		sig = si.getGraphics();

		modeClipColor(false,false,false);

		sig.drawImage(levelone_offscreen_image,
			0,0,source_rect.width,source_rect.height,
			source_rect.x,source_rect.y,source_rect.x+source_rect.width,source_rect.y+source_rect.height,
			null);
		sig.dispose();
			
		levelone_offscreen.drawImage(
			si,
			dest_rect.x,dest_rect.y,dest_rect.x+dest_rect.width,dest_rect.y+dest_rect.height,
			0,0,source_rect.width,source_rect.height,
			nil_observer);

		if (is_direct_draw)
		{
			levelone_base_graphics.drawImage(
				si,
				dest_rect.x,dest_rect.y,dest_rect.x+dest_rect.width,dest_rect.y+dest_rect.height,
				0,0,source_rect.width,source_rect.height,
				levelone_container);
		}
		else
			do_repaint = true;
	}

	/**
	 *
	 * Delete bitmap image.
	 *
	 */

	private final void ImageDelete()
	{
		if (playback)
			return;

	// if destination is disk file, verify filename sumchk
		if (((ExtractWord(0)>>8) & 3) == 0)
		{
			if (disable_file_operations)
			{
				SendEcho(1);
			}
			else if (0 == filename.length())
			{
				SendEcho(2);
			}
			else if (!VerifyFilenameSumChk())
			{
				SendEcho(8);
			}
			else if (using_resource_server)
			{
				SendEcho(1);
			}
			else
			{
			File image_file = CreateFile();

				image_file.delete();
				SendEcho(0);
			}
		}
		else
		{
		int slot = (ExtractWord(0)&255)-1;

			if (slot < 0)
			{
			int i;

				for (i=0;i<IMAGE_SLOTS;i++)
					image_slots[i] = null;

				SendEcho(0);
			}
			else
			{
				image_slots[slot] = null;
				SendEcho(0);
			}
		}
	}

	/**
	 *
	 * Send image result to host.
	 *
	 */

	private final void ImageEcho()
	{
		if	(PortalConsts.is_debugging) System.out.println("image check");
		SendEcho(imerror);
	}

	/**
	 *
	 * Loads an image file into a memory image slot.
	 *
	 */

	private final void ImageLoad()
	{
		end_parse = true;

		if (playback)
			return;
		else if (disable_file_operations && !using_resource_server)
		{
			imerror = 1;
			return;
		}
		else if (0 == filename.length())
		{
			imerror = 2;
			return;
		}
		else if (!VerifyFilenameSumChk())
		{
			imerror = 8;
			return;
		}

	int 	slot = (ExtractWord(0) & 255)-1;

		if	(PortalConsts.is_debugging)	System.out.println("image load;"+filename.toString()+","+slot);

		if (slot < 0 || slot >= IMAGE_SLOTS)
		{
			imerror = 5;
			return;
		}

		if (using_resource_server)
		{
		String file_url = CreateURL();
		Image	file_image = null;

			file_image = quicktime_player.ImageGetFile(file_url);
			image_slots[slot] = file_image;
			if (null == file_image)
				imerror = 4;
			else
				imerror = 0;
		}
		else
		{
		File file_handle = CreateFile();
		Image	file_image = null;

			if (!file_handle.exists())
			{
				imerror = 3;
				return;
			}

			file_image = ImageGetFileJava(file_handle);
			if (null == file_image)
				file_image = quicktime_player.ImageGetFile(file_handle);

			image_slots[slot] = file_image;
			if (null == file_image)
				imerror = 4;
			else
				imerror = 0;
		}
	}

//~~ Need function header.
	void ImagePause()
	{
		quicktime_player.imagePause();
	}

//~~ Need function header.
	void ImageResume()
	{
		quicktime_player.imageResume();
	}

//~~ Need function header.
	void ImageStop()
	{
		quicktime_player.imageStop();
	}


	/**
	 *
	 * Save entire window as bitmap image.  Feature deemed obsolete and
	 * is not supported.
	 *
	 */

	void ImageSaveAll()
	{
	}

	/**
	 *
	 * Load entire window from bitmap image.  Feature deemed obsolete and
	 * is not supported.
	 *
	 */

	void ImageGetAll()
	{
	}

	/**
	 * Set foreground palette slot.
	 */
	void setForeSlot()
	{
		checkPoly();
	}

	/**
	 * Set background palette slot.
	 */
	void setBackSlot()
	{
		checkPoly();
	}

	/**
	 *
	 * Load color into a palette slot.
	 *
	 */

	void LoadSlot()
	{
		do_palette_hold = true;
//~~ More commented out c++ code.		
	/*
		if (colorAvail & 2)
		{
			win->SetPalSlot ( ExtractSlot ( 0), ExtractRed ( 2), ExtractGreen ( 2),
				 ExtractBlue ( 2));
		}
	*/
	}

	/**
	 *
	 * TrapDragStop()
	 *
	 * End a trap drag.
	 *
	 */

	void TrapDragStop()
	{
//~~ More commented out c++ code.		
	// if ( !playback)
	// {
	// 	TrapEndInternal();
	// 	win->TrapDragEnd();
	// }
	}

	/**
	 *
	 * Receive filename sumchk.
	 *
	 */

	void FilenameSumChk()
	{
		filename_sum_check = ExtractWord ( 0);
	}

	/**
	 *
	 * Initiate trap drag.
	 *
	 */

	void TrapDrag()
	{
//~~ More commented out c++ code.		
	/*
	int buffer;

		buffer = ExtractWord(0);
		if (buffer >= 0 && buffer <= MAXTRAP && !playback)
		{
			TrapEndInternal();
			win->TrapDragInit(buffer,ExtractWord(3),ExtractWord(6),
				ExtractWord(9)+center_x,ExtractWord(12)+center_y);
		}
	*/
	}

	/**
	 *
	 * Tests for existance of trap file.
	 *
	 */

	void TrapTst()
	{
		if (!playback)
		{
			if	(PortalConsts.is_debugging) System.out.println("trap test;"+filename.toString());
			TrapEndInternal();
			if (disable_file_operations)
				SendEcho(1);
			else
			{
				if (0 == filename.length())
					SendEcho(2);
				else if (!VerifyFilenameSumChk())
					SendEcho(14);
				else
				{
				File trap_file = CreateFile();

					if (trap_file.isFile())
						SendEcho(0);
					else
						SendEcho(3);
				}
			}
		}
	}

	/**
	 *
	 * Writes trap buffer to disk file.
	 *
	 */

	void TrapWrite()
	{
		if (playback)
			return;

	int buffer;

		TrapEndInternal();
		buffer = ExtractWord (0);
		if	(PortalConsts.is_debugging)	System.out.println("trap write;"+buffer+";"+filename.toString());
		if (buffer < 0 || buffer >= TRAP_SLOTS)
		{
			SendEcho(6);
			return;
		}
		else if (null == trap_slots[buffer])
		{
			SendEcho(11);
			return;
		}
		else if (0 == filename.length())
		{
			SendEcho(2);
			return;
		}
		else if(!VerifyFilenameSumChk())
		{
			SendEcho(14);
			return;
		}

		try
		{
		FileOutputStream	trap_file = new FileOutputStream(CreateFile());

			trap_slots[buffer].writeTo(trap_file);
			SendEcho(0);
			trap_file.close();
		}
		catch (IOException e)
		{
			SendEcho(13);
		}
	}

	/**
	 *
	 * Reads disk file into trap buffer.
	 *
	 */

	void TrapRead()
	{
		if (playback)
			return;

	int 	buffer;

		TrapEndInternal();
		buffer = ExtractWord (0);

		if	(PortalConsts.is_debugging)	System.out.println("trap read;"+buffer+";"+filename.toString());

		if (buffer < 0 || buffer >= TRAP_SLOTS)
		{
			SendEcho(6);
			return;
		}

		if (null == trap_slots[buffer])
			trap_slots[buffer] = new ByteArrayOutputStream();

		if (null == trap_slots[buffer])
		{
			SendEcho(9);
			return;
		}

		trap_slots[buffer].reset();	// discard current contents
		if (0 == filename.length())
		{
			SendEcho(2);
			return;
		}
		else if (!VerifyFilenameSumChk())
		{
			SendEcho(14);
			return;
		}
	File trap_file = CreateFile();
	byte[] read_buffer = new byte[256];
	long file_length = trap_file.length();

		try
		{
		FileInputStream	trap_fis = new FileInputStream(trap_file);
		long 		offset;
		
			for (offset=0; offset < file_length; offset += 256)
			{
			long remain = file_length - offset;
			
				if (remain > 256)
					remain = 256;
				trap_fis.read(read_buffer,0,(int) remain);
				trap_slots[buffer].write(read_buffer,0,(int) remain);
			}
			trap_fis.close();
			SendEcho(0);
		}
		catch (IOException e)
		{
			SendEcho(10);
		}
	}

	/**
	 *
	 * Plots trap buffer on screen.
	 *
	 */

	void TrapDisplay()
	{
		if (!playback)
		{
			TrapEndInternal();
			trapPlot(ExtractWord(0),ExtractWord(3),ExtractWord(6),screen_mode);
		}
	}

	/**
	 *
	 * Deletes trap buffer.
	 *
	 */

	void TrapDelete()
	{
		if (!playback)
		{
		int buffer;

			TrapEndInternal();
			buffer = ExtractWord ( 0);
			if	(PortalConsts.is_debugging)
				System.out.println("trap delete;"+buffer);
		
			if (buffer < TRAP_SLOTS)
			{		
				if (buffer < 0)
					for (buffer = 0; buffer < TRAP_SLOTS; buffer++)
						DeleteTrapBuffer (buffer);
				else
					DeleteTrapBuffer (buffer);
			}
		}
	}

	/**
	 *
	 * Starts saving of output to trap buffer.
	 *
	 */

	void TrapSave()
	{
		if (L1PPDataMode == data_proc && !playback)
		{
		int buffer;

			buffer = ExtractWord (0);
			if	(PortalConsts.is_debugging)	System.out.println("trap save;"+buffer);
			if (buffer >= 0 && buffer < TRAP_SLOTS)
			{
				if (null == trap_slots[buffer])
					trap_slots[buffer] = new ByteArrayOutputStream();
				if (null == trap_slots[buffer])
					trap_status = TRFAILED+9;
				else
				{
					data_proc = TrapDataMode;
					do_display_while_trap = (data[3] == 'B');
					trap_buffer = buffer;
					trap_status = TRACTIVE;
				}
			}
		}
		else
			TrapEndInternal();
	}

	/**
	 *
	 * Ends saving of output to trap buffer.
	 *
	 */

	void TrapEnd()
	{
		if (!playback)
		{
			data_proc = L1PPDataMode;
			if ( trap_status == TRACTIVE)
				SendEcho ( 0);
			else if ( trap_status == TRNONE)
				SendEcho ( 11);
			else
				SendEcho ( trap_status - TRFAILED);

			trap_status = TRNONE;
		}
	}

	/**
	 *
	 * End trap, without echo (for engine internal use).
	 *
	 */

	void TrapEndInternal()
	{
		if (TrapDataMode == data_proc)
		{
			trap_status = TRNONE;
			data_proc = L1PPDataMode;
		}
	}

	/**
	 *
	 * FilenameClear()
	 *
	 * Clear filename buffer.
	 *
	 */

	void FilenameClear()
	{
		filename.setLength(0);
	}

	/**
	 *
	 * FilenameLoad()
	 *
	 * Initialize filename loading.
	 *
	 */

	void FilenameLoad()
	{
		InitTop (sLoadFile);
	}

	/**
	 *
	 * AsyncAlert()
	 *
	 * Process async alert command.
	 *
	 */

	private void AsyncAlert()
	{
	//	win->AsyncAlert();
	}

	/**
	 *
	 * ExtdataInit()
	 *
	 * Enter external data mode.
	 *
	 */

	void ExtdataInit()
	{
		InitTop (sExt);
	}

	/**
	 *
	 * TerminalReport()
	 *
	 * Report terminal characteristics.
	 *
	 */

	void TerminalReport()
	{
	int 	termStatsX,termStatsY;

	/*
	// WORD 1:
	// 0-9: X resolution
	// 10: trap/stretch - yes (400)
	// 11: user patterns/styles - yes (800)
	// 12: local execute - yes (1000)
	// 13: mode xor support - yes (2000)
	// 14: image support - yes (4000)
	// 15: local inhibit - yes (8000)
	// 16: text cursor/scroll - yes (10000)
	// 17: text screen printing - yes (20000)
	// 
	// WORD 2:
	// 0-11: number of palette slots
	// 12: palettes - possible (1000)
	// 13: color - possible (2000)
	// 14: kermit - yes (4000)
	// 15: super/sub/invert textstyles+editing keymap - yes (8000)
	// 16: 7 bit exts/trap buffers 32+ (10000)
	// 17: extended capability bit

		termStatsX = terminalWidth |
	//		0x400 |		// trap + stretch
	//		0x800 |		// user patterns
			0x1000 |	// local execute
			0x2000 |	// mode xor
			0x4000 |	// image
			0x8000 |	// local inhibit
	//		0x10000 |	// text cursor + scroll
	//		0x20000 |	// text screen printing
			0;

	//
		termStatsY =
			0x4000 |	// kermit
	//		0x8000 |
	//		0x10000 |
			0;
	*/

	/*
	With the extended capability bit:
		X:
		0-7:	chars on screen
		8:		ftp support (100)
		9:		local os support (200)
		10:		audio support (400)
		11:		multi-click touch (800)
		12:		extended control key support (1000)
		13:		local font support (2000)
		14:		local printing (4000)
		15:		terminfo2, cursor, mouse modifiers (8000)
		16:		print check (10000)
		17:		local execute on URL, local print, new local fonts,
				quicktime movies & still images, image pause/resume/stop (20000)

		Y:
		0-11:	palette slots
		12:		palette flag (1000)
		13:		color flag (2000)
		14:		2.4.1 features: os autosignon, image load (4000)
		15:		3.1+ features: mouse up, mouse motion (8000)
		16:		7 bit exts/sys trap buffers (32+) (10000)
		17:		extended capability flag (20000)
	*/
		
		termStatsX = (terminalWidth >> 3) |
	//		0x100 |			// local ftp
			0x200 |			// local os
			0x400 |			// sound
			0x800 |			// multi-click touch
			0x1000 |		// control keys
			0x2000 |		// local font
			0x4000 |		// local print
			0x8000 |		// terminfo2, cursor, mouse modifiers
			0x10000 |		// print check
			0x20000	|		// local execute url, new local fonts, quicktime
			0;

		termStatsY =
			(256+16) |		// report 16 bit color
			0x2000 |		// color
			0x4000 |		// image load
			0x8000 |		// mouse motion, mouse up
			0x10000 |		// more trap buffers
			0x20000 |		// extended capability flag
			0;

		if (autosignon)
			SendPixRes ( 3, termStatsX, termStatsY,0);
		else
			SendPixRes ( 2, termStatsX, termStatsY,0);
	}

	/**
	 *
	 * ExtensionSelect()
	 *
	 * Turn on level 1 enhancements.
	 *
	 */

	void ExtensionSelect()
	{
		up_enhance = true;
	//	win->netdata->EnableDetectAbort();
	//	win->netdata->EnableFlowIn();
	}

	/**
	 *
	 * SetXOffset()
	 *
	 * Set X offset.
	 *
	 */

	void SetXOffset()
	{
		center_x = ExtractWord ( 0);
		sys_x = center_x;
		wrap_x = terminalWidth - 1 - (center_x << 1);
		ClipSet (sys_x,sys_y,wrap_x+sys_x,wrap_y+sys_y,true);
	}

	/**
	 *
	 * SetYOffset()
	 *
	 * Set Y offset.
	 *
	 */

	void SetYOffset()
	{
		center_y = ExtractWord ( 0);
		sys_y = center_y;
		wrap_y = terminalHeight - 1 - (center_y << 1);
		ClipSet (sys_x,sys_y,wrap_x+sys_x,wrap_y+sys_y,true);
	}

	/**
	 *
	 * SetClipping()
	 *
	 * Set clipping rectangle.
	 *
	 */

	void SetClipping()
	{
		if ((old_x|old_y|load_x|load_y)==0)
			ClipSet(sys_x,sys_y,wrap_x+sys_x,wrap_y+sys_y,false);
		else
			ClipSet (center_x+old_x,center_y+old_y,center_x+load_x,center_y+load_y,false);
	}

	/**
	 *
	 * EllipticalArcInit()
	 *
	 * Initialize for elliptical arc mode.
	 *
	 */

	void EllipticalArcInit()
	{
		InitTop (sEllipArc);
	}

	/**
	 *
	 * CircularArcInit()
	 *
	 * Initialize for circular arc mode.
	 *
	 */

	void CircularArcInit()
	{
		InitTop (sCircArc);
	}

	/**
	 *
	 * CircleInit()
	 *
	 * Initialize for circle mode.
	 *
	 */

	void CircleInit()
	{
		InitTop (sCircle);
	}

	/**
	 *
	 * EllipseInit()
	 *
	 * Initialize for ellipse mode.
	 *
	 */

	void EllipseInit()
	{
		InitTop (sEllipse);
	}

	/**
	 *
	 * BoxInit()
	 *
	 * Initialize for box mode.
	 *
	 */

	private void BoxInit()
	{
		InitTop (sBox);
	}

	/**
	 *
	 * StretchEndline()
	 *
	 * Process stretch endline command.
	 *
	 */

	void StretchEndline()
	{
	//	win->StretchEndline(ExtractWord(0)+center_x,ExtractWord(3)+center_y,
	//		ExtractWord(6)+center_x,ExtractWord(9)+center_y,
	//		style_pattern,style_fill,style_thickness,style_dash);
	}

	/**
	 *
	 * StretchMidline()
	 *
	 * Process stretch midline command.
	 *
	 */

	void StretchMidline()
	{
//~~ More commented out c++ code.		
	//	win->StretchMidline(ExtractWord(0)+center_x,ExtractWord(3)+center_y,
	//		ExtractWord(6)+center_x,ExtractWord(9)+center_y,
	//		ExtractWord(12)+center_x,ExtractWord(15)+center_y,
	//		style_pattern,style_fill,style_thickness,style_dash);
	}

	/**
	 *
	 * StretchCircle()
	 *
	 * Process stretch circle command.
	 *
	 */

	void StretchCircle()
	{
//~~ More commented out c++ code.		
	//	win->StretchCircle(current_x+center_x,current_y+center_y,
	//		ExtractWord(0),style_pattern,style_fill,style_thickness,style_dash);
	}

	/**
	 *
	 * StretchEllipseX()
	 *
	 * Process stretch ellipseX.
	 *
	 */

	void StretchEllipseX()
	{
//~~ More commented out c++ code.		
	//	win->StretchEllipseX(current_x+center_x,current_y+center_y,
	//		ExtractWord(0),ExtractWord(3),style_pattern,style_fill,
	//		style_thickness,style_dash);
	}

	/**
	 *
	 * Process stretch ellipsey.
	 *
	 */
	void StretchEllipseY()
	{
	//	win->StretchEllipseY(current_x+center_x,current_y+center_y,
	//		ExtractWord(0),ExtractWord(3),style_pattern,style_fill,
	//		style_thickness,style_dash);
	}

	/**
	 *
	 * Process stretch box command.
	 *
	 */
	void StretchBox()
	{
	//	win->StretchBox(ExtractWord(0)+center_x,ExtractWord(3)+center_y,
	//		ExtractWord(6)+center_x,ExtractWord(9)+center_y,
	//		ExtractWord(12),style_pattern,style_fill,style_thickness,style_dash);
	}

	/**
	 *
	 * Process stretch thick command.
	 *
	 */
	void StretchThick()
	{
	//	win->StretchThick(ExtractWord(0)+center_x,ExtractWord(3)+center_y,
	//		ExtractWord(6)+center_x,ExtractWord(9)+center_y,
	//		ExtractWord(12),style_pattern,style_fill,style_thickness,style_dash);
	}

	/**
	 *
	 * Process stretch fill command.
	 *
	 */
	void StretchFill()
	{
	/*
		win->StretchFill(ExtractWord(0)+center_x,ExtractWord(3)+center_y,
			ExtractWord(6)+center_x,ExtractWord(9)+center_y,
			style_pattern,style_fill,style_thickness,style_dash);
	*/
	}

	/**
	 *
	 * Stop stretch.
	 *
	 */
	void StretchStp()
	{
	//	if ( !playback)
	//		win->StretchEnd();
	}

	/**
	 *
	 * Initialize for load memory by slot mode.
	 *
	 */
	void LoadCharSlot()
	{
		mem_conv = 2;
		InitTop (sLoadmem);
	}

	/**
	 * Print the logical plato screen.
	 */
	public void printScreen()
	{
		if	(null != parent_frame && parent_frame instanceof PortalFrame)
			((PortalFrame) parent_frame).doPrint();

	// todo get error code
		SendEcho(0);
	}

	/**
	 *
	 * Process copy text to file command.
	 *
	 */
	void PrintFile()
	{
	// todo implement if desired
	/*
		if ( !playback)
			win->PrintText ( sys_x,sys_y,sys_x+wrap_x,sys_y+wrap_y);
	*/
	}

	/**
	 *
	 * Copy area of text to file.
	 *
	 */
	void CopyFile()
	{
//~~ More commented out c++ code.		
	// todo implement if desired
	/*
		if ( !playback)
		{
			win->PrintText ( sys_x+ExtractWord ( 0),sys_y+ExtractWord ( 3),
				sys_x+ExtractWord ( 6),sys_y+ExtractWord ( 9));
		}
	*/
	}

	/**
	 *
	 * Do local print of specified file.
	 *
	 */
	private final void LocalPrint()
	{
	String	printobj = filename.toString();
	File	fileref = smartFile(swabFile(printobj));

		if	(PortalConsts.is_debugging) System.out.println("local print: "+fileref.getAbsolutePath());

		if	(!fileref.exists())
		{
			if	(PortalConsts.is_debugging) System.out.println("local print failed on fnf error!");
			SendEcho(1);
			return;
		}
		if	(PortalConsts.is_macintosh)
		{
			try
			{
			Process printproc;

				printproc = Runtime.getRuntime().exec("lp "+fileref.getAbsolutePath());
				if	(PortalConsts.is_debugging) System.out.println("executed lp command: "+printproc);
				SendEcho(0);
				if	(PortalConsts.is_debugging) System.out.println("executed lp result: "+printproc.waitFor());
			}
			catch (java.lang.Exception e1)
			{
				if	(PortalConsts.is_debugging) System.out.println("caught exception during lp");
				SendEcho(1);
			}
		}
		else
			SendEcho (1);
	}

	/**
	 * Encodes URL eliminating illegal characters.
	 */
	private final String urlEncode(String prefix,String url)
	{
	StringBuffer	sb = new StringBuffer(prefix);

		for (int i=0; i<url.length(); i++)
		{
		char	c = url.charAt(i);

			switch (c)
			{
				case	' ':
					sb.append("%20");
					break;
				case	'$':
					sb.append("%24");
					break;
				case	'&':
					sb.append("%26");
					break;
				case	'+':
					sb.append("%2b");
					break;
				case	',':
					sb.append("%2c");
					break;
				case	':':
					sb.append("%3a");
					break;
				case	';':
					sb.append("%3b");
					break;
				case	'=':
					sb.append("%3d");
					break;
				case	'?':
					sb.append("%3f");
					break;
				case	'@':
					sb.append("%40");
					break;
				case	'"':
					sb.append("%22");
					break;
				default:
					sb.append(c);
			} // end switch
		}
		return sb.toString();
	} // end encodeurl

	private final void LocalExecuteResult(int result)
	{
		transmit_buffer[0] = 0x1b;
		if	(result != 0)
			transmit_buffer[1] = (byte) '?';	// assume failure
		else
			transmit_buffer[1] = (byte) '/';
		if	(null != levelone_network)
			levelone_network.write(transmit_buffer,0,2);
	}

	/**
	 *
	 * Launch program specified by filename string.
	 *
	 */
	private final void LocalExecute()
	{
	/* on the macintosh, the aw code either does:
 	* /System/Library/Frameworks/Carbon.framework/Versions/Current/Support/LaunchCFMApp ZZZ
 	* e.g. LaunchCFMApp launchA7.apm
 	* OR
 	* open ZZZ
 	* e.g. open launchFL.app
 	*
 	* note that LaunchCFMApp needs the whole path to the executable (missing from tutor)
 	*/
		if (filename.length() > 0 && VerifyFilenameSumChk())
		{
		String	executable = filename.toString();
		File	fileref = smartFile(swabFile(executable));

			if	(executable.endsWith("launchA7.apm"))
			{
				local_os_cmdp = executable.indexOf("launchA7.apm");
				LocalOSCFMLaunch(false);
				return;
			}
/* -- this doesn't work!
			else if (executable.endsWith("launchFL.app"))
			{
				local_os_cmdp = executable.indexOf("launchFL.app");
				LocalOSFLLaunch();
				return;
			}
*/

			if	(fileref.exists())
				executable = fileref.getAbsolutePath();

			if	(PortalConsts.is_debugging)	System.out.println("local execute: "+executable);

			try
			{
				if (executable.startsWith("http:"))
				{
					if	(null != parent_frame && parent_frame instanceof PortalFrame)
						((PortalFrame) parent_frame).openURL(executable);
					LocalExecuteResult(0);
					return;
				}
				else
				{
				// just use exec for other platforms
					child_process = Runtime.getRuntime().exec(executable,null,
						new File(current_dir));
					return;
				}
			}
			catch (java.io.IOException e)
			{
				System.out.println("localexcute exception="+e);
				e.printStackTrace();
			}
			catch (java.lang.Exception e2)
			{
				System.out.println("localexcute exception="+e2);
				e2.printStackTrace();
			}
		}
		LocalExecuteResult(1);
	}

	/**
	 *
	 * Set the inhibit output flag for port.
	 *
	 */
	private final void InhibitOutput()
	{
		do_inhibit_output = true;
	}

	/**
	 *
	 * Clear inhibit output flag for port.
	 *
	 */
	private final void AllowOutput()
	{
		do_inhibit_output = false;
	}

	/**
	 *
	 * Set inhibit input flag for port.
	 *
	 */
	private final void InhibitInput()
	{
		do_inhibit_input = true;
	}

	/**
	 *
	 * Clear input inhibit flag for port.
	 *
	 */
	private final void AllowInput()
	{
		do_inhibit_input = false;
	}

	/**
	 *
	 * Set kermit keymetering rate.
	 *
	 */
	private final void SetUplineRate()
	{
		kerm_delay = ExtractWord ( 0);
	}

	/**
	 *
	 * Enter kermit mode.
	 *
	 */
	private final void KermitInit()
	{
		if (!playback)
		{
		String	kermit_dir;

			TrapEndInternal();
			if (disable_file_operations)
			{
				SendKey(KEY_STOP1);
				return;
			}
			data_proc = KermDataMode;
			if	(original_dir.equals(current_dir))
				kermit_dir = download_dir;
			else
				kermit_dir = current_dir;
			kermit_object = new LevelOneKermit(levelone_network,kermit_dir,tmp_dir,kerm_delay);
			kermit_dialog = new KermitDialog(parent_frame,"Kermit","File transfer in progress; please be patient!");
			kermit_dialog.show();
		}
	}

	/**
	 *
	 * Exit kermit mode.
	 *
	 */
	private final void KermitExit()
	{
		if (null != kermit_object)
		{
			kermit_object.destructor();
			kermit_object = null;
			kermit_dialog.dispose();
			kermit_dialog = null;
	//		win->netdata->EnableDetectAbort();
		}

		if (KermDataMode == data_proc)
			data_proc = L1PPDataMode;
	}

	/**
	 *
	 * Turn on flashing text cursor.
	 *
	 */
	void CursorOn()
	{
		if (!playback)
			cursor_on = true;
	}

	/**
	 *
	 * Turn off flashing text cursor.
	 *
	 */
	void CursorOff()
	{
		if (!playback)
			cursor_on = false;
	}

	/**
	 *
	 * Turn on flashing of text cursor.
	 *
	 */
	void TextFlashOn()
	{
		if (!playback)
			cursor_flashing = true;
	}

	/**
	 *
	 * Turn off flashing of text cursor.
	 *
	 */
	void TextFlashOff()
	{
		if (!playback)
			cursor_flashing = false;
	}

	/**
	 *
	 * Sets right margin for scrolling.
	 *
	 */
	void SetTextMargin()
	{
		text_right = ExtractWord (0);
	}

	/**
	 *
	 * Shifts display for delete of character at current location.
	 *
	 */
	void ScrollDelete()
	{
	int x1,y1,width,right,charsin;

		charsin = data[0]-32;
		right = xlatX(text_right+center_x);
		x1 = xlatX(current_x+center_x);
		y1 = xlatY(current_y+center_y)-CHARHEIGHT+1;
		width = right - x1 - CHARWIDTH*charsin;

		modeClipColor(false,false,false);

		levelone_offscreen.copyArea(
			x1+CHARWIDTH*charsin,y1,	// source
			width,CHARHEIGHT,			// width, height
			-CHARWIDTH*charsin,0);		// deltax, deltay

	Rectangle	fill = MakeRectLegal(right-CHARWIDTH*charsin,y1,right-1,y1+CHARHEIGHT-1);

		levelone_offscreen.setColor (bg_color);
		levelone_offscreen.fillRect(fill.x,fill.y,fill.width,fill.height);

		if (is_direct_draw)
		{
			levelone_graphics.copyArea(
				x1+CHARWIDTH*charsin,y1,	// source
				width,CHARHEIGHT,			// width, height
				-CHARWIDTH*charsin,0);		// deltax, deltay

			levelone_graphics.setColor (bg_color);

			levelone_graphics.fillRect(fill.x,fill.y,fill.width,fill.height);
		}
		else
			do_repaint = true;
	}

	/**
	 *
	 * Shifts display for insert of a character at current location.
	 *
	 */
	void ScrollInsert()
	{
	int x1,y1,width,right,charsout;

		charsout = data[0]-32;
		right = xlatX(text_right+center_x);
		x1 = xlatX(current_x+center_x);
		y1 = xlatY(current_y+center_y) - CHARHEIGHT + 1;
		width = right - x1 - CHARWIDTH*charsout;

		modeClipColor(false,false,false);
		levelone_offscreen.copyArea(
			x1,y1,	// source
			width,CHARHEIGHT,			// width, height
			CHARWIDTH*charsout,0);		// deltax, deltay

	Rectangle	fill = MakeRectLegal(x1,y1,x1+CHARWIDTH*charsout-1,y1+CHARHEIGHT-1);

		levelone_offscreen.setColor (bg_color);
		levelone_offscreen.fillRect(fill.x,fill.y,fill.width,fill.height);

		if (is_direct_draw)
		{
			levelone_graphics.copyArea(
				x1,y1,	// source
				width,CHARHEIGHT,			// width, height
				CHARWIDTH*charsout,0);		// deltax, deltay

			levelone_graphics.setColor (bg_color);

			levelone_graphics.fillRect(fill.x,fill.y,fill.width,fill.height);
		}
		else
			do_repaint = true;
	}

	/**
	 *
	 * Process scroll command.
	 *
	 */
	void Scroll()
	{
	int x1,y1;
	int chars,lines,deltay;

	// shifts area chars right and lines down UP deltay lines, or
	// DOWN deltay lines if deltay is negative
		x1 = xlatX(current_x+center_x);
		y1 = xlatY(current_y+center_y) - CHARHEIGHT + 1;
		chars = data[0]-32;
		lines = data[1]-32;
		deltay = data[2]-70;
		modeClipColor(false,false,false);

		levelone_offscreen.copyArea(
			x1,y1,
			CHARWIDTH*chars,CHARHEIGHT*lines,
			0,-CHARHEIGHT*deltay);
	Rectangle	fill;

		if (deltay < 0)
			fill = MakeRectLegal(x1,y1,x1+CHARWIDTH*chars,y1-CHARHEIGHT*deltay);
		else
			fill = MakeRectLegal(x1,y1+CHARHEIGHT*(lines-deltay),x1+CHARWIDTH*chars,y1+CHARHEIGHT*lines);

		levelone_offscreen.setColor (bg_color);
		levelone_offscreen.fillRect(fill.x,fill.y,fill.width,fill.height);

		if (is_direct_draw)
		{
			levelone_graphics.copyArea(
				x1,y1,
				CHARWIDTH*chars,CHARHEIGHT*lines,
				0,-CHARHEIGHT*deltay);


			levelone_graphics.setColor (bg_color);

			levelone_graphics.fillRect(fill.x,fill.y,fill.width,fill.height);
		}
		else
			do_repaint = true;
	}

	/**
	 *
	 * Set style of text cursor.
	 *
	 */

	void SetCursorStyle()
	{
		if (!playback)
			cursor_style = data[0]-32;
	}

	/**
	 *
	 * Setup text editing keymap.
	 *
	 */

	void TextEditKeys()
	{
		is_text_key_mode = true;
	}

	/**
	 *
	 * Setup travelling cursor and text editing keys.
	 *
	 */
	      
	void TextEditCursor()
	{
		is_text_key_mode = true;
	}

	/**
	 *
	 * TextEditClear()
	 *
	 * Clear text editing keys.
	 *
	 */

	void TextEditClear()
	{
		is_text_key_mode = false;
	}

	/**
	 *
	 * Setup text cursor window.
	 *
	 */

	void TextEditWindow()
	{
	}

	/**
	 *
	 * Enter word coordinate mode.
	 *
	 */

	private void selectWordCoordMode()
	{
		is_word_coord_mode = true;
	}

	/**
	 *
	 * StandardCoords()
	 *
	 * Enter normal coordinate mode.
	 *
	 */

	void StandardCoords()
	{
		is_word_coord_mode = false;
	}

	/**
	 *
	 * PaletteClear()
	 *
	 * Clear palette hold flag.
	 *
	 */

	void PaletteClear()
	{
		do_palette_hold = false;
	}

	/**
	 *
	 * Style Defaults()
	 *
	 * Return graphics styles to the defaults.
	 *
	 */

	void StyleDefaults()
	{
		checkPoly();
		ClearStyles();
	}

	/**
	 *
	 * TimedDelay()
	 *
	 * Delay the specified number of milliseconds.
	 *
	 */
	void TimedDelay()
	{
	int time = ExtractWord(0);

		if	(!playback && time > 0 && time < 2000)
		{
			end_parse = true;
			((LevelOnePanel)levelone_container).startDelay(time);
		}
	}

	/**
	 *
	 * The following functions encompass all rendering for the level 0 ASCII
	 * protocol, which encompasses mode 0 through mode 7.  The higher modes
	 * are for the L1PP.
	 *
	 */

	/**
	 *
	 * Mode 0 data (point mode).
	 *
	 */
	private final void PointData()
	{
		current_x = load_x;
		current_y = load_y;

		modeClipColor(true,false,true);

		levelone_offscreen.drawLine (
			xlatX (load_x+center_x),
			xlatY (load_y+center_y),
			xlatX (load_x+center_x),
			xlatY (load_y+center_y));

		if (is_direct_draw)
		{
			levelone_graphics.drawLine (
				xlatX (load_x+center_x),
				xlatY (load_y+center_y),
				xlatX (load_x+center_x),
				xlatY (load_y+center_y));
		}
		else
			levelone_container.repaint(xlatX(load_x+center_x),xlatY(load_y+center_y),1,1);
	}

	/**
	 *
	 * Mode 1 data (line mode).
	 *
	 */
	private final void LineData()
	{
		if (!first_line && OkDraw())
		{
			PlotLine ( current_x+center_x,current_y+center_y,load_x+center_x,
				load_y+center_y,screen_mode,style_pattern,style_fill,style_thickness,
				style_dash);
		}

		first_line = false;
		current_x = load_x;
		current_y = load_y;
	}

	/**
	 *
	 * Mode 2 data (load memory mode).
	 *
	 */
	private final void LoadMemData()
	{
	// only process charset data
		if (mem_conv != 0)
		{
			build_char [ build_count ++ ] = ExtractWord ( 0);

			if (build_count == 8)
			{
				CharFlip();
				LoadAddrChar (char_bitmap);
				build_count = 0;
				if (mem_conv == 2)
					mem_addr++;
				else
					mem_addr += 16;
			}
		}
	}

	/**
	 *
	 * Mode 3 data (alpha mode).
	 *
	 */
	private final void AlphaData()
	{
	int tempX = 0;

		if (text_dir == REVERSE)
		{
			tempX = current_x;
			XYAdjust (CHARWIDTH,0);
		}

		if (text_axis == HORIZONTAL && text_dir == FORWARD)
		{
			is_quick_text_on = true;
			quick_text_data[0] = data[0];
			quick_text_length = 1;
			quick_text_x = current_x;
			quick_text_y = current_y;
		}
		else
		{
			quick_text_data[0] = data[0];
			quick_text_length = 1;
			quick_text_x = current_x;
			quick_text_y = current_y;
			FlushText();
		}

		if (text_dir == REVERSE)
			current_x = tempX;

		XYAdjust(CHARWIDTH,0);
	}

	/**
	 *
	 * Mode 4 data (block mode).
	 *
	 */
	private final void BlockData()
	{
	byte	temp;

		current_x = old_x;
		current_y = old_y;

		clipPlotBlock(current_x+center_x,current_y+center_y,load_x+center_x,load_y+center_y);

	int lowx,lowy,sizex,sizey;

		if (load_x < current_x)
		{
			lowx = load_x;
			sizex = current_x - load_x + 1;
		}
		else
		{
			lowx = current_x;
			sizex = load_x - current_x + 1;
		}

		if (load_y < current_y)
		{
			lowy = current_y;
			sizey = current_y - load_y + 1;
		}
		else
		{
			lowy = load_y;
			sizey = load_y - current_y + 1;
		}

		modeClipColor(true,false,true);

		levelone_offscreen.fillRect (
			xlatX (lowx+center_x),
			xlatY (lowy+center_y),
			sizex,
			sizey);

		if (is_direct_draw)
		{
			levelone_graphics.fillRect (
				xlatX (lowx+center_x),
				xlatY (lowy+center_y),
				sizex,
				sizey);
		}
		else
		{
			levelone_container.repaint(
				xlatX (lowx+center_x),
				xlatY (lowy+center_y),
				sizex,
				sizey);
		}

		temp = text_size;
		XYAdjust(0,-15);
		text_size = temp;
	}

	/**
	 *
	 * Mode 5 (user mode).
	 *
	 */

	private final void UserData5()
	{
	}

	/**
	 *
	 * Mode 6 (user mode).
	 *
	 */

	private final void UserData6()
	{
	}

	/**
	 *
	 * Mode 7 (user mode).
	 *
	 */

	private final void UserData7()
	{
	}

	/**
	 *
	 * Process elliptical arc mode data.
	 *
	 */

	void EllipseArcData()
	{
		if (OkDraw())
			PlotArc ( ExtractWord ( 0),ExtractWord ( 3),ExtractWord ( 6),
				ExtractWord ( 9),current_x+center_x,current_y+center_y,
				screen_mode,style_pattern,style_thickness,style_fill);
	}

	/**
	 *
	 * Process circular arc mode data.
	 *
	 */

	void CircArcData()
	{
		if (OkDraw())
			PlotArc ( ExtractWord ( 0),ExtractWord ( 0),ExtractWord ( 3),
				ExtractWord ( 6),current_x+center_x,current_y+center_y,
				screen_mode,style_pattern,style_thickness,style_fill);
	}

	/**
	 *
	 * Process circle mode data.
	 *
	 */

	void CircleData()
	{
		if (OkDraw())
			PlotEllipse ( ExtractWord ( 0),ExtractWord ( 0),
				current_x+center_x,current_y+center_y,screen_mode,
				style_pattern,style_thickness,style_fill);
	}

	/**
	 *
	 * Process ellipse mode data.
	 *
	 */

	void EllipseData()
	{
		if (OkDraw())
			PlotEllipse ( ExtractWord ( 0),ExtractWord ( 3),
				current_x+center_x,current_y+center_y,screen_mode,
				style_pattern,style_thickness,style_fill);
	}

	/**
	 *
	 * Process box mode data.
	 *
	 */

	private void BoxData()
	{
		if (OkDraw())
			plotBox( old_x+center_x,old_y+center_y,load_x+center_x,load_y+center_y,
				ExtractWord ( 0),screen_mode,style_pattern,style_fill);
	}

	/**
	 *
	 * Process externals data.
	 *
	 */

	void ExternalData()
	{
	/*
		win->ExtData ( ExtractWord(0) >> 8);
		win->ExtData ( ExtractWord(0) & 0xff);
	*/
	}

	/**
	 *
	 * Process load filename mode data.
	 *
	 */

	void FileNameData()
	{
		if (filename.length() < 512)
			filename.append((char) data[0]);
	}

	/**
	 *
	 * Process local compute data.
	 *
	 */

	void LocalDataMode()
	{
	}

	/**
	 *
	 * Initialize to recieve local data.
	 *
	 */

	void LocalDataInit()
	{
		InitTop (sLocalData);
	}

	/**
	 *
	 * LocalCompute()
	 *
	 * Perform local compute.
	 *
	 */

	void LocalCompute()
	{
	/*
	BYTE	data[2];

		data[0] = 0x1b;
		data[1] = 0x3f;
		SendMessage(AfxGetApp()->m_pMainWnd->m_hWnd,WM_SENDDATA,2,(LPARAM) (LPVOID) data);
	*/
	}

	/**
	 *
	 * LocalFTP()
	 *
	 * Process local FTP command.
	 *
	 */
	void LocalFTP()
	{
		SendPixRes(0,4,0,0);
	// todo: use sun.net.ftp or ibm's alphabean for ftp
	/*
		if (win->config.terminaldisablefileoperations)
			SendPixRes(0,4,0);
		else if (0 == fnlth || !VerifyFilenameSumChk())
			SendPixRes(0,1,0);
		else if (!playback)
		{
			win->FTPClient (filename);
			end_parse = 1;
		}
	*/
	}

	/**
	 * Set local os failure rate, for simulations.
	 */
	public void setLocalFailureRate(double rate)
	{
		local_os_failure_rate = rate;
	}

	/**
	 *
	 * Process local OS command.

		The terminal sends in an echo response:
		0	success, or exists
		1	communications error (filename buffer checksum error)
		2	local error
		3	no such entity or permission denied
		4	file exists but not executable
		5	duplicate (create) or non-existent (delete) file
		6	syntax error or unknown error
		7	initiate KERMIT protocol transfer to get results text
	 *
	 */
	void LocalOS()
	{
	// java.io.File
	//		File(String path) File(String dir,String file) File(File directory,String filename)
	//
	//	exists() isFile() isDirectory() getName() getPath() getAbsolutePath() getCanonicalPath() length()
	//
	// renameTo(File target) to rename a file
	// delete() to delete file or empty directory
	// mkdir() to create directory
	// list() to list files in a directory
		if	(PortalConsts.is_debugging)	System.out.println("local os;"+filename.toString());
		if (0 == filename.length() || !VerifyFilenameSumChk())
			SendEcho(1);
		else if (filename.toString().startsWith("wtitle"))
		{
			if	(null != parent_frame && parent_frame instanceof PortalFrame)
				((PortalFrame)parent_frame).setTitle((LevelOnePanel) levelone_container,filename.toString().substring(7));
			SendEcho(0);
		}
		else if (disable_file_operations)
			SendEcho(3);
		else
		{
		int 	entries = ExtractWord(0);
		int 	entrysize = ExtractWord(3);
		String command = filename.toString().toLowerCase();

			if	(local_os_failure_rate > 0.0)
			{
			double	dice_roll = Math.random();

				if	(dice_roll < local_os_failure_rate)
				{
					if	(PortalConsts.is_debugging)	System.out.println("-local os- simulated fail");
					SendEcho(2);	// local failure
					return;
				}
			}

			local_os_entries = entries;
			local_os_entrysize = entrysize;
			local_os_options = 0;
			if (command.startsWith("list"))
			{
				local_os_cmdp = 4;
				LocalOSList();
			}
			else if (command.startsWith("getvols"))
			{
				local_os_cmdp = 7;
				LocalOSGetvols();
			}
			else if (command.startsWith("exist"))
			{
				local_os_cmdp = 5;
				LocalOSExist();
			}
			else if (command.startsWith("getdir"))
			{
				local_os_cmdp = 6;
				LocalOSGetdir();
			}
			else if (command.startsWith("setdir"))
			{
				local_os_cmdp = 6;
				LocalOSSetdir();
			}
			else if (command.startsWith("createdir"))
			{
				local_os_cmdp = 9;
				LocalOSCreatedir();
			}
			else if (command.startsWith("deletedir"))
			{
				local_os_cmdp = 9;
				LocalOSDeletedir();
			}
			else if (command.startsWith("delete"))
			{
				local_os_cmdp = 6;
				LocalOSDelete();
			}
			else if (command.startsWith("rename"))
			{
				local_os_cmdp = 6;
				LocalOSRename();
			}
			else if (command.startsWith("duplicate"))
			{
				local_os_cmdp = 9;
				LocalOSDuplicate(false);
			}
			else if (command.startsWith("move"))
			{
				local_os_cmdp = 4;
				LocalOSMove();
			}
			else if (command.startsWith("localdata"))
			{
				local_os_cmdp = 9;
				LocalOSLocaldata();
			}
			else if (command.startsWith("version"))
				LocalOSVersion();
			else if (command.startsWith("fontmetrics"))
				LocalOSFontmetrics();
			else if (command.startsWith("kill"))
				LocalOSKill();
			else if (command.startsWith("execute"))
			{
				local_os_cmdp = 8;
				LocalOSExecute();
			}
			else if (command.startsWith("openurl"))
			{
				local_os_cmdp = 8;
				LocalOSURL();
			}
			else if (command.startsWith("cfmlaunch"))
			{
				local_os_cmdp = 10;
				LocalOSCFMLaunch(true);
			}
			else if (command.startsWith("finderlaunch"))
			{
				local_os_cmdp = 13;
				LocalOSFinderLaunch();
			}
			else if (command.startsWith("watch"))
			{
				local_os_cmdp = 6;
				LocalOSWatch();
			}
			else if (command.startsWith("resetwatch"))
			{
				local_os_cmdp = 11;
				LocalOSWatchReset();
			}
			else
				SendEcho(6);

			try
			{
				if	(local_os_fos != null)
				{
					local_os_fos.close();
					local_os_fos = null;
				}
			}
			catch (Exception e1)
			{
			}
		}
	}

	/**
	 * Opens the file used to transmit local os data to system.
	 */
	void LOSOpenFile()
	{
		try
		{
			local_os_fos = new FileOutputStream(new File(tmp_dir,"localos.tmp"));
		}
		catch (Exception e1)
		{
		}
	}

	/**
	 * Pads out lines in the local os file with spaces as desired by tutor.
	 */
	void LOSPad (int codes,int words)
	{
		while (codes++ < 10*words)
		{
			try
			{
				local_os_fos.write(32);
			}
			catch (Exception e1)
			{
				return;
			}
		}
	}

	/**
	 * Returns novanet display codes for each ascii char.
	 */
	private int DisplayCodeCount (byte xchar)
	{
		if	(xchar > 64 && xchar < 91)	// uppercase alpha
			return 2;
		else if (xchar < 32 || xchar > 126)	// outside ascii
			return 0;
		else
		{
			switch (xchar)
			{
				case    '!':
				case    '\"':
				case    '#':
				case    '&':
				case    '\'':
				case    ':':
				case    '?':
				case    '@':
				case    '\\':
				case    '^':
				case    '_':
				case    '`':
				case    '{':
				case    '}':
				case    '~':
					return	2;
				case    '|':
					return	3;
			}
		}

		return 1;
	}

	/**
	 * Adds a string to the local os kermit spooling file.
	 */
	void LOSAddString(byte[] s,int o,int l,int words)
	{
	int	dc,tdc=0;

		for (int i=o;i<l;i++)
		{
			dc = DisplayCodeCount(s[i]);
			if	(dc != 0 && (dc + tdc) <= 10*words)
			{
				try
				{
					local_os_fos.write((int) s[i]);
				}
				catch (Exception e1)
				{
				}
				tdc += dc;
			}
		}
		LOSPad(tdc,words);
	}

	/**
	 * Adds a java string to the local os kermit spooling file.
	 */
	void LOSAddString(String s,int words)
	{
	byte[]	temp = s.getBytes();

		LOSAddString(temp,0,temp.length,words);
	}

	/**
	 *
	 *	Parse options flags.
	 *
	 */
	boolean LOSOptions (byte[] legal)
	{
	int 	local_cmdp = local_os_cmdp;
	int 	command_length = filename.length();
	int 	i,j,k;
	boolean	found;

		for (i=local_os_cmdp;i < command_length; i++)
		{
			if (filename.charAt(i) == ' ')
				continue;
			else if (filename.charAt(i) == '-')
			{
				for (j=i+1; j < command_length && filename.charAt(j) != ' '; j++)
				{
					for (k=0,found=false; k < legal.length; k++)
						if (legal[k] == filename.charAt(j))
						{
							local_os_options |= (1 << k);
							found = true;
							break;
						}
					if (!found)
						return true;
				}
				i = j-1;
			}
			else
				break;
		}

		local_os_cmdp = i;

		return false;
	}


	/**
	 * Gets next character from the filename buffer.
	 */
	char LOSChar()
	{
		if (local_os_cmdp >= filename.length())
			return (char) 0;
		else
			return filename.charAt(local_os_cmdp);
	}

	/**
	 * Gets next lexical element from the filename buffer.
	 */
	String LOSTag()
	{
	StringBuffer	result = new StringBuffer();
	int length = filename.length();
	char			sep;

	// skip leading spaces
		while (LOSChar() == ' ')
			local_os_cmdp++;

	// allow quotes to delimit strings
		if (LOSChar() == '\'' || LOSChar() == '\"')
		{
			sep = LOSChar();
			local_os_cmdp++;
		}
		else
			sep = ' ';

		while (LOSChar() != 0 && LOSChar() != sep)
		{
			result.append(LOSChar());
			local_os_cmdp++;
		}

	// if quoted, skip pointer past last quote
		if (sep != ' ' && LOSChar() != 0)
			local_os_cmdp++;

		if	(PortalConsts.is_debugging)	System.out.println("found tag="+result);

		return result.toString();
	}

	void LOSEmitFile (File fptr)
	{
	int             fdw;

		local_os_entries--;
	// calculate words for filename field
		fdw = local_os_entrysize;
	// "s1,d2,t4,v8,k10,r20"
		if	(0 != (local_os_options & 1))
			fdw--;
		if	(0 != (local_os_options & 2))
			fdw--;
		if	(0 != (local_os_options & 4))
			fdw--;
		if	(0 != (local_os_options & 8))
			fdw--;
		if	(0 != (local_os_options & 0x10))
			fdw--;
	// if space for it, emit filename
		if	(fdw > 0)
		{
			LOSAddString(fptr.getName(),fdw);
		}

	// size
		if	(0 != (local_os_options & 1))
		{
			LOSAddString(Long.toString(fptr.length()),1);
		}
	// date
		if	(0 != (local_os_options & 2))
		{
		long	lastmod = fptr.lastModified();
		Calendar	cal = Calendar.getInstance();
			cal.setTimeInMillis(lastmod);
		int		m = cal.get(Calendar.MONTH);
		int		d = cal.get(Calendar.DATE);
		int		yr = cal.get(Calendar.YEAR);

			LOSAddString(Integer.toString(m)+"/"+Integer.toString(d)+"/"+Integer.toString(yr),1);
		}
	// time
		if	(0 != (local_os_options & 4))
		{
		long	lastmod = fptr.lastModified();
		Calendar	cal = Calendar.getInstance();
			cal.setTimeInMillis(lastmod);
		int		hr = cal.get(Calendar.HOUR_OF_DAY);
		int		m = cal.get(Calendar.MINUTE);
		int		s = cal.get(Calendar.SECOND);

			LOSAddString(Integer.toString(hr)+"."+Integer.toString(m)+"."+Integer.toString(s),1);
		}
	// version
		if	(0 != (local_os_options & 8))
			LOSAddString("unknown",1);
	// kind
		if	(0 != (local_os_options & 0x10))
		{
			if	(fptr.isDirectory())
				LOSAddString("directory",1);
			else
				LOSAddString("file",1);
		}
	}

	void LOSListDirectory (File fptr)
	{
	File		flist[] = fptr.listFiles();

		for (int i=0; local_os_entries > 0 && i<flist.length; i++)
		{
			LOSEmitFile (flist[i]);

	//	if it is a directory and we are recursively listing, cd down, list it, and cd up again
			if	(flist[i].isDirectory() && (local_os_options & 0x20) != 0)
			{
				if	(local_os_entries > 0)
				{
					local_os_entries--;
					LOSAddString(".begin",local_os_entrysize);
				}

				LOSListDirectory (flist[i]);

				if	(local_os_entries > 0)
				{
					local_os_entries--;
					LOSAddString(".end",local_os_entrysize);
				}
			}
		}
	}

	/**
	 *
	 * Local list.
	 * list -s -d -t -v -k -r filespec
	 *
	 */
	private static final byte list_options[] = {(byte) 's',(byte) 'd',(byte) 't',(byte) 'v',(byte) 'k',(byte) 'r'};
	void LocalOSList()
	{
		if	(LOSOptions(list_options))
		{
			SendEcho(6);
			return;

		}
		LOSOpenFile();
		LOSListDirectory(new File(current_dir));
		SendEcho(7);
	}


	/**
	 *
	 * getvols -n -s -k
	 * use java.io.File.listRoots (java 1.2 only!)
	 * TODO
	 *
	 */
	private static final byte getvol_options[] = {(byte) 'n',(byte) 's',(byte) 'k'};
	void LocalOSGetvols()
	{
		if	(LOSOptions(getvol_options))
		{
			SendEcho(6);
			return;
		}

		SendEcho(6);
	}

	/**
	 *
	 * exist -e filespec
	 *
	 */
	private static final byte exist_options[] = {(byte) 'e',(byte) 'f'};
	void LocalOSExist()
	{
	// claim everything exists when using resource server:
		if (using_resource_server)
		{
			SendEcho(0);
			return;
		}

		if (LOSOptions(exist_options))
		{
			SendEcho(6);
			return;
		}

	String tag = swabFile(LOSTag());
	File file_handle = smartFile(tag);
	File file_handle_app = smartFile(tag+".app");

		if	(!file_handle.exists() && !file_handle_app.exists())
			SendEcho(3);	// does not exist
		else if ((local_os_options & 2) != 0)	// checking for non-empty directory
		{
		String[]	res = file_handle.list();

			if	(null == res || 0 == res.length)
				SendEcho(4);	// exists but not in requested format
			else
				SendEcho(0);	// exists and is non-empty directory
		}
		else
		{
			SendEcho(0);
		}
	}

	/**
	 *
	 * getdir
	 * return stored current directory
	 *
	 */
	void LocalOSGetdir()
	{
		LOSOpenFile();
		LOSAddString(current_dir,local_os_entrysize);
		SendEcho(7);
	}

	/**
	 *
	 * setdir -i -r filespec
	 * store new current directory
	 *
	 */
	private static final byte sdoptions[] = {(byte) 'i',(byte) 'r',(byte) 'j'};
	private String carbon_temp;

	void LocalOSSetdir()
	{
		if (LOSOptions(sdoptions))
		{
			SendEcho(6);
			return;
		}
		if (0 != (local_os_options & 1))	// -i
		{
		// sets to inter-application communication directory -
		// typically the OS temp directory.
			iac_saved_dir = current_dir;
			current_dir = System.getProperty("java.io.tmpdir");
		}
		else if (0 != (local_os_options & 2))	// -r
		{
		// restores directory to what was in effect before -i option
			current_dir = iac_saved_dir;
		}
		else if (0 != (local_os_options & 4))	// -j
		{
		// alternate inter-application directory
		// version, to use carbonized temp directory
		// under os x
		PortalFrame	pf = (PortalFrame) parent_frame;
		// need to know os version
		String	version = System.getProperty("os.version");	// 10.3.3 for example

			// as of 10.4 the following does not get the same
			// result as a carbon app, which is a drawback
			// in 10.5 beta this does get the carbon folder again! yay.
			current_dir = pf.getMacintoshTempFolder();
			if	(!version.startsWith("10.1") && !version.startsWith("10.2") && !version.startsWith("10.3") && !version.startsWith("10.4"))
				carbon_temp = current_dir;

			// this chunk gets the same folder as carbon would
			if	(null == carbon_temp)
			{
			String	prefix = "/private/tmp/";
			String	suffix;

				// what they call the directory varies with the os release.
				// uses /private/tmp/UID/Temporary Items
				if	(version.startsWith("10.1") || version.startsWith("10.2"))
					suffix = "Temporary Items";
				// uses /private/tmp/UID/TemporaryItems 10.3+
				else
					suffix = "TemporaryItems";

				// 10.4 is in /private/var/tmp/folders.UID/TemporaryItems
				if	(version.startsWith("10.4"))
					prefix = "/private/var/tmp/folders.";

				try
				{
				// the unix id command spits out the numeric user id
				// which makes up part of the tmp directory name
				// used by os x
				Process	child = Runtime.getRuntime().exec("id");
				// wait for it to get done and populate stdout with the data
					child.waitFor();
				// read the stdout information
				// stdout: uid=###(UNAME)...
				InputStream	is = child.getInputStream();
				byte[]	stdout = new byte[256];
				int		length;

					length = is.read(stdout);

				String	output = new String(stdout,0,length);
				int		lparen = output.indexOf('(');
				int		rparen = output.indexOf(')');
				String	uid = output.substring(4,lparen);
				String	uname = output.substring(lparen+1,rparen);

					if	(PortalConsts.is_debugging)	System.out.println("out="+output+" uid="+uid+"@"+uname);

				String	filepath;
					filepath = prefix+uid+"/"+suffix+"/";
				File	newdir = new File(filepath);

				// this directory does not exist until it gets used -
				// so we need to try to create it if it doesn't
				// exist because we may be immediately trying to write
				// files into it for another application's use.
					if	(PortalConsts.is_debugging)	System.out.println("setdir filepath="+filepath);
					if	(!newdir.exists())
					{
						if	(newdir.mkdirs())
							carbon_temp = filepath;
						else if (PortalConsts.is_debugging)
							System.out.println("mkdirs failed");
					}
					else
						carbon_temp = filepath;
				}
				catch (Exception e1)
				{
				// ignore, by default we already have the java temp directory
					if (PortalConsts.is_debugging) {e1.printStackTrace();}
				}
			}

			if	(null != carbon_temp)
				current_dir = carbon_temp;

			if	(PortalConsts.is_debugging)	System.out.println("setdir -j renders:"+current_dir);

		}
		else
		{
		String tag = swabFile(LOSTag());

			if (tag.length() == 0)
			{
				current_dir = original_dir;
			}
			else
			{
			File newdir = smartFile(tag);

				if	(newdir.isDirectory())
					current_dir = newdir.getAbsolutePath();
				else
				{
					SendEcho(2);
					return;
				}
			}
		}

		if	(PortalConsts.is_debugging)	System.out.println("currentdir="+current_dir);
		SendEcho(0);
	}

	/**
	 *
	 * createdir filespec
	 *
	 */
	void LocalOSCreatedir()
	{
	String	filename = swabFile(LOSTag());

		try
		{
		File	file = smartFile(filename);

			if	(file.mkdir())
				SendEcho(0);	// ok
			else
				SendEcho(2);
		}
		catch (Exception e1)
		{
			SendEcho(2);		// local error
		}
	}

	/**
	 * Deletes the contents of a directory.
	 */
	void deleteContents(File base)
		throws java.lang.SecurityException
	{
	String	list[] = base.list();

		for (int i=0; i<list.length; i++)
		{
		File	dfile = new File(base,list[i]);

			if	(dfile.isDirectory())
				deleteContents(dfile);

			if	(PortalConsts.is_debugging)	System.out.println("rdelete on: "+dfile.getAbsolutePath());
			dfile.delete();
		}
	}

	/**
	 *
	 * deletedir -r filespec
	 *
	 */
	private static final byte deletediro[] = {(byte) 'r'};
	void LocalOSDeletedir()
	{
		if (LOSOptions(deletediro))
		{
		// invalid option
			SendEcho(6);
			return;
		}

	String tag = swabFile(LOSTag());

		try
		{
		File	delfile = smartFile(tag);

			if	(!delfile.exists())
			{
				SendEcho(5);
				return;
			}
			if (0 != (local_os_options & 1) && delfile.isDirectory())	// -r
				deleteContents(delfile);

			if	(PortalConsts.is_debugging)	System.out.println("delete on: "+delfile.getAbsolutePath());

			if	(delfile.delete())
				SendEcho(0);	// ok
			else
				SendEcho(2);
		}
		catch (Exception e1)
		{
			SendEcho(2);		// local error
		}
	}

	/**
	 *
	 * delete filespec
	 *
	 */
	void LocalOSDelete()
	{
	String	filename = swabFile(LOSTag());

		try
		{
		File	delfile = smartFile(filename);

			if	(!delfile.exists())
			{
				SendEcho(5);
				return;
			}

			if	(delfile.delete())
				SendEcho(0);	// ok
			else
				SendEcho(2);	// failed
		}
		catch (Exception e1)
		{
			SendEcho(2);		// local error
		}
	}

	/**
	 *
	 * rename -o filespec newfilespec
	 *
	 */
	private static final byte rnoptions[] = {(byte) 'o'};
	void LocalOSRename()
	{
		if (LOSOptions(rnoptions))
		{
			SendEcho(6);
			return;
		}
	
	String	ofilename = LOSTag(),nfilename = LOSTag();

		try
		{
		File	orig = smartFile(ofilename);
		File	nfile = smartFile(nfilename);

			if	((local_os_options & 1) != 0)	// delete target first
				nfile.delete();

			if	(orig.renameTo(nfile))
				SendEcho(0);
			else
				SendEcho(2);	// local error
		}
		catch (Exception e1)
		{
			SendEcho(2);		// local error
		}
	}

	/**
	 *
	 * duplicate -o filespec newfilespec
	 *
	 */
	void LocalOSDuplicate(boolean is_move)
	{
		if (LOSOptions(rnoptions))
		{
			SendEcho(6);
			return;
		}
	String	ofilename = LOSTag(),nfilename = LOSTag();

		try
		{
		File	orig = smartFile(ofilename);
		File	nfile = smartFile(nfilename);
		byte	buffer[] = new byte[1024];

			if	((local_os_options & 1) != 0)	// delete target first
				nfile.delete();

		// loop thru and copy data between the files
		FileInputStream	fis = new FileInputStream(orig);
		FileOutputStream	fos = new FileOutputStream(nfile);
		int				ret;

			while ((ret = fis.read(buffer)) > 0)
				fos.write(buffer,0,ret);

			fos.close();
			fis.close();

			if	(is_move)
				orig.delete();	// if move operation, delete source file
			SendEcho(0);		// yay, all done!
		}
		catch (Exception e1)
		{
			SendEcho(2);		// local error
		}
	}

	/**
	 *
	 * move -o filespec newdirectory
	 *
	 */
	void LocalOSMove()
	{
	// copy it then delete the source file
		LocalOSDuplicate(true);
	}

	/**
	 *
	 * localdata directory
	 *
	 */
	void LocalOSLocaldata()
	{
	String	tag = swabFile(LOSTag());

		localdata_dir = null;
		try
		{
		File	test = smartFile(tag);

			if	(test.isDirectory())
			{
				localdata_dir = test.getAbsolutePath();
				SendEcho(0);
			}
			else
				SendEcho(3);
		}
		catch (Exception e1)
		{
			SendEcho(2);
		}

		if	(PortalConsts.is_debugging)	System.out.println("set localdata_dir="+localdata_dir);
	}

	/**
	 *
	 * Sends the portal's current version to the NovaNET system.
	 *
	 */
	void LocalOSVersion()
	{
		LOSOpenFile();
		LOSAddString(PortalConsts.short_version,local_os_entrysize);
		SendEcho(7);
	}

	/**
	 *
	 * fontmetrics
	 *
	 */
	void LOSFMFnt()
	{
		try
		{
		int i,width;

			LOSOpenFile();
			local_os_fos.write(local_fnt.getHeight());

			for (i=1; i<256; i++)
			{
				width = local_fnt.charWidth(i);
				local_os_fos.write (width);
			}
		
			local_os_fos.close();
			SendEcho(7);
		}
		catch (Exception e1)
		{
			SendEcho(2);
		}

	}

	void LOSFMFont()
	{
		try
		{
		int i,width;

			LOSOpenFile();
			local_os_fos.write(local_font_metrics.getHeight());

			for (i=1; i<256; i++)
			{
				width = local_font_metrics.charWidth(i);
				local_os_fos.write (width);
			}
		
			SendEcho(7);
		}
		catch (Exception e1)
		{
			SendEcho(2);
		}
	}

	/**
	 * Gets metrics for the local font.
	 */
	void LocalOSFontmetrics()
	{
		if (null != local_fnt)
		{
			LOSFMFnt();
		}
		else if (null != local_font_metrics && !PortalConsts.is_applet)
		{
			LOSFMFont();
		}
		else
			SendEcho(2);	// no local font active
	}

	/**
	 * Command to kill launched child process.
	 */
	void LocalOSKill()
	{
		if	(null != child_process)
		{
			child_process.destroy();
			SendEcho(0);
		}
		else
			SendEcho(2);
	}

	/**
	 * Command to spawn new child process.
	 */
	void LocalOSExecute()
	{
		try
		{
		String	excname = filename.substring(local_os_cmdp);
		File	fileref = smartFile(swabFile(excname));

			if	(fileref.exists())
				excname = fileref.getAbsolutePath();

			if	(PortalConsts.is_debugging)	System.out.println("excname="+excname);
			child_process = Runtime.getRuntime().exec(
				excname,
				null,
				new File(current_dir));
			SendEcho(0);
		}
		catch (java.io.IOException e1)
		{
			SendEcho(2);
		}
	}

	/**
	 * Command to spawn new CFM child process (for macintosh).
	 */
	void LocalOSCFMLaunch(boolean do_echo)
	{
		try
		{
		String	excname = filename.substring(local_os_cmdp);
		File	fileref = smartFile(swabFile(excname));
		String[]	cmdarray = new String[2];

			if	(fileref.exists())
				excname = fileref.getAbsolutePath();

			if	(PortalConsts.is_debugging) System.out.println("excname="+excname+" dir="+current_dir);
			cmdarray[0] = "/System/Library/Frameworks/Carbon.framework/Versions/Current/Support/LaunchCFMApp";
			cmdarray[1] = excname;

			child_process = Runtime.getRuntime().exec(
				cmdarray,
				null,
				new File(current_dir));
			if	(do_echo)
				SendEcho(0);
		}
		catch (java.io.IOException e1)
		{
			if	(do_echo)
				SendEcho(2);
			else
				LocalExecuteResult(1);
		}
	}

	/**
	 * 	 when launching launchFL.app
	 */
	void LocalOSFLLaunch()
	{
		if	(PortalConsts.is_debugging) System.out.println("LocalOSFLLaunch");

		try
		{
		String	excname = filename.substring(local_os_cmdp);
		File	fileref = smartFile(swabFile(excname));
		String[]	cmdarray = new String[1];

			if	(fileref.exists())
				excname = fileref.getAbsolutePath();

			cmdarray[0] = excname+"/Contents/MacOS/launchFL";
			if	(PortalConsts.is_debugging) System.out.println("exists: "+fileref.exists()+" excname="+cmdarray[0]);

			child_process = Runtime.getRuntime().exec(
				cmdarray,
				null,
				new File(current_dir));
		}
		catch (java.io.IOException e1)
		{
			if	(PortalConsts.is_debugging) System.out.println("LocalOSFLLaunch exeception: "+e1);
			LocalExecuteResult(1);
		}
	}


	/**
	 * Command to spawn new child process (for macintosh).
	 */
	void LocalOSFinderLaunch()
	{
		try
		{
		String	executable = filename.substring(local_os_cmdp);
		// execute using finder via openurl interface for macintosh
		String	urlstring;
		File	filespec;

		// silently add bundle suffix to filename if it doesn't exist
			filespec = smartFile(swabFile(executable));
			if	(!filespec.exists())
				filespec = smartFile(swabFile(executable)+".app");

		// encode using proper URL character encodings
			urlstring = urlEncode("file://",filespec.getAbsolutePath());
			if	(PortalConsts.is_debugging)	System.out.println("url encode: "+urlstring);

		// get finder to try and execute it
			if	(null != parent_frame && parent_frame instanceof PortalFrame)
				((PortalFrame) parent_frame).openURL(urlstring);
			SendEcho(0);
		}
		catch (java.io.IOException e1)
		{
			SendEcho(2);
		}
	}

	/**
	 * Command to watch a directory for changes.
	 */
	Vector watch_data = new Vector();
	javax.swing.Timer watch_timer;

	class WatchInfo
	{
	// File we are watching
		File	filespec;
	// for a directory
		String	listdata[];
	// for a file
		long	lastmodified;
	}

	/**
	 * Resets watch conditions to null.
	 */
	void LocalOSWatchReset()
	{
		watch_data.clear();
		if	(null != watch_timer)
			watch_timer.stop();
	}

	/**
	 * Swing callback to check file modifications.
	 */
	public void actionPerformed(ActionEvent e)
	{
		LocalOSWatchTimer();
	}

	/**
	 * Command to watch a directory for changes.
	 */
	void LocalOSWatch()
	{
		try
		{
		String	watcher = filename.substring(local_os_cmdp);
		File	filespec;

			if	(watch_data.size() > 31)
			{
				SendEcho(2);	// too many watches going on
				return;
			}
			filespec = smartFile(swabFile(watcher));

//	ignore duplicates
			for (int i=0;i<watch_data.size();i++)
			{
			WatchInfo	wit = (WatchInfo) watch_data.get(i);

				if	(wit.filespec.equals(filespec))
				{
					SendEcho(0);
					return;
				}
			}

		WatchInfo	wi = new WatchInfo();

			wi.filespec = filespec;
			if	(!filespec.exists())
				wi.lastmodified = 0;
			else if	(filespec.isDirectory())
				wi.listdata = filespec.list();
			else
				wi.lastmodified = filespec.lastModified();
			watch_data.add(wi);

			if	(null == watch_timer)
				watch_timer = new javax.swing.Timer(500, this);
			watch_timer.start();

			SendEcho(0);
		}
		catch (java.lang.Exception e1)
		{
			SendEcho(2);
		}
	}

	/**
	 * Periodic timer function to do the actual file checks.
	 */
	void LocalOSWatchTimer()
	{
	boolean	change = false;

		for (int i=0;i<watch_data.size() && !change; i++)
		{
		WatchInfo	wi = (WatchInfo) watch_data.get(i);
		File	check = wi.filespec;

			if (check.isDirectory())
			{
			String[]	zlist = check.list();

				if	(zlist.length > 0)
					change = true;
			}
			else
			{
				if	(!check.exists())
				{
					if	(wi.lastmodified != 0)
						change = true;	//	file was deleted
					wi.lastmodified = 0;
				}
				else
				{
					if	(check.lastModified() != wi.lastmodified)	// mod
						change = true;
					wi.lastmodified = check.lastModified();
				}
			}
			if	(change && PortalConsts.is_debugging) System.out.println("change detected on: "+wi.filespec);
		}

		if	(change)
			SendKey(KEY_ANS);

		if	(0 == watch_data.size())
			watch_timer.stop();
	}

	/**
	 * Command to open specified URL.
	 */
	void LocalOSURL()
	{
		try
		{
			if (null != parent_frame && parent_frame instanceof PortalFrame)
				((PortalFrame) parent_frame).openURL(filename.substring(local_os_cmdp));
			SendEcho(0);
		}
		catch (java.io.IOException e1)
		{
			SendEcho(2);
		}
	}

	/**
	 *
	 * ExtMode()
	 *
	 * Process enter ascii externals mode command.
	 *
	 */
	void ExtMode()
	{
		InitTop (sExt2);
	}

	/**
	 *
	 * Process sound setplay/setrec command.
	 *
	 */
	void SoundOne()
	{
		if	(PortalConsts.is_debugging)	System.out.println("soundone!!");

		if (disable_file_operations && !using_resource_server)
		{
			SendEcho(1);
			return;
		}
		else if (filename.length()==0 || !VerifyFilenameSumChk())	// communications error
		{
			SendEcho(2);
			return;
		}
		else if ((ExtractWord(0) & 7) != 0)	// we don't do recordings; quicktime can, but we don't care
		{
			SendEcho(1);
			return;
		}

		if (using_resource_server)
		{
			sound_url = CreateURL();
			SendEcho(0);
		}
		else
		{
			sound_file = CreateFile();
			if (sound_file.exists())
				SendEcho(0);
			else
				SendEcho(1);
		}
	}

	/**
	 *
	 * Process set volume command.
	 *
	 */
	void SoundTwo()
	{
	// we don't do volume settings
	}

	/**
	 *
	 * Process set start time command.
	 *
	 */
	void SoundThree()
	{
	// we don't do start time
	}

	/**
	 *
	 * Process set stop time command.
	 *
	 */
	void SoundFour()
	{
	// we don't do stop time
	}

	/**
	 *
	 * Process start record/playback command.
	 *
	 */
	void SoundFive()
	{
		if	(PortalConsts.is_debugging)	System.out.println("soundfive; urs="+using_resource_server);
		if (using_resource_server && jmf_player.hasJMF())
			jmf_player.soundStart(sound_url);
		else
			quicktime_player.soundStart(sound_url,sound_file);
	}

	/**
	 *
	 * Process stop command.
	 *
	 */
	void SoundSix()
	{
		jmf_player.soundStop();
		quicktime_player.soundStop();
	}

	/**
	 *
	 * Process pause command.
	 *
	 */
	void SoundSeven()
	{
		jmf_player.soundPause();
		quicktime_player.soundPause();
	}

	/**
	 *
	 * Process resume command.
	 *
	 */
	void SoundEight()
	{
		jmf_player.soundResume();
		quicktime_player.soundResume();
	}

	/**
	 *
	 * Process report capabilities command.
	 *
	 */
	void SoundNine()
	{
		SendEcho(0);	// no record capability
	}

	/**
	 *
	 * Process reserve channel command.
	 *
	 */
	void SoundTen()
	{
		SendEcho(0);
	}

	/**
	 *
	 * Process release channel command.
	 *
	 */
	void SoundEleven()
	{
	}

	/**
	 *
	 * Process report format command.
	 *
	 */
	void SoundTwelve()
	{
		SendEcho(0);
	}

	/**
	 *
	 * Process enter test mode command.
	 *
	 */
	void TestInit()
	{
		data_proc = TestDataMode;
		testseq = 32;
	}

	/**
	 *
	 * Process auto-login initiate command.
	 *
	 */
	private void AutoLogin()
	{
		DoAutoLogin();
	}

	/**
	 *
	 * Disable multi-click touch keys.
	 *
	 */

	void MltClkDisable()
	{
		is_multi_clickable = false;
	}

	/**
	 *
	 * Enable multi-click touch keys.
	 *
	 */

	void MltClkEnable()
	{
		is_multi_clickable = true;
	}

	/**
	 *
	 * Select a local font for m0.
	 *
	 */

	private static final int sans_sizes[] = {9,16,20,25,28,33,37,48};
	private static final int cent_sizes[] = {16,20,25,28,33,37,48};

	void FontSelect()
	{
	int 	fontspec = ExtractLWord(0);
	int 	fontfamily,fontsize;
	String fontname;
	String resource_prefix = null;
	String resource_name = null;
	int 	i;
	boolean	fontbold,fontitalic;

		DropLocalFont();

		fontfamily = (fontspec >> 12) & 63;
		fontsize = (fontspec & 127);
		fontbold = (fontspec & 0x800) != 0;
		fontitalic = (fontspec & 0x400) != 0;

		if (0 == fontfamily || 0 == fontsize)
		{
			DropLocalFont();
			return;
		}

	// exit if already using this font
		if (fontfamily == local_ff &&
			fontsize == local_fs &&
			fontbold == local_fb &&
			fontitalic == local_fi)
		{
			if	(PortalConsts.is_debugging)	System.out.println("WARNING: font select inefficiency");
			return;
		}

	// store the newly selected font
		local_ff = fontfamily;
		local_fs = fontsize;
		local_fb = fontbold;
		local_fi = fontitalic;

		switch (fontfamily)
		{
			case 20:		// times
				fontname = "Serif";
				break;
			case 21:		// helvetica
				fontname = "SansSerif";
				break;
			case 22:		// courier
				fontname = "Monospaced";
				break;
			case 24:		// novanet sans (sansserif)
				if (fontbold && fontitalic)
					resource_prefix = "SANBI00";
				else if (fontbold)
					resource_prefix = "SANSB00";
				else if (fontitalic)
					resource_prefix = "SANSI00";
				else
					resource_prefix = "SANSN00";
				for (i=0; i<sans_sizes.length; i++)
					if (sans_sizes[i] == fontsize)
						resource_name = resource_prefix+(i+1)+".FNT";

				fontname = "SansSerif";
				fontsize = (2*fontsize)/3;
				break;
			case 25:		// novanet sans mono (monospaced)
				if (fontbold && fontitalic)
					resource_prefix = "SANMBI0";
				else if (fontbold)
					resource_prefix = "SANSMB0";
				else if (fontitalic)
					resource_prefix = "SANSMI0";
				else
					resource_prefix = "SANSM00";
				for (i=0; i<sans_sizes.length; i++)
					if (sans_sizes[i] == fontsize)
						resource_name = resource_prefix
						+(i+1)+".FNT";
				fontname = "Monospaced";
				fontsize = (2*fontsize)/3;
				break;
			case 26:		// novanet century (serif)
				if (fontbold && fontitalic)
					resource_prefix = "CENTBI0";
				else if (fontbold)
					resource_prefix = "CENTB00";
				else if (fontitalic)
					resource_prefix = "CENTI00";
				else
					resource_prefix = "CENT000";
				for (i=0; i<cent_sizes.length; i++)
					if (cent_sizes[i] == fontsize)
						resource_name = resource_prefix+(i+1)+".FNT";
				fontname = "Serif";
				fontsize = (2*fontsize)/3;
				break;
			default:
				if	(PortalConsts.is_debugging)	System.out.println("selected invalid font = "+fontfamily);
				DropLocalFont();
				return;
		}

	// First, try to use FNT stored in our JAR file --
		if (resource_name != null)
		{
			try
			{
			InputStream	is = getClass().getResourceAsStream("/com/nn/fonts/"+resource_name);

				if (null != is)
				{
					local_fnt = new WindowsFNT(is);
					font_height = local_fnt.getHeight();
					return;
				}
				else
				{
					if	(PortalConsts.is_debugging)	System.out.println("ERROR - FNT NOT FOUND");
				}
			}
			catch (java.lang.Exception ioe1)
			{
				if	(PortalConsts.is_debugging)
				{
					System.out.println("ERROR - FNT IO Exception"+ioe1);
					ioe1.printStackTrace();
				}
			}
		}

	// Last, try to use Java TrueType --
	int 	java_style2;

		if (fontbold && fontitalic)
			java_style2 = Font.BOLD|Font.ITALIC;
		else if (fontbold)
			java_style2 = Font.BOLD;
		else if (fontitalic)
			java_style2 = Font.ITALIC;
		else
			java_style2 = Font.PLAIN;

		local_font = new Font(fontname,java_style2,fontsize*85/100);
		local_font_metrics = levelone_offscreen.getFontMetrics(local_font);

		if (is_direct_draw)
			levelone_graphics.setFont(local_font);
		levelone_offscreen.setFont(local_font);
		local_font_descent = local_font_metrics.getDescent();
		font_height = local_font_metrics.getHeight();
	}

	/**
	 *
	 * Return local font checksum.
	 *
	 */

	void FontChecksum()
	{
		if (null == local_font && null == local_fnt)
		{
			if	(PortalConsts.is_debugging)	System.out.println("fontchecksum with null localfont");
			SendPixRes(0,0,0777777,0);
		}
		else
		{
		int 		q,crc = 0;
		int 		i,c;

			for (i=0; i<256; i++)
			{
				if (null != local_fnt)
					c = local_fnt.charWidth(i);
				else
					c = local_font_metrics.charWidth(i);
				q = (crc ^ c) & 017;
				crc = (crc >> 4) ^ (q*010201);
				q = (crc ^ (c>>4)) & 017;
				crc = (crc >> 4) ^ (q*010201);
			}

			SendPixRes (0,crc & 0xffff,0,0);
		}
	}

	/**
	 *
	 * ExtData
	 *
	 * Protocol interpreter while externals mode is in effect.
	 *
	 */

	void ExtData()
	{
	/*
		win->ExtData (data[0]);
	*/
	}

	/**
	 *
	 * ClearStyles()
	 *
	 * Reset styles to their defaults.
	 *
	 */

	void ClearStyles()
	{
		style_pattern = 1;
		style_thickness = 1;
		style_dash = 0;
		style_fill = 0;
		style_cap = 0;
		style_join = 2;
	}

	/**
	 * Initialize various things for plato mode.
	 */
	void initPlatoMode()
	{
	   	center_x = ( terminalWidth - 512) >> 1;
		center_y = ( terminalHeight - 512) >> 1;

		wrap_x = terminalWidth - 1 - (center_x<<1);
		wrap_y = terminalHeight - 1 - (center_y<<1);
		sys_x = center_x;
		sys_y = center_y;

		ClipSet (sys_x, sys_y, wrap_x+sys_x, wrap_y+sys_y,true);
	}

	/**
	 * Initialize various things for TTY mode.
	 */
	private void initTTYMode()
	{   
		center_x = 0;
		center_y = 0;
		sys_x = 0;
		sys_y = 0;
		wrap_x = terminalWidth - 1;
		wrap_y = terminalWidth - 1;
		
		ClipSet (sys_x, sys_y, wrap_x+sys_x, wrap_y+sys_y, true);
	}

	/**
	 *
	 *	Return true if character in specified charset should be styled.
	 *
	 */

	private boolean OkStyle (int character,int charset)
	{
		character = character + 32;
		
		if (charset > 2)
			return false;
			
		if (charset == 1)
			character += 0x60;

		switch (character)
		{
			case ' ':
			case '!':
			case ',':
			case '.':
			case ':':
			case ';':
			case '?':
			case 0x22:
			case 0x27:
			case ' ' + 0x60:
			case 0x23+ 0x60:
			case 0x3f+ 0x60:
			case 0x41+ 0x60:
			case 0x46+ 0x60:
			case 0x47+ 0x60:
			case 0x48+ 0x60:
			case 0x49+ 0x60:
				return false;
		}

		return true;
	}

	private int[] postchar = new int[16];

	/**
	 *
	 *	Apply styles to text bitmap.
	 *
	 */

	final void StyleText (int charset,int charnum,int style)
	{
	int i;
		
		if (charset < 2)
		{
			if (1 == charset)
				charnum += 96;
			if (charnum >= 0 && charnum < 144)
			{
				for	(i=0; i<16; i++)
					postchar[i] = chardefs[144*i+charnum];
			}
			else
			{
				for (i=0; i<16; i++)
					postchar[i] = 0xff;
			}
		}
		else
		{
			if (charnum >= 0 && charnum < 64)
			{
				for (i=0; i<16; i++)
					postchar[i] = m2m3[charnum+384*i + (charset-2)*64];
			}
			else
			{
				for (i=0; i<16; i++)
					postchar[i] = 0xff;
			}
		}

	// exit if text is not styled:
		if (0 == style)
			return;

	// otherwise go through and apply styles:
		if ( (style & 1) != 0)
		{		/* thicken horizontal */
			for ( i = 0 ; i < 16 ; i++)
				postchar[i] |= ((postchar[i] >> 1) & 0x7f);
		}

		if ( (style & 2) != 0)
		{		/* thicken vertical */
			for ( i = 0 ; i < 16-1 ; i++)
				postchar[i] |= postchar[i+1];
		}

		if ( (style & 4) != 0)
		{		/* slant */
			for ( i = 0 ; i < 16/3 ; i++)
				postchar[i] = ((postchar[i] >> 1) & 0x7f);
			for ( i = 2*16/3 ; i < 16 ; i++)
			{
				if ((postchar[i] & 0x80) != 0)
					postchar[i] = ((postchar[i] << 1) & 0xfe) | 0x80;
				else
					postchar[i] = ((postchar[i] << 1) & 0xfe);
			}
		}

	/* underlines */
		if ( (style & 16) != 0 || ((style & 8) != 0 && OkStyle (charnum,charset)))
		{
			postchar[16-1] = 0xff;
		}

	/* strikeouts */
		if ( (style & 64) != 0 || ((style & 32) != 0 && OkStyle (charnum,charset)))
		{
			postchar[16/2] = 0xff;
		}
	/* shadow */
		if ( (style & 128) != 0)
		{		/* sparse */
			for ( i = 0; i < 16-1 ; i++)
			{
				if ( (i & 1) == 0)
					postchar[i] &= 0xaa;
				else
					postchar[i] &= 0x55;
			}
		}

		if ( (style & 256) != 0)
	/* superscript mark */
		{
			postchar[0] |= 0xff;
			for (i=0; i<16; i++)
				postchar[i] |= 1;
		}

		if ( (style & 512) != 0)
	/* subscript mark */
		{
			postchar[16-1] |= 0xff;
			for (i=0; i<16; i++)
				postchar[i] |= 1;
		}

		if ( (style & 1024) != 0)
		{
	/* char invert */
			for (i=0; i<16; i++)
				postchar[i] = ~postchar[i];
		}
	}

	int draw_string_pix[] = new int[QUICK_LENGTH*4*CHARWIDTH*CHARHEIGHT];
	Object	mis_vector[] = new Object[QUICK_LENGTH];
	Object	img_vector[] = new Object[QUICK_LENGTH];


	/**
	 *
	 * Renders string in the NovaNET system font.
	 *
	 */
	final void drawString (
		byte[] string,
		int stringlength,
		int forecolor,
		int backcolor,
		int plotx,
		int ploty,
		int mode,
		int charset,
		int size,
		int style)
	{
	int 			pix[] = draw_string_pix;
	int 			x,y;
	int 			relsize;
	int 			index = 0;
	int 			pixvalue;

		if (size != 0)
			relsize = 2;
		else
			relsize = 1;

	// don't try and draw characters that begin offscreen -- java
	// does kinda weird things with negative coordinates...
		if (ploty-16*relsize+1 < 0)
			return;

	// allocate memory to hold the pixel values
//		pix = new int[relsize*CHARWIDTH*stringlength*relsize*CHARHEIGHT];

		for (int i=0; i<stringlength; i++)
		{
			StyleText(charset,string[i]-32,style);

			for (y=0; y<CHARHEIGHT; y++)
			{
			int 	strip;

				strip = postchar[y];

				for (x=0; x<8; x++)
				{
					if (((strip >> (7-x)) & 1) != 0)
						pixvalue = forecolor;
					else
						pixvalue = backcolor;

					if (size != 0)
					{
						pix[16*i+16*stringlength*2*y+2*x] = pixvalue;
						pix[16*i+16*stringlength*2*y+2*x+1] = pixvalue;
						pix[16*i+16*stringlength*(2*y+1)+2*x] = pixvalue;
						pix[16*i+16*stringlength*(2*y+1)+2*x+1] = pixvalue;
					}
					else
						pix[8*i+8*stringlength*y+x] = pixvalue;
				}
			}
		}

	boolean	cacheable = false;

/* -- this doesn't work on the macintosh
		if	(relsize == 1 && stringlength == 1)
			cacheable = true;
*/

	int					mvi = stringlength;
	Object				o = null;
	MemoryImageSource	mis;
	Image				img;

		if	(cacheable)
			o = mis_vector[mvi];

		if	(o == null)
		{
			mis = new MemoryImageSource (
					8*stringlength*relsize,
					16*relsize,
					pix,
					0,
					8*stringlength*relsize);
			img = levelone_container.createImage(mis);

			if	(cacheable)
			{
				mis.setAnimated(true);
				mis_vector[mvi] = mis;
				img_vector[mvi] = img;
			}
		}
		else
		{
			mis = (MemoryImageSource) o;
			img = (Image) img_vector[mvi];
			mis.newPixels(0,0,8*stringlength*relsize,16*relsize);
		}

		modeClipColor (false,false,false);
		levelone_offscreen.drawImage (img,plotx,ploty-16*relsize+1,null);

		if (is_direct_draw)
			levelone_base_graphics.drawImage (img,plotx,ploty-16*relsize+1,null);
		else
		{
			if	(false)
				do_repaint = true;
			else
				levelone_container.repaint(plotx,ploty-16*relsize+1,
					plotx+8*stringlength*relsize,ploty-16*relsize+1+16*relsize);
		}
	}


	/**
	 *
	 * Plot any text string currently being held.
	 *
	 */
	final void FlushSystemFont()
	{
	int forecolor=0,backcolor=0;

		switch (screen_mode)
		{
			case SCWRITE:
				forecolor = (255 << 24) | fg_color.getRGB();
				break;
			case SCERASE:
				forecolor = (255 << 24) | bg_color.getRGB();
				break;
			case SCREWRITE:
				forecolor = (255 << 24) | fg_color.getRGB();
				backcolor = (255 << 24) | bg_color.getRGB();
				break;
			case SCINVERSE:
				backcolor = (255 << 24) | fg_color.getRGB();
				forecolor = (255 << 24) | bg_color.getRGB();
				break;
		}

		drawString (
			quick_text_data,
			quick_text_length,
			forecolor,
			backcolor,
			xlatX (quick_text_x+center_x),
			xlatY (quick_text_y+center_y),
			screen_mode,
			text_charset,
			text_size,
			text_style);
	}

	final void FlushLocalFnt()
	{
	int forecolor=0,backcolor=0;

		modeClipColor(false,false,false);
		switch (screen_mode)
		{
			case SCWRITE:
				forecolor = (255 << 24) | fg_color.getRGB();
				break;
			case SCERASE:
				forecolor = (255 << 24) | bg_color.getRGB();
				break;
			case SCREWRITE:
				forecolor = (255 << 24) | fg_color.getRGB();
				backcolor = (255 << 24) | bg_color.getRGB();
				break;
			case SCINVERSE:
				backcolor = (255 << 24) | fg_color.getRGB();
				forecolor = (255 << 24) | bg_color.getRGB();
				break;
		}

		if (is_direct_draw)
		{
			current_x = quick_text_x + local_fnt.drawString (
				quick_text_data,
				quick_text_length,
				forecolor,
				backcolor,
				xlatX (quick_text_x+center_x),
				xlatY (quick_text_y+center_y),
				levelone_container,
				levelone_base_graphics,
				levelone_offscreen,
				clip_rect);
		}
		else
		{
			do_repaint = true;
			current_x = quick_text_x + local_fnt.drawString (
				quick_text_data,
				quick_text_length,
				forecolor,
				backcolor,
				xlatX (quick_text_x+center_x),
				xlatY (quick_text_y+center_y),
				levelone_container,
				null,
				levelone_offscreen,
				clip_rect);
		}
	}

	final void FlushLocalFont()
	{
	int x = xlatX (quick_text_x+center_x);
	int y = xlatY (quick_text_y+center_y) - local_font_descent;

		modeClipColor(true,true,true);
		if (is_direct_draw)
			levelone_graphics.drawBytes(quick_text_data,0,quick_text_length,x,y);
		else
			do_repaint = true;
		levelone_offscreen.drawBytes(quick_text_data,0,quick_text_length,x,y);

	// update screen x location
		current_x = quick_text_x + local_font_metrics.bytesWidth(quick_text_data,0,quick_text_length);
	}

	final void FlushText()
	{
		is_quick_text_on = false;
		if (quick_text_length > 0)
		{
			if	(text_charset < 2)
				clipFlushText();

			if (text_charset > 0 || text_size != 0 || text_style != 0)
				FlushSystemFont();
			else if (null != local_fnt)
				FlushLocalFnt();
			else if (null != local_font)
				FlushLocalFont();
			else
				FlushSystemFont();

			quick_text_length = 0;
		}
	}

	final void DropLocalFont()
	{
		local_ff = 0;
		local_fs = 0;
		local_fb = false;
		local_fi = false;
		local_fnt = null;
		local_font = null;
		local_font_metrics = null;
		font_height = 16;
	}

	/**
	 * Plot polygon that is currently being held.
	 */
	protected void checkPoly()
	{
		if (null != polygon)
		{
			if (0 == style_pattern)
				ModeColor (InvertMode (screen_mode));
			else
				ModeColor (screen_mode);

			modeClipColor(true,true,false);

			if (style_fill != 0)
			{
				if (is_direct_draw)
				{
					levelone_graphics.fillPolygon (polygon);
					levelone_graphics.drawPolygon (polygon);
				}
				else
					do_repaint = true;
				levelone_offscreen.fillPolygon (polygon);
				levelone_offscreen.drawPolygon (polygon);
			}
			else
			{
				if (is_direct_draw)
					levelone_graphics.drawPolyline(polygon.xpoints,polygon.ypoints,polygon.npoints);
				else
					do_repaint = true;
				levelone_offscreen.drawPolyline(polygon.xpoints,polygon.ypoints,polygon.npoints);
			}
			polygon = null;
		}
	}

	/**
	 *
	 * Load a character into one of the writable character banks.
	 *
	 */

	final void LoadAddrChar (byte[] charData)
	{   
	int 	loadnum,i;

		if (mem_conv == 2)
			loadnum = mem_addr;
		else
			loadnum = ((mem_addr - 0x3800) >> 4);

		if (loadnum < 0 || loadnum > 384)
			return;

		for (i=0; i<16; i++)
		{
			m2m3[384*i + loadnum] = charData[i];
		}         
	}

	/**
	 *
	 *	OkDraw()
	 *
	 *	Return if ok to draw styled object.
	 *
	 */

	boolean OkDraw()
	{
		if (style_pattern == 1 || do_rule_override)
			return true;
		else if (screen_mode == SCWRITE || screen_mode == SCERASE)
			return false;
		else
			return true;
	}

	/**
	 *
	 *	Return true if filename sumchk is correct.
	 *
	 */
	final boolean VerifyFilenameSumChk()
	{
	int i,xsumchk=0,intbyte;

		for (i=0; i<filename.length(); i++)
		{
			intbyte = filename.charAt(i);
			xsumchk ^= intbyte << (i & 7);
		}

		return xsumchk == filename_sum_check;
	}

	/**
	 * Converts window filepath into unix filepath.
	 */
	String swabFile(String fname)
	{
		if	(!PortalConsts.is_windows)
		{
		StringBuffer	result = new StringBuffer(fname);
		int				i,pos=0;

			while ((i = fname.indexOf('\\',pos)) > -1)
			{
				pos = i+1;
				result.replace(i,i+1,"/");
			}

		if	(PortalConsts.is_debugging)	System.out.println("swabfile: result="+result);
			return result.toString();
		}
		else
			return fname;
	}


	/**
	 * Creates file object for filepath sent from
	 * novanet. Allows for both relative/absolute
	 * paths to be sent.
	 */
	File smartFile(String filepath)
	{
	File	result = new File(filepath);

		if	(!result.isAbsolute())
		{
		File	current_path = new File(current_dir,filepath);

			// if reference to existing path in portal dir or we've switched directories
			if	(!current_dir.equals(original_dir) || current_path.exists())
				result = current_path;
			else
			{
				if	(filepath.endsWith("512"))	// now trap files
					result = new File(tmp_dir,filepath);
				else
					result = new File(download_dir,filepath);
			}
		}
		return result;
	}

	/**
	 *
	 *	Return File object referring to file described by filename buffer.
	 *
	 */
	File CreateFile()
	{
	File result = null;

		if	(PortalConsts.is_debugging)	System.out.println("createfile of: "+filename);
		if ('#' == filename.charAt(0))
		{
		String	nname = swabFile(filename.substring(1));

			if	(null == localdata_dir)
				result = new File(current_dir,nname);
			else
				result = new File(localdata_dir,nname);
		}
		else
		{
			result = smartFile(swabFile(filename.toString()));
		}

		try
		{
			if (!result.exists())
			{
				if	(PortalConsts.is_debugging)	System.out.println("File not found: "+result.getCanonicalPath());
			}
		}
		catch (IOException e1)
		{
		}

		return result;
	}

	/**
	 * Sets resource server to be used by the portal.
	 *
	 * @param	rs_host		Resource server host name.
	 * @param	process_id	Meta resource process ID that prefixes the resouce
	 *						when getting it from the resource server.
	 */
	public void setResourceServer(
		String rs_host,
		String process_id)
	{
		if (PortalConsts.is_debugging) System.out.println("setResourceServer("+rs_host+","+process_id+")");
		using_resource_server = null != rs_host;

		if (using_resource_server)
		{
			this.rs_host = rs_host;
			this.process_id = process_id;
		}
	}

	/**
	 *
	 *	Return String with URLized version of filename buffer.
	 *
	 */

	String CreateURL()
	{
	StringBuffer	result = new StringBuffer();

		result.append("http://").append(rs_host);
		if (process_id != null)
		{
			result.append(":8080/RS/");
			result.append(process_id);
		}
		result.append("/");

		if ('#' == filename.charAt(0))
		{
		char	nfilename[] = new char[filename.length()-1];

			filename.getChars(1,filename.length(),nfilename,0);
			if (null != localdata_dir)
				result.append(localdata_dir);
			result.append(nfilename);
		}
		else
		{
			result.append(filename);
		}

	// convert backslashes in the path into
	// forward slashes:
	int i,j;

		j = result.length();
		for (i=0; i<j; i++)
		{
			if (result.charAt(i) == '\\')
				result.setCharAt(i,'/');
		}

		if	(PortalConsts.is_debugging) System.out.println("createurl->"+result.toString());
	// return a string:
		return result.toString();
	}

	/**
	 *
	 *	Prepare to print new document.
	 *
	 */
	void POpenDoc()
	{
		print_interface.openDoc();
	}

	/**
	 *
	 *	Prepare to print new page.
	 *
	 */
	void POpenPage()
	{
		print_interface.openPage();
	}

	/**
	 *
	 *	Print current page.
	 *
	 */

	void PClosePage()
	{
		print_interface.closePage();
	}

	/**
	 *
	 *	End document (print it).
	 *
	 */
	void PCloseDoc()
	{
		print_interface.closeDoc();
	}


	/**
	 *
	 *	Select font for printing.
	 *
	 */
	void PTextFont()
	{
		print_interface.textFont(filename.toString());
	}

	/**
	 *
	 *	Select font size.
	 *
	 */
	void PTextSize()
	{
		print_interface.textSize(ExtractWord(0));
	}

	/**
	 *
	 *	Plot string on printed page.
	 *
	 */
	void PDrawText()
	{
		print_interface.drawText(filename.toString());
	}

	/**
	 *
	 *	Determine width of text string.
	 *
	 */
	void PTextWidth()
	{
		print_interface.textWidth(filename.toString());
	}

	/**
	 *
	 *	Return ascent for current font.
	 *
	 */
	void PFontAscent()
	{
		print_interface.fontAscent();
	}

	/**
	 *
	 *	Return descent for current font.
	 *
	 */

	void PFontDescent()
	{
		print_interface.fontDescent();
	}

	/**
	 *
	 *	Return leading for current font.
	 *
	 */
	void PFontLeading()
	{
		print_interface.fontLeading();
	}

	/**
	 *
	 *	Establish printer pen location.
	 *
	 */
	void PMoveTo()
	{
		print_interface.moveTo(ExtractWord(0),ExtractWord(3));
	}

	/**
	 *
	 *	Draw line on printer.
	 *
	 */
	void PLineTo()
	{
		print_interface.lineTo(ExtractWord(0),ExtractWord(3));
	}

	/**
	 *
	 *	Return page size in printer coordinates.
	 *
	 */
	void PPage()
	{
		print_interface.pageSize();
	}

	/**
	 *
	 *	Return dpi for x/y.
	 *
	 */
	void PPageResolution()
	{
		print_interface.pageResolution();
	}

	/**
	 *
	 *	Return printer pen location.
	 *
	 */

	void PGetPen()
	{
		print_interface.getPen();
	}

	/**
	 *
	 *	Select font attributes.
	 *
	 */
	void PTextStyle()
	{
		print_interface.textStyle(ExtractWord(0));
	}

	/**
	 *
	 *	Check for errors during printing.
	 *
	 */
	void PError()
	{
		print_interface.error();
	}

	/**
	 *
	 *	Cancel print job.
	 *
	 */
	void PCancel()
	{
		print_interface.cancel();
	}

	/**
	 *
	 *	Select type of MOUSE cursor.
	 *
	 */

	void SetMouseCursor()
	{
	// todo
		mouse_cursor_style = ExtractWord(0) & 3;
	}

	/**
	 *
	 *	Send back portal revision# + physical screen height.
	 *	*ztsrev*
	 *	Also indicate os version..
	 *
	 */
	void TermInfo2()
	{
	String	osv;
	int		osid = 0;

		if	(!PortalConsts.is_applet)
		{
			osv = System.getProperty("os.version");
			// 6 is win vista
			// 10.1.x thru 10.5.x for OSX
			if	(PortalConsts.is_macintosh)
			{
			String	subv = osv.substring(3,4);

				osid = Integer.parseInt(subv);
				if	(PortalConsts.is_debugging)
					System.out.println("subv="+subv);
			}
		}

		SendPixRes (0,15 | (osid << 12),512,0);
	}

	/**
	 *
	 *	Check if printing is possible at this station.
	 *
	 */
	void PCheck()
	{
		SendEcho (0);
	}

	/**
	 *
	 *	CloseMsg()
	 *
	 *	Close connection and give user an informative message.
	 *
	 */
	void CloseMsg()
	{
		end_parse = true;
		if	(PortalConsts.is_debugging)	System.out.println("Connected closed!");

		((LevelOnePanel)levelone_container).closeSession(filename.toString());
	}

	/**
	 * 
	 *
	 * @param	y
	 */
	protected final int xlatY(int y)
	{
		return (511-y);
	}

	/**
	 * 
	 *
	 * @param	x
	 */
	protected final int xlatX(int x)
	{
		return x;
	}

	/**
	 * 
	 *
	 * @param	y	The y coordinate to be translated.
	 */
	protected int unxlatY(int y)
	{
		return (511-y);
	}

	/**
	 *
	 *
	 * @param	x
	 */
	protected int unxlatX(int x)
	{
		return x;
	}

	int InvertMode (int mode)
	{
		switch (mode)
		{
			case SCWRITE:
				return SCERASE;
			case SCREWRITE:
				return SCINVERSE;
			case SCERASE:
				return SCWRITE;
			case SCINVERSE:
				return SCREWRITE;
			case SCXOR:
				return SCXOR;
		}

		return 0;
	}

	void ModeColor (int mode)
	{
		switch (mode)
		{
			case SCWRITE:
			case SCREWRITE:
			case SCXOR:
				if (is_direct_draw)
					levelone_graphics.setColor (fg_color);
				levelone_offscreen.setColor (fg_color);
				break;
			case SCERASE:
			case SCINVERSE:
				if (is_direct_draw)
					levelone_graphics.setColor (bg_color);
				levelone_offscreen.setColor (bg_color);
				break;
		}
	}

	/**
	 *
	 * @return
	 */
	protected Color getModeFGColor()
	{
		if (screen_mode == SCWRITE ||
			screen_mode == SCREWRITE ||
			screen_mode == SCXOR)
		{
			return fg_color;
		}
		else
			return bg_color;
	}

	/**
	 *
	 * @return
	 */
	protected Color getModeBGColor()
	{
		if (screen_mode == SCWRITE ||
			screen_mode == SCREWRITE ||
			screen_mode == SCXOR)
		{
			return bg_color;
		}
		else
			return fg_color;
	}

	void ModeColor()
	{
		ModeColor (screen_mode);
	}

	void ClipSet (int x1,int y1,int x2,int y2,boolean sysclip)
	{
		clip_rect = MakeRectLegal(xlatX(x1),xlatY(y1),xlatX(x2),xlatY(y2));

		if (sysclip)
			sys_clip_rect = MakeRectLegal(xlatX(x1),xlatY(y1),xlatX(x2),xlatY(y2));
	}

	protected void applyClipping()
	{
		if (is_direct_draw)
			levelone_graphics.setClip(clip_rect);
		levelone_offscreen.setClip (clip_rect);
	}

	void UnapplyClipping()
	{
		if (is_direct_draw)
			levelone_graphics.setClip(sys_clip_rect);
		levelone_offscreen.setClip(sys_clip_rect);
	}

	protected int abs (int x)
	{
		if (x < 0)
			return -1*x;
		else
			return x;
	}

	// RENDERING ROUTINES
	protected void modeClipColor(
		boolean allow_xor,
		boolean apply_clipping,
		boolean apply_color)
	{
		if (allow_xor && screen_mode == SCXOR)
		{
			if (is_direct_draw)
				levelone_graphics.setXORMode(Color.white);
			levelone_offscreen.setXORMode(Color.white);
		}
		else
		{
			if (is_direct_draw)
				levelone_graphics.setPaintMode();
			levelone_offscreen.setPaintMode();
		}

		if (apply_clipping)
			applyClipping();
		else
			UnapplyClipping();

		if (apply_color)
			ModeColor();
	}

	void PlotLine(int x1,int y1,int x2,int y2,int mode,
		int spattern,int sfill,int sthick,int sdash)
	{
		x1 = xlatX (x1);
		x2 = xlatX (x2);
		y1 = xlatY (y1);
		y2 = xlatY (y2);

		if (sfill != 0 || sthick > 1 || sdash != 0)
		{
			if (null == polygon)
			{
				polygon = new java.awt.Polygon();
				polygon.addPoint (x1,y1);
			}

			polygon.addPoint (x2,y2);
		}
		else
		{
			if (0 == spattern)
				ModeColor (InvertMode (mode));
			else
				ModeColor (mode);

			modeClipColor(true,true,false);
			levelone_offscreen.drawLine (x1,y1,x2,y2);
			if (is_direct_draw)
				levelone_graphics.drawLine (x1,y1,x2,y2);
			else
			{
			Rectangle	r = MakeRectLegal(x1,y1,x2,y2);

				levelone_container.repaint(r.x,r.y,r.width,r.height);
			}
		}

	}

	/**
	 * Render a box.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	protected void plotBox(
		int x1,
		int y1,
		int x2,
		int y2,
		int thickness,
		int mode,
		int spattern,
		int fillflag)
	{
		if (abs(thickness) > 1024)
			return;
		if (abs(x1) > 2048 || abs(y1) > 2048 || abs(x2) > 2048 || abs(y2) > 2048)
			return;

		x1 = xlatX (x1);
		y1 = xlatY (y1);
		x2 = xlatX (x2);
		y2 = xlatY (y2);

		if (x1 > x2)
		{
		int i;

			i = x1; x1 = x2; x2 = i;
		}
		if (y1 > y2)
		{
		int i;

			i = y1; y1 = y2; y2 = i;
		}

		if (0 == spattern)
			ModeColor (InvertMode (mode));
		else
			ModeColor (mode);

		modeClipColor(true,true,false);

	// If the fill flag is set, fill the rectangle bound
	// by the points:
		if (fillflag != 0)
		{
			if (is_direct_draw)
				levelone_graphics.fillRect (x1,y1,x2-x1+1,y2-y1+1);
			else
				do_repaint = true;
			levelone_offscreen.fillRect (x1,y1,x2-x1+1,y2-y1+1);
		}
	// otherwise draw rectangles for the points:
		else
		{
		int i,delta;

			if (thickness == 0)
				thickness = 1;

			if (thickness < 0)
			{
				delta = -1;
				thickness = -1*thickness;
			}
			else
				delta = 1;

			for (i=0; i<thickness; i++)
			{
				if (is_direct_draw)
				{
					levelone_graphics.drawLine (x1,y1,x2,y1);
					levelone_graphics.drawLine (x2,y1,x2,y2);
					levelone_graphics.drawLine (x2,y2,x1,y2);
					levelone_graphics.drawLine (x1,y2,x1,y1);
				}
				else
					do_repaint = true;
				levelone_offscreen.drawLine (x1,y1,x2,y1);
				levelone_offscreen.drawLine (x2,y1,x2,y2);
				levelone_offscreen.drawLine (x2,y2,x1,y2);
				levelone_offscreen.drawLine (x1,y2,x1,y1);

				x1 -= delta; y1 -= delta;
				x2 += delta; y2 += delta;
			}
		}
	}

	void PlotEllipse (int xRadius, int yRadius, int x, int y,
		int mode,int spattern,int sthick,int sfill)
	{
	int x1,y1;

		x = xlatX (x);
		y = xlatY (y);

		x1 = -xRadius + x;
		y1 = -yRadius + y;

		if (0 == spattern)
			ModeColor (InvertMode (mode));
		else
			ModeColor (mode);

		modeClipColor(true,true,false);

		if (sfill != 0)
		{
			if (is_direct_draw)
			{
				levelone_graphics.fillOval (
					x1,y1,xRadius*2,yRadius*2);
			}
			else
				do_repaint = true;

			levelone_offscreen.fillOval (
				x1,y1,xRadius*2,yRadius*2);
		}
		else
		{
			if (is_direct_draw)
			{
				levelone_graphics.drawOval (
					x1,y1,xRadius*2,yRadius*2);
			}
			else
				do_repaint = true;

			levelone_offscreen.drawOval (
				x1,y1,xRadius*2,yRadius*2);
		}
	}

	void PlotArc ( int xRadius, int yRadius, int startAngle, 
		int arcLength, int x, int y,int mode,int spattern,int sthick,
		int sfill)
	{   
	int 		x1,y1;

		if (0 == arcLength)
			arcLength = 3600;

		if (arcLength >= 3600)
		{
			PlotEllipse (xRadius,yRadius,x,y,mode,spattern,sthick,sfill);
			return;
		}

		if (0 == spattern)
			ModeColor (InvertMode (mode));
		else
			ModeColor (mode);

		modeClipColor(true,true,false);

		startAngle = startAngle / 10;
		if (arcLength < 0)
			startAngle = (360 + (startAngle + (arcLength/10))) % 360;

		arcLength = abs(arcLength/10);

		x = xlatX (x);
		y = xlatY (y);

		x1 = x - xRadius;
		y1 = y - yRadius;

		if (sfill != 0)
		{
			if (is_direct_draw)
			{
				levelone_graphics.fillArc (
					x1,y1,xRadius*2,yRadius*2,startAngle,arcLength);
			}
			else
				do_repaint = true;

			levelone_offscreen.fillArc (
				x1,y1,xRadius*2,yRadius*2,startAngle,arcLength);
		}
		else
		{
			if (is_direct_draw)
			{
				levelone_graphics.drawArc (
					x1,y1,xRadius*2,yRadius*2,startAngle,arcLength);
			}
			else
				do_repaint = true;

			levelone_offscreen.drawArc (
				x1,y1,xRadius*2,yRadius*2,startAngle,arcLength);
		}
	}

	/**
	 * Draws the current style of cursor on the passed
	 * graphics object at the specified coordinates.
	 */
	final void PlotCursor(Graphics g,int x1,int y1)
	{
		g.setXORMode(Color.white);

		switch (cursor_style)
		{
			case 0:	// underline
				g.drawLine(x1,y1,x1+7,y1);
				break;
			case 1:	// vertical bar
				g.drawLine(x1-1,y1,x1-1,y1-15);
				break;
			case 2:	// box
				Rectangle	rect = MakeRectLegal(x1,y1,x1+7,y1-15);
				g.fillRect(rect.x,rect.y,rect.width,rect.height);
				break;
		}
	}

	/**
	 * Repaints screen area where cursor was last drawn.
	 */
	final void repaintCursor()
	{
		levelone_container.repaint(cursor_x-1,cursor_y-15,9,16);
	}

	/**
	 * Plots cursor at the current screen coordinates.
	 */
	public final void ShowCursor(Graphics g)
	{
		if	(cursor_on)
		{
			cursor_x = xlatX(current_x+center_x);
			cursor_y = xlatY(current_y+center_y);
			PlotCursor(g,cursor_x,cursor_y);
		}
	}

	/**
	 * Send a string to novaNET.
	 *
	 * @param	text	The string of characters to be sent.
	 */
	public final void sendString(String text)
	{
		char[] char_text = text.toCharArray();

		// Send every character in the text to novaNET.
		for (int index = 0; index < char_text.length; index++)
		{
			// If it is a carriage return, we need to send something special.
			if (char_text[index] == '\n')
				SendKey(KEY_NEXT);
			else
				sendAsciiDelay(char_text[index],100);
		}
	}

	public final static int KEY_SUPER=0x13;
	public final static int KEY_SUPER1=0x17;
	public final static int KEY_SUB=0x04;
	public final static int KEY_SUB1=0x05;
	public final static int KEY_ANS=0x07;
	public final static int KEY_TERM=0x14;
	public final static int KEY_COPY=0x03;
	public final static int KEY_COPY1=0x16;
	public final static int KEY_TAB=0x0a;
	public final static int KEY_CR=0x1c;
	public final static int KEY_ERASE=0x08;
	public final static int KEY_ERASE1=0x19;
	public final static int KEY_MICRO=0x7b;
	public final static int KEY_FONT=0x7f;
	public final static int KEY_HELP=0x0b;
	public final static int KEY_HELP1=0x09;
	public final static int KEY_SQUARE=0x7d;
	public final static int KEY_ACCESS=0x00;
	public final static int KEY_NEXT=0x0d;
	public final static int KEY_NEXT1=0x1e;
	public final static int KEY_EDIT=0x1a;
	public final static int KEY_EDIT1=0x18;
	public final static int KEY_BACK=0x02;
	public final static int KEY_BACK1=0x0e;
	public final static int KEY_LAB=0x0c;
	public final static int KEY_LAB1=0x0f;
	public final static int KEY_DATA=0x12;
	public final static int KEY_DATA1=0x1d;
	public final static int KEY_STOP=0x01;
	public final static int KEY_STOP1=0x11;
	public final static int KEY_DIV=0x60;
	public final static int KEY_DIV1=0x27;
	public final static int KEY_MULT=0x26;
	public final static int KEY_MULT1=0x40;
	public final static int KEY_SIGMA=0x23;
	public final static int KEY_DELTA=0x7e;
	public final static int KEY_BACKSPACE=0x1f;
	public final static int KEY_SPACE=0x20;
	public final static int KEY_ASSIGN=0x5e;
	public final static int KEY_SHIFT=0x5c;

	private void sendAsciiDelay(int code,int delay)
	{
		switch (code)
		{
			case '\\':
				SendKey(KEY_ACCESS);
				SendKey('/');
				break;
			case '@':
				SendKey(KEY_ACCESS);
				SendKey('5');
				break;
			case '#':
				SendKey(KEY_ACCESS);
				SendKey('$');
				break;
			case '^':
				SendKey(KEY_ACCESS);
				SendKey('x');
				break;
			case '&':
				SendKey(KEY_ACCESS);
				SendKey('+');
				break;
			case '|':
				SendKey(KEY_ACCESS);
				SendKey('I');
				break;
			case '~':
				SendKey(KEY_ACCESS);
				SendKey(';');
				break;
			case '`':
				SendKey(KEY_ACCESS);
				SendKey('q');
				break;
			case '{':
				SendKey(KEY_ACCESS);
				SendKey('[');
				break;
			case '}':
				SendKey(KEY_ACCESS);
				SendKey(']');
				break;
			case 0x27:
				SendKey(0x7c);
				break;
			case 0x09:
				SendKey(KEY_TAB);
				break;
			default:
				SendKeyDelay(code,delay);
		}
	}

	public void SendKey(int keynum)
	{
		SendKeyDelay(keynum,0);
	}

	public void SendKeyDelay(int keynum,int delay)
	{
	int 	count;

		if (is_flow_control_on)
		{
			switch ( keynum)
			{
				case KEY_ACCESS:
					transmit_buffer[0] = 0x1b;
					transmit_buffer[1] = 0x1d;
					count = 2;
					break;
				case KEY_SUB1:
					transmit_buffer[0] = 0x1b;
					transmit_buffer[1] = 0x04;
					count = 2;
					break;
				case KEY_TAB:
					transmit_buffer[0] = 0x09;
					count = 1;
					break;
				case KEY_HELP1:
					transmit_buffer[0] = 0x0a;
					count = 1;
					break;
				case KEY_STOP1:
					transmit_buffer[0] = 0x05;
					count = 1;
					break;
				case KEY_SUPER:
					transmit_buffer[0] = 0x17;
					count = 1;
					break;
				case KEY_SUPER1:
					transmit_buffer[0] = 0x1b;
					transmit_buffer[1] = 0x17;
					count = 2;
					break;
				case 0x7c:			/* apostrophe */
					transmit_buffer[0] = 0x27;
					count = 1;
					break;
				case KEY_DIV1:
					transmit_buffer[0] = 0x7c;
					count = 1;
					break;	
				default:
					transmit_buffer[0] = (byte)keynum;
					count = 1;
			}
		}
		else						/* not flow control */
		{
			transmit_buffer[0] = (byte) keynum;
			count = 1;
		}

		if (!lw_suppress)
		{
			if	(null != levelone_network)
				levelone_network.write(transmit_buffer, 0, count, delay);
		}

		if (KEY_STOP1 == keynum)
			ShiftStop();
	}

	private void Beep()
	{
		java.awt.Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Initiates collection of lightweight information
	 * from -notes-/term comment.
	 */
	private void lightWeightBuffer()
	{
		buffered_data = null;
		buffered_count = 0;
		InitTop(sBufferData);
	}

	/**
	 * Gathers up note or dump data.
	 */
	private void bufferData()
	{
		if	(null == buffered_data)
		{
		int	buffer_length = (int) extractLong();

			buffered_data = new long[buffer_length];
		}
		else if (buffered_count < buffered_data.length)
		{
			buffered_data[buffered_count++] = extractLong();
		}
	}

	/**
	 * Translates a note into ASCII Java String.
	 */
	private String translateNote()
	{
	int				offset = 0;
	// create stringbuffer which will contain entire note text
	StringBuffer	sb = new StringBuffer();

		// put together the lines of the note
		for (int i=0;i<buffered_data.length; i++)
		{
			if	((buffered_data[i] & 07777) == 0)	// cyber EOL
			{
			String	line = Sixbit.displayToSixBitAscii(buffered_data,offset,10*(i-offset+1));

				offset = i+1;
				sb.append(line);
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	/**
	 * Lightweight lesson executed -notes- send.
	 */
	private void lightWeightNote()
	{
		//!! todo: forward note back across resource network here
		if	(PortalConsts.is_debugging)	System.out.println("note: "+translateNote());
		String[] parameters = new String[]{translateNote()};
//!!maybe we should give process id also.
		boolean is_successful = invokeStaticMethod(
			"com.nn.rn.rs.CRSReport",
			"sendReport",
			parameters);
	}

	/**
	 * Lightweight lesson term comment/-notes- with input.
	 */
	private void lightWeightComment()
	{
		if	(PortalConsts.is_debugging)	System.out.println("comment: "+translateNote());
		
		String[] parameters = new String[]{process_id, translateNote()};
	
		boolean is_successful = invokeStaticMethod(
			"com.nn.rn.rs.CRSReport",
			"invokeCommentDialog",
			parameters);
	}

	/**
	 * Lightweight lesson execution error/-libcall dump-.
	 */
	private void lightWeightDump()
	{
		if	(PortalConsts.is_debugging)	System.out.println("dump: length is "+buffered_count+" of "+buffered_data.length);
		Object[] parameters = new Object[]{buffered_data};
//!!maybe we should give process id also.
		boolean is_successful = invokeStaticMethod(
			"com.nn.rn.rs.ErrorReportEngine",
			"sendDump",
			parameters);
	}

	/**
	 * Invoke a static method of a specific class. This is done to connect to
	 * CRS without having to drag in CRS files to compile portal related stuff.
	 *
	 * @param	class_name	Name of the class the static method is in.
	 * @param	method_name	Name of the static method we want to invoke.
	 * @param	parameters	Parameters to pass into the method.
	 */
	private boolean invokeStaticMethod(
		String class_name,
		String method_name,
		Object[] parameters)
	{
		Class[] parameter_classes = new Class[parameters.length];
		
		// Construct parameter class for each parameter.
		for (int index = 0; index < parameters.length; index++)
			parameter_classes[index] = parameters[index].getClass();
		
		try
		{
			Class target_class = Class.forName(class_name);
			java.lang.reflect.Method target_method = target_class.getMethod(
				method_name,
				parameter_classes);

			target_method.invoke(
				null,
				parameters);
			
			return true;		
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Intiates collection of lightweight information
	 * at signout.
	 */
	private void lightWeightLogout()
	{
		if (PortalConsts.is_debugging) System.out.println("lw logout data init len="+signout_data.length);
		/*
		 * Clear student context information.
		 */
		lstatus = 0;
		rstartl = 0;
		rstartu = 0;
		ldone = 0;
		lscore = -1;
		student_variables = null;

		InitTop(sLightWeight);
		signout_count = 0;
	}

	/**
	 * Process lightweight mode data.
	 */
	private void lightWeightData()
	{
		if (signout_count < signout_data.length)
			signout_data[signout_count++] = extractLong();

		// If this is the last one, get the data out.
		if (signout_count == signout_data.length)
		{
		// group,basic_rec,router_vars,student_vars
			InitTop (sAlpha);
			/*
			 * Extract information from the basic record.
			 */
			long[] basic_record_data = extract(signout_data, 1, 64);
			if (PortalConsts.is_debugging)
			{
				System.out.println(Sixbit.displayToSixBitAscii(basic_record_data[0]));
				for (int j=0; j<64;j++)
				 if (basic_record_data[j] != 0)
				  System.out.println("["+j+"]="+Long.toOctalString(basic_record_data[j]));
			}
			rstartl = basic_record_data[3];
			rstartu = basic_record_data[4];
			lstatus = basic_record_data[6];
			if (PortalConsts.is_debugging) System.out.println("lstatus: "+Long.toOctalString(lstatus));

			long ldone_lscore_long = basic_record_data[31];
			int basic_record_ldone = (int)((ldone_lscore_long >> 55) & 0x1f);

			switch (basic_record_ldone)
			{
				case 1:
					ldone = -1;
					break;
					
				case 2:
					ldone = 1;
					break;
					
				case 3:
					ldone = -1;
					break;
					
				default:
					ldone = 0;
					break;
			}
			
			// lscore of 128 means NOT DONE (blank tag -score-)
			lscore = (int)(ldone_lscore_long & 0xffL);
			// Convert negative values from one's complement.
			lscore = lscore > 128 ? lscore-256 : lscore;
			if	(signout_data.length > 128)
				student_variables = extract(signout_data, 129, 150);
			if (PortalConsts.is_debugging) System.out.println("lscore:"+lscore+" ldone:"+ldone);

			// do scorm stuff..
			switch (signout_data_class)
			{
				case 0x34:	// score
					JPortal.global_jportal.scorminterface.setScore(lscore);
					break;
				case 0x35:	// status
					JPortal.global_jportal.scorminterface.setStatus(basic_record_data[6]);
					break;
				case 0x36:	// lesson(ldone mod)
					JPortal.global_jportal.scorminterface.setLdone(ldone);
					break;
				case 0x37:	// restart
					JPortal.global_jportal.scorminterface.setRestart(basic_record_data[3],basic_record_data[4]);
					break;
				case 0x38:	// signout (bye!)
					JPortal.global_jportal.scorminterface.setStatus(basic_record_data[6]);
					JPortal.global_jportal.scorminterface.setStudentVariables(student_variables);
					JPortal.global_jportal.scorminterface.signout();
					break;
			}

		}
	}

	/**
	 * Extract portion of an array of longs.
	 *
	 * @param	longs	Array of longs to be extracted.
	 * @param	offset	The offset to start extracting.
	 * @param	size	Number of elements to extract.
	 * @return			Extracted portion of the given array.
	 */
	private long[] extract(
		long[] longs,
		int offset,
		int size)
	{
		long[] result = new long[size];

		for (int index = 0; index < size; index++, offset++)
		{
			result[index] = longs[offset];
		}

		return result;
	}

	/**
	 * Does signin of a user. It sends student data bank back to the system.
	 * The student data bank has the following format.
	 *		group name (1 word)
	 *		basic record (64 words)
	 *		router variables (64 words)
	 *		student variables (150 words)
	 */
	private void lightWeightLogin()
	{
		// Use array for easy future expansion.
		long[] misc_data = new long[1];
		long[] basic_record_data = new long[64];
		long[] router_vars_data = new long[64];
		long[] student_vars_data;

		if	(PortalConsts.is_debugging)	System.out.println("system requested lw data");

		/*
		 * Pack group information.
		 */
		Sixbit.sixBitAsciiToDisplay(autogroup, misc_data, 0, 8);

		/*
		 * Pack basic record information. Important offsets into basic record.
		 *	+0 = rname (1 word)
		 *	+1 = rname1 (1 word)
		 *	+3 = restart lesson (1 word)
		 *	+4 = restart unit (1 word)
		 *	+6 = lstatus (1 word)
		 *	+15 = user type (1 word)
		 *	+24 = creation date (1 word)
		 *	+31 = ldone, lscore (1 word; ldone 56-60 bits; lscore 1-8 bits)
		 */
		Sixbit.sixBitAsciiToDisplay(autoname, basic_record_data, 0, 18);

		// If restart lesson is set, we want to pass that information.
		basic_record_data[3] = rstartl;

			// If restart unit is set, we want to pass that information.
		basic_record_data[4] = rstartu;

		// If lstatus is set, we want to pass that information.
		basic_record_data[6] = lstatus;

		if (null != auto_lesson && auto_lesson.equals("edit"))
 			Sixbit.sixBitAsciiToDisplay("author", basic_record_data, 15, 10);
  		else
			Sixbit.sixBitAsciiToDisplay("student", basic_record_data, 15, 10);
		
		Sixbit.sixBitAsciiToDisplay(" 06/15/00 ", basic_record_data, 24, 10);

		int basic_record_ldone;
		// Convert database ldone value to basic record ldone value.
		switch (ldone)
		{
			case -1:
				basic_record_ldone = 1;
				break;

			case 1:
				basic_record_ldone = 2;
				break;

			default:
				basic_record_ldone = 0;
		}

		// The long that stores the ldone and lscore information.
		long ldone_lscore_long = (basic_record_ldone & 0x1fL) << 55;
  		ldone_lscore_long |= lscore & 0xff;
		basic_record_data[31] = ldone_lscore_long;

		/*
		 * Pack router variables. These variables are used to tell the system
		 * the TUTOR lesson, unit, and its arguments to be executed. It has
		 * the following format.
		 *		lesson name (1 word)
		 *		unit name (1 word)
		 *		number of aruments (1 word)
		 *		arguments (up to 10 words)
		 *		bypass codeword (1 word)
		 */
		if (null != auto_lesson)
			Sixbit.sixBitAsciiToDisplay(auto_lesson, router_vars_data, 0, 10);

		if (null != auto_unit)
			Sixbit.sixBitAsciiToDisplay(auto_unit, router_vars_data, 1, 10);

		if	(null != auto_bypass)
			Sixbit.sixBitAsciiToDisplay(auto_bypass, router_vars_data, 13, 10);

		int number_args =
			null == auto_lesson_arguments ? 0 : auto_lesson_arguments.length;
		
		router_vars_data[2] = number_args;

		// Copy arguments as they are into the router variables.
		for (int index = 0; index < number_args; index++)
			router_vars_data[3+index] = auto_lesson_arguments[index];

		/*
		 * Pack student variables.
		 */
		if (null != student_variables)
			student_vars_data = student_variables;
		else
			student_vars_data = new long[150];

		/*
		 * Send packed student data bank over to the system.
		 */
		//!!remove after debug.
		if	(PortalConsts.is_debugging)	System.out.println("begin send lightweight-login");
		// Send miscellaneous portion of the student data bank.
		for (int index = 0; index < misc_data.length; index++)
			SendDataKey(misc_data[index]);

		// Send basic record portion of the student data bank.
		for (int index = 0; index < basic_record_data.length; index++)
			SendDataKey(basic_record_data[index]);
		
		// Send router variable portion of the student data bank.
		for (int index = 0; index < router_vars_data.length; index++)
			SendDataKey(router_vars_data[index]);
		
		// Send student variable portion of the student data bank.
		if	(student_vars_data.length != 150)
		{
			if	(PortalConsts.is_debugging)	System.err.println("incorrect student variable buffer size!: "+student_vars_data.length);
		}
		else
		{
			for (int index = 0; index < student_vars_data.length; index++)
				SendDataKey(student_vars_data[index]);
		}

		lw_suppress = false;
		//!!remove after debug.
		if	(PortalConsts.is_debugging)	System.out.println("done send lightweight-login");
	}

	private void DoAutoLogin()
	{
	int  	namel=0,groupl=0,passl=0,i;

		if	(PortalConsts.is_debugging)	System.out.println("doautologin");
		if (autoname != null)
			namel = autoname.length();
		if (autogroup != null)
			groupl = autogroup.length();
		if (autopassword != null)
			passl = autopassword.length();

		if (namel > 18)
			namel = 18;
		if (groupl > 10)
			groupl = 10;
		if (passl > 10)
			passl = 10;

		SendExt(namel);

		SendExt((groupl << 4) | passl);

		for (i=0; i<namel; i++)
			SendExt(autoname.charAt(i));

		for (i=0; i<groupl; i++)
			SendExt(autogroup.charAt(i));

		for (i=0; i<passl; i++)
			SendExt(autopassword.charAt(i));

		SendExt(0xff);
	}

	private final void sleep(int mills)
	{
		try
		{
			Thread.sleep(mills);
		}
		catch (java.lang.InterruptedException e)
		{
		}
	}

	// This is a table of the M0/M1 characters
	// from the NovaNET ascii terminal definition.
	//
	// The array is 144*16 members long; each row
	// of the array is one pixel of each of the 144
	// read-only characters.
	//
	private static final int chardefs [] =
	{
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0xfe, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x28, 0x28, 0x10, 0x40, 0x20, 0x10,
	0x2, 0x80, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x38,
	0x0, 0x38, 0xf8, 0x3c, 0xf8, 0xfe, 0xfe, 0x3c,
	0x82, 0x7c, 0x3e, 0x82, 0x80, 0x82, 0x82, 0x38,
	0xfc, 0x38, 0xfc, 0x7c, 0xfe, 0x82, 0x82, 0x82,
	0x82, 0x82, 0xfe, 0xe, 0x0, 0xe0, 0x10, 0x0,
	0x20, 0x0, 0x80, 0x0, 0x2, 0x0, 0xc, 0x0,
	0xc0, 0x10, 0x10, 0xc0, 0x30, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x20, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x6, 0x10, 0xc0, 0x0, 0x0,
	0x0, 0x2, 0x0, 0x32, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0xfe, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x30, 0x40, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x38, 0x0, 0x30, 0x0, 0xc0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x8, 0x0,
	0x28, 0x0, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x28, 0x28, 0x7c, 0xa2, 0x50, 0x10,
	0x4, 0x40, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x3a, 0x10, 0x38, 0x38, 0x4, 0x7c, 0x18, 0x7c,
	0x38, 0x38, 0x0, 0x0, 0x0, 0x0, 0x0, 0x44,
	0x0, 0x44, 0x84, 0x42, 0x84, 0x80, 0x80, 0x42,
	0x82, 0x10, 0x8, 0x84, 0x80, 0xc6, 0xc2, 0x44,
	0x82, 0x44, 0x82, 0x82, 0x10, 0x82, 0x82, 0x82,
	0x82, 0x82, 0x82, 0x8, 0x0, 0x20, 0x28, 0x0,
	0x10, 0x0, 0x80, 0x0, 0x2, 0x0, 0x12, 0x0,
	0x40, 0x0, 0x0, 0x40, 0x10, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x20, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x8, 0x10, 0x20, 0x0, 0x0,
	0x0, 0x4, 0x0, 0x4c, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x40, 0x10, 0x0, 0x0, 0x0,
	0x0, 0x18, 0x48, 0x20, 0x0, 0x0, 0x0, 0x0,
	0x0, 0xc, 0x60, 0x44, 0x4, 0x48, 0x40, 0xa0,
	0x0, 0x28, 0x0, 0x0, 0x0, 0x0, 0x10, 0x0,
	0x10, 0x0, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x28, 0xfe, 0x92, 0xa4, 0x50, 0x10,
	0x8, 0x20, 0x10, 0x0, 0x0, 0x0, 0x0, 0x2,
	0x44, 0x30, 0x44, 0x44, 0xc, 0x40, 0x20, 0x4,
	0x44, 0x44, 0x0, 0x0, 0x8, 0x0, 0x20, 0x44,
	0x38, 0x82, 0x84, 0x80, 0x82, 0x80, 0x80, 0x80,
	0x82, 0x10, 0x8, 0x88, 0x80, 0xaa, 0xa2, 0x82,
	0x82, 0x82, 0x82, 0x80, 0x10, 0x82, 0x44, 0x82,
	0x44, 0x44, 0x4, 0x8, 0x80, 0x20, 0x0, 0x0,
	0x0, 0x0, 0x80, 0x0, 0x2, 0x0, 0x10, 0x0,
	0x40, 0x0, 0x0, 0x40, 0x10, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x20, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x8, 0x10, 0x20, 0x0, 0x0,
	0x0, 0x8, 0x0, 0x0, 0x10, 0x2, 0x10, 0x0,
	0x10, 0x0, 0x0, 0x20, 0x10, 0x0, 0x0, 0x0,
	0x0, 0x24, 0x44, 0x20, 0x0, 0x2, 0x0, 0x0,
	0x0, 0x30, 0x18, 0x82, 0x8, 0x48, 0x20, 0x90,
	0x3c, 0x0, 0x0, 0x0, 0x10, 0x82, 0x0, 0x0,
	0x0, 0x10, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x0, 0x28, 0x90, 0x48, 0x20, 0x0,
	0x8, 0x20, 0x54, 0x10, 0x0, 0x0, 0x0, 0x4,
	0x4c, 0x10, 0x4, 0x4, 0x14, 0x40, 0x40, 0x8,
	0x44, 0x44, 0x30, 0x30, 0x10, 0x0, 0x10, 0x4,
	0x44, 0x82, 0xfc, 0x80, 0x82, 0x80, 0x80, 0x80,
	0x82, 0x10, 0x8, 0x90, 0x80, 0x92, 0xa2, 0x82,
	0x82, 0x82, 0x82, 0x80, 0x10, 0x82, 0x44, 0x92,
	0x28, 0x28, 0x8, 0x8, 0x40, 0x20, 0x0, 0x0,
	0x0, 0x78, 0xb8, 0x3c, 0x3a, 0x3c, 0x38, 0x7a,
	0x5c, 0x30, 0x30, 0x4c, 0x10, 0xec, 0xdc, 0x38,
	0xb8, 0x3a, 0xdc, 0x7c, 0xfc, 0x84, 0xc6, 0x82,
	0xc2, 0x82, 0xfc, 0x8, 0x10, 0x20, 0x0, 0x0,
	0x0, 0x10, 0x7c, 0x0, 0x30, 0x4, 0x38, 0x8,
	0x10, 0x20, 0x24, 0x10, 0x28, 0x44, 0x38, 0x10,
	0x62, 0x44, 0x20, 0x10, 0x24, 0x7c, 0xc, 0x3e,
	0x44, 0xc0, 0x6, 0x82, 0x18, 0x30, 0x30, 0x48,
	0x42, 0x0, 0x0, 0x18, 0x38, 0x44, 0x0, 0x0,
	0x0, 0x38, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x0, 0xfe, 0x7c, 0x10, 0x62, 0x0,
	0x8, 0x20, 0x28, 0x10, 0x0, 0x0, 0x0, 0x8,
	0x54, 0x10, 0x8, 0x18, 0x24, 0x78, 0x78, 0x8,
	0x38, 0x44, 0x30, 0x30, 0x20, 0x7c, 0x8, 0x8,
	0x9a, 0xfe, 0x82, 0x80, 0x82, 0xf0, 0xf0, 0x80,
	0xfe, 0x10, 0x8, 0xa0, 0x80, 0x82, 0x92, 0x82,
	0xfc, 0x82, 0xfc, 0x7c, 0x10, 0x82, 0x44, 0x92,
	0x10, 0x10, 0x10, 0x8, 0x20, 0x20, 0x0, 0x0,
	0x0, 0x4, 0xc4, 0x42, 0x46, 0x42, 0x10, 0x84,
	0x62, 0x10, 0x10, 0x50, 0x10, 0x92, 0x62, 0x44,
	0xc4, 0x46, 0x62, 0x80, 0x20, 0x84, 0x44, 0x92,
	0x24, 0x82, 0x88, 0x8, 0x10, 0x20, 0x32, 0x0,
	0x0, 0x20, 0x0, 0x0, 0x5e, 0xfe, 0x7c, 0xc,
	0x10, 0x60, 0x18, 0x8, 0x28, 0x44, 0x44, 0x0,
	0x92, 0x78, 0x30, 0x10, 0x24, 0xa8, 0x12, 0x50,
	0x82, 0x30, 0x18, 0xfe, 0x28, 0x0, 0x28, 0x24,
	0x80, 0x0, 0x7c, 0x24, 0x7c, 0x28, 0x0, 0x0,
	0x0, 0x7c, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x0, 0x28, 0x12, 0x24, 0x92, 0x0,
	0x8, 0x20, 0x28, 0x7c, 0x0, 0x7c, 0x0, 0x10,
	0x54, 0x10, 0x10, 0x4, 0x7e, 0x4, 0x44, 0x10,
	0x44, 0x3c, 0x0, 0x0, 0x40, 0x0, 0x4, 0x10,
	0xaa, 0x82, 0x82, 0x80, 0x82, 0x80, 0x80, 0x8e,
	0x82, 0x10, 0x8, 0xd0, 0x80, 0x82, 0x8a, 0x82,
	0x80, 0x82, 0x90, 0x2, 0x10, 0x82, 0x28, 0x92,
	0x28, 0x10, 0x20, 0x8, 0x10, 0x20, 0x0, 0x0,
	0x0, 0x7c, 0x82, 0x80, 0x82, 0xfe, 0x10, 0x84,
	0x42, 0x10, 0x10, 0x60, 0x10, 0x92, 0x42, 0x82,
	0x82, 0x82, 0x40, 0x7c, 0x20, 0x84, 0x28, 0x92,
	0x18, 0x82, 0x10, 0x10, 0x10, 0x10, 0x4c, 0x0,
	0x0, 0x40, 0x7c, 0x0, 0x80, 0x10, 0x10, 0xfe,
	0x10, 0xfe, 0x18, 0x10, 0x44, 0x44, 0x44, 0x7c,
	0x94, 0x44, 0x48, 0x28, 0x24, 0x28, 0x22, 0x88,
	0x92, 0xc, 0x60, 0x82, 0x48, 0x0, 0x24, 0x12,
	0x80, 0x0, 0x44, 0x24, 0xfe, 0x10, 0x0, 0x0,
	0x0, 0x10, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x28, 0x92, 0x4a, 0x8c, 0x0,
	0x8, 0x20, 0x54, 0x10, 0x0, 0x0, 0x0, 0x20,
	0x64, 0x10, 0x20, 0x4, 0x4, 0x4, 0x44, 0x10,
	0x44, 0x4, 0x0, 0x0, 0x20, 0x7c, 0x8, 0x10,
	0xaa, 0x82, 0x82, 0x80, 0x82, 0x80, 0x80, 0x82,
	0x82, 0x10, 0x8, 0x88, 0x80, 0x82, 0x8a, 0x82,
	0x80, 0x82, 0x88, 0x2, 0x10, 0x82, 0x28, 0x92,
	0x44, 0x10, 0x40, 0x8, 0x8, 0x20, 0x0, 0x0,
	0x0, 0x84, 0x82, 0x80, 0x82, 0x80, 0x10, 0x78,
	0x42, 0x10, 0x10, 0x50, 0x10, 0x92, 0x42, 0x82,
	0x82, 0x82, 0x40, 0x2, 0x20, 0x84, 0x28, 0x92,
	0x18, 0x44, 0x20, 0x8, 0x10, 0x20, 0x0, 0x0,
	0x0, 0x80, 0x0, 0x0, 0x5e, 0xfe, 0x10, 0xc,
	0x7c, 0x60, 0x24, 0x20, 0x44, 0x38, 0x44, 0x0,
	0x88, 0x42, 0x44, 0x28, 0x24, 0x28, 0x22, 0x88,
	0x92, 0x0, 0x0, 0x82, 0x28, 0x0, 0x28, 0x24,
	0x42, 0x0, 0x44, 0x18, 0x7c, 0x28, 0x0, 0x0,
	0x0, 0x7c, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x7c, 0x8a, 0x8c, 0x0,
	0x8, 0x20, 0x10, 0x10, 0x30, 0x0, 0x30, 0x40,
	0x44, 0x10, 0x40, 0x44, 0x4, 0x8, 0x44, 0x20,
	0x44, 0x8, 0x30, 0x30, 0x10, 0x0, 0x10, 0x0,
	0x9c, 0x82, 0x82, 0x42, 0x84, 0x80, 0x80, 0x42,
	0x82, 0x10, 0x88, 0x84, 0x80, 0x82, 0x86, 0x44,
	0x80, 0x44, 0x84, 0x82, 0x10, 0x82, 0x10, 0xaa,
	0x82, 0x10, 0x82, 0x8, 0x4, 0x20, 0x0, 0x0,
	0x0, 0x84, 0xc4, 0x42, 0x46, 0x40, 0x10, 0x80,
	0x42, 0x10, 0x10, 0x48, 0x10, 0x92, 0x42, 0x44,
	0xc4, 0x46, 0x40, 0x82, 0x20, 0x84, 0x10, 0xaa,
	0x24, 0x28, 0x44, 0x8, 0x10, 0x20, 0x0, 0x0,
	0x0, 0x0, 0x7c, 0x0, 0x30, 0x40, 0x10, 0x8,
	0x38, 0x20, 0x0, 0x40, 0x82, 0x0, 0x0, 0x10,
	0x98, 0x42, 0x44, 0x44, 0x24, 0x28, 0x64, 0x88,
	0x92, 0xfc, 0x7e, 0x44, 0x18, 0x0, 0x30, 0x48,
	0x3c, 0x0, 0x7c, 0x0, 0x38, 0x44, 0x0, 0x0,
	0x0, 0x38, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x0, 0x0, 0x10, 0x4, 0x72, 0x0,
	0x4, 0x40, 0x0, 0x0, 0x30, 0x0, 0x30, 0x80,
	0xb8, 0x38, 0x7c, 0x38, 0x4, 0x70, 0x38, 0x20,
	0x38, 0x30, 0x30, 0x30, 0x8, 0x0, 0x20, 0x10,
	0x40, 0x82, 0xfc, 0x3c, 0xf8, 0xfe, 0x80, 0x3c,
	0x82, 0x7c, 0x70, 0x82, 0xfe, 0x82, 0x82, 0x38,
	0x80, 0x38, 0x82, 0x7c, 0x10, 0x7c, 0x10, 0x44,
	0x82, 0x10, 0xfe, 0x8, 0x2, 0x20, 0x0, 0x0,
	0x0, 0x7a, 0xb8, 0x3c, 0x3a, 0x3e, 0x38, 0x7c,
	0x42, 0x38, 0x10, 0xc6, 0x38, 0x92, 0x42, 0x38,
	0xb8, 0x3a, 0x40, 0x7c, 0x1c, 0x7a, 0x10, 0x44,
	0x42, 0x10, 0xfc, 0x8, 0x10, 0x20, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x10, 0x80, 0x10, 0x0,
	0x10, 0x0, 0x0, 0xfe, 0xfe, 0x0, 0x0, 0x0,
	0x66, 0x7c, 0x38, 0x44, 0x3a, 0x28, 0x58, 0x70,
	0x6c, 0x0, 0x0, 0x38, 0x8, 0x0, 0x20, 0x90,
	0x0, 0x0, 0x0, 0x0, 0x10, 0x82, 0x0, 0x0,
	0x0, 0x10, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x2, 0x80, 0x0, 0x0, 0x10, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x10, 0x0, 0x0, 0x0, 0x0,
	0x38, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0xe, 0x0, 0xe0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x82,
	0x0, 0x0, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x80, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x20, 0x0, 0x8, 0x0, 0x20, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x40, 0x0, 0x0, 0x20, 0x0, 0x40, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x4, 0x0, 0x40, 0xa0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x20, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x20, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0xc, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0xfe,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x7c,
	0x0, 0x0, 0x10, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x80, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x40, 0x0, 0x6, 0x0, 0xc0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x40, 0x0, 0x0, 0x20, 0x0, 0x80, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0xc0,
	0xfe, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x8,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x60, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x80, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x80, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x80, 0x0, 0x0, 0xc0, 0x0, 0x80, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x10,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
	0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0
	};

	public void SendRawKey (int key)
	{
		transmit_buffer[0] = (byte) key;
		if	(!lw_suppress && levelone_network != null)
			levelone_network.write(transmit_buffer,0,1);
	}

	public void SendESCKey(int key)
	{
		transmit_buffer[0] = 0x1b;
		transmit_buffer[1] = (byte) key;
		if	(!lw_suppress && levelone_network != null)
			levelone_network.write(transmit_buffer,0,2);
	}

	public void AltKeyDown (KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_R:
				SendUsm (2);
				break;
		}
	}

	public void ControlKeyDown (KeyEvent e)
	{
	boolean	shift = e.isShiftDown();

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ENTER:
				SendKey(shift? KEY_CR : KEY_TAB);
				break;

			case KeyEvent.VK_0:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('0');
				}
				else
					SendKey('<');
				break;
			case KeyEvent.VK_1:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('1');
				}
				else
					SendKey('>');
				break;
			case KeyEvent.VK_2:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('2');
				}
				else
					SendKey('[');
				break;
			case KeyEvent.VK_3:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('3');
				}
				else
					SendKey(']');
				break;
			case KeyEvent.VK_4:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('4');
				}
				else
					SendKey('$');
				break;
			case KeyEvent.VK_5:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('5');
				}
				else
					SendKey('%');
				break;
			case KeyEvent.VK_6:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('6');
				}
				else
					SendKey('_');
				break;
			case KeyEvent.VK_7:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('7');
				}
				else
					SendKey(0x7c);
				break;
			case KeyEvent.VK_8:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('8');
				}
				else
					SendKey('*');
				break;
			case KeyEvent.VK_9:
				if (shift)
				{
					SendKey(KEY_ACCESS);
					SendKey(KEY_SHIFT);
					SendKey('9');
				}
				else
					SendKey('(');
				break;
			case KeyEvent.VK_A:
	 			SendKey(shift ? KEY_TERM : KEY_ANS);
				break;
			case KeyEvent.VK_B:
				SendKey(shift ? KEY_BACK1 : KEY_BACK);
				break;
			case KeyEvent.VK_C:
				SendKey(shift ? KEY_COPY1 : KEY_COPY);
				break;
			case KeyEvent.VK_D:
				SendKey(shift ? KEY_DATA1 : KEY_DATA);
				break;
			case KeyEvent.VK_E:
				SendKey(shift ? KEY_EDIT1 : KEY_EDIT);
				break;
			case KeyEvent.VK_F:
				if (shift)
					SendESCKey(0);
				else
					SendKey(KEY_FONT);
				break;
			case KeyEvent.VK_G:
				SendKey(shift ? KEY_DIV1 : KEY_DIV);
				break;
			case KeyEvent.VK_H:
				SendKey(shift ? KEY_HELP1 : KEY_HELP);
				break;
			case KeyEvent.VK_I:
				SendESCKey(shift ? 2 : 1);
				break;
			case KeyEvent.VK_J:
				SendESCKey(shift ? 011 : 006);
				break;
			case KeyEvent.VK_K:
				SendESCKey(shift ? 013 : 012);
				break;
			case KeyEvent.VK_L:
				SendKey(shift ? KEY_LAB1 : KEY_LAB);
				break;
			case KeyEvent.VK_M:
				SendKey(shift ? KEY_FONT : KEY_MICRO);
				break;
			case KeyEvent.VK_N:
				SendESCKey(shift ? 015 : 014);
				break;
			case KeyEvent.VK_O:
				SendESCKey(shift ? 017 : 016);
				break;
			case KeyEvent.VK_P:
				SendKey(shift ? KEY_SUPER1 : KEY_SUPER);
				break;
			case KeyEvent.VK_Q:
				SendKey(shift ? KEY_ACCESS : KEY_SQUARE);
				break;
			case KeyEvent.VK_R:
				SendESCKey(shift ? 022 : 020);
				break;
			case KeyEvent.VK_S:
				SendKey(shift ? KEY_STOP1 : KEY_STOP);
				break;
			case KeyEvent.VK_T:
				if (shift)
					SendESCKey(024);
				else
					SendKey(KEY_TERM);
				break;
			case KeyEvent.VK_U:
				SendESCKey(shift ? 026 : 025);
				break;
			case KeyEvent.VK_V:
				SendESCKey(shift ? 031 : 030);
				break;
			case KeyEvent.VK_W:
				SendESCKey(shift ? 034 : 032);
				break;
			case KeyEvent.VK_X:
				SendKey(shift ? KEY_MULT1 : KEY_MULT);
				break;
			case KeyEvent.VK_Y:
				SendKey(shift ? KEY_SUB1 : KEY_SUB);
				break;
			case KeyEvent.VK_Z:
				SendESCKey(shift ? 052 : 037);
				break;
			case KeyEvent.VK_SUBTRACT:
				SendKey(KEY_DELTA);
				break;
			case KeyEvent.VK_ADD:
				SendKey(KEY_SIGMA);
				break;
		}
	}

	/**
	 * Hanel cursor keypress.
	 *
	 * @param
	 * @param
	 */
	public void SendCursor(
		boolean shift,
		int type)
	{
		if (is_text_key_mode)
		{
			transmit_buffer[0] = 0x1b;
			if (shift)
				transmit_buffer[1] = (byte) (' ' + type);
			else
				transmit_buffer[1] = (byte) ('0' + type);

			if	(!lw_suppress && levelone_network != null)
				levelone_network.write (transmit_buffer,0,2);
		}
		else
		{
			switch (type)
			{
				case 0:	// up
					SendRawKey(shift ? 'W' : 'w');
					break;
				case 1:	// down
					SendRawKey(shift ? 'X' : 'x');
					break;
				case 2:	// right
					SendRawKey(shift ? 'D' : 'd');
					break;
				case 3:	// left
					SendRawKey(shift ? 'A' : 'a');
					break;
				case 4:	// ins
					SendKey(shift ? KEY_DIV1 : KEY_DIV);
					break;
				case 5:	// del
					SendKey(shift ? KEY_MULT1 : KEY_MULT);
					break;
				case 8:	// pageup
					SendKey(shift ? KEY_SUPER1 : KEY_SUPER);
					break;  
				case 9:	// pagedown
					SendKey(shift ? KEY_SUB1 : KEY_SUB);
					break;
			}
		}
	}

	public void ShiftKeyDown (KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ENTER:
				SendKey(KEY_NEXT1);
				break;
			case KeyEvent.VK_UP:
				SendCursor(true,0);
				break;		  
			case KeyEvent.VK_DOWN:
				SendCursor(true,1);
				break;	 
			case KeyEvent.VK_RIGHT:
				SendCursor(true,2);
				break;
			case KeyEvent.VK_LEFT:
				SendCursor(true,3);
				break;
			case KeyEvent.VK_INSERT:
				SendCursor(true,4);
				break;
			case KeyEvent.VK_DELETE:
				SendCursor(true,5);
				break;
			case KeyEvent.VK_HOME:
				SendCursor(true,6);
				break;
			case KeyEvent.VK_END:
				SendCursor(true,7);
				break;
			case KeyEvent.VK_PAGE_UP:
				SendCursor(true,8);
				break;
			case KeyEvent.VK_PAGE_DOWN:
				SendCursor(true,9);
				break;
			case KeyEvent.VK_SPACE:
				SendKey(KEY_BACKSPACE);
				break;
			case KeyEvent.VK_ESCAPE:
				SendKey(KEY_SHIFT);
				break;
			case KeyEvent.VK_TAB:
				SendKey(KEY_CR);
				break;
			case KeyEvent.VK_BACK_SPACE:
				SendKey(KEY_ERASE1);
				break;
			case KeyEvent.VK_F1:
				SendKey(KEY_COPY1);
				break;
			case KeyEvent.VK_F2:
				SendKey(KEY_TERM);
				break;
			case KeyEvent.VK_F3:
				SendKey(KEY_ACCESS);
				break;
			case KeyEvent.VK_F4:
				SendKey(KEY_FONT);
				break;
			case KeyEvent.VK_F5:
				SendKey(KEY_EDIT1);
				break;
			case KeyEvent.VK_F6:
				SendKey(KEY_HELP1);
				break;
			case KeyEvent.VK_F7:
				SendKey(KEY_LAB1);
				break;
			case KeyEvent.VK_F8:
				SendKey(KEY_BACK1);
				break;
			case KeyEvent.VK_F9:
				SendKey(KEY_DATA1);
				break;
			case KeyEvent.VK_F10:
			case KeyEvent.VK_F12:
				SendKey(KEY_STOP1);
				break;
			case KeyEvent.VK_F11:
				SendKey(KEY_COPY1);
				break;
			case KeyEvent.VK_ADD:
				SendKey(KEY_SIGMA);
				break;
			case KeyEvent.VK_SUBTRACT:
				SendKey(KEY_DELTA);
				break;
			default:
				if (e.getKeyChar() >= 32 && e.getKeyChar() <= 127)
					sendAsciiDelay(e.getKeyChar(),0);
		}
	}

	public void NormKeyDown (KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ENTER:
				SendKey(KEY_NEXT);
				break;
			case KeyEvent.VK_UP:
				SendCursor(false,0);
				break;
			case KeyEvent.VK_DOWN:
				SendCursor(false,1);
				break;
			case KeyEvent.VK_RIGHT:
				SendCursor(false,2);
				break;
			case KeyEvent.VK_LEFT:
				SendCursor(false,3);
				break;
			case KeyEvent.VK_INSERT:
				SendCursor(false,4);
				break;
			case KeyEvent.VK_DELETE:
				SendCursor(false,5);
				break;
			case KeyEvent.VK_HOME:
				SendCursor(false,6);
				break;
			case KeyEvent.VK_END:
				SendCursor(false,7);
				break;
			case KeyEvent.VK_PAGE_UP:
				SendCursor(false,8);
				break;
			case KeyEvent.VK_PAGE_DOWN:
				SendCursor(false,9);
				break;
			case KeyEvent.VK_ESCAPE:
				SendKey(KEY_ASSIGN);
				break;
			case KeyEvent.VK_TAB:
				SendKey(KEY_TAB);
				break;
			case KeyEvent.VK_BACK_SPACE:
				SendKey(KEY_ERASE);
				break;
			case KeyEvent.VK_F1:
				SendKey(KEY_COPY);
				break;
			case KeyEvent.VK_F2:
				SendKey(KEY_ANS);
				break;
			case KeyEvent.VK_F3:
				SendKey(KEY_SQUARE);
				break;
			case KeyEvent.VK_F4:
				SendKey(KEY_MICRO);
				break;
			case KeyEvent.VK_F5:
				SendKey(KEY_EDIT);
				break;
			case KeyEvent.VK_F6:
				SendKey(KEY_HELP);
				break;
			case KeyEvent.VK_F7:
				SendKey(KEY_LAB);
				break;
			case KeyEvent.VK_F8:
				SendKey(KEY_BACK);
				break;
			case KeyEvent.VK_F9:
				SendKey(KEY_DATA);
				break;
			case KeyEvent.VK_F10:
			case KeyEvent.VK_F12:
				SendKey(KEY_STOP);
				break;
			case KeyEvent.VK_F11:
				SendKey(KEY_COPY);
				break;
			case KeyEvent.VK_SPACE:
				SendKey(KEY_SPACE);
				break;
			case KeyEvent.VK_ADD:
				SendKey('+');
				break;
			case KeyEvent.VK_SUBTRACT:
				SendKey('-');
				break;
			default:
				if (e.getKeyChar() >= 32 && e.getKeyChar() <= 127)
					sendAsciiDelay(e.getKeyChar(),0);
		}
	}

	public void keyPressed (KeyEvent e)
	{
		if (e.isAltDown())
			AltKeyDown(e);
		else if (e.isControlDown())
			ControlKeyDown(e);
		else if (e.isShiftDown())
			ShiftKeyDown(e);
		else
			NormKeyDown(e);
	}

	private long[] stringToCybers(String input)
	{
		if (input == null || input.length() < 21)
			return null;

	long[] converted = new long[input.length()/21];

		for (int i=0;i<input.length()/21;i++)
			converted[i] = Long.parseLong(input.substring(i*21,i*21+21),8) & 077777777777777777777L;

		return converted;
	}

	/**
	 * Convert array of bytes into array of longs. Every eight bytes will map
	 * to a long.
	 *
	 * @param	bytes	The bytes to be converted into array of longs.
	 * @return			The converted array of longs.
	 */
	private long[] convert(byte[] bytes)
	{
		// Every eight bytes map to a long.
		long[] converted_longs = new long[(int)Math.ceil(bytes.length/8D)];
		long four_bit_value = 0L;
		int start = 0;
		int left_shift = 60;

		for (int index = 0; index < bytes.length; index++)
		{
			if (0 > left_shift)
			{
				converted_longs[++start] = 0L;
				left_shift = 60;
			}

			four_bit_value = (bytes[index] & 0xf0L) >> 4;
			converted_longs[start] |= four_bit_value << left_shift;
			left_shift -= 4;

			if (0 > left_shift)
			{
				converted_longs[++start] = 0L;
				left_shift = 60;
			}

			four_bit_value = bytes[index] & 0x0fL;
			converted_longs[start] |= four_bit_value << left_shift;
			left_shift -= 4;
		}

		return converted_longs;
	}
	
	/**
	 * Convert array of longs to array of bytes. Every long will map to eight
	 * bytes.
	 *
	 * @param	longs	The array of longs to be converted.
	 * @param	offset	The offset (to the array of longs) to start converting.
	 * @param	size	Number of longs to be converted.
	 * @return			The converted array of bytes.
	 */
  	private byte[] convert(
		long[] longs,
		int offset,
		int size)
  	{
  		byte[] converted_bytes = new byte[8*size];
  		int start = 0;
		int end_index = offset+size;

  		for (; offset < end_index; offset++)
  		{
			for (int right_shift = 56; right_shift >= 0; right_shift -= 8)
			{
				converted_bytes[start++] =
					(byte)(longs[offset] >> right_shift);
			}
  		}

		return converted_bytes;
  	}


}
