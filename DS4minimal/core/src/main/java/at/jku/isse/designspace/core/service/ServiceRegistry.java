package at.jku.isse.designspace.core.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import at.jku.isse.designspace.core.model.ServiceProvider;

public class ServiceRegistry implements Serializable {

    private static ServiceRegistry instance;
    private Map<Integer, ServiceDescription> services;
    private Map<String, Integer> serviceOrders;

    public ServiceRegistry() {
        this.services = new HashMap<>();
        this.serviceOrders = new HashMap<>();
    }

    public static void registerService(ServiceProvider service) {
        if (instance == null) {
            instance = new ServiceRegistry();
        }

        if (instance.serviceOrders.containsKey(service.getName())) {
            throw new IllegalStateException("Service already registered!");
        }
        if (instance.services.containsKey(service.getPriority())) {
            throw new IllegalStateException("Service priority already reserved!");
        }

        instance.services.put(service.getPriority(), new ServiceDescription(service.getName(), service.getVersion(), service.getPriority(), () -> service.initialize(), service.isPersistenceAware()));
        instance.serviceOrders.put(service.getName(), service.getPriority());
    }

    public static void initializeAllPersistenceAwareServices(){
        getInstance().getAllRegisteredServices().stream().filter(sd -> sd.isPersistenceAware()).sorted().forEach(sd -> sd.getInitFunction().run());
    }

    public static void initializeAllPersistenceUnawareServices(){
        getInstance().getAllRegisteredServices().stream().filter(sd -> !sd.isPersistenceAware()).sorted().forEach(sd -> sd.getInitFunction().run());
    }

    public static ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    public static void setInstance(ServiceRegistry registry) {
        instance = registry;
    }

    public Collection<ServiceDescription> getAllRegisteredServices(){
        return services.values();
    }

    public boolean containsService(ServiceDescription service){
        if(serviceOrders.containsKey(service.getName())){
            var serviceOrder = serviceOrders.get(service.getName());
            if(!service.getPriority().equals(serviceOrder)){
                return false;
            }
            if(services.containsKey(serviceOrder)){
                ServiceDescription foundService = services.get(serviceOrder);
                if(foundService.getVersion().equals(service.getVersion())){
                    return true;
                }
            }
        }
        return false;
    }

    public ServiceDescription getServiceDescription(String name){
        if(serviceOrders.containsKey(name)){
            var serviceOrder = serviceOrders.get(name);
            if(services.containsKey(serviceOrder)){
                ServiceDescription foundService = services.get(serviceOrder);
                return foundService;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        ServiceRegistry serviceRegistry = (ServiceRegistry) o;
        return serviceRegistry.serviceOrders.equals(instance.serviceOrders);
    }

}
