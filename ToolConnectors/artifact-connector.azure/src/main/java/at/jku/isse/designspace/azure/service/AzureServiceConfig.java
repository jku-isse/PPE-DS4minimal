package at.jku.isse.designspace.azure.service;

import com.google.inject.AbstractModule;

import at.jku.isse.designspace.azure.api.AzureApi;
import at.jku.isse.designspace.azure.api.IAzureApi;
import at.jku.isse.designspace.azure.idcache.IdCache;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;

public class AzureServiceConfig extends AbstractModule {

    private IAzureApi azureApi;
    private Workspace workspace;
    private IdCache idCache;

    public AzureServiceConfig() {
        azureApi = new AzureApi();
        workspace = WorkspaceService.PUBLIC_WORKSPACE;
        idCache = new IdCache(workspace);
    }

    @Override
    protected void configure() {
        bind(IAzureApi.class).toInstance(azureApi);
        bind(Workspace.class).toInstance(workspace);
        bind(IdCache.class).toInstance(idCache);
    }
}
