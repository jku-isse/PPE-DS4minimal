package at.jku.isse.designspace.jama;

import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.artifactconnector.core.updateservice.UpdateManager;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.jama.service.IJamaService;
import at.jku.isse.designspace.jama.service.IJamaService.JamaIdentifiers;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JamaManualPollTest {

	@Autowired
    UpdateManager updateManager;

    @Autowired
    UpdateMemory updateMemory;

    @Autowired
    IJamaService jamaService;

    Workspace workspace;
    
    @Test
    public void testFetchAndWaitForUpdate() throws IOException {
    	Instance inst = this.jamaService.getJamaItem("7230585", JamaIdentifiers.JamaItemId).get();
    	Scanner scanner = new Scanner(System.in);
    	scanner.nextLine();
    }
    
	@SuppressWarnings("unchecked")
	@Test
	public void fetchProjects() throws Exception {
		
		//Instance inst = this.jamaService.getJamaItem("7114578", JamaBaseElementType.ID).get();
//		Instance inst = this.jamaService.getJamaItem("PVCSG-SSS-17222", JamaBaseElementType.DOCUMENTKEY).get();
//		inst.getPropertyAsSet(JamaBaseElementType.UPSTREAM).get().stream()
//			.map(upinst -> this.jamaService.getJamaItem((String) ((Instance)upinst).getPropertyAsValue(BaseElementType.ID), BaseElementType.ID).get() )
//			.peek(upinst -> System.out.println(((Instance)upinst).name()))
//			.map(upinst -> ((Instance)upinst).getPropertyAsInstance(JamaBaseElementType.PROJECT))
//			.filter(Objects::nonNull)
//			.forEach(proj -> System.out.println(((Instance)proj).id()));
	}
}
