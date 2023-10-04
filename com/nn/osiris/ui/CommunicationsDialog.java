/**
 * PROJECT	Portal panel
 * FILE		CommunicationsDialog.java
 *
 *			(c) copyright 2003
 *			Pearson Digital Learning
 *
 * @author	M Webb
 */

package com.nn.osiris.ui;

import java.awt.*;
import javax.swing.*;
//import java.lang.*;
import java.awt.event.*;
//import java.util.*;
//import java.lang.reflect.*;

/**
 * This class represents the dialog used to change the host and port settings.
 */
public class CommunicationsDialog 
	extends JDialog
	implements ActionListener
{
	private static final long serialVersionUID = -111L;
	
	private boolean			is_host_valid = true;
	private boolean			is_port_valid = true;
	private boolean			cancelled = false;

	private JTextField		host_field = new JTextField(20);
	private JTextField		port_field = new JTextField(10);
	private JTextField		name_field = new JTextField(25);
	private JTextField		group_field = new JTextField(15);

	private JButton			ok_button = new JButton("Ok");
	private JButton			cancel_button = new JButton("Cancel");

	//private PortalFrame		parent;
	private Session			session = new Session();

	/**
	 * Construct a kermit dialog which displays a kermit message.
	 *
	 * @param	parent	The parent which owns this dialog.
	 * @param	title	The title for the dialog.
	 */
	public CommunicationsDialog(
		PortalFrame parent,
		String title,
		boolean is_modal,
		Session old_session,
		Options options)
	{
		super(parent, title, is_modal);

		old_session.copyTo(session);

		JPanel control_panel = new JPanel();
        JPanel button_panel = new JPanel();
		control_panel.setLayout(new GridLayout(2,2));	// rows, columns
		button_panel.setLayout(new FlowLayout(FlowLayout.TRAILING));

		// Add controls to control panel.
		control_panel.add(
			new LabeledComponent(
				host_field,
				"Host address:",
				'o',
				LabeledComponent.LABEL_ABOVE));
		control_panel.add(
			new LabeledComponent(
				port_field,
				"Port:",
				'p',
				LabeledComponent.LABEL_ABOVE));
		control_panel.add(
			new LabeledComponent(
				name_field,
				"Name:",
				LabeledComponent.LABEL_ABOVE));
		control_panel.add(
			new LabeledComponent(
				group_field,
				"Group:",
				LabeledComponent.LABEL_ABOVE));

		host_field.setText(session.host);
		
//		host_field.setText("192.168.1.243");
		
		port_field.setText(Integer.toString(session.port));
		name_field.setText(session.name);
		group_field.setText(session.group);

		// Limit the number of characters in text fields.
		try
		{
		// Grab the constructor we need through reflection..
		// wont load without 1.4+
		Class<?>	cls = Class.forName("com.nn.osiris.ui.FieldFilter");
		FieldFilterInterface	ff;

			ff = (FieldFilterInterface) cls.getDeclaredConstructor().newInstance();
			ff.setFilter(host_field,50);

			ff = (FieldFilterInterface) cls.getDeclaredConstructor().newInstance();
			ff.setFilter(port_field,5);

			ff = (FieldFilterInterface) cls.getDeclaredConstructor().newInstance();
			ff.setFilter(name_field,18);

			ff = (FieldFilterInterface) cls.getDeclaredConstructor().newInstance();
			ff.setFilter(group_field,8);
		}
		catch (NoClassDefFoundError e1)
		{
		}
		catch (Exception e2)
		{
		}

		// Add buttons to button panel.
		button_panel.add(ok_button);
		button_panel.add(cancel_button);

		// Set layout and add panels.
  		getContentPane().setLayout(new BorderLayout(6, 6)); // margins of 6
		getContentPane().add(control_panel);
		getContentPane().add(button_panel, BorderLayout.SOUTH);

		host_field.addActionListener(this);
		port_field.addActionListener(this);
		name_field.addActionListener(this);
		group_field.addActionListener(this);
		ok_button.addActionListener(this);
		cancel_button.addActionListener(this);

		if (options.lock_configuration)
		{
			host_field.setEnabled(false);
			port_field.setEnabled(false);
			name_field.setEnabled(false);
			group_field.setEnabled(false);
		}
		else
		{
			// Enable buttons if host and port are valid. (length != 0)
			host_field.addKeyListener(
				new KeyAdapter()
				{
					// Length must be > 0 to be valid.
					public void keyReleased(KeyEvent event)
					{
						is_host_valid = false;
						if (0 < host_field.getText().trim().length())
							is_host_valid = true;
						resetButtonsEnabling();
					}
				});

			port_field.addKeyListener(
				new KeyAdapter()
				{
					// Do not accept non-numeric keys.
					public void keyTyped(KeyEvent event)
					{
						char c = event.getKeyChar();      
						if (!(Character.isDigit(c) ||
							c == KeyEvent.VK_BACK_SPACE ||
							c == KeyEvent.VK_TAB ||
							c == KeyEvent.VK_DELETE))
						{
							getToolkit().beep();
							event.consume();
						}
					}

					// Length must be > 0 to be valid.
					public void keyReleased(KeyEvent event)
					{
						is_port_valid = false;
						if (0 < port_field.getText().trim().length())
							is_port_valid = true;
						resetButtonsEnabling();
					}
				});
		}

		pack();
		setLocationRelativeTo(parent);
	}

	private void resetButtonsEnabling()
	{
		ok_button.setEnabled (is_host_valid && is_port_valid);
	}

	public void actionPerformed(ActionEvent e) 
	{
		// CANCEL BUTTON
		if (e.getSource() == cancel_button)
		{
			cancelled = true;
			setVisible(false);
		}

		// OK BUTTON
		else if (e.getSource() == ok_button)
		{
			cancelled = false;
			session.host = host_field.getText();
			try
			{
				session.port = Integer.parseInt(port_field.getText());
			}
			// Handle any thrown exceptions.
			catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(
					this,
					"Port must be an integer.",
					"Invalid Port",
					JOptionPane.ERROR_MESSAGE);
				port_field.setText("");
			}
			session.name = name_field.getText();
			session.group = group_field.getText();
			setVisible(false);
		}
	}

	public Session getValues()
	{
		return session;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}
}
