package at.jku.isse.designspace.azure.api;

import static java.net.http.HttpClient.newHttpClient;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

public class AzureApi implements IAzureApi {
    final String baseUrl = "https://dev.azure.com/";
    String organizationName;
    String projectName;
    HttpClient client;
    String authHeader;

    public AzureApi() {
        client = newHttpClient();
        Properties props = new Properties();

        //try to load the config from the running directory
        try {
            FileReader reader = new FileReader("./application.properties");
            props.load(reader);
            reader.close();
        } catch (IOException ioe) {
            try {
                props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        organizationName = props.getProperty("azureOrganization").trim();
        projectName = props.getProperty("azureProject").trim();
        String toEncode = props.getProperty("azureConnectorUsername").trim() + ":" + props.getProperty("azurePAT").trim();
        String encodedBytes = Base64.getEncoder().encodeToString(toEncode.getBytes());
        authHeader = "Basic " + encodedBytes;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getOrganizationName() {
        return organizationName;
    }

    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Authorization", authHeader)
                .GET()
                .build();
    }

    private byte[] sendRequest(HttpRequest request) {
        byte[] responseByteArray = null;

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
  //          System.out.println(response); //TODO: remove when not needed
            responseByteArray = response.body();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return responseByteArray;
    }

    @Override
    public byte[] getWorkItem(String projectName, int id) {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workitems/" + id + "?$expand=all");
        return sendRequest(request);
    }

    @Override
    public byte[] getAllWorkItems(List<Integer> ids) {
        StringBuilder idsString = new StringBuilder();
        //build up string of ids to pass in the url
        for (int i = 0; i < ids.size(); i++) {
            idsString.append(ids.get(i));
            if (i < (ids.size() - 1)) {
                idsString.append(",");
            }
        }

        System.out.println(idsString);
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workitems?ids=" + idsString +"&$expand=all");
        return sendRequest(request);
    }

    @Override
    public byte[] getAllWorkItemIds() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + organizationName + "/" + projectName + "/_apis/wit/wiql?api-version=6.1-preview.2"))
                .headers("Authorization", authHeader)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"query\": \"Select [System.Id] From WorkItems\"}"))
                .build();
        return sendRequest(request);
    }

    @Override
    public byte[] getWorkItemRelationTypes() {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/_apis/wit/workitemrelationtypes");
        return sendRequest(request);
    }

    @Override
    public byte[] getProject(String projectId) {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/_apis/projects/" + projectId);
        return sendRequest(request);
    }

    @Override
    public byte[] getComments(int workItemId) {
        HttpRequest request =
                buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workItems/" + workItemId + "/comments");
        return sendRequest(request);
    }

    @Override
    public byte[] getComment(int workItemId, int commentId) {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workItems/" + workItemId
            + "/comments/" + commentId);
        return sendRequest(request);
    }

    @Override
    public byte[] getWorkItemUpdates(int workItemId) {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workItems/" + workItemId + "/updates");
        return sendRequest(request);
    }

    @Override
    public byte[] getWorkItemTypes() {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workitemtypes");
        return sendRequest(request);
    }

    @Override
    public byte[] getWorkItemTypeCategories() {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workitemtypecategories");
        return sendRequest(request);
    }

    @Override
    public byte[] getWorkItemTypesField(String workItemType) {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workitemtypes/" + workItemType + "/fields");
        return sendRequest(request);
    }

    @Override
    public byte[] getWorkItemTransitions(int[] ids) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < ids.length; i++) {
            sb.append(ids[i]);
            if(i < ids.length - 1) {
                sb.append(",");
            }
        }
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/_apis/wit/workitemtransitions?ids=" + sb.toString());
        return sendRequest(request);
    }

    @Override
    public byte[] getWorkItemTypeStates(String workItemType) {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workitemtypes/" + workItemType + "/states");
        return sendRequest(request);
    }

    @Override
    public byte[] getUserByDescriptor(String userDescriptor) {
        HttpRequest request = buildGetRequest("https://vssps.dev.azure.com/" + organizationName + "/_apis/graph/users/" + userDescriptor);
        return sendRequest(request);
    }

    /*
    According to Microsoft, this API endpoint is used to resolve legacy identity information for use with older APIs
     */
    @Override
    public byte[] getUserByEmail(String email) {
        HttpRequest request = buildGetRequest("https://vssps.dev.azure.com/" + organizationName + "/_apis/identities?searchFilter=General&filterValue="
            + email + "&api-version=7.1-preview.1");
        return sendRequest(request);
    }

    @Override
    public byte[] getProjectFields() {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/fields");
        return sendRequest(request);
    }

    @Override
    public byte[] getSpecificWorkItemType(String workItemType) {
        HttpRequest request = buildGetRequest(baseUrl + organizationName + "/" + projectName + "/_apis/wit/workitemtypes/" + workItemType);
        return sendRequest(request);
    }

}
