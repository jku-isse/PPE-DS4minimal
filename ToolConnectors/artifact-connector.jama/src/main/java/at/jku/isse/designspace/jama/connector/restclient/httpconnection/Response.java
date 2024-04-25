package at.jku.isse.designspace.jama.connector.restclient.httpconnection;

public class Response {
    private int statusCode;
    private String response;

    public Response(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }
}
