package at.jku.isse.designspace.jira.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.CaseUtils;

import at.jku.isse.designspace.artifactconnector.core.converter.IConverter;
import at.jku.isse.designspace.artifactconnector.core.converter.ISchemaCache;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraSchemaConverter implements IConverter {

    private static final String TYPE = "type", ITEM_TYPE = "items";
    private static final String ARTIFACT = "jira_artifact", CUSTOM = "custom";

    public static final String RESERVED_PROPERTY_PREFIX_FIELDNAME = "@fieldName/";
    public static final String RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE = "@originFieldType/";
    public static final String RESERVED_PROPERTY_PREFIX_RELATION_OUTWARD_INWARD = "@relationOutwardInward/";
    public static final String RESERVED_PROPERTY_PREFIX_RELATION_INWARD_OUTWARD = "@relationInwardOutward/";

    public enum JIRA_TYPE {
        ISSUE("issue"), ANY("any"), USER("user"), ISSUE_TYPE("issuetype"),
        PRIORITY("priority"), STATUS("status"), PROJECT("project"),
        RESOLUTION("resolution"), ISSUE_LINKS("issuelinks"), VERSION("version"),
        FIX_VERSION("fixVersion"), STATUS_CATEGORY("statusCategory"), COMPONENT("component"),
        STRING("string");

        public final String value;

        public static Set<String> getValues() {
            HashSet<String> typeValues = new HashSet<>();
            for (JIRA_TYPE type : JIRA_TYPE.values()) {
                typeValues.add(type.value);
            }
            return typeValues;
        }

        JIRA_TYPE(String name) {
            this.value = name;
        }
    }

    public final static Map<String, InstanceType> JIRA_TYPE_NAME_INSTANCE_TYPE_MAPPING = new HashMap<>(){{
        //put(JIRA_TYPE.ISSUE.value, BaseElementType.ARTIFACT.getType());
        put(JIRA_TYPE.ISSUE.value, JiraBaseElementType.JIRA_ARTIFACT.getType());
        put(JIRA_TYPE.USER.value, JiraBaseElementType.JIRA_USER.getType());
        put(JIRA_TYPE.PROJECT.value, JiraBaseElementType.JIRA_PROJECT.getType());
        put(JIRA_TYPE.RESOLUTION.value, JiraBaseElementType.JIRA_RESOLUTION.getType());
        put(JIRA_TYPE.VERSION.value, JiraBaseElementType.JIRA_VERSION.getType());
        put(JIRA_TYPE.FIX_VERSION.value, JiraBaseElementType.JIRA_VERSION.getType());
        put(JIRA_TYPE.COMPONENT.value, JiraBaseElementType.JIRA_COMPONENT.getType());
        put(JIRA_TYPE.ISSUE_LINKS.value, JiraBaseElementType.JIRA_ARTIFACT.getType());

        put(JIRA_TYPE.ANY.value, Workspace.STRING);
        put(JIRA_TYPE.STRING.value, Workspace.STRING);
        put(JIRA_TYPE.ISSUE_TYPE.value, Workspace.STRING);
        put(JIRA_TYPE.PRIORITY.value, Workspace.STRING);
        put(JIRA_TYPE.STATUS.value, Workspace.STRING);
        put(JIRA_TYPE.STATUS_CATEGORY.value, Workspace.STRING);
    }};


    public Workspace workspace;
    private ISchemaCache schemaCache;

    public JiraSchemaConverter(Workspace workspace) {
        this.workspace = workspace;
        schemaCache = new JiraSchemaCache(workspace);
    }

    /**
     * in case we are looking for schema, which extends the core schema
     * we have to use this method.
     */
    @Override
    public Optional<InstanceType> findSchema(String schemaId) {
        return Optional.ofNullable(schemaCache.getSchema(schemaId));
    }


    @Override
    public InstanceType createSchema(Map<String, Object> schemaMap, Map<String, Object> issueLinkTypes, Map<String, Object> namesLabelMap, String schemaId) {
        /*
         * before we create a schema, we always check if the schema already exists
         */
        Optional<InstanceType> existingSchema = findSchema(schemaId);

        if(existingSchema.isEmpty()) {
            InstanceType coreTypeExtension = WorkspaceService.createInstanceType(workspace, ARTIFACT, JiraBaseElementType.typeFolder, JiraBaseElementType.JIRA_ARTIFACT.getType());
            MapProperty<String> propertyMetadata = coreTypeExtension.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);

            Map<String, Object> schemaEntry, relationTypeEntry;
            Map<String, InstanceType> fieldToType = new HashMap<>();

            //schemaMap.put("fix version", schemaMap.get("fixVersions"));

            schemaMap.remove(JiraBaseElementType.LINKED_ISSUES);
            for (Map.Entry<String, Object> entry : schemaMap.entrySet()) {
                schemaEntry = ((Map<String, Object>) entry.getValue());

                boolean array = false;
                String fieldId = entry.getKey();
                String jiraType = (String) schemaEntry.get(TYPE);
                if (jiraType.equals(JiraBaseElementType.ARRAY_)) {
                    jiraType = (String) schemaEntry.get(ITEM_TYPE);
                }

                String propertyName = labelToProperty((String) namesLabelMap.get(fieldId));
                InstanceType instanceType = addProperty(schemaEntry, propertyName, coreTypeExtension);

                if(instanceType != null) {
                    if (instanceType == BaseElementType.ARTIFACT.getType() || instanceType == JiraBaseElementType.JIRA_ARTIFACT.getType()) {
                        fieldToType.put(entry.getKey(), coreTypeExtension);
                    } else {
                        fieldToType.put(entry.getKey(), instanceType);
                    }                    
                    if (propertyName != null) {
                        propertyMetadata.put(RESERVED_PROPERTY_PREFIX_FIELDNAME + fieldId, propertyName);
                        propertyMetadata.put(RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + propertyName, jiraType);
                    }
                }
            }

            //propertyMetadata.put(RESERVED_PROPERTY_PREFIX_RELATION_INWARD_OUTWARD + "parent", "subtask");
            //propertyMetadata.put(RESERVED_PROPERTY_PREFIX_RELATION_OUTWARD_INWARD + "subtask", "parent");

            for (Map.Entry<String, Object> entry : issueLinkTypes.entrySet()) {
                relationTypeEntry = ((Map<String, Object>) entry.getValue());
                Object inward = relationTypeEntry.get("inward");
                Object outward = relationTypeEntry.get("outward");

                if (inward != null && outward != null) {
                    String inwardString = labelToProperty(inward.toString());
                    String outwardString = labelToProperty(outward.toString());

                    propertyMetadata.put(RESERVED_PROPERTY_PREFIX_RELATION_INWARD_OUTWARD + inwardString, outwardString);
                    propertyMetadata.put(RESERVED_PROPERTY_PREFIX_RELATION_OUTWARD_INWARD + outwardString, inwardString);

                    addRelationProperty(inwardString, coreTypeExtension);
                    addRelationProperty(outwardString, coreTypeExtension);
                }
            }

            this.schemaCache.addSchema(coreTypeExtension, schemaId);
            return coreTypeExtension;
        } else {
            return existingSchema.get();
        }
    }

    public String labelToProperty(String label) {    	
    	String prop = CaseUtils.toCamelCase(label, false, ' ', '_', '.', '-', '/', '\\', '[', ']', '@', '\u03A3', '&', '?', '(', ')');	
    	if (prop == null)
    		return null;
    	else {
    		if (prop.equalsIgnoreCase("subTasks"))
    			return "subtasks"; // hack to support subtask relations in 'Artifact'
    		else
    			return prop.trim();	
    	}
    	    	
    }
    
    @Override
    public void synchronizeSchemata(InstanceType existingSchema, Map<String, Object> newSchemaMap, Map<String, Object> newLinkTypeMap) {
        Instance exampleInstance = this.workspace.createInstance(existingSchema, "exampleInstance");
        for (Map.Entry<String, Object> entry : newSchemaMap.entrySet()) {
            if (!exampleInstance.hasProperty(entry.getKey())) {
                Object value = entry.getValue();
                if (value != null) {
                    Map<String, Object> schemaEntryMap = (Map<String, Object>) value;
                    addProperty(schemaEntryMap, entry.getKey(), existingSchema);
                }
            }
        }

        for (Map.Entry<String, Object> entry : newLinkTypeMap.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                Map<String, Object> relationType = (Map<String, Object>) value;
                Object sourceRole = relationType.get("outward");
                Object destinationRole = relationType.get("inward");

                if (sourceRole != null && destinationRole != null) {
                    String sourceRoleString = labelToProperty(sourceRole.toString());
                    String destinationRoleString = labelToProperty(destinationRole.toString());

                    if (!exampleInstance.hasProperty(sourceRoleString)) {
                        WorkspaceService.createPropertyType(this.workspace, existingSchema, sourceRoleString, Cardinality.SET, existingSchema);
                    }

                    if (!exampleInstance.hasProperty(destinationRoleString)) {
                        WorkspaceService.createPropertyType(this.workspace, existingSchema, destinationRoleString, Cardinality.SET, existingSchema);
                    }
                }
            }
        }

        //this.workspace.concludeTransaction();
    }

    private void addRelationProperty(String relationRole, InstanceType root) {
        if (!root.hasProperty(relationRole)) {
            WorkspaceService.createPropertyType(this.workspace, root, relationRole, Cardinality.SET, root);
        }
    }

    private InstanceType addProperty(Map<String, Object> schemaEntry, String fieldName, InstanceType root) {

        PropertyType<?> propertyType = root.getPropertyType(fieldName);
        if (propertyType != null) {
            return propertyType.referencedInstanceType();
        }

        String elemType;
        InstanceType instanceType;

        elemType = (String) schemaEntry.get(TYPE);
        Object custom_ = schemaEntry.get(CUSTOM);
        Optional<String> custom = Optional.empty();
        if (custom_ != null) {
            custom = Optional.of((String) custom_);
        }

        instanceType = this.JIRA_TYPE_NAME_INSTANCE_TYPE_MAPPING.get(elemType.toLowerCase());

        if (instanceType == null) {
            if(schemaEntry.get(TYPE).equals(BaseElementType.ARRAY_)) {
                elemType = (String) schemaEntry.get(ITEM_TYPE);
                instanceType = this.JIRA_TYPE_NAME_INSTANCE_TYPE_MAPPING.get(elemType.toLowerCase());
                if (instanceType == null) {
                    instanceType = findPrimitiveProperty(elemType, custom);
                    if(instanceType!=null) {
                        WorkspaceService.createPropertyType(workspace, root, fieldName, Cardinality.SET, instanceType);
                        return instanceType;
                    }

                    //for creating a schema of unfamiliar types, one would have to call createShema() here
                    //with the sub-map we have at this point. The root of that schema
                    //would have to be added as subtype to the current propertyType

                    return null;
                } else {
                    WorkspaceService.createPropertyType(workspace, root, fieldName, Cardinality.SET, instanceType);
                    return instanceType;
                }
            } else {
                instanceType =  findPrimitiveProperty(elemType, custom);
                if(instanceType!=null) {
                    try {
                        WorkspaceService.createPropertyType(workspace, root, fieldName, Cardinality.SINGLE, instanceType);
                    } catch (IllegalArgumentException ie) {
                        log.error("Jira-Service: Creating the propertyType " + fieldName + "failed");
                    }
                    return instanceType;
                }
                return null;
            }
        } else {
            try {
                WorkspaceService.createPropertyType(workspace, root, fieldName, Cardinality.SINGLE, instanceType);
            } catch (IllegalArgumentException ie) {
                log.error("Jira-Service: Creating the propertyType " + fieldName + "failed");
            }
            return instanceType; //TODO: some fields such as votes, watcher, security level and progress are ignored here
        }
    }

    private InstanceType findPrimitiveProperty(String elemType, Optional<String> custom) {

        String elemTypeLoweCase = elemType.toLowerCase();
        if (elemTypeLoweCase.equals("string") || elemTypeLoweCase.equals("option")) {
            return Workspace.STRING;
        } else if (elemTypeLoweCase.equals("integer")) {
            return Workspace.INTEGER;
        } else if (elemType.equalsIgnoreCase("boolean")) {
            return Workspace.BOOLEAN;
        } else if (elemType.equalsIgnoreCase("number")) {
            if (custom.isPresent() && custom.get().contains("float")) {
                return Workspace.STRING;
            }
            return Workspace.INTEGER;
        } else if (elemType.equalsIgnoreCase("date") || elemType.equalsIgnoreCase("datetime")) {
            return Workspace.STRING;
        } else {
            return null;
        }
    }

    public static Optional<InstanceType> resolveFieldIdToInstanceType(String fieldId, InstanceType instanceType) {
        if (fieldId != null && instanceType != null) {
            MapProperty<String> propertyMetadata = instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
            String fieldName = propertyMetadata.get(RESERVED_PROPERTY_PREFIX_FIELDNAME + fieldId);
            if (fieldName != null) {
                String jiraType = propertyMetadata.get(RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + fieldName);

                if (jiraType != null) {
                    InstanceType returnType = JIRA_TYPE_NAME_INSTANCE_TYPE_MAPPING.get(jiraType);
                    if (returnType != null) {
                        return Optional.of(returnType);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<InstanceType> resolveLabelToInstanceType(String fieldLabel, InstanceType instanceType) {
        if (fieldLabel != null && instanceType != null) {
            MapProperty<String> propertyMetadata = instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
            String jiraType = propertyMetadata.get(RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + fieldLabel);

            if (jiraType != null) {
                InstanceType returnType = JIRA_TYPE_NAME_INSTANCE_TYPE_MAPPING.get(jiraType);
                if (returnType != null) {
                    return Optional.of(returnType);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<String> resolveFieldNameToJiraType(String fieldName, InstanceType instanceType) {
        if (fieldName != null && instanceType != null) {
            MapProperty<String> propertyMetadata = instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
            String jiraType = propertyMetadata.get(RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + fieldName);

            if (jiraType != null) {
                return Optional.of(jiraType);
            }

        }
        return Optional.empty();
    }

    public static Optional<String> resolveFieldIdToProperty(String fieldId, InstanceType instanceType) {
        if (fieldId != null && instanceType != null) {
            MapProperty<String> propertyMetadata = instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
            String propertyName = propertyMetadata.get(RESERVED_PROPERTY_PREFIX_FIELDNAME + fieldId);

            if (propertyName != null) {
                return Optional.of(propertyName);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> resolveFieldIdToLabel(String fieldId, InstanceType instanceType) {
        if (fieldId != null && instanceType != null) {
            MapProperty<String> propertyMetadata = instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
            String label = propertyMetadata.get(RESERVED_PROPERTY_PREFIX_FIELDNAME + fieldId);
            if (label != null) {
                return Optional.of(label);
            }
        }
        return Optional.empty();
    }


}
