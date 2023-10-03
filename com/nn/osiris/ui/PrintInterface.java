/*
 * PrintInterface.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Font.*;

/**
 * Class that implements the L1P printing methods,
 * using the earliest AWT printing routines. These
 * are not supported on the macintosh, so this
 * interface was born so we can emulate the behavior
 * on that platform using less ideal methods.
 */
class PrintInterface
{
	LevelOneParser	engine;
	/** Last print result (-print-). */
	protected int prerror;

	/** Print job in progress (-print-). */
	private PrintJob print_job;
	/** Graphics for current page (-print-). */
	protected Graphics print_graphics;
	protected String print_font;
	protected boolean print_font_invalid;
	protected FontMetrics print_font_metrics;
	protected int print_font_size;
	protected int print_font_style;
	protected int print_pen_x;
	protected int print_pen_y;

	/**
	 * Constructor.
	 */
	public PrintInterface()
	{
	}

	public void setEngine(LevelOneParser engine)
	{
		this.engine = engine;
	}

	/**
	 *
	 *	Prepare to print new document.
	 *
	 */
	public void openDoc()
	{
		closeDoc();
		prerror = 0;
		print_job = java.awt.Toolkit.getDefaultToolkit().getPrintJob(
			engine.parent_frame,
			"portal doc",
			null);

		print_font = "Serif";
		print_font_size = 12;
		print_font_style = 0;

		if (null == print_job)
			prerror = 1;
	}

	/**
	 *
	 *	Prepare to print new page.
	 *
	 */
	public void openPage()
	{
		if (null != print_job)
		{
			closePage();
			print_graphics = print_job.getGraphics();
			if (null != print_graphics)
			{
				print_graphics.setColor(Color.black);
				print_font_invalid = true;
				print_pen_x = print_pen_y = 0;
			}
			else
				prerror = 1;
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Print current page.
	 *
	 */
	public void closePage()
	{
		if (null != print_graphics)
		{
			print_graphics.dispose();
			print_graphics = null;
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	End document (print it).
	 *
	 */
	public void closeDoc()
	{
		if (null != print_job)
		{
			if (null != print_graphics)
			{
				print_graphics.dispose();
				print_graphics = null;
			}

			print_job.end();
			print_job = null;
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Realizes the selected font.
	 *
	 */
	private void PRealizeFont()
	{
		if (print_font_invalid && null != print_graphics)
		{
			print_font_invalid = false;

			if (print_font.equals("NovaFixed"))
				print_font = "Monospaced";
			else if (print_font.equals("NovaProportional"))
				print_font = "Serif";

		int java_style;
		
			if ((print_font_style & 6) == 6)
				java_style = Font.BOLD | Font.ITALIC;
			else if ((print_font_style & 4) == 4)
				java_style = Font.BOLD;
			else if ((print_font_style & 2) == 2)
				java_style = Font.ITALIC;
			else
				java_style = Font.PLAIN;

			print_graphics.setFont(new Font(print_font,java_style,print_font_size));
			print_font_metrics = print_graphics.getFontMetrics();
		}
	}

	/**
	 *
	 *	Select font for printing.
	 *
	 */
	public void textFont(String filename)
	{
		print_font = filename;
		print_font_invalid = true;
	}

	/**
	 *
	 *	Select font size.
	 *
	 */
	public void textSize(int size)
	{
		print_font_size = size;
		print_font_invalid = true;
	}

	/**
	 *
	 *	Plot string on printed page.
	 *
	 */
	public void drawText(String s)
	{
		if (null != print_graphics)
		{
			PRealizeFont();
			print_graphics.drawString(s,print_pen_x,print_pen_y);
			print_pen_x += print_font_metrics.stringWidth(s);
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Determine width of text string.
	 *
	 */
	public void textWidth(String s)
	{
		if (null != print_graphics)
		{
			PRealizeFont();
			engine.SendPixRes(0,print_font_metrics.stringWidth(s),0,0);
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Return ascent for current font.
	 *
	 */
	public void fontAscent()
	{
		if (null != print_graphics)
		{
			PRealizeFont();
			engine.SendPixRes (0,print_font_metrics.getAscent(),0,0);
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Return descent for current font.
	 *
	 */
	public void fontDescent()
	{
		if (null != print_graphics)
		{
			PRealizeFont();
			engine.SendPixRes (0,print_font_metrics.getDescent(),0,0);
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Return leading for current font.
	 *
	 */
	public void fontLeading()
	{
		if (null != print_graphics)
		{
			PRealizeFont();
			engine.SendPixRes (0,print_font_metrics.getLeading(),0,0);
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Establish printer pen location.
	 *
	 */
	public void moveTo(int x,int y)
	{
		print_pen_x = x;
		print_pen_y = y;
	}

	/**
	 *
	 *	Draw line on printer.
	 *
	 */
	public void lineTo(int x,int y)
	{
		if (null != print_graphics)
		{
			print_graphics.drawLine(print_pen_x,print_pen_y,x,y);
			print_pen_x = x;
			print_pen_y = y;
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Return page size in printer coordinates.
	 *
	 */
	public void pageSize()
	{
		if (null != print_job)
		{
		Dimension	pdim = print_job.getPageDimension();

			engine.SendPixRes(0,pdim.width,pdim.height,0);
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Return dpi for x/y.
	 *
	 */
	public void pageResolution()
	{
		if (null != print_job)
		{
			engine.SendPixRes (0,print_job.getPageResolution(),print_job.getPageResolution(),0);
		}
		else
			prerror = 1;
	}

	/**
	 *
	 *	Return printer pen location.
	 *
	 */
	public void getPen()
	{
		engine.SendPixRes (0,print_pen_x,print_pen_y,0);
	}

	/**
	 *
	 *	Select font attributes.
	 *
	 */
	public void textStyle(int style)
	{
		print_font_style = style;
		print_font_invalid = true;
	}

	/**
	 *
	 *	Check for errors during printing.
	 *
	 */
	void error()
	{
		if (prerror != 0)
			engine.SendEcho (3);
		else
			engine.SendEcho (0);
		prerror = 0;
	}

	/**
	 *
	 *	Cancel print job.
	 *
	 */
	void cancel()
	{
		closeDoc();
	}
}