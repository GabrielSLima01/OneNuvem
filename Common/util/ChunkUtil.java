package Common.util;

public final class ChunkUtil {

    private ChunkUtil() {
    }

    public static String safeStoredName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
