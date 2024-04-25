package at.jku.isse.designspace.jira.config;

import java.net.URI;
import java.util.Properties;

import com.atlassian.httpclient.api.HttpClient;
import com.google.inject.AbstractModule;

import at.jku.isse.designspace.jira.restclient.connector.HttpClientFactory;
import at.jku.isse.designspace.jira.restclient.connector.JiraRestClient;

public class JiraTicketServiceConfig extends AbstractModule {

    private JiraRestClient jiraRestClient;

    //here the fields to be fetched can be specified,
    //if non are chosen all are fetched
    private String[] fields;

    public JiraTicketServiceConfig(Properties props, String[] fields) {
        this.fields = fields;
        String uri = props.getProperty("jiraServerURI").trim();
        String username = props.getProperty("jiraConnectorUsername").trim();
        String pw = props.getProperty("jiraConnectorPassword").trim();
        URI baseUri = URI.create(uri);
        HttpClientFactory.init(uri, username, pw);
        HttpClient client = HttpClientFactory.createHttpClient();
        this.jiraRestClient = new JiraRestClient(baseUri, username, pw);
    }

    protected void configure() {
        this.bind(JiraRestClient.class).toInstance(this.jiraRestClient);
        this.bind(String[].class).toInstance(this.fields);
    }
}
