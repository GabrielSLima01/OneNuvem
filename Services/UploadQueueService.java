package Services;

import Common.config.AppConfig;
import Common.logging.AppLogger;
import Common.util.ChecksumUtil;
import DTOs.UploadSessionDTO;
import Middleware.Node;
import Middleware.NodeManager;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class UploadQueueService {

    private final ExecutorService workers;
    private final LinkedBlockingQueue<Runnable> queue;
    private final NodeManager nodeManager;
    private final NodeSocketService nodeSocketService;
    private final FileService fileService;
    private final AppLogger logger;

    public UploadQueueService(NodeManager nodeManager, NodeSocketService nodeSocketService, FileService fileService, AppLogger logger) {
        this.queue = new LinkedBlockingQueue<>();
        this.workers = Executors.newFixedThreadPool(AppConfig.workerThreads());
        this.nodeManager = nodeManager;
        this.nodeSocketService = nodeSocketService;
        this.fileService = fileService;
        this.logger = logger;
    }

    public UploadChunkTaskResult enqueueChunk(String userId, String uploadId, int chunkIndex, int totalChunks, String base64Data) {
        CompletableFuture<UploadChunkTaskResult> future = new CompletableFuture<>();
        Runnable task = () -> {
            try {
                byte[] bytes = Base64.getDecoder().decode(base64Data);
                String checksum = ChecksumUtil.sha256(bytes);
                List<Node> nodes = nodeManager.pickNodes(Math.max(2, AppConfig.replicaCount()));
                if (nodes.size() < 2) {
                    throw new IllegalStateException("Nos insuficientes para replicacao");
                }

                Node primary = nodes.getFirst();
                Node replica = nodes.get(1);
                nodeSocketService.storeChunk(primary, uploadId, chunkIndex, checksum, bytes);
                nodeSocketService.storeChunk(replica, uploadId, chunkIndex, checksum, bytes);
                UploadChunkTaskResult result = fileService.recordUploadedChunk(
                        userId,
                        uploadId,
                        chunkIndex,
                        totalChunks,
                        checksum,
                        bytes.length,
                        primary.name(),
                        replica.name()
                );
                logger.info(
                        "upload_chunk",
                        "Chunk persistido com replicacao",
                        "{\"uploadId\":\"" + uploadId + "\",\"chunkIndex\":" + chunkIndex + "}",
                        userId
                );
                future.complete(result);
            } catch (Exception exception) {
                logger.error(
                        "upload_chunk_error",
                        exception.getMessage(),
                        "{\"uploadId\":\"" + uploadId + "\",\"chunkIndex\":" + chunkIndex + "}",
                        userId
                );
                future.completeExceptionally(exception);
            }
        };
        queue.offer(task);
        workers.submit(() -> {
            Runnable queued = queue.poll();
            if (queued != null) {
                queued.run();
            }
        });

        try {
            UploadChunkTaskResult result = future.get();
            return new UploadChunkTaskResult(
                    result.uploadId(),
                    result.chunkIndex(),
                    result.uploadedChunks(),
                    result.totalChunks(),
                    result.status(),
                    queue.size()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao processar fila de upload", exception);
        }
    }

    public UploadSessionDTO initUpload(String userId, String fileName, String mimeType, long sizeBytes, int totalChunks) {
        return fileService.createUploadSession(userId, fileName, mimeType, sizeBytes, totalChunks);
    }

    public void completeUpload(String userId, String uploadId) {
        fileService.completeUpload(userId, uploadId);
    }

    public record UploadChunkTaskResult(
            String uploadId,
            int chunkIndex,
            int uploadedChunks,
            int totalChunks,
            String status,
            int queueSize
    ) {
    }
}
