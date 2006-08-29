package data;

import graphics.GmmlGraphicsData;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.jdom.*;

import util.ColorConverter;


public class GmmlDataObject extends GmmlGraphicsData 
{	
	public void mapComplete(Element e)
	{
		String tag = e.getName();
		objectType = ObjectType.getTagMapping(tag);
		switch (objectType)
		{
		
			case ObjectType.BRACE: // brace
				mapNotesAndComment(e);
				mapColor(e);
				mapBraceData(e);
				break;
			case ObjectType.GENEPRODUCT:
				mapShapeData(e);
				mapColor(e);
				mapNotesAndComment(e);
				mapGeneProductData(e);
				break;
			case ObjectType.LABEL:
				mapShapeData(e);
				mapColor(e);
				mapLabelData(e);
				mapNotesAndComment(e);
				break;
			case ObjectType.LINE:
				mapLineData(e);
				mapColor(e);
				mapNotesAndComment(e);
				break;
			case ObjectType.MAPPINFO:
				mapMappInfoData(e);
				break;
			case ObjectType.SHAPE:
				mapShapeData(e);
				mapColor(e);
				mapNotesAndComment(e);
				mapShapeType(e);
				mapRotation(e);
				break;
			case ObjectType.LEGEND:
				mapSimpleCenter(e);
				break;
			case ObjectType.INFOBOX:
				mapSimpleCenter (e);
				break;
		}
	}
	
	private static final List<String> gmmlLineTypes = Arrays.asList(new String[] {
			"Line", "Arrow", "TBar", "Receptor", "LigandSquare", 
			"ReceptorSquare", "LigandRound", "ReceptorRound"});

	private void mapLineData(Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	
    	Element p1 = (Element)graphics.getChildren().get(0);
    	Element p2 = (Element)graphics.getChildren().get(1);
    	
    	startx = Double.parseDouble(p1.getAttributeValue("x")) / GmmlData.GMMLZOOM;
    	starty = Double.parseDouble(p1.getAttributeValue("y")) / GmmlData.GMMLZOOM;
    	endx = Double.parseDouble(p2.getAttributeValue("x")) / GmmlData.GMMLZOOM;
    	endy = Double.parseDouble(p2.getAttributeValue("y")) / GmmlData.GMMLZOOM; 
    	
    	String style = e.getAttributeValue("Style");
    	String type = e.getAttributeValue("Type");
    	
		lineStyle = (style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED;
		lineType = gmmlLineTypes.indexOf(type);
	}
	
	private void updateLineData(Element e)
	{
		if(e != null) {
			e.setAttribute("Type", gmmlLineTypes.get(lineType));
			e.setAttribute("Style", lineStyle == LineStyle.SOLID ? "Solid" : "Broken");
			
			Element jdomGraphics = e.getChild("Graphics");
			Element p1 = new Element("Point");
			jdomGraphics.addContent(p1);
			p1.setAttribute("x", Double.toString(startx * GmmlData.GMMLZOOM));
			p1.setAttribute("y", Double.toString(starty * GmmlData.GMMLZOOM));
			Element p2 = new Element("Point");
			jdomGraphics.addContent(p2);
			p2.setAttribute("x", Double.toString(endx * GmmlData.GMMLZOOM));
			p2.setAttribute("y", Double.toString(endy * GmmlData.GMMLZOOM));			
		}
	}
	
	private void mapColor(Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	String scol = graphics.getAttributeValue("Color");
    	color = ColorConverter.gmmlString2Color(scol);
    	fTransparent = scol.equals("Transparent");
	}
	
	private void updateColor(Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics != null) 
			{
				if (fTransparent)
					jdomGraphics.setAttribute("Color", "Transparent");
				else
					jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
			}
		}
	}
		
	private void mapNotesAndComment(Element e)
	{
    	notes = e.getChildText("Notes");
    	if (notes == null) notes = "";
    	
    	comment = e.getChildText("Comment");
    	if (comment == null) comment = "";
	}
	
	private void updateNotesAndComment(Element e)
	{
		if(e != null) 
		{
			Element n = new Element("Notes");
			n.setText(notes);
			e.addContent(n);
			
			Element c = new Element ("Comment");
			c.setText(comment);
			e.addContent(c);
		}
	}
	
	private void mapGeneProductData(Element e)
	{
		geneID = e.getAttributeValue("GeneID");
		xref = e.getAttributeValue("Xref");
		geneProductType = e.getAttributeValue("Type");
		geneProductName = e.getAttributeValue("Name");
		backpageHead = e.getAttributeValue("BackpageHead");
		dataSource = e.getAttributeValue("GeneProduct-Data-Source");
	}

	private void updateGeneProductData(Element e)
	{
		if(e != null) {
			e.setAttribute("GeneID", geneID);
			e.setAttribute("Xref", xref);
			e.setAttribute("Type", geneProductType);
			e.setAttribute("Name", geneProductName);
			e.setAttribute("BackpageHead", backpageHead);
			e.setAttribute("GeneProduct-Data-Source", dataSource);
		}
	}
	 
	/**
	 * Sets the width of the graphical representation.
	 * This differs from the GMML representation:
	 * in GMML height and width are radius, here for all shapes the width is diameter
	 * TODO: change to diameter in gmml
	 * @param gmmlWidth the width as specified in the GMML representation
	 */
	private void setGmmlWidth(double gmmlWidth) {
//		if ((objectType == ObjectType.SHAPE &&
//				shapeType == ShapeType.RECTANGLE) ||
//				objectType == ObjectType.LABEL ||
//				objectType == ObjectType.GENEPRODUCT)
//		{
//			width = gmmlWidth;
//		}		
//		else
//		{
//			width = gmmlWidth * 2;
//		}
		width = gmmlWidth;
	}
	
	/**
	 * Get the width as stored in GMML
	 * @return
	 */
	private double getGmmlWidth() 
	{
//		if ((objectType == ObjectType.SHAPE &&
//				shapeType == ShapeType.RECTANGLE) ||
//				objectType == ObjectType.LABEL ||
//				objectType == ObjectType.GENEPRODUCT)
//		{
//			return width;
//		}		
//		else
//		{
//			return width / 2;
//		}
		return width;
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
//		if ((objectType == ObjectType.SHAPE &&
//				shapeType == ShapeType.RECTANGLE) ||
//				objectType == ObjectType.LABEL ||
//				objectType == ObjectType.GENEPRODUCT)
//		{
//			height = gmmlHeight;
//		}		
//		else
//		{
//			height = gmmlHeight * 2;
//		}			
		height = gmmlHeight;
	}
	
	/**
	 * Get the height as stored in GMML
	 * @return
	 */
	private double getGmmlHeight() 
	{
//		if ((objectType == ObjectType.SHAPE &&
//				shapeType == ShapeType.RECTANGLE) ||
//				objectType == ObjectType.LABEL ||
//				objectType == ObjectType.GENEPRODUCT)
//		{
//			return height;
//		}
//		else
//		{
//			return height / 2;
//		}
		return height;
	}

	// internal helper routine
	private void mapCenter(Element e)
	{
    	Element graphics = e.getChild("Graphics");
		centerx = Double.parseDouble(graphics.getAttributeValue("CenterX")) / GmmlData.GMMLZOOM; 
		centery = Double.parseDouble(graphics.getAttributeValue("CenterY")) / GmmlData.GMMLZOOM;	
	}
	
	private void updateCenter(Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("CenterX", Double.toString(centerx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Double.toString(centery * GmmlData.GMMLZOOM));
			}
		}		
	}

	private void mapSimpleCenter(Element e)
	{
		centerx = Double.parseDouble(e.getAttributeValue("CenterX")) / GmmlData.GMMLZOOM; 
		centery = Double.parseDouble(e.getAttributeValue("CenterY")) / GmmlData.GMMLZOOM;	
	}
	
	private void updateSimpleCenter(Element e)
	{
		if(e != null) 
		{
			e.setAttribute("CenterX", Double.toString(centerx * GmmlData.GMMLZOOM));
			e.setAttribute("CenterY", Double.toString(centery * GmmlData.GMMLZOOM));			
		}		
	}

	private void mapShapeData(Element e)
	{
    	mapCenter(e);
		Element graphics = e.getChild("Graphics");
		setGmmlWidth(Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM); 
		setGmmlHeight(Double.parseDouble(graphics.getAttributeValue("Height")) / GmmlData.GMMLZOOM);
	}
	
	private void updateShapeData(Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				updateCenter(e);
				jdomGraphics.setAttribute("Width", Double.toString(getGmmlWidth() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Double.toString(getGmmlHeight() * GmmlData.GMMLZOOM));
			}
		}
	}
	
	private void mapShapeType(Element e)
	{
		shapeType = ShapeType.getMapping(e.getAttributeValue("Type"));
	}
	
	private void updateShapeType(Element e)
	{
		if(e != null) 
		{
			e.setAttribute("Type", ShapeType.getMapping(shapeType));
		}
	}
	
	private void mapBraceData(Element e)
	{
    	mapCenter(e);
		Element graphics = e.getChild("Graphics");
		setGmmlWidth(Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM); 
		setGmmlHeight(Double.parseDouble(graphics.getAttributeValue("PicPointOffset")) / GmmlData.GMMLZOOM);
		int orientation = OrientationType.getMapping(graphics.getAttributeValue("Orientation"));
		if(orientation > -1)
			setOrientation(orientation);
	}
	
	private void updateBraceData(Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				updateCenter(e);
				jdomGraphics.setAttribute("Width", Double.toString(width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("PicPointOffset", Double.toString(height * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Orientation", OrientationType.getMapping(getOrientation()));
			}
		}
	}

	private void mapRotation(Element e)
	{
    	Element graphics = e.getChild("Graphics");
		rotation = Double.parseDouble(graphics.getAttributeValue("Rotation")); 
	}

	private void updateRotation(Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("Rotation", Double.toString(rotation));
			}
		}	
	}
	
	private void mapLabelData(Element e)
	{
		labelText = e.getAttributeValue("TextLabel");
    	Element graphics = e.getChild("Graphics");
    	
    	fontSize = Integer.parseInt(graphics.getAttributeValue("FontSize"));
    	
    	String fontWeight = graphics.getAttributeValue("FontWeight");
    	String fontStyle = graphics.getAttributeValue("FontStyle");
    	String fontDecoration = graphics.getAttributeValue ("FontDecoration");
    	String fontStrikethru = graphics.getAttributeValue ("FontStrikethru");
    	
    	fBold = (fontWeight != null && fontWeight.equals("Bold"));   	
    	fItalic = (fontStyle != null && fontStyle.equals("Italic"));    	
    	fUnderline = (fontDecoration != null && fontDecoration.equals("Underline"));    	
    	fStrikethru = (fontStrikethru != null && fontStrikethru.equals("Strikethru"));
    	
    	fontName = graphics.getAttributeValue("FontName");
	}
	
	private void updateLabelData(Element e)
	{
		if(e != null) 
		{
			e.setAttribute("TextLabel", labelText);
			Element graphics = e.getChild("Graphics");
			if(graphics !=null) 
			{
				graphics.setAttribute("FontName", fontName);			
				graphics.setAttribute("FontWeight", fBold ? "Bold" : "Normal");
				graphics.setAttribute("FontStyle", fItalic ? "Italic" : "Normal");
				graphics.setAttribute("FontDecoration", fUnderline ? "Underline" : "Normal");
				graphics.setAttribute("FontStrikethru", fStrikethru ? "Strikethru" : "Normal");
				graphics.setAttribute("FontSize", Integer.toString((int)fontSize));
			}
		}
	}
	
	private void mapMappInfoData(Element e)
	{
		mapInfoName = e.getAttributeValue("Name");
		organism = e.getAttributeValue("Organism");
		mapInfoDataSource = e.getAttributeValue("Data-Source");
		version = e.getAttributeValue("Version");
		author = e.getAttributeValue("Author");
		maintainedBy = e.getAttributeValue("Maintained-By");
		email = e.getAttributeValue("Email");
		lastModified = e.getAttributeValue("Last-Modified");
		
		Element g = e.getChild("Graphics");
		boardWidth = Double.parseDouble(g.getAttributeValue("BoardWidth")) / GmmlData.GMMLZOOM;
		boardHeight = Double.parseDouble(g.getAttributeValue("BoardHeight"))/ GmmlData.GMMLZOOM;
		windowWidth = Double.parseDouble(g.getAttributeValue("WindowWidth")) / GmmlData.GMMLZOOM;
		windowHeight = Double.parseDouble(g.getAttributeValue("WindowHeight"))/ GmmlData.GMMLZOOM;
		mapInfoLeft = 0;//Integer.parseInt(g.getAttributeValue("MapInfoLeft")) / GmmlData.GMMLZOOM;		
		mapInfoTop = 0;//Integer.parseInt(g.getAttributeValue("MapInfoTop")) / GmmlData.GMMLZOOM;		
	}
	
	private void updateMappInfoData(Element e)
	{
		e.setAttribute("Name", mapInfoName);
		e.setAttribute("Organism", organism);
		e.setAttribute("Data-Source", mapInfoDataSource);
		e.setAttribute("Version", version);
		e.setAttribute("Author", author);
		e.setAttribute("Maintained-By", maintainedBy);
		e.setAttribute("Email", email);
		e.setAttribute("Availability", availability);
		e.setAttribute("Last-Modified", lastModified);
		
		Element jdomGraphics = e.getChild("Graphics");
		if(jdomGraphics !=null) {
			jdomGraphics.setAttribute("BoardWidth", Integer.toString((int)boardWidth * GmmlData.GMMLZOOM));
			jdomGraphics.setAttribute("BoardHeight", Integer.toString((int)boardHeight * GmmlData.GMMLZOOM));
			jdomGraphics.setAttribute("WindowWidth", Integer.toString((int)windowWidth * GmmlData.GMMLZOOM));
			jdomGraphics.setAttribute("WindowHeight", Integer.toString((int)windowHeight * GmmlData.GMMLZOOM));
			//jdomGraphics.setAttribute("MapInfoLeft", Integer.toString(mapInfoLeft * GmmlData.GMMLZOOM));
			//jdomGraphics.setAttribute("MapInfoTop", Integer.toString(mapInfoTop * GmmlData.GMMLZOOM));
		}
	}

	public List<String> getAttributes()
	{
		return attributes;
	}
	
	/*
		public void updateToPropItems()
		{
			if (propItems == null)
			{
				propItems = new Hashtable();
			}
			
			Object[] values = new Object[] {name, organism, dataSource, version,
					author, maintainedBy, email, availability, lastModified,
					boardWidth, boardHeight, windowWidth, windowHeight, mapInfoLeft, mapInfoTop
					};
			
			for (int i = 0; i < attributes.size(); i++)
			{
				propItems.put(attributes.get(i), values[i]);
			}
		}

		public void updateFromPropItems()
		{
			markDirty();
			name			= (String)propItems.get(attributes.get(0));
			organism		= (String)propItems.get(attributes.get(1));
			dataSource		= (String)propItems.get(attributes.get(2));
			version			= (String)propItems.get(attributes.get(3));
			author			= (String)propItems.get(attributes.get(4));
			maintainedBy	= (String)propItems.get(attributes.get(5));
			email			= (String)propItems.get(attributes.get(6));
			availability	= (String)propItems.get(attributes.get(7));
			lastModified	= (String)propItems.get(attributes.get(8));
			boardWidth		= (Integer)propItems.get(attributes.get(9));
			boardHeight		= (Integer)propItems.get(attributes.get(10));
			windowWidth		= (Integer)propItems.get(attributes.get(11));
			windowHeight	= (Integer)propItems.get(attributes.get(12));
			mapInfoLeft	= (Integer)propItems.get(attributes.get(13));
			mapInfoTop		= (Integer)propItems.get(attributes.get(14));
			markDirty();
			canvas.redrawDirtyRect();
			//Also update the canvas and window size:
			canvas.setSize((int)boardWidth, (int)boardHeight);
//			canvas.gmmlVision.getShell().setSize(windowWidth, windowHeight);
		}
		*/

	private static final List<String> attributes = Arrays.asList(new String[] {
			
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
			propItems = new Hashtable<String, Object>();
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
	public Hashtable<String, Object> propItems;

	public Element createJdomElement() 
	{		
		Element e = null;
		
		switch (objectType)
		{
			case ObjectType.GENEPRODUCT:
				e = new Element("GeneProduct");
				updateNotesAndComment(e);
				e.addContent(new Element("Graphics"));			
				updateGeneProductData(e);
				updateColor(e);
				updateShapeData(e);
				break;
			case ObjectType.SHAPE:
				e = new Element ("Shape");		
				updateNotesAndComment(e);
				e.addContent(new Element("Graphics"));
					
				updateColor(e);
				updateRotation(e);
				updateShapeData(e);
				updateShapeType(e);
				break;
			case ObjectType.BRACE:
				e = new Element("Brace");
				updateNotesAndComment(e);
				e.addContent(new Element("Graphics"));
					
				updateColor(e);
				updateBraceData(e);
				break;
			case ObjectType.LINE:
				e = new Element("Line");
				updateNotesAndComment(e);
				e.addContent(new Element("Graphics"));				
				updateLineData(e);
				updateColor(e);
				break;
			case ObjectType.LABEL:
				e = new Element("Label");
				updateNotesAndComment(e);			
				e.addContent(new Element("Graphics"));					
				updateLabelData(e);
				updateColor(e);
				updateShapeData(e);
				break;
			case ObjectType.LEGEND:
				e = new Element ("Legend");
				updateSimpleCenter (e);
				break;
			case ObjectType.INFOBOX:
				e = new Element ("InfoBox");
				updateSimpleCenter (e);
				break;
		}
		return e;
		// TODO: throw exception if null!
	}
}
