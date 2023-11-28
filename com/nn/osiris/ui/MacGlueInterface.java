/*
 * MacGlueInterface.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

class MacGlueInterface
{
	PlatoFrame	frame;

	public MacGlueInterface()
	{
	}

	public void init(PlatoFrame frame)
	{
	}

	public void associateConfigFile(java.io.File fref)
	{
	}

	public boolean isConfigFile(java.io.File fref)
	{
		return true;
	}

	public String getTempFolder()
	{
		return null;
	}

	public void openURL(String url)
		throws java.io.IOException
	{
	}
}