package Common.db;

import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = ConnectionFactory.open();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id UUID PRIMARY KEY,
                        full_name VARCHAR(160) NOT NULL,
                        email VARCHAR(160) NOT NULL UNIQUE,
                        password_hash VARCHAR(120) NOT NULL,
                        quota_bytes BIGINT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    );
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS upload_sessions (
                        id UUID PRIMARY KEY,
                        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        original_name VARCHAR(255) NOT NULL,
                        stored_name VARCHAR(255) NOT NULL,
                        mime_type VARCHAR(120) NOT NULL,
                        size_bytes BIGINT NOT NULL,
                        total_chunks INTEGER NOT NULL,
                        uploaded_chunks INTEGER NOT NULL DEFAULT 0,
                        status VARCHAR(32) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    );
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS files (
                        id UUID PRIMARY KEY,
                        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        original_name VARCHAR(255) NOT NULL,
                        stored_name VARCHAR(255) NOT NULL,
                        mime_type VARCHAR(120) NOT NULL,
                        size_bytes BIGINT NOT NULL,
                        total_chunks INTEGER NOT NULL,
                        checksum VARCHAR(128) NOT NULL,
                        status VARCHAR(32) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    );
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS file_chunks (
                        id UUID PRIMARY KEY,
                        file_id UUID NOT NULL REFERENCES files(id) ON DELETE CASCADE,
                        chunk_index INTEGER NOT NULL,
                        checksum VARCHAR(128) NOT NULL,
                        size_bytes BIGINT NOT NULL,
                        primary_node VARCHAR(120) NOT NULL,
                        replica_node VARCHAR(120) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE(file_id, chunk_index)
                    );
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS logs (
                        id UUID PRIMARY KEY,
                        user_id UUID NULL,
                        level VARCHAR(16) NOT NULL,
                        action VARCHAR(120) NOT NULL,
                        message TEXT NOT NULL,
                        details_json TEXT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    );
                    """);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao inicializar o banco de dados", exception);
        }
    }
}
