package DTOs;

public record FileActivityDTO(
        String fileName,
        String action,
        String createdAt
) {
}
