
package at.jku.isse.designspace.core.model;

import static at.jku.isse.designspace.core.model.ReservedNames.BOOLEAN_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.CONTAINED_FOLDER;
import static at.jku.isse.designspace.core.model.ReservedNames.DATE_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.ELEMENT_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.INSTANCE_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.INTEGER_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.META_INSTANCE_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.META_PROPERTY_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.PROJECTS_FOLDER_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.REAL_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.ROOT_FOLDER_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.STRING_NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.TYPES_FOLDER_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;

//import at.jku.isse.designspace.core.repository.PartitionedElementRepository;
import at.jku.isse.designspace.core.service.WorkspaceService;

/**
 * The Public Workspace is the top-level root workspace. It has children but no parent.
 * see Workspace for more details on what a workspace is.
 * This Public Workspace is also the very first workspace to be created and it will trigger the creation
 * of many other elements that are part (e.g., all essential types such as Folder, all pre-defined instances
 * such as typesFolder). Most of the basic workspace behaviors are, however, disabled (e.g., it is not
 * possible to commit a Public Workspace)
 */
public class PublicWorkspace extends Workspace {


    //****************************************************************************
    //*** PROPERTIES
    //****************************************************************************

    @Value("${persistencePath}")
    static private String persistencePath;

    @Value("${captureInstanceTypes}")
    static private boolean captureInstanceTypes=false;

    @Value("${captureInstances}")
    static private boolean captureInstances=false;

    static public String capturePath=null;

    @Value("${replayAtStartup}")
    static private List<String> replayAtStartup=null;

    public static void createPublicWorkspace() {
        try {
            Properties props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            persistencePath = props.getProperty("persistencePath","");
            persistencePath = System.getProperty("user.dir","") + persistencePath.replace('/', '\\');

            capturePath = props.getProperty("capturePath","");
            captureInstanceTypes = Boolean.parseBoolean(props.getProperty("captureInstanceTypes","false").trim());
            captureInstances = Boolean.parseBoolean(props.getProperty("captureInstances","false").trim());
            if(props.containsKey("replayAtStartup")){
                replayAtStartup = Arrays.asList(props.getProperty("replayAtStartup","").split(","));
            }else{
                replayAtStartup = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.isNull(WorkspaceService.PUBLIC_WORKSPACE, "Root workspace was already created");
        new PublicWorkspace();
    }

    public static String persistencePath() { return persistencePath; }

    public static String capturePath() { return capturePath; }
    public static boolean captureInstanceTypes() { return captureInstanceTypes; }
    public static boolean captureInstances() { return captureInstances; }
    public static List<String> replayAtStartup() { return replayAtStartup; }



    //****************************************************************************
    //*** CONSTRUCTOR
    //****************************************************************************

    public PublicWorkspace() {
        super();

        WorkspaceService.PUBLIC_WORKSPACE = this;

        //this.partitionedRepository = partitionedRepository;
        //this.repository = partitionedRepository.getPartition(id(), this, null);

        ELEMENT = new MetaInstance(this); //1
        META_INSTANCE_TYPE = new MetaInstance(this); //2
        META_PROPERTY_TYPE = new MetaProperty(this); //3
        INSTANCE_TYPE = new MetaInstance(this, META_INSTANCE_TYPE); //4

        STRING = new MetaInstance(this); //5
        INTEGER = new MetaInstance(this); // 6
        REAL = new MetaInstance(this); //7
        BOOLEAN = new MetaInstance(this); //8
        DATE = new MetaInstance(this); //9

        GENERIC_SINGLE_PROPERTY_TYPE = new MetaPropertyType(this, "GenericSinglePropertyType", Cardinality.SINGLE); //10
        GENERIC_SET_PROPERTY_TYPE = new MetaPropertyType(this, "GenericSetPropertyType", Cardinality.SET); //11
        GENERIC_LIST_PROPERTY_TYPE = new MetaPropertyType(this, "GenericListPropertyType", Cardinality.LIST); //12
        GENERIC_MAP_PROPERTY_TYPE = new MetaPropertyType(this, "GenericMapPropertyType", Cardinality.MAP); //13

        //reserve the first 100 numbers for pre-defined elements (so that sdk can refer to them by id)
        Id.currentId=new AtomicLong(100l);

        ((MetaProperty) META_PROPERTY_TYPE).define(META_PROPERTY_NAME);
        ((MetaInstance) META_INSTANCE_TYPE).define(META_INSTANCE_NAME, ELEMENT);

        ((MetaInstance) ELEMENT).defineWithoutPropertyTypes(ELEMENT_NAME, null);

        ((MetaInstance) STRING).defineWithoutPropertyTypes(STRING_NAME, ELEMENT);
        ((MetaInstance) INTEGER).defineWithoutPropertyTypes(INTEGER_NAME, ELEMENT);
        ((MetaInstance) REAL).defineWithoutPropertyTypes(REAL_NAME, ELEMENT);
        ((MetaInstance) BOOLEAN).defineWithoutPropertyTypes(BOOLEAN_NAME, ELEMENT);
        ((MetaInstance) DATE).defineWithoutPropertyTypes(DATE_NAME, ELEMENT);

        ((MetaInstance) INSTANCE_TYPE).define(INSTANCE_NAME, ELEMENT);

        Folder.buildType();
            new SingleProperty(ELEMENT, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(META_INSTANCE_TYPE, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(META_PROPERTY_TYPE, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(INSTANCE_TYPE, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(STRING, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(INTEGER, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(REAL, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(BOOLEAN, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
            new SingleProperty(DATE, CONTAINED_FOLDER, null, ELEMENT.getPropertyType(CONTAINED_FOLDER));
        EnumerationType.buildType();

        ROOT_FOLDER = Folder.create(this, ROOT_FOLDER_NAME);
        PROJECTS_FOLDER = ROOT_FOLDER.createSubfolder(PROJECTS_FOLDER_NAME);
        TYPES_FOLDER = ROOT_FOLDER.createSubfolder(TYPES_FOLDER_NAME);

        TYPES_FOLDER.addElement(ELEMENT);
        TYPES_FOLDER.addElement(META_INSTANCE_TYPE);
        TYPES_FOLDER.addElement(META_PROPERTY_TYPE);
        TYPES_FOLDER.addElement(INSTANCE_TYPE);
        TYPES_FOLDER.addElement(STRING);
        TYPES_FOLDER.addElement(INTEGER);
        TYPES_FOLDER.addElement(REAL);
        TYPES_FOLDER.addElement(BOOLEAN);
        TYPES_FOLDER.addElement(DATE);
        TYPES_FOLDER.addElement(ENUMERATION_TYPE);
        TYPES_FOLDER.addElement(FOLDER_TYPE);

        STRING_PROPERTY = new MetaPropertyType(this, "STRING", Cardinality.SINGLE, STRING);
        INTEGER_PROPERTY = new MetaPropertyType(this, "INTEGER", Cardinality.SINGLE, INTEGER);
        REAL_PROPERTY = new MetaPropertyType(this, "REAL", Cardinality.SINGLE, REAL);
        BOOLEAN_PROPERTY = new MetaPropertyType(this, "BOOLEAN", Cardinality.SINGLE, BOOLEAN);
        DATE_PROPERTY = new MetaPropertyType(this, "DATE", Cardinality.SINGLE, DATE);
        REFERENCE_PROPERTY = new MetaPropertyType(this, "REFERENCE", Cardinality.SINGLE, ELEMENT);

        name = "Public";
        //parentWorkspace = null;
        user = WorkspaceService.ANY_USER;
        tool = null;

        concludeTransaction();
    }

    //****************************************************************************
    //*** CHANGES
    //****************************************************************************

    public boolean isPublic() { return true; }

    public void setParent(PublicWorkspace newParentWorkspace, boolean autoUpdate, boolean autoCommit) {}
    public void setAutoUpdate(boolean autoUpdate) {}
    public void setAutoCommit(boolean autoCommit) {}
    public void update() {}
    public void commit() {}

    public PublicWorkspace parentWorkspace() {
        return null;
    }

    public String name() { return "Public"; }
    
    @Override
    public String toString() { return "Public{"+id()+"}"; }

}
