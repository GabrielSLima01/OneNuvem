package Common.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private final String method;
    private final String path;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, String> queryParams;

    public HttpRequest(
            String method,
            String path,
            String httpVersion,
            Map<String, String> headers,
            String body,
            Map<String, String> queryParams
    ) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.body = body;
        this.queryParams = Collections.unmodifiableMap(new HashMap<>(queryParams));
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String httpVersion() {
        return httpVersion;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    public Map<String, String> queryParams() {
        return queryParams;
    }

    public String header(String name) {
        return headers.getOrDefault(name.toLowerCase(), "");
    }
}
