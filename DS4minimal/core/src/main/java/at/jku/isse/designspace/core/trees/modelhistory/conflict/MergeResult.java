package at.jku.isse.designspace.core.trees.modelhistory.conflict;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.helper.HashMapList;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeNode;

public class MergeResult {
    public boolean overwriteParent;

    public List<ModelHistoryTreeNode> listSource;
    public HashMapList<Long, ModelHistoryTreeNode>  elementsInSourceContext;
    public HashMapList<Long, ModelHistoryTreeNode>  elementsInSourceContextNonPropertyNodes;
    public HashMap<Long, HashMapList<String, ModelHistoryTreeNode>> elementPropertiesInSourceContext;

    public List<ModelHistoryTreeNode> listTarget;
    public HashMapList<Long, ModelHistoryTreeNode> elementsInTargetContext;
    public HashMapList<Long, ModelHistoryTreeNode>  elementsInTargetContextNonPropertyNodes;
    public HashMap<Long, HashMapList<String, ModelHistoryTreeNode>>  elementPropertiesInTargetContext;

    public List<Long> conflictingElementKeys;
    public HashMapList<Long, Collection<String>> conflictingElementPropertyNames;
    public List<Conflict> conflicts = new ArrayList<>();

    public MergeResult(List<ModelHistoryTreeNode> listSource, List<ModelHistoryTreeNode> listTarget, boolean overwriteParent) {
        this.overwriteParent = overwriteParent;
        this.listSource = listSource;
        this.listTarget = listTarget;
        resolve();
    }

    public int conflictsCount(){
        //return conflictingElementKeys.size() - conflictingElementPropertyNames.keySet().size() + conflictingElementPropertyNames.values().stream().mapToInt(ArrayList::size).sum();
        return conflicts.size();
    }

    private void resolve(){
        elementsInSourceContext = new HashMapList<>();
        elementPropertiesInSourceContext = new HashMap<>();
        elementsInSourceContextNonPropertyNodes = new HashMapList<>();
        for (var node : listSource) {
            elementsInSourceContext.put(node.idElement(), node);
            if((node.data).deletedGroupId != -1){
                elementsInSourceContextNonPropertyNodes.put((node.data).deletedGroupId , node);
                continue;
            }
            if(!(node.data instanceof ElementUpdate)){
                elementsInSourceContextNonPropertyNodes.put(node.idElement(), node);
                continue;
            }
            if(!elementPropertiesInSourceContext.containsKey(node.idElement())){
                elementPropertiesInSourceContext.put(node.idElement(), new HashMapList<>());
            }
            elementPropertiesInSourceContext.get(node.idElement()).put(((ElementUpdate) node.data).name(), node);
        }

        elementsInTargetContext = new HashMapList<>();
        elementPropertiesInTargetContext = new HashMap<>();
        elementsInTargetContextNonPropertyNodes = new HashMapList<>();
        for (var node : listTarget) {
            elementsInTargetContext.put(node.idElement(), node);
            if((node.data).deletedGroupId != -1){
                elementsInTargetContextNonPropertyNodes.put((node.data).deletedGroupId , node);
                continue;
            }
            if(!(node.data instanceof ElementUpdate)){
                elementsInTargetContextNonPropertyNodes.put(node.idElement(), node);
                continue;
            }
            if(!elementPropertiesInTargetContext.containsKey(node.idElement())){
                elementPropertiesInTargetContext.put(node.idElement(), new HashMapList<>());
            }
            elementPropertiesInTargetContext.get(node.idElement()).put(((ElementUpdate) node.data).name(), node);
        }

        conflictingElementKeys = CollectionUtils.intersection(elementsInSourceContext.keySet(), elementsInTargetContext.keySet()).stream().collect(Collectors.toList());
        conflictingElementPropertyNames = new HashMapList<>();
        for (var conflictingElementKey : conflictingElementKeys) {
            var propertiesInSourceContext = elementPropertiesInSourceContext.get(conflictingElementKey);
            var propertiesInTargetContext = elementPropertiesInTargetContext.get(conflictingElementKey);

            if(propertiesInSourceContext != null && propertiesInTargetContext != null){//conflicting properties
                var propertiesInConflict = CollectionUtils.intersection(propertiesInSourceContext.keySet(), propertiesInTargetContext.keySet());
                if (!propertiesInConflict.isEmpty()) {
                    conflictingElementPropertyNames.put(conflictingElementKey,propertiesInConflict);
                    for(var conflictingPropertyName : propertiesInConflict){
                        Conflict newPropertyConflict = new Conflict(propertiesInSourceContext.get(conflictingPropertyName), propertiesInTargetContext.get(conflictingPropertyName), overwriteParent);
                        conflicts.add(newPropertyConflict);
                    }
                }
            }

            if(elementsInSourceContextNonPropertyNodes.containsKey(conflictingElementKey) && !elementsInSourceContextNonPropertyNodes.get(conflictingElementKey).isEmpty()
                    && elementsInTargetContextNonPropertyNodes.containsKey(conflictingElementKey) && !elementsInTargetContextNonPropertyNodes.get(conflictingElementKey).isEmpty()){
                Conflict newElementConflict = new Conflict(elementsInSourceContextNonPropertyNodes.get(conflictingElementKey), elementsInTargetContextNonPropertyNodes.get(conflictingElementKey), overwriteParent);
                conflicts.add(newElementConflict);
            }

            if (elementsInSourceContextNonPropertyNodes.containsKey(conflictingElementKey)
                    && !elementsInSourceContextNonPropertyNodes.get(conflictingElementKey).isEmpty()
                    && !elementPropertiesInTargetContext.get(conflictingElementKey).values().isEmpty()) {
                //element deleted in source and property changed in target conflict
                var allTargetPropertyOperations = propertiesInTargetContext.values().stream().flatMap(List::stream).collect(Collectors.toList());
                Conflict newElementConflict = new Conflict(elementsInSourceContextNonPropertyNodes.get(conflictingElementKey), allTargetPropertyOperations, overwriteParent);
                conflicts.add(newElementConflict);
            }

            if(elementsInTargetContextNonPropertyNodes.containsKey(conflictingElementKey)
                    && !elementsInTargetContextNonPropertyNodes.get(conflictingElementKey).isEmpty()
                    && !elementPropertiesInSourceContext.get(conflictingElementKey).values().isEmpty()) {
                //element deleted in target and property changed in source conflict
                var allSourcePropertyOperations = propertiesInSourceContext.values().stream().flatMap(List::stream).collect(Collectors.toList());
                Conflict newElementConflict = new Conflict(allSourcePropertyOperations, elementsInTargetContextNonPropertyNodes.get(conflictingElementKey), overwriteParent);
                conflicts.add(newElementConflict);
            }
        }
    }
}

