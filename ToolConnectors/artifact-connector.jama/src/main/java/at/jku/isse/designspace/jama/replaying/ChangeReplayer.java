package at.jku.isse.designspace.jama.replaying;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.jama.model.JamaBaseElementType;
import at.jku.isse.designspace.jama.model.JamaSchemaConverter;
import at.jku.isse.designspace.jama.replaying.ChangeParser.CreationChange;
import at.jku.isse.designspace.jama.replaying.ChangeParser.PropertyChange;
import at.jku.isse.designspace.jama.replaying.ChangeParser.RelationChange;
import at.jku.isse.designspace.jama.replaying.JamaActivity.EventTypes;
import at.jku.isse.designspace.jama.replaying.JamaActivity.ObjectTypes;
import at.jku.isse.designspace.jama.service.IJamaService.JamaIdentifiers;
import at.jku.isse.designspace.jama.service.JamaService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChangeReplayer {

	HashMap<Integer,Instance> items = new HashMap<>();
	List<JamaActivity> allchanges = new LinkedList<JamaActivity>();
	int replayIndex = -2;
	Instance lastChangedItem = null;
	JamaSchemaConverter jamaSchemaConverter;
	JamaService jamaService;
	
	// assume items have relations in the cache already
	public ChangeReplayer(Collection<Instance> itemsToReplay, Set<JamaActivity> events, JamaSchemaConverter jamaSchemaConverter, JamaService jamaService) {
		this.jamaSchemaConverter = jamaSchemaConverter;
		this.jamaService = jamaService;
		itemsToReplay.stream().forEach(item -> items.put(Integer.parseInt((String) item.getPropertyAsValue(BaseElementType.ID)), item));
		collectEvents(events);
	}
	
	public List<JamaActivity> getChanges() {
		return allchanges;
	}
	
	public long revertToBegin() {
		allchanges.stream().forEach(change -> {
			Instance item =  items.get(change.getItemId());
			applyBackward(item, change);
		});		
		Collections.reverse(allchanges); // to have them with oldest change
		if (allchanges.size() > 0) // only if there where indeed changes that were replayed
			replayIndex = -1;		
		return allchanges.size(); // return number of changes reverted
	}
	
	private void collectEvents(Set<JamaActivity> events) {
		events.stream()
			.filter(c -> c.getEventType() != null)
			.filter(c -> c.getEventType().equals(EventTypes.BATCH_UPDATE) || c.getEventType().equals(EventTypes.UPDATE) || c.getEventType().equals(EventTypes.CREATE) || c.getEventType().equals(EventTypes.DELETE))
			.filter(c -> c.getObjectType().equals(ObjectTypes.RELATIONSHIP) || c.getObjectType().equals(ObjectTypes.ITEM))
			.flatMap(c -> ChangeParser.parseToChange(c).stream())
			.filter(change -> { //as we cant handle description changes yet, dont bother including them in replaying 
				if (change instanceof PropertyChange && !(change instanceof CreationChange)) {
					if (((PropertyChange) change).getField().equalsIgnoreCase("description")) {
						return false;
					}
				} 
				return true;
			})
			.forEach(change -> allchanges.add(change));
		long count = allchanges.size();
		//sort by Date, newest first
		allchanges.sort(new Comparator<JamaActivity>(){
			@Override
			public int compare(JamaActivity o1, JamaActivity o2) { //newest first
				return o2.getId().compareTo(o1.getId());
			}			
		});
	}
	
	public JamaActivity applyNextForwardChange() {
		int nextPos = replayIndex+1;
		if (nextPos < allchanges.size() && nextPos >= 0) { // there is a next change			
			JamaActivity change = allchanges.get(nextPos);
			// apply change
			Instance item = items.get(change.getItemId());
			applyForward(item, change);
			replayIndex = nextPos;
			return change; // returns the change that was applied	
		} else {
			System.out.println("Reached End of Changes for Forward Replaying");
		}
		return null;
	}
	
//	public Optional<Instance> lastChangedItem() {
//		return Optional.ofNullable(lastChangedItem);
//	}
	
	public Optional<ZonedDateTime> getDateOfNextFutureChange() {
		if (replayIndex == -2)
			return Optional.empty();
		int nextPos = replayIndex+1;
		if (nextPos < allchanges.size()) { // there is a next change
			return Optional.of(allchanges.get(nextPos).getDate());
		} else return Optional.empty();
	}
	
	public Optional<ZonedDateTime> getDateOfNextChangeOfThePast() {		
		int prevPos = replayIndex-1;
		if (prevPos >= 0) { // there is a next change
			return Optional.of(allchanges.get(prevPos).getDate());
		} else return Optional.empty();
	}
	
    private void applyChange(Instance item, JamaActivity change, boolean forward) {
    	// lets assume that change matches this item
    	String designspaceItemId = item.name();
    	
    	if (change instanceof PropertyChange) {
    		if (change instanceof CreationChange) {
    			// nothing to do
    			return;
    		}
    		PropertyChange pc = (PropertyChange)change;
    		if (pc.getField().equalsIgnoreCase("description") || pc.getField().equalsIgnoreCase("shortName")) // TODO: we don;t have 'description' changes yet, and there is no field with "Short Name" label somehow
    			return;
    		Instance itemType = item.getPropertyAsInstance(JamaBaseElementType.ITEM_TYPE);
    		String designspaceItemTypeId = (String) itemType.getPropertyAsValue(BaseElementType.ID);
    		
    		String field = pc.getField(); // assumed to be already lowerCase, camel cased
            if (!field.equals(BaseElementType.ID.toString()) && item.hasProperty(field)) {
                Property property = item.getProperty(field);
                if (!property.name.equals(BaseElementType.ID)) {
                    Optional<String> jamaType_ = this.jamaSchemaConverter.getJamaTypeForFieldNameOfItemType(designspaceItemTypeId, property.name);
                    if (jamaType_.isPresent()) {
                        String jamaType = jamaType_.get();
                        String cValue = forward ? pc.getNewValue() : pc.getPreviousValue(); 
                        try {
                            switch (jamaType) {
                                case "USER":
                                	//TODO: resolve user name to User! for now, ignore
                                    //Integer userId = cValue != null ? Integer.parseInt(cValue) : -1;
                                    //jamaService.mapUserToField(userId, property.name, item);
                                    break;
                                case "PROJECT":
                                    Integer project_Id = cValue != null ? Integer.parseInt(cValue) : -1;
                                    jamaService.mapProjectToField(project_Id, property.name, item);
                                    break;
                                case "MULTI-LOOKUP":
                                		log.warn(String.format("Multilookup update for property %s not supported", pc.getField()));
//                                    SetProperty lookUpValues = item.getPropertyAsSet(property.name);
//                                    ArrayList<Object> lookUpIds = AccessToolsJSON.accessArray(fields, property.name);
//                                    for (Object lookUpId : lookUpIds) {
//                                        lookUpValues.add(lookUpId);
//                                    }
                                    break;
                                case "LOOKUP":
                                	item.getPropertyAsSingle(property.name).set(cValue);
//                                    Integer lookUpId = cValue != null ? Integer.parseInt(cValue) : -1;
//                                    if (lookUpId == -1) 
//                                    	 item.getPropertyAsSingle(property.name).set(null);
//                                    else {
//                                    	Optional<String> optionName = jamaService.getPickListOptionName(lookUpId);
//                                    	if (optionName.isPresent()) {
//                                    		item.getPropertyAsSingle(property.name).set(optionName.get());
//                                    	} else {
//                                    		Workspace.logger.warn(String.format("Lookup value %s not found for property %s of artifact type %s", lookUpId, property.name, designspaceItemTypeId));
//                                    	}
//                                    }
                                    break;
                                case "RELEASE":
                                	//TODO: resolve release name to release!
                                    //Integer releaseId = cValue != null ? Integer.parseInt(cValue) : -1;
                                    jamaService.mapReleaseToField(cValue, property.name, item);
                                    break;
                                case "DATE":
                                case "URL_STRING":
                                case "CALCULATED":
                                case "STEPS":
                                case "TEST_CASE_STATUS":	
                                case "TEST_RUN_RESULTS":
                                case "STRING":
                                case "TEXT":
                                    item.getPropertyAsSingle(property.name).set(cValue);
                                    break;
                                case "BOOLEAN":
                                    Boolean boolValue = cValue != null ? Boolean.parseBoolean(cValue) : null;
                                    item.getPropertyAsSingle(property.name).set(boolValue);
                                    break;
                                case "INTEGER":
                                    Integer intValue = cValue != null ? Integer.parseInt(cValue) : null;
                                    item.getPropertyAsSingle(property.name).set(intValue);
                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            log.debug("JAMA-SERVICE: replaying the property " + property.name + " failed for the item " + designspaceItemId + "");
                            e.printStackTrace();
                        }
                    }
                }
            }    	    		
    	} else if (change instanceof RelationChange) {
    		RelationChange rc = (RelationChange)change;
    		String docKey = (String) item.getPropertyAsValue(BaseElementType.KEY);
    		// check if this item is up or downstream:    THIS == from/source, opposite is to		
    		boolean isDownstream = docKey.equals(rc.getSourceDocKey()) ? true : false;
    		String oppositeKey = isDownstream ? rc.getDestinationDocKey() : rc.getSourceDocKey();
    		Optional<Instance> oppositeItem = jamaService.getJamaItem(oppositeKey, JamaIdentifiers.JamaItemDocKey);
    		if (oppositeItem.isEmpty()) {
    			//System.err.println("Cannot resolve item by docKey: "+oppositeKey); // we didnt make all items available offline, this may happen
    			return;
    		}
    		SetProperty toManipulate = isDownstream ? item.getPropertyAsSet(JamaBaseElementType.DOWNSTREAM) : item.getPropertyAsSet(JamaBaseElementType.UPSTREAM);
    		switch(rc.getEventType()) {						
			case CREATE:
				if (forward){ // add an item
					toManipulate.add(oppositeItem.get());
				} else { // remove from existing					
					toManipulate.remove(oppositeItem.get());
				}
				break;
			case DELETE:
				if (forward){ // remove an item
					toManipulate.remove(oppositeItem.get());
				} else { // add to existing					
					toManipulate.add(oppositeItem.get());					
				}
				break;			
			case UPDATE: //switches direction, i.e., from upstream to downstream and viceversa				
				SetProperty  toRemoveFrom;
				SetProperty  toAddTo;
				if (forward) {					
					toAddTo = isDownstream ? item.getPropertyAsSet(JamaBaseElementType.DOWNSTREAM) : item.getPropertyAsSet(JamaBaseElementType.UPSTREAM);
					toRemoveFrom = isDownstream ? item.getPropertyAsSet(JamaBaseElementType.UPSTREAM) : item.getPropertyAsSet(JamaBaseElementType.DOWNSTREAM);
				} else {
					//if (isDownstream) {//this is a downstream relation, thus we need to remove opposite from upstream and add to downstream 
					toRemoveFrom = isDownstream ? item.getPropertyAsSet(JamaBaseElementType.DOWNSTREAM) : item.getPropertyAsSet(JamaBaseElementType.UPSTREAM);
					toAddTo = isDownstream ? item.getPropertyAsSet(JamaBaseElementType.UPSTREAM) : item.getPropertyAsSet(JamaBaseElementType.DOWNSTREAM);									
				}
				toRemoveFrom.remove(oppositeItem.get());
				toAddTo.add(oppositeItem.get());
				break;
			default:
				System.err.println("Found unexpected RelationChange: EventType:"+rc.getEventType().toString());
				break;
    			
    		}
    		
    	}
    }
   
    public void applyForward(Instance inst, JamaActivity change) {
		applyChange(inst, change, true);    	
    }
    
    public void applyBackward(Instance inst, JamaActivity change) {
    	applyChange(inst, change, false);
    }
}
