/**
 * PROJECT	Portal panel
 * FILE		KermitDialog.java
 *
 *			(c) copyright 2000
 *			NCS NovaNET Learning
 *
 * @author	J Hegarty
 */

package com.nn.osiris.ui;

import java.awt.*;
import javax.swing.*;

/**
 * This class represents the dialog while doing a kermit operation.
 */
public class KermitDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a kermit dialog which displays a kermit message.
	 *
	 * @param	parent	The parent which owns this dialog.
	 * @param	title	The title for the dialog.
	 * @param	message	The content of the dialog.
	 */
	public KermitDialog(
		Frame parent,
		String title,
		String message)
	{
		super(parent, title, false);
  		getContentPane().setLayout(new BorderLayout(15, 15));
		getContentPane().add(new JLabel(message));
		pack();
		setLocationRelativeTo(parent);
	}
}
