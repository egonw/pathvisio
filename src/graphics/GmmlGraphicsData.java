package graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.jdom.*;

import util.ColorConverter;

import data.*;

public abstract class GmmlGraphicsData 
{
	protected int objectType = ObjectType.GENEPRODUCT;
	public int getObjectType() { return objectType; }
	public void setObjectType(int v) { objectType = v; }
	
	// only for lines:	
	protected double startx = 0;
	public double getStartX() { return startx; }
	public void setStartX(double value) { startx = value; }
	
	protected double starty = 0;
	public double getStartY() { return starty; }
	public void setStartY(double value) { starty = value; }
	
	protected double endx = 0;
	public double getEndX() { return endx; }
	public void setEndX(double value) { endx = value; }
	
	protected double endy = 0;
	public double getEndY() { return endy; }
	public void setEndY(double value) { endy = value; }
	
	protected int lineStyle = LineStyle.SOLID;
	public int getLineStyle() { return lineStyle; }
	public void setLineStyle(int value) { lineStyle = value; }
	
	protected int lineType = LineType.LINE;
	public int getLineType() { return lineType; }
	public void setLineType(int value) { lineType = value; }
			
	protected RGB color = new RGB(0, 0, 0);	
	public RGB getColor() { return color; }
	public void setColor(RGB value) { color = value; }
	
	// general
	protected String comment = "";
	public String getComment() { return comment; }
	public void setComment (String v) { comment = v; }
	
	protected String notes = "";
	public String getNotes() { return notes; }
	public void setNotes (String v) { notes = v; }
	
	// for geneproduct only
	protected String geneID = "";
	public String getGeneID() { return geneID; }
	public void setGeneID(String v) { geneID = v; }
	
	protected String xref = "";
	public String getXref() { return xref; }
	public void setXref(String v) { xref = v; }
	
	protected String geneProductName = "";
	public String getGeneProductName() { return geneProductName; }
	public void setGeneProductName(String v) { geneProductName = v; } 
	
	protected String backpageHead = "";
	public String getBackpageHead() { return backpageHead; }
	public void setBackpageHead(String v) { backpageHead = v; }
	
	protected String geneProductType = "unknown";
	public String getGeneProductType() { return geneProductType; }
	public void setGeneProductType(String v) { geneProductType = v; }
	
	protected String dataSource = "";
	public String getDataSource() { return dataSource; }
	public void setDataSource(String v) { dataSource = v; } 
	 
	protected double centerx = 0;
	public double getCenterX() { return centerx; }
	public void setCenterX(double v) { centerx = v; }
	
	protected double centery = 0;
	public double getCenterY() { return centery; }
	public void setCenterY(double v) { centery = v; }
	
	protected double width = 0;
	public double getWidth() { return width; }
	public void setWidth(double v) { width = v; }
	
	protected double height = 0;
	public double getHeight() { return height; }
	public void setHeight(double v) { height = v; }
		
	// starty for shapes
	public double getTop() { return centery - height / 2; }
	public void setTop(double v) { centery = v + height / 2; }
	
	// startx for shapes
	public double getLeft() { return centerx - width / 2; }
	public void setLeft(double v) { centerx = v + width / 2; }
	
	protected int shapeType = ShapeType.RECTANGLE;
	public int getShapeType() { return shapeType; }
	public void setShapeType(int v) { shapeType = v; }
	
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

	protected double rotation = 0; // in radians
	public double getRotation() { return rotation; }
	public void setRotation(double v) { rotation = v; }
	
	// for labels
	protected boolean fBold = false;
	public boolean isBold() { return fBold; }
	public void setBold(boolean v) { fBold = v; }
	
	protected boolean fStrikethru = false;
	public boolean isStrikethru() { return fStrikethru; }
	public void setStrikethru(boolean v) { fStrikethru = v; }
	
	protected boolean fUnderline = false;
	public boolean isUnderline() { return fUnderline; }
	public void setUnderline(boolean v) { fUnderline = v; }
	
	protected boolean fItalic = false;
	public boolean isItalic() { return fItalic; }
	public void setItalic(boolean v) { fItalic = v; }
	
	protected String fontName= "Arial";
	public String getFontName() { return fontName; }
	public void setFontName(String v) { fontName = v; }
	
	protected String labelText = "";
	public String getLabelText() { return labelText; }
	public void setLabelText (String v) { labelText = v; }
	
	protected double fontSize = 1;	
	public double getFontSize() { return fontSize; }
	public void setFontSize(double v) { fontSize = v; }	
	
	protected String mapInfoName = "";
	public String getMapInfoName() { return mapInfoName; }
	public void setMapInfoName (String v) { mapInfoName = v; }
	
	protected String organism = "";
	public String getOrganism() { return organism; }
	public void setOrganism (String v) { organism = v; }

	protected String mapInfoDataSource = "";
	public String getMapInfoDataSource() { return mapInfoDataSource; }
	public void setMapInfoDataSource (String v) { mapInfoDataSource = v; }

	protected String version = "";
	public String getVersion() { return version; }
	public void setVersion (String v) { version = v; }

	protected String author = "";
	public String getAuthor() { return author; }
	public void setAuthor (String v) { author = v; }

	protected String maintainedBy = ""; 
	public String getMaintainedBy() { return maintainedBy; }
	public void setMaintainedBy (String v) { maintainedBy = v; }

	protected String email = "";
	public String getEmail() { return email; }
	public void setEmail (String v) { email = v; }

	protected String availability = "";
	public String getAvailability() { return availability; }
	public void setAvailability (String v) { availability = v; }

	protected String lastModified = "";
	public String getLastModified() { return lastModified; }
	public void setLastModified (String v) { lastModified = v; }
	
	protected double boardWidth;
	public double getBoardWidth() { return boardWidth; }
	public void setBoardWidth(double v) { boardWidth = v; }

	protected double boardHeight;
	public double getBoardHeight() { return boardHeight; }
	public void setBoardHeight(double v) { boardHeight = v; }

	protected double windowWidth;
	public double getWindowWidth() { return windowWidth; }
	public void setWindowWidth(double v) { windowWidth = v; }

	protected double windowHeight;
	public double getWindowHeight() { return windowHeight; }
	public void setWindowHeight(double v) { windowHeight = v; }
	
	protected int mapInfoLeft;
	public int getMapInfoLeft() { return mapInfoLeft; }
	public void setMapInfoLeft(int v) { mapInfoLeft = v; }

	protected int mapInfoTop;
	public int getMapInfoTop() { return mapInfoTop; }
	public void setMapInfoTop(int v) { mapInfoTop = v; }
}
