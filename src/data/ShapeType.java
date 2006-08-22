package data;

import java.util.Arrays;
import java.util.List;

public class ShapeType 
{
	public static final int ARC = 1;
	public static final int OVAL = 2;
	public static final int RECTANGLE = 3;
	
	public static final List typeMappings = Arrays.asList(new String[] {
			"Rectangle","Oval","Arc"
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

