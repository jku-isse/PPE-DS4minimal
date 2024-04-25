package at.jku.isse.designspace.rule.arl.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.jku.isse.designspace.rule.arl.evaluator.ModelAccess;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;

public class ArlType {

	public enum TypeKind {ANY, NUMBER, REAL, INTEGER, BOOLEAN, STRING, DATE, NULL, INSTANCE, TUPLE};
	public enum CollectionKind {ANY, SINGLE, COLLECTION, SET, LIST, MAP};

	static public HashMap<String, ArlType> ARLTYPES = new HashMap<String, ArlType>();

	//commonly used, predefined
	static public ArlType ANY = new ArlType(TypeKind.ANY, CollectionKind.ANY, null);

	static public ArlType NUMBER = new ArlType(TypeKind.NUMBER, CollectionKind.SINGLE, null);
	static public ArlType REAL = new ArlType(TypeKind.REAL, CollectionKind.SINGLE, null);
	static public ArlType INTEGER = new ArlType(TypeKind.INTEGER, CollectionKind.SINGLE, null);
	static public ArlType BOOLEAN = new ArlType(TypeKind.BOOLEAN, CollectionKind.SINGLE, null);
	static public ArlType STRING = new ArlType(TypeKind.STRING, CollectionKind.SINGLE, null);
	static public ArlType DATE = new ArlType(TypeKind.DATE, CollectionKind.SINGLE, null);
	static public ArlType NULL = new ArlType(TypeKind.NULL, CollectionKind.SINGLE, null);

	static public ArlType COLLECTION = new ArlType(TypeKind.ANY, CollectionKind.COLLECTION, null);
	static public ArlType LIST = new ArlType(TypeKind.ANY, CollectionKind.LIST, null);
	static public ArlType SET = new ArlType(TypeKind.ANY, CollectionKind.SET, null);
	static public ArlType MAP = new ArlType(TypeKind.ANY, CollectionKind.MAP, null);

	static public ArlType INSTANCE = new ArlType(TypeKind.INSTANCE, CollectionKind.SINGLE, null);

	public TypeKind type;
	public CollectionKind collection;
	public Object nativeType;


	//************ FIND ARLTYPE BY NAME
	static public ArlType get(List<String> typeName, String collectionName) {
		TypeKind type = TypeKind.ANY;
		if (typeName!=null) {
			switch (typeName.get(typeName.size() - 1).toLowerCase()) {
				case "number":
					type = TypeKind.NUMBER;
					break;
				case "real":
					type = TypeKind.REAL;
					break;
				case "integer":
					type = TypeKind.INTEGER;
					break;
				case "boolean":
					type = TypeKind.BOOLEAN;
					break;
				case "string":
					type = TypeKind.STRING;
					break;
				case "date":
					type = TypeKind.DATE;
					break;
				default:
					type = TypeKind.INSTANCE;
			}
		}
		CollectionKind collection = CollectionKind.SINGLE;
		if (collectionName!=null) {
			switch (collectionName.toLowerCase()) {
				case "collection":
					collection = CollectionKind.COLLECTION;
					break;
				case "set":
					collection = CollectionKind.SET;
					break;
				case "list":
					collection = CollectionKind.LIST;
					break;
				case "map":
					collection = CollectionKind.MAP;
					break;
			}
		}
		if (type==TypeKind.INSTANCE)
			return ModelAccess.instance.arlTypeByQualifiedName(typeName);
		else
			return get(type, collection, null);
	}

	static public ArlType get(Object value) {
		switch (value.getClass().getName()) {
			case "java.lang.Long":
				return INTEGER;
			case "java.lang.Double":
				return REAL;
			case "java.lang.Boolean":
				return BOOLEAN;
			case "java.lang.String":
				return STRING;
			case "java.util.Date":
				return DATE;
			default:
				CollectionKind collection = CollectionKind.ANY;
				if (value instanceof Set)
					collection = CollectionKind.SET;
				else if (value instanceof List)
					collection = CollectionKind.LIST;
				else if (value instanceof Map)
					collection = CollectionKind.MAP;
				else
					return ModelAccess.instance.arlTypeOfInstance(value);

				TypeKind type = TypeKind.ANY;
				if (((Collection)value).size()>0) {
					ArlType collectionElementType = ModelAccess.instance.arlTypeOfInstance(((Collection)value).iterator().next());
					return get(collectionElementType.type, collection, collectionElementType.nativeType);
				}
				else
					return get(TypeKind.ANY, collection, null);
		}
	}

	//************ FIND ARLTYPE BY PROPERTIES
	static public ArlType get(TypeKind type, CollectionKind collection, Object nativeType) {
		//make sure we reuse basic types for easier matching
		ArlType existingType = ARLTYPES.get(uuid(type, collection, nativeType));
		if (existingType!=null) return existingType;

		return new ArlType(type, collection, nativeType);
	}

	private ArlType(TypeKind type, CollectionKind collection, Object nativeType) {
		this.type = type;
		this.collection = collection;
		if (nativeType instanceof ArlType)
			throw new ParsingException("native type cannot be arl type");
		this.nativeType = nativeType;

		ARLTYPES.put(toString(), this);
	}

	public static String uuid(TypeKind type, CollectionKind collection, Object nativeType) {
		return ""+collection+"<"+(type==TypeKind.INSTANCE?nativeType:type)+">";
	}

	public String toString() { return uuid(type, collection, nativeType); }

	public ArlType toSingle() { return get(type, CollectionKind.SINGLE, nativeType); }

	public boolean equals(ArlType otherType) {
		return (this.conformsTo(otherType) && otherType.conformsTo(this));
	}

	public ArlType commonSuperType(ArlType otherType) {
		if (this.conformsTo(otherType)) return this;
		if (this==INTEGER) return NUMBER.commonSuperType(otherType);
		if (this==REAL) return NUMBER.commonSuperType(otherType);
		return ANY;
	}

	public ArlType superType() {
		if (this.collection!=CollectionKind.SINGLE) return ANY;
		if (this==INTEGER) return NUMBER;
		if (this==REAL) return NUMBER;
		if (this.type==TypeKind.INSTANCE) return ModelAccess.instance.arlSuperTypeOfType(this);
		return ANY;
	}

	/**
	 * @return true, if this type conforms with (is assignable to) the other type
	 */
	public boolean conformsTo(ArlType otherType) {

		switch (this.collection) {
			case ANY:
				break;
			case SINGLE:
				if (otherType.collection != CollectionKind.SINGLE && otherType.collection != CollectionKind.ANY) return false;
				break;
			case COLLECTION:
				if (otherType.collection != CollectionKind.COLLECTION && otherType.collection != CollectionKind.ANY) return false;
				break;
			case LIST:
				if (otherType.collection != CollectionKind.LIST && otherType.collection != CollectionKind.COLLECTION && otherType.collection != CollectionKind.ANY) return false;
				break;
			case SET:
				if (otherType.collection != CollectionKind.SET && otherType.collection != CollectionKind.COLLECTION && otherType.collection != CollectionKind.ANY) return false;
				break;
			default:
				throw new EvaluationException("collection '%s' not supported", this.collection);
		}

		switch (this.type) {
			case ANY:
				break;
			case INTEGER:
				if (otherType.type !=TypeKind.INTEGER && otherType.type !=TypeKind.NUMBER && otherType.type !=TypeKind.ANY) return false;
				break;
			case REAL:
				if (otherType.type !=TypeKind.REAL && otherType.type !=TypeKind.NUMBER && otherType.type !=TypeKind.ANY) return false;
				break;
			case BOOLEAN:
				if (otherType.type !=TypeKind.BOOLEAN && otherType.type !=TypeKind.ANY) return false;
				break;
			case STRING:
				if (otherType.type !=TypeKind.STRING && otherType.type !=TypeKind.ANY) return false;
				break;
			case NULL:
				break;
			case INSTANCE:
				if (this.nativeType!=null && otherType.nativeType!=null && !ModelAccess.instance.arlIsKindOf(this,otherType)) return false;
				break;
			case TUPLE:
				break;
			default: throw new EvaluationException("type '%s' not supported", this.type);
		}

		return true;
	}

	public boolean isContainable(ArlType otherType){
		if (otherType.collection==CollectionKind.SINGLE) return false;
		return this.conformsTo(ArlType.get(otherType.type, CollectionKind.SINGLE, otherType.nativeType));
	}

	/**
	 * @return true, if this type is comparable with the other type (<,>,=)
	 */
	public boolean isComparable(ArlType otherType) {
		if (this.collection != otherType.collection) return false;

		if (this.type ==otherType.type) return true;
		if (this.type ==TypeKind.INTEGER && otherType.type ==TypeKind.REAL) return true;
		if (this.type ==TypeKind.INTEGER && otherType.type ==TypeKind.NUMBER) return true;
		if (this.type ==TypeKind.REAL && otherType.type ==TypeKind.INTEGER) return true;
		if (this.type ==TypeKind.REAL && otherType.type ==TypeKind.NUMBER) return true;
		if (this.type ==TypeKind.NUMBER && otherType.type ==TypeKind.INTEGER) return true;
		if (this.type ==TypeKind.NUMBER && otherType.type ==TypeKind.REAL) return true;
		if (this.type ==TypeKind.INSTANCE && otherType.type ==TypeKind.NULL) return true;
		if (this.type ==TypeKind.NULL && otherType.type ==TypeKind.INSTANCE) return true;
		return false;
	}

	public ArlType propertyType(String property) {
		if (type ==TypeKind.INSTANCE) return ModelAccess.instance.arlTypeOfProperty(this, property);
		throw new ParsingException("'%s' does not have property '%s'.", this, property);
	}
}
