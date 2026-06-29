package Server.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class PacketWriter {

    public static void writePacket(DataOutputStream output, String type, Map<String, String> headers, byte[] payload)
            throws IOException {
        output.writeUTF(type);
        output.writeInt(headers.size());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            output.writeUTF(entry.getKey());
            output.writeUTF(entry.getValue());
        }
        output.writeInt(payload.length);
        output.write(payload);
        output.flush();
    }

    public static void sendSuccess(DataOutputStream output, String message) throws IOException {
        writePacket(output, MessageType.SUCCESS, Map.of("message", message), new byte[0]);
    }

    public static void sendError(DataOutputStream output, String message) throws IOException {
        writePacket(output, MessageType.ERROR, Map.of("message", message), new byte[0]);
    }

    public static void sendFile(DataOutputStream output, byte[] data) throws IOException {
        writePacket(output, MessageType.FILE_DATA, Map.of(), data);
    }

    public static void sendAuthError(DataOutputStream out) throws IOException {
        writePacket(out, MessageType.ERROR, Map.of("message", "Token inválido ou expirado"), new byte[0]);
    }
}
