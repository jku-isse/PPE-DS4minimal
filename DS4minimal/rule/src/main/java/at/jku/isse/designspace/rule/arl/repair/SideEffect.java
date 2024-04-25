package at.jku.isse.designspace.rule.arl.repair;

import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluation;
import at.jku.isse.designspace.rule.model.ConsistencyRule;

public class SideEffect<E> {



    private E inconsistency;
    private SideEffect.Type type;

    public SideEffect(E inconsistency, SideEffect.Type type) {
        this.inconsistency = inconsistency;
        this.type = type == null ? SideEffect.Type.UNKNOWN : type;
    }



    public SideEffect(E inconsistency, int type) {
        this.inconsistency = inconsistency;
        switch (type) {
            case 0:
                this.type = Type.NONE;
                break;
            case 1:
                this.type = Type.NEGATIVE;
                break;
            case 2:
                this.type = Type.POSITIVE;
                break;
            case 4:
                this.type = Type.UNKNOWN;
                break;
            case 8:
                this.type = Type.ERROR;
                break;
            case 16:
                this.type = Type.DELETED;
                break;
        }
    }


    public E getInconsistency() {
        return this.inconsistency;
    }


    public SideEffect.Type getSideEffectType() {
        return this.type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16);
        sb.append(this.type.toString());
        sb.append(" - ");
        sb.append(this.inconsistency.toString());
        return sb.toString();
    }
    /**
     * The {@link Type} specifies <em>how</em> the {@link Repair} affects the
     * {@link ConsistencyRule}.
     */
    public enum Type {
        /**
         * Applying the {@link Repair} deletes the {@link RuleEvaluation}
         */
        DELETED(16, "deleted"),
        /**
         * Applying the {@link Repair} causes the {@link RuleEvaluation}'s validation
         * to perform a run-time error (e.g. access of <code>null</code>
         * pointers).
         */
        ERROR(8, "error"),
        /**
         * The effect of applying the {@link Repair} is unknown. Mainly used for
         * non-executable {@link Repair}s, in which case their effect can not be
         * tested.
         */
        UNKNOWN(4, "unknown"),
        /**
         * Applying the {@link Repair} causes the {@link RuleEvaluation} to become
         * consistent
         */
        POSITIVE(2, "positive"),
        /**
         * Applying the {@link Repair} causes the {@link RuleEvaluation} to become
         * inconsistent
         */
        NEGATIVE(1, "negative"),
        /**
         * Applying the {@link Repair} has no effect the {@link RuleEvaluation}
         */
        NONE(0, "none");

        private final int n;
        private final String name;

        Type(int n, String name) {
            this.n = n;
            this.name = name;
        }

        /**
         * Returns a integer representation of the side effect type.
         *
         * @return the integer number
         */
        public final int getSideEffect() {
            return this.n;
        }

        /**
         * Returns the textual representation of the side effect type.
         *
         * @return the textual representation
         */
        public final String getName() {
            return this.name;
        }
    }
}
