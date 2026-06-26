package DTOs;

public record AuthDTO(
        String token,
        UserDTO user
) {
}
