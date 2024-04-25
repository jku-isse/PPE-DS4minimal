package at.jku.isse.designspace.rule.arl.repair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import at.jku.isse.designspace.rule.arl.expressions.Expression;

public class RepairSingleValueOption {
    protected Object value;
    protected boolean expectedEvaluationResult;
    public Operator operator;
    protected RepairRestriction restriction = null;
    
    public RepairSingleValueOption(Operator operator, Object value, boolean expectedEvaluationResult) {
        this.operator = operator;
        this.value = value;
        this.expectedEvaluationResult = expectedEvaluationResult;
    }
    public RepairSingleValueOption(Operator operator, Object value) {
        this.operator = operator;
        this.value = value;
        this.expectedEvaluationResult = true;
    }



    /**

     * <code>boolean</code> value <code>true</code>.
     */
    public static final RepairSingleValueOption TRUE = new RepairSingleValueOption(Operator.MOD_EQ,
            true) {
        @Override
        public Collection<? extends RepairSingleValueOption> invert() {
            return Collections.singleton(FALSE);
        }
    };
    /**
     * States that the  value has to be changed to the
     * <code>boolean</code> value <code>false</code>.
     */
    public static final RepairSingleValueOption FALSE = new RepairSingleValueOption(Operator.MOD_EQ,
            false) {
        @Override
        public Collection<? extends RepairSingleValueOption> invert() {
            return Collections.singleton(TRUE);
        }
    };



    /**
     * Calculates the inverse of <code>this</code> {@link Operator}. This may be
     * a {@link Collection} containing a single {@link Operator}, or multiple
     * ones if necessary. It is generally used by {@link Expression}s inverting
     * the expected result (e.g. <code>not</code>).<br/>
     * See below for examples:
     * <ol>
     * <li>invert({@link #TRUE}) -> {{@link #FALSE}</li>
     * <li>invert((+, x)) -> {(-, x)}</li>
     * <li>invert((=, x)) -> {(<>, x)}</li>
     * <li>invert((>, x)) -> {(<, x), (=, x)}</li>
     * </ol>
     *
     * @return the inverse of this {@link Operator} or <code>this</code> if no
     *         meaningful inverse can be calculated.
     */
    public Collection<? extends RepairSingleValueOption> invert() {
        switch (operator) {
            case ADD:
                return Collections.singleton(new RepairSingleValueOption(Operator.REMOVE, value));
            case REMOVE:
                return Collections.singleton(new RepairSingleValueOption(Operator.ADD, value));
            case MOD_EQ:
                return Collections.singleton(new RepairSingleValueOption(Operator.MOD_NEQ, value));
            case MOD_NEQ:
                return Collections.singleton(new RepairSingleValueOption(Operator.MOD_EQ, value));
            case MOD_GT:
                Collection<RepairSingleValueOption> inverts = new ArrayList<>(2);
                inverts.add(new RepairSingleValueOption(Operator.MOD_LT, value));
                inverts.add(new RepairSingleValueOption(Operator.MOD_EQ, value));
                return inverts;
            case MOD_LT:
                inverts = new ArrayList<>(2);
                inverts.add(new RepairSingleValueOption(Operator.MOD_GT, value));
                inverts.add(new RepairSingleValueOption(Operator.MOD_EQ, value));
                return inverts;
            default:
                return Collections.singleton(this);
        }
    }

    public Object getValue() {
        return this.value;
    }

    public Boolean getExpectedEvaluationResult() {
        return this.expectedEvaluationResult;
    }

    public RepairSingleValueOption duplicate() {
    	return new RepairSingleValueOption(operator, value, expectedEvaluationResult);
    }
    
    public RepairSingleValueOption setRestriction(RepairRestriction restriction) {
    	this.restriction = restriction;
    	return this;
    }
    
    public RepairRestriction getRestriction() {
    	return restriction;
    }
    
    public RestrictionNode getRootRestriction() {
    	if (restriction != null) {
    		return restriction.getRootNode();
    	} 
    	return null;
    }
    
    @Override
    public String toString() {
        return operator.toString() + ", " +
                (this.value == null ? "null": this.value.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RepairSingleValueOption other = (RepairSingleValueOption) obj;
        if(!this.operator.equals(other.operator))
            return false;
        if (this.getValue() == null && other.getValue() != null)
        	return false;
        if (this.getValue() != null && other.getValue() == null)
        	return false;        
        if(this.getValue() != null && !this.getValue().equals(other.getValue()))
            return false;
        if (this.getRestriction() != null) {
        	if (other.getRestriction() == null)
        		return false;
        	if (!this.getRestriction().equals(other.getRestriction()))
        		return false;
        }
        return true;
    }
}
