package Responses;

public record ErrorResponse(
        boolean success,
        String message
) {
}
