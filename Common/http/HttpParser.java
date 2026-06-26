package Common.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class HttpParser {

    private HttpParser() {
    }

    public static HttpRequest parse(BufferedInputStream input) throws IOException {
        String startLine = readLine(input);
        if (startLine == null || startLine.isBlank()) {
            throw new IOException("Request vazia");
        }

        String[] firstLineParts = startLine.split(" ");
        if (firstLineParts.length < 3) {
            throw new IOException("Request invalida");
        }

        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = readLine(input)) != null && !headerLine.isEmpty()) {
            int separator = headerLine.indexOf(':');
            if (separator > 0) {
                headers.put(
                        headerLine.substring(0, separator).trim().toLowerCase(),
                        headerLine.substring(separator + 1).trim()
                );
            }
        }

        int contentLength = Integer.parseInt(headers.getOrDefault("content-length", "0"));
        byte[] bodyBytes = input.readNBytes(contentLength);
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        String rawTarget = firstLineParts[1];
        String path = rawTarget;
        Map<String, String> query = new HashMap<>();
        int queryIndex = rawTarget.indexOf('?');
        if (queryIndex >= 0) {
            path = rawTarget.substring(0, queryIndex);
            String queryString = rawTarget.substring(queryIndex + 1);
            for (String pair : queryString.split("&")) {
                if (pair.isBlank()) {
                    continue;
                }
                String[] parts = pair.split("=", 2);
                query.put(parts[0], parts.length > 1 ? parts[1] : "");
            }
        }

        return new HttpRequest(firstLineParts[0], path, firstLineParts[2], headers, body, query);
    }

    private static String readLine(BufferedInputStream input) throws IOException {
        StringBuilder line = new StringBuilder();
        int previous = -1;
        while (true) {
            int current = input.read();
            if (current == -1) {
                if (line.isEmpty()) {
                    return null;
                }
                break;
            }
            if (previous == '\r' && current == '\n') {
                line.setLength(line.length() - 1);
                break;
            }
            line.append((char) current);
            previous = current;
        }
        return line.toString();
    }
}
