package Server.protocol;

import java.util.Map;

public record Packet(
        String type,
        Map<String, String> headers,
        byte[] payload
) {
}
