package graphics;

import org.eclipse.swt.graphics.RGB;
import org.jdom.*;

import util.ColorConverter;

import data.*;

public class GmmlGraphicsData 
{
	public 
		Element jdomElement;
	
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
	
	// only for lines:	
	private double startx;
	public double getStartX() { return startx; }
	public void setStartX(double value) { startx = value; }
	
	private double starty;
	public double getStartY() { return starty; }
	public void setStartY(double value) { starty = value; }
	
	private double endx;
	public double getEndX() { return endx; }
	public void setEndX(double value) { endx = value; }
	
	private double endy;
	public double getEndY() { return endy; }
	public void setEndY(double value) { endy = value; }
	
	private int lineStyle;
	public int getLineStyle() { return lineStyle; }
	public void setLineStyle(int value) { lineStyle = value; }
	
	private int lineType;
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
	
	private RGB color;
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
	private String comment = null;
	String getComment() { return comment; }
	void setComment (String v) { comment = v; }
	
	private String notes = null;
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
	public String ID;
	public String SystemCode;
	public String Type;
	
	void mapGeneProductData()
	{
		
	}
	
	void updateGeneProductData()
	{

	}

	 
	double centerx;
	double getCenterX() { return centerx; }
	void setCenterX(double v) { centerx = v; }
	
	double centery;
	double getCenterY() { return centery; }
	void setCenterY(double v) { centery = v; }
	
	double width;
	double getWidth() { return width; }
	void setWidth(double v) { width = v; }
	
	double height;
	double getHeight() { return height; }
	void setHeight(double v) { height = v; }
		
	// starty for shapes
	double getTop() { return centery - height / 2; }
	void setTop(double v) { centery = v + height / 2; }
	
	// startx for shapes
	double getLeft() { return centerx - width / 2; }
	void setLeft(double v) { centerx = v + width / 2; }
	
	void mapShapeData()
	{
    	Element graphics = jdomElement.getChild("Graphics");
		centerx = Double.parseDouble(graphics.getAttributeValue("CenterX")) / GmmlData.GMMLZOOM ; 
		centery = Double.parseDouble(graphics.getAttributeValue("CenterY")) / GmmlData.GMMLZOOM; 
		width = Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM; 
		height = Double.parseDouble(graphics.getAttributeValue("Height")) / GmmlData.GMMLZOOM;
	}
	
	void updateShapeData()
	{
		if(jdomElement != null) 
		{
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("CenterX", Double.toString(centerx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Double.toString(centery * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Double.toString(width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Double.toString(height * GmmlData.GMMLZOOM));
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
	
	private String fontName= "";
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

}
