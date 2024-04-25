package at.jku.isse.designspace.core.model;

public abstract class Evaluator {
    public String rule = null;
    public InstanceType instanceType = null;

    public Evaluator(InstanceType instanceType, String rule) {
        this.instanceType=instanceType;
        this.rule=rule;
    }

    public abstract Object evaluate(Object self);
}