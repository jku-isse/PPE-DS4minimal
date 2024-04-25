package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.parser.ArlType;

public abstract class TemporalExpression extends Expression<Boolean> {

    //    protected List<Expression<Boolean>> rules;
    protected Expression<Boolean> rule;
    protected static Map<EvaluationNodeHistoryKey, EvaluationNodeHistory> evaluationNodes = new HashMap<>();

    private static Map<Expression, Long> durations = new HashMap<>();

    public TemporalExpression(Expression<Boolean> rule) {
        super();
        this.rule = rule;
        this.rule.setParent(this);
        this.resultType = ArlType.BOOLEAN;
    }

    @Override
    public EvaluationNode evaluate(HashSet scopeElements) {
        long start = System.nanoTime();
        Expression parent = this.parent;
        while (!(parent == null || parent instanceof RootExpression))
            parent = parent.getParent();
        if (parent == null) return new EvaluationNode(this, false);

        Element contextInstance = (Element) ((RootExpression<?>) parent).contextElement;

        EvaluationNodeHistoryKey key = new EvaluationNodeHistoryKey(contextInstance, this, getIteratorScopeChain(this));

        EvaluationNodeHistory historyNode;
        if (!evaluationNodes.containsKey(key)) {
            historyNode = new EvaluationNodeHistory(null);
            evaluationNodes.put(key, historyNode);
        } else {
            historyNode = evaluationNodes.get(key);
        }

        EvaluationNode result = evaluate(scopeElements, rule, key, historyNode);
//        else
//            result = evaluateWithTrigger(scopeElements, rules.get(0), rules.get(1), key, historyNode);
        long end = System.nanoTime();
        durations.put(this, end - start);
        return result;
    }

    /**
     * We iterate upwards towards the root node to check if there are any iterators that define subscopes of evaluation,
     * we differentiate between multiple evaluation subscopes by chaining the iterator values towards the root.
     */
    protected static List<Object> getIteratorScopeChain(Expression exp) {
        List<Object> scopeChain = new LinkedList<>();
        if (exp == null) return scopeChain;
        Expression p = exp.parent;
        while (!(p == null || p instanceof RootExpression)) {
            p = p.getParent();
            if (p instanceof IteratorExpression) {
                IteratorExpression expIt = (IteratorExpression) p;
                scopeChain.add(expIt.iterator1Value);
                if (expIt.iterator2Value != null) {
                    scopeChain.add(expIt.iterator2Value);
                }
            }
        }
        return scopeChain;
    }

    protected static Stream<TemporalExpression> getAllChildTemporalExpressionsRecursively(Expression exp) {
        if (exp == null) return Stream.empty();
        if (exp.getChildren() == null || exp.getChildren().isEmpty()) return Stream.empty();
        else {
            return exp.getChildren().stream().flatMap(child -> {
                if (child instanceof TemporalExpression) {
                    return Stream.concat(Stream.of((TemporalExpression) child), getAllChildTemporalExpressionsRecursively((Expression) child));
                } else return getAllChildTemporalExpressionsRecursively((Expression) child);
            });
        }
    }

    @Override
    public Boolean evaluate(Expression<?> child) {
        return null;
    }

    protected abstract EvaluationNode evaluate(HashSet scopeElements, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode);
//
//    protected abstract EvaluationNode evaluateWithTrigger(HashSet scopeElement, Expression trigger, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode);

//    protected void reset(EvaluationNodeHistoryKey key) {
//        List<EvaluationNodeHistory> subNodes = getSubNodes(key);
//        for (EvaluationNodeHistory node : subNodes)
//            node.reset();
//    }

    protected void resetAllTemporalChildExressionsRecursively(EvaluationNodeHistoryKey parentKey) {
        getAllChildTemporalExpressionsRecursively(parentKey.expression)
                .map(tempExpr -> new EvaluationNodeHistoryKey(parentKey.instance, tempExpr, getIteratorScopeChain(tempExpr)))
                .map(childKey -> TemporalExpression.evaluationNodes.get(childKey))
                .filter(Objects::nonNull)
                .forEach(childHistoryNode -> childHistoryNode.reset());
    }

    protected Boolean canTerminate(EvaluationNodeHistoryKey key) {
        return getAllChildTemporalExpressionsRecursively(key.expression)
                .map(tempExpr -> new EvaluationNodeHistoryKey(key.instance, tempExpr, getIteratorScopeChain(tempExpr)))
                .map(childKey -> TemporalExpression.evaluationNodes.get(childKey))
                .filter(Objects::nonNull)
                .allMatch(childNodeHistory -> childNodeHistory.isTerminated());

//    	List<EvaluationNodeHistory> subNodes = getSubNodes(key);
//        EvaluationNodeHistory rootNode = evaluationNodes.get(key);
//        for (EvaluationNodeHistory node : subNodes)
//            if (!node.isTerminated() && !node.equals(rootNode))
//                return false;
//
//        return true;
    }
}

class EvaluationNodeHistoryKey {
    Element instance;
    Expression expression;
    List<Object> scopeChain;

    public EvaluationNodeHistoryKey(Element instance, Expression expression, List<Object> scopeChain) {
        this.instance = instance;
        this.expression = expression;
        this.scopeChain = scopeChain;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression.getARL(), instance.id(), scopeChain);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EvaluationNodeHistoryKey other = (EvaluationNodeHistoryKey) obj;
        return Objects.equals(expression, other.expression) && Objects.equals(instance, other.instance)
                && Objects.equals(scopeChain, other.scopeChain);
    }


//    @Override
//    public boolean equals(Object obj) {
//        if (!(obj instanceof EvaluationNodeHistoryKey))
//            return false;
//        return instance.equals(((EvaluationNodeHistoryKey) obj).instance) && expression.equals(((EvaluationNodeHistoryKey) obj).expression);
//    }
//
//    @Override
//    public int hashCode() {
//        return instance.id().hashCode() + expression.getARL().hashCode();
//    }
}

class EvaluationNodeHistory {
    private boolean triggered;
    private boolean terminated;
    private EvaluationNode lastEvaluation;

    public EvaluationNodeHistory(EvaluationNode lastEvaluation) {
        this.lastEvaluation = lastEvaluation;
        this.triggered = false;
        this.terminated = false;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered() {
        this.triggered = true;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void setTerminated() {
        terminated = true;
    }

    public void reset() {
        this.triggered = false;
        this.terminated = false;
        this.lastEvaluation = null;
    }

    public EvaluationNode getLastEvaluation() {
        return lastEvaluation;
    }

    public void setLastEvaluation(EvaluationNode lastEvaluation) {
        this.lastEvaluation = lastEvaluation;
    }
}