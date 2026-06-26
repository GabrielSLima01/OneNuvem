package Services;

import Common.auth.PasswordService;
import Common.db.ConnectionFactory;
import Common.logging.AppLogger;
import DTOs.UserDTO;
import Requests.RegisterRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

public class UserService {

    private final PasswordService passwordService;
    private final AppLogger logger;

    public UserService(PasswordService passwordService, AppLogger logger) {
        this.passwordService = passwordService;
        this.logger = logger;
    }

    public UserDTO register(RegisterRequest request) {
        validateRegisterRequest(request);

        try (Connection connection = ConnectionFactory.open()) {
            if (existsByEmail(connection, request.email())) {
                throw new IllegalArgumentException("Email ja cadastrado");
            }

            UUID userId = UUID.randomUUID();
            String passwordHash = passwordService.hash(request.password());

            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO users (id, full_name, email, password_hash, quota_bytes)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                statement.setObject(1, userId);
                statement.setString(2, request.fullName().trim());
                statement.setString(3, request.email().trim().toLowerCase());
                statement.setString(4, passwordHash);
                statement.setLong(5, 1024L * 1024L * 1024L);
                statement.executeUpdate();
            }

            UserDTO user = findById(userId.toString());
            logger.info("register", "Usuario cadastrado", "{\"email\":\"" + user.email() + "\"}", user.id());
            return user;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao cadastrar usuario", exception);
        }
    }

    public UserDTO findById(String id) {
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id, full_name, email, quota_bytes, created_at
                     FROM users
                     WHERE id = ?
                     """)) {
            statement.setObject(1, UUID.fromString(id));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Usuario nao encontrado");
                }
                return toDto(resultSet);
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao buscar usuario", exception);
        }
    }

    public UserRecord findByEmail(String email) {
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id, full_name, email, password_hash, quota_bytes, created_at
                     FROM users
                     WHERE email = ?
                     """)) {
            statement.setString(1, email.trim().toLowerCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Credenciais invalidas");
                }
                return new UserRecord(
                        resultSet.getObject("id", UUID.class).toString(),
                        resultSet.getString("full_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getLong("quota_bytes"),
                        resultSet.getTimestamp("created_at")
                );
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao buscar usuario por email", exception);
        }
    }

    private boolean existsByEmail(Connection connection, String email) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM users WHERE email = ?")) {
            statement.setString(1, email.trim().toLowerCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || request.fullName() == null || request.email() == null || request.password() == null) {
            throw new IllegalArgumentException("Dados de cadastro invalidos");
        }
        if (request.fullName().trim().length() < 3) {
            throw new IllegalArgumentException("Nome deve ter ao menos 3 caracteres");
        }
        if (!request.email().contains("@")) {
            throw new IllegalArgumentException("Email invalido");
        }
        if (request.password().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter ao menos 6 caracteres");
        }
    }

    private UserDTO toDto(ResultSet resultSet) throws Exception {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new UserDTO(
                resultSet.getObject("id", UUID.class).toString(),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getLong("quota_bytes"),
                createdAt.toInstant().toString()
        );
    }

    public record UserRecord(
            String id,
            String fullName,
            String email,
            String passwordHash,
            long quotaBytes,
            Timestamp createdAt
    ) {
    }
}
