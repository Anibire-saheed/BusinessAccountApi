package com.payvantage.businessaccountonboarding.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

public interface DocumentStorageService {

    String upload(String keyPrefix, MultipartFile file);

    URL presignedGetUrl(String s3Key);

    void delete(String s3Key);
}
