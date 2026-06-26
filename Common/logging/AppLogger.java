package Common.logging;

import Common.db.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class AppLogger {

    public void info(String action, String message, String detailsJson, String userId) {
        log("INFO", action, message, detailsJson, userId);
    }

    public void error(String action, String message, String detailsJson, String userId) {
        log("ERROR", action, message, detailsJson, userId);
    }

    public void log(String level, String action, String message, String detailsJson, String userId) {
        System.out.printf("[%s] %s - %s%n", level, action, message);

        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO logs (id, user_id, level, action, message, details_json)
                     VALUES (?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setObject(1, UUID.randomUUID());
            if (userId == null) {
                statement.setObject(2, null);
            } else {
                statement.setObject(2, UUID.fromString(userId));
            }
            statement.setString(3, level);
            statement.setString(4, action);
            statement.setString(5, message);
            statement.setString(6, detailsJson == null ? "{}" : detailsJson);
            statement.executeUpdate();
        } catch (Exception ignored) {
            System.err.println("Falha ao persistir log: " + ignored.getMessage());
        }
    }
}
