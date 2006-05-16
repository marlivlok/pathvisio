package colorSet;
import graphics.GmmlGeneProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

public class GmmlColorSet {
	public RGB color_no_criteria_met = new RGB(200, 200, 200);
	public RGB color_gene_not_found = new RGB(255, 255, 255);
	
	public String name;
	
	public Vector colorSetObjects;
	
	public GmmlColorSet(String name)
	{
		this.name = name;
		colorSetObjects = new Vector();
	}
	
	public Vector getColorSetObjects()
	{
		return colorSetObjects;
	}
	
	public void addObject(GmmlColorSetObject o)
	{
		colorSetObjects.add(o);
	}
	
	public Object getParent()
	{
		return null;
	}
	
	public RGB getColor(HashMap data)
	{
		RGB rgb = color_no_criteria_met;
		Iterator it = colorSetObjects.iterator();
		while(it.hasNext())
		{
			GmmlColorSetObject gc = (GmmlColorSetObject)it.next();
			RGB gcRgb = gc.getColor(data);
			if(gcRgb != null)
			{
				return gcRgb;
			}
		}
		return rgb;
	}
}
