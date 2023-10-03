/**
 * PROJECT		Portal panel
 *				(c) copyright 2000
 *				NCS NovaNET Learning
 *
 * @author		Don Appleman
 */

package com.nn.osiris.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Represent a panel with novaNET specific keypad. Keys include functional
 * keys (next, data, copy, edit, etc.), mathematic keys, greek keys, foreign
 * keys, mark keys. This keypad is divided into function and symbol keypads
 * separated by a vertical separator. This normally is part of the portal
 * panel. When it is used that way, there is no need to use this class at all.
 * This is useful aside from the portal panel when several portal panels will
 * share the same keypad. In that case, programmer must take care to switch
 * keypad output to appropriate level one panel.
 */
public class ONNKeypadPanel extends JPanel
{
	/**
	 * The target panel to send the keys to when key buttons are pressed.
	 * This allows sharing a keypad among different novaNET sessions.
	 * Application should not use this directly.
	 */
	public LevelOnePanel level_one_panel;

	/**
	 * Construct novaNET keypad panel with no output. This is for sharing
	 * a keypad with many novaNET sessions. Use OPortalPanel.setExternalKeypad
	 * to associate keypad with a specific session.
	 */
	public ONNKeypadPanel()
	{
		this(null);
	}

	/**
	 * Construct novaNET keypad panel specifying output level one panel on
	 * a key press. This mainly is for internal use by OPortalPanel.
	 *
	 * @param	level_one_panel	The level one protocol panel to send key to.
	 */
	public ONNKeypadPanel(LevelOnePanel level_one_panel)
	{
		this.level_one_panel = level_one_panel;

		/*
		 * Prepare a vertical separator to separate the function and symbol
		 * keypads. This vertical separator is a single panel with an 
		 * emptyborder, comprised of 2 single-pixel wide vertical panels, 
		 * one light, one dark.
		 */
		JPanel separator_panel = new JPanel();
		
		// Set separator panel to have 0 horizontal spacing.
		separator_panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		separator_panel.setBorder(new EmptyBorder(0, 3, 0, 1));
		Dimension line_size = new Dimension(1, LevelOnePanel.PANEL_HEIGHT);
		JPanel light_spacer = new JPanel();
		light_spacer.setBackground(Color.white);
		light_spacer.setPreferredSize(line_size);
		JPanel dark_spacer = new JPanel();
		dark_spacer.setBackground(Color.gray);
		dark_spacer.setPreferredSize(line_size);
		separator_panel.add(light_spacer);
		separator_panel.add(dark_spacer);
		
		Dimension spacer_size = new Dimension(7, LevelOnePanel.PANEL_HEIGHT);
		separator_panel.setMinimumSize(spacer_size);
		separator_panel.setMaximumSize(spacer_size);
		separator_panel.setPreferredSize(spacer_size);
		
		/*
		 * Assemble function keypad, a vertical separator, and symbol keypad.
		 */
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(new EmptyBorder(0, 4, 0, 2));
		
		add(new NNFunctionKeypad(this));
		add(separator_panel);
		add(new NNSymbolKeypad(this));
	}
}

/**
 * This class represents the abstract novaNET keypads. This takes care of
 * action performed when a button is pressed in the keypad.
 */
abstract class Keypad
	extends JPanel
	implements ActionListener
{
	/** Shift icon constant. */
	public static final ImageIcon SHIFT_ICON = getImageIcon("shiftico.gif");
	/** Empty (nothing) icon constant. */
	public static final ImageIcon EMPTY_ICON = getImageIcon("echar.gif");

	/**
	 * Get the image icon for a given file name. This provides getting images
	 * from a standard location.
	 *
	 * @param	file_name	File name of the image file.
	 */
	public static ImageIcon getImageIcon(String file_name)
	{
		return new ImageIcon(ClassLoader.getSystemResource(
			"com/nn/osiris/media/"+file_name));
	}
	
	/**
	 * Construct a generic novaNET keypad specifying key target location when
	 * a key button is pressed in the keypad.
	 *
	 * @param	parent_panel	The keypad panel this belongs to.
	 */
	public Keypad(ONNKeypadPanel parent_panel)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.parent_panel = parent_panel;
	}

	/**
	 * Override interface method so that the key is sent to the level one
	 * protocol panel.
	 *
	 * @see	ActionListener#actionPerformed
	 */
	public void actionPerformed(ActionEvent event)
	{
		NNKeyPress[] nn_keys =
			((NNKeyButton)event.getSource()).getKeyPresses();
		
		for (int key_counter = 0; 
			key_counter < nn_keys.length; 
			key_counter++)
		{
			nn_keys[key_counter].sendKey(parent_panel.level_one_panel);
			
			// If there are more keys, delay before sending next key.
			if (key_counter < nn_keys.length)
			{
				try 
				{
					Thread.sleep(50);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		// Make sure level one panel gets the focus.
		parent_panel.level_one_panel.requestFocus();
	}

	/** The keypad panel this belongs to. */
	private ONNKeypadPanel parent_panel;
}

/**
 * A keypad representing all NovaNET function keys as NNKeyButtons.
 */
class NNFunctionKeypad extends Keypad
{
	/**
	 * Construct a component representing all novaNET function keys as buttons.
	 *
	 * @param	parent_panel	The keypad panel this belongs to.
	 */
	public NNFunctionKeypad(ONNKeypadPanel parent_panel)
	{
 		super(parent_panel);

		// Set the keypad size, since it doesn't look good any other size.
		Dimension keypad_size = new Dimension(60, 512);
  		setMaximumSize(keypad_size);
		setMinimumSize(keypad_size);

		// Add the labels that identify the panel & display the legend
		add(getLegendPanel());

		ButtonCluster cluster;

		/*
		 * Create NEXT and BACK buttons cluster.
		 */
		// Row of next and shift-next buttons.
		NNFunctionKeyRow next_row = new NNFunctionKeyRow(
			"NEXT",
			KeyEvent.VK_ENTER,
			"Enter",
			"Shift+Enter");
		// Row of back and shift-back buttons.
		NNFunctionKeyRow back_row = new NNFunctionKeyRow(
			"BACK",
			KeyEvent.VK_F8,
			"CTRL+b or F8",
			"CTRL+B or Shift+F8");
		cluster = new ButtonCluster(
			new NNFunctionKeyRow[]{next_row, back_row});
		add(cluster);

		/*
		 * Create HELP button cluster.
		 */
		// Row of help and shift-help buttons.
		NNFunctionKeyRow help_row = new NNFunctionKeyRow(
			"HELP",
			KeyEvent.VK_F6,
			"CTRL+h or F6",
			"CTRL+H or Shift+F6");
		cluster = new ButtonCluster(new NNFunctionKeyRow[]{help_row});
		add(cluster);		

		/*
		 * Create LAB and DATA buttons cluster.
		 */
		// Row of lab and shift-lab buttons.
		NNFunctionKeyRow lab_row = new NNFunctionKeyRow(
			"LAB",
			KeyEvent.VK_F7,
			"CTRL+l or F7",
			"CTRL+L or Shift+F7");
		// Row of data and shift-data buttons.
		NNFunctionKeyRow data_row = new NNFunctionKeyRow(
			"DATA",
			KeyEvent.VK_F9,
			"CTRL+d or F9",
			"CTRL+D or Shift+F9");
		cluster = new ButtonCluster(new NNFunctionKeyRow[]{lab_row, data_row});
		add(cluster);

		/*
		 * Create TERM button cluster.
		 */
		// Row of term button.
		NNFunctionKeyRow term_row = new NNFunctionKeyRow(
			"TERM",
			new NNKeyPress(KeyEvent.VK_F2, KeyEvent.SHIFT_MASK),
			"CTRL+t or Shift+F2");
		cluster = new ButtonCluster(
			new NNFunctionKeyRow[] {term_row});
		add(cluster);		
		
		/*
		 * Create COPY, EDIT, SQUARE, ANS, and ERASE buttons cluster.
		 */
		// Row of copy and shift-copy buttons.
		NNFunctionKeyRow copy_row = new NNFunctionKeyRow(
			"COPY",
			KeyEvent.VK_F11,
			"CTRL+c or F11",
			"CTRL+C or Shift+F11");
		// Row of edit and shift-edit buttons.
		NNFunctionKeyRow edit_row = new NNFunctionKeyRow(
			"EDIT",
			KeyEvent.VK_F5,
			"CTRL+e or F5",
			"CTRL+E or Shift+F5");
		// Row of square button.
		NNFunctionKeyRow square_row = new NNFunctionKeyRow(
			"SQUARE",
			KeyEvent.VK_F3,
			"F3");
		// Row of answer button.
		NNFunctionKeyRow ans_row = new NNFunctionKeyRow(
			"ANS",
			KeyEvent.VK_F2,
			"CTRL+a or F2");
		// Row of erase and shift-erase buttons.
		NNFunctionKeyRow erase_row = new NNFunctionKeyRow(
			"ERASE",
			KeyEvent.VK_BACK_SPACE,
			"Backspace, Erases one character",
			"Shift+Backspace, Erases one word");
		cluster = new ButtonCluster(
			new NNFunctionKeyRow[] {
				copy_row,
				edit_row,
				square_row,
				ans_row,
				erase_row});
		add(cluster);		
		
		/*
		 * Create SUPER, SUB, MICRO, FONT, and ACCESS buttons cluster.
		 */
		// Row of super and shift-super buttons.
		NNFunctionKeyRow super_row = new NNFunctionKeyRow(
			"SUPER",
			KeyEvent.VK_PAGE_UP,
			"(SUPER) CTRL+p or PageUp, Raises the next character",
			"(SHIFT+SUPER) CTRL+P Shift+PageUp, "+
				"Raises characters until you press SHIFT+SUB");
		// Row of sub and shift-sub buttons.
		NNFunctionKeyRow sub_row = new NNFunctionKeyRow(
			"SUB",
			KeyEvent.VK_PAGE_DOWN,
			"(SUB) CTRL+y or PageDown, Lowers the next character",
			"(SHIFT+SUB) CTRL+Y Shift+PageDown, "+
				"Lowers characters until you press SHIFT+SUPER");
		// Row of micro button.
		NNFunctionKeyRow micro_row = new NNFunctionKeyRow(
			"MICRO",
			KeyEvent.VK_F4,
			"CTRL+m or F4, Substitutes from microtable for next character");
		// Row of font button.
		NNFunctionKeyRow font_row = new NNFunctionKeyRow(
			"FONT",
			new NNKeyPress(KeyEvent.VK_F4, KeyEvent.SHIFT_MASK),
			"CTRL+f or SHIFT+F4, Toggles alternate font plotting");
		// Row of access button.
		NNFunctionKeyRow access_row = new NNFunctionKeyRow(
			"ACCESS",
			new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
			"CTRL+SHIFT+q or SHIFT+F3");
		cluster = new ButtonCluster(
			new NNFunctionKeyRow[] {
				super_row,
				sub_row,
				micro_row,
				font_row,
				access_row});
		add(cluster);		

		/*
		 * Create STOP buttons cluster.
		 */
		// Row of stop and shift-stop buttons.
		NNFunctionKeyRow stop_row = new NNFunctionKeyRow(
			"STOP",
			KeyEvent.VK_F10,
			"CTRL+s or F10, Aborts plotting the display",
			"CTRL+SHIFT+s or SHIFT+F10, "+
				"Exits the current lesson or signs you off NovaNET");
		cluster = new ButtonCluster(new NNFunctionKeyRow[]{stop_row});
		add(cluster);		
	}
	
	/**
	 * Return a composite component of the label & legend for the function
	 * keypad.
	 *
	 * @return	Laid out panel of the legend for the function keypad.
	 */
	 private JPanel getLegendPanel()
	 {
		// Add the labels that identify the panel & display the legend
		JPanel legend_panel = new JPanel();

		legend_panel.setLayout(new BoxLayout(legend_panel, BoxLayout.Y_AXIS));

		// Now build the labels
		JPanel keypad_label = new JPanel();
		keypad_label.setLayout(new GridLayout(2, 1));
		JLabel keypad_label_1 = new JLabel("NovaNET", SwingConstants.CENTER);
		JLabel keypad_label_2 = new JLabel("Keys", SwingConstants.CENTER);
		// Set to a PLAIN font rather than the default
		Font label_font = keypad_label_1.getFont();
		label_font = label_font.deriveFont(Font.PLAIN);
		legend_panel.setFont(label_font);
		keypad_label_1.setFont(label_font);
		keypad_label_2.setFont(label_font);
		keypad_label.add(keypad_label_1);
		keypad_label.add(keypad_label_2);
		
		keypad_label.setBorder(new EmptyBorder(4, 0, 20, 0));
		keypad_label.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// Add the label to the overall pane
		legend_panel.add(keypad_label);

		// Now use a panel to build the legend
		// ... set to a smaller font than the label font
		Font legend_font = label_font.deriveFont(
			(float)0.8*label_font.getSize2D());
		JPanel keypad_legend = new JPanel();
		keypad_legend.setLayout(new GridLayout(2, 1));
		keypad_legend.setBorder(new EmptyBorder(4, 0, 0, 3));
		JLabel keypad_legend_1 = new JLabel("SHIFTed", SwingConstants.RIGHT);
		keypad_legend_1.setFont(legend_font);
		// The second line is a composite panel of a text label & an icon label
		JPanel keypad_legend_2ab = new JPanel();
		keypad_legend_2ab.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		keypad_legend_2ab.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		JLabel keypad_legend_2a = new JLabel("key:", SwingConstants.RIGHT);
		JLabel keypad_legend_2b = new JLabel(SHIFT_ICON);

		keypad_legend_2a.setFont(legend_font);
		keypad_legend_2ab.add(keypad_legend_2a);
		keypad_legend_2ab.add(keypad_legend_2b);
		
		keypad_legend.add(keypad_legend_1);
		keypad_legend.add(keypad_legend_2ab);
		keypad_legend.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Add the legend to the overall pane.
		legend_panel.add(keypad_legend);
		
		return legend_panel;
	}
	
	/**
	 * This class represents a vertical cluster of keys for the function
	 * keypad.
	 */
	private class ButtonCluster extends JPanel
	{
		/** 
		 * Generate panel holding array of buttons vertically aligned.
		 *
		 * @param	rows	Array of NNFunctionKeyRows to cluster.
		 */
		ButtonCluster(NNFunctionKeyRow[] rows)
		{
			// Set cluster to single column layout
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
			
			for (int row_count = 0; row_count < rows.length; row_count++)
			{
				// Add this row to the cluster
				add(rows[row_count]);
			}
			
			// Set a border for the cluster.
			setBorder(new EmptyBorder(5, 0, 5, 0));
			setPreferredSize(getPreferredSize());
		}
	}

	/**
	 * This class represents a row of function key and its optional shift
	 * function key.
	 */
	private class NNFunctionKeyRow extends JPanel
	{
		/**
		 * Construct a row of one function key.
		 *
		 * @param	button_label	Text label for the primary button
		 * @param	key_code		Base key_code for function key
		 * @param	tooltip			Text for tooltip
		 */
		public NNFunctionKeyRow(
			String button_label,
			int key_code,
			String tooltip)
		{
			this(
				button_label,
				new NNKeyPress(key_code),
				null,
				tooltip,
				null);
		}
		
		/**
		 * Construct a row of one function key.
		 *
		 * @param	button_label	Text label for the primary button
		 * @param	key_press		NNKeyPress for function key
		 * @param	tooltip			Text for tooltip
		 */
		public NNFunctionKeyRow(
			String button_label,
			NNKeyPress key_press,
			String tooltip)
		{
			this(
				button_label,
				key_press,
				null,
				tooltip,
				null);
		}
		
		/**
		 * Construct a row of one function key and its shift key.
		 *
		 * @param	button_label	Text label for the primary button
		 * @param	key_code		Base key code for both NNKeyPresses
		 * @param	tooltip1		Text for tooltip for primary
		 * @param	tooltip2		Text for tooltip for shift button
		 */
		public NNFunctionKeyRow(
			String button_label,
			int key_code,
			String tooltip1,
			String tooltip2)
		{
			this(
				button_label,
				new NNKeyPress(key_code),
				new NNKeyPress(key_code, KeyEvent.SHIFT_MASK),
				tooltip1,
				tooltip2);
		}
		
		/**
		 * Construct a row of one function key and its shift key.
		 *
		 * @param	button_label	Text label for the primary button
		 * @param	key_press1		NNKeyPress for primary key
		 * @param	key_press2		NNKeyPress for shifted key
		 * @param	tooltip1		Text for tooltip for primary
		 * @param	tooltip2		Text for tooltip for shifted key
		 */
		public NNFunctionKeyRow(
			String button_label,
			NNKeyPress key_press1,
			NNKeyPress key_press2,
			String tooltip1,
			String tooltip2)
		{
			// Create the key row panel and give it a layout
			setLayout(new BorderLayout());
		
			// Create the primary button
			NNFunctionKeyButton primary = new NNFunctionKeyButton(
				button_label,
				key_press1,
				tooltip1);
			primary.addActionListener(NNFunctionKeypad.this);
				
			// Add the primary button to the row
			add(primary, BorderLayout.CENTER);
		
			// If we have a shift key, let's add it.
			if (tooltip2 != null)
			{
				NNShiftFunctionKeyButton shifted =
					new NNShiftFunctionKeyButton(
						key_press2,
						tooltip2);
				shifted.addActionListener(NNFunctionKeypad.this);
				
				// Add the shift button to the row.		
				add(shifted, BorderLayout.EAST);
			}
		}
	}			
}

/**
 * This class represents novaNET symbol keypad.
 */
class NNSymbolKeypad extends Keypad
{
	/**
	 * Construct a panel with all NovaNET special symbols keys as NNKeyButtons.
	 * This is constructs nested panels. A panel for the label for each type.
	 * One panel for each row of symbols. Another panel to hold each cluster
	 * of one label plus one or more rows(4 clusters).
	 *
	 * @param	parent_panel	The keypad panel this belongs to.
	 */
	public NNSymbolKeypad(final ONNKeypadPanel parent_panel)
	{
		super(parent_panel);

		// Set the keypad size, since it doesn't look good any other size.
		Dimension keypad_size = new Dimension(73, 512);
  		setMaximumSize(keypad_size);
		setMinimumSize(keypad_size);

		// Construct a CardLayout panel to alternate the symbols & logo panels
		final JPanel card_panel = new JPanel(new CardLayout());

		// Construct a special panel to hold all of the symbol key clusters
		JPanel clusters = new JPanel();
		clusters.setLayout(new BoxLayout(clusters,BoxLayout.Y_AXIS));
		clusters.add(getMathCluster());
		clusters.add(getGreekCluster());
		clusters.add(getForeignCluster());
		clusters.add(getMarksCluster());
		
		// Construct a panel to hold the logo, and alternate with the
		// above panel
		JPanel logo_panel = new JPanel(new BorderLayout());

		// Create the logo as a label
		JLabel logo = new JLabel(getImageIcon("enovanet.gif"));

		// Add the logo to the panel
		logo_panel.add(logo, BorderLayout.CENTER);
		
		// Now finish building the card panel
		card_panel.add(logo_panel, "Logo");
		card_panel.add(clusters, "Symbols");
		
		/*
		 * Prepare the toggle button that toggles between the symbol keypad
		 * and the novaNET logo which hides the symbol keypad.
		 */
		ImageIcon button_icon = getImageIcon("symbolsl.gif");
		JToggleButton symbol_keypad_toggle = new JToggleButton(button_icon)
		{
			/**
			 * Override super class to inhibit button from receiving the focus.
			 *
			 * @see	JComponent#isFocusTraversable
			 */
			public boolean isFocusTraversable()
			{
				return false;
			}
		};
		
		symbol_keypad_toggle.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				((CardLayout)card_panel.getLayout()).next(card_panel);

				// Make sure level one panel gets the focus.
				parent_panel.level_one_panel.requestFocus();
			}
		});
		symbol_keypad_toggle.setMargin(new Insets(0, 0, 0, 0));
  		symbol_keypad_toggle.setAlignmentX(Component.CENTER_ALIGNMENT);
		symbol_keypad_toggle.setRequestFocusEnabled(false);

		/*
		 * Add the symbol keypad toggle button and the symbol keys panel.
		 */
		add(symbol_keypad_toggle);
		add(card_panel);
	}

	/**
	 * Get a component which contains all NovaNET special math symbols.
	 *
	 * @return	The composited math symbols keypad cluster
	 */
	private Component getMathCluster()
	{
		// Create the cluster itself, label it, and build it up
		Box math_cluster = new Box(BoxLayout.Y_AXIS);
		// Add the label
		JLabel math_label = new JLabel("Math", SwingConstants.LEFT);
		math_label.setAlignmentX(Component.CENTER_ALIGNMENT);
		math_label.setBorder(new EmptyBorder(13, 0, 0, 0));
		math_cluster.add(math_label);

		/*
		 * Add the first row of keys to the math cluster.
		 */
		Box key_row = new Box(BoxLayout.X_AXIS);			
		NNSymbolButton plus = new NNSymbolButton(
			getImageIcon("plus.gif"),
			new NNKeyPress(KeyEvent.VK_ADD),
			"plus");
		plus.addActionListener(NNSymbolKeypad.this);
		key_row.add(plus);
		NNSymbolButton minus = new NNSymbolButton(
			getImageIcon("minus.gif"),
			new NNKeyPress(KeyEvent.VK_SUBTRACT),
			"minus");
		minus.addActionListener(NNSymbolKeypad.this);
		key_row.add(minus);
		NNSymbolButton times = new NNSymbolButton(
			getImageIcon("times.gif"),
			new NNKeyPress(KeyEvent.VK_DELETE),
			"times");
		times.addActionListener(NNSymbolKeypad.this);
		key_row.add(times);
		NNSymbolButton divide = new NNSymbolButton(
			getImageIcon("divide.gif"),
			new NNKeyPress(KeyEvent.VK_INSERT),
			"divide");
		divide.addActionListener(NNSymbolKeypad.this);
		key_row.add(divide);
		NNSymbolButton degree = new NNSymbolButton(
			getImageIcon("degree.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_O)},
			"degree symbol");
		degree.addActionListener(NNSymbolKeypad.this);
		key_row.add(degree);
		
		math_cluster.add(key_row);
		
		/*
		 * Add the second row of keys to the math cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton lte = new NNSymbolButton(
			getImageIcon("lte.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_COMMA, KeyEvent.SHIFT_MASK)},
			"less than or equal");
		lte.addActionListener(NNSymbolKeypad.this);
		key_row.add(lte);
		NNSymbolButton gte = new NNSymbolButton(
			getImageIcon("gte.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_MASK)},
			"greater than or equal");
		gte.addActionListener(NNSymbolKeypad.this);
		key_row.add(gte);
		NNSymbolButton notequal = new NNSymbolButton(
			getImageIcon("notequal.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_EQUALS)},
			"not equal to");
		notequal.addActionListener(NNSymbolKeypad.this);
		key_row.add(notequal);
		NNSymbolButton notsym = new NNSymbolButton(
			getImageIcon("notsym.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_SEMICOLON)},
			//++ Not sure what this is called. Modify it when we know the name.
			"not");
		notsym.addActionListener(NNSymbolKeypad.this);
		key_row.add(notsym);
		NNSymbolButton equiv = new NNSymbolButton(
			getImageIcon("equiv.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_0, KeyEvent.SHIFT_MASK)},
			"equivalence");
		equiv.addActionListener(NNSymbolKeypad.this);
		key_row.add(equiv);
		
		math_cluster.add(key_row);
		
		/*
		 * Add the third row of the math cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton cross = new NNSymbolButton(
			getImageIcon("cross.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_DELETE, KeyEvent.SHIFT_MASK)},
			"cross product");
		cross.addActionListener(NNSymbolKeypad.this);
		key_row.add(cross);
		NNSymbolButton dotprod = new NNSymbolButton(
			getImageIcon("dotprod.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_DELETE)},
			"dot product");
		dotprod.addActionListener(NNSymbolKeypad.this);
		key_row.add(dotprod);
		NNSymbolButton intersection = new NNSymbolButton(
			getImageIcon("intersec.gif"),
			new NNKeyPress[] {
				new NNKeyPress(
					KeyEvent.VK_G,
					KeyEvent.SHIFT_MASK + KeyEvent.CTRL_MASK)},
			"intersection");
		intersection.addActionListener(NNSymbolKeypad.this);
		key_row.add(intersection);
		NNSymbolButton union = new NNSymbolButton(
			getImageIcon("union.gif"),
			new NNKeyPress[] {
				new NNKeyPress(
					KeyEvent.VK_X,
					KeyEvent.SHIFT_MASK+KeyEvent.CTRL_MASK)},
			"union");
		union.addActionListener(NNSymbolKeypad.this);
		key_row.add(union);
		NNSymbolButton assign = new NNSymbolButton(
			getImageIcon("assign.gif"),
			new NNKeyPress[] {new NNKeyPress(KeyEvent.VK_ESCAPE)},
			"assignment arrow");
		assign.addActionListener(NNSymbolKeypad.this);
		key_row.add(assign);
		
		math_cluster.add(key_row);
		
		// Return the completed cluster.
		return math_cluster;
	}

	/**
	 * Get a component which contains all NovaNET special greek symbols.
	 *
	 * @return	The composited greek symbols keypad cluster.
	 */
	private Component getGreekCluster()
	{
		// Create the cluster itself, label it, and build it up
		Box greek_cluster = new Box(BoxLayout.Y_AXIS);
		// Add the label
		JLabel greek_label = new JLabel("Greek", SwingConstants.LEFT);
		greek_label.setAlignmentX(Component.CENTER_ALIGNMENT);
		greek_label.setBorder(new EmptyBorder(13, 0, 0, 0));
		greek_cluster.add(greek_label);

		/*
		 * Add the first row of keys to the greek cluster.
		 */
		Box key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton alpha = new NNSymbolButton(
			getImageIcon("alpha.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_A)},
			"alpha");
		alpha.addActionListener(NNSymbolKeypad.this);
		key_row.add(alpha);
		NNSymbolButton beta = new NNSymbolButton(
			getImageIcon("beta.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_B)},
			"beta");
		beta.addActionListener(NNSymbolKeypad.this);
		key_row.add(beta);
		NNSymbolButton delta = new NNSymbolButton(
			getImageIcon("delta.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_D)},
			"delta");
		delta.addActionListener(NNSymbolKeypad.this);
		key_row.add(delta);
		NNSymbolButton big_delta = new NNSymbolButton(
			getImageIcon("bigdelta.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_SUBTRACT, KeyEvent.SHIFT_MASK)},
			"shift delta");
		big_delta.addActionListener(NNSymbolKeypad.this);
		key_row.add(big_delta);

		// Add place holder button.
		key_row.add(new NNSymbolButton());
		
		greek_cluster.add(key_row);
		
		/*
		 * Add the second row of keys to the greek cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton theta = new NNSymbolButton(
			getImageIcon("theta.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_T)},
			"theta");
		theta.addActionListener(NNSymbolKeypad.this);
		key_row.add(theta);
		NNSymbolButton lambda = new NNSymbolButton(
			getImageIcon("lambda.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_L)},
			"lambda");
		lambda.addActionListener(NNSymbolKeypad.this);
		key_row.add(lambda);
		NNSymbolButton mu = new NNSymbolButton(
			getImageIcon("mu.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_M)},
			"mu");
		mu.addActionListener(NNSymbolKeypad.this);
		key_row.add(mu);
		NNSymbolButton pi = new NNSymbolButton(
			getImageIcon("pi.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_P)},
			"pi    micro p");		
		pi.addActionListener(NNSymbolKeypad.this);
		key_row.add(pi);		

		// Add a place holder button.
		key_row.add(new NNSymbolButton());
	
		greek_cluster.add(key_row);

		/*
		 * Add the third row of keys to the greek cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton rho = new NNSymbolButton(
			getImageIcon("rho.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_R)},
			"rho");
		rho.addActionListener(NNSymbolKeypad.this);
		key_row.add(rho);
		NNSymbolButton sigma = new NNSymbolButton(
			getImageIcon("sigma.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_S)},
			"sigma");
		sigma.addActionListener(NNSymbolKeypad.this);
		key_row.add(sigma);
		NNSymbolButton big_sigma = new NNSymbolButton(
			getImageIcon("bigsigma.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_ADD, KeyEvent.SHIFT_MASK)},
			"shift sigma");
		big_sigma.addActionListener(NNSymbolKeypad.this);
		key_row.add(big_sigma);
		NNSymbolButton omega = new NNSymbolButton(
			getImageIcon("omega.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_W)},
			"omega");		
		omega.addActionListener(NNSymbolKeypad.this);
		key_row.add(omega);		

		// Add a place holder button.
		key_row.add(new NNSymbolButton());
	
		greek_cluster.add(key_row);
		
		// Return the completed cluster.
		return greek_cluster;
	}
		
	/**
	 * Get a component which contains all NovaNET special foreign symbols.
	 *
	 * @return	The composited foreign symbols keypad cluster.
	 */
	 private Component getForeignCluster()
	 {
	 	// Create the cluster itself, label it, and build it up
	 	Box foreign_cluster = new Box(BoxLayout.Y_AXIS);
	 	// Add the label
	 	JLabel foreign_label = new JLabel("Foreign", SwingConstants.LEFT);
		foreign_label.setAlignmentX(Component.CENTER_ALIGNMENT);
	 	foreign_label.setBorder(new EmptyBorder(13, 0, 0, 0));
	 	foreign_cluster.add(foreign_label);

 		/*
 		 * Add the first row of keys to the foreign cluster.
 		 */
 		Box key_row = new Box(BoxLayout.X_AXIS);
 		NNSymbolButton acute_a = new NNSymbolButton(
 			getImageIcon("aacute.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_A),
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_E)},
 			"acute a");
		acute_a.addActionListener(NNSymbolKeypad.this);
 		key_row.add(acute_a);
 		NNSymbolButton grave_a = new NNSymbolButton(
 			getImageIcon("agrave.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_A),
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_Q)},
 			"grave a");
		grave_a.addActionListener(NNSymbolKeypad.this);
 		key_row.add(grave_a);
 		NNSymbolButton umlaut_a = new NNSymbolButton(
 			getImageIcon("aumlaut.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_K)},
 			"umlaut a");
		umlaut_a.addActionListener(NNSymbolKeypad.this);
 		key_row.add(umlaut_a);
 		NNSymbolButton circumflex_a = new NNSymbolButton(
 			getImageIcon("acflex.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_A),
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_X)},
 			"circumflex a");
		circumflex_a.addActionListener(NNSymbolKeypad.this);
 		key_row.add(circumflex_a);
 		NNSymbolButton circle_a = new NNSymbolButton(
 			getImageIcon("acircle.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_J)},
 			"circle a");
		circle_a.addActionListener(NNSymbolKeypad.this);
 		key_row.add(circle_a);
		
		foreign_cluster.add(key_row);

		/*
		 * Add the second row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_e = new NNSymbolButton(
			getImageIcon("eacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute e");
		acute_e.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_e);
		NNSymbolButton grave_e = new NNSymbolButton(
			getImageIcon("egrave.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave e");
		grave_e.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_e);
		NNSymbolButton umlaut_e = new NNSymbolButton(
			getImageIcon("eumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_U)},
			"umlaut e");
		umlaut_e.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_e);
		NNSymbolButton circumflex_e = new NNSymbolButton(
			getImageIcon("ecflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex e");
		circumflex_e.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_e);
		NNSymbolButton ae = new NNSymbolButton(
			getImageIcon("ae.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_G)},
			"ae ligature");
		ae.addActionListener(NNSymbolKeypad.this);
		key_row.add(ae);

		foreign_cluster.add(key_row);
		
		/*
		 * Add the third row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_i = new NNSymbolButton(
			getImageIcon("iacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute i");
		acute_i.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_i);
		NNSymbolButton grave_i = new NNSymbolButton(
			getImageIcon("igrave.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave i");
		grave_i.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_i);
		NNSymbolButton umlaut_i = new NNSymbolButton(
			getImageIcon("iumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_U)},
			// Note that umlaut-i & circumflex-i appears identical in novaNET.
			"umlaut i");
		umlaut_i.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_i);
		NNSymbolButton circumflex_i = new NNSymbolButton(
			getImageIcon("icflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex i");
		circumflex_i.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_i);

		// Add a place holder button.
		key_row.add(new NNSymbolButton());

		foreign_cluster.add(key_row);
		
		/*
		 * Add the fourth row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_o = new NNSymbolButton(
			getImageIcon("oacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_O),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute o");
		acute_o.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_o);
		NNSymbolButton grave_o = new NNSymbolButton(
			getImageIcon("ograve.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_O),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave o");
		grave_o.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_o);
		NNSymbolButton umlaut_o = new NNSymbolButton(
			getImageIcon("oumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Y)},
			"umlaut o");
		umlaut_o.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_o);
		NNSymbolButton circumflex_o = new NNSymbolButton(
			getImageIcon("ocflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_O),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex o");
		circumflex_o.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_o);
		NNSymbolButton slash_o = new NNSymbolButton(
			getImageIcon("oslash.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_H)},
			"slash o");
		slash_o.addActionListener(NNSymbolKeypad.this);
		key_row.add(slash_o);

		foreign_cluster.add(key_row);
		
		/*
		 * Add the fifth row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_u = new NNSymbolButton(
			getImageIcon("uacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute u");
		acute_u.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_u);
		NNSymbolButton grave_u = new NNSymbolButton(
			getImageIcon("ugrave.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave u");
		grave_u.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_u);
		NNSymbolButton umlaut_u = new NNSymbolButton(
			getImageIcon("uumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_U)},
			"umlaut u");
		umlaut_u.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_u);
		NNSymbolButton circumflex_u = new NNSymbolButton(
			getImageIcon("ucflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex u");
		circumflex_u.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_u);

		// Add a place holder button.
		key_row.add(new NNSymbolButton());

		foreign_cluster.add(key_row);
		/*
		 * Add the sixth row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_A = new NNSymbolButton(
			getImageIcon("saacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_A, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute A");
		acute_A.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_A);
		NNSymbolButton grave_A = new NNSymbolButton(
			getImageIcon("sagrave.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_A, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave A");
		grave_A.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_A);
		NNSymbolButton umlaut_A = new NNSymbolButton(
			getImageIcon("saumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_K, KeyEvent.SHIFT_MASK)},
			"umlaut A");
		umlaut_A.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_A);
		NNSymbolButton circumflex_A = new NNSymbolButton(
			getImageIcon("sacflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_A, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex A");
		circumflex_A.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_A);
		NNSymbolButton circle_A = new NNSymbolButton(
			getImageIcon("sacircle.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_J, KeyEvent.SHIFT_MASK)},
			"circle A");
		circle_A.addActionListener(NNSymbolKeypad.this);
		key_row.add(circle_A);
		
		foreign_cluster.add(key_row);
		/*
		 * Add the seventh row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_E = new NNSymbolButton(
			getImageIcon("seacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute E");
		acute_E.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_E);
		NNSymbolButton grave_E = new NNSymbolButton(
			getImageIcon("segrave.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave E");
		grave_E.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_E);
		NNSymbolButton umlaut_E = new NNSymbolButton(
			getImageIcon("seumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_U)},
			"umlaut E");
		umlaut_E.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_E);
		NNSymbolButton circumflex_E = new NNSymbolButton(
			getImageIcon("secflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_E, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex E");
		circumflex_E.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_E);
		NNSymbolButton aE = new NNSymbolButton(
			getImageIcon("sae.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_G, KeyEvent.SHIFT_MASK)},
			"AE ligature");
		aE.addActionListener(NNSymbolKeypad.this);
		key_row.add(aE);

		foreign_cluster.add(key_row);
		/*
		 * Add the eighth row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_I = new NNSymbolButton(
			getImageIcon("siacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute I");
		acute_I.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_I);
		NNSymbolButton grave_I = new NNSymbolButton(
			getImageIcon("sigrave.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave I");
		grave_I.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_I);
		NNSymbolButton umlaut_I = new NNSymbolButton(
			getImageIcon("siumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_U)},
			"umlaut I");
		umlaut_I.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_I);
		NNSymbolButton circumflex_I = new NNSymbolButton(
			getImageIcon("sicflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_I, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex I");
		circumflex_I.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_I);

		// Add a place holder button.
		key_row.add(new NNSymbolButton());

		foreign_cluster.add(key_row);
		/*
		 * Add the ninth row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_O = new NNSymbolButton(
			getImageIcon("soacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_O, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute O");
		acute_O.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_O);
		NNSymbolButton grave_O = new NNSymbolButton(
			getImageIcon("sograve.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_O, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave O");
		grave_O.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_O);
		NNSymbolButton umlaut_O = new NNSymbolButton(
			getImageIcon("soumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Y, KeyEvent.SHIFT_MASK)},
			"umlaut O");
		umlaut_O.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_O);
		NNSymbolButton circumflex_O = new NNSymbolButton(
			getImageIcon("socflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_O, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex O");
		circumflex_O.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_O);
		NNSymbolButton slash_O = new NNSymbolButton(
			getImageIcon("soslash.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_H, KeyEvent.SHIFT_MASK)},
			"slash O");
		slash_O.addActionListener(NNSymbolKeypad.this);
		key_row.add(slash_O);

		foreign_cluster.add(key_row);
		/*
		 * Add the tenth row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton acute_U = new NNSymbolButton(
			getImageIcon("suacute.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_E)},
			"acute U");
		acute_U.addActionListener(NNSymbolKeypad.this);
		key_row.add(acute_U);
		NNSymbolButton grave_U = new NNSymbolButton(
			getImageIcon("sugrave.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_Q)},
			"grave U");
		grave_U.addActionListener(NNSymbolKeypad.this);
		key_row.add(grave_U);
		NNSymbolButton umlaut_U = new NNSymbolButton(
			getImageIcon("suumlaut.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_U)},
			"umlaut U");
		umlaut_U.addActionListener(NNSymbolKeypad.this);
		key_row.add(umlaut_U);
		NNSymbolButton circumflex_U = new NNSymbolButton(
			getImageIcon("sucflex.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_U, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_X)},
			"circumflex U");
		circumflex_U.addActionListener(NNSymbolKeypad.this);
		key_row.add(circumflex_U);

		// Add a place holder button.
		key_row.add(new NNSymbolButton());

		foreign_cluster.add(key_row);
		/*
		 * Add the eleventh row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton cedilla_c = new NNSymbolButton(
			getImageIcon("ccedilla.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_C),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_C)},
			"cedilla c");
		cedilla_c.addActionListener(NNSymbolKeypad.this);
		key_row.add(cedilla_c);
		NNSymbolButton tilde_n = new NNSymbolButton(
			getImageIcon("ntilde.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_N),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_N)},
			"tilde n");
		tilde_n.addActionListener(NNSymbolKeypad.this);
		key_row.add(tilde_n);

		// Add place holder buttons.
		key_row.add(new NNSymbolButton());
		key_row.add(new NNSymbolButton());
		key_row.add(new NNSymbolButton());

		foreign_cluster.add(key_row);
		/*
		 * Add the twelfth row of keys to the foreign cluster.
		 */
		key_row = new Box(BoxLayout.X_AXIS);
		NNSymbolButton cedilla_C = new NNSymbolButton(
			getImageIcon("sccedill.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_C, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_C)},
			"cedilla C");
		cedilla_C.addActionListener(NNSymbolKeypad.this);
		key_row.add(cedilla_C);
		NNSymbolButton tilde_N = new NNSymbolButton(
			getImageIcon("sntilde.gif"),
			new NNKeyPress[] {
				new NNKeyPress(KeyEvent.VK_N, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
				new NNKeyPress(KeyEvent.VK_N)},
			"tilde N");
		tilde_N.addActionListener(NNSymbolKeypad.this);
		key_row.add(tilde_N);

		// Add place holder buttons.
		key_row.add(new NNSymbolButton());
		key_row.add(new NNSymbolButton());
		key_row.add(new NNSymbolButton());

		foreign_cluster.add(key_row);

		// Return the completed cluster
		return foreign_cluster;
	}

	/**
	 * Return a composite component representing all NovaNET diacritical marks.
	 *
	 * @return	Component	The composited diacritical marks keypad cluster.
	 */
	 private Component getMarksCluster()
	 {
	 	// Create the cluster itself, label it, and build it up
	 	Box marks_cluster = new Box(BoxLayout.Y_AXIS);
	 	// Add the label
	 	JLabel marks_label = new JLabel("Marks", SwingConstants.LEFT);
		marks_label.setAlignmentX(Component.CENTER_ALIGNMENT);
	 	marks_label.setBorder(new EmptyBorder(13, 0, 0, 0));
	 	marks_cluster.add(marks_label);

		/*
		 * Add the first row of keys to the marks cluster.
		 */
 		Box key_row = new Box(BoxLayout.X_AXIS);

 		NNSymbolButton acute = new NNSymbolButton(
 			getImageIcon("acute.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_E)},
 			"acute accent");
		acute.addActionListener(NNSymbolKeypad.this);
 		key_row.add(acute);
 		NNSymbolButton grave = new NNSymbolButton(
 			getImageIcon("grave.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_Q)},
 			"grave accent");
		grave.addActionListener(NNSymbolKeypad.this);
 		key_row.add(grave);
 		NNSymbolButton umlaut = new NNSymbolButton(
 			getImageIcon("umlaut.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_U)},
 			"umlaut");
		umlaut.addActionListener(NNSymbolKeypad.this);
 		key_row.add(umlaut);
 		NNSymbolButton circumflex = new NNSymbolButton(
 			getImageIcon("cflex.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_X)},
 			"circumflex");
		circumflex.addActionListener(NNSymbolKeypad.this);
 		key_row.add(circumflex);
 		NNSymbolButton caron = new NNSymbolButton(
 			getImageIcon("caron.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_V)},
 			"caron");
		caron.addActionListener(NNSymbolKeypad.this);
 		key_row.add(caron);

 		marks_cluster.add(key_row);
 		/*
 		 * Add the first row of keys to the marks cluster.
 		 */
 		key_row = new Box(BoxLayout.X_AXIS);
 		NNSymbolButton cedilla = new NNSymbolButton(
 			getImageIcon("cedilla.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_C)},
 			"cedilla");
		cedilla.addActionListener(NNSymbolKeypad.this);
 		key_row.add(cedilla);
 		NNSymbolButton tilde = new NNSymbolButton(
 			getImageIcon("tilde.gif"),
 			new NNKeyPress[] {
 				new NNKeyPress(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
 				new NNKeyPress(KeyEvent.VK_N)},
 			"tilde");
		tilde.addActionListener(NNSymbolKeypad.this);
 		key_row.add(tilde);

		// Add place holder buttons.
		key_row.add(new NNSymbolButton());
		key_row.add(new NNSymbolButton());
		key_row.add(new NNSymbolButton());

		marks_cluster.add(key_row);
	 	
	 	// Return the completed cluster
	 	return marks_cluster;
	}
}

/**
 * This class represents the abstract novaNET keypad buttons.
 */
abstract class NNKeyButton extends JButton
{
	/**
	 * Construct a keypad key button with its label, key presses this button
	 * represents, and its tooltip.
	 *
	 * @param	button_label	Text for the displayed button label
	 * @param	nn_keys			Array of NNKeyPress(es) associated with this
	 *							button.
	 * @param	tooltip			Text for the associated tooltip.
	 */
	public NNKeyButton(
		String button_label,
		NNKeyPress[] nn_keys,
		String tooltip)
	{
		// Pass along the button_label to JButton()
		super(button_label);
	
		// Send the rest of this to a standard initializer
		initialize(nn_keys, tooltip);
	}

	/**
	 * Construct a keypad key button with its label, a key press this button
	 * represents, and its tooltip.
	 *
	 * @param	button_label	Text for the displayed button label
	 * @param	nn_key			Single NNKeyPress associated with this button.
	 * @param	tooltip			Text for the associated tooltip
	 */
	public NNKeyButton(
		String button_label,
		NNKeyPress nn_key,
		String tooltip)
	{
		this(button_label, new NNKeyPress[]{nn_key}, tooltip);
	}

	/**
	 * Construct a keypad key button with its icon, key presses this button
	 * represents, and its tooltip.
	 *
	 * @param	icon	ImageIcon for the displayed button label
	 * @param	nn_keys	Array of NNKeyPress(es) associated with this button.
	 * @param	tooltip	Text for the associated tooltip
	 */
	public NNKeyButton(
		ImageIcon icon,
		NNKeyPress[] nn_keys,
		String tooltip)
	{
		// Pass along the icon to JButton()
		super(icon);

		// Send the rest of this to a standard initializer
		initialize(nn_keys, tooltip);
	}

	/**
	 * Construct a keypad key button with its icon, a key presse this button
	 * represents, and its tooltip.
	 *
	 * @param	icon			ImageIcon for the displayed button label
	 * @param	nn_key			Single NNKeyPress associated w/this button
	 * @param	tooltip			Text for the associated tooltip
	 */
	public NNKeyButton(
		ImageIcon icon,
		NNKeyPress nn_key,
		String tooltip)
	{
		this(icon, new NNKeyPress[]{nn_key}, tooltip);
	}

	/**
	 * Get a list of key presses associated with this novaNET key button.
	 *
	 * @return	Array of key presses associated with this button.
	 */
	public NNKeyPress[] getKeyPresses()
	{
		return nn_keys;
	}

	// NovaNET keypresses associated with this button.
	private NNKeyPress[] nn_keys;

	/**
	 * Set those attributes that are common to all NNKeyButtons
	 *
	 * @param	nn_keys			Keypress(es) associated w/this button
	 * @param	tooltip			Text for the associated tooltip
	 */
	private void initialize(
		NNKeyPress[] nn_keys,
		String tooltip)
	{
		this.nn_keys = nn_keys;
		if (tooltip != null)
			setToolTipText(tooltip);

		// Set to a narrow margin.
		setMargin(new Insets(0, 0, 0, 0));
		// These buttons cannot be the default
		setDefaultCapable(false);
		// Set to not request focus when selected.
		setRequestFocusEnabled(false);
	}
}


/**
 * This class represents key button in the function keypad.
 */
class NNFunctionKeyButton extends NNKeyButton
{
	/**
	 * Create a button for a named NN function key.
	 *
	 * @param	button_label	Text for the displayed button label
	 * @param	nn_key			NNKeyPress associated w/this buttton
	 * @param	tooltip			Text for the associated tooltip
	 */
	public NNFunctionKeyButton(
		String button_label,
		NNKeyPress nn_key,
		String tooltip)
	{
  		super(button_label, nn_key, tooltip);
							 
		// Use a smaller, PLAIN font rather than the default BOLD
		Font button_font = getFont();
		button_font = button_font.deriveFont(
			Font.PLAIN,
			(float)0.8*button_font.getSize2D());
		setFont(button_font);
	}
	
	/**
	 * Override of JComponent.isFocusTraversable to prevent these buttons from
	 * ever receiving the focus.
	 */
	public boolean isFocusTraversable()
	{
		return false;
	}
}

/**
 * This class represents a shift button in the function keypad.
 */
class NNShiftFunctionKeyButton extends NNKeyButton
{
	/**
	 * Create a button for a shifted, named NN function key given a key code.
	 *
	 * @param	key_code	Int key code for the button's associated keypress.
	 * @param	tooltip		Text for the associated tooltip.
	 */
	public NNShiftFunctionKeyButton(
		int key_code,
		String tooltip)
	{
		this(
			new NNKeyPress(key_code, KeyEvent.SHIFT_MASK),
			tooltip);
	}

	/**
	 * Create a button for a shifted, named NN function key given a key press.
	 *
	 * @param	key_press	NNKeyPress associated with this button.
	 * @param	tooltip		Text for button's tooltip.
	 */
	public NNShiftFunctionKeyButton(
		NNKeyPress key_press,
		String tooltip)
	{
		super(Keypad.SHIFT_ICON, key_press, tooltip);	
	}

	/**
	 * Override of JComponent.isFocusTraversable to prevent these buttons from
	 * ever receiving the focus.
	 */
	public boolean isFocusTraversable()
	{
		return false;
	}
}	

/**
 * This class represents buttons in the symbol keypad.
 */
class NNSymbolButton extends NNKeyButton
{
	/**
	 * Construct a place holder symbol button for the symbol keypad.
	 */
	public NNSymbolButton()
	{
		this(
			Keypad.EMPTY_ICON,
			new NNKeyPress[]{},
			null);
	}

	/**
	 * Construct a symbol button with specified icon, its key press this
	 * button represents, and its tooltip.
	 *
	 * @param		icon		ImageIcon to label the button
	 * @param		key_press	Single keypress to produce this symbol
	 * @param		tooltip		Text to display as tooltip
	 */
	public NNSymbolButton(
		ImageIcon icon,
		NNKeyPress key_press,
		String tooltip)
	{
		this(
			icon,
			new NNKeyPress[]{key_press},
			tooltip);
	}

	/** 
	 * Construct a symbol button with specified icon, key presses this
	 * button represents, and its tooltip.
	 *
	 * @param		icon		ImageIcon to label the button
	 * @param		key_presses	Array of NNKeyPresses to produce this symbol
	 * @param		tooltip		Text to display as tooltip
	 */
	public NNSymbolButton(
		ImageIcon icon,
		NNKeyPress[] key_presses,
		String tooltip)
	{
		// Create a button with an icon, a keypress, and a tooltip
		super(
			icon,
			key_presses,
			tooltip);

		// Set to no visible border
		setBorderPainted(false);

		// Set button size to icon size.
		Dimension my_size = new Dimension(15, icon.getIconHeight()+2);
		setMinimumSize(my_size);
		setMaximumSize(my_size);
		setPreferredSize(my_size);
		
		// Add mouse listener to modify plotting on mouse entry or exit
		// (but only if this is a non-null symbol key, i.e. w/tooltip)
		if (tooltip != null)
		{
			addMouseListener(new MouseAdapter() 
			{					
				public void mouseEntered(MouseEvent e) 
				{
					setBorderPainted(true);
				}
				public void mouseExited(MouseEvent e) 
				{
					setBorderPainted(false);
				}
			});
		}
	}

	/**
	 * Override of JComponent.isFocusTraversable to prevent these buttons from
	 * ever receiving the focus.
	 */
	public boolean isFocusTraversable()
	{
		return false;
	}
}

/**
 * This class represents a novaNET key press.
 */
class NNKeyPress
{
	/**
	 * Construct a novaNET key press with specified key code.
	 *
	 * @param	key_code	Key code associated w/this keypress.
	 */		
	public NNKeyPress(int key_code)
	{
		this(key_code, 0);
	}

	/**
	 * Construct a novaNET key press with specified key code and its modifier.
	 *
	 * @param	key_code	Key code associated w/this key press.
	 * @param	modifiers	Key modifiers associated with this key press.
	 */		
	public NNKeyPress(
		int key_code,
		int modifiers)
	{
		this.key_code = key_code;
		this.modifiers = modifiers;

		// Construct the unicode character associated with this keypress.
		if ((key_code >= (int) ' ') && (key_code <= (int) ']'))
		{
			key_char = (char) key_code;

			if ((key_char >= (int) 'A') && (key_char <= (int) 'Z'))
			{
				// If not modified w/shift, set ASCII char to lower case.
				if ((modifiers & KeyEvent.SHIFT_MASK) == 0)
					key_char = (char)((int) key_char + 32);
			}
			// If we have a shifted, non-alphabetic key, determine its
			// ASCII char.
			else if ((modifiers & KeyEvent.SHIFT_MASK) != 0)
			{
				switch (key_code)
				{
					case (int)',':
						key_char = '<';
						break;
					case (int)'.':
						key_char = '>';
						break;
					case (int)'/':
						key_char = '?';
						break;
					case (int)';':
						key_char = ':';
						break;
					case (int)'\'':
						key_char = '"';
						break;
					case (int)'0':
						key_char = ')';
						break;
					default:
						break;
				}
			}
		}
		else
		{
			key_char = KeyEvent.CHAR_UNDEFINED;
		}
		
		// Uncomment when want to debug.
//		System.out.println("NNKeyPress: "+
//			KeyEvent.getKeyModifiersText(modifiers)+"+"+
//			KeyEvent.getKeyText(key_code)+
//			"("+getKeyChar()+")");
	}


	/**
	 * Send the key press to a specified level one panel.
	 *
	 * @param	key_target	The level one panel to send the key to.
	 */
	public void sendKey(LevelOnePanel key_target)
	{
		key_target.getParser().keyPressed(getKeyEvent());
	}

	/**
	 * Return a KeyEvent for this NNKeyPress.
	 *
	 * @return	KeyEvent	The KeyEvent resulting from this NNKeyPress
	 */
	public KeyEvent getKeyEvent()
	{
		// Construct the desired KeyEvent
		KeyEvent key_event = new KeyEvent(
			COMPONENT_PLACE_HOLDER,
			KeyEvent.KEY_PRESSED,		// type of event
			System.currentTimeMillis(),	// (current) time of event
			modifiers,
			key_code,
			key_char);
			
		// Uncomment when want to debug.
//		System.out.println("Press of: "+
//			KeyEvent.getKeyModifiersText(modifiers)+"+"+
//			KeyEvent.getKeyText(key_code));

		return key_event;
	}

	/**
	 * This is a component place holder for creating a key event. We really
	 * don't care what generates the key press, so we use it to satisfy
	 * KeyEvent construction.
	 */
	private static final Container COMPONENT_PLACE_HOLDER = new Container();
	
	/** The key code for this key press. */
	private int	key_code;
	/** The modifier for this key press. */
	private int modifiers;
	/** The key character for this key press. */
	private char key_char;	
}
