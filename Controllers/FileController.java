package Controllers;

import Common.http.HttpRequest;
import Common.http.HttpResponse;
import Common.http.JsonUtil;
import DTOs.FileItemDTO;
import DTOs.UploadSessionDTO;
import Requests.UploadChunkRequest;
import Requests.UploadCompleteRequest;
import Requests.UploadInitRequest;
import Responses.ApiResponse;
import Responses.FileDetailsResponse;
import Responses.UploadChunkResponse;
import Services.FileService;
import Services.UploadQueueService;
import java.util.List;

public class FileController {

    private final FileService fileService;
    private final UploadQueueService uploadQueueService;

    public FileController(FileService fileService, UploadQueueService uploadQueueService) {
        this.fileService = fileService;
        this.uploadQueueService = uploadQueueService;
    }

    public HttpResponse initUpload(HttpRequest request, String userId) {
        UploadInitRequest payload = JsonUtil.fromJson(request.body(), UploadInitRequest.class);
        UploadSessionDTO session = uploadQueueService.initUpload(
                userId,
                payload.fileName(),
                payload.mimeType(),
                payload.sizeBytes(),
                payload.totalChunks()
        );
        return HttpResponse.json(201, new ApiResponse<>(true, session, "Upload preparado"));
    }

    public HttpResponse uploadChunk(HttpRequest request, String userId) {
        UploadChunkRequest payload = JsonUtil.fromJson(request.body(), UploadChunkRequest.class);
        UploadQueueService.UploadChunkTaskResult result = uploadQueueService.enqueueChunk(
                userId,
                payload.uploadId(),
                payload.chunkIndex(),
                payload.totalChunks(),
                payload.base64Data()
        );
        UploadChunkResponse response = new UploadChunkResponse(
                result.uploadId(),
                result.chunkIndex(),
                result.uploadedChunks(),
                result.totalChunks(),
                result.status(),
                result.queueSize()
        );
        return HttpResponse.json(202, new ApiResponse<>(true, response, "Chunk processado"));
    }

    public HttpResponse completeUpload(HttpRequest request, String userId) {
        UploadCompleteRequest payload = JsonUtil.fromJson(request.body(), UploadCompleteRequest.class);
        uploadQueueService.completeUpload(userId, payload.uploadId());
        FileItemDTO file = fileService.getFile(userId, payload.uploadId());
        return HttpResponse.json(200, new ApiResponse<>(true, file, "Upload concluido"));
    }

    public HttpResponse listFiles(String userId) {
        List<FileItemDTO> files = fileService.listFiles(userId);
        return HttpResponse.json(200, new ApiResponse<>(true, files, "Arquivos listados"));
    }

    public HttpResponse fileDetails(String userId, String fileId) {
        FileDetailsResponse response = new FileDetailsResponse(
                fileService.getFile(userId, fileId),
                fileService.getFileChunks(fileId)
        );
        return HttpResponse.json(200, new ApiResponse<>(true, response, "Detalhes carregados"));
    }

    public HttpResponse download(String userId, String fileId) {
        FileItemDTO file = fileService.getFile(userId, fileId);
        byte[] bytes = fileService.downloadFile(userId, fileId);
        return HttpResponse.binary(200, bytes, file.mimeType(), file.originalName());
    }
}
