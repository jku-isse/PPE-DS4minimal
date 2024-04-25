package at.jku.isse.designspace.jama.connector.restclient.httpconnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import at.jku.isse.designspace.jama.connector.restclient.exception.JamaApiException;
import at.jku.isse.designspace.jama.connector.restclient.exception.RestClientException;

public class ApacheHttpClient implements HttpClient {
    private final Logger log = Logger.getLogger(ApacheHttpClient.class.getName());

    private org.apache.http.impl.client.CloseableHttpClient client;

    public ApacheHttpClient() throws RestClientException {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(10);
        try
        {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                            return true;
                        }
                    })
                    .build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE);
            SocketConfig socketConfig = SocketConfig.custom()
                    .setSoTimeout(20000)
                    .build();
            connectionManager.setDefaultSocketConfig(socketConfig);
            client = HttpClients
                    .custom()
                    .disableAuthCaching()
                    .setConnectionTimeToLive(20, TimeUnit.SECONDS)
                    .disableCookieManagement()
                    .disableRedirectHandling()
                    .setSSLSocketFactory(sslsf)
                    .setSSLContext(sslContext)
                    .build();
        }
        catch (Exception e)
        {
            throw new RestClientException(e);
        }
    }

    private Header getAuthenticationHeader(UsernamePasswordCredentials credentials, HttpRequest request) throws RestClientException {
        try {
            return new BasicScheme().authenticate(credentials, request, null);
        } catch(AuthenticationException e) {
            log.log(Level.SEVERE, e.toString(), e);
            throw new RestClientException("Unable to create authentication header");
        }
    }

    private String getEntityContentOrNull(HttpEntity responseEntity) {
        try {
//            return (new BufferedReader(new InputStreamReader(responseEntity.getContent()))).readLine();
            return (new BufferedReader(new InputStreamReader(responseEntity.getContent(), "UTF-8"))).readLine();
        } catch(IOException | NullPointerException e) {
            return null;
        }
    }


    private Response execute(HttpRequestBase request, UsernamePasswordCredentials credentials) throws RestClientException {
        HttpEntity responseEntity = null;
        try {
            request.addHeader(getAuthenticationHeader(credentials, request));
            HttpResponse rawResponse = client.execute(request);
            responseEntity = rawResponse.getEntity();
            return new Response(rawResponse.getStatusLine().getStatusCode(),
                    getEntityContentOrNull(rawResponse.getEntity()));
        } catch(IOException e) {
            throw new RestClientException(e);
        } finally {
            try {
                EntityUtils.consume(responseEntity);
            } catch(IOException e) {
                // Not throwing here because it's clean up
                log.log(Level.SEVERE, e.toString(), e);
            }
        }
    }
    
    @Override
	public Response get(String url, String username, String password, String apiKey) throws RestClientException {
//        System.out.println("GET: " + url);
        HttpGet getRequest = new HttpGet(url);
        if(apiKey != null)
            getRequest.setHeader("api-key", apiKey);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        Response response = execute(getRequest, credentials);
        if (response.getStatusCode() >= 400) {
            throw new JamaApiException(response.getStatusCode(), response.getResponse() + "\nURL: " + url);
        }
        return response;
    }

    @Override
	public Response delete(String url, String username, String password, String apiKey) throws RestClientException {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        HttpDelete deleteRequest = new HttpDelete(url);
        if(apiKey != null)
            deleteRequest.setHeader("api-key", apiKey);
        Response response = execute(deleteRequest, credentials);
        if (response.getStatusCode() >= 400) {
            throw new JamaApiException(response.getStatusCode(), response.getResponse() + "\nURL: " + url);
        }
        return response;
    }

    @Override
	public Response post(String url, String username, String password, String apiKey, String payload) throws RestClientException {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        HttpPost postRequest = new HttpPost(url);
        if(apiKey != null)
            postRequest.setHeader("api-key", apiKey);
        StringEntity body = new StringEntity(payload, "UTF-8");
        body.setContentType("application/json");
        postRequest.setEntity(body);
        Response response = execute(postRequest, credentials);
        if(response.getStatusCode() >= 400) {
            throw new JamaApiException(response.getStatusCode(), response.getResponse() + "\nURL: " + url + "\nPayload: " + payload);
        }
        return response;
    }

    @Override
	public Response put(String url, String username, String password, String apiKey, String payload) throws RestClientException {
        System.out.println("PUT: " + url);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        HttpPut putRequest = new HttpPut(url);
        if(apiKey != null)
            putRequest.setHeader("api-key", apiKey);
        StringEntity body = new StringEntity(payload, "UTF-8");
        body.setContentType("application/json");
        putRequest.setEntity(body);
        Response response = execute(putRequest, credentials);
        if(response.getStatusCode() >= 400) {
            throw new JamaApiException(response.getStatusCode(), response.getResponse() + "\nURL: " + url + "\nPayload: " + payload);
        }
        return response;
    }


    @Override
	public Response putFile(String url, String username, String password, String apiKey, File file) throws RestClientException {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        HttpPut putRequest = new HttpPut(url);
        if(apiKey != null)
            putRequest.setHeader("api-key", apiKey);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("file", file);
        multipartEntityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
        HttpEntity fileEntity = multipartEntityBuilder.build();
        putRequest.setEntity(fileEntity);
        Response response = execute(putRequest, credentials);
        if(response.getStatusCode() >= 400) {
            throw new JamaApiException(response.getStatusCode(), response.getResponse() + "\nURL: " + url + ", Filename: " + file.getName());
        }
        return response;
    }

    @Override
	public FileResponse getFile(String url, String username, String password, String apiKey) throws RestClientException {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        HttpEntity responseEntity = null;
        HttpGet request = new HttpGet(url);
        if(apiKey != null)
            request.setHeader("api-key", apiKey);
        try {
            request.addHeader(getAuthenticationHeader(credentials, request));
            HttpResponse rawResponse = client.execute(request);
            responseEntity = rawResponse.getEntity();
            return new FileResponse(rawResponse.getStatusLine().getStatusCode(),
                    rawResponse.getEntity().getContent());
        } catch (IOException e) {
            throw new RestClientException(e);
        } finally {
            try {
                EntityUtils.consume(responseEntity);
            } catch (IOException e) {
                // Not throwing here because it's clean up
                log.log(Level.SEVERE, e.toString(), e);
            }
        }
    }
}
