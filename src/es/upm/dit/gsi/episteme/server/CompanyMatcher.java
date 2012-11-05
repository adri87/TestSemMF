package es.upm.dit.gsi.episteme.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import es.upm.dit.gsi.episteme.matching.ServiceMatching;

/**
 * Servlet implementation class CompanyMatcher
 */
public class CompanyMatcher extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CompanyMatcher() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}

	/**
	 * 
	 */
	protected void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// access the params		
//		String[] categorie = request.getParameterValues("categorie");
//		String[] weights = request.getParameterValues("weight");
 		@SuppressWarnings("rawtypes")
		Enumeration enu = request.getParameterNames();
		HashMap<String, String> requeriments = new HashMap<String, String>();
		while (enu.hasMoreElements()){
			String name = enu.nextElement().toString();
			if (name.contains("categorie")){
				if (request.getParameter("weight["+name.substring(10, 11)+"]") != null){
					requeriments.put(request.getParameter(name), request.getParameter("weight["+name.substring(10, 11)+"]"));
				}
			}
		}
		
		String[] weights = requeriments.values().toArray(new String[requeriments.size()]);
		String[] categorie = requeriments.keySet().toArray(new String[requeriments.size()]);

		// write rdf advertising/offer
		File file = null;
		String pathFileOffer = "";
        try {
    		file = new File(getServletContext().getRealPath("/temp") + "/cr" + Long.toString(System.nanoTime()) + ".rdf");
    		file.createNewFile();
    		FileWriter out = new FileWriter(file);
    		out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            out.write("<rdf:RDF\n");
            out.write("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
            out.write("xmlns:cr=\"http://example.org/CategoriesRequired.rdfs#\"\n");
            out.write("xmlns:skill=\"http://kmm.lboro.ac.uk/ecos/1.0#\"\n");
            out.write("xml:base=\"http://example.org/CategoriesRequired.rdfs#\">\n\r");
            out.write("<cr:CategoriesRequired rdf:ID=\"CR\">\n\r");
            out.write("<cr:hasDetails>\n");
            out.write("<cr:Details>\n");
            out.write("<cr:endDate>2012-08-31</cr:endDate>\n");
            out.write("<cr:startDate>2012-07-01</cr:startDate>\n");
            out.write("</cr:Details>\n");
            out.write("</cr:hasDetails>\n\r");
            out.write("<cr:hasCategorieDetails>\n");
            out.write("<cr:CategorieDetails>\n\r");
            for (int i = 0; i < categorie.length; i++) {
            	out.write("<cr:requiredCompetence>\n");
                out.write("<skill:"+categorie[i]+">\n");
                out.write("</skill:"+categorie[i]+">\n");
                out.write("</cr:requiredCompetence>\n\r");            		
            }
            out.write("</cr:CategorieDetails>\n\r");
            out.write("</cr:hasCategorieDetails>\n\r");
            out.write("</cr:CategoriesRequired>\n");
            out.write("</rdf:RDF>");
            out.close();       
            pathFileOffer = file.getAbsolutePath();
        } catch (IOException e) {
        	printError(response, e);
        	return;      	
        }
        
	// execute semantic matching (using semmf)
        String baseUrl = getServletContext().getRealPath("/");
        JSONObject json = ServiceMatching.calMatching(baseUrl, pathFileOffer, weights);
//        ServiceMatching.calMatching(baseUrl, pathFileOffer, weights);
           
		// return output
		PrintWriter pw = new PrintWriter(response.getOutputStream());
		pw.println(json);
		pw.close();
		
	}

	/**
	 *  Print error message
	 * @param e 
	 */
	private void printError(HttpServletResponse response, Exception e) throws IOException {
		PrintWriter pw = new PrintWriter(response.getOutputStream());
		pw.println("<html>" +
				"<body>" +
				"  <p>We could not write file</p>" +
				"  <p>" + e.getMessage() + "</p>" +
				"</body>" +
				"</html>");
		pw.close();
	}
	
}
