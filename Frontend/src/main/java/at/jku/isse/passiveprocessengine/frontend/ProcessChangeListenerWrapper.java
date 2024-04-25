package at.jku.isse.passiveprocessengine.frontend;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import at.jku.isse.designspace.artifactconnector.core.repository.ArtifactIdentifier;
import at.jku.isse.designspace.core.model.Element;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.passiveprocessengine.frontend.artifacts.ArtifactResolver;
import at.jku.isse.passiveprocessengine.frontend.ui.IFrontendPusher;
import at.jku.isse.passiveprocessengine.instance.ProcessInstanceChangeProcessor;
import at.jku.isse.passiveprocessengine.instance.activeobjects.ProcessInstance;
import at.jku.isse.passiveprocessengine.instance.messages.Commands.ProcessScopedCmd;
import at.jku.isse.passiveprocessengine.instance.messages.EventDistributor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessChangeListenerWrapper extends ProcessInstanceChangeProcessor{

	AtomicInteger counter = new AtomicInteger(0);
	
	Set<ProcessInstance> updatedInstances = Collections.synchronizedSet(new HashSet<>());
	Set<ArtifactIdentifier> lazyLoaded = Collections.synchronizedSet(new HashSet<>());
	
	IFrontendPusher uiUpdater;
	//LazyLoadingListener lazyLoader;
	ArtifactResolver resolver;
	Workspace ws;
	
	public ProcessChangeListenerWrapper(Workspace ws, IFrontendPusher uiUpdater, ArtifactResolver resolver, EventDistributor eventDistributor) {
		super(ws, eventDistributor);
		this.ws = ws;
		this.uiUpdater = uiUpdater;
		//this.lazyLoader = lazyLoader;
		this.resolver = resolver;
	}
	
	public boolean foundLazyLoaded() {
		return lazyLoaded.size() > 0;
	}

	@Override
	public void handleUpdated(Collection<Operation> operations) {
		counter.updateAndGet(i -> { return i < 0 ? 1 : i+1; });
		// get lazyloading
		operations.stream()
		 .map(operation -> {
			if (operation instanceof PropertyUpdateAdd) {
				return processPropertyUpdateAdd((PropertyUpdateAdd) operation);
			} else return null;
		 })
		 .filter(Objects::nonNull)
		 .distinct()
		 .map(inst -> getArtifactIdentifier(inst))
		 .filter(Objects::nonNull)
		 .forEach(ai -> lazyLoaded.add(ai));
		
		updatedInstances.addAll(super.handleUpdates(operations));
		
		int current = counter.decrementAndGet();
		if (current == 0 && updatedInstances.size() > 0) {
			//all cascading updates have settled, lets signal update to
			uiUpdater.update(new HashSet<>(updatedInstances));
			updatedInstances.clear();
		}
	}
	
	public int resetAndUpdate() {
		// just a quick hack for now 
		// as the counter above sometimes remains at the end at a value > 0, 
		// hence no more updates are done thereafter
		counter.set(0);
		int count = updatedInstances.size();
		if (updatedInstances.size() > 0) {
			uiUpdater.update(new HashSet<>(updatedInstances));
			updatedInstances.clear();
		}
		return count;
	}
	
	private static Supplier<Boolean> falseSupplier = () -> Boolean.FALSE;
	
	private Instance processPropertyUpdateAdd(PropertyUpdateAdd op) {
		if (op.name().endsWith("@rl_ruleScopes") ) {		
			Id ruleId = (Id) op.value();
			Element rule = ws.findElement(ruleId);
			if (rule.getName().startsWith("crd")) {
				Element element = ws.findElement(op.elementId());
				if (element instanceof Instance 
						&& element.hasProperty("fullyFetched") 
						&& (      element.getPropertyAsValueOrElse("fullyFetched", falseSupplier) == null 
						|| ((Boolean)element.getPropertyAsValueOrElse("fullyFetched", falseSupplier)) == false     )
						){
					return (Instance) element;
				}
			}			
		}
		return null;
	}
	
	@Override
	protected void prepareQueueExecution(List<ProcessScopedCmd> mostRecentQueuedEffects) {
		// check for each queued effect if it undos a previous one: e.g., a constraint fufillment now is unfulfilled, datamapping fulfilled is no unfulfilled
		if (mostRecentQueuedEffects.size() == 0)
			return;
		mostRecentQueuedEffects.stream().forEach(cmd -> {
			ProcessScopedCmd overriddenCmd = cmdQueue.put(cmd.getId(), cmd);
			if (overriddenCmd != null) {
				log.trace("Overridden Command: "+overriddenCmd.toString());
			}
		});
		
		// get lazyloading
		batchFetchLazyLoaded();
		//fetchLazyLoaded();
	}
	
	protected Set<ProcessInstance> executeCommands() {
		if (lazyLoaded.isEmpty())
			return super.executeCommands(); // then there is no lazy loading necessary anymore, we can execute the commands
		else // we skip this until lazy loading is complete
			return Collections.emptySet();
	}

//	private void fetchLazyLoaded() {
//		Optional<ArtifactIdentifier> optAI = getLazyLoadedArtifact();
//		while (optAI.isPresent()) {
//			ArtifactIdentifier art = optAI.get();	
//			try {
//					log.debug("Trying to fetch lazyloaded artifact: "+art.toString());
//					Instance inst =  resolver.get(art);
//				} catch (ProcessException e) {
//					log.warn("Could not fetch lazyloaded artifact: "+art.toString()+" due to: "+e.getMessage());
//				}
//			lazyLoaded.remove(art);
//			optAI = getLazyLoadedArtifact(); // get the next one.
//		}
//	}
	
	public ArtifactIdentifier getArtifactIdentifier(Instance inst) {
		// less brittle, but requires consistent use by artifact connectors
		String artId = (String) inst.getPropertyAsValue("id");
		InstanceType instType = inst.getInstanceType();
		List<String> idOptions = resolver.getIdentifierTypesForInstanceType(instType);
		if (idOptions.isEmpty()) {
			log.warn("Cannot determine identifier option for instance type: "+instType.getName());
			return new ArtifactIdentifier(artId, instType.getName());
		} 
		String idType = idOptions.get(0);				
		return new ArtifactIdentifier(artId, instType.getName(), idType);
	}
	
	private Optional<ArtifactIdentifier> getLazyLoadedArtifact() {
		return lazyLoaded.stream().findAny();
	}
	
	public void batchFetchLazyLoaded() {
		Map<String, Set<ArtifactIdentifier>> idsPerType = getIdsPerType();
		
		while (!idsPerType.isEmpty()) {
			String type =idsPerType.keySet().stream().findAny().get();
			Set<ArtifactIdentifier> aboutToBeFetched = idsPerType.get(type);			
			log.debug(String.format("Trying to fetch %s lazyloaded artifact(s) of type: %s ", aboutToBeFetched.size(), type));
			aboutToBeFetched.stream().forEach(ai -> lazyLoaded.remove(ai)); //we need to remove here, as fetching artifacts results in reentry due to transaction conclusion
			Set<Instance> inst =  resolver.get(aboutToBeFetched.stream().map(ai -> ai.getId()).collect(Collectors.toSet()), type);							
			
			idsPerType = getIdsPerType();
		}
	}
	
	private Map<String, Set<ArtifactIdentifier>> getIdsPerType() {
		return lazyLoaded.stream()					
				.map(ais -> { return new AbstractMap.SimpleEntry<String, ArtifactIdentifier>(ais.getIdType(), ais); })
				.collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey, Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toSet())));
	}
	
	public void executeConstraintStateInconsistencyRepairingCommands(List<ProcessScopedCmd> queuedInconEffects) {
		prepareQueueExecution(queuedInconEffects);
		Set<ProcessInstance> incons = executeCommands();
		uiUpdater.update(incons);
	}
}
