/**
 * PROJECT	Portal panel
 * FILE		LabeledComponent.java
 *
 *			(c) copyright 2003
 *			Pearson Digital Learning
 *
 * @author	M Webb
 */

package com.nn.osiris.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Component class which takes a JComponent and a string that labels the
 * component. The label can either be placed above the component or to the
 * left of the component.
 */
public class LabeledComponent extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComponent		component;
	private JLabel			label;
	//private BorderLayout	layout;
	
	public static int		LABEL_LEFT = 0;
	public static int		LABEL_ABOVE = 1;

	/**
	 * Construct a labeled component with specified component, label, hot key,
	 * and alignment.
	 *
	 * @param	component			Main component.
	 * @param	label_text			Text of the component label.
	 * @param	mnemonic			Mnemonic character for the label.
	 * @param	alignment			Alignment of the label relative to the component.
	 *								Valid values are OLABEL_LEFT and OLABEL_ABOVE.
	 * @param	background_color	The background color of component.
	 */
	public LabeledComponent(
		JComponent component,
		String label_text,
		char mnemonic,
		int alignment,
		Color background_color)
	{
		setBackground(background_color);
		commonInit(component, label_text, alignment);
		setMnemonic(mnemonic);
	}

	/**
	 * Construct a labeled component with specified component, label, hot key,
	 * and aliagnment.
	 *
	 * @param	component	Main component.
	 * @param	label_text	Text of the component label.
	 * @param	mnemonic	Mnemonic character for the label.
	 * @param	alignment	Alignment of the label relative to the component.
	 *						Valid values are OLABEL_LEFT and OLABEL_ABOVE.
	 */
	public LabeledComponent(
		JComponent component,
		String label_text,
		char mnemonic,
		int alignment)
	{	
		commonInit(component, label_text, alignment);
		setMnemonic(mnemonic);
	}

	/**
	 * Construct a labeled component with specified component, label,
	 * and aliagnment.
	 *
	 * @param	component			Main component.
	 * @param	label_text			Text of the component label.
	 * @param	alignment			Alignment of the label relative to the component.
	 *								Valid values are OLABEL_LEFT and OLABEL_ABOVE.
	 * @param	background_color	The background color of component.
	 */
	public LabeledComponent(
		JComponent component,
		String label_text,
		int alignment,
		Color background_color)
	{
		setBackground(background_color);
		commonInit(component, label_text, alignment);
	}

	/**
	 * Construct a labeled component with specified component, label,
	 * and aliagnment.
	 *
	 * @param	component	Main component.
	 * @param	label_text	Text of the component label.
	 * @param	alignment	Alignment of the label relative to the component.
	 *						Valid values are OLABEL_LEFT and OLABEL_ABOVE.
	 */
	public LabeledComponent(
		JComponent component,
		String label_text,
		int alignment)
	{
		commonInit(component, label_text, alignment);
	}

	/**
	 * Initialization that is done to all instances of this class, regardless
	 * of the constructor invoked.
	 *
	 * @param	component			Main component.
	 * @param	label_text			Text of the component label.
	 * @param	alignment			Alignment of the label relative to the component.
	 *								Valid values are OLABEL_LEFT and OLABEL_ABOVE.
	 */
	private void commonInit(JComponent component, String label_text, int alignment)
	{
		this.component = component;
		label = new JLabel(label_text);
		setLayout(new BorderLayout(2, 2)); // 2 pixel gap between components
		
		if (alignment == LABEL_LEFT)
		{
			add(label, BorderLayout.WEST);
			add(component, BorderLayout.CENTER);
		}
		else
		{
			add(label, BorderLayout.NORTH);
			add(component, BorderLayout.CENTER);
		}
	}

	/**
	 * Change label to include mnemonic, then reset label.
	 *
 	 * @param	mnemonic	Hot key.
	 */
	private void setMnemonic(char mnemonic)
	{
		label.setDisplayedMnemonic(mnemonic);

		// If component is a scroll pane, we want to set the label for the
		// component inside of the scroll pane.
		if (component instanceof JScrollPane)
		{
			label.setLabelFor(
				((JScrollPane)component).getViewport().getView());
		}
		// If component is not a scroll pane, set the label for the component.
		else
			label.setLabelFor(component);
	}

	/**
	 * Retrieves the main component.
	 *
 	 * @return	component	The main component being labeled.
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * Retrieves the component's label.
	 *
 	 * @return	label		The label currently applied to the component.
	 */
	public String getLabel()
	{
		return label.getText();
	}

	/**
	 * Sets the label to a new string.
	 *
 	 * @param	text		The string to which the label will be set.
	 */
	public void setLabelText(String text)
	{
		label.setText(text);
	}
}
