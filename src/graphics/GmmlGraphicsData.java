package graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.jdom.*;

import util.ColorConverter;

import data.*;

public class GmmlGraphicsData 
{
	public 
		Element jdomElement = null;
	
	void createJdomElement(Document doc, String tag, boolean addGraphics)
	{
		if(jdomElement == null) {
			jdomElement = new Element(tag);
			if (addGraphics)
			{
				jdomElement.addContent(new Element("Graphics"));		
			}
			doc.getRootElement().addContent(jdomElement);
		}		
	}
	
	private int objectType = ObjectType.GENEPRODUCT;
	public int getObjectType() { return objectType; }
	public void setObjectType(int v) { objectType = v; }
	
	// only for lines:	
	private double startx = 0;
	public double getStartX() { return startx; }
	public void setStartX(double value) { startx = value; }
	
	private double starty = 0;
	public double getStartY() { return starty; }
	public void setStartY(double value) { starty = value; }
	
	private double endx = 0;
	public double getEndX() { return endx; }
	public void setEndX(double value) { endx = value; }
	
	private double endy = 0;
	public double getEndY() { return endy; }
	public void setEndY(double value) { endy = value; }
	
	private int lineStyle = LineStyle.SOLID;
	public int getLineStyle() { return lineStyle; }
	public void setLineStyle(int value) { lineStyle = value; }
	
	private int lineType = LineType.LINE;
	public int getLineType() { return lineType; }
	public void setLineType(int value) { lineType = value; }
		
	void mapLineData()
	{
    	Element graphics = jdomElement.getChild("Graphics");
    	
    	startx = Double.parseDouble(graphics.getAttributeValue("StartX")) / GmmlData.GMMLZOOM;
    	starty = Double.parseDouble(graphics.getAttributeValue("StartY")) / GmmlData.GMMLZOOM;
    	endx = Double.parseDouble(graphics.getAttributeValue("EndX")) / GmmlData.GMMLZOOM;
    	endy = Double.parseDouble(graphics.getAttributeValue("EndY")) / GmmlData.GMMLZOOM; 
    	
    	String style = jdomElement.getAttributeValue("Style");
    	String type = jdomElement.getAttributeValue("Type");
    	
		lineStyle = (style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED;
		lineType = (type.equals("Line")) ? LineType.LINE : LineType.ARROW;
	}
	
	void updateLineData()
	{
		if(jdomElement != null) {
			jdomElement.setAttribute("Type", lineType == LineType.LINE ? "Line" : "Arrow");
			jdomElement.setAttribute("Style", lineStyle == LineStyle.SOLID ? "Solid" : "Broken");
			
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics != null) {
				jdomGraphics.setAttribute("StartX", Double.toString(startx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("StartY", Double.toString(starty * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("EndX", Double.toString(endx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("EndY", Double.toString(endy * GmmlData.GMMLZOOM));
			}
			
		}
	}
	
	private RGB color = new RGB(0, 0, 0);	
	public RGB getColor() { return color; }
	public void setColor(RGB value) { color = value; }
	
	void mapColor()
	{
    	Element graphics = jdomElement.getChild("Graphics");    	
    	color = ColorConverter.gmmlString2Color(graphics.getAttributeValue("Color"));	
	}
	
	void updateColor()
	{
		if(jdomElement != null) 
		{
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics != null) 
			{
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
			}
		}
	}
	
	// general
	private String comment = "";
	String getComment() { return comment; }
	void setComment (String v) { comment = v; }
	
	private String notes = "";
	String getNotes() { return notes; }
	void setNotes (String v) { notes = v; }
	
	void mapNotesAndComment()
	{
    	notes = jdomElement.getChildText("Notes");
    	if (notes == null) notes = "";
    	
    	String comment = jdomElement.getChildText("Comment");
    	if (comment == null) comment = "";
	}
	
	void updateNotesAndComment()
	{
		if(jdomElement != null) 
		{
			jdomElement.setAttribute("Notes", notes);
			jdomElement.setAttribute("Comments", comment);
		}
	}
	
	// for geneproduct only
	private String geneID = "";
	public String getGeneID() { return geneID; }
	public void setGeneID(String v) { geneID = v; }
	
	private String xref = "";
	public String getXref() { return xref; }
	public void setXref(String v) { xref = v; }
	
	private String geneProductName = "";
	public String getGeneProductName() { return geneProductName; }
	public void setGeneProductName(String v) { geneProductName = v; } 
	
	private String backpageHead = "";
	public String getBackpageHead() { return backpageHead; }
	public void setBackpageHead(String v) { backpageHead = v; }
	
	private String geneProductType = "unknown";
	public String getGeneProductType() { return geneProductType; }
	public void setGeneProductType(String v) { geneProductType = v; }
	
	private String dataSource = "";
	public String getDataSource() { return dataSource; }
	public void setDataSource(String v) { dataSource = v; } 

	public void mapGeneProductData()
	{
		geneID = jdomElement.getAttributeValue("GeneID");
		xref = jdomElement.getAttributeValue("Xref");
		geneProductType = jdomElement.getAttributeValue("Type");
		geneProductName = jdomElement.getAttributeValue("Name");
		backpageHead = jdomElement.getAttributeValue("BackpageHead");
		dataSource = jdomElement.getAttributeValue("GeneProduct-Data-Source");
	}

	public void updateGeneProductData()
	{
		if(jdomElement != null) {
			jdomElement.setAttribute("GeneID", geneID);
			jdomElement.setAttribute("Xref", xref);
			jdomElement.setAttribute("Type", geneProductType);
			jdomElement.setAttribute("Name", geneProductName);
			jdomElement.setAttribute("BackpageHead", backpageHead);
			jdomElement.setAttribute("GeneProduct-Data-Source", dataSource);
		}
	}
	 
	private double centerx = 0;
	public double getCenterX() { return centerx; }
	public void setCenterX(double v) { centerx = v; }
	
	private double centery = 0;
	public double getCenterY() { return centery; }
	public void setCenterY(double v) { centery = v; }
	
	private double width = 0;
	public double getWidth() { return width; }
	public void setWidth(double v) { width = v; }
	
	private double height = 0;
	public double getHeight() { return height; }
	public void setHeight(double v) { height = v; }
		
	// starty for shapes
	public double getTop() { return centery - height / 2; }
	public void setTop(double v) { centery = v + height / 2; }
	
	// startx for shapes
	public double getLeft() { return centerx - width / 2; }
	public void setLeft(double v) { centerx = v + width / 2; }
	
	private int shapeType = ShapeType.RECTANGLE;
	public int getShapeType() { return shapeType; }
	public void setShapeType(int v) { shapeType = v; }
	
	/**
	 * Sets the width of the graphical representation.
	 * This differs from the GMML representation:
	 * in GMML height and width are radius, here for all shapes the width is diameter
	 * TODO: change to diameter in gmml
	 * @param gmmlWidth the width as specified in the GMML representation
	 */
	private void setGmmlWidth(double gmmlWidth) {
		if ((objectType == ObjectType.SHAPE &&
				shapeType == ShapeType.RECTANGLE) ||
				objectType == ObjectType.LABEL ||
				objectType == ObjectType.GENEPRODUCT)
		{
			width = gmmlWidth;
		}		
		else
		{
			width = gmmlWidth * 2;
		}			
	}
	
	/**
	 * Get the width as stored in GMML
	 * @return
	 */
	private double getGmmlWidth() 
	{
		if ((objectType == ObjectType.SHAPE &&
				shapeType == ShapeType.RECTANGLE) ||
				objectType == ObjectType.LABEL ||
				objectType == ObjectType.GENEPRODUCT)
		{
			return width;
		}		
		else
		{
			return width / 2;
		}
	}
	
	/**
	 * Sets the height of the graphical representation.
	 * This differs from the GMML representation:
	 * in some GMML objects height and width are radius, here for all shapes the width is diameter
	 * TODO: change to diameter in gmml
	 *  @param gmmlHeight the height as specified in the GMML representation
	 */
	private void setGmmlHeight(double gmmlHeight) 
	{
		if ((objectType == ObjectType.SHAPE &&
				shapeType == ShapeType.RECTANGLE) ||
				objectType == ObjectType.LABEL ||
				objectType == ObjectType.GENEPRODUCT)
		{
			height = gmmlHeight;
		}		
		else
		{
			height = gmmlHeight * 2;
		}			
	}
	
	/**
	 * Get the height as stored in GMML
	 * @return
	 */
	private double getGmmlHeight() 
	{
		if ((objectType == ObjectType.SHAPE &&
				shapeType == ShapeType.RECTANGLE) ||
				objectType == ObjectType.LABEL ||
				objectType == ObjectType.GENEPRODUCT)
		{
			return height;
		}
		else
		{
			return height / 2;
		}
	}

	// internal helper routine
	private void mapCenter()
	{
    	Element graphics = jdomElement.getChild("Graphics");
		centerx = Double.parseDouble(graphics.getAttributeValue("CenterX")) / GmmlData.GMMLZOOM; 
		centery = Double.parseDouble(graphics.getAttributeValue("CenterY")) / GmmlData.GMMLZOOM;	
	}
	
	private void updateCenter()
	{
		if(jdomElement != null) 
		{
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("CenterX", Double.toString(centerx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Double.toString(centery * GmmlData.GMMLZOOM));
			}
		}		
	}
	
	public void mapShapeData()
	{
    	mapCenter();
		Element graphics = jdomElement.getChild("Graphics");
		setGmmlWidth(Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM); 
		setGmmlHeight(Double.parseDouble(graphics.getAttributeValue("Height")) / GmmlData.GMMLZOOM);
	}
	
	public void updateShapeData()
	{
		if(jdomElement != null) 
		{
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				updateCenter();
				jdomGraphics.setAttribute("Width", Double.toString(getGmmlWidth() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Double.toString(getGmmlHeight() * GmmlData.GMMLZOOM));
			}
		}
	}

	public void setOrientation(int orientation) {
		switch (orientation)
		{
			case OrientationType.TOP: setRotation(0); break;
			case OrientationType.LEFT: setRotation(Math.PI/2); break;
			case OrientationType.RIGHT: setRotation(Math.PI); break;
			case OrientationType.BOTTOM: setRotation(Math.PI*(3.0/2)); break;
		}
	}
		
	public int getOrientation() {
		double r = rotation / Math.PI;
		if(r < 1.0/4 || r >= 7.0/4) return OrientationType.TOP;
		if(r > 1.0/4 && r <= 3.0/4) return OrientationType.LEFT;
		if(r > 3.0/4 && r <= 5.0/4) return OrientationType.BOTTOM;
		if(r > 5.0/4 && r <= 7.0/4) return OrientationType.RIGHT;
		return 0;
	}

	public void mapBraceData()
	{
    	mapCenter();
		Element graphics = jdomElement.getChild("Graphics");
		setGmmlWidth(Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM); 
		setGmmlHeight(Double.parseDouble(graphics.getAttributeValue("PicPointOffset")) / GmmlData.GMMLZOOM);
		int orientation = OrientationType.getMapping(graphics.getAttributeValue("Orientation"));
		if(orientation > -1)
			setOrientation(orientation);
	}
	
	public void updateBraceData()
	{
		if(jdomElement != null) 
		{
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				updateCenter();
				jdomGraphics.setAttribute("Width", Double.toString(width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("PicPointOffset", Double.toString(height * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Orientation", OrientationType.getMapping(getOrientation()));
			}
		}
	}

	private double rotation = 0; // in radians
	public double getRotation() { return rotation; }
	public void setRotation(double v) { rotation = v; }

	void mapRotation()
	{
    	Element graphics = jdomElement.getChild("Graphics");
		rotation = Double.parseDouble(graphics.getAttributeValue("Rotation")); 
	}

	void updateRotation()
	{
		if(jdomElement != null) 
		{
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("Rotation", Double.toString(rotation));
			}
		}	
	}
	
	// for labels
	private boolean fBold = false;
	boolean isBold() { return fBold; }
	void setBold(boolean v) { fBold = v; }
	
	private boolean fStrikethru = false;
	boolean isStrikethru() { return fStrikethru; }
	void setStrikethru(boolean v) { fStrikethru = v; }
	
	private boolean fUnderline = false;
	boolean isUnderline() { return fUnderline; }
	void setUnderline(boolean v) { fUnderline = v; }
	
	private boolean fItalic = false;
	boolean isItalic() { return fItalic; }
	void setItalic(boolean v) { fItalic = v; }
	
	private String fontName= "Arial";
	String getFontName() { return fontName; }
	void setFontName(String v) { fontName = v; }
	
	private String labelText = "";
	String getLabelText() { return labelText; }
	void setLabelText (String v) { labelText = v; }
	
	private double fontSize = 1;	
	double getFontSize() { return fontSize; }
	void setFontSize(double v) { fontSize = v; }
	
	void mapLabelData()
	{
		labelText = jdomElement.getAttributeValue("TextLabel");
    	Element graphics = jdomElement.getChild("Graphics");
    	
    	String fontWeight = graphics.getAttributeValue("FontWeight");
    	String fontStyle = graphics.getAttributeValue("FontStyle");
    	String fontDecoration = graphics.getAttributeValue ("FontDecoration");
    	String fontStrikethru = graphics.getAttributeValue ("FontStrikethru");
    	
    	fBold = (fontWeight != null && fontWeight.equals("Bold"));   	
    	fItalic = (fontStyle != null && fontStyle.equals("Italic"));    	
    	fUnderline = (fontDecoration != null && fontDecoration.equals("Underline"));    	
    	fStrikethru = (fontStrikethru != null && fontStrikethru.equals("Strikethru"));    	
	}
	
	void updateLabelData()
	{
		if(jdomElement != null) 
		{
			jdomElement.setAttribute("TextLabel", labelText);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomElement.setAttribute("FontName", fontName);			
				jdomElement.setAttribute("FontWeight", fBold ? "Bold" : "Normal");
				jdomElement.setAttribute("FontStyle", fItalic ? "Italic" : "Normal");
				jdomElement.setAttribute("FontDecoration", fUnderline ? "Underline" : "Normal");
				jdomElement.setAttribute("FontStrikethru", fStrikethru ? "Strikethru" : "Normal");
				jdomElement.setAttribute("FontSize", Integer.toString((int)fontSize));
			}
		}
	}

	List<String> getAttributes()
	{
		return attributes;
	}
	
	private static final List attributes = Arrays.asList(new String[] {
			
			// all
			"Notes", "Comment",

			// line, shape, brace, geneproduct, label
			"Color", 
			
			// shape, brace, geneproduct, label
			"CenterX", "CenterY", "Width", "Height", 
			
			// shape
			"ShapeType", "Rotation", 
			
			// line
			"StartX", "StartY", "EndX", "EndY",			
			"LineType", "LineStyle",
			
			// brace
			"Orientation",
			
			// gene product
			"Name", "GeneProduct-Data-Source", "GeneID", 
			"Xref", "BackpageHead", "Type", 
			
			// label
			"TextLabel", 
			"FontName","FontWeight","FontStyle","FontSize"
			
			 
	});
	
	public void updateToPropItems()
	{
		if (propItems == null)
		{
			propItems = new Hashtable();
		}
		
		Object[] values = new Object[] {
				getNotes(), getComment(),
				
				getColor(),
				
				getCenterX(), getCenterY(),
				getWidth(), getHeight(), 
				
				getShapeType(), 
				getRotation(),
				
				getStartX(), getStartY(),
				getEndX(), getEndY(),
				getLineType(), getLineStyle(),
				
				getOrientation(),
				
				getGeneProductName(),
				getDataSource(),
				getGeneID(),
				getXref(),
				getBackpageHead(),
				getGeneProductType(),
				
				getLabelText(),				
				getFontName(),
				isBold(),
				isItalic(),
				getFontSize()
				
		};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		setNotes		((String) propItems.get("Notes"));
		setComment 		((String) propItems.get("Comment"));

		setColor 		((RGB)    propItems.get("Color"));
		
		setCenterX 		((Double) propItems.get("CenterY"));
		setCenterY 		((Double) propItems.get("CenterX"));
		setWidth		((Double) propItems.get("Width"));
		setHeight		((Double) propItems.get("Height"));
		
		setShapeType	((Integer)propItems.get("ShapeType"));
		setRotation		((Double) propItems.get("Rotation"));
		
		setStartX 		((Double) propItems.get("StartX"));
		setStartY 		((Double) propItems.get("StartY"));
		setEndX 		((Double) propItems.get("EndX"));
		setEndY 		((Double) propItems.get("EndY"));
		setLineType		((Integer)propItems.get("LineType"));
		setLineStyle	((Integer)propItems.get("LineStyle"));
		
		setOrientation	((Integer)propItems.get("Orientation"));

		setGeneID 		((String)  propItems.get("GeneID"));
		setXref			((String)  propItems.get("Xref"));
		setGeneProductName	((String)propItems.get("Name"));
		setBackpageHead	((String)  propItems.get("BackpageHead"));
		setGeneProductType	((String)propItems.get("Type"));
		setDataSource 	((String)  propItems.get("GeneProduct-Data-Source"));
		
		setLabelText 	(((String) propItems.get("TextLabel")));
		setFontName		((String)  propItems.get("FontName"));
		setBold 		((Boolean) propItems.get("FontWeight"));
		setItalic 		((Boolean) propItems.get("FontStyle"));
		setFontSize		((Double)  propItems.get("FontSize"));
	}
	
	//Methods dealing with property table
	public Hashtable propItems;

}
