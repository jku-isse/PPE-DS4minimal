package at.jku.isse.designspace.jira.service;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.events.PropertyUpdateSet;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.jira.model.JiraBaseElementType;
import at.jku.isse.designspace.jira.service.IJiraService.JiraIdentifier;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@DependsOn({"controleventengine"})
@ConditionalOnExpression(value = "${jira.enabled:false}") 
public class JiraEpicLinkItemAugmentor implements ServiceProvider {


	@Autowired
	IJiraService jiraService;
	
//	@Autowired 
//	WorkspaceService workspaceService;
		
	public static final String JIRA_EPIC_CHILD_PARENTREF = "epicLink"; //FIXME this is hardcoded, very ugly
		
	private Workspace ws;
		
	public JiraEpicLinkItemAugmentor() {		
		ServiceRegistry.registerService(this);		
	}
		
	public void initialize() {
		this.ws = WorkspaceService.PUBLIC_WORKSPACE;
		//the epic link is defined by default, no need to modify the schema here anywhere
		Workspace.serviceProviders.add(this);
	}
	
	@Override
	public String getName() {		
		return "JiraEpicLinkAugmentor";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public int getPriority() {
		return 91;
	}

	@Override
	public boolean isPersistenceAware() {
		return true;
	}

	@Override
	public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
		if (workspace != ws) return;
		operations.stream().forEach(op -> {
			if (op instanceof PropertyUpdateSet ) {
				handlePropertyUpdateSet((PropertyUpdateSet) op);
			}
		});				
	}

	
	private void handlePropertyUpdateSet(PropertyUpdateSet op) { // we only set from the jira side
		if (op.name().equalsIgnoreCase(JIRA_EPIC_CHILD_PARENTREF)) {
			Instance inst = ws.findElement(op.elementId());
			if (inst.hasProperty(JIRA_EPIC_CHILD_PARENTREF)) {
				setJiraParentEpicLink(inst, op.value());
			}
		} 
	}
	
	private void setJiraParentEpicLink(Instance jiraItem, Object jiraEpicKey) {
		if (jiraEpicKey == null || jiraEpicKey.toString().length() == 0) {
			// remove
			jiraItem.getProperty(JiraBaseElementType.EPICPARENT).set(null);
		} else {			
			try {
				// or resolve it to a jira item
				Optional<Instance> jiraInstOpt = jiraService.getArtifact(jiraEpicKey.toString(), JiraIdentifier.JiraIssueKey, false);
				if (jiraInstOpt.isPresent()) {
					// and set it to the link property
					jiraItem.getProperty(JiraBaseElementType.EPICPARENT).set(jiraInstOpt.get());
				} else {
					log.debug(jiraEpicKey + " could not be resolved to a jira item");
				}
			} catch (Exception ie) {
				log.debug(jiraEpicKey + " could not be assigned as epic parent "+ie.getMessage());
			}
		}
	}
	
		



}