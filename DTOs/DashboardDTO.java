package DTOs;

import java.util.List;

public record DashboardDTO(
        long usedBytes,
        long quotaBytes,
        int totalFiles,
        double usagePercentage,
        List<FileActivityDTO> lastUploads,
        List<FileActivityDTO> lastDownloads
) {
}
