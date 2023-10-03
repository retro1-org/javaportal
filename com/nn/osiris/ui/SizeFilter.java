/**
 * PROJECT	Portal panel
 * FILE		SizeFilter.java
 *
 *			(c) copyright 2003
 *			Pearson Digital Learning
 *
 * @author	M Webb
 */

package com.nn.osiris.ui;

import javax.swing.text.*;
import java.awt.*;

/**
 * This class represents a filter to limit the size of a document.
 */
class SizeFilter extends DocumentFilter
{
	int maxSize;

	/**
	 * Construct a filter of the given size limit.
	 *
 	 * @param	limit	The maximium length of the document.
	 */
	public SizeFilter(int limit)
	{
		maxSize = limit;
	}

	/**
	 * Override super class method to check the size as strings are inserted.
	 *
	 * @see	DocumentFilter#insertString
	 */
	public void insertString(
		DocumentFilter.FilterBypass fb,
		int offset,
		String string,
		AttributeSet attr)
	throws BadLocationException 
	{
		replace(fb, offset, 0, string, attr);
	}

	/**
	 * Override super class method to check the size as strings are replaced.
	 *
	 * @see	DocumentFilter#replace
	 */
	public void replace(
		DocumentFilter.FilterBypass fb,
		int offset,
		int length,
		String string,
		AttributeSet attrs)
	throws BadLocationException 
	{
		int newLength = fb.getDocument().getLength()-length+string.length();
		if (newLength <= maxSize)
		{
			fb.replace(offset, length, string, attrs);
		}
		else
		{
			Toolkit.getDefaultToolkit().beep();
		}
	}
}

