package data;

import java.util.Arrays;
import java.util.List;

public class OrientationType {

	public static final int TOP		= 0;
	public static final int RIGHT	= 1;
	public static final int BOTTOM	= 2;
	public static final int LEFT	= 3;

	// Some mappings to Gmml
	private static final List orientationMappings = Arrays.asList(new String[] {
			"top", "right", "bottom", "left"
	});

	public static int getMapping(String value)
	{
		return orientationMappings.indexOf(value);
	}
	
	public static String getMapping(int value)
	{
		return (String)orientationMappings.get(value);
	}

}
