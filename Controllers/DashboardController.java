package Controllers;

import Common.http.HttpResponse;
import DTOs.DashboardDTO;
import Responses.ApiResponse;
import Services.FileService;
import Services.UserService;

public class DashboardController {

    private final FileService fileService;
    private final UserService userService;

    public DashboardController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    public HttpResponse dashboard(String userId) {
        var user = userService.findById(userId);
        DashboardDTO dashboard = fileService.dashboard(userId, user.quotaBytes());
        return HttpResponse.json(200, new ApiResponse<>(true, dashboard, "Dashboard carregado"));
    }
}
