/*
 * ConnectDialog.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * This class represents the dialog while connecting to NovaNET.
 */
public class ConnectDialog extends JDialog
	implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -21L;
	PlatoFrame	pf;

	/**
	 * Construct a connection dialog which displays a message.
	 *
	 * @param	parent	The parent which owns this dialog.
	 * @param	title	The title for the dialog.
	 * @param	message	The content of the dialog.
	 */
	public ConnectDialog(
		Frame parent,
		String title,
		String message)
	{
		super(parent, title, false);
		this.pf = (PlatoFrame) parent;
  		getContentPane().setLayout(new BorderLayout(15, 15));
		getContentPane().add(new JLabel(message),BorderLayout.CENTER);

	JButton	cancel = new JButton("Cancel");
	JPanel	buttonpane = new JPanel();

		buttonpane.setLayout(new BoxLayout(buttonpane,BoxLayout.X_AXIS));
		buttonpane.add(Box.createHorizontalGlue());
		buttonpane.add(cancel);

		getContentPane().add(buttonpane,BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		cancel.addActionListener(this);
	}

	public void actionPerformed(ActionEvent event)
	{
		pf.doCloseConnection();
	}
}
