package at.jku.isse.designspace.jira.updateservice;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.jira.model.JiraBaseElementType;
import at.jku.isse.designspace.jira.model.JiraSchemaConverter;
import at.jku.isse.designspace.jira.restclient.connector.IJiraTicketService;
import at.jku.isse.designspace.jira.service.IArtifactPusher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraCloudChangeLogItemFactory implements IChangeLogItemFactory {

    static class BaseChangeLogItem {

        String field;
        String fieldId;
        String fieldType;
        String fromString;
        String toString;
        String oldValue;
        String newValue;
        String id;

        BaseChangeLogItem(Map<String, Object> baseChange, String logTime, int numberInLog) {
            field = (String) baseChange.get(JiraBaseElementType.FIELD);
            fieldId = (String) baseChange.get(JiraBaseElementType.FIELD_ID);
            fieldType = (String) baseChange.get(JiraBaseElementType.FIELD_TYPE);
            fromString = (String) baseChange.get(JiraBaseElementType.FROM_STRING_CHANGE);
            toString = (String) baseChange.get(JiraBaseElementType.TO_STRING_CHANGE);
            oldValue = (String) baseChange.get(JiraBaseElementType.FROM_CHANGE);
            newValue = (String) baseChange.get(JiraBaseElementType.TO_CHANGE);
            id = logTime + "_" + field + "_" + numberInLog;
        }

    }

    private final Map<String, String> GENERIC_FIELD_RELATIONS = new HashMap<>() {
        {
            put("priority", "priority");
            put("issuetype", "issuetype");
            put("project", "project");
            put("Fix Version", "version");
            put("Fix Versions", "version");
            put("status", "status");
            put("creator", "user");
            put("assignee", "user");
            put("reporter", "user");
            put("Component", "component");
            put("components", "component");
            put("resolution", "resolution");
        }
    };

    private final IArtifactPusher artifactPusher;
    private final IJiraTicketService jiraTicketService;
    private final InstanceType instanceType;

    public JiraCloudChangeLogItemFactory(IArtifactPusher artifactPusher, IJiraTicketService jiraTicketService, InstanceType instanceType) {
        this.artifactPusher = artifactPusher;
        this.jiraTicketService = jiraTicketService;
        this.instanceType = instanceType;
    }

    public ArrayList<ChangeLogItem> createChangeLog(Map<String, Object> rawChangeLog, String issueId, String issueKey) {
        String timeCreated = rawChangeLog.get(JiraBaseElementType.CREATED).toString();
        ArrayList<Map<String, Object>> rawItems = (ArrayList<Map<String, Object>>) rawChangeLog.get(JiraBaseElementType.ITEMS);
        ArrayList<ChangeLogItem> changeLogItems = new ArrayList<>();

        for (int i = 0; i<rawItems.size(); i++) {
            Optional<ChangeLogItem> changeLogItem = createChangeLogItem(rawItems.get(i), issueId, issueKey, timeCreated ,i);
            if (changeLogItem.isPresent()) {
                changeLogItems.add(changeLogItem.get());
            }
        }

        return changeLogItems;

    }

    private Optional<ChangeLogItem> createChangeLogItem(Map<String, Object> rawItem, String issueId, String issueKey, String logTime, int numberInLog) {
        String relationship;

        BaseChangeLogItem baseChangeLogItem = new BaseChangeLogItem(rawItem, logTime, numberInLog);

        boolean subtask = baseChangeLogItem.field.equals(JiraBaseElementType.PARENT_CHANGE);
        boolean genericFieldRelation = GENERIC_FIELD_RELATIONS.containsKey(baseChangeLogItem.field);
        boolean link = baseChangeLogItem.field.equals(JiraBaseElementType.LINK_CHANGE);

        Optional<Instance> instance = this.artifactPusher.findArtifact(issueId);
        if (instance.isPresent()) {
            if (link || subtask || genericFieldRelation) {

                RelationChangeLogItem changeLogItem = new RelationChangeLogItem();
                if (subtask) {
                    RelationChangeLogItem parentChange = new RelationChangeLogItem();
                    changeLogItem.setField(JiraBaseElementType.PARENT);

                    if (baseChangeLogItem.oldValue != null) {
                        changeLogItem.setFromKey(baseChangeLogItem.fromString);
                        changeLogItem.setFromId(baseChangeLogItem.oldValue);
                    }

                    if (baseChangeLogItem.newValue != null) {
                        changeLogItem.setToKey(baseChangeLogItem.toString);
                        changeLogItem.setToId(baseChangeLogItem.newValue);

                        //the changelogItem for the parent
                        //we know that that from was null, because a subtask was added
                        parentChange.setFromId(null);
                        parentChange.setFromKey(null);
                        //we know that the to must be the artifact subject to the parent change
                        parentChange.setToId(issueId);
                        parentChange.setToKey(issueKey);
                        //and the ChangeLogItem off course belongs to the parent
                        parentChange.setField("subtask");
                        parentChange.setId(baseChangeLogItem.id);
                        parentChange.setArtifactId(baseChangeLogItem.newValue);
                        parentChange.setCorrespondingArtifactIdInSource(baseChangeLogItem.toString);
                        parentChange.setCorrespondingArtifactId(baseChangeLogItem.newValue);
                        parentChange.setTimeCreated(reformatTime(logTime));
                        parentChange.setArtifactIsSource(true);
                        parentChange.setDestinationRole("is SUBTASK of");
                        parentChange.setSourceRole("is PARENT of");
                    }

                    changeLogItem.setArtifactIsSource(false);
                    changeLogItem.setDestinationRole("is SUBTASK of");
                    changeLogItem.setSourceRole("is PARENT of");

                    return Optional.of(parentChange);

                } else if (genericFieldRelation) {

                    String type = GENERIC_FIELD_RELATIONS.get(baseChangeLogItem.field);
                    changeLogItem.setField(baseChangeLogItem.fieldId);

                    if (baseChangeLogItem.oldValue != null) {
                        changeLogItem.setFromId(type + "_" + baseChangeLogItem.oldValue);
                        changeLogItem.setFromKey(baseChangeLogItem.fromString);
                    }

                    if (baseChangeLogItem.newValue != null) {
                        changeLogItem.setToId(type + "_" + baseChangeLogItem.newValue);
                        changeLogItem.setToKey(baseChangeLogItem.toString);
                    }

                    changeLogItem.setArtifactIsSource(true);
                    changeLogItem.setSourceRole("is " + baseChangeLogItem.field + " of");
                    changeLogItem.setDestinationRole("is of " + baseChangeLogItem.field);

                } else {

                    changeLogItem.setField(JiraBaseElementType.LINK_CHANGE);

                    if (baseChangeLogItem.oldValue != null) {
                        relationship = baseChangeLogItem.fromString;
                        changeLogItem.setFromKey(baseChangeLogItem.oldValue);

                        String otherArtifactId = resolveId(baseChangeLogItem.oldValue);
                        if (otherArtifactId != null) {
                            changeLogItem.setFromId(otherArtifactId);
                        } else {
                            changeLogItem.setFromId(changeLogItem.getFromKey());
                        }
                    } else {
                        relationship = baseChangeLogItem.toString;
                        changeLogItem.setToKey(baseChangeLogItem.newValue);
                        String otherArtifactId = resolveId(baseChangeLogItem.newValue);
                        if (baseChangeLogItem.id != null) {
                            changeLogItem.setToId(otherArtifactId);
                        } else {
                            changeLogItem.setToId(changeLogItem.toKey);
                        }
                    }

                    MapProperty<String> propertyMetadata = instance.get().getInstanceType().getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
                    Set<String> outgoingRelationNames = instance.get().getPropertyNames().stream().
                            filter(name -> propertyMetadata.containsKey(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_RELATION_OUTWARD_INWARD + name)).
                            collect(Collectors.toUnmodifiableSet());

                    relationship = CaseUtils.toCamelCase(relationship, false, ' ', '_', '.', '-', '/', '\\', '[', ']', '@', '\u03A3', '&', '?', '(', ')');
                    Optional<String> outgoingRelationNameForChange = Optional.empty();

                    for (String rName : outgoingRelationNames) {
                        if (relationship.contains(rName)) {
                            outgoingRelationNameForChange = Optional.of(rName);
                            break;
                        }
                    }

                    if (outgoingRelationNameForChange.isPresent()) {
                        changeLogItem.setArtifactIsSource(true);
                        changeLogItem.setSourceRole(outgoingRelationNameForChange.get());
                        changeLogItem.setDestinationRole(propertyMetadata.get(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_RELATION_OUTWARD_INWARD + outgoingRelationNameForChange.get()));
                    } else {
                        Set<String> incomingRelationNames = instance.get().getPropertyNames().stream().
                                filter(name -> propertyMetadata.containsKey(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_RELATION_INWARD_OUTWARD + name)).
                                collect(Collectors.toUnmodifiableSet());

                        relationship = CaseUtils.toCamelCase(relationship, false, ' ', '_', '.', '-', '/', '\\', '[', ']', '@', '\u03A3', '&', '?', '(', ')');
                        Optional<String> incomingRelationNameForChange = Optional.empty();
                        for (String rName : incomingRelationNames) {
                            if (relationship.contains(rName)) {
                                incomingRelationNameForChange = Optional.of(rName);
                                break;
                            }
                        }

                        if (incomingRelationNameForChange.isPresent()) {
                            changeLogItem.setArtifactIsSource(false);
                            changeLogItem.setDestinationRole(incomingRelationNameForChange.get());
                            changeLogItem.setSourceRole(propertyMetadata.get(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_RELATION_INWARD_OUTWARD + incomingRelationNameForChange.get()));
                        }
                    }

                    if (changeLogItem.getSourceRole() == null) {
                        log.debug("JIRA-SERVICE: JiraCloudChangeLogItemFactory: ChangeLogItem could not be created, because the corresponding relation property was not found!");
                        return null;
                    }

                }

                changeLogItem.setArtifactId(issueId);
                changeLogItem.setCorrespondingArtifactIdInSource(issueKey);
                changeLogItem.setCorrespondingArtifactId(issueId);
                changeLogItem.setId(baseChangeLogItem.id);
                changeLogItem.setTimeCreated(logTime);

                return Optional.of(changeLogItem);

            } else {

                PropertyChangeLogItem changeLogItem = new PropertyChangeLogItem();
                Object fromData = null, toData = null;
                Map<String, Object> fromMap = new HashMap<>(), toMap = new HashMap<>();

                //create entry in properties, which can be serialized
                updateFields(changeLogItem, baseChangeLogItem, instance.get());

                changeLogItem.setArtifactId(issueId);
                changeLogItem.setCorrespondingArtifactIdInSource(issueKey);
                changeLogItem.setCorrespondingArtifactId(issueId);
                changeLogItem.setId(issueId);
                changeLogItem.setTimeCreated(logTime);

                PropertyType propertyType = this.instanceType.getPropertyType(baseChangeLogItem.fieldId);
                if(propertyType != null) {
                    if (propertyType.cardinality() == Cardinality.SET) {
                        try {
                            //since arrays may occur in three different formats, we have to handle each case separately
                            if (baseChangeLogItem.oldValue == null && baseChangeLogItem.newValue == null) {
                                if (baseChangeLogItem.fromString != null) {
                                    fromData = stringToArrayListOtherFormat(baseChangeLogItem.fromString);
                                }

                                if (baseChangeLogItem.toString != null) {
                                    toData = stringToArrayListOtherFormat(baseChangeLogItem.toString);
                                }
                            } else {
                                try {
                                    if (baseChangeLogItem.newValue != null) {
                                        toData = jsonToArrayList(baseChangeLogItem.newValue);
                                    }

                                    if (baseChangeLogItem.oldValue != null) {
                                        fromData = jsonToArrayList(baseChangeLogItem.oldValue);
                                    }
                                } catch (IOException e) {
                                    if (baseChangeLogItem.newValue != null) {
                                        toData = stringToArrayList(baseChangeLogItem.newValue);
                                    }

                                    if (baseChangeLogItem.oldValue != null) {
                                        fromData = stringToArrayList(baseChangeLogItem.oldValue);
                                    }
                                }
                            }

                            fromMap.put(baseChangeLogItem.fieldId, fromData);
                            toMap.put(baseChangeLogItem.fieldId, toData);
                            changeLogItem.setFrom(fromMap);
                            changeLogItem.setTo(toMap);

                            return Optional.of(changeLogItem);
                        } catch (Exception e) {
                            log.debug("JIRA_SERVICE: JiraCloudChangeLogItemFactory: Parsing array-field changes caused error!");
                            return Optional.empty();
                        }
                    } else {

                        try {
                            fromData = jsonToMap(baseChangeLogItem.newValue);
                            if (fromData == null && baseChangeLogItem.toString != null) {
                                fromData = baseChangeLogItem.toString;
                            }
                        } catch (Exception e) {
                            if (baseChangeLogItem.newValue != null && !baseChangeLogItem.newValue.equals("")) {
                                fromData = baseChangeLogItem.newValue;
                            } else {
                                if (baseChangeLogItem.toString != null) {
                                    fromData = baseChangeLogItem.toString;
                                }
                            }
                        }

                        try {
                            toData = jsonToMap(baseChangeLogItem.oldValue);
                            if (toData == null && baseChangeLogItem.fromString != null) {
                                toData = baseChangeLogItem.fromString;
                            }
                        } catch (Exception e) {
                            if (baseChangeLogItem.oldValue != null && !baseChangeLogItem.oldValue.equals("")) {
                                toData = baseChangeLogItem.oldValue;
                            } else {
                                if (baseChangeLogItem.fromString != null) {
                                    toData = baseChangeLogItem.fromString;
                                }
                            }
                        }

                        if (toData == null) {
                            toData = "no information";
                        }

                        if (fromData == null) {
                            fromData = "no information";
                        }

                        fromMap.put(baseChangeLogItem.fieldId, toData);
                        toMap.put(baseChangeLogItem.fieldId, fromData);
                        changeLogItem.setFrom(fromMap);
                        changeLogItem.setTo(toMap);

                        return Optional.of(changeLogItem);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private ChangeLogItem updateFields(PropertyChangeLogItem changeLogItem, BaseChangeLogItem baseChangeLogItem, Instance instance) {

        Object fromData = null, toData = null;
        Map<String, Object> fromMap = new HashMap<>(), toMap = new HashMap<>();

        changeLogItem.setField(baseChangeLogItem.fieldId);
        changeLogItem.setId(baseChangeLogItem.id);

        if (instance.hasProperty(baseChangeLogItem.field)) {
            Property<?> property = instance.getProperty(baseChangeLogItem.field);
            if (property.propertyType().cardinality() == Cardinality.SET) {
                try {
                    try {
                        if (baseChangeLogItem.newValue != null) {
                            fromData = jsonToArrayList(baseChangeLogItem.newValue);
                        }
                        if (baseChangeLogItem.oldValue != null) {
                            toData = jsonToArrayList(baseChangeLogItem.oldValue);
                        }
                    } catch (IOException e) {
                        //Jira is inconsistent with it's array fields,
                        //there are custom fields of type string, which
                        //are structured by simply having a string containing
                        //keys separated by commas
                        if (baseChangeLogItem.newValue != null) {
                            fromData = stringToArrayList(baseChangeLogItem.newValue);
                        }
                        if (baseChangeLogItem.oldValue != null) {
                            toData = stringToArrayList(baseChangeLogItem.oldValue);
                        }
                    }

                    fromMap.put(baseChangeLogItem.field, toData);
                    toMap.put(baseChangeLogItem.field, fromData);
                    changeLogItem.setFrom(fromMap);
                    changeLogItem.setTo(toMap);

                    return changeLogItem;
                } catch (Exception e) {
                    log.debug("JIRA_SERVICE:  JiraCloudChangeLogItemFactory: ChangeLogItem for array field was not generated!");
                    e.printStackTrace();
                }
            }
        }

        try {
            fromData = jsonToMap(baseChangeLogItem.newValue);
            if (fromData == null && baseChangeLogItem.toString != null) {
                fromData = baseChangeLogItem.toString;
            }
        } catch (Exception e) {
            if (baseChangeLogItem.newValue != null && !baseChangeLogItem.newValue.equals("")) {
                fromData = baseChangeLogItem.newValue;
            } else {
                if (baseChangeLogItem.toString != null) {
                    fromData = baseChangeLogItem.toString;
                }
            }
        }

        try {
            toData = jsonToMap(baseChangeLogItem.oldValue);
            if (toData == null && baseChangeLogItem.fromString != null) {
                toData = baseChangeLogItem.fromString;
            }
        } catch (Exception e) {
            if (baseChangeLogItem.oldValue != null && !baseChangeLogItem.oldValue.equals("")) {
                toData = baseChangeLogItem.oldValue;
            } else {
                if (baseChangeLogItem.fromString != null) {
                    toData = baseChangeLogItem.fromString;
                }
            }
        }

        if (toData == null) {
            toData = "no information";
        }

        if (fromData == null) {
            fromData = "no information";
        }

        fromMap.put(baseChangeLogItem.field, toData);
        toMap.put(baseChangeLogItem.field, fromData);
        changeLogItem.setFrom(fromMap);
        changeLogItem.setTo(toMap);

        return changeLogItem;

    }

    private String reformatTime(String timeCreated) {
        String modifiedTime = timeCreated.replace('T', ' ');
        modifiedTime = modifiedTime.substring(0, modifiedTime.indexOf('+'));
        return modifiedTime;
    }

    public static Map<String, Object> jsonToMap(String json) throws IOException {
        if(json==null) return null;
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
    }

    public static ArrayList<Object> jsonToArrayList(String json) throws IOException {
        if(json==null) return null;
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<ArrayList<Object>>(){});
    }

    public static ArrayList<String> stringToArrayList(String arrayString) {
        arrayString.replace("[", "");
        arrayString.replace("]", "");
        String[] splitted = arrayString.split(",");
        ArrayList<String> result = new ArrayList();
        for (String split : splitted) {
            result.add(split.trim());
        }
        return result;
    }

    public static ArrayList<String> stringToArrayListOtherFormat(String arrayString) {
        String[] splitted = arrayString.split(" ");
        ArrayList<String> result = new ArrayList();
        for (String split : splitted) {
            result.add(split.trim());
        }
        return result;
    }

    private String resolveId(String key) {
        Optional<Instance> instance = this.artifactPusher.findArtifact(key);
        if (instance.isPresent()) {
            return instance.get().getProperty(BaseElementType.ID).getValue().toString();
        } else {
            return jiraTicketService.getArtifactIdFromKeyServer(key);
        }
    }


}
