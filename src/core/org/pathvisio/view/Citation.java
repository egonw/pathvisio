package org.pathvisio.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.pathvisio.biopax.BiopaxEvent;
import org.pathvisio.biopax.BiopaxListener;
import org.pathvisio.biopax.BiopaxReferenceManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.model.PathwayElement;

/**
 * Draws a citation number on top of a pathway object.
 * @author thomas
 */
public class Citation extends VPathwayElement implements BiopaxListener, VElementMouseListener, VPathwayListener {
	static final int MFONT_SIZE = 8 * 15;
	static final String FONT_NAME = "Arial";
	static final Color FONT_COLOR = new Color(0, 0, 128);
	static final int M_PADDING = 3 * 15;
	private Graphics parent;
	private Point2D rPosition;

	/**
	 * @param canvas The parent VPathway
	 * @param parent The Graphics for which the references need to be displayed
	 * @param rPosition The position to place the references, relative to the parent Graphics
	 */
	public Citation(VPathway canvas, Graphics parent, Point2D rPosition) {
		super(canvas);
		this.parent = parent;
		this.rPosition = rPosition;
		getRefMgr().addBiopaxListener(this);
		refresh();
		canvas.addVPathwayListener(this);
		canvas.addVElementMouseListener(this);
	}

	public void vPathwayEvent(VPathwayEvent e) {
		if(e.getType() == VPathwayEvent.ELEMENT_CLICKED_DOWN &&
				e.getAffectedElement() == this) {
				PathwayElementDialog d = PathwayElementDialog.getInstance(
						parent.getPathwayElement(), false
				);
				d.selectPathwayElementPanel(PathwayElementDialog.TAB_LITERATURE);
				d.setVisible(true);
		}
	}
	
	public void vElementMouseEvent(VElementMouseEvent e) {
		if(e.getElement() == parent) {
			if(e.getType() == VElementMouseEvent.TYPE_MOUSEENTER) {
				highlight();
			} else if(e.getType() == VElementMouseEvent.TYPE_MOUSEEXIT) {
				unhighlight();
			}
			canvas.redrawDirtyRect();
		}
	}
	public void setRPosition(Point2D rPosition) {
		this.rPosition = rPosition;
		markDirty();
	}

	public BiopaxReferenceManager getRefMgr() {
		return parent.getPathwayElement().getBiopaxReferenceManager();
	}

	protected Rectangle2D getTextBounds(Graphics2D g) {
		Rectangle2D tb = null;
		Point2D vp = getVPosition();
		double pd = vFromM(M_PADDING);
		String xrefStr = getXRefText();

		if(xrefStr == null || "".equals(xrefStr)) {
			tb = new Rectangle2D.Double(vp.getX(), vp.getY(), 0, 0);
		} else if(g != null) {
			tb = g.getFontMetrics(getVFont()).getStringBounds(getXRefText(), g);
			tb.setRect(
					vp.getX() + tb.getX() - pd, 
					vp.getY() + tb.getY() - pd, 
					tb.getWidth() + 2*pd, 
					tb.getHeight() + 2*pd
			);
		} else { //No graphics context, we can only guess...
			tb = new Rectangle2D.Double(vp.getX() - pd, vp.getY() - pd, 15 + 2*pd, 15 + 2*pd);
		}
		return tb;
	}

	protected Shape calculateVOutline() {
		return getTextBounds(g2d);
	}

	protected int getVFontSize() {
		return (int)vFromM(MFONT_SIZE);
	}

	protected Font getVFont() {
		return new Font(FONT_NAME, Font.PLAIN, getVFontSize());
	}

	protected String getXRefText() {
		String xrefStr = "";
		for(PublicationXRef xref : getRefMgr().getPublicationXRefs()) {
			xrefStr += getRefMgr().getBiopaxElementManager().getOrdinal(xref) + ", ";
		}
		if(xrefStr.length() > 1) {
			xrefStr = xrefStr.substring(0, xrefStr.length() - 2);
		}
		return xrefStr;
	}

	protected Point2D getVPosition() {
		PathwayElement mParent = parent.getPathwayElement();
		Point2D mp = mParent.toAbsoluteCoordinate(rPosition);
		return new Point2D.Double(vFromM(mp.getX()), vFromM(mp.getY()));
	}

	Graphics2D g2d;

	protected void doDraw(Graphics2D g2d) {
		Graphics2D g = (Graphics2D)g2d.create();
		
		if(this.g2d == null) resetShapeCache();
		this.g2d = g;

		String xrefStr = getXRefText();
		if("".equals(xrefStr)) return;

		g.setFont(getVFont());

		Rectangle2D bounds = getTextBounds(g);
		g.setClip(bounds);

		if(isHighlighted()) {
			Color hc = getHighlightColor();
			g.setColor(new Color(hc.getRed(), hc.getGreen(), hc.getBlue(), (int)(255 * 0.5)));
			g.fill(bounds);
		}

		g.setColor(FONT_COLOR);
		int pd = (int)vFromM(M_PADDING);
		g.drawString(xrefStr, (int)bounds.getX() + pd, (int)bounds.getMaxY() - pd);
		
	}

	protected int getZOrder() {
		return parent.getZOrder() + 1;
	}

	protected void refresh() {
		markDirty();
	}

	public void biopaxEvent(BiopaxEvent e) {
		refresh();
	}
}
