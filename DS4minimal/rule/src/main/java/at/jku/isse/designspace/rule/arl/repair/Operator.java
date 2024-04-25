package at.jku.isse.designspace.rule.arl.repair;

import  java.util.Collection;
public enum Operator {
    /**
     * an element has to be removed from a {@link Collection}
     */
    ADD("Add"),
    /**
     * an element has to be added to a {@link Collection}
     */
    REMOVE("Remove"),
    /**
     * values must be equal
     */
    MOD_EQ("="),
    /**
     * values must NOT be equal
     */
    MOD_NEQ("<>"),
    /**
     * left value must be less than right value
     */
    MOD_LT("<"),
    /**
     * left value must be greater than right value
     */
    MOD_GT(">");

    private final String operatorString;

    Operator(String operatorString) {
        this.operatorString = operatorString;
    }

    @Override
    public String toString() {
        return operatorString;
    }

}