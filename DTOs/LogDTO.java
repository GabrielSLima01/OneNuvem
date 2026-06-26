package DTOs;

public record LogDTO(
        String id,
        String level,
        String action,
        String message,
        String detailsJson,
        String createdAt
) {
}
