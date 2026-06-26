package Services;

import Common.config.AppConfig;
import Common.db.ConnectionFactory;
import Common.logging.AppLogger;
import Common.util.ChecksumUtil;
import Common.util.ChunkUtil;
import DTOs.DashboardDTO;
import DTOs.FileActivityDTO;
import DTOs.FileChunkDTO;
import DTOs.FileItemDTO;
import DTOs.LogDTO;
import DTOs.UploadSessionDTO;
import Middleware.Node;
import Middleware.NodeManager;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileService {

    private final AppLogger logger;
    private final NodeManager nodeManager;
    private final NodeSocketService nodeSocketService;

    public FileService(AppLogger logger, NodeManager nodeManager, NodeSocketService nodeSocketService) {
        this.logger = logger;
        this.nodeManager = nodeManager;
        this.nodeSocketService = nodeSocketService;
    }

    public UploadSessionDTO createUploadSession(String userId, String fileName, String mimeType, long sizeBytes, int totalChunks) {
        if (sizeBytes <= 0 || sizeBytes > AppConfig.maxUploadBytes()) {
            throw new IllegalArgumentException("Arquivo fora do limite permitido");
        }
        validateQuota(userId, sizeBytes);

        String uploadId = UUID.randomUUID().toString();
        String storedName = ChunkUtil.safeStoredName(uploadId + "_" + fileName);

        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO upload_sessions (id, user_id, original_name, stored_name, mime_type, size_bytes, total_chunks, status)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setObject(1, UUID.fromString(uploadId));
            statement.setObject(2, UUID.fromString(userId));
            statement.setString(3, fileName);
            statement.setString(4, storedName);
            statement.setString(5, mimeType == null ? "application/octet-stream" : mimeType);
            statement.setLong(6, sizeBytes);
            statement.setInt(7, totalChunks);
            statement.setString(8, "QUEUED");
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao iniciar upload", exception);
        }

        logger.info("upload_init", "Upload iniciado", "{\"fileName\":\"" + fileName + "\"}", userId);
        return new UploadSessionDTO(uploadId, fileName, "QUEUED", totalChunks, 0, AppConfig.uploadChunkSize(), sizeBytes);
    }

    public UploadQueueService.UploadChunkTaskResult recordUploadedChunk(
            String userId,
            String uploadId,
            int chunkIndex,
            int totalChunks,
            String checksum,
            long sizeBytes,
            String primaryNode,
            String replicaNode
    ) {
        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try (PreparedStatement upsertChunk = connection.prepareStatement("""
                    INSERT INTO file_chunks (id, file_id, chunk_index, checksum, size_bytes, primary_node, replica_node)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (file_id, chunk_index)
                    DO UPDATE SET checksum = EXCLUDED.checksum,
                                  size_bytes = EXCLUDED.size_bytes,
                                  primary_node = EXCLUDED.primary_node,
                                  replica_node = EXCLUDED.replica_node
                    """)) {
                upsertChunk.setObject(1, UUID.randomUUID());
                upsertChunk.setObject(2, UUID.fromString(uploadId));
                upsertChunk.setInt(3, chunkIndex);
                upsertChunk.setString(4, checksum);
                upsertChunk.setLong(5, sizeBytes);
                upsertChunk.setString(6, primaryNode);
                upsertChunk.setString(7, replicaNode);
                upsertChunk.executeUpdate();
            }

            int uploadedChunks = 0;
            try (PreparedStatement countChunks = connection.prepareStatement("""
                    SELECT COUNT(*) AS total
                    FROM file_chunks
                    WHERE file_id = ?
                    """)) {
                countChunks.setObject(1, UUID.fromString(uploadId));
                try (ResultSet resultSet = countChunks.executeQuery()) {
                    if (resultSet.next()) {
                        uploadedChunks = resultSet.getInt("total");
                    }
                }
            }

            try (PreparedStatement updateSession = connection.prepareStatement("""
                    UPDATE upload_sessions
                    SET uploaded_chunks = ?, status = ?, updated_at = ?
                    WHERE id = ? AND user_id = ?
                    """)) {
                updateSession.setInt(1, uploadedChunks);
                updateSession.setString(2, uploadedChunks >= totalChunks ? "UPLOADED" : "PROCESSING");
                updateSession.setTimestamp(3, Timestamp.from(Instant.now()));
                updateSession.setObject(4, UUID.fromString(uploadId));
                updateSession.setObject(5, UUID.fromString(userId));
                updateSession.executeUpdate();
            }

            connection.commit();
            String status = uploadedChunks >= totalChunks ? "UPLOADED" : "PROCESSING";
            return new UploadQueueService.UploadChunkTaskResult(uploadId, chunkIndex, uploadedChunks, totalChunks, status, 0);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao registrar chunk", exception);
        }
    }

    public void completeUpload(String userId, String uploadId) {
        try (Connection connection = ConnectionFactory.open()) {
            UploadSessionData session = findUploadSession(connection, userId, uploadId);
            List<FileChunkDTO> chunks = listChunks(connection, uploadId);
            if (chunks.size() != session.totalChunks()) {
                throw new IllegalArgumentException("Upload incompleto");
            }

            String checksum = calculateFileChecksum(uploadId);
            try (PreparedStatement insertFile = connection.prepareStatement("""
                    INSERT INTO files (id, user_id, original_name, stored_name, mime_type, size_bytes, total_chunks, checksum, status, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (id)
                    DO UPDATE SET checksum = EXCLUDED.checksum,
                                  status = EXCLUDED.status,
                                  updated_at = EXCLUDED.updated_at
                    """)) {
                insertFile.setObject(1, UUID.fromString(uploadId));
                insertFile.setObject(2, UUID.fromString(userId));
                insertFile.setString(3, session.originalName());
                insertFile.setString(4, session.storedName());
                insertFile.setString(5, session.mimeType());
                insertFile.setLong(6, session.sizeBytes());
                insertFile.setInt(7, session.totalChunks());
                insertFile.setString(8, checksum);
                insertFile.setString(9, "AVAILABLE");
                insertFile.setTimestamp(10, session.createdAt());
                insertFile.setTimestamp(11, Timestamp.from(Instant.now()));
                insertFile.executeUpdate();
            }

            try (PreparedStatement updateSession = connection.prepareStatement("""
                    UPDATE upload_sessions SET status = ?, updated_at = ? WHERE id = ?
                    """)) {
                updateSession.setString(1, "COMPLETED");
                updateSession.setTimestamp(2, Timestamp.from(Instant.now()));
                updateSession.setObject(3, UUID.fromString(uploadId));
                updateSession.executeUpdate();
            }
            logger.info("upload_complete", "Upload concluido", "{\"uploadId\":\"" + uploadId + "\"}", userId);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao concluir upload", exception);
        }
    }

    public List<FileItemDTO> listFiles(String userId) {
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id, original_name, mime_type, size_bytes, total_chunks, checksum, status, created_at, updated_at
                     FROM files
                     WHERE user_id = ?
                     ORDER BY created_at DESC
                     """)) {
            statement.setObject(1, UUID.fromString(userId));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<FileItemDTO> files = new ArrayList<>();
                while (resultSet.next()) {
                    files.add(toFileItem(resultSet));
                }
                return files;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao listar arquivos", exception);
        }
    }

    public FileItemDTO getFile(String userId, String fileId) {
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id, original_name, mime_type, size_bytes, total_chunks, checksum, status, created_at, updated_at
                     FROM files
                     WHERE user_id = ? AND id = ?
                     """)) {
            statement.setObject(1, UUID.fromString(userId));
            statement.setObject(2, UUID.fromString(fileId));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Arquivo nao encontrado");
                }
                return toFileItem(resultSet);
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao buscar arquivo", exception);
        }
    }

    public List<FileChunkDTO> getFileChunks(String fileId) {
        try (Connection connection = ConnectionFactory.open()) {
            return listChunks(connection, fileId);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao listar chunks", exception);
        }
    }

    public byte[] downloadFile(String userId, String fileId) {
        FileItemDTO file = getFile(userId, fileId);
        List<FileChunkDTO> chunks = getFileChunks(fileId);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (FileChunkDTO chunk : chunks) {
                byte[] bytes = downloadChunkResilient(fileId, chunk);
                output.write(bytes);
            }
            logger.info("download", "Download concluido", "{\"fileId\":\"" + fileId + "\"}", userId);
            return output.toByteArray();
        } catch (Exception exception) {
            logger.error("download_error", exception.getMessage(), "{\"fileId\":\"" + fileId + "\"}", userId);
            throw new IllegalStateException("Falha no download de " + file.originalName(), exception);
        }
    }

    public DashboardDTO dashboard(String userId, long quotaBytes) {
        long usedBytes = 0;
        int totalFiles = 0;
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT COALESCE(SUM(size_bytes), 0) AS used_bytes, COUNT(*) AS total_files
                     FROM files
                     WHERE user_id = ?
                     """)) {
            statement.setObject(1, UUID.fromString(userId));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    usedBytes = resultSet.getLong("used_bytes");
                    totalFiles = resultSet.getInt("total_files");
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao montar dashboard", exception);
        }

        double usage = quotaBytes == 0 ? 0 : (usedBytes * 100.0) / quotaBytes;
        return new DashboardDTO(
                usedBytes,
                quotaBytes,
                totalFiles,
                Math.min(100.0, usage),
                findRecentActivities(userId, "upload_complete"),
                findRecentActivities(userId, "download")
        );
    }

    public List<LogDTO> logs(String userId) {
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id, level, action, message, details_json, created_at
                     FROM logs
                     WHERE user_id = ? OR user_id IS NULL
                     ORDER BY created_at DESC
                     LIMIT 100
                     """)) {
            statement.setObject(1, UUID.fromString(userId));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<LogDTO> items = new ArrayList<>();
                while (resultSet.next()) {
                    items.add(new LogDTO(
                            resultSet.getObject("id", UUID.class).toString(),
                            resultSet.getString("level"),
                            resultSet.getString("action"),
                            resultSet.getString("message"),
                            resultSet.getString("details_json"),
                            resultSet.getTimestamp("created_at").toInstant().toString()
                    ));
                }
                return items;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao listar logs", exception);
        }
    }

    private byte[] downloadChunkResilient(String fileId, FileChunkDTO chunk) {
        Node primary = nodeManager.findByName(chunk.primaryNode());
        Node replica = nodeManager.findByName(chunk.replicaNode());
        if (primary != null) {
            try {
                return nodeSocketService.readChunk(primary, fileId, chunk.chunkIndex());
            } catch (Exception ignored) {
                nodeManager.markAsFailed(primary);
            }
        }
        if (replica != null) {
            try {
                return nodeSocketService.readChunk(replica, fileId, chunk.chunkIndex());
            } catch (Exception ignored) {
                nodeManager.markAsFailed(replica);
            }
        }
        throw new IllegalStateException("Nenhuma replica disponivel para chunk " + chunk.chunkIndex());
    }

    private List<FileChunkDTO> listChunks(Connection connection, String fileId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT chunk_index, checksum, size_bytes, primary_node, replica_node
                FROM file_chunks
                WHERE file_id = ?
                ORDER BY chunk_index ASC
                """)) {
            statement.setObject(1, UUID.fromString(fileId));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<FileChunkDTO> chunks = new ArrayList<>();
                while (resultSet.next()) {
                    chunks.add(new FileChunkDTO(
                            resultSet.getInt("chunk_index"),
                            resultSet.getString("checksum"),
                            resultSet.getLong("size_bytes"),
                            resultSet.getString("primary_node"),
                            resultSet.getString("replica_node")
                    ));
                }
                return chunks;
            }
        }
    }

    private UploadSessionData findUploadSession(Connection connection, String userId, String uploadId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT original_name, stored_name, mime_type, size_bytes, total_chunks, created_at
                FROM upload_sessions
                WHERE id = ? AND user_id = ?
                """)) {
            statement.setObject(1, UUID.fromString(uploadId));
            statement.setObject(2, UUID.fromString(userId));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Upload nao encontrado");
                }
                return new UploadSessionData(
                        resultSet.getString("original_name"),
                        resultSet.getString("stored_name"),
                        resultSet.getString("mime_type"),
                        resultSet.getLong("size_bytes"),
                        resultSet.getInt("total_chunks"),
                        resultSet.getTimestamp("created_at")
                );
            }
        }
    }

    private String calculateFileChecksum(String fileId) {
        List<FileChunkDTO> chunks = getFileChunks(fileId);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (FileChunkDTO chunk : chunks) {
                output.write(downloadChunkResilient(fileId, chunk));
            }
            return ChecksumUtil.sha256(output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao consolidar checksum", exception);
        }
    }

    private void validateQuota(String userId, long incomingBytes) {
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT u.quota_bytes, COALESCE(SUM(f.size_bytes), 0) AS used_bytes
                     FROM users u
                     LEFT JOIN files f ON f.user_id = u.id
                     WHERE u.id = ?
                     GROUP BY u.quota_bytes
                     """)) {
            statement.setObject(1, UUID.fromString(userId));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long quota = resultSet.getLong("quota_bytes");
                    long used = resultSet.getLong("used_bytes");
                    if (used + incomingBytes > quota) {
                        throw new IllegalArgumentException("Cota insuficiente para upload");
                    }
                }
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao validar cota", exception);
        }
    }

    private List<FileActivityDTO> findRecentActivities(String userId, String action) {
        try (Connection connection = ConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT message, created_at
                     FROM logs
                     WHERE user_id = ? AND action = ?
                     ORDER BY created_at DESC
                     LIMIT 5
                     """)) {
            statement.setObject(1, UUID.fromString(userId));
            statement.setString(2, action);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<FileActivityDTO> activities = new ArrayList<>();
                while (resultSet.next()) {
                    activities.add(new FileActivityDTO(
                            resultSet.getString("message"),
                            action,
                            resultSet.getTimestamp("created_at").toInstant().toString()
                    ));
                }
                return activities;
            }
        } catch (Exception exception) {
            return List.of();
        }
    }

    private FileItemDTO toFileItem(ResultSet resultSet) throws Exception {
        return new FileItemDTO(
                resultSet.getObject("id", UUID.class).toString(),
                resultSet.getString("original_name"),
                resultSet.getString("mime_type"),
                resultSet.getLong("size_bytes"),
                resultSet.getInt("total_chunks"),
                resultSet.getString("checksum"),
                resultSet.getString("status"),
                resultSet.getTimestamp("created_at").toInstant().toString(),
                resultSet.getTimestamp("updated_at").toInstant().toString()
        );
    }

    private record UploadSessionData(
            String originalName,
            String storedName,
            String mimeType,
            long sizeBytes,
            int totalChunks,
            Timestamp createdAt
    ) {
    }
}
