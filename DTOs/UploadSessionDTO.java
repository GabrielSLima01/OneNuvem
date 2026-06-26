package DTOs;

public record UploadSessionDTO(
        String uploadId,
        String originalName,
        String status,
        int totalChunks,
        int uploadedChunks,
        int chunkSize,
        long sizeBytes
) {
}
