package at.jku.isse.designspace.core.model;

/**
 * The cardinality of a property describes how values are stored. Usually
 * properties are mapped to a single value, but a different cardinality may
 * allow the storage of value collections as well.
 */
public enum Cardinality {

    /**
     * The property holds a single value. Setting a new value will overwrite the previous value.
     */
    SINGLE,

    /**
     * The property holds a set of values. Adding the same value again will not change the collection, i.e. duplicates are not allowed.
     */
    SET,

    /**
     * The property holds an ordered list of values. Duplicates are allowed.
     */
    LIST,

    /**
     * The property holds key-value pairs. The key must be a string
     */
    MAP,
}
