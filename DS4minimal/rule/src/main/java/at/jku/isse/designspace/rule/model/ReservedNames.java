package at.jku.isse.designspace.rule.model;


public class ReservedNames {

    public final static String META_RULE_TYPE_NAME = "RuleType";
    public final static String RULE_TYPE_NAME = "Rule";
    public final static String META_CONSISTENCY_RULE_TYPE_NAME = "ConsistencyRuleType";
    public final static String CONSISTENCY_RULE_TYPE_NAME = "ConsistencyRule";
    public final static String META_DERIVED_PROPERTY_RULE_TYPE_NAME = "DerivedPropertyRuleType";
    public final static String DERIVED_PROPERTY_RULE_TYPE_NAME = "DerivedPropertyRule";
    public final static String DERIVED_PROPERTY_RULE_SINGLE_TYPE_NAME = "DerivedPropertySingleRule";
    public final static String DERIVED_PROPERTY_RULE_SET_TYPE_NAME = "DerivedPropertySetRule";
    public final static String DERIVED_PROPERTY_RULE_LIST_TYPE_NAME = "DerivedPropertyListRule";

    /**
     * The actual rule of a Consistency Rule Definition (CRD). The rule is a string to allow for different languages
     */
    public final static String RULE = "rule";
    public final static String RULE_XML = "ruleXML";

    /**
     * The outcome of parsing the rule, either {@code true} or {@code false}.
     */
    public final static String RULE_ERROR = "ruleError";

    public final static String RULE_EVALUATIONS_BY_CONTEXT_INSTANCE = "ruleEvaluationsByContextInstance";

    /**
     * CONTEXT_INSTANCE_TYPE of a consistency rule type is a reference to the type to which a rule applies.
     * CONSISTENCY_RULES refers to the set of consistency rules that have it as its CONTEXT_INSTANCE_TYPE (opposable)
     */
    public final static String CONTEXT_INSTANCE_TYPE = "contextInstanceType";
    public final static String RULE_TYPES = "@rl_ruleTypes";

    /**
     * The outcome of a Consistency Rule Evaluation (CRE), either {@code true} or {@code false}.
     */
    public final static String RESULT = "result";
    public final static String IS_CONSISTENT = "isConsistent";
    public final static String EVALUATION_ERROR = "evaluationError";

    /**
     * The concrete artifact (instance) for which a Consistency Rule Definition (CRD) has been evaluated.
     */
    public final static String CONTEXT_INSTANCE = "contextInstance";
    public final static String RULE_EVALUATIONS_IN_CONTEXT = "@rl_ruleContexts";

    /**
     * A CRE references all properties that are in its scope. Each property references back to its CRE
     */
    public final static String PROPERTIES_IN_SCOPE = "propertiesInScope";


    public final static String OWNERSHIP_PROPERTY = "modifiedBy";
    public final static String RULE_EVALUATIONS_IN_SCOPE = "@rl_ruleScopes";
    public final static String DERIVED_PROPERTY_NAME = "derivedPropertyName";


    /**
     * Used in the property meta data to specify if a property can be repaired, defaults to true
     */
    public final static String IS_PROPERTY_REPAIRABLE_PREFIX = "@isRepairable/";
    
    /**
     * Class Names
     */
    public static final String RULE_TYPE_CLASS_NAME = "rule.model.RuleType";
    public static final String RULE_CLASS_NAME = "rule.model.Rule";
    public static final String CONSISTENCY_RULE_TYPE_CLASS_NAME = "rule.model.ConsistencyRuleType";
    public static final String CONSISTENCY_RULE_CLASS_NAME = "rule.model.ConsistencyRule";
    public static final String DERIVED_PROPERTY_RULE_TYPE_CLASS_NAME = "rule.model.DerivedPropertyRuleType";
    public static final String DERIVED_PROPERTY_RULE_CLASS_NAME = "rule.model.DerivedPropertyRule";
    public static final String DERIVED_PROPERTY_LIST_RULE_TYPE_CLASS_NAME = "rule.model.DerivedPropertyRuleListType";
    public static final String DERIVED_PROPERTY_LIST_RULE_CLASS_NAME = "rule.model.DerivedPropertyListRule";
    public static final String DERIVED_PROPERTY_SET_RULE_TYPE_CLASS_NAME = "rule.model.DerivedPropertyRuleSetType";
    public static final String DERIVED_PROPERTY_SET_RULE_CLASS_NAME = "rule.model.DerivedPropertySetRule";
    public static final String DERIVED_PROPERTY_SINGLE_RULE_TYPE_CLASS_NAME = "rule.model.DerivedPropertyRuleSingleType";
    public static final String DERIVED_PROPERTY_SINGLE_RULE_CLASS_NAME = "rule.model.DerivedPropertySingleRule";
}
