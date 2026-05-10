package Mensageria;

public class MensageriaHttpResponse {

    private final int statusCode;
    private final String responseBody;

    public MensageriaHttpResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public boolean isSucesso() {
        return (statusCode >= 200 && statusCode < 300) || statusCode == 409;
    }
}
