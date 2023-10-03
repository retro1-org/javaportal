/*
 * QuickTimeInterface.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.awt.*;
import java.io.*;

/**
 * Base class for quicktime multi-media access,
 * to create interface object that can be called
 * whether or not quicktime is present. Derived
 * class is loaded with reflection with this
 * as a nil implementation fallback.
 */
class QuickTimeInterface
{
	public QuickTimeInterface()
	{
	}

	void setEngine(LevelOneParser engine)
	{
	}

	void dispose()
	{
	}

	Dimension ImageInfoFileMovie(String file_url)
	{
		return null;
	}

	Dimension ImageInfoFileMovie(File file_handle)
	{
		return null;
	}

	void idle()
	{
	}

	void PlotQuicktimeMoviePoster()
	{
	}

	void ImageGetFileQuicktimeMovie (Rectangle r,String file_url,int loadpal)
	{
	}

	void ImageGetFileQuicktimeMovie (Rectangle r,File file_handle,int loadpal)
	{
	}

	Image ImageGetFile(String file_url)
	{
		return null;
	}

	Image ImageGetFile(File file_handle)
	{
		return null;
	}

	void imagePause()
	{
	}

	void imageResume()
	{
	}

	void imageStop()
	{
	}

	void soundStart(String sound_url,File sound_file)
	{
	}

	void soundStop()
	{
	}

	void soundPause()
	{
	}

	void soundResume()
	{
	}
}