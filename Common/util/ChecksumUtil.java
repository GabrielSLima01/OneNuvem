package Common.util;

import java.security.MessageDigest;

public final class ChecksumUtil {

    private ChecksumUtil() {
    }

    public static String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder builder = new StringBuilder();
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao gerar checksum", exception);
        }
    }
}
