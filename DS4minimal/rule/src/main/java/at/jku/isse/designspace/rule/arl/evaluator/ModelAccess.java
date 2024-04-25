package at.jku.isse.designspace.rule.arl.evaluator;

import java.util.List;
import java.util.Set;

import at.jku.isse.designspace.rule.arl.parser.ArlType;

public abstract class ModelAccess<ET,E> {

    static public ModelAccess instance;

    //**************************************************************************************************
    //*** Instance and Type Support
    //**************************************************************************************************

    abstract public Set<E> instancesOfArlType(ArlType type);

    abstract public Object scopeElement(E instance, String property);
    abstract public Object propertyValueOfInstance(E instance, String property);

    abstract public ArlType arlTypeByQualifiedName(List<String> name);

    abstract public boolean arlIsKindOf(ArlType typeA, ArlType typeB);

    abstract public ArlType arlSuperTypeOfType(ArlType type);
    abstract public ArlType arlTypeOfInstance(E element);
    abstract public ArlType arlTypeOfProperty(ArlType type, String property);

}
