/*
 * OptionsDialog.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
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
public class OptionsDialog 
	extends JDialog
	implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -171L;

	private Options			options = new Options();

	private JCheckBox		disconnect_button = new JCheckBox("Disconnect at Signoff");
	private JCheckBox		lock_button = new JCheckBox("Lock Configuration");
	private JCheckBox		disable_name_button = new JCheckBox("Disable Signon in Title");
	private JCheckBox		multi_connect_button = new JCheckBox("Allow Multiple Connections");
	private JCheckBox		scale2x_button = new JCheckBox("Scale to twice normal size");
	private JButton			ok_button = new JButton("Ok");
	private JButton			cancel_button = new JButton("Cancel");
	private boolean			cancelled;

	private PortalFrame		parent;

	/**
	 * Construct a kermit dialog which displays a kermit message.
	 *
	 * @param	parent	The parent which owns this dialog.
	 * @param	title	The title for the dialog.
	 */
	public OptionsDialog(
		PortalFrame parent,
		String title,
		boolean is_modal,
		Options old_options)
	{
		super(parent, title, is_modal);

		old_options.copyTo(options);

		JPanel control_panel = new JPanel();
        JPanel button_panel = new JPanel();
		control_panel.setLayout(new GridLayout(4,1));	// rows, 1 column
		button_panel.setLayout(new FlowLayout(FlowLayout.TRAILING));

		// Add controls to control panel.
		control_panel.add(disconnect_button);
		control_panel.add(disable_name_button);
		control_panel.add(multi_connect_button);
		control_panel.add(scale2x_button);
		control_panel.add(lock_button);

		disconnect_button.setSelected(options.disconnect_at_signoff);
		lock_button.setSelected(options.lock_configuration);
		disable_name_button.setSelected(options.disable_signon_display);
		multi_connect_button.setSelected(options.multi_connect);
		scale2x_button.setSelected(options.scale2x);
		
		// Add buttons to button panel.
		button_panel.add(ok_button);
		button_panel.add(cancel_button);

		// Set layout and add panels.
  		getContentPane().setLayout(new BorderLayout(6, 6)); // margins of 6
		getContentPane().add(control_panel);
		getContentPane().add(button_panel, BorderLayout.SOUTH);

		ok_button.addActionListener(this);
		cancel_button.addActionListener(this);

		if (options.lock_configuration)
		{
			disable_name_button.setEnabled(false);
			disconnect_button.setEnabled(false);
			multi_connect_button.setEnabled(false);
			lock_button.setEnabled(false);
			scale2x_button.setEnabled(false);
		}
		else
		{
			disable_name_button.setEnabled(true);
			disconnect_button.setEnabled(true);
			lock_button.setEnabled(true);
			multi_connect_button.setEnabled(true);
			scale2x_button.setEnabled(true);
		}

		pack();
		setLocationRelativeTo(parent);
	}
/*
	private void resetButtonsEnabling()
	{
		ok_button.setEnabled (true);
	}
*/
	public void actionPerformed(ActionEvent e) 
	{
		// CANCEL BUTTON
		if (e.getSource() == cancel_button)
		{
			setVisible(false);
			cancelled = true;
		}

		// OK BUTTON
		else if (e.getSource() == ok_button)
		{
			options.disconnect_at_signoff = disconnect_button.isSelected();
			options.lock_configuration = lock_button.isSelected();
			options.disable_signon_display = disable_name_button.isSelected();
			options.multi_connect = multi_connect_button.isSelected();
			options.scale2x = scale2x_button.isSelected();
			cancelled = false;
			setVisible(false);
		}
	}

	public boolean isCancelled()
	{
		return cancelled;
	}

	public Options getValues()
	{
		return options;
	}
}
