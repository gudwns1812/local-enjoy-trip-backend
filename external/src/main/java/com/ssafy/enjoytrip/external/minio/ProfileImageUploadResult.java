package com.ssafy.enjoytrip.external.minio;

import java.time.Instant;

public record ProfileImageUploadResult(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
}
