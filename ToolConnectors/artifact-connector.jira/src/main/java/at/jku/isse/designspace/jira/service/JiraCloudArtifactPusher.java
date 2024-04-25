package at.jku.isse.designspace.jira.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;

import at.jku.isse.designspace.artifactconnector.core.idcache.IIdCache;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.ListProperty;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.jira.model.JiraBaseElementType;
import at.jku.isse.designspace.jira.model.JiraSchemaConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraCloudArtifactPusher implements IArtifactPusher {

    private Workspace workspace;
    private InstanceType jiraSchema;
    private IIdCache idCache;

    public JiraCloudArtifactPusher(Workspace workspace, InstanceType jiraSchema, IIdCache idCache) {
        this.workspace = workspace;
        this.jiraSchema = jiraSchema;
        this.idCache = idCache;
    }

    @Override
    public Optional<Instance> createInstance(Map<String, Object> dataMap, InstanceType instanceType, boolean update, boolean withIssueLinks) {
        if (instanceType == JiraBaseElementType.JIRA_ARTIFACT.getType() || instanceType == BaseElementType.ARTIFACT.getType()) {
            instanceType = jiraSchema;
        }

        if (dataMap != null) {
            Object rawId = dataMap.get(BaseElementType.ID);
            if (rawId == null) {
                //only users have no id, but am account Id.
                //Everything else will be ignored
                rawId = dataMap.get(JiraBaseElementType.ACCOUNT_ID);
                if (rawId == null) {
                    return Optional.empty();
                }
            }

            String id = reformatId(rawId.toString(), instanceType);
            Object key_ = dataMap.get(BaseElementType.KEY);
            String key;

            MapProperty<String> propertyMetadata = instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
            if (key_ == null) {
                key = id;
            } else {
                key = key_.toString();
            }

            Map<String, Object> issueFields;
            if (dataMap.containsKey(JiraBaseElementType.FIELDS)) {
                issueFields = (Map<String, Object>) dataMap.get(JiraBaseElementType.FIELDS);
            } else {
                issueFields = dataMap;
            }

            String instanceName = key + "_" + issueFields.getOrDefault(JiraBaseElementType.SUMMARY, "");

            Instance instance;
            Optional<Instance> instance_ = findInstance(id);

            if (instance_.isPresent()) {
                if (!update) {
                    return instance_;
                }

                instance = instance_.get();
            } else {
                instance = WorkspaceService.createInstance(this.workspace, instanceName, instanceType);
            }

            
            Object selfUrl = dataMap.getOrDefault(JiraBaseElementType.SELF, null);

            instance.getProperty(BaseElementType.ID).set(id);
            instance.getProperty(BaseElementType.KEY).set(key);
            if (selfUrl != null) {
            	String browseableURL = apiUrl2BrowsableUrl(selfUrl.toString(), key);
            	instance.getProperty(JiraBaseElementType.LINK).set(browseableURL);
            }
            instance.setName(instanceName);

            this.idCache.addEntry(id, instance.id());
            this.idCache.addEntry(key, instance.id());

            //-----------Mapping primitive and subtype fields-----------
            for (Map.Entry<String, Object> entry : issueFields.entrySet()) {
                //for (String propertyName : instance.getPropertyNames()) {
                Optional<String> propertyName_ = JiraSchemaConverter.resolveFieldIdToLabel(entry.getKey(), instanceType);
                String propertyName;
                if (propertyName_.isEmpty()) {
                    propertyName = entry.getKey();
                } else {
                    propertyName = propertyName_.get();
                }

                if (!propertyName.equals("id") && !propertyName.equals("linkedIssues") && instance.hasProperty(propertyName)) {
                    Property property = instance.getProperty(propertyName);
                    InstanceType propertyInstanceType = property.propertyType.referencedInstanceType();
                    Optional<String> jiraType_ = JiraSchemaConverter.resolveFieldNameToJiraType(propertyName, instanceType);

                    if (jiraType_.isPresent()) {
                        String jiraType = jiraType_.get();
                        Cardinality cardinality = property.propertyType.cardinality();
                        Object value = issueFields.get(entry.getKey());

                        try {
                            if (propertyInstanceType == Workspace.STRING) {
                                if (cardinality == Cardinality.SINGLE) {
                                    try {
                                        if (jiraType.equalsIgnoreCase(JiraSchemaConverter.JIRA_TYPE.ISSUE_TYPE.value) ||
                                                jiraType.equalsIgnoreCase(JiraSchemaConverter.JIRA_TYPE.PROJECT.value) ||
                                                jiraType.equalsIgnoreCase(JiraSchemaConverter.JIRA_TYPE.PRIORITY.value) ||
                                                jiraType.equalsIgnoreCase(JiraSchemaConverter.JIRA_TYPE.USER.value) ||
                                                jiraType.equalsIgnoreCase(JiraSchemaConverter.JIRA_TYPE.STATUS.value)) {
                                            Map<String, Object> subtypeMap = (Map<String, Object>) value;
                                            property.set(subtypeMap.get(BaseElementType.NAME));
                                        } else {
                                            if (value != null) {
                                                // now options are also handled as strings but are contained in a map:
                                            	if (value instanceof Map) {
                                            		Object obj = ((Map) value).get("value");
                                            		if (obj != null)
                                            			property.set(obj.toString());
                                            		else 
                                            			log.debug("Property has empty map "+property.getName());
                                            	} else	
                                            		property.set(value.toString());
                                            }
                                        }
                                    } catch (Exception e) {
                                    	e.printStackTrace();
                                        log.debug("Jira-Service: The value for the string property " + propertyName + " could not be mapped to the instance!");
                                    }
                                } else if (cardinality == Cardinality.SET) {
                                    SetProperty<String> setProperty = instance.getPropertyAsSet(propertyName);
                                    if (value != null) {
                                        try {
                                            setProperty.clear();
                                            List<Object> values = (List<Object>) value;
                                            values.stream().forEach(v -> { 
                                            	if (value instanceof Map) 
                                            		property.set(((Map) v).get("value").toString());
                                            	else 
                                            		property.set(v.toString());
                                            });
                                        } catch (ClassCastException ce) {
                                        	ce.printStackTrace();
                                            log.debug("Jira-Service: The value for string list property " + propertyName + " could not be cast to a List! (No mapping took place)");
                                        }
                                    } else {
                                        setProperty.clear();
                                    }
                                }
                            } else if (propertyInstanceType == Workspace.INTEGER) {
                                mapPrimitivePropertyToInstance(property, instance, value, Long::parseLong);
                            } else if (propertyInstanceType == Workspace.BOOLEAN) {
                                mapPrimitivePropertyToInstance(property, instance, value, Boolean::parseBoolean);
                            } else {
                                if (cardinality == Cardinality.SINGLE) {
                                    if (value == null) {
                                        property.set(null);
                                    } else {
                                        Optional<Instance> instanceForValue = createInstance((Map<String, Object>) value, propertyInstanceType, false, false);
                                        if (instanceForValue.isPresent()) {
                                            property.set(instanceForValue.get());
                                        }
                                    }
                                } else {
                                    SetProperty<Instance> setProperty = instance.getPropertyAsSet(propertyName);
                                    if (value != null) {
                                        try {
                                            List<Map<String, Object>> dataObjects = (List<Map<String, Object>>) value;
                                            Set<Instance> instanceSet = new HashSet<>();
                                            for (Map<String, Object> dataObjectMap : dataObjects) {
                                                Optional<Instance> linkedInstance = createInstance(dataObjectMap, propertyInstanceType, false, false);
                                                if (linkedInstance.isPresent()) {
                                                    instanceSet.add(linkedInstance.get());
                                                }
                                            }
                                            synchronizeArtifactWithInstanceSets(instance, setProperty, instanceSet, true, false, update);
                                        } catch (Exception e1) {
                                        	e1.printStackTrace();
                                            log.debug("Jira-Service: The value for the issue list property " + propertyName + " could not be mapped to the instance!");
                                        }
                                    } else {
                                        setProperty.clear();
                                    }
                                }
                            }
                        } catch (Exception exception) {
                        	exception.printStackTrace();
                            log.debug("Jira Service : " + propertyName + " could not be assigned to instance, because the value " + value + " had an unexpected format");
                        }
                    }
                }
            }

//            if (instance.hasProperty(JiraBaseElementType.PARENT)) {
//            	//Making sure parents contain their subtasks in the corresponding list
//            	Object parent_ = instance.getProperty(JiraBaseElementType.PARENT).get();
//            	if (parent_ != null) {
//            		Instance parent = (Instance) parent_;
//            		if (parent.hasProperty(JiraBaseElementType.SUBTASKS)) {
//            			SetProperty<Instance> subtasks = parent.getPropertyAsSet(JiraBaseElementType.SUBTASKS);
//            			if (!subtasks.contains(parent)) {
//            				subtasks.add(instance);
//            			}
//            		}
//            	}
//            }

            //-----------mapping relationships-----------
            if (withIssueLinks) {
                if (instance.hasProperty(BaseElementType.FULLY_FETCHED)) {
                    instance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
                }
            } else {
                if (BaseElementType.ARTIFACT.getType() == instanceType && instance.hasProperty(BaseElementType.FULLY_FETCHED)) {
                    instance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(false);
                }
            }

            if (instanceType == jiraSchema) {

                //gathering property names of all link fields
                Set<String> outgoingRelationNames = instance.getPropertyNames().stream().
                        filter(name -> propertyMetadata.containsKey(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_RELATION_OUTWARD_INWARD + name)).
                        collect(Collectors.toUnmodifiableSet());

                Set<String> incomingRelationNames = instance.getPropertyNames().stream().
                        filter(name -> propertyMetadata.containsKey(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_RELATION_INWARD_OUTWARD + name)).
                        collect(Collectors.toUnmodifiableSet());


                Object issueLinks_ = issueFields.get(JiraBaseElementType.ISSUE_LINKS);
                if (issueLinks_ != null) {
                    ArrayList<Map<String, Object>> issueLinks = (ArrayList<Map<String, Object>>) issueLinks_;

                    for (String outgoingRelationName : outgoingRelationNames) {
                        Set<Instance> relationsFromJSON = retrieveAllLinksForRelationProperty(issueLinks, outgoingRelationName, true);
                        SetProperty<Instance> outgoingRelationSet = instance.getPropertyAsSet(outgoingRelationName);
                        synchronizeArtifactWithInstanceSets(instance, outgoingRelationSet, relationsFromJSON, false, false, update);
                    }

                    for (String incomingRelationName : incomingRelationNames) {
                        Set<Instance> relationsFromJSON = retrieveAllLinksForRelationProperty(issueLinks, incomingRelationName, false);
                        SetProperty<Instance> incomingRelationSet = instance.getPropertyAsSet(incomingRelationName);
                        synchronizeArtifactWithInstanceSets(instance, incomingRelationSet, relationsFromJSON, false, true, update);
                    }
                }
            }

            return Optional.of(instance);

        }

        return Optional.empty();
    }
    
    private String apiUrl2BrowsableUrl(String apiUrl, String key) {
    	
    	URI selfURI = URI.create(apiUrl);
    	StringBuffer url = new StringBuffer(selfURI.getScheme());    	
    	url.append("://");
    	url.append(selfURI.getHost());
    	if (selfURI.getPort() > 0) {
    		url.append(":");
    		url.append(selfURI.getPort());
    	}
    	url.append("/browse/");
    	url.append(key);
    	return url.toString();
    }

    @Override
    public Optional<Instance> findArtifact(String id) {
        return findInstance(id);
    }

    @Override
    public Instance createPlaceholderArtifact(String id, String key, InstanceType type) {
        Instance placeholderInstance = this.workspace.createInstance(type, id);
        placeholderInstance.getProperty(BaseElementType.ID).set(id);
        placeholderInstance.getProperty(BaseElementType.KEY).set(key);

        this.idCache.addEntry(id, placeholderInstance.id());
        this.idCache.addEntry(key, placeholderInstance.id());

        return placeholderInstance;
    }

    private void synchronizeArtifactWithInstanceSets(Instance artifactInstance, SetProperty<Instance> instanceSet, Set<Instance> changedInstanceSet, boolean subtype, boolean incoming, boolean update) {

        //checking whether the set of instances mapped to the current outgoing property name
        //is contains links which have been removed since the last update
        if (update && !instanceSet.isEmpty()) {
            for (Object instance_ : instanceSet.toArray()) {
                try {
                    Instance instance = (Instance) instance_;
                    String id = instance.getProperty(BaseElementType.ID).getValue().toString();

                    if (!changedInstanceSet.contains(instance)) {
                        instanceSet.remove(instance);
                        if (!subtype) {
                            if (incoming) {
                                SetProperty<Instance> incomingRelations = artifactInstance.getPropertyAsSet(JiraBaseElementType.LINKS_INCOMING);
                                incomingRelations.remove(instance);
                            } else {
                                SetProperty<Instance> outgoingRelations = artifactInstance.getPropertyAsSet(JiraBaseElementType.LINKS_OUTGOING);
                                outgoingRelations.remove(instance);
                            }
                        }
                    }
                } catch (ClassCastException ce) {
                    System.out.println("test");
                }
            }
        }

        //adding outgoing relation artifacts to their respective SetProperties
        for (Instance instance : changedInstanceSet) {
            if (!instanceSet.contains(instance)) {
                instanceSet.add(instance);
                if (!subtype) {
                    if (incoming) {
                        SetProperty<Instance> incomingRelations = artifactInstance.getPropertyAsSet(JiraBaseElementType.LINKS_INCOMING);
                        incomingRelations.add(instance);
                    } else {
                        SetProperty<Instance> outgoingRelations = artifactInstance.getPropertyAsSet(JiraBaseElementType.LINKS_OUTGOING);
                        outgoingRelations.add(instance);
                    }
                }
            }
        }
    }

    private Set<Instance> retrieveAllLinksForRelationProperty(ArrayList<Map<String, Object>> issueLinks, String relationPropertyName, boolean outward) {
        HashSet<Instance> instanceSet = new HashSet<>();

        for (Map<String, Object> linkMap : issueLinks) {
            Map<String, Object> linkType = (Map<String, Object>) linkMap.get(JiraBaseElementType.TYPE);
            Map<String, Object> linkData;
            String relationAttribute;

            if (outward && linkMap.containsKey(JiraBaseElementType.OUTWARD_ISSUE)) {
                String role = linkType.get(JiraBaseElementType.OUTWARD).toString();
                relationAttribute = CaseUtils.toCamelCase(role, false, ' ', '_', '.', '-', '/', '\\').trim();
                linkData = (Map<String, Object>) linkMap.get(JiraBaseElementType.OUTWARD_ISSUE);
            } else if (!outward && linkMap.containsKey(JiraBaseElementType.INWARD_ISSUE)) {
                String role = linkType.get(JiraBaseElementType.INWARD).toString();
                relationAttribute = CaseUtils.toCamelCase(role, false, ' ', '_', '.', '-', '/', '\\').trim();
                linkData = (Map<String, Object>) linkMap.get(JiraBaseElementType.INWARD_ISSUE);
            } else {
                continue;
            }

            if (relationAttribute.equals(relationPropertyName)) {
                Optional<Instance> linkedInstance = this.createInstance(linkData, jiraSchema, false, false);
                if (linkedInstance.isPresent()) {
                    instanceSet.add(linkedInstance.get());
                }
            }
        }

        return instanceSet;
    }

    private <T> boolean mapPrimitivePropertyToInstance(Property property, Instance instance, Object value, Function<String, T> stringTransformationFunction) {
        if (property.propertyType.cardinality() == Cardinality.SINGLE) {
            try {
                property.set(stringTransformationFunction.apply(value.toString()));
                return true;
            } catch (Exception e) {
                try {
                    property.set(value);
                    return true;
                } catch (Exception e1) {
                    log.debug("Jira-Service: The value for the primitive property " + property.name + " could not be mapped to the instance!");
                    return false;
                }
            }
        } else {
            ListProperty<T> listProperty = instance.getPropertyAsList(property.name);
            listProperty.clear();
            try {
                ArrayList<T> ints = (ArrayList<T>) value;
                for (T primitiveValue : ints) {
                    listProperty.add(primitiveValue);
                }
                return true;
            } catch (Exception e) {
                try {
                    ArrayList<String> strings = (ArrayList<String>) value;
                    for (String stringValue : strings) {
                        listProperty.add(stringTransformationFunction.apply(stringValue));
                    }
                    return true;
                } catch (Exception e1) {
                    log.debug("Jira-Service: The value for the primitive list property " + property.name + " could not be mapped to the instance!");
                    return false;
                }
            }
        }
    }

    private String reformatId(String id, InstanceType instanceType) {
        if (instanceType != jiraSchema && instanceType != JiraBaseElementType.JIRA_ARTIFACT.getType()) {
            id = instanceType.name() + "_" + id;
        }
        return id;
    }

    private Optional<Instance> findInstance(String issueId) {
        Id instanceId = this.idCache.getDesignspaceId(issueId);
        Instance instance = this.workspace.findElement(instanceId);
        return Optional.ofNullable(instance);
    }

}
