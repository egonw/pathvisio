package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.*;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.rosuda.JRI.Rengine;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import util.FileUtils;
import util.SwtUtils.SimpleRunnableWithProgress;
import util.XmlUtils.PathwayParser;
import util.XmlUtils.PathwayParser.Gene;
import R.RCommands.RException;
import R.RCommands.RTemp;
import R.RCommands.RniException;
import data.GmmlGdb;
import data.GmmlGex;
import data.GmmlGdb.IdCodePair;
import data.GmmlGex.Sample;

public class RData {
	List<File> pwFiles;
	
	boolean exportPws;			//Export pathways or not
	boolean exportData;			//Export data or not
	boolean incCrit;			//Include criteria in export data or not
	
	String pwDir = "";			//Pathway directory to import
	String exportFile = "";		//File name to export RData
	String pwsName = "";		//Name of pathwayset object
	String dsName = "";			//Name of dataset object
 	
	static int totalWorkData = (int)1E6;
	static int totalWorkPws = (int)1E6;
	
	DataSet cacheDataSet;
	PathwaySet cachePathwaySet;
	
	public RData() {
		pwFiles = new ArrayList<File>();
	}
	
	/**
	 * Create a new RData instance containing the given pathway(s) and expression data (if loaded).
	 * Extra cross-references for GeneProducts will
	 * be stored when neccessary to match the expression data with pathways.
	 * @param pathways	The pathway or directory to include.
	 * @param recursive Whether to include subdirectories or not (ignored if argument 'pathways' points
	 * to a single file)
	 */
	public RData(File pathways, boolean recursive) {
		this();
		//Get the pathway files
		pwFiles = FileUtils.getFiles(pathways, "xml", recursive);
	}
	
	public List<File> getPathwayFiles() { return pwFiles; }
	
	public void doExport(Rengine re) throws Exception {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(GmmlVision.getWindow().getShell());
		SimpleRunnableWithProgress rwp = null;
		try {
			if(exportData) {
				rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doExportData", 
						new Class[] { re.getClass() }, new Object[] { re }, this);
				SimpleRunnableWithProgress.setMonitorInfo(
						"Exporting data", totalWorkData);
				dialog.run(true, true, rwp); 
			}
			if(exportPws) {
				rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doExportPws", 
						new Class[] { re.getClass() }, new Object[] { re }, this);
				SimpleRunnableWithProgress.setMonitorInfo(
						"Exporting pathways", totalWorkPws);
				dialog.run(true, true, rwp);
			}
			RCommands.eval("save.image(file='"+ exportFile + "')");
		} catch(InvocationTargetException ex) {
			rwp.openMessageDialog("Error", "Unable to export data: " + ex.getCause().getMessage());
			GmmlVision.log.error("Unable to export to R", ex);
			RTemp.flush(true); //Clear temp variables
			RCommands.eval("save.image(file='"+ exportFile + ".EX.RData')"); //Save datafile (to check what went wrong)
			RCommands.eval("rm(list=ls())"); //Remove everything from R workspace
			return;
		}
	}

	public void doExportPws(Rengine re) throws Exception {
		double contribXml = 0.2;
		PathwaySet.contribGdb = 0.7;
		double contribR = 0.1;
		
		File pwDir = new File(this.pwDir);
		
		//Check some parameters
		if(exportFile.equals("")) throw new Exception("specify file to export to");
		if(!pwDir.canRead()) throw new Exception("invalid pathway directory: " + this.pwDir);
		if(pwsName.equals("")) throw new Exception("No name specified for the exported pathways object");
		
		pwFiles = FileUtils.getFiles(pwDir, "xml", true);

		//Calculate contribution of single Pathway
		Pathway.progressContribution = (int)((double)totalWorkPws * contribR / pwFiles.size());
		int pwContribXml = (int)((double)totalWorkPws * contribXml / pwFiles.size());
		
		cachePathwaySet = new PathwaySet(pwsName);
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		for(File f : pwFiles) {
			RCommands.checkCancelled();

			PathwayParser p = new PathwayParser(xmlReader);
			try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { 
				GmmlVision.log.error("Couldn't read " + f, e); 
				continue; 
			}
			
			Pathway pw = new Pathway(p.getName(), f.getName(), cachePathwaySet);

			for(Gene g : p.getGenes()) {
				String id = g.getId();
				String code = g.getCode();
				if(id.length() == 0 && code.length() == 0) continue; //Skip empty fields
				pw.addGeneProduct(new IdCodePair(g.getId(), g.getCode()));
			}
			
			//Update progress
			SimpleRunnableWithProgress.updateMonitor(pwContribXml);
		}

		if(exportData && cacheDataSet != null) 
			cachePathwaySet.addCrossRefs(cacheDataSet);
		
		cachePathwaySet.toR(pwsName);
		
		RTemp.flush(true);
	}
	
	public void doExportData(Rengine re) throws Exception {		
		cacheDataSet = new DataSet(dsName);
		
		cacheDataSet.toR(dsName);
		
		RTemp.flush(true);
	}
			
	static abstract class RObject {	
		static final RException EX_NO_GDB = 
			new RException(null, "No gene database loaded!");
		static final RException EX_NO_GEX = 
			new RException(null, "No expression dataset selected!");
		
		void toR(String symbol) throws RException {
			Rengine re = RController.getR();
			long xp = getRef();
			re.rniAssign(symbol, xp, 0);
		}
		
		String toRTemp() throws RException { return toRTemp(true); }
		
		String toRTemp(boolean protect) throws RException {
			String tmpVar = RTemp.getNewVar(protect);
			toR(tmpVar);		
			return tmpVar;
		}
		
		abstract long getRef() throws RException;
		
		protected long returnRef(long xp, String tmp) throws RException {
			return returnRef(xp, new String[] { tmp });
		}
		
		long returnRef(long xp, String[] tmp) throws RException {			
			//Check for error in reference
			if(xp == 0) throw new RniException(RController.getR(), RniException.CAUSE_XP_ZERO);
//			RController.getR().rniProtect(xp); //Protect this reference...we have to unprotect it somewhere?
			
			RTemp.dispose(tmp);
			return xp;
		}
	}
	
	static class PathwaySet extends RObject {
		static double contribGdb;
		String name;
		List<Pathway> pathways;	
		
		HashMap<IdCodePair, GeneProduct> geneProducts;
		
		PathwaySet(String name) {
			this.name = name;
			pathways = new ArrayList<Pathway>();
			geneProducts = new HashMap<IdCodePair, GeneProduct>();
		}
		
		void addPathway(Pathway pw) { pathways.add(pw); }
		
		long getRef() throws RException {				
			String tmpVar = RTemp.getNewVar(true);
			
			RCommands.assign(tmpVar, pathways);
	
			String cmd = "PathwaySet(name = '" + name + "', pathways = " + tmpVar + ")";		
			long xp = RCommands.eval(cmd).xp;
			
			return returnRef(xp, tmpVar);
		}
		
		GeneProduct getUniqueGeneProduct(IdCodePair idc) {
			GeneProduct gp = geneProducts.get(idc);
			if(gp == null) {
				gp = new GeneProduct(idc);
				geneProducts.put(idc, gp);
			}
			return gp;
		}
		
		void addCrossRefs(DataSet dataSet) throws Exception {
			int worked = (int)((double)(totalWorkPws * contribGdb) / geneProducts.size());
			
			List<String> codes = dataSet.getCodes();
			HashMap repHash = dataSet.getReporterHash();
			
			for(IdCodePair pwidc : geneProducts.keySet()) {
				RCommands.checkCancelled();
				
				System.out.println("Processing " + pwidc);
				List<IdCodePair> pwrefs = GmmlGdb.getCrossRefs(pwidc, codes);
				System.out.println("Crossrefs: " + pwrefs);
				for(IdCodePair ref : pwrefs) {
					if(repHash.containsKey(ref)) {
						geneProducts.get(pwidc).addReference(ref);
						System.out.println("adding " + ref + " to " + geneProducts.get(pwidc));
						break;
					}
				}
				SimpleRunnableWithProgress.updateMonitor(worked);
			}
			System.out.println("Finished processing " + geneProducts.size() + " geneproducts");
		}
}
	
	static class Pathway extends RObject {
		static int progressContribution;
		
		String name;
		String fileName;
		List<GeneProduct> geneProducts;
		PathwaySet pws;
		
		Pathway(String name, String fileName, PathwaySet pathwaySet) {
			this.name = name;
			this.fileName = fileName;
			this.pws = pathwaySet;
			geneProducts = new ArrayList<GeneProduct>();
			
			pws.addPathway(this);
		}
		
		void addGeneProduct(IdCodePair ref) {
			geneProducts.add(pws.getUniqueGeneProduct(ref));
		}
		
		long getRef() throws RException {			
			String tmpVar = RTemp.getNewVar(true);
			RCommands.assign(tmpVar, geneProducts);
						
			String cmd = "Pathway(name = '" + name + 
				"',fileName = '" + fileName + 
				"', geneProducts = " + tmpVar + ")";			
			long xp =  RCommands.eval(cmd).xp;
			
			SimpleRunnableWithProgress.updateMonitor(progressContribution);
			return returnRef(xp, tmpVar);
		}
	}
	
	static class GeneProduct extends RObject {		
		List<IdCodePair> refs;
		
		GeneProduct() {
			refs = new ArrayList<IdCodePair>();
		}
		
		GeneProduct(String id, String code) {
			this();
			addReference(id, code);
		}
		
		GeneProduct(IdCodePair idc) {
			this();
			addReference(idc);
		}
		
		void addReference(IdCodePair idc) {
			if(!refs.contains(idc)) refs.add(idc);
		}
		
		void addReference(String id, String code) {
			IdCodePair idc = new IdCodePair(id, code);
			addReference(idc);
		}
		
		long getRef() throws RException {
			Rengine re = RController.getR();
			String[] ar = new String[refs.size() * 2];
			int i = 0;
			for(IdCodePair ref : refs) {
				ar[i] = ref.getId(); //id
				ar[i++ + refs.size()] = ref.getCode(); //code
			}
			
			long ref_gp = re.rniPutStringArray(ar);
			re.rniProtect(ref_gp);
			re.rniSetAttr(ref_gp, "dim", re.rniPutIntArray(new int[] { refs.size(), 2 }));
			re.rniSetAttr(ref_gp, "class", re.rniPutString("GeneProduct"));
            re.rniUnprotect(1);
            
            return ref_gp;            
		}
		
		public void merge(GeneProduct gp) {
			for(IdCodePair ref : gp.refs) addReference(ref);
		}
		
		public String toString() {
			return "GeneProduct: " + refs;
		}
		
		public String getName() {
			return refs.size() == 0 ? "no reference" : refs.get(0).toString();
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof GeneProduct)) return false;
			GeneProduct gp = (GeneProduct)o;
			for(IdCodePair ref : refs) 
				for(IdCodePair oref : gp.refs) 
					if(ref.equals(oref)) return true;
			return false;
		}
	}
		
	static class DataSet extends RObject {
		String[][] data; //[samples][reporters] (transposed for export convenience)
		IdCodePair [] reporters;
		HashMap<Integer, Integer> sample2Col;
		int[] col2Sample;
		
		String name;
		
		DataSet(String name) throws Exception {		
			this.name = name;
			queryData(); //Get the data from the gex database
		}
		
		HashMap<IdCodePair, IdCodePair> getReporterHash() {
			HashMap<IdCodePair, IdCodePair> repHash = new HashMap<IdCodePair, IdCodePair>();
			for(IdCodePair rep : reporters) {
				repHash.put(rep, rep);
			}
			return repHash;
		}
		
		List<String> getCodes() throws Exception {
			List<String> codes = new ArrayList<String>();
			ResultSet r = GmmlGex.getCon().createStatement().executeQuery(
					"SELECT DISTINCT code FROM expression");
			while(r.next()) codes.add(r.getString("code"));
			return codes;
		}
		
		long getRef() throws RException {
			Rengine re = RController.getR();
			
			//Create the mixed-type data matrix in R
			//#1 create a long 1-dimensional list -> fill rows first
			//#2 set dims attribute to c(nrow, ncol)
			//et voila, we have a matrix
			HashMap<Integer, Sample> samples = GmmlGex.getSamples();
			long l_ref = re.rniInitVector(data.length * data[0].length);
			re.rniProtect(l_ref);
			
			for(int i = 0; i < data.length; i++) { //Rows (samples)
				int sid = col2Sample[i];
				int type = samples.get(sid).getDataType();
				
				for(int j = 0; j < data[i].length; j++) { //Columns (reporters)
					long e_ref;
					if(type == Types.REAL) {
						double[] value = new double[1];
						try {
							value[0] = Double.parseDouble(data[i][j]);
						} catch(Exception e) {
							GmmlVision.log.error("Unable to parse double when converting data to R: " + data[i][j], e);
						}
						e_ref = re.rniPutDoubleArray(value);
					} else {
						e_ref = re.rniPutString(data[i][j]);
					}
					re.rniVectorSetElement(e_ref, l_ref, i*data[i].length + j);
				}
			}
			
			
			//Set dimensions
			long d_ref = re.rniPutIntArray(new int[] { data[0].length, data.length });
			re.rniSetAttr(l_ref, "dim", d_ref);
			
			//Set rownames (reporters) and colnames (samples)
			String[] rep_names = new String[reporters.length];
			String[] smp_names = new String[data.length];
			
			for(int k = 0; k < reporters.length; k++)
				rep_names[k] = reporters[k].getName();
			
			for(int k = 0; k < data.length; k++) 
				smp_names[k] = samples.get(col2Sample[k]).getName();
			
			long dn_ref = re.rniInitVector(2);
			re.rniProtect(dn_ref);
			long rown_ref = re.rniPutStringArray(rep_names);
			re.rniVectorSetElement(rown_ref, dn_ref, 0);
			long coln_ref = re.rniPutStringArray(smp_names);
			re.rniVectorSetElement(coln_ref, dn_ref, 1);
			re.rniSetAttr(l_ref, "dimnames", dn_ref);
			re.rniUnprotect(1);
					
			//Assign data matrix
			String tmpData = RTemp.getNewVar();
			re.rniAssign(tmpData, l_ref, 0);
			
			//Assign new dataset
			long xp = RCommands.eval("DataSet(data = " + tmpData + ", name = '" + name + "')").xp;
			
			re.rniUnprotect(1);
			return returnRef(xp, tmpData);
		}
		
		void queryData() throws Exception {
			//Get the 'groups'
			Statement s = GmmlGex.getCon().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			ResultSet r = s.executeQuery("SELECT DISTINCT groupId FROM expression");
			
			//Set the proper dimensions for the data matrix
			//Rows:
			r.last();
			int nrow = r.getRow();
			r.beforeFirst(); //Set the cursor back to the start
			//Columns:
			int ncol = GmmlGex.getSamples().size();
			
			data = new String[ncol][nrow];
			reporters = new IdCodePair[nrow];
			sample2Col = new HashMap<Integer, Integer>();
			int col = 0;
			col2Sample = new int[ncol];
			for(int sid : GmmlGex.getSamples().keySet()) {
				col2Sample[col] = sid;
				sample2Col.put(sid, col++);
			}
			
			//Calculate the progress contribution of a single group
			int progressContribution = (int)((double)totalWorkData / nrow);
						
			//Fill data matrix for every 'group'
			PreparedStatement pst_dta = GmmlGex.getCon().prepareStatement(
					"SELECT idSample, data FROM expression WHERE groupId = ?");
			PreparedStatement pst_rep = GmmlGex.getCon().prepareStatement(
					"SELECT DISTINCT id, code FROM expression WHERE groupid = ?");
			int i = -1;
			while(r.next()) {
				RCommands.checkCancelled();
				
				int group = r.getInt(1);
				pst_dta.setInt(1, group);
				pst_rep.setInt(1, group);
				
				ResultSet r1 = pst_rep.executeQuery();
				if(r1.next()) 
					reporters[++i] = new IdCodePair(r1.getString("id"), r1.getString("code"));
				r1 = pst_dta.executeQuery();
				while(r1.next()) {
					int sid = r1.getInt("idSample");
					String dta = r1.getString("data");
					data[sample2Col.get(sid)][i] = dta;
				}
				
				SimpleRunnableWithProgress.updateMonitor(progressContribution);
			}
		}	
	}
}
