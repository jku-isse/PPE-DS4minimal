package at.jku.isse.designspace.core.model;


public class ReservedNames {

    /**
     * InstanceType names
     */
    public static final String ELEMENT_NAME = "Element";

    public static final String META_INSTANCE_NAME = "MetaInstanceType";
    public static final String META_PROPERTY_NAME = "MetaPropertyInstanceType";

    public static final String INSTANCE_NAME = "Instance";

    public static final String FOLDER_NAME = "Folder";
    public static final String ENUMERATION_NAME = "Enumeration";

    public static final String STRING_NAME = "String";
    public static final String INTEGER_NAME = "Integer";
    public static final String REAL_NAME = "Real";
    public static final String BOOLEAN_NAME = "Boolean";
    public static final String DATE_NAME = "Date";

    public static final String PROPERTY_TYPE_DECLARATION = "PropertyTypeDeclaration";

    public final static String ROOT_FOLDER_NAME = "root";
    public final static String TYPES_FOLDER_NAME = "types";
    public final static String PROJECTS_FOLDER_NAME = "projects";

    public final static String QUALIFIED_NAME_SEPARATOR = "/";

    /**
     * Property Names used by TypedElements
     */
    public static final String NAME = "name";

    /**
     * Property Names used by InstanceTypes
     */
    public static final String INSTANCE_OF = "@instanceOf";
    public static final String INSTANCES = "@instances";
    public static final String SUPER_TYPES = "@superTypes";
    public static final String SUB_TYPES = "@subTypes";
    public static final String INSTANTIATION_CLASS = "@instantiationClass";
    public static final String Authorized_Users = "@authorizedUsers";

    public static final String PROPERTY_DEFINITION_PREFIX = "@propertyTypeOf_";
    public static final String PROPERTY_TYPES = "@propertyTypes";
    public static final String PROPERTY_OWNER = "@propertyOwner";
    public static final String OWNER_INSTANCE_TYPE = "ownerInstanceType";
    public static final String CARDINALITY = "cardinality";
    public static final String ISOPTIONAL = "isOptional";
    public static final String REFERENCED_INSTANCE_TYPE = "referencedInstanceType";
    public static final String NATIVE_TYPE = "nativeType";
    public static final String DERIVED_RULE = "derivedRule";
    public static final String OPPOSED_PROPERTY_TYPE = "opposedPropertyType";

    public static final String CONTAINED_FOLDER = "@containedFolder";

    public static final String OWNER = "@owner";
    public static final String MEMBERS = "@members";

    public static final String FOLDER_CONTENT = "folderContent";
    public static final String FOLDER_SUBFOLDERS = "subfolders";
    public static final String FOLDER_PARENT = "parentFolder";
    public static final String OWNERSHIP_PROPERTY = "modifiedBy";

    public static final String INSTANCETYPE_PROPERTY_METADATA = "@propertyMetadata";

    public static final String IS_CONTAINER = "isContainer";
    public static final String IS_CONTAINED = "isContained";


    /**
     * Class Names
     */
    public static final String INSTANCE_CLASS_NAME = "core.model.Instance";
    public static final String INSTANCE_TYPE_CLASS_NAME = "core.model.InstanceType";
    public static final String PROPERTY_TYPE_CLASS_NAME = "core.model.PropertyType";
    public static final String FOLDER_CLASS_NAME = "core.model.Folder";
    public static final String ENUMERATION_TYPE_CLASS_NAME = "core.model.EnumerationType";
    public static final String ENUMERATION_CLASS_NAME = "core.model.Enumeration";



}