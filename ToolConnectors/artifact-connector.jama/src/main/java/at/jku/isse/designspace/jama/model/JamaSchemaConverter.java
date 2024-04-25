package at.jku.isse.designspace.jama.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.text.CaseUtils;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JamaSchemaConverter {

    private Workspace workspace;
    private JamaInstanceTypeCache jamaInstanceTypeCache;
    //private Map<String, Map<String, String>> itemTypeToFieldToInstanceTypeMapper; // not persisted thus no longer used but InstantTypePropertyMetadata used instead
   // private Map<String, Map<String, String>> itemTypeToFieldNameToFieldLabelMapper;

    public static final String RESERVED_PROPERTY_PREFIX_FIELDNAME = "@fieldName/";
    public static final String RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE = "@originFieldType/";
    
    public enum JAMA_TYPE {
        USER("USER"), PROJECT("PROJECT"), MULTI_LOOKUP("MULTI_LOOKUP"),
        STRING("STRING"), TEXT("TEXT"), URL_STRING("URL_STRING"), DATE("DATE"), LOOKUP("LOOKUP"), CALCULATED("CALCULATED"),
        TEST_CASE_STATUS("TEST_CASE_STATUS"), STEPS("STEPS"), TEST_RUN_RESULTS("TEST_RUN_RESULTS"),
        BOOLEAN("BOOLEAN"), INTEGER("INTEGER"), RELEASE("RELEASE");

        public final String value;
        JAMA_TYPE(String name) {
            this.value = name;
        }
    }

    public final static Map<String, InstanceType> JAMA_TYPE_NAME_INSTANCE_TYPE_MAPPING = new HashMap<>(){{
        put(JAMA_TYPE.USER.value, JamaBaseElementType.JAMA_USER.getType());
        put(JAMA_TYPE.PROJECT.value, JamaBaseElementType.JAMA_PROJECT.getType());
        put(JAMA_TYPE.RELEASE.value, JamaBaseElementType.JAMA_RELEASE.getType());

        //For Multi LookUps we need a more complex type
        put(JAMA_TYPE.MULTI_LOOKUP.value, JamaBaseElementType.JAMA_MULTI_PICKLIST_OPTION.getType());
        put(JAMA_TYPE.STRING.value, Workspace.STRING);
        put(JAMA_TYPE.TEXT.value, Workspace.STRING);
        put(JAMA_TYPE.CALCULATED.value, Workspace.STRING);
        put(JAMA_TYPE.DATE.value, Workspace.STRING);
        put(JAMA_TYPE.URL_STRING.value, Workspace.STRING);
        put(JAMA_TYPE.TEST_CASE_STATUS.value, Workspace.STRING);
        put(JAMA_TYPE.STEPS.value, Workspace.STRING);       
        put(JAMA_TYPE.TEST_RUN_RESULTS.value, Workspace.STRING);
        

        //For Lookups we only store the value
        put(JAMA_TYPE.LOOKUP.value, Workspace.STRING);
        put(JAMA_TYPE.BOOLEAN.value, Workspace.BOOLEAN);
        put(JAMA_TYPE.INTEGER.value, Workspace.INTEGER);
        //put(JAMA_TYPE.ROLLUP.value, Workspace.ROLLUP);
    }};

    public JamaSchemaConverter(Workspace workspace) {
        this.workspace = workspace;
        this.jamaInstanceTypeCache = new JamaInstanceTypeCache(this.workspace);
   //     this.itemTypeToFieldToInstanceTypeMapper = new HashMap<>();
   //     this.itemTypeToFieldNameToFieldLabelMapper = new HashMap<>();
    }

    public Optional<InstanceType> getOrCreateJamaItemType(Map<String, Object> data) {
        if (data != null) {
            try {
            	if (data.containsKey("data"))
            		data = (Map<String, Object>) data.get("data");
                ArrayList<Map<String, Object>> fields = (ArrayList<Map<String, Object>>) data.get("fields");
                String designspaceItemTypeId = "JamaItemType/" + data.get(BaseElementType.ID);

                //in case the type already exists we will return it immediately
                Optional<InstanceType> instanceType_ = this.getJamaInstanceType(designspaceItemTypeId);
                if (instanceType_.isPresent()) {
                    return instanceType_;
                }

                //Creating the extendedItemType as a descendant of the jama core item
                String itemTypeName = data.get(JamaBaseElementType.ITEM_TYPE_SHORT).toString();
                InstanceType coreTypeExtension = WorkspaceService.createInstanceType(workspace, itemTypeName, JamaBaseElementType.getJamaTypeFolder(), JamaBaseElementType.JAMA_CORE_ITEM.getType());

               // Map<String, String> fieldToInstanceTypeMapper = new HashMap<>();
                //Map<String, String> fieldToLabelMapper = new HashMap<>();
                //creating the types specific to the provided item type

                MapProperty<String> propertyMetadata = coreTypeExtension.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA); 
                
                for (Map<String, Object> field : fields) {
                    InstanceType instanceType = JamaSchemaConverter.JAMA_TYPE_NAME_INSTANCE_TYPE_MAPPING.get(field.get("fieldType"));
                    if (instanceType != null) {
                        String propertyName = field.get(BaseElementType.NAME).toString();
                    	String propertyLabel = labelToProperty(field.get(JamaBaseElementType.LABEL).toString());                    	
                        if (coreTypeExtension.getPropertyType(propertyLabel) == null) {
                            WorkspaceService.createPropertyType(workspace, coreTypeExtension, propertyLabel, Cardinality.SINGLE, instanceType);                            
                        }
                        propertyMetadata.put(RESERVED_PROPERTY_PREFIX_FIELDNAME+propertyName, propertyLabel);
                        propertyMetadata.put(RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE+propertyLabel, field.get("fieldType").toString());
                        //fieldToLabelMapper.put(propertyName, propertyLabel);
                        //fieldToInstanceTypeMapper.put(propertyLabel, field.get("fieldType").toString());
                    } else {
                    	log.debug("JAMA SERVICE: InstanceType Creator: unable to determine Designspace InstanceType for field: "+field.get("field.label")+" :: "+field.get("fieldType"));
                    }                    
                }

                //Adding the created InstanceType to the cache of jama instance types
                this.jamaInstanceTypeCache.putInstanceId(designspaceItemTypeId, coreTypeExtension.id());
                //this.itemTypeToFieldToInstanceTypeMapper.put(designspaceItemTypeId, fieldToInstanceTypeMapper);
                //this.itemTypeToFieldNameToFieldLabelMapper.put(designspaceItemTypeId, fieldToLabelMapper);

                return Optional.of(coreTypeExtension);

            } catch (Exception e) {            	
                log.debug("JAMA SERVICE: JamaModelConverter: createJamaType(Map itemTypeMap) was given an invalid JSON-format!", e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public static String labelToProperty(String label) {
    	return CaseUtils.toCamelCase(label, false, ' ', '_', '.', '-', '/', '\\').trim();
    }
    
    public Optional<InstanceType> getJamaInstanceType(String itemTypeId) {
        InstanceType instanceType = this.jamaInstanceTypeCache.getInstanceType(itemTypeId);
            if (instanceType != null) {
                return Optional.of(instanceType);
            }
        return Optional.empty();
    }

    public Optional<String> getJamaTypeForFieldNameOfItemType(String itemTypeId, String fieldName) {
        Optional<InstanceType> optType = getJamaInstanceType(itemTypeId);
        if (optType.isPresent()) {
        	Map<String,String> propertyMetadata = optType.get().getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA).get();
        	return Optional.ofNullable(propertyMetadata.get(RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE+fieldName));
        }
//    	Map<String, String> fieldToInstanceTypeMapper = this.itemTypeToFieldToInstanceTypeMapper.get(itemTypeId);
//        if (fieldToInstanceTypeMapper != null) {
//            String jamaType = fieldToInstanceTypeMapper.get(fieldName);
//            if (jamaType != null) {
//                return Optional.of(jamaType);
//            }
//        }
        return Optional.empty();
    }

    public Optional<InstanceType> getItemTypeForFieldOfItemType(String jamaType) {
        InstanceType instanceType = JamaSchemaConverter.JAMA_TYPE_NAME_INSTANCE_TYPE_MAPPING.get(jamaType);
        if (instanceType != null) {
            return Optional.of(instanceType);
        }
        return Optional.empty();
    }

	public Optional<String> getPropertyForFieldName(String itemTypeId, String fieldName) {
		 Optional<InstanceType> optType = getJamaInstanceType(itemTypeId);
	        if (optType.isPresent()) {
	        	Map<String,String> propertyMetadata = optType.get().getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA).get();
	        	return Optional.ofNullable(propertyMetadata.get(RESERVED_PROPERTY_PREFIX_FIELDNAME+fieldName));
	        }
		//		Map<String, String> fieldToLabelMapper = this.itemTypeToFieldNameToFieldLabelMapper.get(itemTypeId);
//        if (fieldToLabelMapper != null) {
//            String prop = fieldToLabelMapper.get(fieldName);
//            if (prop != null) {
//                return Optional.of(prop);
//            }
//        }
        return Optional.empty();
	}

}
