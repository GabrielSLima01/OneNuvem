package Common.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    private final int statusCode;
    private final String contentType;
    private final byte[] body;
    private final Map<String, String> headers;

    private HttpResponse(int statusCode, String contentType, byte[] body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
        this.headers = headers;
    }

    public static HttpResponse json(int statusCode, Object payload) {
        byte[] responseBody = JsonUtil.toJsonBytes(payload);
        Map<String, String> headers = defaultHeaders(responseBody.length, "application/json; charset=utf-8");
        return new HttpResponse(statusCode, "application/json; charset=utf-8", responseBody, headers);
    }

    public static HttpResponse text(int statusCode, String body) {
        byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
        Map<String, String> headers = defaultHeaders(responseBody.length, "text/plain; charset=utf-8");
        return new HttpResponse(statusCode, "text/plain; charset=utf-8", responseBody, headers);
    }

    public static HttpResponse binary(int statusCode, byte[] body, String contentType, String fileName) {
        Map<String, String> headers = defaultHeaders(body.length, contentType);
        headers.put("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        return new HttpResponse(statusCode, contentType, body, headers);
    }

    private static Map<String, String> defaultHeaders(int length, String contentType) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Content-Length", String.valueOf(length));
        headers.put("Connection", "close");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        return headers;
    }

    public byte[] toBytes() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(reasonPhrase(statusCode)).append("\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        response.append("\r\n");
        byte[] prefix = response.toString().getBytes(StandardCharsets.UTF_8);
        byte[] full = new byte[prefix.length + body.length];
        System.arraycopy(prefix, 0, full, 0, prefix.length);
        System.arraycopy(body, 0, full, prefix.length, body.length);
        return full;
    }

    private static String reasonPhrase(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 413 -> "Payload Too Large";
            case 422 -> "Unprocessable Entity";
            case 500 -> "Internal Server Error";
            default -> "OK";
        };
    }
}
