package at.jku.isse.designspace.jama.service;

import at.jku.isse.designspace.core.model.*;
import at.jku.isse.designspace.jama.replaying.ChangeParser.CreationChange;
import at.jku.isse.designspace.jama.replaying.ChangeReplayer;
import at.jku.isse.designspace.jama.replaying.JamaActivity;
import at.jku.isse.designspace.jama.service.IJamaService;
import at.jku.isse.designspace.jama.service.IJamaService.JamaIdentifiers;
import at.jku.isse.designspace.jama.service.JamaService;
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
public class JamaReplayTest  {

    @Autowired
    UpdateManager updateManager;

    @Autowired
    UpdateMemory updateMemory;

    @Autowired
    JamaService jamaService;

    Workspace workspace;

    static Map<String, Set<String>> srs2sss = new HashMap<>();
    static Map<String, Set<String>> sss2srs = new HashMap<>();
    static Map<String, Instance> sssSet = new HashMap<>();
    static Map<String, Instance> srsSet = new HashMap<>();
    
    @Test
    public void testReplayAll() throws IOException {
    	Instance inst = this.jamaService.getJamaItem("10196957", JamaIdentifiers.JamaItemId).get();
    	workspace = inst.workspace;
    	InstanceType jamaType = workspace.debugInstanceTypeFindByName("jama_item");
    	Set<Instance> all = jamaType.instancesIncludingThoseOfSubtypes().collect(Collectors.toSet());
    	
    	Set<JamaActivity> history = jamaService.getCompleteHistory();
    	//jamaService.
    	ChangeReplayer replayer = new ChangeReplayer(all, history, jamaService.getJamaSchemaConverter(), jamaService);
    	long count = replayer.revertToBegin();
    	workspace.concludeTransaction();
    	
    	while (replayer.getDateOfNextFutureChange().isPresent()) {
    		JamaActivity change = replayer.applyNextForwardChange();
    		if (change instanceof CreationChange) {
    			System.out.println("Created "+change.getId());
    		}
    		workspace.concludeTransaction();
    	}
    	
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