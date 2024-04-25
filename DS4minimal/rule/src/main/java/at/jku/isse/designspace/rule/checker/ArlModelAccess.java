package at.jku.isse.designspace.rule.checker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.rule.arl.evaluator.ModelAccess;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.service.RuleService;

public class ArlModelAccess extends ModelAccess<Element, Instance> {

    public ArlModelAccess() {
        this.instance = this;
    }

    public Set<Instance> instancesOfArlType(ArlType type) {
        if (type.nativeType==null) return new HashSet<>();
        return ((InstanceType)type.nativeType).instances().get();
    }

    public Object scopeElement(Instance instance, String propertyName) {
        if (instance == null) return null;
    	return instance.getProperty(propertyName);
    }

    public ArlType arlTypeByQualifiedName(List<String> name) {
        if (name.size() <= 2)
        	return null;
        name.remove(0);
        name.remove(0);
        InstanceType type = RuleService.currentWorkspace.instanceTypeWithQualifiedName(name.toArray(new String[0]));
        return ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, type);
    }

    @Override
    public boolean arlIsKindOf(ArlType typeA, ArlType typeB) {
        InstanceType instanceTypeA = (InstanceType) typeA.nativeType;
        InstanceType instanceTypeB = (InstanceType) typeB.nativeType;
        return instanceTypeA.isKindOf(instanceTypeB);
    }

    public ArlType arlSuperTypeOfType(ArlType type) {
        if (type.nativeType==null) throw new EvaluationException("supertype needs a native type");
        var superTypes = ((InstanceType)type.nativeType).superTypes();

        if (superTypes==null || superTypes.isEmpty())
            return null;
        else {
            // TODO due to MultiInheritance, this is now a Set, here we assert that the size is 1, maybe this will cause issues?
            if(superTypes.size()>1) {
                throw new IllegalStateException("MultiInheritance: ArlType must have exactly one supertype but has " + superTypes.size());
            }
            return ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, superTypes.iterator().next());
        }
    }

    //needed to obtain the arl type of an instance
    public ArlType arlTypeOfInstance(Instance element) {
        if (element instanceof Instance)
            return ArlType.get(ArlType.TypeKind.INSTANCE, ArlType.CollectionKind.SINGLE, ((Instance)element).getInstanceType());
        else
            return null;
    }

    public ArlType arlTypeOfProperty(ArlType type, String property) {
        if (type.nativeType==null) throw new EvaluationException("supertype needs a native type");
        PropertyType propertyType = ((InstanceType)type.nativeType).getPropertyType(property);
        if (propertyType==null)
            throw new ParsingException("type '%s' does not have a property '%s'", type, property);

        return ArlType.get(typeKind(propertyType.referencedInstanceType()), collectionKind(propertyType.cardinality()), propertyType.referencedInstanceType());
    }

    public Object propertyValueOfInstance(Instance element, String propertyName) {
        if (element == null) return null;
        if (element.getProperty(propertyName) == null) return null;//FIXME: hack to avoid NPE on unexpected instances without an expected property
    	return element.getProperty(propertyName).get();
    }

    public ArlType.TypeKind typeKind(InstanceType type) {
        if (type.id()== WorkspaceService.PUBLIC_WORKSPACE.INTEGER.id()) return ArlType.TypeKind.INTEGER;
        if (type.id()== WorkspaceService.PUBLIC_WORKSPACE.REAL.id()) return ArlType.TypeKind.REAL;
        if (type.id()== WorkspaceService.PUBLIC_WORKSPACE.BOOLEAN.id()) return ArlType.TypeKind.BOOLEAN;
        if (type.id()== WorkspaceService.PUBLIC_WORKSPACE.STRING.id()) return ArlType.TypeKind.STRING;
        if (type.id()== WorkspaceService.PUBLIC_WORKSPACE.DATE.id()) return ArlType.TypeKind.DATE;
        return ArlType.TypeKind.INSTANCE;
    }

    public ArlType.CollectionKind collectionKind(Cardinality cardinality) {
        if (cardinality==Cardinality.SINGLE) return ArlType.CollectionKind.SINGLE;
        if (cardinality==Cardinality.SET) return ArlType.CollectionKind.SET;
        if (cardinality==Cardinality.LIST) return ArlType.CollectionKind.LIST;
        if (cardinality==Cardinality.MAP) return ArlType.CollectionKind.MAP;
        throw new ParsingException("property cardinality '%s' not supported", cardinality);
    }
}
