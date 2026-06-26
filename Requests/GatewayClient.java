package Requests;

import Common.http.JsonUtil;
import DTOs.AuthDTO;
import DTOs.DashboardDTO;
import DTOs.FileItemDTO;
import DTOs.LogDTO;
import DTOs.UploadSessionDTO;
import Responses.ApiResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GatewayClient {

    private final String host;
    private final int port;
    private String token;

    public GatewayClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public AuthDTO login(LoginRequest request) {
        String body = "{\"email\":\"" + request.email() + "\",\"password\":\"" + request.password() + "\"}";
        ApiResponse<?> response = request("POST", "/api/auth/login", body);
        return JsonUtil.fromJson(JsonUtilString.valueOf(response.data()), AuthDTO.class);
    }

    public UploadSessionDTO initUpload(String jsonBody) {
        ApiResponse<?> response = request("POST", "/api/files/upload/init", jsonBody);
        return JsonUtil.fromJson(JsonUtilString.valueOf(response.data()), UploadSessionDTO.class);
    }

    public List<FileItemDTO> listFiles() {
        String response = rawRequest("GET", "/api/files", "");
        return List.of();
    }

    public DashboardDTO dashboard() {
        String response = rawRequest("GET", "/api/dashboard", "");
        return JsonUtil.fromJson(response, DashboardDTO.class);
    }

    public List<LogDTO> logs() {
        rawRequest("GET", "/api/logs", "");
        return List.of();
    }

    private ApiResponse<?> request(String method, String path, String body) {
        String response = rawRequest(method, path, body);
        return JsonUtil.fromJson(response.substring(response.indexOf("\r\n\r\n") + 4), ApiResponse.class);
    }

    private String rawRequest(String method, String path, String body) {
        try (Socket socket = new Socket(host, port);
             OutputStream output = socket.getOutputStream();
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            StringBuilder request = new StringBuilder()
                    .append(method).append(" ").append(path).append(" HTTP/1.1\r\n")
                    .append("Host: ").append(host).append("\r\n")
                    .append("Content-Type: application/json\r\n")
                    .append("Content-Length: ").append(bytes.length).append("\r\n");
            if (token != null) {
                request.append("Authorization: Bearer ").append(token).append("\r\n");
            }
            request.append("\r\n").append(body);
            output.write(request.toString().getBytes(StandardCharsets.UTF_8));
            output.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao chamar gateway", exception);
        }
    }

    private static final class JsonUtilString {
        private JsonUtilString() {
        }

        static String valueOf(Object value) {
            return new String(JsonUtil.toJsonBytes(value), StandardCharsets.UTF_8);
        }
    }
}
