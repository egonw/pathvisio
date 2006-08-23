package graphics;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.jdom.Document;
import org.jdom.Element;

import preferences.GmmlPreferences;
import data.*;


/**
 * This class is a parent class for all graphics
 * that can be added to a GmmlDrawing.
 */
public abstract class GmmlGraphics extends GmmlDrawingObject
{
	public static RGB selectColor = GmmlPreferences.getColorProperty("colors.selectColor");
	public static RGB highlightColor = GmmlPreferences.getColorProperty("colors.highlightColor");
	
	protected GmmlDataObject gdata = new GmmlDataObject();
	
	public GmmlGraphics(GmmlDrawing canvas) {
		super(canvas);
	}
	
	public void select()
	{
		super.select();
		for (GmmlHandle h : getHandles())
		{
			h.show();
		}
	}
	
	public void deselect()
	{
		super.deselect();
		for (GmmlHandle h : getHandles())
		{
			h.hide();
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();	
		gdata.updateFromPropItems();
		markDirty();
		//setHandleLocation();
		canvas.redrawDirtyRect();
	}
	
	public List getAttributes() { return gdata.getAttributes() ;}
}