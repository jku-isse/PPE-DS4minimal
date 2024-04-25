package at.jku.isse.designspace.core.trees.modelhistory.conflict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.events.ElementDelete;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.events.PropertyDelete;
import at.jku.isse.designspace.core.events.PropertyUpdate;
import at.jku.isse.designspace.core.events.PropertyUpdateAdd;
import at.jku.isse.designspace.core.events.PropertyUpdateRemove;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTree;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeNode;

public class Conflict {
    public boolean overwriteParent;
    public boolean merge;//only for set & map properties
    public ConflictTier tier;
    public long elementId;
    public String propertyName;

    public List<ModelHistoryTreeNode> sourceNodes;
    public List<ModelHistoryTreeNode> targetNodes;

    public List<Operation> resolutionPrepareUpdate = new LinkedList<>();

    public List<Operation> resolutionUpdate = new LinkedList<>();
    //public List<Operation> resolutionExecute = new LinkedList<>();

    @Override
    public String toString() {
        return "Conflict{" +
                "overwrite=" + overwriteParent +
                ", tier=" + tier +
                ", elementId=" + elementId +
                ", propertyName='" + propertyName + '\'' +
                '}';
    }

    public Conflict(List<ModelHistoryTreeNode> sourceNodes, List<ModelHistoryTreeNode> targetNodes, boolean overwriteParent) {
        this.overwriteParent = overwriteParent;
        this.sourceNodes = sourceNodes;
        this.targetNodes = targetNodes;
        scan();
        resolve();
    }

    private void scan() {
        tier = ConflictTier.FIRST;
        for (var sourceOperation : sourceNodes) {
            elementId = sourceOperation.idElement();
            if (sourceOperation.data instanceof ElementDelete || sourceOperation.data instanceof ElementCreate){
                tier = ConflictTier.LEVERAGE;
                break;
            }
            if(sourceOperation.data instanceof PropertyUpdate){
                var index = ((PropertyUpdate) sourceOperation.data).indexOrKey();
                propertyName = ((PropertyUpdate) sourceOperation.data).name();
                if(index instanceof Integer && ((int)index) > -1){
                    tier = ConflictTier.SECOND;
                }
            }
        }
        if(tier != ConflictTier.LEVERAGE){
            for (var targetOperation : targetNodes) {
                if (targetOperation.data instanceof ElementDelete || targetOperation.data instanceof ElementCreate){
                    tier = ConflictTier.LEVERAGE;
                    break;
                }
                if(targetOperation.data instanceof PropertyUpdate){
                    var index = ((PropertyUpdate) targetOperation.data).indexOrKey();
                    if(index instanceof Integer && ((int)index) > -1){
                        tier = ConflictTier.SECOND;
                    }else if(targetOperation.data instanceof PropertyUpdateAdd && ((index instanceof Integer && ((int)index) == -1) || index instanceof String)){
                        merge = true;
                    }
                }
            }
        }
    }

    private int countPreAndIntraIndexKeyOperations(List<ModelHistoryTreeNode> sortedListAfterIndex, int operationIndexKey) {
        Supplier<List<Integer>> supplier = () -> new ArrayList<Integer>();
        var arr = sortedListAfterIndex.stream().map(x -> (int)((PropertyUpdate)x.data).indexOrKey()).collect(Collectors.toCollection(supplier));
        var arrayTransformIndex = new HashMap<Integer,Integer>();

        var indexSumBuff = 0;
        //count how many operations are before the index, +1 for add, -1 for remove, this is the transformation value
        for (int i = 0; i < arr.size(); i++) {
            arrayTransformIndex.put(i, indexSumBuff);
            if(sortedListAfterIndex.get(i).data instanceof PropertyUpdateAdd){
                indexSumBuff++;
            }else{
                indexSumBuff--;
            }
        }

        if(arr.size() == 0){
            return 0;
        }else if(arr.size() == 1){
            return arr.get(0) <= operationIndexKey? indexSumBuff:0;
            //return indexSumBuff;
        }

        int firstIndex = 0;
        int lastIndex = arr.size()-1;

        // termination condition (element isn't present)
        while(firstIndex != lastIndex && firstIndex < lastIndex) {
            int middleIndex = ((firstIndex + lastIndex) / 2);
            // if the middle element is our goal element, return its index
            if (arr.get(middleIndex) <= operationIndexKey) {
                firstIndex = middleIndex + 1;
            }
            else if (arr.get(middleIndex) > operationIndexKey) {
                lastIndex = middleIndex - 1;
            }
        }
        //return firstIndex;
        return arrayTransformIndex.get(firstIndex);
    }

    private int calculateTransformationIndex(List<PropertyUpdate> operations) {
        var count = 0;
        for(var op : operations){
            if(op instanceof PropertyUpdateAdd){
                count++;
            }else{
                count--;
            }
        }
        return count;
    }


    private void resolve() {

        var preNodes = sourceNodes; //sourceNodes are always the parent nodes, only difference is the commitToParent flag
        var postNodes = targetNodes; //targetNodes are always the child nodes, only difference is the commitToParent flag

        if (tier == ConflictTier.LEVERAGE) { //element delete <-> property change conflict, only resolution is to redo the element deletion
            if (overwriteParent) {
                //var lastOperationPre = preNodes.get(preNodes.size()-1).data;
                //if (lastOperationPre instanceof ElementDelete) {
                //resolution.add(lastOperationPre.invert());
                //}
                if(preNodes.stream().anyMatch(x -> x.data instanceof ElementDelete)) {
                    for (var preOperation : preNodes) {
                        resolutionUpdate.add(preOperation.data.invert());
                    }
                }
            } else {
                //var lastOperationPost = postNodes.get(postNodes.size()-1).data;
                // if (lastOperationPost instanceof ElementDelete) {
                //resolution.add(lastOperationPost.invert());
                // }
                if(postNodes.stream().anyMatch(x -> x.data instanceof ElementDelete)) {
                    for (var postOperation : postNodes) {
                        resolutionUpdate.add(postOperation.data.invert());
                    }
                }
            }
        }
        else if (tier == ConflictTier.FIRST) {
            if(overwriteParent) {
                //repeat source last state change, set or delete
                var lastOperationPre = preNodes.get(preNodes.size()-1).data;
                var index = ((PropertyUpdate) lastOperationPre).indexOrKey();
                if (lastOperationPre instanceof PropertyDelete || lastOperationPre instanceof ElementDelete) {
                    //resolutionUpdate.add(lastOperationPre.invert());
                } else if(merge && index instanceof String) {
                    var lastOperationPost = postNodes.get(postNodes.size()-1).data;
                    var indexLast = ((PropertyUpdate) lastOperationPost).indexOrKey();
                    if(index == indexLast){
                        resolutionUpdate.add(lastOperationPost.clone());
                    }
                }
            } else {
                //repeat target last state change, set or delete
                var lastOperationPost = preNodes.get(preNodes.size()-1).data;
                if (lastOperationPost instanceof PropertyDelete || lastOperationPost instanceof ElementDelete) {
                    resolutionUpdate.add(lastOperationPost.invert());
                    if (preNodes.size() > 2) {
                        var beforeDeleteOperation = preNodes.get(preNodes.size()-2).data;
                        resolutionUpdate.add(beforeDeleteOperation.clone());
                    }
                } else {
                    resolutionUpdate.add(lastOperationPost.clone());
                }
            }
        } else if (tier == ConflictTier.SECOND) {

            var preOperations = preNodes.stream().filter(x -> x.data instanceof PropertyUpdate).map(x -> (PropertyUpdate)x.data).collect(Collectors.toList());
            var postOperations = postNodes.stream().filter(x -> x.data instanceof PropertyUpdate).map(x -> (PropertyUpdate)x.data).collect(Collectors.toList());

            var preNodesSortedAfterIndex = preNodes.stream().sorted(Comparator.comparingInt(a -> (int) ((PropertyUpdate) a.data).indexOrKey())).collect(Collectors.toList());
            var postNodesSortedAfterIndex = postNodes.stream().sorted(Comparator.comparingInt(a -> (int) ((PropertyUpdate) a.data).indexOrKey())).collect(Collectors.toList());
            var conclusionId = ModelHistoryTree.getNextConclusionId();
            if(overwriteParent) {
                for (var pre : preOperations) {
                    var invert = pre.invert();
                    invert.conclude(conclusionId);
                    resolutionPrepareUpdate.add(invert);

//                    if(postOperations.stream().anyMatch(x -> x.indexOrKey() == pre.indexOrKey() && x.value() == pre.value())){
//                        continue;
//                    }

                    if(pre instanceof PropertyUpdateRemove) {
                        var countPreAndIntraIndexKeyOperations = countPreAndIntraIndexKeyOperations(postNodesSortedAfterIndex, (int)((PropertyUpdate) pre).indexOrKey());
                        //var countPreAndIntraIndexKeyOperations = calculateTransformationIndex(postOperations);
                        var transform = ((PropertyUpdate) pre).transform(countPreAndIntraIndexKeyOperations);
                        transform.conclude(conclusionId);
                        resolutionUpdate.add(transform);

                        if((int)((PropertyUpdate)transform).indexOrKey() < 0) {
                            throw new IllegalStateException("Something went wrong with the operation transformation!");
                        }
                    } else {
                        var clone = pre.clone();
                        clone.conclude(conclusionId);
                        resolutionUpdate.add(clone);
                    }
                }
                Collections.reverse(resolutionPrepareUpdate);
            } else {
                for (var pre : preOperations) {
                    var invert = pre.invert();
                    invert.conclude(conclusionId);
                    resolutionPrepareUpdate.add(invert);

//                    if(postOperations.stream().anyMatch(x -> x.indexOrKey() == pre.indexOrKey() && x.value() == pre.value())) {
//                        continue;
//                    }

                    var countPreAndIntraIndexKeyOperations = 0;
                    if(pre instanceof PropertyUpdateRemove) {

                        countPreAndIntraIndexKeyOperations = countPreAndIntraIndexKeyOperations(postNodesSortedAfterIndex, (int)((PropertyUpdate) pre).indexOrKey());
                    } else {
                        countPreAndIntraIndexKeyOperations = calculateTransformationIndex(postOperations);
                    }

                    //var countPreAndIntraIndexKeyOperations = countPreAndIntraIndexKeyOperations(postNodesSortedAfterIndex, (int)((PropertyUpdate) pre).indexOrKey());

                    var transform = ((PropertyUpdate) pre).transform(countPreAndIntraIndexKeyOperations);
                    transform.conclude(conclusionId);
                    resolutionUpdate.add(transform);

                    if((int)((PropertyUpdate)transform).indexOrKey() < 0){
                        throw new IllegalStateException("Something went wrong with the operation transformation!");
                    }
                }
                Collections.reverse(resolutionPrepareUpdate);
            }

        } else {
            //ignore this tier -> inconsistencies induced by conflict resolutions.
        }
    }

}
