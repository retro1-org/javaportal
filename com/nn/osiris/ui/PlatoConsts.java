/*
 * PortalConsts.java
 *
 * Started 1999
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

/**
 * Class for values that are constant during a single
 * instance of the portal, but they may be set during
 * application startup.
 */
class PlatoConsts
{
	
	static final public String	theName = "PlatoAccess";
	
	// version string displayed in dialog and sent to system via -local-
	static public String	short_version = "0.1.0 beta";
	static public String	options_file = "portopt.cf2";
	// user-specific configuration file name
	static public String	user_config_file = "jpterm.cfg";
	// global configuration file name
	static public String	global_config_file = "jpterm.cfg";
	// macintosh creator code for portal
	static public String	creator_string = "L2pP";
	static public int		creator_code = 0x4c327050;	// L2pP
	// macintosh config file type for portal
	static public String	config_string = "L2pF";
	static public int		config_code = 0x4c327046;	// L2pF
	// default width of portal window
	static final int		default_width = 640;
	
	static  float  			SCALE = (float)1; 

	static public boolean	is_threaded = false;		// for Z80  -- currently ignored
	static public boolean	is_macintosh = false;
	static public boolean	is_windows = false;
	static public boolean	is_debugging = true;
	static public boolean	is_applet = false;
	static public boolean	is_quicktime = false;
	// Default tcp host & port to connect to:
	static final String	default_host = "cybis.retro1.net";
	static final int default_port = 8005;
	
	
	
	// HELP strings
	static final String codexroot = "https://codex.retro1.org/plato:portal.help:platoaccess.help:start";
	static final String sessHelp = "https://codex.retro1.org/plato:portal.help:platoaccess.help:configfiles";
	static final String optHelp = "https://codex.retro1.org/plato:portal.help:platoaccess.help:sizing";
	static final String fileHelp = "https://codex.retro1.org/plato:portal.help:platoaccess.help:filemenu";
	static final String tabsHelp = "https://codex.retro1.org/plato:portal.help:platoaccess.help:multiconnect";
	static final String editHelp = "https://codex.retro1.org/plato:portal.help:platoaccess.help:editmenu";
	static final String settingsHelp = "https://codex.retro1.org/plato:portal.help:platoaccess.help:settings";
	static final String keysHelp = "https://codex.retro1.org/plato:portal.help:platoaccess.help:keyboard";
	
	
	// The rest of this file is for executing Z80 code - MTUTOR
	
	// time slicing initial values
	static final int skipper_mod = 3;			// number of data processing loops (plato bytes) before giving z80 a time slice.
	static final long z80_loops = 500000;		// number of z80 instructions / time slice
	static public final int r_exec_mod = 5;
	
	// Resident entry points.
	
	static final int R_MAIN =  0x3d;    // Fake return address for mode 5/6/7 call
	static final int R_INIT =  0x40;
	static final int R_DOT =   0x43;
	static final int R_LINE =  0x46;
	static final int R_CHARS = 0x49;
	static final int R_BLOCK = 0x4c;
	static final int R_INPX =  0x4f;
	static final int R_INPY =  0x52;
	static final int R_OUTX =  0x55;
	static final int R_OUTY =  0x58;
	static final int R_XMIT =  0x5b;
	static final int R_MODE =  0x5e;
	static final int R_STEPX = 0x61;
	static final int R_STEPY = 0x64;
	static final int R_WE    = 0x67;
	static final int R_DIR   = 0x6a;
	static final int R_INPUT = 0x6d;
	static final int R_SSF   = 0x70;
	static final int R_CCR   = 0x73;
	static final int R_EXTOUT= 0x76;
	static final int R_EXEC  = 0x79;
	static final int R_GJOB  = 0x7c;
	static final int R_XJOB  = 0x7f;
	static final int R_RETURN =0x82;  // obsolete
	static final int R_CHRCV = 0x85;
	static final int R_ALARM = 0x88;
	static final int R_PRINT = 0x8b;
	static final int R_FCOLOR= 0x8e;
	static final int R_BCOLOR= 0x91;
	static final int R_PAINT=  0x94;
	static final int R_WAIT16= 0x97;
	static final int R_EXTEND= 0x9a;
	static final int R_DUMMY3= 0x9d;

	// Interrupt mask bits
	static final int  IM_SIR = 0x80;
	static final int  IM_KST = 0x40;
	static final int  IM_TP  = 0x20;
	static final int  IM_EXT0= 0x08;
	static final int  IM_CON = 0x02;
	static final int  IM_CAR = 0x01;
	
	// Initial SP
	static final int INITSP = 0xffff;      /* To make microTutor not scribble on the stack */       //0x2200
	
	// PPT Resident variables
	static final int  M_FLAG0 = 0x22ea;
	static final int M_TYPE  = 0x22eb;
	static final int M_CLOCK = 0x22ec;
	static final int M_EXTPA = 0x22ee;
	static final int M_MARGIN = 0x22f0;
	static final int M_JOBS = 0x22f2;
	static final int M_CCR  = 0x22f4;
	static final int M_MODE = 0x22f6;
	static final int M_DIR  = 0x22f8;
	static final int M_KSW  = 0x22fa;
	static final int M_ENAB = 0x22fc;
	
	static final int WORKRAM = 0x02000;
	
	static final int CSETS   =        8;       // leave room for the PPT multiple sets

	static final int M2ADDR    =      0x2340;  // PPT start address for set 2
	static final int M3ADDR   =       0x2740;  // PPT start address for set 3

	static final int M5ORIGIN   =     (WORKRAM + 0x0300);  // Pointer to mode 5 program
	static final int M6ORIGIN   =     (WORKRAM + 0x0302);  // Pointer to mode 6 program
	static final int M7ORIGIN   =     (WORKRAM + 0x0304);  // Pointer to mode 7 program

	static final int C2ORIGIN   =     (WORKRAM + 0x0306);  // Pointer to M2 characters
	static final int C3ORIGIN   =     (WORKRAM + 0x0308);  // Pointer to M3 characters
	static final int C4ORIGIN   =     (WORKRAM + 0x030a);  // Pointer to M4 characters
	static final int C5ORIGIN   =     (WORKRAM + 0x030c);  // Pointer to M5 characters
	static final int C6ORIGIN   =     (WORKRAM + 0x030e);  // Pointer to M6 characters
	static final int C7ORIGIN   =     (WORKRAM + 0x0310);  // Pointer to M7 characters

	
	static final int TM_HALT   =  (WORKRAM + 0x0314);
	static final int TM_STATUS =  (WORKRAM + 0x0316);
	
	// PPT input port addresses
	static final int SIO     =0x00;
	static final int COMSTAT =0x01;
	static final int INTVECT =0x02;
	static final int KST     =0x04;
	static final int TP      =0x05;
	static final int XL      =0x10;
	static final int XU      =0x11;
	static final int YL      =0x12;
	static final int YU      =0x13;

	
	// PPT output port addresses
	
	//static final int SIO     =0x00;
	//static final int COMSTAT =0x01;
	//static final int INTVECT =0x02;
	
	static final int IMASK =  0x03;
	static final int XLONG =  0x08;
	static final int YLONG =  0x09;
	static final int SETXR =  0x0a;
	static final int SETXF =  0x0b;
	static final int SETYR =  0x0c;
	static final int SETYF =  0x0d;
	static final int SETABT=  0x0e;
	static final int CLRABT=  0x0f;
	
//	static final int XL    =  0x10;
//	static final int XU    =  0x11;
//	static final int YL    =  0x12;
//	static final int YU    =  0x13

	static final int PDL   =  0x14;
	static final int PDU   =  0x15;
	static final int PDM   =  0x16;
	static final int PDLU  =  0x17;
	static final int CLOCKX=  0x18;
	static final int CLOCKY=  0x19;
	static final int CLOCKXY= 0x1a;
	static final int CLOCKL = 0x1b;
	static final int HCHAR  = 0x1c;
	static final int VCHAR  = 0x1d;
	static final int WE     = 0x1e;
	static final int SCREEN = 0x1f;
	static final int SLIDEL = 0x20;
	static final int SLIDEU = 0x21;
	
	// These used for patching levels of mtutor

	static final int CALL8080 = 0xcd;
	static final int JUMP8080 = 0xc3;
	static final int RET8080 =  0xc9;

	static final int Level2Pause = 0x68d9;
	static final int Level3Pause = 0x6978;
	static final int Level4Pause = 0x6967;
	static final int Level56Pause = 0x6967;		// 5 and 6 same - 4 too
	
	static final int Level56Xplato = 0x6061;	// 5 and 6 same - 4 too
	static final int Level4Xplato = 0x6061;
	static final int Level3Xplato = 0x60d5;
	static final int Level2Xplato = 0x602c;

	/**
	 * GetVar address in levels 4, 5, 6 of Mtutor
	 */
	static final int Level4GetVar = 0x71e5;
	static final int Level56GetVar = 0x71eb;
	
	/**
	 * Misc addresses in levels 4, 5, 6 of Mtutor
	 */
	static final int Level4Mode6Ret = 0x5996;
	static final int Level4MainLoop = 0x6152;
	static final int Level4MainLoopPlus = 0x615d;

/**	
 *  RAM fwa of Mtutor floating point accumulator - 6 bytes long, 48 bits
 *  
 *	floating point format -           
 *	                                   
 *	1st  01 bits = mantissa sign      
 *	 next 15      = biased exponent    
 *	 next 32      = unsigned mantissa 
 */
	static final int FLOATACC = 0x7d25;	
	
	static final int MTutorLoad = 0x5300;		// MTutor RAM load address
	static final int MTutorOffset = 0x5400;		// MTutor offset on disk
	static final int MTutorBoot = 0x5306; 		// Boot RAM address for MTutor
	
	static final int MTUTLVL = 0x530a;			// base address in RAM of MTutor level

}
