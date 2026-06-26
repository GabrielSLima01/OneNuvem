package Responses;

public record UploadChunkResponse(
        String uploadId,
        int chunkIndex,
        int uploadedChunks,
        int totalChunks,
        String status,
        int queueSize
) {
}
