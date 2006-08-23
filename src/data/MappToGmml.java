// $Id: MappToGmml.java,v 1.5 2005/10/21 12:33:27 gontran Exp $
package data;

import java.util.*;
import org.jdom.*;
import debug.Logger;

// don't need this for the commandline version!
//import javax.swing.*;


public class MappToGmml
{	
	public static Logger log;

	// These constants define columns in the info table.
	static final int icolTitle = 0;
	static final int icolMAPP = 1;
	static final int icolGeneDB = 2;
	static final int icolGeneDBVersion = 3;
	static final int icolVersion = 4;
	static final int icolAuthor = 5;
	static final int icolMaint = 6;
	static final int icolEmail = 7;
	static final int icolCopyright = 8;
	static final int icolModify = 9;
	static final int icolRemarks = 10;
	static final int icolBoardWidth = 11;
	static final int icolBoardHeight = 12;
	static final int icolWindowWidth = 13;
	static final int icolWindowHeight = 14;
	static final int icolNotes = 15;

    String[] headers = {"Title", "MAPP", "GeneDB", "GeneDBVersion", "Version", "Author",
            "Maint", "Email", "Copyright",
            "Modify", "Remarks", "BoardWidth", "BoardHeight",
            "WindowWidth", "WindowHeight", "Notes"};

	public static String[][] uncopyMappInfo (GmmlData data)
	{
		String[][] mappInfo = new String[2][16];
		
		GmmlDataObject mi = null;
		for (GmmlDataObject o : data.dataObjects)
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
				mi = o;
		}
			
		mappInfo[1][icolTitle] = mi.getMapInfoName();
		mappInfo[1][icolVersion] = mi.getVersion();
		mappInfo[1][icolAuthor] = mi.getAuthor();
		mappInfo[1][icolMaint] = mi.getMaintainedBy();
		mappInfo[1][icolEmail] = mi.getEmail();
		mappInfo[1][icolCopyright] = mi.getAuthor();
		mappInfo[1][icolModify] = mi.getLastModified();
		
		mappInfo[1][icolNotes] = mi.getNotes();
		mappInfo[1][icolRemarks] = mi.getComment();		
		
		mappInfo[1][icolBoardWidth] = "" + mi.getBoardWidth();
		mappInfo[1][icolBoardHeight] = "" + mi.getBoardHeight();
		mappInfo[1][icolWindowWidth] = "" + mi.getWindowWidth();
		mappInfo[1][icolWindowHeight] = "" + mi.getWindowHeight();
		
		return mappInfo;
	}
	
	// This method copies the Info table of the genmapp mapp to a new gmml
	// pathway
	public static void copyMappInfo( String[][] mappInfo, GmmlData data)
	{

		/* Data is lost when converting from GenMAPP to GMML:
		*
		* GenMAPP: 
		*		"Title", "MAPP", "Version", "Author","GeneDBVersion",
		* 		"Maint", "Email", "Copyright","Modify", 
		*		"Remarks", "BoardWidth", "BoardHeight","WindowWidth",
		*		"WindowHeight", "GeneDB", "Notes"
		* GMML:    
		*		"Name", NONE, Version, "Author", NONE, 
		*		"MaintainedBy", "Email", "Availability", "LastModified",
		*		"Comment", "BoardWidth", "BoardHeight", NONE, 
		*		NONE, NONE, "Notes"
		*
		*/
	
		log.trace ("CONVERTING INFO TABLE TO GMML");
	
		/*
		*
    	* from: fileInOut.MappFile.importMAPPInfo(String filename)
		* below mappInfo array is indexed in the following order -- NOT
		* like in the SQL order from the Info table.  *sigh*
		*
		* 0 "Title", 1 "MAPP", 2 "Version", 3 "Author", 4 "GeneDBVersion",
		* 5 "Maint", 6 "Email", 7 "Copyright", 8 "Modify", 9 "Remarks",
		* 10 "BoardWidth", 11 "BoardHeight", 12 "WindowWidth",
		* 13 "WindowHeight", 14 "GeneDB", 15 "Notes"
		*
		*/

		GmmlDataObject o = new GmmlDataObject();
		o.setObjectType(ObjectType.MAPPINFO);
		o.setMapInfoName(mappInfo[1][icolTitle]);
		o.setMapInfoDataSource("GenMAPP 2.0");
		o.setVersion(mappInfo[1][icolVersion]);
		o.setAuthor(mappInfo[1][icolAuthor]);
		o.setMaintainedBy(mappInfo[1][icolMaint]);
		o.setEmail(mappInfo[1][icolEmail]);
		o.setAvailability(mappInfo[1][icolCopyright]);
		o.setLastModified(mappInfo[1][icolModify]);
		
		o.setNotes(mappInfo[1][icolNotes]);
		o.setComment(mappInfo[1][icolRemarks]);		

		o.setBoardWidth(Double.parseDouble(mappInfo[1][icolBoardWidth]));
		o.setBoardHeight(Double.parseDouble(mappInfo[1][icolBoardHeight]));
		o.setWindowWidth(Double.parseDouble(mappInfo[1][icolWindowWidth]));
		o.setWindowHeight(Double.parseDouble(mappInfo[1][icolWindowHeight]));
	}
    
	// these constants define the columns in the Objects table.
	static final int colObjKey = 0;
	static final int colID = 1;
	static final int colSystemCode = 2;
	static final int colType = 3;
	static final int colCenterX = 4;
	static final int colCenterY = 5;
	static final int colSecondX = 6;
	static final int colSecondY = 7;
	static final int colWidth = 8;
	static final int colHeight = 9;
	static final int colRotation = 10;
	static final int colColor = 11;
	static final int colLabel = 12;
	static final int colHead = 13;
	static final int colRemarks = 14;
	static final int colImage = 15;
	static final int colLinks = 16;
	static final int colNotes = 17;
    
	private static String mapBetween (String[] from, String[] to, String value) throws ConverterException
    {
    	for(int i=0; i < from.length; i++) 
		{
		    if(from[i].equals(value)) 
		    {
		    	return to[i];
		    }		    
		    else if (i == from.length-1) 
		    {
		    	throw new ConverterException ("'" + value + "' is invalid\n");
		    }
		}
    	return null;
    }

	public static List uncopyMappObjects(GmmlData data) throws ConverterException
	{
		List result = new ArrayList();
		
		for (GmmlDataObject o : data.dataObjects)
		{
			int objectType = o.getObjectType();
			String[] row = new String[18];
			
			// init:
			row[colCenterX] = "0.0";
			row[colCenterY] = "0.0";
			row[colSecondX] = "0.0";
			row[colSecondY] = "0.0";
			row[colWidth] = "0.0";
			row[colHeight] = "0.0";
			row[colRotation] = "0.0";
			row[colColor] = "-1";
			
			switch (objectType)
			{
				case ObjectType.LINE:
					unmapNotesAndComments (o, row);
					unmapLineType(o, row);
					break;
				case ObjectType.BRACE:
					unmapNotesAndComments (o, row);
					unmapBraceType(o, row);
					break;
				case ObjectType.GENEPRODUCT:	
					unmapNotesAndComments (o, row);
					unmapGeneProductType(o, row);
					break;
				case ObjectType.INFOBOX:
					unmapInfoBoxType(o, row);
					break;
				case ObjectType.LABEL:
					unmapNotesAndComments (o, row);
					unmapLabelType(o, row);
					break;
				case ObjectType.LEGEND:
					unmapLegendType(o, row);
					break;
				case ObjectType.SHAPE:			
					unmapNotesAndComments (o, row);
					unmapShapeType(o, row);
					break;
				case ObjectType.FIXEDSHAPE:					
					unmapNotesAndComments (o, row);
					unmapFixedShapeType(o, row);
					break;
				case ObjectType.COMPLEXSHAPE:			
					unmapNotesAndComments (o, row);
					unmapComplexShapeType(o, row);
					break;
			}
			result.add(row);
		}
				
		return result;
	}

	private static void unmapNotesAndComments(GmmlDataObject o, String[] row)
	{
		row[colNotes] = o.getNotes();
		row[colRemarks] = o.getComment();
	}
	
	private static void mapNotesAndComments(GmmlDataObject o, String[] row)
	{
        if (row[colNotes] != null &&
        		!row[colNotes].equals(""))
        {        	
        	o.setNotes(row[colNotes]);
        }

        if (row[colRemarks] != null &&
        		!row[colRemarks].equals(""))
        {            
            o.setComment(row[colRemarks]);
        }
	}

	// This list adds the elements from the OBJECTS table to the new gmml
	// pathway
    public static void copyMappObjects(String[][] mappObjects, GmmlData data) throws ConverterException
    {
    	List elementList = new ArrayList();
    	
        log.trace ("CONVERTING OBJECTS TABLE TO GMML");

		// Create the GenMAPP --> GMML mappings list for use in the switch
		// statement

		List typeslist = Arrays.asList(new String[] { 
				"Arrow", "DottedArrow", "DottedLine", "Line",
				"Brace", "Gene", "InfoBox", "Label", "Legend", "Oval",
				"Rectangle", "TBar", "Receptor", "LigandSq",  "ReceptorSq",
				"LigandRd", "ReceptorRd", "CellA", "Arc", "Ribosome",
				"OrganA", "OrganB", "OrganC", "ProteinB", "Poly", "Vesicle"
		});

		/*index 0 are heades*//*last row is always null*/

		for(int i=1; i<mappObjects.length-1; i++)
		{
			GmmlDataObject o = null;
			
			int index = typeslist.indexOf(mappObjects[i][colType]);
			
			switch(index) {
			
					case 0: /*Arrow*/							
					case 1: /*DottedArrow*/							
					case 2: /*DottedLine"*/							
					case 3: /*Line*/
					case 11: /*TBar*/
					case 12: /*Receptor*/           
					case 13: /*LigandSq*/           
					case 14: /*ReceptorSq*/         
					case 15: /*LigandRd*/
					case 16: /*ReceptorRd*/
							o = mapLineType(mappObjects[i]);							
							break;							
					case 4: /*Brace*/
							o = mapBraceType(mappObjects[i]);							
							break;							
					case 5: /*Gene*/
							o = mapGeneProductType(mappObjects[i]);
							break;																					
					case 6: /*InfoBox*/
							o = mapInfoBoxType (mappObjects[i]);
							break;
					case 7: /*Label*/
							o = mapLabelType(mappObjects[i]);
							break;
					case 8: /*Legend*/
							o = mapLegendType(mappObjects[i]);
							break;							
					case 9: /*Oval*/						
					case 10: /*Rectangle*/
					case 18: /*Arc*/
							o = mapShapeType( mappObjects[i]);
							break;							
					case 17: /*CellA*/
					case 19: /*Ribosome*/							
					case 20: /*OrganA*/							
					case 21: /*OrganB*/							
					case 22: /*OrganC*/
							o = mapFixedShapeType(mappObjects[i]);							
							break;							
					case 23: /*ProteinB*/
					case 24: /*Poly*/
					case 25: /*Vesicle*/
							o = mapComplexShapeType(mappObjects[i]);							
							break;
					default: 
							throw new ConverterException (
								"-> Type '" 
								+ mappObjects[i][colType]
								+ "' is not recognised as a GenMAPP type "
								+ "and is therefore not processed.\n");							
			}
			
			data.dataObjects.add(o);
		}		
    }

    
    public static void unmapLineType (GmmlDataObject o, String[] mappObject)
    {    	
    	final String[] genmappLineTypes = {
    		"DottedLine", "DottedArrow", "Line", "Arrow", "TBar", "Receptor", "LigandSq", 
    		"ReceptorSq", "LigandRd", "ReceptorRd"};
    	
    	int lineStyle = o.getLineStyle();
		int lineType = o.getLineType();
		String style = genmappLineTypes[lineType];
		if (lineStyle == LineStyle.DASHED && (lineType == LineType.ARROW || lineType == LineType.LINE))
			style = "DOTTED" + style;
		
		mappObject[colType] = style;		
		mappObject[colCenterX] = "" + o.getStartX();
    	mappObject[colCenterY] = "" + o.getStartY();
    	mappObject[colSecondX] = "" + o.getEndX();
    	mappObject[colSecondY] = "" + o.getEndY();
    	unmapColor (o, mappObject);    	
    }

	public static void mapColor(GmmlDataObject o, String[] mappObject)
	{
        o.setColor(ConvertType.fromMappColor(mappObject[colColor]));	
	}

	public static void unmapColor(GmmlDataObject o, String[] mappObject)
	{
		mappObject[colColor] = ConvertType.toMappColor(o.getColor());	
	}

	public static GmmlDataObject mapLineType(String [] mappObject) throws ConverterException
	{
		final List gmmlLineTypes = Arrays.asList(new String[] {
				"Line", "Arrow", "Tbar", "Receptor", "LigandSquare", 
				"ReceptorSquare", "LigandRound", "ReceptorRound"});
		
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.LINE);
    	
		String type = mappObject[colType];
    	int lineStyle = LineStyle.SOLID;		
    	int lineType = gmmlLineTypes.indexOf(type);
    	if(type.equals("DottedLine") || type.equals("DottedArrow"))
    	{
			lineStyle = LineStyle.DASHED;
    	}
    	else
    	{
    		lineType -= 2;
    	}
    	o.setLineStyle(lineStyle);
    	o.setLineType(lineType);
		
        o.setStartX(Double.parseDouble(mappObject[colCenterX]));       
        o.setStartY(Double.parseDouble(mappObject[colCenterY]));
        o.setEndX(Double.parseDouble(mappObject[colSecondX]));
        o.setEndY(Double.parseDouble(mappObject[colSecondY]));		
        
        return o;
	}
    
	private static void unmapCenter (GmmlDataObject o, String[] mappObject)
	{
		mappObject[colCenterX] = "" + o.getCenterY();
    	mappObject[colCenterY] = "" + o.getCenterX();	
	}
	
	private static void mapCenter (GmmlDataObject o, String[] mappObject)
	{
		o.setCenterX(Double.parseDouble(mappObject[colCenterX]));
		o.setCenterY(Double.parseDouble(mappObject[colCenterY]));
	}

	private static void unmapRotation (GmmlDataObject o, String[] mappObject)
	{
		mappObject[colRotation] = "" + o.getRotation();
	}
	
	private static void mapRotation (GmmlDataObject o, String[] mappObject)
	{
		o.setRotation(Double.parseDouble(mappObject[colRotation]));
	}
	
	private static void unmapShape (GmmlDataObject o, String[] mappObject)
	{
    	unmapCenter(o, mappObject);    	
    	mappObject[colWidth] = "" + o.getWidth();
    	mappObject[colHeight] = "" + o.getHeight();	
	}

	private static void mapShape (GmmlDataObject o, String[] mappObject)
	{
    	mapCenter(o, mappObject);    	
    	o.setWidth(Double.parseDouble(mappObject[colWidth]));
    	o.setHeight(Double.parseDouble(mappObject[colHeight]));	
	}

	public static void unmapBraceType (GmmlDataObject o, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Brace";    	
    	mappObject[colRotation] = "" + o.getOrientation();    	
    	unmapShape (o, mappObject);
    	unmapColor (o, mappObject);
    }

    public static GmmlDataObject mapBraceType(String[] mappObject) throws ConverterException
    {
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.BRACE);
    	
    	mapShape(o, mappObject);
    	mapColor(o, mappObject);
    	o.setOrientation(Integer.parseInt(mappObject[colRotation]));
        return o;          
    }
    
    public static void unmapGeneProductType (GmmlDataObject o, String[] mappObject) throws ConverterException
    {    	
    	mappObject[colType] = "Gene";
    	mappObject[colSystemCode] =
			mapBetween (dataSources, systemCodes, 
					o.getDataSource());

		mappObject[colHead] = o.getBackpageHead();
		mappObject[colID] = o.getGeneProductName();
		mappObject[colLabel] = o.getGeneID();
		mappObject[colLinks] = o.getXref();    	
		unmapShape(o, mappObject);
    }
    
    final static String[] systemCodes = 
		{ 
		"D", "F", "G", "I", "L", "M",
		"Q", "R", "S", "T", "U",
		"W", "Z", "X", "O", ""
		};
	
	final static String[] dataSources = 
		{
		"SGD", "FlyBase", "GenBank", "InterPro" ,"LocusLink", "MGI",
		"RefSeq", "RGD", "SwissProt", "GeneOntology", "UniGene",
		"WormBase", "ZFIN", "Affy", "Other", ""
		};

    public static GmmlDataObject mapGeneProductType(String[] mappObject) throws ConverterException
	{
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.GENEPRODUCT);
    	
        o.setDataSource(mapBetween (
				systemCodes, dataSources, mappObject[colSystemCode].trim()));  
        
        o.setBackpageHead(mappObject[colHead]);
        o.setGeneProductName(mappObject[colID]);
        o.setGeneID(mappObject[colLabel]);

        // TODO:  for some IDs the type is known, e.g. SwissProt is always a
		// protein, incorporate this knowledge to assign a type per ID
        o.setGeneProductType("unknown");
        o.setXref(mappObject[colLinks]);
        
        mapShape(o, mappObject);
        return o;			
	}
    
	public static GmmlDataObject mapInfoBoxType (String[] mappObject)
	{
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.INFOBOX);
        
    	mapCenter (o, mappObject);                
        return o;
	}
	
	public static void unmapInfoBoxType (GmmlDataObject o, String[] mappObject)
    {    	
    	mappObject[colType] = "InfoBox";
    	
    	unmapCenter (o, mappObject);
    }

	public static GmmlDataObject mapLegendType (String[] mappObject)
	{
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.LEGEND);
 
    	mapCenter (o, mappObject);
    	        
        return o;
	}
	
	public static void unmapLegendType (GmmlDataObject o, String[] mappObject)
    {    	
    	mappObject[colType] = "Legend";
    	
    	unmapCenter (o, mappObject);    	
    }

	final static int styleBold = 1; 
    final static int styleItalic = 2;
    final static int styleUnderline = 4;
    final static int styleStrikethru = 8;
    
    public static GmmlDataObject mapLabelType(String[] mappObject) 
    {
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.LABEL);

    	mapShape(o, mappObject);
    	mapColor(o, mappObject);
        
    	o.setLabelText(mappObject[colLabel]);
        
        o.setFontName(mappObject[colID]);
        
        o.setFontSize(Double.parseDouble(mappObject[colSecondX]));
        
        String styleString = mappObject[colSystemCode]; 
        int style = styleString == null ? 0 : (int)(styleString.charAt(0));
            
        o.setBold((style & styleBold) > 0);
        o.setItalic((style & styleItalic) > 0);
        o.setUnderline((style & styleUnderline) > 0);
        o.setStrikethru((style & styleStrikethru) > 0);
        
        return o;
    }

    public static void unmapLabelType (GmmlDataObject o, String[] mappObject)
    {    	
    	mappObject[colType] = "Label";
    	mappObject[colLabel] = o.getLabelText();
    	
    	unmapShapeType(o, mappObject);
    	unmapColor(o, mappObject);
    	
    	mappObject[colID] = o.getFontName();
    	mappObject[colSecondX] = "" + o.getFontSize();
    	
    	int style = 16; 
    	// note: from VB source I learned that 16 is added to prevent field from becoming 0, 
    	// as this can't be stored in a text field in the database
    	if (o.isBold()) style |= styleBold;   	
    	if (o.isItalic()) style |= styleItalic;    	
    	if (o.isUnderline()) style |= styleUnderline;    	
    	if (o.isStrikethru()) style |= styleStrikethru;
    	
    	char stylechars[] = new char[1];
    	stylechars[0] = (char)style;
    	
    	mappObject[colSystemCode] = new String (stylechars);    	
    }
    
	public static GmmlDataObject mapShapeType(String[] mappObject)
    {
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.SHAPE);
    	o.setShapeType(ShapeType.getMapping(mappObject[colType]));        
        mapShape (o, mappObject);
        mapColor (o, mappObject);
        mapRotation (o, mappObject);        
        return o;
    }
    
    public static void unmapShapeType (GmmlDataObject o, String[] mappObject)
    {    	
    	int shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.getMapping(shapeType);    	
    	unmapShape (o, mappObject);
    	unmapColor (o, mappObject);
    	unmapRotation (o, mappObject);    	
    }
    
    public static GmmlDataObject mapFixedShapeType( String[] mappObject)
    {
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.FIXEDSHAPE);
        o.setShapeType(ShapeType.getMapping(mappObject[colType]));
        mapCenter (o, mappObject);
        return o;        
    }

    public static void unmapFixedShapeType (GmmlDataObject o, String[] mappObject)
    {    	
    	int shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.getMapping(shapeType);
    	
    	if (shapeType == ShapeType.CELLA)
    	{
    		mappObject[colRotation] = "-1.308997";
    		mappObject[colColor] = "0";
    		mappObject[colWidth] = "1500";
    		mappObject[colHeight] = "375";
    	}    	
    	unmapCenter (o, mappObject);
    }
        
    
    public static GmmlDataObject mapComplexShapeType(String[] mappObject) throws ConverterException 
	{       		
    	GmmlDataObject o = new GmmlDataObject();
    	o.setObjectType(ObjectType.COMPLEXSHAPE);
    	o.setShapeType(ShapeType.getMapping(mappObject[colType]));
        
    	/*
    	 //TODO
    	 
        if (mappObject[colType].equals("Poly"))
        {
        	switch ((int)Double.parseDouble(mappObject[colSecondY]))
        	{
        	case 3: type = "Triangle"; break;
        	case 5: type = "Pentagon"; break;
        	case 6: type = "Hexagon"; break;
        	default: throw
        		new ConverterException ("Found polygon with unexpectec edge count: " + 
        				mappObject[colSecondY]); 
        	}
        }
        else if (mappObject[colType].equals("Vesicle"))
        {
        	type = "Vesicle";        
        }
        else if (mappObject[colType].equals("ProteinB"))
        {
        	type = "ProteinComplex";
        }
        else
        {
        	throw new ConverterException (
        			"Unexpected ComplexShape type: " + mappObject[colType]);
        }
        */
        
        o.setWidth(Double.parseDouble(mappObject[colWidth]));
        mapCenter (o, mappObject);
        mapRotation (o, mappObject);
        return o;
    }
    
    public static void unmapComplexShapeType (GmmlDataObject o, String[] mappObject)
    {   
    	int shapeType = o.getShapeType();
    	mappObject[colType] = ShapeType.getMapping(shapeType);
/*
 		//TODO
    	String type = e.getAttributeValue("Type");
    	if (type.equals("Triangle"))
    	{
    		mappObject[colType] = "Poly";
    		mappObject[colSecondY] = "3";
    	} else if (type.equals("Pentagon"))
    	{
    		mappObject[colType] = "Poly";
    		mappObject[colSecondY] = "5";    		
		} else if (type.equals("Hexagon"))
		{
			mappObject[colType] = "Poly";
			mappObject[colSecondY] = "6";  			
		} else if (type.equals("ProteinComplex"))
		{
			mappObject[colType] = "ProteinB";
		} else if (type.equals("Vesicle"))
		{
			mappObject[colType] = "Vesicle";
		}
    	*/
    	
    	unmapCenter (o, mappObject);
        unmapRotation (o, mappObject);
    	mappObject[colWidth] = "" + o.getWidth();
    }
    
}
