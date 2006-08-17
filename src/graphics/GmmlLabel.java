package graphics;
/*
Copyright 2005 H.C. Achterberg, R.M.H. Besseling, I.Kaashoek, 
M.M.Palm, E.D Pelgrim, BiGCaT (http://www.BiGCaT.unimaas.nl/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and 
limitations under the License.
*/


import gmmlVision.GmmlVision;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;
import util.SwtUtils;
import data.GmmlData;


public class GmmlLabel extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
	
	public static final int INITIAL_FONTSIZE = 10;
	public static final int INITIAL_WIDTH = 80;
	public static final int INITIAL_HEIGHT = 20;
	
	public final List attributes = Arrays.asList(new String[] {
			"TextLabel", "CenterX", "CenterY", "Width","Height",
			"FontName","FontWeight","FontStyle","FontSize","Color",
			"Notes", "Comment"
	});
	
	double getFontSize()
	{
		return gdata.getFontSize() * canvas.getZoomFactor();
	}
	
	void setFontSize(double v)
	{
		gdata.setFontSize(v / canvas.getZoomFactor());
	}
	
	String getLabelText()
	{
		return gdata.getLabelText();
	}
				
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this label will be part of
	 */
	public GmmlLabel(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_LABEL;

		gdata.setFontSize (INITIAL_FONTSIZE);
	}
	
	/**
	 * Constructor for this class
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param width - widht
	 * @param height - height
	 * @param text - the labels text
	 * @param font - the labels font
	 * @param fontWeight - fontweigth
	 * @param fontStyle - fontstyle
	 * @param fontSize - fontsize
	 * @param color - the color the label is painted
	 * @param canvas - the GmmlDrawing the label will be part of
	 */
	public GmmlLabel (int x, int y, int width, int height, String text, String font, String fontWeight, 
		String fontStyle, int fontSize, RGB color, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.centerX  = x;
		this.centerY = y;
		setGmmlWidth(width);
		setGmmlHeight(height);
				
		calcStart();	
		setHandleLocation();
		createJdomElement(doc);
	}
	
	public GmmlLabel (int x, int y, int width, int height, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.centerX = x;
		this.centerY = y;
		setGmmlWidth(width);
		setGmmlHeight(height);
		
		calcStart();	
		setHandleLocation();
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlLabel
	 * @param canvas - the GmmlDrawing this GmmlLabel will be part of
	 */
	public GmmlLabel (Element e, GmmlDrawing canvas) {
		this(canvas);
		
		this.gdata.jdomElement = e;
		mapAttributes(e);
		gdata.mapColor();
		gdata.mapLabelData();
		gdata.mapNotesAndComment();
		calcStart();
		setHandleLocation();
	}
	
	public void setLabelText(String text) {
		gdata.setLabelText (text);
		
		//Adjust width to text length
		GC gc = new GC(canvas.getDisplay());
		Font f = new Font(canvas.getDisplay(), 
				gdata.getFontName(), 
				(int)gdata.getFontSize(), getFontStyle());
		gc.setFont (f);
		Point ts = gc.textExtent(text);
		
		//Keep center location
		double nWidth = ts.x + 10 * getDrawing().getZoomFactor();
		double nHeight = ts.y + 10 * getDrawing().getZoomFactor();
		startX -= (nWidth - width)/2;
		startY -= (nHeight - height)/2;
		width = nWidth;
		height = nHeight;
		
		updateToPropItems();
		
		setHandleLocation();
		f.dispose();
		gc.dispose();
	}
	
	/**
	 * Updates the JDom representation of this label
	 */
	public void updateJdomElement() {
		if(gdata.jdomElement != null) {
			gdata.updateNotesAndComment();
			Element jdomGraphics = gdata.jdomElement.getChild("Graphics");			
			if(jdomGraphics !=null) {
				gdata.updateLabelData();
				gdata.updateColor();
				jdomGraphics.setAttribute("CenterX", Integer.toString(getCenterX() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString(getCenterY() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Integer.toString((int)height * GmmlData.GMMLZOOM));				
			}
		}
	}
	
	private Text t;
	public void createTextControl()
	{
		Color background = canvas.getShell().getDisplay()
		.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		
		Composite textComposite = new Composite(canvas, SWT.NONE);
		textComposite.setLayout(new GridLayout());
		textComposite.setLocation(getCenterX(), getCenterY() - 10);
		textComposite.setBackground(background);
		
		Label label = new Label(textComposite, SWT.CENTER);
		label.setText("Specify label:");
		label.setBackground(background);
		t = new Text(textComposite, SWT.SINGLE | SWT.BORDER);

		t.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
				
		t.setFocus();
		
		Button b = new Button(textComposite, SWT.PUSH);
		b.setText("OK");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
		
		textComposite.pack();
	}
	
	protected void disposeTextControl()
	{
		markDirty();
		setLabelText(t.getText());
		markDirty();
		Composite c = t.getParent();
		c.setVisible(false);
		c.dispose();
				
		canvas.redrawDirtyRect();
	}
	
	protected void createJdomElement(Document doc) {
		if(gdata.jdomElement == null) {
			gdata.jdomElement = new Element("Label");
			gdata.jdomElement.addContent(new Element("Graphics"));
			
			doc.getRootElement().addContent(gdata.jdomElement);
		}
	}
	
	protected void adjustToZoom(double factor)
	{
		gdata.setLeft(gdata.getLeft() * factor);
		gdata.setTop(gdata.getTop() * factor);
		gdata.setWidth(gdata.getWidth() * factor);
		gdata.setHeight(gdata.getHeight() * factor);
		gdata.setFontSize(gdata.getFontSize() * factor);
		setHandleLocation();
	}

	private int getFontStyle() {
		int style = SWT.NONE;
		
		if (gdata.isBold())
		{
			style |= SWT.BOLD;
		}
		
		if (gdata.isItalic())
		{
			style |= SWT.ITALIC;
		}
		return style;
	}
	
	protected void draw(PaintEvent e, GC buffer)
	{
		int style = getFontStyle();
		
		Font f = new Font(e.display, gdata.getFontName(), (int)gdata.getFontSize(), style);
		
		buffer.setFont (f);
		
		Point textSize = buffer.textExtent (gdata.getLabelText());
		
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
		
		buffer.drawString (gdata.getLabelText(), 
			(int) getCenterX() - (textSize.x / 2) , 
			(int) getCenterY() - (textSize.y / 2), true);
		
		f.dispose();
		c.dispose();
		
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
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
		
		Object[] values = new Object[] {gdata.getLabelText(), (double)getCenterX(), 
				(double)getCenterY(), width, height, gdata.getFontName(), gdata.isBold(), 
				gdata.isItalic(), gdata.getFontSize(), gdata.getColor(), 
				gdata.getNotes(), gdata.getComment()};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();
		
		gdata.setLabelText (((String)propItems.get(attributes.get(0))));
		centerX		= (Double)propItems.get(attributes.get(1));
		centerY		= (Double)propItems.get(attributes.get(2));
		width		= (Double)propItems.get(attributes.get(3));
		height 		= (Double)propItems.get(attributes.get(4));
		gdata.setFontName	((String)propItems.get(attributes.get(5)));
		gdata.setBold ((Boolean)propItems.get(attributes.get(6)));
		gdata.setItalic ((Boolean)propItems.get(attributes.get(7)));
		gdata.setFontSize	((Double)propItems.get(attributes.get(8)));
		gdata.setColor		((RGB)propItems.get(attributes.get(9)));
		gdata.setNotes((String)propItems.get(attributes.get(10)));
		gdata.setComment ((String)propItems.get(attributes.get(11)));
		//Check for change in text and resize width if needed
		
		calcStart();
		markDirty();
		setHandleLocation();
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
					case 0: // TextLabel
						break;
					case 1: // CenterX
						this.centerX = Double.parseDouble(value) / GmmlData.GMMLZOOM ; break;
					case 2: // CenterY
						this.centerY = Double.parseDouble(value) / GmmlData.GMMLZOOM; break;
					case 3: // Width
						this.width = Double.parseDouble(value) / GmmlData.GMMLZOOM; break;
					case 4:	// Height
						this.height = Double.parseDouble(value) / GmmlData.GMMLZOOM; break;
					case 5: // FontName
						break;
					case 6: // FontWeight
						break;
					case 7: // FontStyle
						break;
					case 8: // FontSize
						break;
					case 9: // Color
						break;
					case 10: // Notes
						break;
					case 11: // Comment
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
}
