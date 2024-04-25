package at.jku.isse.designspace.rule.checker;

import java.util.HashSet;

import at.jku.isse.designspace.core.model.Evaluator;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.rule.arl.expressions.Expression;
import at.jku.isse.designspace.rule.arl.expressions.RootExpression;
import at.jku.isse.designspace.rule.arl.parser.ArlParser;
import at.jku.isse.designspace.rule.arl.parser.ArlType;

public class ArlEvaluator extends Evaluator {

    public ArlParser parser = new ArlParser();
    public RootExpression<Object> syntaxTree=null;
    public HashSet scopeElements = new HashSet();
    private Object contextElement = null;

    public ArlEvaluator(InstanceType instanceType, String rule) {
        super(instanceType, rule);
        try {
            syntaxTree = new RootExpression((Expression) parser.parse(rule, ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, instanceType), null));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(String.format("Parsing error in \"%s\": %s (Line=%d, Column=%d)", rule, ex.getMessage(), parser.getLine(), parser.getColumn()));
        }

    }

    public Object evaluate(Object self) {
        contextElement=self;
        scopeElements = new HashSet();
        return syntaxTree.evaluate(contextElement, scopeElements).resultValue;
    }
}

