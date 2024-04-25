package at.jku.isse.designspace.jama;

import at.jku.isse.designspace.core.model.*;
import at.jku.isse.designspace.jama.service.IJamaService;
import at.jku.isse.designspace.jama.service.IJamaService.JamaIdentifiers;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.artifactconnector.core.updateservice.UpdateManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JamaExtractAllRequirementsTest  {

    @Autowired
    UpdateManager updateManager;

    @Autowired
    UpdateMemory updateMemory;

    @Autowired
    IJamaService jamaService;

    Workspace workspace;

    static Map<String, Set<String>> srs2sss = new HashMap<>();
    static Map<String, Set<String>> sss2srs = new HashMap<>();
    static Map<String, Instance> sssSet = new HashMap<>();
    static Map<String, Instance> srsSet = new HashMap<>();
    
    @Test
    public void testFetchAll() throws IOException {
    	Instance inst = this.jamaService.getJamaItem("10196957", JamaIdentifiers.JamaItemId).get();
    	workspace = inst.workspace;
    	
    	InstanceType jamaType = workspace.debugInstanceTypeFindByName("SSS");
    	Set<Instance> all = jamaType.instances().get();
    	all.stream()    	
    		.filter(sss -> getDownstreamSRS(sss).size() > 0)
    		.peek(sss -> System.out.println("----------"))
    		.peek(sss -> System.out.println("SSS "+sss.name()+" ("+sss.getPropertyAsValue("key")+") : "+plainTextFromHtml(sss.getPropertyAsSingle("description"))))
    		.flatMap(sss -> getDownstreamSRS(sss).stream())
    		.peek(srs -> calcSRS2SSS(srs))
    		.forEach(srs -> System.out.println("SRS "+srs.name()+" ("+srs.getPropertyAsValue("key")+") : "+plainTextFromHtml(srs.getPropertyAsSingle("description"))));
    	
    	System.out.println("----------");	
    	printSRSwithMultipleUpstreamSSS();     	
    	
    }

    @Test
    public void testAccessNonexistingProperty() throws IOException {
    	Instance inst = this.jamaService.getJamaItem("10196957", JamaIdentifiers.JamaItemId).get();
    	workspace = inst.workspace;
    	
    	Set<Instance> downstream = (Set<Instance>) inst.getPropertyAsSet("downstream").get();
    	downstream.stream()
    		.map(down -> down.getPropertyAsValueOrElse("XXX", () -> "UNKNOWN"))
    		.forEach(value -> System.out.println("Value of XXX is: "+value));    		    	
    	System.out.println("----------");	
    	     	
    	
    }
   
    
    private static void printSRSwithMultipleUpstreamSSS() {
    	srs2sss.entrySet().stream()
    		.filter(entry -> entry.getValue().size() > 1)
    		.forEach(entry -> {     			
    			String sameSRS = havePairwiseSameSRS(List.copyOf(entry.getValue())) ? "all the same" : "different";
    			System.out.println(String.format("%s : %s with SSS containing %s downstream SRS items ", entry.getKey(), entry.getValue(), sameSRS)); 
    			// now check pairwise if SSS have same SRS set, or not    			    		
    		});
    }
    
    private static boolean havePairwiseSameSRS(List<String> sssSet) {    	
    	for (int i = 0; i<sssSet.size()-1; i++) {    		
    		for (int j = i+1; j < sssSet.size(); j++) {
    			Set<String> sssI = sss2srs.get(sssSet.get(i));
    			Set<String> sssJ = sss2srs.get(sssSet.get(j));
    			if (!Sets.symmetricDifference(sssI, sssJ).isEmpty()) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    
    private static void calcSRS2SSS(Instance srs) {
    	srsSet.putIfAbsent((String)srs.getPropertyAsValue("key"), srs);
    	
    	String srsId = (String) srs.getPropertyAsValue("key");
    	Set<Instance> sss = getUpstream(srs, "SSS");
    	sss.stream().forEach(sssInst -> {
    		String sssId = (String) sssInst.getPropertyAsValue("key");
    		srs2sss.computeIfAbsent(srsId, k -> new HashSet<String>()).add(sssId);
    		// add also inverse:
        	sss2srs.computeIfAbsent(sssId, k -> new HashSet<String>()).add(srsId);
    	});    	    	    	
    }
    

    private static Set<Instance> getDownstreamSRS(Instance sss) {
    	sssSet.putIfAbsent((String)sss.getPropertyAsValue("key"), sss);
    	Set<Instance> downstream = (Set<Instance>) sss.getPropertyAsSet("downstream").get();
    	return downstream.stream()
    		.filter(inst -> inst.getPropertyAsValue("typeKey").equals("SRS"))
    		.collect(Collectors.toSet());    	
    }
    
    private static Set<Instance> getUpstream(Instance jamaItem, String type) {
    	Set<Instance> downstream = (Set<Instance>) jamaItem.getPropertyAsSet("upstream").get();
    	return downstream.stream()
    		.filter(inst -> inst.getPropertyAsValue("typeKey").equals(type))
    		.collect(Collectors.toSet());    	
    }
    
    
    private static String plainTextFromHtml(SingleProperty prop) {
    	 return (String)prop.getValue();
    }


}