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
class PortalConsts
{
	// version string displayed in dialog and sent to system via -local-
	static public String	short_version = "3.4.3";
	static public String	options_file = "portopt.cfg";
	// user-specific configuration file name
	static public String	user_config_file = "portal.cfg";
	// global configuration file name
	static public String	global_config_file = "portal.cfg";
	// macintosh creator code for portal
	static public String	creator_string = "L2pP";
	static public int		creator_code = 0x4c327050;	// L2pP
	// macintosh config file type for portal
	static public String	config_string = "L2pF";
	static public int		config_code = 0x4c327046;	// L2pF
	// default width of portal window
	static final int		default_width = 640;

	static public boolean	is_macintosh = false;
	static public boolean	is_windows = false;
	static public boolean	is_debugging = true;
	static public boolean	is_applet = false;
	static public boolean	is_quicktime = false;
	// Default tcp host & port to connect to:
	static final String	default_host = "nohost.nn.com";
	static final int default_port = 6005;
}
