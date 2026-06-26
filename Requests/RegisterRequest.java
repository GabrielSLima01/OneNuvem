package Requests;

public record RegisterRequest(
        String fullName,
        String email,
        String password
) {
}
