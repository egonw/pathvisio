// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MPoint;

public class Group extends Graphics implements LinkProvider
{

	public Group(VPathway canvas, PathwayElement pe)
	{
		super(canvas, pe);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generates current id-ref pairs from all current groups
	 * 
	 * @return HashMap<String, String>
	 */
	protected HashMap<String, String> getIdRefPairs()
	{
		// idRefPairs<id, ref>
		HashMap<String, String> idRefPairs = new HashMap<String, String>();

		// Populate hash map of id-ref pairs for all groups
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && vpe instanceof Group)
			{
				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
				if (pe.getGroupRef() != null)
				{
					idRefPairs.put(pe.getGroupId(), pe.getGroupRef());
				}
			}
		}

		return idRefPairs;
	}

	/**
	 * Generates list of group references nested under this group
	 * 
	 * @return ArrayList<String>
	 */
	protected ArrayList<String> getRefList()
	{
		HashMap<String, String> idRefPairs = this.getIdRefPairs();
		ArrayList<String> refList = new ArrayList<String>();
		String thisId = this.getPathwayElement().getGroupId();
		refList.add(thisId);
		boolean hit = true;

		while (hit)
		{
			hit = false;
			// search for hits in hash map; add to refList
			for (String id : idRefPairs.keySet())
			{
				if (refList.contains(idRefPairs.get(id)))
				{
					refList.add(id);
					hit = true;
				}
			}
			// remove hits from hash map
			for (int i = 0; i < refList.size(); i++)
			{
				idRefPairs.remove(refList.get(i));
			}
		}
		return refList;
	}

//	/**
//	 * Determines whether any member of the highest-level group object related
//	 * to the current group object contains the point specified
//	 * 
//	 * @param point -
//	 *            the point to check
//	 * @return True if the object contains the point, false otherwise
//	 */
//	protected boolean vContains(Point2D point)
//	{
//		ArrayList<String> refList = this.getRefList();
//
//		// return true if group object is referenced by selection
//		for (VPathwayElement vpe : canvas.getDrawingObjects())
//		{
//			if (vpe instanceof Graphics && !(vpe instanceof Group)
//					&& vpe.vContains(point))
//			{
//				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
//				String ref = pe.getGroupRef();
//				// System.out.println("pe: " + pe + " ref: " + ref + " refList:
//				// "
//				// + refList.toString());
//				if (ref != null && refList.contains(ref))
//				{
//					// System.out.println(ref + " contains point");
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	/**
	 * Determines whether the area defined by the grouped elements
	 * contains the point specified. The elements themselves are 
	 * excluded to support individual selection within a group. The
	 * ultimate effect is then selection of group by clicking the area
	 * and not the members of the group.
	 * 
	 * @param point -
	 *            the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected boolean vContains(Point2D point)
	{
		// return false if point falls on any individual element
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && !(vpe instanceof Group)
					&& vpe.vContains(point))
			{
				return false;
			
			}
		}
		// return true if point within bounds of grouped objects
		if (this.getVShape(true).contains(point))
		{
			return true;
		}
		else 
		{
			return false;
		}
	}

//	@Override
//	protected boolean vIntersects(Rectangle2D r)
//	{ // always return false. Groups are not selected by drag selection.
//		ArrayList<String> refList = this.getRefList();
//
//		// return true if group object is referenced by selection
//		for (VPathwayElement vpe : canvas.getDrawingObjects())
//		{
//			if (vpe instanceof Graphics && !(vpe instanceof Group)
//					&& vpe.vIntersects(r))
//			{
//				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
//				String ref = pe.getGroupRef();
//				if (ref != null && refList.contains(ref))
//				{
//					// System.out.println(ref + " intersects point");
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	/**
	 * Returns graphics for members of a group, including nested members
	 * 
	 * @return ArrayList<Graphics>
	 */
	public ArrayList<Graphics> getGroupGraphics()
	{
		ArrayList<Graphics> gg = new ArrayList<Graphics>();
		// return true if group object is referenced by selection
		for (VPathwayElement vpe : canvas.getDrawingObjects())
		{
			if (vpe instanceof Graphics && vpe != this)
			{
				Graphics vpeg = (Graphics) vpe;
				PathwayElement pe = vpeg.getPathwayElement();
				String ref = pe.getGroupRef();
				if (ref != null && ref.equals(getPathwayElement().getGroupId()))
				{
					gg.add(vpeg);
				}
			}
		}
		return gg;
	}

	@Override
	public void select()
	{
		for (Graphics g : getGroupGraphics())
		{
			g.select();
		}
		super.select();
	}

	@Override
	public void deselect() {
		for (Graphics g : getGroupGraphics())
		{
			g.deselect();
		}
		super.deselect();
	}
	
	@Override
	protected void vMoveBy(double dx, double dy)
	{
		for (Graphics g : getGroupGraphics())
		{
			g.vMoveBy(dx, dy);
		}
		//Move graphRefs
		//GraphLink.moveRefsBy(gdata, mFromV(vdx), mFromV(vdy));
		Set<VPoint> toMove = new HashSet<VPoint>();
		for(GraphRefContainer ref : gdata.getReferences()) {
			if(ref instanceof MPoint) {
				toMove.add(canvas.getPoint((MPoint)ref));
			}
		}
		for(VPoint p : toMove) p.vMoveBy(dx, dy);
		
		// update group outline
		markDirty();
	}

	protected void doDraw(Graphics2D g2d)
	{
		if(showLinkAnchors) {
			for(LinkAnchor la : getLinkAnchors()) {
				la.draw((Graphics2D)g2d.create());
			}
		}
		// Draw group outline
		int sw = 1;
		g2d.setColor(Color.GRAY);
		g2d.setStroke(new BasicStroke(sw, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_MITER, 1, new float[] { 4, 2 }, 0));
		Rectangle2D rect = getVBounds();
		g2d.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect
				.getWidth()
				- sw, (int) rect.getHeight() - sw);
		g2d.setColor(new Color(255, 255, 245));
		g2d.fillRect((int) rect.getX()+sw, (int) rect.getY()+sw, (int) rect
				.getWidth()
				- 2*sw, (int) rect.getHeight() - 2*sw);

	}

	public void highlight(Color c) {
		super.highlight(c);
		//Highlight the children
		for(Graphics g : getGroupGraphics()) {
			g.highlight();
		}
	}
	
	protected Shape calculateVOutline() {
		//Include rotation and stroke
		Area a = new Area(getVShape(true));
		//Include link anchors
		if(showLinkAnchors) {
			for(LinkAnchor la : getLinkAnchors()) {
				a.add(new Area(la.getShape()));
			}
		}
		return a;
	}
	
	protected Shape getVShape(boolean rotate)
	{
		Rectangle2D mb = null;
		if(rotate) {
			mb = gdata.getRBounds();
		} else {
			mb = gdata.getMBounds();
		}
		return canvas.vFromM(mb);
	}

	protected void setVScaleRectangle(Rectangle2D r)
	{
		// TODO Auto-generated method stub

	}

	List<LinkAnchor> linkAnchors = new ArrayList<LinkAnchor>();
	
	private static final int MIN_SIZE_LA = 15 * 25;
	private int num_linkanchors_h = -1;
	private int num_linkanchors_v = -1;
	
	public List<LinkAnchor> getLinkAnchors() {
		//Number of link anchors depends on the size of the object
		//If the width/height is large enough, there will be three link anchors per side,
		//Otherwise there will be only one link anchor per side
		int n_h = gdata.getMWidth() >= MIN_SIZE_LA ? 3 : 1;
		int n_v = gdata.getMHeight() >= MIN_SIZE_LA ? 3 : 1;
		if(n_h != num_linkanchors_h || n_v != num_linkanchors_v) {
			createLinkAnchors(n_h, n_v);
		}
		return linkAnchors;
	}
	
	private void createLinkAnchors(int n_h, int n_v) {
		linkAnchors.clear();
		double d_h = 2.0/(n_h + 1);
		for(int i = 1; i <= n_h; i++) {
			linkAnchors.add(new LinkAnchor(canvas, gdata, -1 + i * d_h, -1));
			linkAnchors.add(new LinkAnchor(canvas, gdata, -1 + i * d_h, 1));
		}
		double d_v = 2.0/(n_v + 1);
		for(int i = 1; i <= n_v; i++) {
			linkAnchors.add(new LinkAnchor(canvas, gdata, -1, -1 + i * d_v));
			linkAnchors.add(new LinkAnchor(canvas, gdata, 1, -1 + i * d_v));
		}
		num_linkanchors_h = n_h;
		num_linkanchors_v = n_v;
	}
	
	boolean showLinkAnchors = false;
	
	public void showLinkAnchors() {
		if(!showLinkAnchors) {
			showLinkAnchors = true;
			markDirty();
		}
	}
	
	public void hideLinkAnchors() {
		if(showLinkAnchors) {
			showLinkAnchors = false;
			markDirty();
		}
	}
	
	public LinkAnchor getLinkAnchorAt(Point2D p) {
		for(LinkAnchor la : getLinkAnchors()) {
			if(la.getMatchArea().contains(p)) {
				return la;
			}
		}
		return null;
	}
}
