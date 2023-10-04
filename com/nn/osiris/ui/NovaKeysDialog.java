/**
 * PROJECT	Portal panel
 * FILE		NovaKeysDialog.java
 *
 *			(c) copyright 2003
 *			Pearson Digital Learning
 *
 * @author	J Hegarty
 */

package com.nn.osiris.ui;

//import java.awt.*;
import javax.swing.*;
//import javax.swing.text.*;
//import java.lang.*;
import java.awt.event.*;
//import java.util.*;

/**
 * This class represents the non-modal dialog that gives buttons for NovaNET function keys.
 */
public class NovaKeysDialog 
	extends JDialog
	implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	PortalFrame	frame;

	public NovaKeysDialog(PortalFrame parent)
	{
		super(parent, "NovaNET Keys", true);
		this.frame = parent;
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));

	JEditorPane	ep = new JEditorPane();

		ep.setContentType("text/html");
		ep.setText("<html><body>ACCESS<br>ANS<br>BACK<br>COPY<br>DATA<br>EDIT<br>"+
			"ERASE<br>FONT<br>HELP<br>LAB<br>MICRO<br>STOP<br>SUB<br>SUPER<br>TERM<br>"+
			"delta<br>divide<br>multiply<br>pi<br>sigma<br>"+
			"</body></html");
		ep.setEditable(false);
		getContentPane().add(ep);

	JEditorPane	ep2 = new JEditorPane();

		ep2.setContentType("text/html");
		ep2.setText("<html><body>"+
			"SHIFT-F3 or CTRL-SHIFT-q<br>"+
			"F2 or CTRL-a<br>"+
			"F8 or CTRL-b<br>"+
			"F11 or CTRL-c<br>"+
			"F9 or CTRL-d<br>"+
			"F5 or CTRL-e<br>"+
			"Backspace<br>"+
			"SHIFT-F4 or CTRL-f<br>"+
			"F6 or CTRL-h<br>"+
			"F7 or CTRL-l<br>"+
			"F4 or CTRL-m<br>"+
			"F10 or CTRL-s<br>"+
			"Page Down or CTRL-y<br>"+
			"Page Up or CTRL-p<br>"+
			"SHIFT-F2 or CTRL-t<br>"+
			"CTRL-KEYPAD -<br>"+
			"Insert or Ins or CTRL-g<br>"+
			"Delete or Del or CTRL-x<br>"+
			"MICRO-p<br>"+
			"CTRL-KEYPAD +<br>"+
			"</body></html");
		ep2.setEditable(false);
		getContentPane().add(ep2);

		pack();
		setLocationRelativeTo(parent);
	}

	public void actionPerformed(ActionEvent e) 
	{
	}
}