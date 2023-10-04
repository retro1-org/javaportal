/*
 * KeyBarDialog.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.awt.*;
import javax.swing.*;
//import javax.swing.text.*;
//import java.lang.*;
import java.awt.event.*;
//import java.util.*;

/**
 * This class represents the non-modal dialog that gives buttons for NovaNET function keys.
 */
public class KeyBarDialog 
	extends JDialog
	implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -131L;

	PortalFrame	frame;

	/** Display names of keys for the button labels */
	String	bstrings[] = {"ACCESS","ANS","BACK","COPY","DATA","EDIT","ERASE",
				"FONT","HELP","LAB","MICRO","NEXT","SQUARE","STOP","SUB","SUPER","TERM"};
	/** unshifted keycodes of keys */
	int		bkeys[] = {LevelOneParser.KEY_ACCESS,
					LevelOneParser.KEY_ANS,
					LevelOneParser.KEY_BACK,
					LevelOneParser.KEY_COPY,
					LevelOneParser.KEY_DATA,
					LevelOneParser.KEY_EDIT,
					LevelOneParser.KEY_ERASE,
					LevelOneParser.KEY_FONT,
					LevelOneParser.KEY_HELP,
					LevelOneParser.KEY_LAB,
					LevelOneParser.KEY_MICRO,
					LevelOneParser.KEY_NEXT,
					LevelOneParser.KEY_SQUARE,
					LevelOneParser.KEY_STOP,
					LevelOneParser.KEY_SUB,
					LevelOneParser.KEY_SUPER,
					LevelOneParser.KEY_TERM};
	/** shifted keycodes of keys */
	int		bshiftkeys[] = {LevelOneParser.KEY_ACCESS,
					LevelOneParser.KEY_TERM,
					LevelOneParser.KEY_BACK1,
					LevelOneParser.KEY_COPY1,
					LevelOneParser.KEY_DATA1,
					LevelOneParser.KEY_EDIT1,
					LevelOneParser.KEY_ERASE1,
					LevelOneParser.KEY_FONT,
					LevelOneParser.KEY_HELP1,
					LevelOneParser.KEY_LAB1,
					LevelOneParser.KEY_FONT,
					LevelOneParser.KEY_NEXT1,
					LevelOneParser.KEY_ACCESS,
					LevelOneParser.KEY_STOP1,
					LevelOneParser.KEY_SUB1,
					LevelOneParser.KEY_SUPER1,
					LevelOneParser.KEY_TERM};

	/**
	 * Constructor, takes parent frame as an argument.
	 */
	public KeyBarDialog(PortalFrame parent)
	{
		super(parent, "Key Bar", false);
		this.frame = parent;
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		for (int i=0; i<bstrings.length; i++)
		{
		JButton	button;

			button = new JButton(bstrings[i]);
			button.addActionListener(this);
			button.setAlignmentX(Component.CENTER_ALIGNMENT);
			getContentPane().add(button);
		}
		pack();
		setLocationRelativeTo(parent);
	}

	/**
	 * Callback invoked when a button is pushed. Sends appropriate key to novanet.
	 */
	public void actionPerformed(ActionEvent e) 
	{
	String	cmd = e.getActionCommand();
	boolean	shift = ((e.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) != 0);

		for (int i=0; i<bstrings.length; i++)
		{
			if	(cmd.equals(bstrings[i]))
			{
				frame.SendKey(shift ? bshiftkeys[i] : bkeys[i]);
				break;
			}
		}
	}
}