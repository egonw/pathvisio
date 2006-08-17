package graphics;

import gmmlVision.GmmlVision;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;
import util.SwtUtils;
import data.*;
 
/**
 * This class implements and handles a line
 */
public class GmmlLine extends GmmlGraphicsLine
{
	private static final long serialVersionUID = 1L;
			
	public final List attributes = Arrays.asList(new String[] {
			"StartX", "StartY", "EndX", "EndY", "Color", "Style", "Type", "Notes", "Comment"
	});
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this line will be part of
	 */
	public GmmlLine(GmmlDrawing canvas, double startx, double starty, double endx, double endy)
	{
		super(canvas, startx, starty, endx, endy);
		drawingOrder = GmmlDrawing.DRAW_ORDER_LINE;
	}
	
	/**
	 * Constructor for this class
	 * @param startx - start x coordinate
	 * @param starty - start y coordinate
	 * @param endx - end x coordinate
	 * @param endy - end y coordinate
	 * @param color - color this line will be painted
	 * @param canvas - the GmmlDrawing this line will be part of
	 */
	public GmmlLine(double startx, double starty, double endx, double endy, RGB color, GmmlDrawing canvas, Document doc)
	{
		this(canvas, startx, starty, endx, endy);
		
		gdata.setColor (color);
		
		setHandleLocation();
		
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlLine
	 * @param canvas - the GmmlDrawing this GmmlLine will be part of
	 */
	public GmmlLine (Element e, GmmlDrawing canvas) {
		this(canvas, 0, 0, 0, 0);
		
		this.gdata.jdomElement = e;

//		mapAttributes(e);
		gdata.mapLineData();
		gdata.mapColor();
		gdata.mapNotesAndComment();
		
		setHandleLocation();
	}

	/**
	 * Updates the JDom representation of this label
	 */	
	public void updateJdomElement() {
		gdata.updateLineData();
		if(gdata.jdomElement != null) {
			gdata.updateNotesAndComment();
			gdata.updateColor();
		}
	}
	
	protected void createJdomElement(Document doc) {
		gdata.createJdomElement(doc, "Line", true);
	}
	
	protected void draw(PaintEvent e, GC buffer)
	{
		Line2D line = getLine();
		Color c = null;
		if (isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, gdata.getColor(), e.display);
		}
		buffer.setForeground (c);
		buffer.setBackground (c);
		
		buffer.setLineWidth (1);
		int ls = gdata.getLineStyle();
		if (ls == LineStyle.SOLID)
		{
			buffer.setLineStyle (SWT.LINE_SOLID);
		}
		else if (ls == LineStyle.DASHED)
		{ 
			buffer.setLineStyle (SWT.LINE_DASH);
		}
		
		buffer.drawLine ((int)line.getX1(), (int)line.getY1(), (int)line.getX2(), (int)line.getY2());
		
		if (gdata.getLineType() == LineType.ARROW)
		{
			drawArrowhead(buffer);
		}
		c.dispose();
		
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	protected boolean isContain(Point2D p)
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(getLine());
		return outline.contains(p);
	}

	protected boolean intersects(Rectangle2D.Double r)
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(getLine());
		
		return outline.intersects(r);
	}
	
	protected Rectangle getBounds()
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(getLine());
		return outline.getBounds();
	}
	
	/**
	 * If the line type is arrow, this method draws the arrowhead
	 */
	private void drawArrowhead(GC buffer) //TODO! clean up this mess.....
	{
		double angle = 25.0;
		double theta = Math.toRadians(180 - angle);
		double[] rot = new double[2];
		double[] p = new double[2];
		double[] q = new double[2];
		double a, b, norm;
		
		rot[0] = Math.cos(theta);
		rot[1] = Math.sin(theta);
		
		buffer.setLineStyle (SWT.LINE_SOLID);
		
		double endx = gdata.getEndX();
		double endy = gdata.getEndY();
		double startx = gdata.getStartX();
		double starty = gdata.getStartY();
		
		a = endx-startx;
		b = endy-starty;
		norm = 8/(Math.sqrt((a*a)+(b*b)));				
		p[0] = ( a*rot[0] + b*rot[1] ) * norm + endx;
		p[1] = (-a*rot[1] + b*rot[0] ) * norm + endy;
		q[0] = ( a*rot[0] - b*rot[1] ) * norm + endx;
		q[1] = ( a*rot[1] + b*rot[0] ) * norm + endy;
		int[] points = {
			(int)endx, (int)endy,
			(int)(p[0]), (int)(p[1]),
			(int)(q[0]), (int)(q[1])
		};
		
		buffer.drawPolygon (points);
		buffer.fillPolygon (points);
	}

	/**
	 * Maps attributes to internal variables.
	 * @param e - the element to map to a GmmlArc
	 */
	private void mapAttributes (Element e) {
		// Map attributes
		GmmlVision.log.trace("> Mapping element '" + e.getName()+ "'");
		Iterator it = e.getAttributes().iterator();
		while(it.hasNext()) {
			Attribute at = (Attribute)it.next();
			int index = attributes.indexOf(at.getName());
			String value = at.getValue();
			switch(index) {
					case 0: // StartX
						break;
					case 1: // StartY
						break;
					case 2: // EndX
						break;
					case 3: // EndY
						break;
					case 4: // Color
						break;
					case 5: // Style
						break;
					case 6: // Type
						break;
					case 7: //Notes
						break;
					case -1:
						GmmlVision.log.trace("\t> Attribute '" + at.getName() + "' is not recognized");
			}
		}
		// Map child's attributes
		it = e.getChildren().iterator();
		while(it.hasNext()) {
			mapAttributes((Element)it.next());
		}
	}
	
	public List getAttributes() {
		return attributes;
	}
	
	public void updateToPropItems()
	{
		if (propItems == null)
		{
			propItems = new Hashtable();
		}
		
		Object[] values = new Object[] {
				gdata.getStartX(), gdata.getStartY(), 
				 gdata.getEndX(), gdata.getEndY(), 
				 gdata.getColor(), 
				 gdata.getLineStyle(), gdata.getLineType(), 
				 gdata.getNotes(), gdata.getComment()};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();
		
		gdata.setStartX ((Double)propItems.get(attributes.get(0)));
		gdata.setStartY ((Double)propItems.get(attributes.get(1)));
		gdata.setEndX ((Double)propItems.get(attributes.get(2)));
		gdata.setEndY ((Double)propItems.get(attributes.get(3)));
		gdata.setColor ((RGB)propItems.get(attributes.get(4)));
		gdata.setLineStyle	((Integer)propItems.get(attributes.get(5)));
		gdata.setLineType   ((Integer)propItems.get(attributes.get(6)));
		gdata.setNotes ((String)propItems.get(attributes.get(7)));
		gdata.setComment((String)propItems.get(attributes.get(8)));
				
		markDirty();
		setHandleLocation();
		
	}
}
