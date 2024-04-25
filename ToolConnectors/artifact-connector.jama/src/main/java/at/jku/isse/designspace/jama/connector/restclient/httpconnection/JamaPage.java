package at.jku.isse.designspace.jama.connector.restclient.httpconnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class JamaPage {
    private JamaClient jamaClient;
    private String url;
    private int startIndex;
    private int resultCount;
    private int totalResults;
    private int maxResults;

    private List<Map<String, Object>> results = new ArrayList<>();

    public JamaPage(int startIndex, int resultCount, int totalResults, int maxResults) {
        this.startIndex = startIndex;
        this.resultCount = resultCount;
        this.totalResults = totalResults;
        this.maxResults = maxResults;
    }

    public JamaPage(int startIndex, int resultCount, int totalResults) {
        this(startIndex, resultCount, totalResults, 50);
    }

    public boolean hasNext() {
        return startIndex + resultCount <= totalResults;
    }



    public JamaPage getNext() throws Exception {
        int nextPageStart = startIndex + maxResults;
        String delim = !url.contains("?") ? "?" : "&";
        return jamaClient.getPage(url, delim + "startAt=" + nextPageStart);
    }

    public void addResource(Map<String, Object> jamaDomainObject) {
        if(jamaDomainObject != null) {
            results.add(jamaDomainObject);
        }
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public void setJamaClient(JamaClient jamaClient) {
        this.jamaClient = jamaClient;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
