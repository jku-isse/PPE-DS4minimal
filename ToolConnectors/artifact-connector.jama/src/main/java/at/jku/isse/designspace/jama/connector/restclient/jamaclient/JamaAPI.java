package at.jku.isse.designspace.jama.connector.restclient.jamaclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;

import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry.Status;
import at.jku.isse.designspace.jama.connector.restclient.httpconnection.JamaClient;
import at.jku.isse.designspace.jama.model.JamaBaseElementType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JamaAPI extends SchemaProvider implements IJamaAPI {

	JamaClient jc;
	IProgressObserver obs;
	

    public JamaAPI(JamaClient jc, IProgressObserver obs) {
		super(jc, obs);
		this.jc = jc;
		this.obs = obs;
		
	}

	@Override
    public Map<String, Object> getJamaItem(String docKey) {
		Map<String, Object> wrapper = new HashMap<>();
		ProgressEntry pe = dispatchNewStartedActivity("Fetching Item for key: "+docKey);
		try {									
			List<Map<String, Object>> candidates =  jc.getAll("abstractitems?contains=%22documentKey%3A"+docKey+"%22");
		//	List<Map<String, Object>> candidates =  jc.getAll("abstractitems?documentKey="+docKey);
			if (candidates.size() > 0) {
				
				Map<String, Object> item = candidates.get(0);
				pe.setStatusAndComment(Status.InProgress, "Details fetched");				
				wrapper.put("data", item);
				Integer id = Integer.parseInt(item.get("id").toString());
				
				wrapper.put("upstream", getUpstreamIds(id));			
				wrapper.put("downstream", getDownstreamIds(id));				
			}
		} catch (Exception e) {			
			pe.setStatusAndComment(Status.Failed, e.getMessage());
			e.printStackTrace();
			return Collections.emptyMap();  			
		}
		pe.setStatus(Status.Completed);
		return wrapper;
    }	

    @Override
    public Map<String, Object> getJamaItem(int id) {
    	Map<String, Object> wrapper = new HashMap<>();
    	ProgressEntry pe = dispatchNewStartedActivity("Fetching Item for id: "+id);
		try {						
			Map<String, Object> item =  jc.getResource("items/"+id);	
			pe.setStatusAndComment(Status.InProgress, "Details fetched");		
			wrapper.put("data", item.get("data"));
			wrapper.put("upstream", getUpstreamIds(id));			
			wrapper.put("downstream", getDownstreamIds(id));
		} catch (Exception e) {	
			pe.setStatusAndComment(Status.Failed, e.getMessage());
			e.printStackTrace();
			return Collections.emptyMap();  
		}
		pe.setStatus(Status.Completed);
		return wrapper;    	    	    	    
    }
    
    public List<Integer> getDownstreamIds(int jamaId) throws Exception{
    	ProgressEntry peDown = dispatchNewStartedActivity("Fetching downstream items for: "+jamaId);
    	List<Map<String, Object>> rels;		
    	rels = jc.getAll("items/"+jamaId+"/downstreamrelationships?maxResults=50");
    	peDown.setStatus(Status.Completed);
    	return rels.stream()
    			.map(rel -> (Integer)rel.get("toItem"))
    			.collect(Collectors.toList());		
    }
    
    public List<Integer> getUpstreamIds(int jamaId) throws Exception{    	
    	ProgressEntry peUp = dispatchNewStartedActivity("Fetching upstream items for: "+jamaId);
    	List<Map<String, Object>> rels;		
    	rels = jc.getAll("items/"+jamaId+"/upstreamrelationships?maxResults=50");
    	peUp.setStatus(Status.Completed);
    	return rels.stream()
    			.map(rel -> (Integer)rel.get("fromItem"))
    			.collect(Collectors.toList());		
    }
    
    @Override
	public List<Map<String, Object>> getItemsViaFilter(int filterId) throws Exception{
    	ProgressEntry pe = dispatchNewStartedActivity("Fetching Items for filter: "+filterId);
    	List<Map<String, Object>> result = jc.getAll("filters/"+filterId+"/results?maxResults=50");    	
    	List<Map<String, Object>> items = new ArrayList<>();
    	pe.setStatusAndComment(Status.InProgress, String.format("Details fetched for %s items", result.size()));	
    	result.stream().forEach(entry -> {
		try {
			Map<String, Object> wrapper = new HashMap<>();					
			wrapper.put("data", entry);
			Integer id = Integer.parseInt(entry.get("id").toString());
			wrapper.put("upstream", getUpstreamIds(id));			
			wrapper.put("downstream", getDownstreamIds(id));
			items.add(wrapper);
		} catch (Exception e) {			
			pe.setStatusAndComment(Status.Failed, e.getMessage());
			e.printStackTrace();			 
		}
    	});
    	pe.setStatus(Status.Completed);
		return items;    	
    }
    
    public void writeToCacheItems(List<Map<String, Object>> items) throws Exception{
    	final ObjectMapper mapper = new ObjectMapper();
    	final File outputFile = new File("jamaitems.ldjson");    	
    	try(FileOutputStream fos = new FileOutputStream(outputFile, true);
    			JsonGenerator g = mapper.getFactory().createGenerator(fos);) {    		
    		try (SequenceWriter seq = mapper.writer()
    				.withRootValueSeparator("\n") // Important! Default value separator is single space    			  
    				.writeValues(g)) {    			         		
    			System.out.println(items);
    			long success = items.stream().map(entry -> {
    				try {					
    					seq.write(entry);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    				return true;
    			})
    			.filter(b -> b == true)
    			.count();	
    			if (success > 0)
    				fos.write("\n".getBytes());
    			fos.flush();	
    		}

    	};
    }
    
    public List<Map<String, Object>> readFromCacheItems() throws Exception{
    	List<Map<String,Object>> items = new ArrayList<>();
    	final ObjectMapper mapper = new ObjectMapper();
    	final File inputFile = new File("jamaitems.ldjson");
    	try (MappingIterator<Map<String, Object>> it = mapper.readerFor(Map.class)
    			.readValues(inputFile)) {
    		while (it.hasNextValue()) {
    			items.add(it.nextValue());
    		}    		
    	}
    	return items;
    }
    
    
    @Override
    public Map<String, Object> getAllItemsMappedToKey() {
        log.warn("getAllItemsMappedToKey() not available for live server access");
    	return Collections.emptyMap();    	
    }

    @Override
    public Map<Integer, Object> getAllItemsMappedToId() {
    	log.warn("getAllItemsMappedToId() not available for live server access");
    	return Collections.emptyMap();    
    }

    @Override
    public Map<String, Object> getJamaUser(int userId) {
//    	try { //DISABLED FOR ANONYMITY
//			Map<String, Object> user = jc.getResource("users/"+userId);
//			return user;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    	//return null;
    	Map<String, Object> user = new HashMap<>();
    	user.put("id", userId);
    	user.put(JamaBaseElementType.USERNAME, userId+"");
    	return user;    	 
    }

    @Override
    public Map<String, Object> getJamaProject(int projectId) {
    	try {
			Map<String, Object> proj = jc.getResource("projects/"+projectId);			
			return (Map<String, Object>) proj.get("data");
		} catch (Exception e) {
			e.printStackTrace();
		}     
    	return null; 
    }

    @Override
    public Map<String, Object> getPickList(int pickListId) {
        Object pickList = idToPickList.get(pickListId);
        if (pickList != null) {
            return (Map<String, Object>) pickList;
        }
        return null;
    }

    @Override
    public Map<Integer, Object> getAllPickLists() {
        return this.idToPickList;
    }

    @Override
    public Map<String, Object> getItemType(int itemTypeId) {
        return (Map<String, Object>) this.idToItemType.get(itemTypeId);
    }

    @Override
    public Map<Integer, Object> getAllItemTypes() {
        return this.idToItemType;
    }

    @Override
    public Map<String, Object> getPickListOption(int pickListId, int optionId) {
        Object pickListOption = idToPickListOption.get(pickListId);
        if (pickListOption != null) {
            return (Map<String, Object>) pickListOption;
        }
        return null;
    }

    @Override
    public Map<Integer, Object> getAllPickListOptions() {
        return this.idToPickListOption;
    }

    @Override
    public Map<String, Object> getRelease(int releaseId) {
    	try {
			Map<String, Object> release = jc.getResource("releases/"+releaseId);
			return release;
		} catch (Exception e) {
			e.printStackTrace();
		}     
    	return null; 
    }

	@Override
	public Map<Integer, Object> getAllReleasesMappedToId() {
		log.warn("getAllReleasesMappedToId() not available for live server access");
    	return Collections.emptyMap();   
	}

	@Override
	public Map<Integer, Object> getAllProjectsMappedToId() {
		log.warn("getAllProjectsMappedToId() not available for live server access");
    	return Collections.emptyMap();   
	}
}
