package at.jku.isse.designspace.jama;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;

import at.jku.isse.designspace.jama.connector.restclient.httpconnection.ApacheHttpClient;
import at.jku.isse.designspace.jama.connector.restclient.httpconnection.JamaClient;
import at.jku.isse.designspace.jama.connector.restclient.jamaclient.JamaAPI;
import at.jku.isse.designspace.jama.connector.restclient.jamaclient.SchemaProvider;
import at.jku.isse.designspace.jama.service.NoOpProgressObserver;


public class JamaClientTest {

	static JamaClient jc;
	static JamaAPI jama;
	
	@BeforeAll
	public static void setupClient() throws Exception {
		Properties props = new Properties(); 
		props.load(new FileInputStream("application.properties"));
		jc = new JamaClient(new ApacheHttpClient(), props.getProperty("jama.serverURI"), props.getProperty("jama.user"), props.getProperty("jama.password"));
		jama = new JamaAPI(jc, new NoOpProgressObserver());
	}
	
	@Test
	public void testFetchSchema() throws Exception {	
		SchemaProvider sp = new SchemaProvider(jc,  new NoOpProgressObserver());
		System.out.println(sp);
	}
	
	@Test
	public void testFetchByKey() throws Exception {
		String docKey = "PVCSG-WP-475";
		List<Map<String, Object>> candidates = jc.getAll("abstractitems?contains=%22documentKey%3A"+docKey+"%22&maxResults=50");
		System.out.println(candidates);
	}
	
	@Test
	public void fetchRelations() throws Exception {
		String jamaId = "7230585";
		List<Map<String, Object>> links = jc.getAll("items/"+jamaId+"/downstreamrelationships?maxResults=50");
		System.out.println(links);
	}
	
	@Test
	public void fetchFullItem() throws Exception {
		String docKey = "PVCSG-SRS-20033";
		Map<String, Object> rawItem = jama.getJamaItem(docKey);
		assert(((List) rawItem.get("downstream")).size() == 7);
		assert(((List) rawItem.get("upstream")).size() == 2);
	}
	
	
	
	@Test
	public void fetchFullViaId() throws Exception {
		int id = 7187508;
		Map<String, Object> rawItem = jama.getJamaItem(id);
		System.out.println(rawItem);
	}
	
    @Test
    public void testFetchViaFilter() throws Exception {
    	int filterId = 60181;
    	
    	final ObjectMapper mapper = new ObjectMapper();
    	final File outputFile = new File("jamaitems.ldjson");    	
    	FileOutputStream fos = new FileOutputStream(outputFile, true);
    	final JsonGenerator g = mapper.getFactory().createGenerator(fos);    	
    	
    	try (SequenceWriter seq = mapper.writer()
    			  .withRootValueSeparator("\n") // Important! Default value separator is single space    			  
    			  .writeValues(g)) {    			     
    		List<Map<String, Object>> items = jama.getItemsViaFilter(filterId);
        	System.out.println(items);
        	items.stream().forEach(entry -> {
        		try {					
        			seq.write(entry);
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}) ;	
        	}
    	fos.write("\n".getBytes());
    	fos.flush();

   }
    
   @Test
   public void testCacheLoad() throws Exception {
	   List<Map<String, Object>> items = jama.readFromCacheItems();
	   items.stream().forEach(entry -> System.out.println(entry));
   }
    
//    @Test DOES NOT SCALE: 10-20sec for 50 relationsships each
//    public void fetchAllRelationships() throws Exception {
//    	List<Map<String, Object>> rels = jc.getAll("relationships?project=470&maxResults=50");
//    	File jsonOut = new File("./jamatraces.json");
//    	new ObjectMapper().writeValue(jsonOut, rels);
//    	
//    }
    
	 public static void writeToFile(String jsonAsString, String filename) {
		 try {
			Files.write(Paths.get(filename+".json"), jsonAsString.getBytes());
		} catch (IOException e) {			
			e.printStackTrace();
		}		 
	 }
}
