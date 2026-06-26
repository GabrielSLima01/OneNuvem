package Responses;

import DTOs.FileChunkDTO;
import DTOs.FileItemDTO;
import java.util.List;

public record FileDetailsResponse(
        FileItemDTO file,
        List<FileChunkDTO> chunks
) {
}
