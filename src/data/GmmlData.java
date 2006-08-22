package data;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVisionWindow;
import graphics.GmmlDrawing;

import java.io.*;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.xml.sax.SAXException;


/**
*	This class handles GMML file IO and keeps a JDOM representation of the GMML document
*/
public class GmmlData
{
	/**
	 * factor to convert screen cordinates used in GenMAPP to pixel cordinates
	 * NOTE: maybe it is better to adapt gmml to store cordinates as pixels and
	 * divide the GenMAPP cordinates by this factor on conversion
	 */
	final public static int GMMLZOOM = 15;
	/**
	 * file containing the gmml schema definition
	 */
	final private static File xsdFile = new File("GMML_compat.xsd");
	
	public List<GmmlDataObject> dataObjects = new ArrayList<GmmlDataObject>();
	
	private File xmlFile;
	/**
	 * Gets the xml file containing the Gmml pathway currently displayed
	 * @return
	 */
	public File getXmlFile () { return xmlFile; }
	public void setXmlFile (File file) { xmlFile = file; }

	/**
	 * Contructor for this class, creates a new gmml document
	 * @param drawing {@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
	public GmmlData() 
	{
		GmmlVisionWindow window = GmmlVision.getWindow();
		
		GmmlDataObject mapInfo = new GmmlDataObject();
		mapInfo.setObjectType(ObjectType.MAPPINFO);
		mapInfo.setBoardWidth(window.sc.getSize().x);
		mapInfo.setBoardHeight(window.sc.getSize().y);
		mapInfo.setWindowWidth(window.getShell().getSize().x);
		mapInfo.setWindowHeight(window.getShell().getSize().y);
		mapInfo.setMapInfoName("New Pathway");
	}
		
	/**
	 * Constructor for this class, opens a gmml pathway and adds its elements to the drawing
	 * @param file		String pointing to the gmml file to open
	 * @param drawing	{@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
	public GmmlData(String file) throws Exception
	{
		// Start XML processing
		GmmlVision.log.info("Start reading the Gmml file: " + file);
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback

		xmlFile = new File(file);
		readFromXml(xmlFile);		
	}
	
	/**
	 * Maps the element specified to a GmmlGraphics object
	 * @param e		the JDOM {@link Element} to map
	 */
	private void mapElement(Element e) {
		// Check if a GmmlGraphics exists for this element
		// Assumes that classname = 'Gmml' + Elementname
		GmmlDataObject o = new GmmlDataObject();
		o.mapComplete(e);
		dataObjects.add(o);
	}

	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */
	public static void validateDocument(Document doc) {
		// validate JDOM tree if xsd file exists
		if(xsdFile.canRead()) {
	
			Schema schema;
			try {
				SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				StreamSource ss = new StreamSource(xsdFile);
				schema = factory.newSchema(ss);
				ValidatorHandler vh =  schema.newValidatorHandler();
				SAXOutputter so = new SAXOutputter(vh);
				so.output(doc);
				// If no errors occur, the file is valid according to the gmml xml schema definition
				//TODO: open dialog to report error
				GmmlVision.log.info("Document is valid according to the xml schema definition '" + 
						xsdFile.toString() + "'");
			} catch (SAXException se) {
				GmmlVision.log.error("Could not parse the xml-schema definition", se);
			} catch (JDOMException je) {
				GmmlVision.log.error("Document is invalid according to the xml-schema definition!: " + 
						je.getMessage(), je);
			}
		} else {
			GmmlVision.log.info("Document is not validated because the xml schema definition '" + 
					xsdFile.toString() + "' could not be found");
		}
	}
	
	/**
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
	 */
	public void writeToXML(File file, boolean validate) {
		try 
		{
			Document doc = new Document();
			//Set the root element (pathway) and its graphics
			Element root = new Element("Pathway");
			Element graphics = new Element("Graphics");
			root.addContent(graphics);
			root.addContent(new Element("InfoBox"));
			doc.setRootElement(root);
			
			//TODO... magic happens here
			
			//Validate the JDOM document
			if (validate) validateDocument(doc);
			//Get the XML code
			XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
			//Open a filewriter
			FileWriter writer = new FileWriter(file);
			//Send XML code to the filewriter
			xmlcode.output(doc, writer);
		}
		catch (IOException e) 
		{
			GmmlVision.log.error("Unable to save file " + file + ": " + e.getMessage(), e);
		}
	}
	
	public void readFromXml(File file)
	{
		// Start XML processing
		GmmlVision.log.info("Start reading the XML file: " + file);
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback
		try
		{
			// build JDOM tree
			Document doc = builder.build(file);

			// Copy the pathway information to a GmmlDrawing
			Element root = doc.getRootElement();
			
			mapElement(root); // MappInfo
			
			// Iterate over direct children of the root element
			Iterator it = root.getChildren().iterator();
			while (it.hasNext()) {
				mapElement((Element)it.next());
			}
		}
		catch(JDOMParseException pe) 
		{
			 GmmlVision.log.error(pe.getMessage());
		}
		catch(JDOMException e)
		{
			GmmlVision.log.error(file + " is invalid.");
			GmmlVision.log.error(e.getMessage());
		}
		catch(IOException e)
		{
			GmmlVision.log.error("Could not access " + file);
			GmmlVision.log.error(e.getMessage());
		}
	}
	
	/*
	public void readFromMapp (File file) throws ConverterException
	{
        String inputString = file.getAbsolutePath();

        String[][] mappObjects = MappFile.importMAPPObjects(inputString);
        String[][] mappInfo = MappFile.importMAPPInfo(inputString);

        // Copy the info table to the new gmml pathway
        
        // Copy the objects table to the new gmml pahtway
    	MappToGmml.copyMappInfo(mappInfo, doc);
        MappToGmml.copyMappObjects(mappObjects, doc);        	
	}
	
	public void writeToMapp (File file) throws ConverterException
	{
		String[][] mappInfo = MappToGmml.uncopyMappInfo (doc);
		List mappObjects = MappToGmml.uncopyMappObjects (doc);
		
		MappFile.exportMapp (file.getAbsolutePath(), mappInfo, mappObjects);		
	}
	*/
	
	public final static String[] systemCodes = new String[] 	{ 
		"D", "F", "G", "I", "L", "M",
		"Q", "R", "S", "T", "U",
		"W", "Z", "X", "O"
	};
	
	public final static String[] systemNames = new String[] {
		"SGD", "FlyBase", "GenBank", "InterPro" ,"LocusLink", "MGI",
		"RefSeq", "RGD", "SwissProt", "GeneOntology", "UniGene",
		"WormBase", "ZFIN", "Affy", "Other"
	};
	
	/**
	 * {@link HashMap} containing mappings from system name (as used in Gmml) to system code
	 */
	public static final HashMap<String,String> sysName2Code = initSysName2Code();

	/**
	 * Initializes the {@link HashMap} containing the mappings between system name (as used in gmml)
	 * and system code
	 */
	private static HashMap<String, String> initSysName2Code()
	{
		HashMap<String, String> sn2c = new HashMap<String,String>();
		for(int i = 0; i < systemNames.length; i++)
			sn2c.put(systemNames[i], systemCodes[i]);
		return sn2c;
	}
}
