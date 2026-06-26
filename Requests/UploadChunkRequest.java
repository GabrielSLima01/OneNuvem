package Requests;

public record UploadChunkRequest(
        String uploadId,
        int chunkIndex,
        int totalChunks,
        String base64Data
) {
}
