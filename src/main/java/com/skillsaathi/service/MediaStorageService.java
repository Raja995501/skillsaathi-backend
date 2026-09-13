package com.skillsaathi.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {
    /**
     * Uploads an image and returns its public HTTPS URL.
     * @param file the image file (validated for type/size by the caller)
     * @param folder logical folder in Cloudinary, e.g. "skillsaathi/profile-pictures"
     */
    String uploadImage(MultipartFile file, String folder);
}
