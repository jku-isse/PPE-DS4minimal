package at.jku.isse.designspace.core.controlflow;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.jku.isse.designspace.core.controlflow.controlevents.AutoCommitChangeEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.AutoUpdateChangeEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.CommitEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.ControlEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.NameChangeEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.OperationEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.ToolCreationEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.TransactionEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.UpdateEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.UserCreationEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.WorkspaceCreationEvent;
import at.jku.isse.designspace.core.controlflow.modifiedOutputStream.AppendableObjectOutputstream;
import at.jku.isse.designspace.core.events.Event;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.helper.Time;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.Tool;
import at.jku.isse.designspace.core.model.User;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTree;
import lombok.extern.slf4j.Slf4j;

/**
 * For import and export the statis methods are suffcient
 * and the service must no be activated.
 *
 * In case of service activation every operation will be persisted
 * and on initialization every stored operation will be loaded.
 */

@Service("controleventengine")
@Slf4j
public class ControlEventEngine implements ServiceProvider {

    @Autowired
    public static WorkspaceService workspaceService;

    public static boolean CACHEONLY = true;

    private static String persistenceFileName;
    private static final List<Event> controlEvents = new ArrayList<>();

    private static ObjectOutputStream outputStream = null;
    private static ObjectInputStream inputStream = null;

    private static boolean headerExists = false;
    private static boolean initialized = false;

    private static long timeOffset = 0;
    private static long maxElementId = 0;
    private static long maxOperationId = 0;

    private static int operationsRestored = 0;
    private static int workspacesRestored = 1;

    private static String fileName;

    private ControlEventEngine() {
        ServiceRegistry.registerService(this);
    }

    @Override
    public String getName() {
        return "ControlEventEngine";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean isPersistenceAware() {
        return true;
    }

    @Override
    public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
    }

    public void initialize() {
        try {
            FileReader reader = new FileReader("./application.properties");
            Properties appProps = new Properties();
            appProps.load(reader);
            //String userDir = System.getProperty("user.dir", "");
            String localPersistencePath = appProps.getProperty("persistencePath", "./filepersistence.ds").toString();
//            String persistencePath = userDir + localPersistencePath.replace('/', '\\');
            Path persistenceFile = Paths.get(localPersistencePath);//FileSystems.getDefault().getPath(userDir, localPersistencePath);
            
            initWithPath(persistenceFile, !(Boolean.parseBoolean(appProps.get("persistence.enabled").toString())));
        } catch (Exception e) {
           // initWithPath(PublicWorkspace.persistencePath(), true);
        	CACHEONLY = true;
        	initialized = true;
        }
    }

    /**
     *
     * The String must contain the byte array of an ObjectEventStream of Event,
     * where the byte array has been converted to a String using Base64 encoding.
     *
     * @param fileContent
     * @param persistenceFileName
     */
    public static void importAsString(String fileContent, String persistenceFileName) throws Exception {
        if (inInCleanState()) {
            fileName = persistenceFileName;
            File persistenceFile = new File(fileName);

            if (persistenceFile.exists()) {
                persistenceFile.delete();
            }

            persistenceFile.createNewFile();

            byte[] data = Base64.getDecoder().decode(fileContent);
            log.error("ControlEventEngine: Failed creating the persistence file " + fileName);

            outputStream = new ObjectOutputStream(new FileOutputStream(persistenceFile));
            inputStream = new ObjectInputStream(new ByteArrayInputStream(data));
            readControlEventStream(inputStream, persistenceFile.toString());
        }
    }

    /**
     * Use this for Junit tests
     * @param path
     * @return
     */
    public static String importAsBASE64(Path path) {
        String content;
        try {
            content = Files.readString(path);
            return ControlEventEngine.importAsBASE64(content);
        } catch (IOException e) {
            e.printStackTrace();
            return "Import Error!";
        }
    }

    @Deprecated
    public static String importAsBASE64_old(Path path) {
        String content;
        try {
            content = Files.readString(path);
            return ControlEventEngine.importAsBASE64_old(content, path);
        } catch (IOException e) {
            e.printStackTrace();
            return "Import Error!";
        }
    }

    public static String importAsBASE64(String content){

        if (!Event.isCleanState()) {
            return "Operationtree state is not clean! Import aborted!";
        }
        byte[] data = Base64.getMimeDecoder().decode(content);
        try {
            inputStream = new ObjectInputStream(new ByteArrayInputStream(data));

            ServiceRegistry readServiceRegistry = (ServiceRegistry) inputStream.readObject();

            for (var service : readServiceRegistry.getAllRegisteredServices()) {
                if(!ServiceRegistry.getInstance().containsService(service)){
                    var foundService = ServiceRegistry.getInstance().getServiceDescription(service.getName());
                    if(foundService == null){
                        return "The persistence file was created in an incompatible version! \n"+
                                "Persistence file service: "+service.getName()+" not running!";
                    }

                    if(!foundService.getPriority().equals(service.getPriority())){
                        return "The persistence file was created in an incompatible version! \n Running Service:"+
                                foundService.getName()+" in Priority:"+foundService.getPriority()+" \n "+
                                "Persistence file service is in Priority:"+service.getPriority();
                    }

                    return "The persistence file was created in an incompatible version! \n Running Service:"+
                            foundService.getName()+" in Version:"+foundService.getVersion()+" \n "+
                            "Persistence file service is in Version:"+service.getVersion();
                }
            }

            var result = readControlEventStream(inputStream, "import BASE64");
            if(!result){
                return "The persistence file could not be loaded! Maybe it was created in an incompatible version!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "The persistence file could not be loaded! Maybe it was created in an incompatible version!";
        }
        return "Imported!";
    }

    @Deprecated
    public static String importAsBASE64_old(String content, Path path){
        if (!Event.isCleanState()) {
            return "Operationtree state is not clean! Import aborted!";
        }
        byte[] data = Base64.getDecoder().decode(content);
        try {
            inputStream = new ObjectInputStream(new ByteArrayInputStream(data));
            if(!readControlEventStream(inputStream, path.toString())){
                return "The persistence file could not be loaded! Maybe it was created in an incompatible version!";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Import Error!";
        }
        return "Imported!";
    }

    public static void initWithPath(Path persistenceFile, boolean cacheOnly) {
        CACHEONLY = cacheOnly;
        if (!CACHEONLY) {
            initWithPath(persistenceFile);
        } else
        	initialized = true;
    }

    /**
     *
     * This method tries to load the Designspace from the specified file.
     * Exported Files have to be imported first, in order to use this method.
     *
     */
    public static void initWithPath(Path persistenceFile)  {
        fileName = persistenceFile.toString();
        if (!initialized) {

            try {
                File file = persistenceFile.toFile(); // File(fileName);
                if (file.exists()) {
                    inputStream = new ObjectInputStream(new FileInputStream(file));
                } else {
                    file.createNewFile();
                    log.debug("ControlEventEngine: The storage file did not exist, it was created");
                }
            } catch (Exception e) {
                log.error("ControlEventEngine: Trying to open or create the storage file cause an error");
                e.printStackTrace();
            }

            if (inputStream != null) {
                readControlEventStream(inputStream, persistenceFile.toString());
            }

            try {
                File file = new File(fileName);
                if (!file.exists()) file.createNewFile();
                if (headerExists) {
                    outputStream = new AppendableObjectOutputstream(new FileOutputStream(file, true));
                } else {
                    if (operationsRestored == 0) {
                        file.delete();
                        file.createNewFile();
                    }
                    outputStream = new ObjectOutputStream(new FileOutputStream(file, true));
                }
            } catch (Exception ex) {
                log.error("Error writing persistence file " + fileName);
            }

            initialized = true;
        }
    }

    public static void storeControlEvent(ControlEvent controlEvent) {
        if(!Event.isInitialized()) {
            return;
        }

        if(CACHEONLY){
            controlEvents.add(controlEvent);
            return;
        }
        if (outputStream != null) {
            try {
                outputStream.writeObject(controlEvent);
                controlEvents.add(controlEvent);
                if (controlEvents.size() % 10_000 == 0) {
                    File file = new File(fileName);
                    outputStream.close();
                    outputStream = new AppendableObjectOutputstream(new FileOutputStream(file, true));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Optional<String> exportAsStringFromCache() {
        if(controlEvents.isEmpty()){
            return Optional.of("");
        }

        try {
            String path = new SimpleDateFormat("'export_'yyyy_MM_dd_HH_mm_ss'.dsp'").format(new Date());
            var fileoutputstream = new FileOutputStream(path);
            OutputStream outBase64 = new Base64OutputStream(fileoutputstream);
            ObjectOutputStream oos = new ObjectOutputStream(outBase64);
            int counter = 0;
            oos.writeObject(ServiceRegistry.getInstance());
            for (var event : new ArrayList(controlEvents)) {
                oos.writeObject(event);
                if (counter % 100_000 == 0) {
                    System.out.println(counter+"/"+controlEvents.size());
                }
                counter++;
            }
            System.out.println("Finished! - "+counter+"/"+controlEvents.size());
            oos.flush();
            oos.close();
            outBase64.flush();
            outBase64.close();
            fileoutputstream.flush();
            fileoutputstream.close();
            return Optional.of(Path.of(path).toAbsolutePath().toString());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.of("Could not export data! "+e.getMessage());
        }
    }

    /**
     *
     * This method converts the ObjectStream of ControlEvents to a Base64 encoded String.
     *
     * @return
     */
    public static Optional<String> exportAsString() {
        if (!inInCleanState()) {
            try {
                outputStream.close();
                try {
                    File file = new File(fileName);
                    String fileContent = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                    outputStream = new AppendableObjectOutputstream(new FileOutputStream(file, true));
                    return Optional.of(fileContent);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Error exporting persistence file " + fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    private static boolean readControlEventStream(ObjectInputStream inputStream, String persistenceFile) {
        ControlEvent controlEvent = null;
        try {
            while (true) {
                controlEvent = (ControlEvent) inputStream.readObject();
                //log.trace("Loaded " + operationsRestored + " operations from " + sourceFile);
                applyControlEvent(controlEvent);
            }
        } catch (EOFException e) {
            try {
                inputStream.close();
            } catch (Exception ex) {
            }

            workspacesRestored -= 1;
            log.info("Loaded " + operationsRestored + " operations from " + persistenceFile);
            log.info("Loaded " + workspacesRestored + " workspaces from " + persistenceFile);

            if (operationsRestored > 0) {
                headerExists = true;

                if (controlEvent != null) {
                    Time.prescribeTime(controlEvent.timestamp() + 1);
                    Time.unprescribeTime();
                }

                Id.currentId.getAndSet(maxElementId + 1);

                /*
                 *  maxEventId from all operations is added to EventId generator,
                 * 	this ensures that no id is used twice, even if their might me a gap between the id's
                 * 	depending how the MaxInitOperations are set.
                 *
                 * 	Additionally it must be assured, that when files are loaded the amount and order
                 * 	of the additional deltas is the same, otherwise the references to to elements
                 * 	will no longer be valid. This can be made certain of, by only importing files
                 * 	that were created in a Designspace with the same services and same version.
                 */
                Event.offsetId(maxOperationId + 1);
            }
        } catch (Exception e) {
            System.out.println("The persistence file could not be loaded! Maybe it was created in an incompatible version!");
            e.printStackTrace();
            log.error("Error reading persistence file " + persistenceFile);
            return false;
        }
        return true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void applyControlEvent(ControlEvent controlEvent) {
        switch (controlEvent.getType()) {
            case OPERATION:
                OperationEvent operationEvent = (OperationEvent) controlEvent;
                Operation operation = operationEvent.getOperation();

                if( maxOperationId < operation.id()){
                    maxOperationId = operation.id();
                }

                Workspace workspace = workspaceService.getWorkspace(operationEvent.getWorkspaceId());

                if (workspace != null) {

                    long operationElementId = operation.elementId().value();
                    if (operationElementId > maxElementId) {
                        maxElementId = operationElementId;
                    }

                    ModelHistoryTree.getInstance().add(operation, workspace);
                    operationsRestored++;
                } else {
                    log.debug("ControlEventEngine: The control flow file is corrupted");
                }
                break;

            case WORKSPACE_CREATION:
                WorkspaceCreationEvent workspaceCreationEvent = (WorkspaceCreationEvent) controlEvent;
                recreateWorkspace(workspaceCreationEvent);
                workspacesRestored++;
                break;

            case COMMIT:
                CommitEvent commitEvent = (CommitEvent) controlEvent;
                Workspace workspaceToCommit = workspaceService.getWorkspace(commitEvent.getWorkspaceId());
                if (workspaceToCommit != null) {
                    workspaceToCommit.commit();
                } else {
                    log.debug("ControlEventEngine: The control flow file is corrupted");
                }
                break;

            case UPDATE:
                UpdateEvent updateEvent = (UpdateEvent) controlEvent;
                Workspace workspaceToUpdate = workspaceService.getWorkspace(updateEvent.getWorkspaceId());
                if (workspaceToUpdate != null) {
                    workspaceToUpdate.update();
                } else {
                    log.debug("ControlEventEngine: The control flow file is corrupted");
                }
                break;

            case TRANSACTION:
                TransactionEvent transactionEvent = (TransactionEvent) controlEvent;
                Workspace workspaceConcluded = workspaceService.getWorkspace(transactionEvent.getWorkspaceId());
                if (workspaceConcluded != null) {
                    //triggers listeners and can therefore cause services to react
                    workspaceConcluded.concludeTransactionWithoutNotifyingServices();
                } else {
                    log.debug("ControlEventEngine: The control flow file is corrupted");
                }
                break;

//            case PARENT_CHANGE:
//                ParentChangeEvent parentChangeEvent = (ParentChangeEvent) controlEvent;
//                Workspace childWorkspace = workspaceService.getWorkspace(parentChangeEvent.getWorkspaceId());
//                Workspace parentWorkspace = workspaceService.getWorkspace(parentChangeEvent.getParentWorkspaceId());
//
//                if (childWorkspace != null && parentWorkspace != null) {
//                    childWorkspace.setParent(parentWorkspace, childWorkspace.isAutoUpdate(), childWorkspace.isAutoCommit());
//                } else {
//                    log.debug("ControlEventEngine: The control flow file is corrupted");
//                }
//                break;

            case USER_CREATION:
                UserCreationEvent userCreationEvent = (UserCreationEvent) controlEvent;
                User userToCreate = workspaceService.registerUser(userCreationEvent.getUserName());
                break;

            case TOOL_CREATION:
                ToolCreationEvent toolCreationEvent = (ToolCreationEvent) controlEvent;
                Tool toolToCreate = workspaceService.registerTool(toolCreationEvent.getToolName(), toolCreationEvent.getVersion());
                break;

            case AUTO_COMMIT_CHANGE:
                AutoCommitChangeEvent autoCommitChangeEvent = (AutoCommitChangeEvent) controlEvent;
                Workspace workspaceModified = workspaceService.getWorkspace(autoCommitChangeEvent.getWorkspaceId());

                if (workspaceModified != null) {
                    workspaceModified.setAutoCommit(autoCommitChangeEvent.getToValue());
                } else {
                    log.debug("ControlEventEngine: The control flow file is corrupted");
                }
                break;

            case AUTO_UPDATE_CHANGE:
                AutoUpdateChangeEvent autoUpdateChange = (AutoUpdateChangeEvent) controlEvent;
                Workspace workspaceChanged = workspaceService.getWorkspace(autoUpdateChange.getWorkspaceId());

                if (workspaceChanged != null) {
                    workspaceChanged.setAutoCommit(autoUpdateChange.getToValue());
                } else {
                    log.debug("ControlEventEngine: The control flow file is corrupted");
                }
                break;

            case NAME_CHANGE:
                NameChangeEvent nameChangeEvent = (NameChangeEvent) controlEvent;
                Workspace workspaceRenamed = workspaceService.getWorkspace(nameChangeEvent.getWorkspaceId());

                if (workspaceRenamed != null) {
                    workspaceRenamed.setName(nameChangeEvent.getToValue());
                } else {
                    log.debug("ControlEventEngine: The control flow file is corrupted");
                }
                break;

        }
    }

    private static Workspace recreateWorkspace(WorkspaceCreationEvent workspaceCreationEvent) {
        Workspace workspace = workspaceService.getWorkspace(workspaceCreationEvent.getWorkspaceId());

        if (workspace != null) {
            return workspace;
        }

        Workspace parent = null;
        if (workspaceCreationEvent.getParentWorkspaceId() != -1) {
            parent = workspaceService.getWorkspace(workspaceCreationEvent.getParentWorkspaceId());
        }

        User user = null;
        if (workspaceCreationEvent.getUserId() != -1) {
            user = workspaceService.getUser(workspaceCreationEvent.getUserId());
            if (user==null) {
                log.debug("FileStorageEngine: The storage file is corrupted");
            }
        }

        Tool tool = null;
        if (workspaceCreationEvent.getToolId() != -1) {
            tool = workspaceService.getTool(workspaceCreationEvent.getToolId());
            if (tool == null) {
                log.debug("FileStorageEngine: The storage file is corrupted");
            }
        }

        workspace = workspaceService.createWorkspace(workspaceCreationEvent.getWorkspaceName(),
                parent,
                user,
                tool,
                workspaceCreationEvent.isAutoUpdate(),
                workspaceCreationEvent.isAutoCommit());

        return workspace;
    }

    public static boolean inInCleanState() {
        return controlEvents.isEmpty();
    }

}