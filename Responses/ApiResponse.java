package Responses;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {
}
