package Middleware;

import Common.auth.JwtService;
import Common.auth.PasswordService;
import Common.http.HttpRequest;
import Common.http.HttpResponse;
import Common.logging.AppLogger;
import Controllers.AuthController;
import Controllers.DashboardController;
import Controllers.FileController;
import Controllers.LogController;
import Controllers.ProfileController;
import Responses.ErrorResponse;
import Services.AuthService;
import Services.FileService;
import Services.NodeSocketService;
import Services.UploadQueueService;
import Services.UserService;
import com.auth0.jwt.interfaces.DecodedJWT;

public class Middleware {

    private final JwtService jwtService;
    private final AuthController authController;
    private final FileController fileController;
    private final ProfileController profileController;
    private final DashboardController dashboardController;
    private final LogController logController;

    public Middleware(NodeManager nodeManager) {
        AppLogger logger = new AppLogger();
        PasswordService passwordService = new PasswordService();
        JwtService jwt = new JwtService();
        NodeSocketService nodeSocketService = new NodeSocketService();
        FileService fileService = new FileService(logger, nodeManager, nodeSocketService);
        UserService userService = new UserService(passwordService, logger);
        AuthService authService = new AuthService(userService, passwordService, jwt, logger);
        UploadQueueService uploadQueueService = new UploadQueueService(nodeManager, nodeSocketService, fileService, logger);

        this.jwtService = jwt;
        this.authController = new AuthController(userService, authService);
        this.fileController = new FileController(fileService, uploadQueueService);
        this.profileController = new ProfileController(userService);
        this.dashboardController = new DashboardController(fileService, userService);
        this.logController = new LogController(fileService);
    }

    public HttpResponse handle(HttpRequest request) {
        try {
            if ("OPTIONS".equalsIgnoreCase(request.method())) {
                return HttpResponse.text(204, "");
            }

            if ("/api/auth/register".equals(request.path()) && "POST".equalsIgnoreCase(request.method())) {
                return authController.register(request);
            }
            if ("/api/auth/login".equals(request.path()) && "POST".equalsIgnoreCase(request.method())) {
                return authController.login(request);
            }

            String userId = requireUserId(request);

            if ("/api/profile".equals(request.path()) && "GET".equalsIgnoreCase(request.method())) {
                return profileController.profile(userId);
            }
            if ("/api/dashboard".equals(request.path()) && "GET".equalsIgnoreCase(request.method())) {
                return dashboardController.dashboard(userId);
            }
            if ("/api/logs".equals(request.path()) && "GET".equalsIgnoreCase(request.method())) {
                return logController.list(userId);
            }
            if ("/api/files".equals(request.path()) && "GET".equalsIgnoreCase(request.method())) {
                return fileController.listFiles(userId);
            }
            if ("/api/files/upload/init".equals(request.path()) && "POST".equalsIgnoreCase(request.method())) {
                return fileController.initUpload(request, userId);
            }
            if ("/api/files/upload/chunk".equals(request.path()) && "POST".equalsIgnoreCase(request.method())) {
                return fileController.uploadChunk(request, userId);
            }
            if ("/api/files/upload/complete".equals(request.path()) && "POST".equalsIgnoreCase(request.method())) {
                return fileController.completeUpload(request, userId);
            }
            if (request.path().matches("^/api/files/[^/]+$") && "GET".equalsIgnoreCase(request.method())) {
                String fileId = request.path().substring("/api/files/".length());
                return fileController.fileDetails(userId, fileId);
            }
            if (request.path().matches("^/api/files/[^/]+/download$") && "GET".equalsIgnoreCase(request.method())) {
                String fileId = request.path()
                        .substring("/api/files/".length(), request.path().length() - "/download".length());
                return fileController.download(userId, fileId);
            }

            return HttpResponse.json(404, new ErrorResponse(false, "Rota nao encontrada"));
        } catch (IllegalArgumentException exception) {
            return HttpResponse.json(400, new ErrorResponse(false, exception.getMessage()));
        } catch (SecurityException exception) {
            return HttpResponse.json(401, new ErrorResponse(false, exception.getMessage()));
        } catch (Exception exception) {
            return HttpResponse.json(500, new ErrorResponse(false, "Erro interno do servidor"));
        }
    }

    private String requireUserId(HttpRequest request) {
        String header = request.header("authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("Token ausente");
        }
        String token = header.substring("Bearer ".length());
        DecodedJWT decoded = jwtService.verify(token);
        return decoded.getSubject();
    }
}
