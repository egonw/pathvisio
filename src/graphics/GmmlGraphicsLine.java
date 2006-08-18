package graphics;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * This is an {@link GmmlGraphics} class representing line forms,
 * which all have a start and end coordinates + a start and end handle
 */
public abstract class GmmlGraphicsLine extends GmmlGraphics {
	GmmlHandle handleStart;
	GmmlHandle handleEnd;
		 
	public GmmlGraphicsLine(GmmlDrawing canvas, double startx, double starty, double endx, double endy) 
	{
		super(canvas);
		
		handleStart	= new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		handleEnd	= new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		
		gdata.setStartX(startx);
		gdata.setStartY(starty);
		gdata.setEndX(endx);
		gdata.setEndY(endy);		
	}
	
	/**
	 * Constructs the line for the coordinates stored in this class
	 */
	public Line2D getLine()
	{
		return new Line2D.Double(gdata.getStartX(), gdata.getStartY(), gdata.getEndX(), gdata.getEndY());
	}
	
	/**
	 * Sets the line start and end to the coordinates specified
	 * <DL><B>Parameters</B>
	 * <DD>Double x1	- new startx 
	 * <DD>Double y1	- new starty
	 * <DD>Double x2	- new endx
	 * <DD>Double y2	- new endy
	 */
	public void setLine(double x1, double y1, double x2, double y2)
	{
		gdata.setStartX(x1);
		gdata.setStartY(y1);
		gdata.setEndX(x2);
		gdata.setEndY(y2);
		
		setHandleLocation();		
	}
	
	public void setScaleRectangle(Rectangle2D.Double r) {
		markDirty();
		gdata.setStartX(r.x);
		gdata.setStartY(r.y);
		gdata.setEndY (r.x + r.width);
		gdata.setEndY (r.y + r.height);
		
		setHandleLocation();
		markDirty();
	}
	
	protected Rectangle2D.Double getScaleRectangle() {
		return new Rectangle2D.Double(gdata.getStartX(), gdata.getStartY(), gdata.getEndX()
				- gdata.getStartX(), gdata.getEndY() - gdata.getStartY());
	}
	
	/**
	 * Sets this class handles at the correct position 
	 */
	protected void setHandleLocation()
	{
		handleStart.setLocation(gdata.getStartX(), gdata.getStartY());
		handleEnd.setLocation(gdata.getEndX(), gdata.getEndY());
	}
	
	public GmmlHandle[] getHandles()
	{
		return new GmmlHandle[] { handleStart, handleEnd };
	}
	
	protected void adjustToHandle(GmmlHandle h) {
		markDirty();
		if		(h == handleStart) {
			gdata.setStartX(h.centerx); 
			gdata.setStartY(h.centery);
		}
		else if	(h == handleEnd) {
			gdata.setEndX(h.centerx); 
			gdata.setEndY(h.centery);
		}
		markDirty();
	}
	
	protected void moveBy(double dx, double dy)
	{
		markDirty();
		setLine(gdata.getStartX() + dx, gdata.getStartY() + dy, 
				gdata.getEndX() + dx, gdata.getEndY() + dy);
		markDirty();		
		setHandleLocation();
	}
	
	protected void adjustToZoom(double factor)
	{
		gdata.setStartX(gdata.getStartX() * factor);
		gdata.setStartY(gdata.getStartY() * factor);
		gdata.setEndX(gdata.getEndX() * factor);
		gdata.setEndY(gdata.getEndY() * factor);
		
		setHandleLocation();
	}

	public void updateFromPropItems()
	{
		markDirty();	
		gdata.updateFromPropItems();
		markDirty();
		setHandleLocation();
		canvas.redrawDirtyRect();
	}
}
