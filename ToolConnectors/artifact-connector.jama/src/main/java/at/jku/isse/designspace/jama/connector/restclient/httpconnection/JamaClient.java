package at.jku.isse.designspace.jama.connector.restclient.httpconnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.jku.isse.designspace.jama.connector.restclient.exception.RestClientException;
import at.jku.isse.designspace.jama.utility.AccessToolsJSON;

public class JamaClient {
    private HttpClient httpClient;
    private String username;
    private String password;
    private String baseUrl;
    private String apiKey = null;

    public JamaClient(HttpClient httpClient,  String baseUrl, String username, String password) {
        this.httpClient = httpClient;
        //this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        setBaseUrl(baseUrl);
        
        
    }

  
    public void setBaseUrl(String baseUrl) {
        if(baseUrl.contains("/rest/")) {
            if(baseUrl.endsWith("/")) {
                this.baseUrl = baseUrl;
                return;
            }
            this.baseUrl = baseUrl + "/";
            return;
        }
        if(baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl + "rest/v1/";
            return;
        }
        this.baseUrl = baseUrl + "/rest/v1/";
    }

  
    
    public Map<String, Object> getResource(String resource) throws Exception {
        Response response = httpClient.get(baseUrl + resource, username, password, apiKey);
        if (response == null) return null;  // addition for offline mode: OfflineHttpClientMock always returns null!
        return new ObjectMapper().readValue(response.getResponse(), Map.class);        
    }

//    public JamaPage getPage(String url, JamaInstance jamaInstance) throws RestClientException {
//        return getPage(url, jamaInstance);
//    }

    public JamaPage getPage(String url) throws Exception {
        return getPage(url, "");
    }


    public JamaPage getPage(String url, String startAt) throws Exception {
        Response response = httpClient.get(url + startAt, username, password, apiKey);
        if (response == null) return null;  // addition for offline mode: OfflineHttpClientMock always returns null!
        JamaPage page = parseToPage(response.getResponse());
        page.setJamaClient(this);
        page.setUrl(url);
        return page;
    }

    public List<Map<String, Object>> getAll(String url) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        JamaPage page = getPage(baseUrl + url);
        if (page == null) return new ArrayList<>(); // addition for offline mode: OfflineHttpClientMock always returns null!
        results.addAll(page.getResults());
        while(page.hasNext()) {
            page = page.getNext();
            results.addAll(page.getResults());
        }
        return results;
    }


    public void ping() throws RestClientException {
        httpClient.get(baseUrl, username, password, apiKey);
    }

    public JamaPage parseToPage(String json) throws Exception {
    	Map<String, Object> response = new ObjectMapper().readValue(json, Map.class);
    	
    	Map<String, Object> meta = AccessToolsJSON.accessMap(response, "meta");
    	Map<String, Object> pageInfo = AccessToolsJSON.accessMap(meta, "pageInfo");        
        if(pageInfo == null) {
            // todo handle beta case
            throw new RuntimeException("not implemented");
        }
        int startIndex = AccessToolsJSON.accessInteger(pageInfo, "startIndex");
        int resultCount = AccessToolsJSON.accessInteger(pageInfo, "resultCount");
        int totalResults = AccessToolsJSON.accessInteger(pageInfo, "totalResults");
        JamaPage page = new JamaPage(startIndex, resultCount, totalResults);

        ArrayList<Object> data = AccessToolsJSON.accessArray(response, "data");
        for(Object object : data) {                   
            page.addResource((Map<String, Object>)object);
        }
        return page;
    }


}
