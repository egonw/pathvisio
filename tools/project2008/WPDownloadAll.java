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
// import the things needed to run this java file.
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;


public class WPDownloadAll
{
	/**
	 * download all the pathways from a server, using WikiPathWaysClient
	 * 
	 * 
	 * in the String[] args, 1 argument is given:
	 * in example:
	 * "C:\\WPClient"
	 * 
	 * This is the directory of the cache
	 * 	
	 * Good Luck!
	 */
	public static void main(String[] args) throws XmlRpcException, IOException
	{
		// make a new WikiPathwaysClient
		WikiPathwaysClient wp = new WikiPathwaysClient();
		
		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wp.getPathwayList();
		
		
		// path to store the pathway cache
		String path = args[0];
		
		// a for loop that downloads all individual pathways
		for (int i = 0; i < pathwayNames.size(); ++i)
		{

			// get the species and pathwayname
			String pathwayName= pathwayNames.get(i);
			String[] temporary = pathwayName.split(":");
			String species = temporary[0];
			String namePathway = temporary[1];
			
			// construct the download path
			String pathToDownload = path + "\\" + species + "\\";
			
			//	make a folder for a species when it doesn't exist
			new File(pathToDownload).mkdir();
			
			// make a 2 letters species code
			temporary = species.split("_");
			String code = temporary[0].substring(0,1) + temporary[1].substring(0,1);
			
			
			// download the pathway and give status in console
			wp.downloadPathway(pathwayNames.get(i), 
				new File (pathToDownload + code + "_" + namePathway + ".gpml"));
			System.out.println("Downloaded file "+i+" of "+pathwayNames.size()+ ": " + pathwayNames.get(i));
		}
	}
}