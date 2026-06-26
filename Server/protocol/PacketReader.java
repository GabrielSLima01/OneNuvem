package Server.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketReader {

    public static Packet readPacket(DataInputStream input) throws IOException {
        String type = input.readUTF();
        int headerCount = input.readInt();
        Map<String, String> headers = new HashMap<>();
        for (int index = 0; index < headerCount; index++) {
            headers.put(input.readUTF(), input.readUTF());
        }
        int payloadSize = input.readInt();
        byte[] payload = input.readNBytes(payloadSize);
        return new Packet(type, headers, payload);
    }
}
