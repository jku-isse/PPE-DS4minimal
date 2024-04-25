package at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service;

public class ServiceResponse {

    public static final long SUCCESS = 0;
    public static final long INVALID = 1;
    public static final long UNAVAILABLE = 2;
    public static final long UNKNOWN = 3;

    private Long kind;
    private String serviceName;
    private String msg;
    private String instanceId;

    public ServiceResponse(long kind, String serviceName, String msg, String instanceId) {
        this.kind = kind;
        this.serviceName = serviceName;
        this.msg = msg;
        this.instanceId = instanceId;
    }

    public long getKind() {
        return this.kind;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

}