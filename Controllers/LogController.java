package Controllers;

import Common.http.HttpResponse;
import DTOs.LogDTO;
import Responses.ApiResponse;
import Services.FileService;
import java.util.List;

public class LogController {

    private final FileService fileService;

    public LogController(FileService fileService) {
        this.fileService = fileService;
    }

    public HttpResponse list(String userId) {
        List<LogDTO> logs = fileService.logs(userId);
        return HttpResponse.json(200, new ApiResponse<>(true, logs, "Logs carregados"));
    }
}
