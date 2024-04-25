package at.jku.isse.designspace.jama.connector.restclient.jamaclient;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry.Status;
import at.jku.isse.designspace.jama.connector.restclient.httpconnection.JamaClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaProvider {
	String schemaFile = "./jamaschema.json";
	
	protected Map<Integer, Object> idToItemType = new HashMap<>();
    protected Map<Integer, Object> idToPickList = new HashMap<>();
    protected Map<Integer, Object> idToPickListOption = new HashMap<>();
    
    IProgressObserver obs;
	
	public SchemaProvider(JamaClient jc, IProgressObserver obs) {
		this.obs = obs;
		Map<String, Object> schema;
		try {
			schema = getSchemaAsJsonFromFile();
			log.info("Loading Schema from file");
			dispatchAtomicFinishedActivity("Loaded Schema from file");						
		} catch( Exception e) {
			log.error("Error loading schema from file: "+e.getMessage());
			if (jc != null) { // offline use only
				ProgressEntry peLoad = dispatchNewStartedActivity( "Loading Schema from Server");
				try {						
					schema = getSchemaAsJsonFromServer(jc);
					peLoad.setStatus(Status.Completed);
				} catch (Exception e1) {
					peLoad.setStatusAndComment(Status.Failed, e.getMessage());
					log.error("Error loading schema from server: "+e.getMessage());
					return;
				}
				try {
					storeSchemaAsJson(schema);
				} catch (Exception e1) {
					log.warn("Error storing schema from server in file: "+e.getMessage()); // we continue but load next time again from server
				}				
			} else {
				log.error("Error no jama client provided, hence unable to load schema on the fly.");
				return;
			}			
		}
		importSchema(schema);		
	}
	
	private Map<String, Object> getSchemaAsJsonFromFile() throws Exception {
		String json = new String(Files.readAllBytes(Paths.get(schemaFile)));
		Map<String, Object> schema = new ObjectMapper().readValue(json, Map.class);
		return schema;
	}
	
	
	private Map<String, Object> getSchemaAsJsonFromServer(JamaClient jc) throws Exception {			
		Map<String, Object> schema = new HashMap<>();
		ProgressEntry peTypes = dispatchNewStartedActivity( "Loading ItemTypes from Server");
		List<Map<String, Object>> itemTypes = jc.getAll("itemtypes?maxResults=50");
		peTypes.setStatus(Status.Completed);
		schema.put("types", itemTypes);
		ProgressEntry pePL = dispatchNewStartedActivity( "Loading Picklist from Server");
		List<Map<String, Object>> picklists = jc.getAll("picklists?maxResults=50");
		schema.put("picklists", picklists);
		pePL.setStatus(Status.Completed);
		ProgressEntry peOpt = dispatchNewStartedActivity( "Loading PicklistOptions from Server");
		List<Map<String, Object>> options = new LinkedList<>();
		for (Object pl : picklists) {
            Map<String, Object> data = (Map<String, Object>) pl;            
            Integer id = Integer.parseInt(data.get("id").toString());
            List<Map<String, Object>> optionSet = jc.getAll("picklists/"+id+"/options");
            options.addAll(optionSet);
        }
		peOpt.setStatus(Status.Completed);
		schema.put("options", options);		
		return schema;
	}
	
	private void storeSchemaAsJson(Map<String, Object> schema) throws Exception {
		String json = new ObjectMapper().writeValueAsString(schema);
		FileWriter fileWriter = new FileWriter(schemaFile);
		fileWriter.write(json);
		fileWriter.flush();
		fileWriter.close();
	}
	
	private void importSchema(Map<String, Object> schema) {
		
		List<Object> types = (List<Object>) schema.get("types");
        for (Object type : types) {
            Map<String, Object> data = (Map<String, Object>) type;            
            Integer id = Integer.parseInt(data.get("id").toString());
            this.idToItemType.put(id, data);
        }
        
        List<Object> picklists = (List<Object>) schema.get("picklists");
        for (Object pl : picklists) {
            Map<String, Object> data = (Map<String, Object>) pl;            
            Integer id = Integer.parseInt(data.get("id").toString());
            this.idToPickList.put(id, data);
        }
        
        List<Object> options = (List<Object>) schema.get("options");
        for (Object opt : options) {
            Map<String, Object> data = (Map<String, Object>) opt;            
            Integer id = Integer.parseInt(data.get("id").toString());
            this.idToPickListOption.put(id, data);
        }
	}
	
	protected ProgressEntry dispatchNewStartedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("JamaConnector", activity, Status.Started);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}
	
	protected ProgressEntry dispatchAtomicFinishedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("JamaConnector", activity, Status.Completed);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}
}
