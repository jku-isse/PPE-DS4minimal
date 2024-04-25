package at.jku.isse.designspace.core.model;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.service.WorkspaceService;

/**
 * Enumerations are subtypes of EnumerationType and the various literals are Instances of those Enumeration(type)s
 */
public class EnumerationType extends InstanceType {

    static public EnumerationType create(Workspace workspace, String name, String... literalNames) {
        Assert.hasText(name, "Name must not be null or empty");

        EnumerationType enumerationType = new EnumerationType(workspace, name);

        for (String literalName : literalNames) enumerationType.createLiteral(literalName);
        return enumerationType;
    }

    protected EnumerationType(Workspace workspace, String name) { super(workspace, workspace.META_INSTANCE_TYPE, name, workspace.ENUMERATION_TYPE); }
    protected EnumerationType(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    public String className() { return ReservedNames.ENUMERATION_TYPE_CLASS_NAME; }

    public Enumeration createLiteral(String name) {
        return Enumeration.create(workspace, this, name);
    }

    public SetProperty<Enumeration> literals() {
        return (SetProperty)instances();
    }

    public Enumeration literal(String name) {
        for (Enumeration e : literals()) {
            if (e.name().equals(name)) return e;
        }
        return null;
    }

    public static void buildType() {
        WorkspaceService.PUBLIC_WORKSPACE.ENUMERATION_TYPE = WorkspaceService.PUBLIC_WORKSPACE.createInstanceType(ReservedNames.ENUMERATION_NAME, WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER, WorkspaceService.PUBLIC_WORKSPACE.INSTANCE_TYPE);
    }

}
