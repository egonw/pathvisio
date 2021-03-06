// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.wikipathways.server;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.wikipathways.client.IdSearchPanel;
import org.pathvisio.wikipathways.client.Query;
import org.pathvisio.wikipathways.client.Result;
import org.pathvisio.wikipathways.client.ResultsTable;
import org.pathvisio.wikipathways.client.SearchException;
import org.pathvisio.wikipathways.client.SearchService;
import org.pathvisio.wikipathways.webservice.WSIndexField;
import org.pathvisio.wikipathways.webservice.WSSearchResult;
import org.wikipathways.client.WikiPathwaysClient;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implements the {@link SearchService}.
 * @author thomas
 */
public class SearchServiceImpl extends RemoteServiceServlet implements SearchService {
	WikiPathwaysClient client;

	public SearchServiceImpl() throws ServiceException {
		BioDataSource.init();
	}

	protected static URL getClientUrl(ServletContext servlet) {
		URL url = null;
		try {
			url = new URL(
					"http://www.wikipathways.org/wpi/webservice/webservice.php"
			);
			Properties prop = new Properties();
			prop.load(new FileInputStream(
					new File(servlet.getRealPath(""), "wikipathways.props"))
			);
			String wsurl = (String)prop.get("webservice-url");
			if(wsurl != null) {
				url = new URL(wsurl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	private WikiPathwaysClient getClient() {
		if(client == null) {
			try {
				client = new WikiPathwaysClient(getClientUrl(getServletContext()));
			} catch(Exception e) {
				throw new RuntimeException("Unable to create wikipathways client", e);
			}
		}
		return client;
	}

	private ImageManager getImageManager() {
		if(!ImageManager.isInit()) {
			ImageManager.init(getServletContext().getRealPath(""), getClient());
		}
		return ImageManager.getInstance();
	}

	public Result[] search(Query query) throws SearchException {
		try {
			if(Query.TYPE_ID.equals(query.getType())) { //System code specified, use id search
				return searchIdentifier(query);
			} else { //Use text search
				return searchText(query.getText());
			}
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	private Result createPathwayResult(final WSSearchResult wsr, String id) {
		Logger.log.info("Creating pathway result for: " + wsr.getId());
		String iddescr = null;
		try {
			if(id != null) {
				Pathway pathway = getImageManager().getPathway(wsr);
				String idlist = "<ul>";
				WSIndexField[] fields = wsr.getFields();
				if(fields == null) fields = new WSIndexField[0];
				for(WSIndexField f : fields) {
					if("graphId".equals(f.getName())) {
						String[] graphIds = f.getValues();
						if(graphIds == null) graphIds = new String[0];
						for(String graphId : graphIds) {
							PathwayElement pwe = pathway.getElementById(graphId);
							if(pwe != null) {
								idlist += "<li>" + pwe.getTextLabel() + " (";
								Xref xref = pwe.getXref();
								DataSource ds = xref.getDataSource();
								idlist += "<a href='" + xref.getUrl() + "'>" +
									xref.getId() + (ds == null ? "" : ", " + ds.getFullName()) + "</a>)";
							}
						}
					}
				}
				if(!"<ul>".equals(idlist)) {
					iddescr = idlist + "</ul>";
				}
			}

		} catch(ConverterException e) {
			e.printStackTrace();
		}

		String descr = "<table class='" + ResultsTable.STYLE_DESCRIPTION + "'>";
		descr += "<tr><td><i>Title:</i> " + wsr.getName();
		descr += "<tr><td><i>Organism:</i> " + wsr.getSpecies();
		descr += "<tr><td><i>Pathway ID:</i> " + wsr.getId();
		if(iddescr != null) descr += "<tr><td><i>" +
				"IDs mapping to " + id + ":</i> " + iddescr;
		descr += "</table>";

		Result r = new Result(
				wsr.getId(),
				wsr.getName() + " (" + wsr.getSpecies() + ")",
				descr,
				wsr.getUrl(),
				wsr.getSpecies()
		);
		WSIndexField[] fields = wsr.getFields();
		if(fields != null) {
			for(WSIndexField f : wsr.getFields()) {
				r.setProperty(f.getName(), f.getValues());
			}
		}
		r.setImageId("" + ImageManager.getGpmlId(wsr.getId(), wsr.getRevision()));

		getImageManager().startDownload(wsr); //Start downloading image that will be requested by client later
		return r;
	}

	private WSSearchResult[] mergeResults(WSSearchResult[] results) {
		List<WSSearchResult> merged = new ArrayList<WSSearchResult>();
		Map<String, WSSearchResult> resultById = new HashMap<String, WSSearchResult>();
		//Merge result if it's the same pathway
		for(WSSearchResult r : results) {
			WSSearchResult m = resultById.get(r.getId());
			if(m == null) {
				resultById.put(r.getId(), r);
				merged.add(r);
			} else {
				//Merge fields
				Set<WSIndexField> fields = new HashSet<WSIndexField>();
				for(WSIndexField f : r.getFields()) fields.add(f);
				for(WSIndexField f : m.getFields()) fields.add(f);
				m.setFields(fields.toArray(new WSIndexField[fields.size()]));
				//Merge score
				m.setScore(Math.max(m.getScore(), r.getScore()));
			}
		}
		
		return merged.toArray(new WSSearchResult[merged.size()]);
	}
	
	private Result[] searchText(String query) throws ServiceException, RemoteException {
		WSSearchResult[] wsResults = getClient().findPathwaysByText(query, null);
		Result[] results = new Result[wsResults.length];
		for(int i = 0; i < wsResults.length; i++) {
			results[i] = createPathwayResult(wsResults[i], null);
		}
		return results;
	}

	private Result[] searchIdentifier(Query query) throws RemoteException {
		WSSearchResult[] wsResults = new WSSearchResult[0];

		String id = query.getText();
		String system = query.getField(Query.FIELD_SYSTEM);
		if(IdSearchPanel.SYSTEM_ALL.equals(system) || system == null) {
			wsResults = getClient().findPathwaysByXref(id);
		} else {
			DataSource ds = DataSource.getByFullName(system);
			Xref xref = new Xref(id, ds);
			wsResults = getClient().findPathwaysByXref(xref);
		}		
		wsResults = mergeResults(wsResults);

		Result[] results = new Result[wsResults.length];
		for(int i = 0; i < wsResults.length; i++) {
			results[i] = createPathwayResult(wsResults[i], id);
		}
		return results;
	}

	public void waitForImage(String id) {
		try {
			getImageManager().waitForImage(id);
		} catch(Exception e) {
			throw new SearchException(e);
		}
	}

	public String[] getSystemNames() {
		String[] names = DataSource.getFullNames().toArray(new String[0]);
		Arrays.sort(names);
		return names;
	}

	public String[] getOrganismNames() {
		String[] orgs;
		try {
			orgs = getClient().listOrganisms();
			Arrays.sort(orgs);
			return orgs;
		} catch (RemoteException e) {
			throw new SearchException(e);
		}
	}
}
