package at.jku.isse.designspace.rule.service;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.events.ElementDelete;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.events.PropertyUpdate;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Factory;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.checker.ArlEvaluator;
import at.jku.isse.designspace.rule.checker.ArlRuleEvaluator;
import at.jku.isse.designspace.rule.checker.RuleEvaluator;
import at.jku.isse.designspace.rule.model.ConsistencyRule;
import at.jku.isse.designspace.rule.model.ConsistencyRuleType;
import at.jku.isse.designspace.rule.model.DerivedPropertyRuleListType;
import at.jku.isse.designspace.rule.model.DerivedPropertyRuleSetType;
import at.jku.isse.designspace.rule.model.DerivedPropertyRuleSingleType;
import at.jku.isse.designspace.rule.model.DerivedPropertyRuleType;
import at.jku.isse.designspace.rule.model.Rule;
import at.jku.isse.designspace.rule.model.RuleType;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RuleService implements ServiceProvider {
    
    static public Workspace currentWorkspace;
    static public RuleEvaluator evaluator = null;
    static public boolean isEnabled = true;

    @Autowired
    public RuleService() {
        ServiceRegistry.registerService(this);
    }

    public String getName(){
        return "Rule";
    }
    public String getVersion(){
        return "1.0.0";
    }
    public int getPriority(){
        return 1;
    }
    public boolean isPersistenceAware(){
        return false;
    }

    public void initialize() {
        log.debug("starting...;");

        //Define rule-relevant types
        RuleType.buildType();
        ConsistencyRuleType.buildType();
        DerivedPropertyRuleType.buildType();
        DerivedPropertyRuleSingleType.buildType();
        DerivedPropertyRuleSetType.buildType();
        DerivedPropertyRuleListType.buildType();

        if (evaluator == null)
        	setEvaluator(new ArlRuleEvaluator());
        Factory.setEvaluator(ArlEvaluator.class);

        //Listen for events that may trigger a re-evaluation of rules
        if (isEnabled) Workspace.serviceProviders.add(this);

        WorkspaceService.PUBLIC_WORKSPACE.concludeTransaction();
    }


    /**
     * change the default ARL evaluator against another evaluator
     */
    static public void setEvaluator(RuleEvaluator evaluator) {
        log.debug("setEvaluator;" + evaluator);
        RuleService.evaluator = evaluator;
    }


    public static RepairNode repairTree(ConsistencyRule cre) {
        return evaluator.repairTree(cre);
    }

    static public EvaluationNode evaluationTree(ConsistencyRule cre) {
        return evaluator.evaluationTree(cre);
    }


    @Override
    public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
        try {
            if (evaluator == null) return;
            if (operations.size() == 0) return;
            currentWorkspace=workspace;

            log.debug("{};handleServiceRequest;{} operations", currentWorkspace, operations.size());

            Set<Element> ignorePropertyValueUpdatedOfElement = new HashSet<>();
            for (Operation operation : operations) {
                //TODO: inefficient that elements are reconstructed for all operations
                Element element = WorkspaceService.getWorkspaceElement(currentWorkspace, operation.elementId());
                if (element.isDeleted()) ignorePropertyValueUpdatedOfElement.add(element);  //often there are changes immediately preceeding a delete (as part of a delete). these should be ignored also
                if (element instanceof InstanceType) {
                    InstanceType instanceType = (InstanceType)element;
                    if (operation instanceof ElementCreate) {
                        if (instanceType instanceof RuleType) {
                            log.debug("{};ruleCreated;{}", currentWorkspace, instanceType);
                            evaluator.ruleCreated((RuleType) instanceType);
                            ignorePropertyValueUpdatedOfElement.add(instanceType);
                        }
                    } else if (operation instanceof PropertyUpdate) {
                        if (!ignorePropertyValueUpdatedOfElement.contains(instanceType)) {
                            if (instanceType instanceof RuleType) {
                                log.debug("{};ruleUpdated;{};property={} changed to {}", currentWorkspace, instanceType, ((PropertyUpdate) operation).name(), ((PropertyUpdate) operation).value());
                                evaluator.ruleUpdated((RuleType) instanceType, ((PropertyUpdate) operation));
                            }
                        }
                    } else if (operation instanceof ElementDelete) {
                        if (instanceType instanceof RuleType) {
                            log.debug("{};ruleDeleted;{}", currentWorkspace, instanceType);
                            evaluator.ruleDeleted((RuleType) instanceType);
                            ignorePropertyValueUpdatedOfElement.add(instanceType);
                        }
                    }
                }
                else if (element instanceof Instance) {
                    Instance instance = (Instance)element;
                    if (operation instanceof ElementCreate) {
                        if (!(instance instanceof Rule)) {
                            log.debug("{};instanceCreated;{}", currentWorkspace, instance);
                            evaluator.instanceCreated(instance);
                            ignorePropertyValueUpdatedOfElement.add(instance);
                        }
                    } else if (operation instanceof PropertyUpdate) {
                        if (!ignorePropertyValueUpdatedOfElement.contains(instance)) {
                            if (!(instance instanceof Rule)) {
                                log.debug("{};instanceUpdated;{};property={} changed to {}", currentWorkspace, instance, ((PropertyUpdate) operation).name(), ((PropertyUpdate) operation).value());
                                evaluator.instanceUpdated(instance, ((PropertyUpdate) operation));
                            }
                        }
                    } else if (operation instanceof ElementDelete) {
                        if (!(instance instanceof Rule)) {
                            log.debug("{};instanceDeleted;{}", currentWorkspace, instance);
                            evaluator.instanceDeleted(instance);
                            ignorePropertyValueUpdatedOfElement.add(instance);
                        }
                    }
                }
            }
            evaluator.evaluateAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}








