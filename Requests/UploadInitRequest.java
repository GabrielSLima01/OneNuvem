package Requests;

public record UploadInitRequest(
        String fileName,
        String mimeType,
        long sizeBytes,
        int totalChunks
) {
}
