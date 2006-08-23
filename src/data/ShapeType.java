package data;

import java.util.Arrays;
import java.util.List;

public class ShapeType 
{
	public static final int ARC = 1;
	public static final int OVAL = 2;
	public static final int RECTANGLE = 3;
	public static final int CELLA = 4;
	public static final int RIBOSOME = 5;
	public static final int ORGANA = 6;
	public static final int ORGANC = 7;
	public static final int PROTEINB = 8;
	public static final int POLY = 9;
	public static final int VESICLE = 10;
	
	
	public static final List typeMappings = Arrays.asList(new String[] {
			"Rectangle","Oval","Arc",
			"CellA", "Ribosome",
			"OrganA", "OrganB", "OrganC", "ProteinB", "Poly", "Vesicle"
	});
		
	public static int getMapping(String value)
	{
		return typeMappings.indexOf(value);
	}
	
	public static String getMapping(int value)
	{
		return (String)typeMappings.get(value);
	}

}

