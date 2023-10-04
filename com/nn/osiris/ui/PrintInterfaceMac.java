/*
 * PrintInterfaceMac.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.awt.*;
//import java.awt.event.*;
import java.awt.image.*;
//import java.awt.Font.*;

/**
 * Class that implements the L1P printing methods,
 * for macintosh which doesn't support the
 * java.awt.PrintJob class.
 */
class PrintInterfaceMac extends PrintInterface
{
	/** Graphics for current page (-print-). */
	private int printWidth = 540;
	private int printHeight = 720;
	private int printDPI = 144;
	private Image offscreen = null;
	private boolean page_open = false;

	/**
	 * Constructor.
	 */
	public PrintInterfaceMac()
	{
	}


	/**
	 *
	 *	Prepare to print new document.
	 *
	 */
	public void openDoc()
	{
	java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
	java.awt.print.PageFormat	pf = job.defaultPage();

		// we print in double the "natural" dimensions so the image
		// has to be shrunk to the paper. with java 1.3/1.4 on mac,
		// awt printing doesn't print images properly if they
		// aren't scaled...go figure!
		printWidth = 2*(int) pf.getImageableWidth();
		printHeight = 2*(int) pf.getImageableHeight();
		job.cancel();

		closeDoc();
		prerror = 0;
		print_font = "Serif";
		print_font_size = 12;
		print_font_style = 0;
		offscreen = new BufferedImage(printWidth,printHeight,BufferedImage.TYPE_INT_RGB);
		print_graphics = offscreen.getGraphics();
	}

	/**
	 *
	 *	Prepare to print new page.
	 *
	 */
	public void openPage()
	{
		if (null != offscreen && null != print_graphics)
		{
			print_graphics.setColor(Color.white);
			print_graphics.fillRect(0,0,printWidth,printHeight);
			print_graphics.setColor(Color.black);
			print_font_invalid = true;
			print_pen_x = print_pen_y = 0;
			page_open = true;
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
		if (null != offscreen && page_open)
		{
		PortalFrame	pf = (PortalFrame) engine.parent_frame;

			pf.printImage(offscreen,false);
			page_open = false;
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
		page_open = false;
		if (null != offscreen)
		{
			if (null != print_graphics)
			{
				print_graphics.dispose();
				print_graphics = null;
			}

			offscreen = null;
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
		engine.SendPixRes(0,printWidth,printHeight,0);
	}

	/**
	 *
	 *	Return dpi for x/y.
	 *
	 */
	public void pageResolution()
	{
		engine.SendPixRes (0,printDPI,printDPI,0);
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