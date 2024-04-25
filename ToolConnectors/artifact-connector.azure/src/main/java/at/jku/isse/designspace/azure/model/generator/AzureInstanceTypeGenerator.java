package at.jku.isse.designspace.azure.model.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import at.jku.isse.designspace.azure.model.AzureBaseElementType;
import at.jku.isse.designspace.azure.service.IAzureService;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AzureInstanceTypeGenerator {

    private Map<String, InstanceType> existingTypes;
    private IAzureService azureService;

    public AzureInstanceTypeGenerator(IAzureService azureService) {
        this.azureService = azureService;
        existingTypes = new HashMap<>();

        existingTypes.put("Assigned To", AzureBaseElementType.AZURE_USER.getType());
        existingTypes.put("Created By", AzureBaseElementType.AZURE_USER.getType());
        //existingTypes.put("State", AzureBaseElementType.AZURE_STATE.getType());
        existingTypes.put("Team Project", AzureBaseElementType.AZURE_PROJECT.getType());
    }

    public InstanceType generateInstanceType(byte[] workItemTypeJson, byte[] fieldsJson, Workspace workspace) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(workItemTypeJson);
            System.out.println(rootNode.toString());
            ArrayNode fields = (ArrayNode) rootNode.get("fields");
            String workItemTypeName = rootNode.get("name").textValue();
            //lookup the work item type in id cache and use it
            Optional<Instance> workItemTypeInstance = azureService.searchForInstance(workItemTypeName);
            if(workItemTypeInstance.isEmpty()) {
                log.debug("AZURE-SERVICE: Work item type not found");
                return null;
            }
            JsonNode rootNode2 = mapper.readTree(fieldsJson);
            System.out.println(rootNode2.toString());
            ArrayNode allFields = (ArrayNode) rootNode2.get("value");

            if(fields == null || allFields == null) {
                log.debug("AZURE-SERVICE: There was a problem when parsing json during the creation of types");
                return null;
            }

            //InstanceType with AZURE_WORKITEM as its supertype
            InstanceType derivedInstanceType = WorkspaceService.createInstanceType(workspace, workItemTypeName + "_generatedArtifact",  AzureBaseElementType.typeFolder , AzureBaseElementType.AZURE_WORKITEM.getType());
            for(JsonNode field : fields) {
                String fieldName = field.get("name").textValue();
                System.out.println(fieldName);
                if(existingTypes.containsKey(fieldName)) {
                    return existingTypes.get(fieldName);
                }

                //go through another json response with information about field types
                for(JsonNode item : allFields) {
                    if(item.get("name").textValue().equals(fieldName)) {
                        String fieldType = item.get("type").textValue();
                        System.out.println(fieldType);
                        InstanceType instanceType = findPrimitiveProperty(fieldType);
                        if(instanceType != null) {
                            //extend the core type
                            try {
                                WorkspaceService.createPropertyType(workspace, derivedInstanceType, fieldName, Cardinality.SINGLE, instanceType);
                            } catch (IllegalArgumentException iae) {
                                log.debug("AZURE-SERVICE: Creating property type" + fieldName + "failed.");
                            }
                        }
                    }
                }
            }
            return derivedInstanceType;
        } catch (IOException e) {
            log.debug("AZURE-SERVICE There was an error while parsing json for generating a type");
        }
        return null;
    }

    private InstanceType findPrimitiveProperty(String elemType) {

        String elemTypeLowerCase = elemType.toLowerCase();
        if (elemTypeLowerCase.equals("string") || elemTypeLowerCase.equals("datetime") || elemTypeLowerCase.equals("plaintext")) {
            return Workspace.STRING;
        } else if (elemTypeLowerCase.equals("integer")) {
            return Workspace.INTEGER;
        } else if(elemTypeLowerCase.equals("double")) {
            return Workspace.REAL;
        } else if (elemType.equalsIgnoreCase("boolean")) {
            return Workspace.BOOLEAN;
        } else {
            return null;
        }

    }
}
