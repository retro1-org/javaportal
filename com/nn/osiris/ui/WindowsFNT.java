/*
 * WindowsFNT.java
 *
 * Started 1999
 *
 * Copyright Pearson Digital Learning
 */

package com.nn.osiris.ui;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * Class for windows bitmap font files (FNT files). Renders them
 * in java with calls like the built-in java font classes.
 */
public final class WindowsFNT
{
	/**
	 * Constructor for FNT. Reads and stores the font header data and the
	 * character glyphs.
	 *
	 * @param	nfnt_stream	The font stream.
	 */
	public WindowsFNT(InputStream nfnt_stream)
	{
		// Store handle to the input stream with the font data.
		fnt_stream = nfnt_stream;

		// Read all the header data from the FNT.
		df_version = readShort();
		df_size = readWord();
		skip(60);	// Ignore copyright string.
		df_type = readShort();
		df_points = readShort();
		df_vert_res = readShort();
		df_horiz_res = readShort();
		df_ascent = readShort();
		df_interal_leading = readShort();
		df_external_leading = readShort();
		df_italic = readByte();
		df_underline = readByte();
		df_strike_out = readByte();
		df_weight = readShort();
		df_char_set = readByte();
		df_pix_width = readShort();
		df_pix_height = readShort();
		df_pitch_and_family = readByte();
		df_avg_width = readShort();
		df_max_width = readShort();
		df_first_char = readByte();
		df_last_char = readByte();
		df_default_char = readByte();
		df_break_char = readByte();
		df_width_bytes = readShort();
		df_device = readWord();
		df_face = readWord();
		df_bits_pointer = readWord();
		df_bits_offset = readWord();
		df_reserved = readByte();
		
		// Read and store the character width/offset table.
		for (int i = 0; i < 96; i++)
		{
			char_width[i] = readShort();
			char_offset[i] = readShort();
		}
		
		// Skip any remaining data to the glyphs.
		skip(df_bits_offset-fnt_offset);
		
		// Allocate space for the glyphs.
		char_patterns = new byte[df_size-df_bits_offset];
		// Read the glyphs.
		for (int i=0; i<char_patterns.length; i++)
			char_patterns[i] = readByte();
		
		//!! Code that prints out ascii representations of the glyphs.
/*
		for (int i = 32; i < 127; i++)
		{
			int lrowmax = df_pix_height;
			int lcolmax = charWidth(i);
			
			System.out.println("char="+i+" rows="+lrowmax+" cols="+lcolmax);
			for (int lrow = 0; lrow < lrowmax; lrow++)
			{
				StringBuffer x = new StringBuffer();
				
				for (int lcol = 0; lcol < lcolmax; lcol++)
				{
					if (0 != getBit(i, lrow, lcol))
						x.append('#');
					else
						x.append(' ');
				}
				System.out.println(x.toString());
			}
			System.out.println("----------newchar-----");
		}
*/
	}

	/**
	 * Returns display width of given character in the FNT.
	 *
	 * @param	char_index	Character index.
	 * @return				The display width of a given character.
	 */
	public int charWidth(int char_index)
	{
		if (char_index < 32 || char_index > 126)
			return 0;
		else
			return char_width[char_index-32];
	}

	/**
	 * Returns height of the FNT.
	 *
	 * @return		The height of the FNT.
	 */
	public int getHeight()
	{
		return df_pix_height;
	}

	private InputStream fnt_stream;
	private long fnt_offset;

	// Extracted fields from the fnt file.
	private short df_version;
	private int df_size;
	private short df_type;
	private short df_points;
	private short df_vert_res;
	private short df_horiz_res;
	private short df_ascent;
	private short df_interal_leading;
	private short df_external_leading;
	private byte df_italic;
	private byte df_underline;
	private byte df_strike_out;
	private short df_weight;
	private byte df_char_set;
	private short df_pix_width;
	private short df_pix_height;
	private byte df_pitch_and_family;
	private short df_avg_width;
	private short df_max_width;
	private byte df_first_char;
	private byte df_last_char;
	private byte df_default_char;
	private byte df_break_char;
	private short df_width_bytes;
	private int df_device;
	private int df_face;
	private int df_bits_pointer;
	private int df_bits_offset;
	private byte df_reserved;
	private short[] char_width = new short[96];
	private short[] char_offset = new short[96];
	private byte[] char_patterns;
	
	/**
	 * Get bit from glyph of passed ascii character code.
	 *
	 * @param	char_index	Character index.
	 * @param	row			Row number.
	 * @param	col			Column number.
	 * @return				Bit from glyph of passed ascii character code.
	 */
	private int getBit(
		int char_index,
		int row,
		int col)
	{
		// Bounds check.
		if (char_index < 32 || char_index > 126)
			return 0;
		
		// Get location for the char from the ctabe entry.
		int	offset = char_offset[char_index-32]-df_bits_offset;
		
		return (char_patterns[offset+row+(col>>3)*df_pix_height] >> 
			(7-(col & 7))) & 1;
	}

	/**
	 * Renders a string in the FNT font.  Returns display width of the string
	 * rendered.
	 *
	 * @param	string			String to be drawn.
	 * @param	string_length	Number of characters in the string.
	 * @param	foreground		The foreground color to be drawn in.
	 * @param	background		The background color to be drawn in.
	 * @param	plot_x			X location to be plotted.
	 * @param	plot_y			Y location to be plotted.
	 * @param	container		Container we can create an image from.
	 * @param	graphics1		Graphics to draw the string on.
	 * @param	graphics2		Graphics to draw the string on.
	 * @param	rectangle		Rectangle boundary to draw the string.
	 * @return					The display width of the string rendered.
	 */
	public int drawString(
		byte[] string,
		int string_length,
		int foreground,
		int background,
		int plot_x,
		int plot_y,
		Container container,
		Graphics graphics1,
		Graphics graphics2,
		Rectangle rectangle)
	{
		int	pix[];
		int	x;
		int y;
		//int	index = 0;
		int	pix_value;
		int	display_width = 0;
		
		// Determine width of string, so we know how large an image to create.
		for (int i = 0; i < string_length; i++)
			display_width += charWidth(string[i]);

		/*
		 * Figure out what will be left after clipping.
		 */
		// Drawing destination before clipping applied.
		Rectangle ir = new Rectangle(
			plot_x,
			plot_y-df_pix_height+1,
			display_width,
			df_pix_height);

		// Drawing destination rectangle.
		Rectangle dr = ir.intersection(rectangle);
		
		// If clipping eliminates the entire string, we are done.
		if (dr.isEmpty())
			return display_width;
		
		// Calculate the number of pixels we skip before rendering the string.
		int	skipx = dr.x-plot_x;
		int	skipy = dr.y-(plot_y-df_pix_height+1);
		
		
		// Allocate memory to hold the pixel values.
		pix = new int[dr.width*dr.height];
		
		int	x_rendered = 0;
		
		// Setup the pixels from the character glyph data.
		for (int i = 0; i < string_length; i++)
		{
			int	char_display_width = charWidth(string[i]);
			
			for (y = skipy; y < dr.height+skipy; y++)
			{
				for (x = 0; x < char_display_width; x++)
				{
					if (0 != getBit(string[i], y, x))
						pix_value = foreground;
					else
						pix_value = background;
					
					if (x_rendered+x >= skipx && 
						x_rendered+x-skipx < dr.width)
					{
						pix[x_rendered+dr.width*(y-skipy)+x-skipx] = pix_value;
					}
				}
			}
			x_rendered += char_display_width;
		}
		
		Image img = container.createImage(
			new MemoryImageSource(dr.width, dr.height, pix, 0, dr.width));

		if (null != graphics1)
			graphics1.drawImage(img,dr.x,dr.y,null);
		
		if (null != graphics2)
			graphics2.drawImage(img,dr.x,dr.y,null);
		
		return display_width;
	}

	/**
	 * Reads little endian 16 bit integer from font stream.
	 *
	 * @return	16 bit integer read from font stream.
	 */
	private short readShort()
	{
		int	s;
		short result;
		
		s = readByte() & 0xff;
		result = (short) s;
		s = readByte() & 0xff;
		result |= (short)(s << 8);
		
		return result;
	}

	/**
	 * Reads little endian 32 bit integer from font stream.
	 *
	 * @return	32 bit integer word read from font stream.
	 */
	private int readWord()
	{
		int one = readShort();
		int two = readShort();
		int result = one | (two << 16);

		return result;
	}

	/**
	 * Reads byte from font stream.
	 *
	 * @return	Byte from read font stream.
	 */
	private byte readByte()
	{
		fnt_offset += 1;
		try
		{
			return (byte) fnt_stream.read();
		}
		catch (java.lang.Exception e1)
		{
			return 0;
		}
	}

	/**
	 * Skips bytes on font stream.
	 *
	 * @param	n	Number to skip.
	 */
	private void skip(long n)
	{
		fnt_offset += n;

		try
		{
			fnt_stream.skip(n);
		}
		catch (java.lang.Exception e2)
		{
		}
	}	
}
