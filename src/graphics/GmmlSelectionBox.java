package graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.jdom.Document;

/**
 * This class implements a selectionbox 
 */ 
class GmmlSelectionBox extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
		
	private ArrayList<GmmlDrawingObject> selection;
	boolean isSelecting;
	boolean isVisible;
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this selectionbox will be part of
	 */
	public GmmlSelectionBox(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_SELECTIONBOX;
		
		selection = new ArrayList<GmmlDrawingObject>();
	}	
	
	/**
	 * Add an object to the selection
	 * @param o
	 */
	public void addToSelection(GmmlDrawingObject o) {
		if(o == this || selection.contains(o)) return; //Is selectionbox or already in selection
		o.select();
		selection.add(o);
		if(isSelecting) return; //All we have to do if user is dragging selectionbox
		if(hasMultipleSelection()) { 
			stopSelecting(); //show and fit to SelectionBox if performed after dragging
		}
		 
	}
	/**
	 * Remove an object from the selection
	 * @param o
	 */
	public void removeFromSelection(GmmlDrawingObject o) { 
		selection.remove(o); 
		o.deselect();
		if(!isSelecting) fitToSelection();
	}
	
	/**
	 * Removes the object at the given co�rdinates from the selection
	 * (if exists and is selected)
	 * @param p
	 */
	public void selectionClicked(Point2D p) {
		GmmlDrawingObject clicked = null;
		for(GmmlDrawingObject o : selection) {
			if(o.isContain(p)) clicked = o;
		}
		if(clicked.isSelected()) //Object is selected, remove
		{
			removeFromSelection(clicked);
		} 
		else //Object is not selected, add
		{
			addToSelection(clicked);
		}
	}
	
	/**
	 * Returns true if the selectionbox has multiple objects in its selection, false otherwise
	 * @return
	 */
	public boolean hasMultipleSelection() { return selection.size() > 1 ? true : false; }
	
	/**
	 * Resets the selectionbox (unselect selected objects, clear selection, reset rectangle
	 * to upperleft corner
	 */
	public void reset() { 
		reset(0, 0);
	}
	
	/**
	 * Resets the selectionbox (unselect selected objects, clear selection, reset rectangle
	 * to specified start co�rdinates
	 */
	public void reset(double startX, double startY) {
		markDirty();
		for(GmmlDrawingObject o : selection) o.deselect();
		selection.clear();
		
		gdata.setLeft(startX);
		gdata.setTop(startY);
		gdata.setWidth(0);
		gdata.setHeight(0);
		setHandleLocation();
	}

	/**
	 * Returns true if this selectionbox is in selecting state (selects containing objects when resized)
	 * @return
	 */
	public boolean isSelecting() { return isSelecting; }
	
	/**
	 * Start selecting
	 */
	public void startSelecting() {
		isSelecting = true;
		setHandleRestriction(false);
		show();
	}
	
	/**
	 * Stop selecting
	 */
	public void stopSelecting() {
		isSelecting = false;
		if(!hasMultipleSelection()) {
			if(selection.size() == 1) {
				GmmlDrawingObject passTo = selection.get(0);
				reset();
				passTo.select();
			} else {
				reset();
			}
		} else {
			select();
			fitToSelection();
			setHandleRestriction(true);
		}
	}
	
	/**
	 * Sets movement direction restriction for this object's handles
	 * @param restrict if true, handle movement is restricted in XY direction,
	 * else handles can move freely
	 */
	private void setHandleRestriction(boolean restrict) {
		if(restrict) {
			handleNE.setDirection(GmmlHandle.DIRECTION_MINXY);
			handleSW.setDirection(GmmlHandle.DIRECTION_MINXY);
			handleNW.setDirection(GmmlHandle.DIRECTION_XY);
			handleSE.setDirection(GmmlHandle.DIRECTION_XY);
		} else {
			for(GmmlHandle h : getHandles()) 
				h.setDirection(GmmlHandle.DIRECTION_FREE); 
		}
	}
	
	public void select() {
		super.select();
		for(GmmlDrawingObject o : selection) {
			o.select();
			for(GmmlHandle h : o.getHandles()) h.hide();
		}
	}
		
	/**
	 * Fit the size of this object to the selected objects
	 */
	public void fitToSelection() {
		if(selection.size() == 0) return; //No objects in selection
		if(! hasMultipleSelection()) { //Only one object in selection, hide selectionbox
			GmmlDrawingObject passTo = selection.get(0);
			reset();
			passTo.select();
			return;
		}
		markDirty();
		Rectangle r = null;
		for(GmmlDrawingObject o : selection) {
			if(r == null) r = o.getBounds();
			else r.add(o.getBounds());
			for(GmmlHandle h : o.getHandles()) h.hide();
		}
		gdata.setLeft(r.x);
		gdata.setTop(r.y);
		gdata.setWidth(r.width);
		gdata.setHeight(r.height);
		setHandleLocation();
		markDirty();
	}
			
	/**
	 * Show the selectionbox
	 */
	public void show() { 
		isVisible = true; 
		markDirty();
	}
	
	/**
	 * Hide the selectionbox
	 */
	public void hide() { 
		isVisible = false;
		reset();
	}
	
	/**
	 * Gets the corner handle (South east) for start dragging
	 * @return
	 */
	public GmmlHandle getCornerHandle() { return handleSE; }
	
	public void adjustToHandle(GmmlHandle h) {	
		//Store original size and location before adjusting to handle
		double oWidth = gdata.getWidth();
		double oHeight = gdata.getHeight();
		double oCenterX = getCenterX();
		double oCenterY = getCenterY();
		
		super.adjustToHandle(h);
		if(isSelecting) { //Selecting, so add containing objects to selection
			Rectangle r = getBounds();
			Rectangle2D.Double bounds = new Rectangle2D.Double(r.x, r.y, r.width, r.height);
			for(GmmlDrawingObject o : canvas.getDrawingObjects()) {
				if((o == this) || (o instanceof GmmlHandle)) continue;
				if(o.intersects(bounds)) { 
					addToSelection(o);
				} else if(o.isSelected()) removeFromSelection(o);
			}
		} else { //Resizing, so resize child objects too
			//Scale all selected objects in x and y direction			
			for(GmmlDrawingObject o : selection) { 
				Rectangle2D.Double r = o.getScaleRectangle();
				double rw = gdata.getWidth() / oWidth;
				double rh = gdata.getHeight() / oHeight;
				double nwo = r.width * rw;
				double nho = r.height * rh;
				double ncdx = (r.x - oCenterX) * rw;
				double ncdy = (r.y - oCenterY) * rh;
				o.setScaleRectangle(new Rectangle2D.Double(getCenterX() + ncdx, getCenterY() + ncdy, nwo, nho));
			}
		}
	}
	
	public void moveBy(double dx, double dy) {
		super.moveBy(dx, dy);
		//Move the selected objects
		for(GmmlDrawingObject o : selection) {
			o.moveBy(dx, dy);
		}
	}
	
	protected void draw(PaintEvent e, GC buffer)
	{
		if(isVisible) {
			buffer.setAntialias(SWT.OFF);
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.setLineStyle (SWT.LINE_DOT);
			buffer.setLineWidth (1);
			buffer.drawRectangle ((int)gdata.getLeft(), (int)gdata.getTop(), (int)gdata.getWidth(), (int)gdata.getHeight());
			buffer.setAntialias(SWT.ON);
		}
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
}