package at.jku.isse.designspace.jira.restclient.connector;

import java.net.URI;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;

public class HttpClientFactory {

	private static boolean isInitialized = false;
	private static BasicHttpAuthenticationHandler authenticationHandler;
	private static URI uri;
	
	public static void init(String baseUri, String username, String pw) {
		uri =  URI.create(baseUri);

		authenticationHandler = new BasicHttpAuthenticationHandler(username, pw);	
		isInitialized = true;
	}
	
	public static HttpClient createHttpClient() {	
		if(!isInitialized) throw new RuntimeException();
		AsynchronousHttpClientFactory clientFactory = new AsynchronousHttpClientFactory();
		return clientFactory.createClient(uri, authenticationHandler);
	}

}
