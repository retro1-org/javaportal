/**
 * PROJECT	Portal panel
 * FILE		LevelOneParser2.java
 *
 *			(c) copyright 1999
 *			NCS NovaNET Learning
 *			
 * @author	J Hegarty
 */

package com.nn.osiris.ui;

import java.awt.*;
import java.awt.image.*;
//import java.awt.geom.*;

/**
 * Decodes the level one terminal protocol with graphics 2D capability.
 */
public class LevelOneParser2 extends LevelOneParser
{
	/**
	 * Construct a graphics 2D capable level one parser.
	 *
	 * @param	frame
	 * @param	container
	 * @param	g1
	 * @param	g2
	 * @param	image
	 * @param	g3
	 * @param	network			
	 * @param	width
	 * @param	resource_server
	 */
	public LevelOneParser2(
		Frame frame,
		Container container,
		Graphics g1,
		Graphics g2,
		Image image,
		Graphics g3,
		LevelOneNetwork network,
		int width)
	{
		super(
			frame,
			container,
			g1,
			g2,
			image,
			g3,
			network,
			width);
	}

	/**
	 * Construct a graphics 2D capable level one parser from another.
	 *
	 * @param	base	The one to copy from.
	 */
	public LevelOneParser2(LevelOneParser2 base)
	{
		super(base);
	}

	private Paint old_paint;
	private Paint old_paint2;
	private Stroke old_stroke;
	private Stroke old_stroke2;

	/**
	 * Render ellipse.
	 *
	 * @param	x_radius	X radius for the ellipse.
	 * @param	y_radius	Y radius for the ellipse.
	 * @param	x			X coordinate of center point.
	 * @param	y			Y coordinate of center point.
	 * @param	mode		Mode to draw the ellipse in.
	 * @param	spattern	Pattern to fill the ellipse. Currently not used.
	 * @param	sthick		Thickness of the pen stroke.
	 * @param	sfill		Ellipse is filled.
	 */
	/*
	private void plotEllipse(
		int x_radius,
		int y_radius,
		int x,
		int y,
		int mode,
		int spattern,
		int sthick,
		int sfill)
	{
		int	x1;
		int y1;
		
		x = xlatX(x);
		y = xlatY(y);
		
		x1 = -x_radius + x;
		y1 = -y_radius + y;
		
		applyClipping();
		
		if (sfill != 0)
		{
			Ellipse2D.Float	obj = new Ellipse2D.Float(
				x1,
				y1,
				x_radius*2,
				y_radius*2);
			
			renderShape(obj, true, false, false, true);
		}
		else
		{
			Ellipse2D.Float	obj = new Ellipse2D.Float(
				x1,
				y1,
				x_radius*2,
				y_radius*2);

			renderShape(obj, true, true, true, false);
		}
	}
*/
	/**
	 * Render arc.
	 *
	 * @param	x_radius	X radius for the arc.
	 * @param	y_radius	Y radius for the arc.
	 * @param	start_angle	Start angle of the arc.
	 * @param	arc_length	Arc length.
	 * @param	x			X coordinate of center point.
	 * @param	y			Y coordinate of center point.
	 * @param	mode		Mode to draw the arc in.
	 * @param	spattern	Pattern to fill the arc. Currently not used.
	 * @param	sthick		Thickness of the pen stroke.
	 * @param	sfill		Arc is filled.
	 */
	
	/*
	private void plotArc(
		int x_radius,
		int y_radius,
		int start_angle, 
		int arc_length,
		int x,
		int y,
		int mode,
		int spattern,
		int sthick,
		int sfill)
	{   
		int x1;
		int y1;

		if (0 == arc_length)
			arc_length = 3600;

		if (arc_length >= 3600)
		{
			plotEllipse(
				x_radius,
				y_radius,
				x,
				y,
				mode,
				spattern,
				sthick,
				sfill);
			
			return;
		}

		start_angle = start_angle/10;
		if (arc_length < 0)
			start_angle =(360+(start_angle+(arc_length/10)))%360;

		arc_length = abs(arc_length/10);

		x = xlatX(x);
		y = xlatY(y);

		x1 = x-x_radius;
		y1 = y-y_radius;

		if (sfill != 0)
		{
			Arc2D.Float	thearc = new Arc2D.Float(
				x1,
				y1,
				x_radius*2,
				y_radius*2,
				start_angle,
				arc_length,
				Arc2D.PIE);

			renderShape(thearc, true, false, false, true);
		}
		else
		{
			Arc2D.Float	thearc = new Arc2D.Float(
				x1,
				y1,
				x_radius*2,
				y_radius*2,
				start_angle,
				arc_length,
				Arc2D.OPEN);

			renderShape(thearc, true, true, false, false);
		}
	}
*/
	/**
	 * Gets pattern brush to draw with.
	 *
	 * @param	pattern	Include pattern in the brush.
	 * @return			A paint brush with specified quality.
	 */
	private Paint getPaint(boolean pattern)
	{
		if (!pattern || style_pattern == 1)
			return getModeFGColor();
		else if (style_pattern == 0)
			return getModeBGColor();
		else
		{
			BufferedImage bimage = 
				new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
			int x;
			int y;
			int fpixel = (255 << 24) | getModeFGColor().getRGB();
			int	bpixel = (255 << 24) | getModeBGColor().getRGB();
			int[] hpat;
			int npat;

			if (style_pattern < 32)
			{
				hpat = system_patterns;
				npat = style_pattern;
			}
			else
			{
				hpat = user_patterns;
				npat = style_pattern-32;
			}

			for (x=0; x<8; x++)
			{
				for (y=0; y<8; y++)
				{
					if (0 != (hpat[8*npat+y] & (0x80 >> x)))
						bimage.setRGB(x, y, fpixel);
					else
						bimage.setRGB(x, y, bpixel);
				}
			}

			Rectangle rect = new Rectangle(xlatX(0), xlatY(LevelOnePanel.PANEL_HEIGHT-1), 8, 8);

			return new TexturePaint(bimage, rect);
		}
	}

	/**
	 * Sets up stroke(cap, join, thickness, dash) to draw with.
	 *
	 * @param	thick	Thick stroke.
	 * @param	dash	Dash stroke.
	 * @return			A stroke with specified quality.
	 */
	private BasicStroke getStroke(
		boolean thick,
		boolean dash)
	{
		int	cap = BasicStroke.CAP_BUTT;
		int join = BasicStroke.JOIN_ROUND;

		switch (style_cap)
		{
			// Flat.
			case 0:
				cap = BasicStroke.CAP_BUTT;
				break;
			// Round.
			case 1:
				cap = BasicStroke.CAP_ROUND;
				break;
			// Square.
			case 2:
				cap = BasicStroke.CAP_SQUARE;
				break;
		}

		switch (style_join)
		{
			// Mitre.
			case 0:
				join = BasicStroke.JOIN_MITER;
				break;
			// Bevel.
			case 1:
				join = BasicStroke.JOIN_BEVEL;
				break;
			// Round.
			case 2:
				join = BasicStroke.JOIN_ROUND;
				break;
		}

		if (dash && style_dash != 0)
		{
			float[]	sdash;

			if (style_dash < 8)
				sdash = system_dashes[style_dash];
			else
				sdash = user_dashes[style_dash-8];

			return new BasicStroke(
				thick ? style_thickness : 1,
				cap,
				join,
				10,
				sdash,
				0);
		}
		else
		{
			return new BasicStroke(
				thick ? style_thickness : 1,
				cap,
				join);
		}
	}

	/**
	 * Saves current paint & stroke.
	 */
	private void saveState()
	{
		Graphics2D g2d;

		if (is_direct_draw)
		{
			g2d = (Graphics2D)levelone_graphics;

			old_paint = g2d.getPaint();
			old_stroke = g2d.getStroke();
		}

		g2d = (Graphics2D)levelone_offscreen;
		old_paint2 = g2d.getPaint();
		old_stroke2 = g2d.getStroke();
	}

	/**
	 * Restores previous paint & stroke.
	 */
	private void restoreState()
	{
		Graphics2D g2d;

		if (is_direct_draw)
		{
			g2d = (Graphics2D)levelone_graphics;

			g2d.setPaint(old_paint);
			g2d.setStroke(old_stroke);
		}

		g2d = (Graphics2D)levelone_offscreen;
		g2d.setPaint(old_paint2);
		g2d.setStroke(old_stroke2);

		old_paint = old_paint2 = null;
		old_stroke = old_stroke2 = null;
	}

	/**
	 * Renders a shape.
	 *
	 * @param	obj			The shape we want to render.
	 * @param	pattern		Render with pattern.
	 * @param	thickness	Render with thick stroke.
	 * @param	dash		Render with dash stroke.
	 * @param	fill		Fill the shape.
	 */
	private void renderShape(
		Shape obj,
		boolean pattern,
		boolean thickness,
		boolean dash,
		boolean fill)
	{
		BasicStroke stroke = getStroke(thickness, dash);
		Paint paint = getPaint(pattern);
		Graphics2D g2d;

		saveState();
		modeClipColor(true, true, false);

		if (is_direct_draw)
		{
			g2d = (Graphics2D)levelone_graphics;
			g2d.setPaint(paint);
			g2d.setStroke(stroke);

			if (fill && style_fill != 0)
				g2d.fill(obj);
			else
				g2d.draw(obj);
		}
		else
			do_repaint = true;

		g2d = (Graphics2D)levelone_offscreen;
		g2d.setPaint(paint);
		g2d.setStroke(stroke);

		if (fill && style_fill != 0)
			g2d.fill(obj);
		else
			g2d.draw(obj);

		restoreState();
	}

  	/**
  	 * Override super class method. Plot polygon that is currently being held.
	 *
	 * @see	LevelOneParser#checkPoly.
  	 */
  	protected void checkPoly()
  	{
  		if (null != polygon)
  		{
  			if (style_fill != 0)
  			{
  				renderShape(polygon, true, false, false, true);
  				renderShape(polygon, true, false, false, false);
  			}
  			else
  			{
  				BasicStroke	stroke = getStroke(true, true);
  				Paint paint = getPaint(true);
  				Graphics2D g2d;
				
  				saveState();
  				modeClipColor(true, true, false);
  				if (is_direct_draw)
  				{
  					g2d = (Graphics2D) levelone_graphics;
  					g2d.setPaint(paint);
  					g2d.setStroke(stroke);
					
  					levelone_graphics.drawPolyline(
  						polygon.xpoints,
  						polygon.ypoints,
  						polygon.npoints);
  				}
  				else
  					do_repaint = true;
				
  				g2d = (Graphics2D)levelone_offscreen;
  				g2d.setPaint(paint);
  				g2d.setStroke(stroke);
				
  				levelone_offscreen.drawPolyline(
  					polygon.xpoints,
  					polygon.ypoints,
  					polygon.npoints);
  				restoreState();
  			}
  			polygon = null;
  		}
  	}

	/**
	 * Override super class method. Render box.
	 *
	 * @see	LevelOneParser#plotBox
	 */
	protected void plotBox(
		int x1,
		int y1,
		int x2,
		int y2,
		int thickness,
		int mode,
		int spattern,
		int fill_flag)
	{
		if (abs(thickness) > 1024)
			return;
		if (abs(x1) > 2048 ||
			abs(y1) > 2048 ||
			abs(x2) > 2048 ||
			abs(y2) > 2048)
		{
			return;
		}

		x1 = xlatX(x1);
		y1 = xlatY(y1);
		x2 = xlatX(x2);
		y2 = xlatY(y2);
		
		if (x1 > x2)
		{
			int	i;
			
			i = x1;
			x1 = x2;
			x2 = i;
		}
		if (y1 > y2)
		{
			int	i;
			
			i = y1;
			y1 = y2;
			y2 = i;
		}
		
		
		// If the fill flag is set, fill the rectangle bound by the points.
		if (fill_flag != 0)
		{
		Rectangle obj = new Rectangle();

			obj.setBounds(x1, y1, x2-x1+1, y2-y1+1);
			renderShape(obj, true, false, false, true);
		}

		// Otherwise draw rectangles for the points.
		else
		{
			int	i;
			int delta;
			
			if (thickness == 0)
				thickness = 1;
			
			if (thickness < 0)
			{
				delta = -1;
				thickness = -1*thickness;
			}
			else
				delta = 1;

			BasicStroke stroke = getStroke(false, false);
			Paint paint = getPaint(true);
			Graphics2D g2d;
			// note: the code only uses 5 points, but
			// the Mac AWT code blows up with less than
			// 6 points in these arrays!
			int polyx[] = new int[6];
			int polyy[] = new int[6];
			
			saveState();
			modeClipColor(true, true, false);
			
			// Setup graphics state for drawing.
			if (is_direct_draw)
			{
				g2d = (Graphics2D)levelone_graphics;
				g2d.setPaint(paint);
				g2d.setStroke(stroke);
			}

			g2d = (Graphics2D)levelone_offscreen;
			g2d.setPaint(paint);
			g2d.setStroke(stroke);
			
			for (i = 0; i < thickness; i++)
			{
				polyx[0] = x1;
				polyy[0] = y1;
				polyx[1] = x2;
				polyy[1] = y1;
				polyx[2] = x2;
				polyy[2] = y2;
				polyx[3] = x1;
				polyy[3] = y2;
				polyx[4] = polyx[0];
				polyy[4] = polyy[0];
				
				if (is_direct_draw)
					levelone_graphics.drawPolyline(polyx, polyy, 5);
				else
					do_repaint = true;

				levelone_offscreen.drawPolyline(polyx, polyy, 5);
				x1 -= delta; y1 -= delta;
				x2 += delta; y2 += delta;
			}
			
			restoreState();
		}
	}

	/**
	 * Override super class method. Plots a trap buffer.
	 *
	 * @see	LevelOneParser#trapPlot
	 */
	protected void trapPlot(
		int buffer,
		int offx,
		int offy,
		int mode)
	{
		if (buffer >= 0 && buffer < TRAP_SLOTS)
		{
			if (null != trap_slots[buffer])
			{
				LevelOneParser trapengine = new LevelOneParser2(this);
				byte[] trap_buffer;

				trapengine.center_x = center_x+offx;
				trapengine.center_y = center_y+offy;
				trapengine.screen_mode = mode;

				trap_buffer = trap_slots[buffer].toByteArray();
				trapengine.ParseStream(trap_buffer, 0, trap_buffer.length);
			}
		}
	}
}
