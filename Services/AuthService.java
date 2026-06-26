package Services;

import Common.auth.JwtService;
import Common.auth.PasswordService;
import Common.logging.AppLogger;
import DTOs.AuthDTO;
import DTOs.UserDTO;
import Requests.LoginRequest;

public class AuthService {

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final AppLogger logger;

    public AuthService(UserService userService, PasswordService passwordService, JwtService jwtService, AppLogger logger) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.logger = logger;
    }

    public AuthDTO login(LoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            throw new IllegalArgumentException("Credenciais invalidas");
        }

        UserService.UserRecord user = userService.findByEmail(request.email());
        if (!passwordService.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Credenciais invalidas");
        }

        UserDTO dto = new UserDTO(
                user.id(),
                user.fullName(),
                user.email(),
                user.quotaBytes(),
                user.createdAt().toInstant().toString()
        );
        String token = jwtService.generateToken(dto);
        logger.info("login", "Login realizado", "{\"email\":\"" + dto.email() + "\"}", dto.id());
        return new AuthDTO(token, dto);
    }
}
