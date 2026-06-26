package DTOs;

public record UserDTO(
        String id,
        String fullName,
        String email,
        long quotaBytes,
        String createdAt
) {
}
