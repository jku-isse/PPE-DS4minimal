package at.jku.isse.designspace.azure;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import at.jku.isse.designspace.AzureConnDesignSpace;
import at.jku.isse.designspace.azure.service.AzureService;
import at.jku.isse.designspace.core.model.Instance;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=AzureConnDesignSpace.class)
class AzureServiceTests {

	@Autowired
	AzureService azureService;
	
	@Test
	void test() {
		Optional<Instance> itemOpt = azureService.transferAzureWorkItem("UserStudy1Prep", 875, true);
		assert(itemOpt.isPresent());
		Instance item = itemOpt.get();
		item.name();
	}

	@Test
	void refetchtest() {
		Optional<Instance> itemOpt = azureService.transferAzureWorkItem("UserStudy1Prep", 875, true);
		assert(itemOpt.isPresent());
		Instance item = itemOpt.get();
		item.name();
		
		Optional<Instance> itemOpt2 = azureService.transferAzureWorkItem("UserStudy1Prep", 875, false);
		assert(itemOpt2.isPresent());
	}
	
//	@Test
//	void extractSchema() throws JsonProcessingException {
//		 Folder typesFolder = WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER;
//	     Folder typeFolder = typesFolder.subfolder(AzureBaseElementType.AZURE_TYPES_FOLDER_NAME);
//	     Schema schema = new Schema(WorkspaceService.PUBLIC_WORKSPACE, Set.of(typeFolder.id().value()));
//	     XmlMapper mapper = new XmlMapper();
//	     String xml = mapper.writeValueAsString(schema);
//	     System.out.println(xml);
//	}
	
}
