package at.jku.isse.designspace.rule.checker;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.SimpleDirectedGraph;

import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.User;
import at.jku.isse.designspace.core.model.Workspace;
//import at.jku.isse.designspace.core.model.*;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;
import at.jku.isse.designspace.rule.arl.expressions.Expression;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.ConsistencyRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.Repair;
import at.jku.isse.designspace.rule.arl.repair.RepairAction;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairTreeFilter;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.Change;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.ChangeEdge;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.ModelState;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.RepairChangePropagator;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.RepairPathNode;
import at.jku.isse.designspace.rule.model.ConsistencyRule;
import at.jku.isse.designspace.rule.model.ConsistencyRuleType;
import at.jku.isse.designspace.rule.model.ReservedNames;
import at.jku.isse.designspace.rule.service.RuleService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsistencyUtils {

    private static Workspace childW;

    public static void printCRDsAndCREs(Workspace workspace, String header, boolean ignoreHousekeepingTypes) {
        log.debug(header);
        log.debug("instances of workspace: " + workspace.name());
        for (InstanceType instanceType : workspace.debugInstanceTypes()) {
            log.debug("- InstanceType: " + instanceType);

            for (Instance instance : instanceType.instances()) {
                String details = "";
                for (PropertyType propertyType : instanceType.getPropertyTypes(true)) {
                    details += "(" + propertyType.name() + ":" + instance.getProperty(propertyType.name()) + ")";
                }
                log.debug("-     Instance: " + instance + " " + details);
            }
        }
    }

    public static boolean crdExists(Workspace workspace, String rule, InstanceType context, boolean hasError, boolean isUpdatedInThisWorkspace) {
        SetProperty<ConsistencyRuleType> crds = (SetProperty)workspace.its(ConsistencyRuleType.CONSISTENCY_RULE_TYPE).subTypes();
        for (ConsistencyRuleType crd : crds) {
            if (crd.contextInstanceType().equals(context) && crd.rule().equals(rule)) {
                if (crd.hasRuleError() != hasError) {
                    log.warn("RULE ERROR PROBLEM: " + crd.ruleError());
                    return false;
                }
                if (isUpdatedInThisWorkspace != workspace.wasModifiedHere(crd)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean crdValid(ConsistencyRuleType crd) {
        if(crd.hasRuleError()) {
            System.out.println("Rule Error: " + crd.ruleError());
            return false;
        }
        return true;
    }


    public static boolean creExists(ConsistencyRuleType crd, Instance contextInstance, boolean isConsistent, boolean hasError, boolean isUpdatedInThisWorkspace) {
        SetProperty<ConsistencyRule> cres = crd.consistencyRuleEvaluations();

        for (ConsistencyRule cre : cres) {
            if (cre.ruleDefinition().equals(crd) && cre.contextInstance().equals(contextInstance)) {
                if (cre.hasEvaluationError() != hasError) {
                    log.warn("RULE ERROR PROBLEM: " + cre.evaluationError());
                    return false;
                }
                if (cre.isConsistent() != isConsistent) {
                    return false;
                }
                if (isUpdatedInThisWorkspace != crd.workspace.wasModifiedHere(cre)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean repairGenerated(ConsistencyRuleType crd, Instance contextInstance, int childrenSize) {
        RepairNode rn = getRepairTree(crd, contextInstance);
        if (rn == null)
            return false;
        else if (rn.getChildren().size() == childrenSize)
            return true;
        else
            return false;

    }
    public static boolean containsRepair(ConsistencyRuleType crd, Instance contextInstance, Change c) {
        RepairNode rn = getRepairTree(crd, contextInstance);
        if (rn == null)
            return false;
        for (RepairAction ra:rn.getRepairActions()) {
            if(ra.equals(c))
                return true;
        }
        return false;


    }
    public static boolean repairGenerated(ConsistencyRuleType crd, Instance contextInstance, int childrenSize, int numberOfRepairs) {
        RepairNode rn = getRepairTree(crd, contextInstance);
        if (rn == null)
            return false;
        else if (rn.getChildren().size() == childrenSize && rn.getRepairActions().size() == numberOfRepairs)
            return true;
        else
            return false;

    }


    public static RepairNode highlightRepairsByOwnership(User owner, RepairNode repairTree){
        repairTree.highlightOwnedRepairs(String.valueOf(owner.id));
        return repairTree.flattenRepairTree();
    }


    public static RepairNode getRepairTree(ConsistencyRuleType crd, Instance contextInstance) {
        SetProperty<ConsistencyRule> cres = crd.consistencyRuleEvaluations();
        for (ConsistencyRule cre : cres) {
            if (cre.ruleDefinition().equals(crd) && cre.contextInstance().equals(contextInstance)) {
                return RuleService.repairTree(cre);
            }
        }
        return null;
    }

    public static Set<Repair> getFilteredConcreteRepairs(Set<Instance> ignoredInstances, Set<String> ignoredProperties, Set<Operator> ignoredOperators,
                                                         Set<Object> objects, int limit,ConsistencyRuleType crd, Instance contextInstance){
        Set<Repair> returnRepairs = new HashSet<>();
        Set<Repair> repairs = getConcreteRepairs(null, objects,limit,crd,contextInstance);
        for (Repair r : new HashSet<>(repairs)){
            boolean returnRepair = true;
            for (Object o : r.getRepairActions()){
                ConsistencyRepairAction action = (ConsistencyRepairAction) o;
                if(ignoredInstances != null)
                    if(action.getElement() != null && ignoredInstances.contains(action.getElement())) {
                        returnRepair = false;
                        break;
                    }
                if(ignoredProperties!= null && ignoredProperties.contains(action.getProperty())) {
                    returnRepair = false;
                    break;
                }
                if(ignoredOperators != null && ignoredOperators.contains(action.getOperator())) {
                    returnRepair = false;
                    break;
                }
            }
            if(returnRepair)
                returnRepairs.add(r);
        }
        return returnRepairs;
    }

    /**
     * Limits the number of concrete repairs generated. A limit of 0 (Zero) will not limit the generation
     * @param filter
     * @param objects to be used for generating the concrete values
     * @param limit
     * @param crd
     * @param contextInstance
     * @return
     */
    public static Set<Repair> getConcreteRepairs(RepairTreeFilter filter, Set<Object> objects, int limit, ConsistencyRuleType crd, Instance contextInstance) {
        ConsistencyRuleType childCRD = getChildWorkspaceConsistencyRule(crd);
        RuleService.currentWorkspace = childW;
        RuleService.evaluator.evaluateAll();
        ConsistencyRule inconsistency = childCRD.consistencyRuleEvaluation(contextInstance);
        RuleService.evaluator.evaluate(inconsistency);
        RepairNode repairTree = RuleService.repairTree(inconsistency);
        if(repairTree == null) {
            RuleService.currentWorkspace = crd.workspace;
            return null;
        }
        if(filter!=null)
            filter.filterRepairTree(repairTree);
        Set<Repair> concreteRepairs = repairTree.getConcreteRepairs(objects,limit);
        convertRepairsToWorkspace(crd.workspace,concreteRepairs);
        RuleService.currentWorkspace = crd.workspace;
        return concreteRepairs;
    }

    private static void convertRepairsToWorkspace(Workspace targetWorkspace, Set<Repair> repairs){
        for (Repair repair : repairs) {
            for (Object o : repair.getRepairActions()) {
                RepairAction action = (RepairAction) o;
                ConsistencyRule cre = (ConsistencyRule) action.getInconsistency();
                action.setInconsistency(targetWorkspace.findElement(cre.id()));
                Element e = (Element) action.getElement();
                if (e != null)
                    action.setElement(targetWorkspace.findElement(e.id()));
                Object value = action.getValue();
                Instance instanceValue;
                if (value instanceof Instance) {
                    instanceValue = (Instance) value;
                    action.setValue(targetWorkspace.findElement(instanceValue.id()));
                }
            }
        }
    }

    public static boolean executeRepair(Repair repair) {
        if (repair.isExecutable()) {
            try {
                repair.execute();
                return repair.executed();
            } catch (ChangeExecutionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }




    public static int creCount(ConsistencyRuleType crd) {
        int counter = 0;
        for (ConsistencyRule cre : crd.consistencyRuleEvaluations()) {
            if (cre.ruleDefinition().equals(crd)) {
                counter++;
            }
        }
        return counter;
    }
    public static int creCount(Workspace workspace) {
        Set<Instance> crds = workspace.debugInstances().stream().filter(instance -> instance instanceof ConsistencyRule).collect(Collectors.toSet());
        return crds.size();
    }

    public static int crdCount(Workspace workspace) {
        SetProperty<ConsistencyRuleType> crds = (SetProperty)workspace.its(ConsistencyRuleType.CONSISTENCY_RULE_TYPE).subTypes();
        return crds.size();
    }

    public static void printWorkspace(Workspace workspace) {
        log.trace("============================================ " + workspace + " ============================================");
        for (InstanceType instanceType : workspace.debugInstanceTypes()) {
            log.trace("------------------------------------------------- InstanceType: " + instanceType);
            for (Instance instance : instanceType.instances()) {
                log.trace("-     Instance: " + instance);
                for (PropertyType propertyType : instanceType.getPropertyTypes(true)) {
                    log.trace("    " + propertyType.name() + ": " + instance.getProperty(propertyType.name()));
                }
                log.trace("#################################################");
            }
            log.trace("-------------------------------------------------");
        }
    }

    static public void printSyntaxNode(Expression<?> node, int position) {
        String treeLevel = "-";
        for (int i = 0; i < position; i++) treeLevel.concat(" -");
        log.trace(treeLevel.concat(node.toString()));

        for (Expression<?> child : node.getChildren()) {
            printSyntaxNode(child, position + 1);
        }
    }

    static public void printSyntaxTree(Expression<?> node) {
        log.trace("================================= printing syntax tree ==================================");
        printSyntaxNode(node, 1);
    }

    static public void printEvaluationNode(EvaluationNode node, int position) {
        String treeLevel = "-";
        for (int i = 0; i < position; i++) treeLevel = treeLevel.concat(" -");
        log.trace(treeLevel.concat(node.toString()));

        for (EvaluationNode child : node.children) {
            printEvaluationNode(child, position + 1);
        }
    }

    static public void printEvaluationTree(EvaluationNode node) {
        log.trace("================================ printing evaluation tree =================================");
        if (log.isTraceEnabled())
        	printEvaluationNode(node, 1);
    }

    static public void printElementsInScope(Set<String> elementsInScope) {
        for (String elementInScope : elementsInScope) {
            log.trace("     " + elementInScope);
        }
    }

    static public void printRepair(AbstractRepairAction action, Object oldValue) {
        log.trace("==== Repair Made ====");
        log.trace("==== Repair Action: " + action);
        log.trace("==== Old value: " + oldValue);

    }

    static public void printUndo(Object value, Object newValue) {
        log.trace("==== Undo Made ====");
        log.trace("==== Undo value: " + value);
        log.trace("==== Restored value: " + newValue);

    }

    static public void printRepairTree(RepairNode node) {
        log.warn("================================ printing repair tree CU =================================");
        printRepairNode(node, 1);
        log.warn("================================ END =================================");
    }
    
    //Added Methods

    static public void printRepairTreeWithRestrictions(RepairNode node) {
        log.warn("================================ printing repair tree CU =================================");
        StringBuffer sb=new StringBuffer();
        printRepairNodeWithRestrictions(node, 1,sb);
        System.out.println(sb.toString());
        log.warn("================================ END =================================");
    }
    static public void printRepairNodeWithRestrictions(RepairNode node, int position,StringBuffer printInto) {
        String treeLevel = "";
        for (int i = 0; i < position; i++) 
        	treeLevel = treeLevel.concat(" -- ");
        if(node instanceof AbstractRepairAction)
        {
        AbstractRepairAction ra = (AbstractRepairAction)node;
		RestrictionNode rootNode =  ra.getValue()==UnknownRepairValue.UNKNOWN && ra.getRepairValueOption().getRestriction() != null ? ra.getRepairValueOption().getRestriction().getRootNode() : null;
		if (rootNode != null) {
			//rootNode.resetRestComplexity();
			String ret=rootNode.printNodeTree(false, 40);
			ret=ret.replaceAll("(?m)^[ \t]*\r?\n", "");
			printInto.append(treeLevel.concat(node.toString())+"\n"+ret+"\n");
		}
		else
		{
			printInto.append(treeLevel.concat(node.toString())+"\n");
		}
        }
		else
		{
			printInto.append(treeLevel.concat(node.toString())+"\n");
		}
        for (RepairNode child : node.getChildren()) {
        	printRepairNodeWithRestrictions(child, position + 1,printInto);
        }
    }
    
    static public void printRepairTreeWithRankAndScore(RepairNode node) {
        log.warn("================================ printing repair tree CU =================================");
        printRepairNodeWithRankAndScore(node, 1);
        log.warn("================================ END =================================");
    }
    
    static public void printRepairNodeWithRankAndScore(RepairNode node, int position) {
        String treeLevel = "";
        for (int i = 0; i < position; i++) 
        	treeLevel = treeLevel.concat(" -- ");
        log.warn(treeLevel.concat(node.toString())+"  ==>S= "+node.getScore()+" ==> R="+node.getRank());
        for (RepairNode child : node.getChildren()) {
        	printRepairNodeWithRankAndScore(child, position + 1);
        }
    }
    
    static public String getRepairTreeText(RepairNode node,int position, String tree)
    {
    	for (int i = 0; i < position; i++)
			tree = tree.concat(" -- ");
		if(node.getScore()==2147483646)
			tree=tree+(node.toString())+"  ==>S= -1"+" ==> R="+node.getRank()+"\n";
		else
			tree=tree+(node.toString())+"  ==>S= "+node.getScore()+" ==> R="+node.getRank()+"\n";
		for (RepairNode child : node.getChildren()) {
			tree=getRepairTreeText(child, position + 1,tree);
		}
    	return tree;
    }
    //End Method

    static public void printChangeGraph(RepairChangePropagator propagator) {
        log.trace("================================ printing change propagation graph =================================");
        log.trace("============== initial changes: "+ propagator.getInitialChange());
        SimpleDirectedGraph<ModelState, ChangeEdge> graph = (SimpleDirectedGraph<ModelState, ChangeEdge>) propagator.getModelGraph();
        for(ChangeEdge re : graph.edgeSet()){
            log.trace(graph.getEdgeSource(re) + "--" + re.getChange() +"--"+ graph.getEdgeTarget(re));
        }
    }

    static public void printRepairNode(RepairNode node, int position) {
        String treeLevel = "";
        for (int i = 0; i < position; i++) 
        	treeLevel = treeLevel.concat(" -- ");
        log.warn(treeLevel.concat(node.toString()));
        for (RepairNode child : node.getChildren()) {
            printRepairNode(child, position + 1);
        }
    }

    static public void printRepairDecisionTree(RepairPathNode node) {
        log.trace("================================ printing decision tree =================================");
        printRepairDecisionNode(node, 1);
    }

    static public void printRepairDecisionNode(RepairPathNode node, int position) {
        String treeLevel = "";
        for (int i = 0; i < position; i++) treeLevel = treeLevel.concat(" ... ");
        log.trace(treeLevel.concat(node.toStringCompact()));
        for (RepairPathNode child : node.getChildren()) {
            printRepairDecisionNode(child, position + 1);
        }
    }

    /**
     * Creates a child workspace and returns the crd in that workspace
     * @param crd original crd
     * @return child workspace
     */
    protected static ConsistencyRuleType getChildWorkspaceConsistencyRule(ConsistencyRuleType crd){
        if(childW == null)
            createChildWorkspace(crd.workspace);
        else
            childW.update();
        Object childInconsistency =  childW.findElement(crd.id());
        return (ConsistencyRuleType) childInconsistency;
    }

    private static void createChildWorkspace(Workspace workspace) {
        childW = WorkspaceService.createWorkspace("childWorkspace", workspace,
                WorkspaceService.ANY_USER, null, false, false);
        childW.update();
        childW.concludeTransaction();
    }


    public static void setChildW(Workspace childW) {
        ConsistencyUtils.childW = childW;
    }
    
    public static boolean isPropertyRepairable(InstanceType type, String property) {
    	// access property metadata, then see if property is listed there
    	MapProperty<String> propertyMetadata = type.getPropertyAsMap(at.jku.isse.designspace.core.model.ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
    	String strBool = propertyMetadata.get(ReservedNames.IS_PROPERTY_REPAIRABLE_PREFIX+property);
    	if (strBool == null) strBool = "true"; //default value
    	return Boolean.valueOf(strBool);
    }
    
    public static void setPropertyRepairable(InstanceType type, String property, boolean isRepairable) {
    	MapProperty<String> propertyMetadata = type.getPropertyAsMap(at.jku.isse.designspace.core.model.ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
    	propertyMetadata.put(ReservedNames.IS_PROPERTY_REPAIRABLE_PREFIX+property, Boolean.toString(isRepairable));
    }
}
