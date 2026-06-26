package DTOs;

public record FileChunkDTO(
        int chunkIndex,
        String checksum,
        long sizeBytes,
        String primaryNode,
        String replicaNode
) {
}
