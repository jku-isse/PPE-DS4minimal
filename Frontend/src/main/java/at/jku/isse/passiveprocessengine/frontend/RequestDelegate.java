package at.jku.isse.passiveprocessengine.frontend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import at.jku.isse.designspace.artifactconnector.core.repository.ArtifactIdentifier;
import at.jku.isse.designspace.core.model.Instance;

import at.jku.isse.designspace.core.model.User;
import at.jku.isse.designspace.core.model.Workspace;

import at.jku.isse.designspace.rule.checker.ArlRuleEvaluator;
import at.jku.isse.designspace.rule.service.RuleService;
import at.jku.isse.passiveprocessengine.definition.activeobjects.ProcessDefinition;
import at.jku.isse.passiveprocessengine.definition.serialization.ProcessRegistry;
import at.jku.isse.passiveprocessengine.frontend.artifacts.ArtifactResolver;
import at.jku.isse.passiveprocessengine.frontend.security.persistence.ProcessProxyRepository;
import at.jku.isse.passiveprocessengine.frontend.security.persistence.RestrictionProxy;
import at.jku.isse.passiveprocessengine.frontend.security.persistence.RestrictionProxyRepository;
import at.jku.isse.passiveprocessengine.frontend.ui.IFrontendPusher;
import at.jku.isse.passiveprocessengine.frontend.ui.utils.UIConfig;
import at.jku.isse.passiveprocessengine.instance.ProcessException;

import at.jku.isse.passiveprocessengine.instance.ProcessInstanceError;
import at.jku.isse.passiveprocessengine.instance.activeobjects.ProcessInstance;
import at.jku.isse.passiveprocessengine.instance.messages.Commands.ProcessScopedCmd;
import at.jku.isse.passiveprocessengine.instance.messages.EventDistributor;
import at.jku.isse.passiveprocessengine.instance.messages.Responses.IOResponse;

import at.jku.isse.passiveprocessengine.monitoring.UsageMonitor;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class RequestDelegate {


	@Autowired
	UIConfig uiconfig;
	
	@Autowired
	ArtifactResolver resolver;
	
	@Autowired
	ProcessRegistry procReg;
	
	@Autowired IFrontendPusher frontend;
	
	@Autowired EventDistributor eventDistributor;
	
	//@Autowired RepairAnalyzer repAnalyzer;
	
	@Autowired UsageMonitor monitor;
	
	@Autowired ProcessProxyRepository processACL;
	
	@Autowired RestrictionProxyRepository restrictionACL;
	
	@Autowired
	private ProcessConfigBaseElementFactory configFactory;
	
	ProcessChangeListenerWrapper picp;
	
	boolean isInitialized = false;
	
	public RequestDelegate() {
		
	}
	
    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
       initialize();
    }
	
	public ProcessRegistry getRegistry() {
		if (!isInitialized) initialize();
		return procReg;
	}
	
	public Workspace getWorkspace() {
		if (!isInitialized) initialize();
		return ws;
	}
	
	public ArtifactResolver getArtifactResolver() {
		if (!isInitialized) initialize();
		return resolver;
	}
	
	public UsageMonitor getMonitor() {
		return monitor;
	}
	
//	public ProcessConfigBaseElementFactory getProcessConfigTypeFactory() {		
//		return configFactory;
//	}
	
	public IFrontendPusher getFrontendPusher() {
		return frontend;
	}
	
	public EventDistributor getEventDistributor() {
		return eventDistributor;
	}
	
	public ProcessInstance instantiateProcess(String procName, Map<String, ArtifactIdentifier> inputs, String procDefinitionId ) throws ProcessException{
		if (!isInitialized) initialize();
		
		Optional<ProcessDefinition> optProcDef = procReg.getProcessDefinition(procDefinitionId, true);
		if (optProcDef.isEmpty()) {
			String msg = String.format("ProcessDefinition %s not found", procDefinitionId);
			log.warn(msg);
			throw new ProcessException(msg);
		}
	
		List<ProcessException> pexs = new LinkedList<>();
		Map<String, Set<Instance>> procInput = new HashMap<>();
		inputs.entrySet().stream()
				.forEach(entry -> {
						Instance inst;
						try {
							inst = resolver.get(entry.getValue());
							procInput.put(entry.getKey(), Set.of(inst));
						} catch (ProcessException e) {
							pexs.add(e);
						}
				});
		if (pexs.size() > 0) {
			ProcessException pe = new ProcessException("Error resolving artifacts");
			pexs.stream().forEach(pex -> pe.getErrorMessages().add(pex.getMainMessage()));
			throw pe;
		}
		SimpleEntry<ProcessInstance,List<ProcessInstanceError>> pd = procReg.instantiateProcess(optProcDef.get(), procInput);
		if (pd.getValue().isEmpty())
			return pd.getKey();
		else
			throw new ProcessException(pd.getValue().toString());		
	}
	
	public void addProcessInput(ProcessInstance pInst, String inParam, String artId, String artIdType) throws ProcessException{
		Instance inst = resolver.get(new ArtifactIdentifier(artId, artIdType));
		IOResponse resp = pInst.addInput(inParam, inst);
		if (resp.getError() != null) {
			throw new ProcessException(resp.getError());
		} else {
			ws.concludeTransaction();
		}
	}
	
	private void addIO(boolean isInput, String procId, String stepId, String param, String artId, String artType) throws ProcessException {
		if (!isInitialized) initialize();
		ProcessException pex = new ProcessException("Error adding i/o of step: "+stepId);
	//	ProcessInstance pi = pInstances.get(procId);
		ProcessInstance pi = procReg.getProcess(procId);
		if (pi != null) {
			pi.getProcessSteps().stream()
				.filter(step -> step.getName().equals(stepId))
				.findAny().ifPresent(step -> {
					try {
						Instance inst = resolver.get(new ArtifactIdentifier(artId, artType));
						IOResponse resp = isInput ? step.addInput(param, inst) : step.addOutput(param, inst);
						if (resp.getError() != null)
							pex.getErrorMessages().add(resp.getError());
					} catch (ProcessException e) {
						pex.getErrorMessages().add(e.getMessage());
					}
				});
		} else {
			pex.getErrorMessages().add("Process not found by id: "+procId);
		}
		if (pex.getErrorMessages().size() > 0)
			throw pex;
		else {
			ws.concludeTransaction();
			//ws.commit();
			//fetchLazyLoaded();
		}
	}
	
	public void addInput(String procId, String stepId, String param, String artId, String artType) throws ProcessException {
		if (!isInitialized) initialize();
		addIO(true, procId, stepId, param, artId, artType);
	}
	
	public void addOutput(String procId, String stepId, String param, String artId, String artType) throws ProcessException {
		if (!isInitialized) initialize();
		addIO(false, procId, stepId, param, artId, artType);
	}
	
	public void removeInputFromProcess(ProcessInstance proc, String inParam, String artId) {
		Set<Instance> inputs = proc.getInput(inParam);
		inputs.stream()
			.filter(art -> art.name().equalsIgnoreCase(artId))
			.findAny()
			.ifPresent(art -> {
				proc.removeInput(inParam, art);
				ws.concludeTransaction();
			});
		
	}
	
	public ProcessInstance getProcess(String id) {
		return procReg.getProcess(id);
		//return pInstances.get(id);
	}
	
	public void deleteProcessInstance(String id) {
		if (!isInitialized) initialize();
		frontend.remove(id);
		procReg.removeProcess(id);
	}

	public void initialize() {
		if (isInitialized)
			return;
		//Tool tool = new Tool("PPEv3", "v1.0");
		//ws = WorkspaceService.createWorkspace("PPEv3", WorkspaceService.PUBLIC_WORKSPACE, WorkspaceService.ANY_USER, tool, true, false);
		ws = WorkspaceService.PUBLIC_WORKSPACE;
		
		//configFactory = new ProcessConfigBaseElementFactory(ws, "processConfigTypes");
		
		resolver.inject(ws);
		procReg.inject(ws, configFactory);
		if (repAnalyzer != null)
			repAnalyzer.inject(ws);
		ArlRuleEvaluator arl = new ArlRuleEvaluator();
		arl.registerListener(repAnalyzer);
		RuleService.setEvaluator(arl);
		RuleService.currentWorkspace = ws;		
		
		// load any persisted process instances
		loadPersistedProcesses();		
		picp = new ProcessChangeListenerWrapper(ws, frontend, resolver, eventDistributor);
		WorkspaceListenerSequencer wsls = new WorkspaceListenerSequencer(ws);
		//if (repAnalyzer != null)
		//	wsls.registerListener(repAnalyzer);
		wsls.registerListener(picp);
		
		 
		
		isInitialized = true;
	}
	
	public ProcessChangeListenerWrapper getProcessChangeListenerWrapper() {
		// to enable automatic lazyloading from ARL playground
		return picp;
	}
	
	public UIConfig getUIConfig() {
		return uiconfig;
	}
	
	public void ensureConstraintStatusConsistency(ProcessInstance proc) {
		log.info("ConstraintStatus Consistency check requested for process instance: "+proc.getName());
		List<ProcessScopedCmd> incons = proc.ensureRuleToStateConsistency();
		picp.executeConstraintStateInconsistencyRepairingCommands(incons);
	}
	
	public int resetAndUpdate() { // just a quick hack for now.
		return picp.resetAndUpdate();
	}
	
	private void loadPersistedProcesses() {
		Set<ProcessInstance> existingPI = procReg.loadPersistedProcesses();
		frontend.update(existingPI);
	}
	
	public void dumpDesignSpace() {
		Optional<String> dumpOpt = ControlEventEngine.exportAsString();
		if (dumpOpt.isPresent()) {
			Path path  = Paths.get("./dump.txt");	
			try {
				Files.writeString(path, dumpOpt.get(),
						StandardCharsets.UTF_8);
			}
			catch (IOException ex) {
				// Print messqage exception occurred as
				// invalid. directory local path is passed
				log.error(ex.getMessage());
			}
		}
	}
	
	public boolean doShowRestrictions(ProcessInstance proc) {
		if (proc == null)
			return true;
		else {
			//if (doShowRepairs(proc)) // shortcut, as we only show repairtree when repairs are enabled, we dont need to check here again	
				return restrictionACL.findAll().stream().anyMatch(proxy -> proxy.getName().equalsIgnoreCase(proc.getDefinition().getName()+RestrictionProxy.RESTRICTION_SELECTOR)
																		|| proxy.getName().equalsIgnoreCase("*"));
			//else
			//	return false;
		}
	}
	
	public boolean doShowRepairs(ProcessInstance proc) {
		if (proc == null)
			return true;
		else
			return restrictionACL.findAll().stream().anyMatch(proxy -> proxy.getName().equalsIgnoreCase(proc.getDefinition().getName()+RestrictionProxy.REPAIR_SELECTOR)
																	|| proxy.getName().equalsIgnoreCase("*") 
																	|| proxy.getName().equalsIgnoreCase("+"));				
	}
	
	public boolean doAllowProcessInstantiation(String procInputId) {
		List<String> accessTo = processACL.findAll().stream().map(pp -> pp.getName()).collect(Collectors.toList());
		
		return processACL.findAll().stream().anyMatch(proxy -> proxy.getName().equalsIgnoreCase(procInputId) 
															|| proxy.getName().equalsIgnoreCase("*"));
	}

	public String isAllowedAsNextProc(String procDefId, String userId) {
		if (processACL.findAll().stream().anyMatch(entry -> entry.getName().equalsIgnoreCase("*")))
			return procDefId;

		Optional<List<String>> orderOpt = processACL.findAll().stream()
				.filter(entry -> entry.getName().contains("::"))
				.map(entry -> entry.tokenize())
				.findAny();
		if (orderOpt.isEmpty() || userId == null) return null;		
		
		List<String> order = orderOpt.get();
		// check if that procDef has already been instantiated before, if so, then deny and search next 
		Optional<Instance> procInst = findAnyProcessInstanceByDefinitionAndOwner(procDefId, userId);
		if (procInst.isPresent())
			return "Process already (previously) instantiated, cannot instantiate again";
		
		int pos = order.indexOf(procDefId);		
		// or if this is first one	
		if (pos == 0) return procDefId; // all ok, good to go
		if (pos > 0) {
			String prevProcDef = order.get(pos-1);
			Optional<Instance> prevInst = findAnyProcessInstanceByDefinitionAndOwner(prevProcDef, userId);
			// if not yet instantiated, check if prior one has been closed
			if (prevInst.isPresent()) {
				Instance prevP = prevInst.get();
				if (prevP.isDeleted)
					return procDefId; // all good to go
				else
					return "Previous process "+prevProcDef+" is not completed (and deleted) yet";
			} else {
				return "Previous process "+prevProcDef+" is not instantiated yet";
			}				 
		}
		return "You are not allowed to instantiate this process";
	}
	
	public Optional<Instance> findAnyProcessInstanceByDefinitionAndOwner(String processDefinition, String owner) {
		//InstanceType procType =	ProcessInstance.getOrCreateDesignSpaceInstanceType(ws, procReg.getProcessDefinition(processDefinition, true).get());
		// instances() does not return deleted instances!!
		return procReg.getExistingAndPriorInstances().stream()
			.filter(proc -> proc.getDefinition().getName().equals(processDefinition))
			.map(proc -> proc.getInstance())				
			.filter(instance -> isOwner(owner, instance))
			.findAny();		
	}
	
	private boolean isOwner(String userName, Instance instance) {
		return instance.getPropertyAsSet(ReservedNames.OWNERSHIP_PROPERTY).get().stream()
			.map(strId -> Long.parseLong((String)strId))
			.map(id -> User.users.get((Long)id))
			.anyMatch(user -> ((User)user).name().equals(userName));
	}
}