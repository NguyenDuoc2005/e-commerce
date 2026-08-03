package com.ecommerce.user.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    public String uploadAvatar(MultipartFile avatar, String ownerId) throws IOException {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        avatar.getBytes();
        return null;
    }
}
