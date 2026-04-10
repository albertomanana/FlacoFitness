package com.flacofitness.app.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("userPhotoUrlResolver")
public class UserPhotoUrlResolver {

    private static final String DEFAULT_AVATAR_URL = "/img/avatar-placeholder.svg";

    public String resolvePhotoUrl(String fotoPath) {
        if (!StringUtils.hasText(fotoPath)) {
            return DEFAULT_AVATAR_URL;
        }

        String normalizedPath = fotoPath.trim().replace("\\", "/");

        if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://")) {
            return normalizedPath;
        }

        if (normalizedPath.startsWith("/uploads/")) {
            return normalizedPath;
        }

        if (normalizedPath.startsWith("uploads/")) {
            return "/" + normalizedPath;
        }

        int uploadIndex = normalizedPath.indexOf("/uploads/");
        if (uploadIndex >= 0) {
            return normalizedPath.substring(uploadIndex);
        }

        uploadIndex = normalizedPath.indexOf("uploads/");
        if (uploadIndex >= 0) {
            return "/" + normalizedPath.substring(uploadIndex);
        }

        return DEFAULT_AVATAR_URL;
    }

    public boolean hasCustomPhoto(String fotoPath) {
        return !DEFAULT_AVATAR_URL.equals(resolvePhotoUrl(fotoPath));
    }

    public String getDefaultAvatarUrl() {
        return DEFAULT_AVATAR_URL;
    }
}
