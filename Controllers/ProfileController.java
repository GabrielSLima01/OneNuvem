package Controllers;

import Common.http.HttpResponse;
import DTOs.UserDTO;
import Responses.ApiResponse;
import Services.UserService;

public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    public HttpResponse profile(String userId) {
        UserDTO user = userService.findById(userId);
        return HttpResponse.json(200, new ApiResponse<>(true, user, "Perfil carregado"));
    }
}
