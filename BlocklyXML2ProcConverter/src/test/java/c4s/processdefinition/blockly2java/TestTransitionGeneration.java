package c4s.processdefinition.blockly2java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;

import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import https.developers_google_com.blockly.xml.Xml;

class TestTransitionGeneration {

Xml2Java x2j = new Xml2Java();
	
	
	@Test
	void testConfig() throws Exception {
		String path = "./configexampleprocess.xml"; 
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> { 
				printWFD(wfd);
				printDataMappings(wfd);
				printTransition(t.getContext());
				});
		});	
	}


	@Test
	void testQAincluded() throws IOException, JAXBException {

		String path = "./qacheckshouldbeincluded.xml"; 
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> { 
				printWFD(wfd);
				printDataMappings(wfd);
				printTransition(t.getContext());
				
				});
		});		
	}
	
	
	@Test
	void testQAasMultiline() throws IOException, JAXBException {

		String path = "./testmultilineconstraint.xml"; 
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> { 
				printWFD(wfd);
				printDataMappings(wfd);
				printTransition(t.getContext());
				
				});
		});		
	}
	
	@Test
	void testDeriveOrigin() throws IOException, JAXBException {
		String path = "./sielaV3exp.xml"; 
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> { 
				printWFD(wfd);
				printDataMappings(wfd);
				printTransition(t.getContext());
				
				});
		});		
	}
	
	private void printTransition(ProcessingContext ctx) {
		ctx.getTask2transition().entrySet().stream()
		.forEach(entry -> {
			System.out.println("Task: "+entry.getKey());
			entry.getValue().stream().forEach(spec -> {
				System.out.println(" Transition: "+spec.stateId);
				spec.var2source.entrySet().stream().forEach(entry2 -> {
					System.out.println(" ArtUsed: "+entry2.getKey() +" via "+entry2.getValue());
					spec.varFirstSource.stream()
						.filter(orig -> orig.localRoleId.equals(entry2.getKey()) )
						.findAny()
						.ifPresent(artOrig -> 
							System.out.println(String.format("   originated at %s as %s via %s", artOrig.originTaskId, artOrig.roleId, artOrig.location )));
				});
			});
		});
	}
	
	private void printWFD(DTOs.Process wfd) {		
		System.out.println(wfd.toString());
		wfd.getSteps().stream().forEach(td -> System.out.println(td.toString()));
		wfd.getDns().stream().forEach(dnd -> System.out.println(dnd.toString()));
	}

	private void printDataMappings(DTOs.Process wfd) {
		wfd.getDns().stream()
			.flatMap(dnd -> dnd.getMapping().stream())
			.forEach(mapping -> System.out.println(String.format("M: %s : %s -> %s : %s", mapping.getFromStep(), mapping.getFromParam() , mapping.getToStep(), mapping.getToParam()))) ;
	}
	

	
}
