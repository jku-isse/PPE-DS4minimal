package at.jku.isse.designspace.rule.checker;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.collect.Sets;

import at.jku.isse.designspace.core.events.PropertyUpdate;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.evaluator.RuleDefinition;
import at.jku.isse.designspace.rule.arl.evaluator.RuleDefinitionImpl;
import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluation;
import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluationImpl;
import at.jku.isse.designspace.rule.arl.expressions.RootExpression;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.model.ReservedNames;
import at.jku.isse.designspace.rule.model.Rule;
import at.jku.isse.designspace.rule.model.RuleType;

public class ArlRuleEvaluator extends RuleEvaluator {

    protected HashMap<RuleType, RuleDefinition> ruleDefinitionByCRD = new HashMap<>();
    protected HashMap<Rule, RuleEvaluation> ruleEvaluationByCRE = new HashMap<>();

    public ArlRuleEvaluator() {
        new ArlModelAccess();
    }

    @Override
    public void ruleCreated(RuleType crd) {
        RuleDefinition ruleDefinition = ruleDefinitionByCRD.get(crd);
        if (ruleDefinition==null) {
            ruleDefinitionByCRD.put(crd, ruleDefinition = new RuleDefinitionImpl("", crd.rule(), ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, crd.contextInstanceType())));
            crd.setRuleError(ruleDefinition.getRuleError());
        }
        super.ruleCreated(crd);
    }

    @Override
    public void ruleUpdated(RuleType crd, PropertyUpdate op) {
        RuleDefinition ruleDefinition = ruleDefinitionByCRD.get(crd);
        if (ruleDefinition==null) { // may happen upon restarting with persistence enabled
        	ruleDefinitionByCRD.put(crd, ruleDefinition = new RuleDefinitionImpl("", crd.rule(), ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, crd.contextInstanceType())));
            crd.setRuleError(ruleDefinition.getRuleError());
            super.ruleCreated(crd);
        }        
        ruleDefinition.setRule(crd.rule());
        ruleDefinition.setContextType(ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, crd.contextInstanceType()));
        super.ruleUpdated(crd, op);
    }

    @Override public void ruleDeleted(RuleType crd) {
        super.ruleDeleted(crd);
        RuleDefinition ruleDefinition = ruleDefinitionByCRD.get(crd);
        if (ruleDefinition==null) return;
        ruleDefinitionByCRD.remove(crd);
    }

    @Override public Rule evaluationCreated(RuleType crd, Instance instance) {
        RuleDefinition ruleDefinition = ruleDefinitionByCRD.get(crd);
        //a commit to a parent workspace my have a crd even though its rule definition was not created
        if (ruleDefinition==null) {
            ruleDefinitionByCRD.put(crd, ruleDefinition = new RuleDefinitionImpl("", crd.rule(), ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, crd.contextInstanceType())));
            crd.setRuleError(ruleDefinition.getRuleError());
        }

        Rule cre = super.evaluationCreated(crd, instance);

        if ((RuleEvaluationImpl)ruleEvaluationByCRE.get(cre)==null) {
            ruleEvaluationByCRE.put(cre, new RuleEvaluationImpl(ruleDefinition, instance));
        }
        return cre;
    }

    @Override public void evaluationDeleted(Rule cre) {
        ruleEvaluationByCRE.remove(cre);
        super.evaluationDeleted(cre);
    }

    public EvaluationNode evaluationTree(Rule cre) {
        RuleEvaluation ruleEvaluation = ruleEvaluationByCRE.get(cre);
        return ruleEvaluation.getEvaluationTree();
    }


    public RepairNode repairTree(Rule cre) {
        RuleEvaluation evaluation = ruleEvaluationByCRE.get(cre);
        // after persistance reload, hashmaps are empty, refilled via call to evaluate
        if (evaluation==null) {        	
        	evaluate(cre); //TODO: do we need to call conclude transaction here, when this is called after persistance reloading
        	evaluation = ruleEvaluationByCRE.get(cre);
        }
        if(evaluation.getEvaluationTree()==null) { 
            return null;
        }
        RootExpression rootExpression = (RootExpression) evaluation.getEvaluationTree().expression.getRootExpression();
        rootExpression.contextElement = evaluation.getContextElement();
        rootExpression.evaluation = evaluation;
        return evaluation.getRepairTree(cre);
    }

    public Entry<RuleEvaluation, Boolean> evaluate(Rule cre) {
        RuleEvaluation ruleEvaluation = ruleEvaluationByCRE.get(cre);
        if (ruleEvaluation==null) {
            //needed when crd/cre is reconstructed in another workspace - we are not reusing ARL across workspace as crds could change
            RuleDefinition ruleDefinition=new RuleDefinitionImpl("", cre.ruleDefinition().rule(), ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, cre.ruleDefinition().contextInstanceType()));
            ruleDefinitionByCRD.put(cre.ruleDefinition(), ruleDefinition);
            ruleEvaluationByCRE.put(cre, ruleEvaluation=new RuleEvaluationImpl(ruleDefinition, cre.contextInstance()));
        }

        ruleEvaluation.evaluate();
        Object priorResult = cre.getPropertyAsSingle(ReservedNames.IS_CONSISTENT).get();
        cre.setResult(ruleEvaluation.getResult());
        cre.setEvaluationError(ruleEvaluation.getError());
        Object currentResult = cre.getPropertyAsSingle(ReservedNames.IS_CONSISTENT).get();
        
        if (ruleEvaluation.getAddedScopeElements()!=null) {
            for (Property property : (Sets.SetView<Property>) ruleEvaluation.getAddedScopeElements()) {
                cre.addPropertyToScope(property);
            }
        }
        if (ruleEvaluation.getRemovedScopeElements()!=null) {
            for (Property property : (Sets.SetView<Property>) ruleEvaluation.getRemovedScopeElements()) {
                cre.removePropertyFromScope(property);
            }
        }
        return new AbstractMap.SimpleEntry<RuleEvaluation, Boolean>(ruleEvaluation, !Objects.equals(priorResult,currentResult)); // returns if the outcome has changed
    }
}

