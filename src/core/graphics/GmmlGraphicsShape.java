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
package graphics;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;

import util.LinAlg;
import util.SwtUtils;
import util.LinAlg.Point;
import data.GmmlDataObject;
import data.GmmlEvent;

/**
 * This is an {@link GmmlGraphics} class representing shapelike forms,
 * and provides implementation for containing 8 handles placed in a 
 * (rotated) rectangle around the shape and a rotation handle
 */
public abstract class GmmlGraphicsShape extends GmmlGraphics {

	private static final double M_ROTATION_HANDLE_POSITION = 20.0 * 15;

	//Side handles
	GmmlHandle handleN;
	GmmlHandle handleE;
	GmmlHandle handleS;
	GmmlHandle handleW;
	//Corner handles
	GmmlHandle handleNE;
	GmmlHandle handleSE;
	GmmlHandle handleSW;
	GmmlHandle handleNW;
	//Rotation handle
	GmmlHandle handleR;
		
	final GmmlHandle[][] handleMatrix; //Used to get opposite handles
	
	public GmmlGraphicsShape(GmmlDrawing canvas, GmmlDataObject o) {
		super(canvas, o);
		
		handleN	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleE	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
		handleS	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleW	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
				
		handleNE = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		handleSE = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		handleSW = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		handleNW = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		
		handleR = new GmmlHandle(GmmlHandle.DIRECTION_ROT, this, canvas);
		
		handleMatrix = new GmmlHandle[][] {
				{ handleNW, 	handleNE },
				{ handleSW, 	handleSE }};
	}
	
	
	/**
	 * Adjust model to changes in the shape, 
	 * and at the same time calculates the new position 
	 * in gpml coordinates (so without zoom factor)
	 */
	private void setVShape(double vleft, double vtop, double vwidth, double vheight) 
	{
//		gdata.dontFireEvents(3);
		gdata.setMWidth(mFromV(vwidth));
		gdata.setMHeight(mFromV(vheight));
		gdata.setMLeft(mFromV(vleft));
		gdata.setMTop(mFromV(vtop));
	}
	
	protected void vMoveBy(double vdx, double vdy)
	{
		gdata.setMLeft(gdata.getMLeft()  + mFromV(vdx));
		gdata.setMTop(gdata.getMTop() + mFromV(vdy));
	}

	public void setVScaleRectangle(Rectangle2D.Double r) {
		setVShape(r.x, r.y, r.width, r.height);
	}
	
	protected Rectangle2D.Double getVScaleRectangle() {
		return new Rectangle2D.Double(getVLeftDouble(), getVTopDouble(), getVWidthDouble(), getVHeightDouble());
	}
	
	public GmmlHandle[] getHandles()
	{
		if( this instanceof GmmlSelectionBox) {
			// Only corner handles
			return new GmmlHandle[] {
					handleNE, handleSE,
					handleSW, handleNW
			};
		}
		if(	this instanceof GmmlGeneProduct || 
			this instanceof GmmlLabel) {
			// No rotation handle for these objects
			return new GmmlHandle[] {
					handleN, handleNE, handleE, handleSE,
					handleS, handleSW, handleW,	handleNW,
			};
		}
		return new GmmlHandle[] {
				handleN, handleNE, handleE, handleSE,
				handleS, handleSW, handleW,	handleNW,
				handleR
		};
	}
	
	/**
	 * Translate the given point to internal coordinate system
	 * (origin in center and axis direction rotated with this objects rotation
	 * @param Point p
	 */
	private Point mToInternal(Point p) {
		Point pt = mRelativeToCenter(p);
		Point pr = LinAlg.rotate(pt, gdata.getRotation());
		return pr;
	}

	/**
	 * Translate the given point to external coordinate system (of the
	 * drawing canvas)
	 * @param Point p
	 */
	private Point mToExternal(Point p) {
		Point pr = LinAlg.rotate(p, -gdata.getRotation());
		Point pt = mRelativeToCanvas(pr);
		return pt;
	}

	/**
	 * Translate the given coordinates to external coordinate system (of the
	 * drawing canvas)
	 * @param x
	 * @param y
	 */
	private Point mToExternal(double x, double y) {
		return mToExternal(new Point(x, y));
	}

	/**
	 * Get the coordinates of the given point relative
	 * to this object's center
	 * @param p
	 */
	private Point mRelativeToCenter(Point p) {
		return p.subtract(getMCenter());
	}

	/**
	 * Get the coordinates of the given point relative
	 * to the canvas' origin
	 * @param p
	 */
	private Point vRelativeToCanvas(Point p) {
		return p.add(getVCenter());
	}

	private Point mRelativeToCanvas(Point p) {
		return p.add(getMCenter());
	}

	/**
	 * Get the center point of this object
	 */
	public Point getVCenter() {
		return new Point(getVCenterX(), getVCenterY());
	}

	/**
	 * Get the center point of this object
	 */
	public Point getMCenter() {
		return new Point(gdata.getMCenterX(), gdata.getMCenterY());
	}

	/**
	 * Set the center point of this object
	 * @param cn
	 */
	public void setMCenter(Point mcn) {
//		gdata.dontFireEvents(1);
		gdata.setMCenterX(mcn.x);
		gdata.setMCenterY(mcn.y);
	}

	public void setVCenter(Point vcn) {
//		gdata.dontFireEvents(1);
		gdata.setMCenterX(mFromV(vcn.x));
		gdata.setMCenterY(mFromV(vcn.y));
	}

	/**
	 * Calculate a new center point given the new width and height, in a
	 * way that the center moves over the rotated axis of this object
	 * @param mWidthNew
	 * @param mHeightNew
	 */
	public Point mCalcNewCenter(double mWidthNew, double mHeightNew) {
		Point mcn = new Point((mWidthNew - gdata.getMWidth())/2, (mHeightNew - gdata.getMHeight())/2);
		Point mcr = LinAlg.rotate(mcn, -gdata.getRotation());
		return mRelativeToCanvas(mcr);
	}

	public Point vCalcNewCenter(double vWidthNew, double vHeightNew) {
		Point vcn = new Point((vWidthNew - getVWidth())/2, (vHeightNew - getVHeight())/2);
		Point vcr = LinAlg.rotate(vcn, -gdata.getRotation());
		return vRelativeToCanvas(vcr);
	}

	/**
	 * Set the rotation of this object
	 * @param angle angle of rotation in radians
	 */
	public void setRotation(double angle) {
		if(angle < 0) gdata.setRotation(angle + Math.PI*2);
		else if(angle > Math.PI*2) gdata.setRotation (angle - Math.PI*2);
		else gdata.setRotation(angle);
	}
	
	/**
	 * Rotates the {@link GC} around the objects center
	 * @param gc	the {@link GC} to rotate
	 * @param tr	a {@link Transform} that can be used for rotation
	 */
	protected void rotateGC(GC gc, Transform tr) {		
		SwtUtils.rotateGC(gc, tr, (float)Math.toDegrees(gdata.getRotation()), 
				getVCenterX(), getVCenterY());
	}
	
	public void adjustToHandle(GmmlHandle h) {
		//Rotation
		if 	(h == handleR) {
			Point def = mRelativeToCenter(getMHandleLocation(h));
			Point cur = mRelativeToCenter(new Point(h.mCenterx, h.mCentery));
			
			setRotation(gdata.getRotation() + LinAlg.angle(def, cur));
			
			return;
		}
					
		// Transformation
		Point mih = mToInternal(new Point(h.mCenterx, h.mCentery));
		
		double mdx = 0;
		double mdy = 0;
		double mdw = 0;
		double mdh = 0;
			
		if	(h == handleN || h == handleNE || h == handleNW) {
			mdy = -(mih.y + gdata.getMHeight()/2);
			mdh = -mdy;
		}
		if	(h == handleS || h == handleSE || h == handleSW ) {
			mdy = mih.y - gdata.getMHeight()/2;
			mdh = mdy;
		}
		if	(h == handleE || h == handleNE || h == handleSE) {
			mdx = mih.x - gdata.getMWidth()/2;
			mdw = mdx;
		}
		if	(h == handleW || h == handleNW || h== handleSW) {
			mdx = -(mih.x + gdata.getMWidth()/2);
			mdw = -mdx;
		};
		
		Point mnc = mCalcNewCenter(gdata.getMWidth() + mdw, gdata.getMHeight() + mdh);
//		gdata.dontFireEvents(1);
		gdata.setMHeight(gdata.getMHeight() + mdy);
		gdata.setMWidth(gdata.getMWidth() + mdx);
		setMCenter(mnc);		
	
		//In case object had zero width, switch handles
		if(gdata.getMWidth() < 0) {
			negativeWidth(h);
		}
		if(gdata.getMHeight() < 0) {
			negativeHeight(h);
		}
	}
	
	/**
	 * This method implements actions performed when the width of
	 * the object becomes negative after adjusting to a handle
	 * @param h	The handle this object adjusted to
	 */
	public void negativeWidth(GmmlHandle h) {
		if(h.getDirection() == GmmlHandle.DIRECTION_FREE)  {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_X);
		} else {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_XY);
		}
		double mw = -gdata.getMWidth();
		double msx = gdata.getMLeft() - mw;
//		gdata.dontFireEvents(1);
		gdata.setMWidth (mw);
		gdata.setMLeft(msx);
		canvas.setPressedObject(h);
	}
	
	/**
	 * This method implements actions performed when the height of
	 * the object becomes negative after adjusting to a handle
	 * @param h	The handle this object adjusted to
	 */
	public void negativeHeight(GmmlHandle h) {
		if(h.getDirection() == GmmlHandle.DIRECTION_FREE)  {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_Y);
		} else {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_XY);
		}
		double ht = -gdata.getMHeight();
		double sy = gdata.getMTop() - ht;
//		gdata.dontFireEvents(1);
		gdata.setMHeight(ht);
		gdata.setMTop(sy);
		canvas.setPressedObject(h);
	}
	
	/**
	 * Sets the handles at the correct location;
	 * @param ignore the position of this handle will not be adjusted
	 */
	private void setHandleLocation(GmmlHandle ignore)
	{
		Point p;
		p = getMHandleLocation(handleN);
		if(ignore != handleN) handleN.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleE);
		if(ignore != handleE) handleE.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleS);
		if(ignore != handleS) handleS.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleW);
		if(ignore != handleW) handleW.setMLocation(p.x, p.y);
		
		p = getMHandleLocation(handleNE);
		if(ignore != handleNE) handleNE.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleSE);
		if(ignore != handleSE) handleSE.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleSW);
		if(ignore != handleSW) handleSW.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleNW);
		if(ignore != handleNW) handleNW.setMLocation(p.x, p.y);

		p = getMHandleLocation(handleR);
		if(ignore != handleR) handleR.setMLocation(p.x, p.y);
		
		for(GmmlHandle h : getHandles()) h.rotation = gdata.getRotation();
	}
	
	/**
	 * Sets the handles at the correct location
	 */
	public void setHandleLocation()
	{
		setHandleLocation(null);
	}
	
	/**
	 * Get the default location of the given handle 
	 * (in coordinates relative to the canvas)
	 * @param h
	 */
	protected Point getVHandleLocation(GmmlHandle h) 
	{
		Point mp = getMHandleLocation (h);
		if (mp != null)			
			return new Point (vFromM(mp.x), vFromM(mp.y));
		else return null;
	}

	protected Point getMHandleLocation(GmmlHandle h) {
		if(h == handleN) return mToExternal(0, -gdata.getMHeight()/2);
		if(h == handleE) return mToExternal(gdata.getMWidth()/2, 0);
		if(h == handleS) return mToExternal(0,  gdata.getMHeight()/2);
		if(h == handleW) return mToExternal(-gdata.getMWidth()/2, 0);
		
		if(h == handleNE) return mToExternal(gdata.getMWidth()/2, -gdata.getMHeight()/2);
		if(h == handleSE) return mToExternal(gdata.getMWidth()/2, gdata.getMHeight()/2);
		if(h == handleSW) return mToExternal(-gdata.getMWidth()/2, gdata.getMHeight()/2);
		if(h == handleNW) return mToExternal(-gdata.getMWidth()/2, -gdata.getMHeight()/2);

		if(h == handleR) return mToExternal(gdata.getMWidth()/2 + M_ROTATION_HANDLE_POSITION, 0);
		return null;
	}
	
	/**
	 * Gets the handle opposite to the given handle.
	 * For directions N, E, S and W this is always their complement,
	 * for directions NE, NW, SE, SW, you can constraint the direction, e.g.:
	 * if direction is X, the opposite of NE will be NW instead of SW
	 * @param h	The handle to find the opposite for
	 * @param direction	Constraints on the direction, one of {@link GmmlHandle}#DIRECTION_*.
	 * Will be ignored for N, E, S and W handles
	 * @return	The opposite handle
	 */
	GmmlHandle getOppositeHandle(GmmlHandle h, int direction) {
		//Ignore direction for N, E, S and W
		if(h == handleN) return handleS;
		if(h == handleE) return handleW;
		if(h == handleS) return handleN;
		if(h == handleW) return handleE;
				
		int[] pos = handleFromMatrix(h);
		switch(direction) {
		case GmmlHandle.DIRECTION_XY:
		case GmmlHandle.DIRECTION_MINXY:
		case GmmlHandle.DIRECTION_FREE:
			return handleMatrix[ Math.abs(pos[0] - 1)][ Math.abs(pos[1] - 1)];
		case GmmlHandle.DIRECTION_Y:
			return handleMatrix[ Math.abs(pos[0] - 1)][pos[1]];
		case GmmlHandle.DIRECTION_X:
			return handleMatrix[ pos[0]][ Math.abs(pos[1] - 1)];
		default:
			return null;
		}
	}
	
	int[] handleFromMatrix(GmmlHandle h) {
		for(int x = 0; x < 2; x++) {
			for(int y = 0; y < 2; y++) {
				if(handleMatrix[x][y] == h) return new int[] {x,y};
			}
		}
		return null;
	}
	
	/**
	 * Creates a shape of the outline of this object
	 */
	protected Shape getVOutline()
	{
		int[] x = new int[4];
		int[] y = new int[4];
		
		int[] p = getVHandleLocation(handleNE).asIntArray();
		x[0] = p[0]; y[0] = p[1];
		p = getVHandleLocation(handleSE).asIntArray();
		x[1] = p[0]; y[1] = p[1];
		p = getVHandleLocation(handleSW).asIntArray();
		x[2] = p[0]; y[2] = p[1];
		p = getVHandleLocation(handleNW).asIntArray();
		x[3] = p[0]; y[3] = p[1];
		
		Polygon pol = new Polygon(x, y, 4);
		return pol;
	}
			
	public void gmmlObjectModified(GmmlEvent e) {		
		markDirty(); // mark everything dirty
		setHandleLocation();
	}
	
}
