package Controllers;

import Common.http.HttpRequest;
import Common.http.HttpResponse;
import Common.http.JsonUtil;
import DTOs.AuthDTO;
import DTOs.UserDTO;
import Requests.LoginRequest;
import Requests.RegisterRequest;
import Responses.ApiResponse;
import Services.AuthService;
import Services.UserService;

public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    public HttpResponse register(HttpRequest request) {
        RegisterRequest payload = JsonUtil.fromJson(request.body(), RegisterRequest.class);
        UserDTO user = userService.register(payload);
        return HttpResponse.json(201, new ApiResponse<>(true, user, "Cadastro realizado com sucesso"));
    }

    public HttpResponse login(HttpRequest request) {
        LoginRequest payload = JsonUtil.fromJson(request.body(), LoginRequest.class);
        AuthDTO auth = authService.login(payload);
        return HttpResponse.json(200, new ApiResponse<>(true, auth, "Login realizado com sucesso"));
    }
}
