package es.upm.dit.gsi.episteme.matching;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.semmf.engine.MatchingEngine;
import de.fuberlin.wiwiss.semmf.result.ClusterMatchingResult;
import de.fuberlin.wiwiss.semmf.result.GraphMatchingResult;
import de.fuberlin.wiwiss.semmf.result.MatchingResult;
import de.fuberlin.wiwiss.semmf.result.NodeMatchingResult;
import de.fuberlin.wiwiss.semmf.result.PropertyMatchingResult;
import de.fuberlin.wiwiss.semmf.vocabulary.MD;

/**
 * 
 * @author Adriano Martin
 * @version 1.0
 */
public class ServiceMatching {

	/**
	 * @param baseURL
	 * @param localURL
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
//	public static void calMatching(String baseURL, String pathFileOffer, String[] weights) throws MalformedURLException, IOException {
	public static JSONObject calMatching(String baseURL, String pathFileOffer, String[] weights) throws MalformedURLException, IOException {		
		String filePathServiceMD = baseURL + "doc/serviceMD.n3";
		Model serviceMD = createServiceMD(baseURL, pathFileOffer, weights);
		writeServiceMDtoFile (serviceMD, filePathServiceMD, "N3");	     
			
		MatchingEngine me = new MatchingEngine("file:" + baseURL + "config/assemblerMappings.rdf", 
				"file:" + baseURL + "doc/serviceMD.n3", "N3");
//		File archivo = new File(filePathServiceMD);
//		FileReader fr = new FileReader(archivo);
//		BufferedReader  br = new BufferedReader(fr);
//	    String linea;
//	    while((linea=br.readLine())!=null)
//	    	System.out.println(linea);
		MatchingResult mr = me.exec();
		
		File outputFile = new File(filePathServiceMD);
		if (outputFile.exists()) {
        	outputFile.delete();        	 
		}
		
		printMatchingResult(mr);
		return jsonResponse(mr);
	}
	

	/**
	 * see tutorial: how to create a matching description (included in SemMF distribution)
	 */ 
	public static Model createServiceMD (String baseURL, String pathFileOffer, String[] weights) {
		
		Model m = ModelFactory.createDefaultModel();
		
		Resource gmd = m.createResource();
		gmd.addProperty(RDF.type, MD.GraphMatchingDescription);
		gmd.addProperty(MD.queryModelURL, "file:" + pathFileOffer);
		gmd.addProperty(MD.resModelURL, "file:" + baseURL + "doc/ent2.rdf");
		gmd.addProperty(MD.queryGraphURI, "http://example.org/CategoriesRequired.rdfs#CR");
		gmd.addProperty(MD.resGraphURIpath, "(?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://kmm.lboro.ac.uk/ecos/1.0#Enterprise>)");
						
		Bag cmds = m.createBag();
		gmd.addProperty(MD.hasClusterMatchingDescriptions, cmds);
		
		Resource cmd_skills = m.createResource();
		cmd_skills.addProperty(RDF.type, MD.ClusterMatchingDescription);		
		cmd_skills.addProperty(MD.label, "sectors");
		cmd_skills.addProperty(MD.weight, "1");						
		cmds.add(cmd_skills);
			
		Bag nmds_cskills = m.createBag();
		cmd_skills.addProperty(MD.hasNodeMatchingDescriptions, nmds_cskills);
		
		Resource cskills_nmd = m.createResource();
		nmds_cskills.add(cskills_nmd);
		cskills_nmd.addProperty(RDF.type, MD.NodeMatchingDescription);
		cskills_nmd.addProperty(MD.label, "skills");
		cskills_nmd.addProperty(MD.weight, "1");
		cskills_nmd.addProperty(MD.queryNodePath, "(<http://example.org/CategoriesRequired.rdfs#CR> <http://example.org/CategoriesRequired.rdfs#hasCategorieDetails> ?categorieDetails) (?categorieDetails <http://example.org/CategoriesRequired.rdfs#requiredCompetence> ?x)");
//		cskills_nmd.addProperty(MD.resNodePath, "(<#graphEntryURI#> <http://kmm.lboro.ac.uk/ecos/1.0#Specific> ?Description) (?Description <http://kmm.lboro.ac.uk/ecos/1.0#Skill> ?Description2) (<http://kmm.lboro.ac.uk/ecos/1.0#Skill> ?Description2 ?x)");
		cskills_nmd.addProperty(MD.resNodePath, "(<#graphEntryURI#> <http://kmm.lboro.ac.uk/ecos/1.0#Skill> ?x)");
		cskills_nmd.addProperty(MD.reverseMatching, "false");
		
		Bag pmds = m.createBag();
		cskills_nmd.addProperty(MD.hasPropertyMatchingDescriptions, pmds);
							
		Resource pmd_skill = m.createResource();
		pmds.add(pmd_skill);
		pmd_skill.addProperty(RDF.type, MD.PropertyMatchingDescription);
		pmd_skill.addProperty(MD.label, "skill");
		pmd_skill.addProperty(MD.weight, "1");
		pmd_skill.addProperty(MD.queryPropURI, RDF.type);
		pmd_skill.addProperty(MD.resPropURI, RDF.type);
		pmd_skill.addProperty(MD.reverseMatching, "false");
		
		Resource tm = m.createResource();
		tm.addProperty(RDF.type, MD.TaxonomicMatcher);
		pmd_skill.addProperty(MD.useMatcher, tm);
		tm.addProperty(MD.simInheritance, "true");
					
		Resource taxon_skills = m.createResource();
		taxon_skills.addProperty(RDF.type, MD.Taxonomy);
		taxon_skills.addProperty(MD.taxonomyURL, "file:" + baseURL + "doc/it-cat.rdfs");
		taxon_skills.addProperty(MD.rootConceptURI, "http://kmm.lboro.ac.uk/ecos/1.0#IT_Cats");
		tm.addProperty(MD.taxonomy, taxon_skills);
					
		Resource emc = m.createResource();
		emc.addProperty(RDF.type, MD.ExpMilestCalc);
		emc.addProperty(MD.k_factor, "2");
		tm.addProperty(MD.useMilestoneCalc, emc);

			
		

		
//		Resource[] cskills_nmd = new Resource[weights.length];
//		Bag[] pmds = new Bag[weights.length];
//		Resource[] pmd_skill= new Resource[weights.length];
//		Resource[] tm = new Resource[weights.length];
//		Resource[] taxon_skills = new Resource[weights.length];
//		Resource[] emc = new Resource[weights.length];
//		
//		for (int i = 0; i < weights.length; i++) {
//			
//			cskills_nmd[i] = m.createResource();
//			nmds_cskills.add(cskills_nmd[i]);
//			cskills_nmd[i].addProperty(RDF.type, MD.NodeMatchingDescription);
//			cskills_nmd[i].addProperty(MD.label, "skills");
//			cskills_nmd[i].addProperty(MD.weight, weights[i]);
//			cskills_nmd[i].addProperty(MD.queryNodePath, "(<http://example.org/CategoriesRequired.rdfs#CR> <http://example.org/CategoriesRequired.rdfs#hasCategorieDetails> ?categorieDetails) (?categorieDetails <http://example.org/CategoriesRequired.rdfs#requiredCompetence> ?x)");
//			cskills_nmd[i].addProperty(MD.resNodePath, "(<#graphEntryURI#> <http://example.org/Company.rdfs#hasCat> ?x)");
//			cskills_nmd[i].addProperty(MD.reverseMatching, "false");
//			
//			pmds[i] = m.createBag();
//			cskills_nmd[i].addProperty(MD.hasPropertyMatchingDescriptions, pmds[i]);
//								
//			pmd_skill[i] = m.createResource();
//			pmds[i].add(pmd_skill[i]);
//			pmd_skill[i].addProperty(RDF.type, MD.PropertyMatchingDescription);
//			pmd_skill[i].addProperty(MD.label, "skill");
//			pmd_skill[i].addProperty(MD.weight, "1");
//			pmd_skill[i].addProperty(MD.queryPropURI, RDF.type);
//			pmd_skill[i].addProperty(MD.resPropURI, RDF.type);
//			pmd_skill[i].addProperty(MD.reverseMatching, "false");
//			
//			tm[i] = m.createResource();
//			tm[i].addProperty(RDF.type, MD.TaxonomicMatcher);
//			pmd_skill[i].addProperty(MD.useMatcher, tm[i]);
//			tm[i].addProperty(MD.simInheritance, "true");
//					
//			taxon_skills[i] = m.createResource();
//			taxon_skills[i].addProperty(RDF.type, MD.Taxonomy);
//			taxon_skills[i].addProperty(MD.taxonomyURL, "file:" + baseURL + "doc/it-cat.rdfs");
//			taxon_skills[i].addProperty(MD.rootConceptURI, "http://example.org/it-cat.rdfs#IT_Cats");
//			tm[i].addProperty(MD.taxonomy, taxon_skills[i]);
//					
//			emc[i] = m.createResource();
//			emc[i].addProperty(RDF.type, MD.ExpMilestCalc);
//			emc[i].addProperty(MD.k_factor, "2");
//			tm[i].addProperty(MD.useMilestoneCalc, emc[i]);
//			
//		}
		
		return m;		
	}
	
	/**
	 * @param mr
	 * @return 
	 */
	@SuppressWarnings("rawtypes")
	public static JSONObject jsonResponse (MatchingResult mr) {
		boolean requestOffer = true;
		List<String> auxOffer = new ArrayList<String>();
		String aux = "{\"offer\":[";
		String candidates = "\"candidates\": \n\r[";
		while (mr.hasNext()) {
			GraphMatchingResult gmr = mr.next();
			System.out.println(gmr.getResGraphEntryNode().getURI());
			System.out.println(gmr.getSimilarity());
			candidates += "{\"company\":\""+gmr.getResGraphEntryNode().getURI().substring(19)+"\", " +
					"\"global_value\":"+gmr.getSimilarity()+",\n\r\r\"results\":["; 
			List clusterList = gmr.getClusterMatchingResultList();
			for (Iterator itC = clusterList.iterator(); itC.hasNext();) {
				ClusterMatchingResult cmr = (ClusterMatchingResult) itC.next();
				List nodeList = cmr.getNodeMatchingResultList();
				for (Iterator itN = nodeList.iterator(); itN.hasNext();) {
					NodeMatchingResult nmr = (NodeMatchingResult) itN.next();
					List propertyList = nmr.getPropertyMatchingResultList();
					for (Iterator itP = propertyList.iterator(); itP.hasNext();) {
						PropertyMatchingResult pmr = (PropertyMatchingResult) itP.next();
						System.out.println("MIRAR AQUI");
						System.out.println(pmr.getResPropVal().toString());
						if (requestOffer == true)
							auxOffer.add(pmr.getQueryPropVal().toString().substring(34));
						if (itP.hasNext()) candidates += "{\"skill\":\""+pmr.getResPropVal().toString().substring(0)+"\",";
						else candidates += "\"level\":\""+pmr.getResPropVal().toString().substring(0)+"\","; 
					}
					candidates += "\"value\":"+nmr.getSimilarity();
					if (itN.hasNext()) candidates += "},\n\r\r\r";
					else candidates += "}";
				}
			}
			requestOffer=false;
			if (!mr.hasNext()) candidates += "]}\n\r]\n}";
			else candidates += "]},\n\r";
		}
		
		for (int k=0; k<auxOffer.size(); k=k+2){
			aux += "{\"skill\":\"" + auxOffer.get(k) + "\", \"level\":\""+auxOffer.get(k+1)+"\"}";
			if (k+2==auxOffer.size()) aux += "],\n";
			else aux += ",";
		}
		
		aux += candidates;
				
		JSONObject json = new JSONObject();
		try {
			json = new JSONObject(aux);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	@SuppressWarnings("rawtypes")
	public static void printMatchingResult (MatchingResult mr) {
		
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("Query Graph: " + mr.getFirst().getQueryGraphEntryNode().getURI());
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\r");
		
		while (mr.hasNext()) {
			
			GraphMatchingResult gmr = mr.next();
						
			System.out.println("\r===========================================================");
			System.out.println("Res Graph : " + gmr.getResGraphEntryNode().getURI());
			System.out.println("Sim       : " + gmr.getSimilarity());
			System.out.println("===========================================================\r");
					
			List clusterList = gmr.getClusterMatchingResultList();
			for (Iterator itC = clusterList.iterator(); itC.hasNext();) {
				
				ClusterMatchingResult cmr = (ClusterMatchingResult) itC.next();
				
				System.out.println("-------------------------------------------");
				System.out.println("Cluster label : " + cmr.getLabel());
				System.out.println("Cluster sim   : " + cmr.getSimilarity());
				System.out.println("Cluster weight: " + cmr.getWeight());
				
				List nodeList = cmr.getNodeMatchingResultList();
				
				for (Iterator itN = nodeList.iterator(); itN.hasNext();) {
					
					NodeMatchingResult nmr = (NodeMatchingResult) itN.next();
					
					List propertyList = nmr.getPropertyMatchingResultList();
										
					System.out.println("    - - - - - - - - - - - - - - - - - - --");
					System.out.println("    Node label : " + nmr.getLabel());					
					if (propertyList.isEmpty()) {
						System.out.println("    query node : " + nmr.getQueryNode().toString());
						System.out.println("    res node   : " + nmr.getResNode().toString());									
					}
					System.out.println("    Node sim   : " + nmr.getSimilarity());
					System.out.println("    Node weight: " + nmr.getWeight());

					
					for (Iterator itP = propertyList.iterator(); itP.hasNext();) {
						
						PropertyMatchingResult pmr = (PropertyMatchingResult) itP.next();
						
						System.out.println("        . . . . . . . . . . . . . . . . . ");
						System.out.println("        prop label : " + pmr.getLabel());
						System.out.println("        query prop : " + pmr.getQueryPropVal().toString());
						System.out.println("        res prop   : " + pmr.getResPropVal().toString());			
						System.out.println("        prop sim   : " + pmr.getSimilarity());
						System.out.println("        prop weight: " + pmr.getWeight());
					
					}
									
				}				
			}
		}		
		mr.setToFirst();
	}
	
	/**
	 * @param m
	 * @param filePath
	 * @param lang
	 */
	private static void writeServiceMDtoFile(Model m, String filePath, String lang) {
		
		m.setNsPrefix("semmf", MD.NS);
		m.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		m.setNsPrefix("ja", "http://jena.hpl.hp.com/2005/11/Assembler#");
		
		try {
			File outputFile = new File(filePath);
			if (!outputFile.exists()) {
	        	outputFile.createNewFile();        	 
	        }
			FileOutputStream out = new FileOutputStream(outputFile);
			m.write(out, lang);
			out.close();
		}
		catch (IOException e) { System.out.println(e.toString()); }
	}
	
}