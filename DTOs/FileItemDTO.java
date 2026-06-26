package DTOs;

public record FileItemDTO(
        String id,
        String originalName,
        String mimeType,
        long sizeBytes,
        int totalChunks,
        String checksum,
        String status,
        String createdAt,
        String updatedAt
) {
}
