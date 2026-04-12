package com.yechan.fishing.fishing_api.domain.community.storage;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

  List<StoredCommunityImage> storeCommunityImages(List<MultipartFile> files);

  String storeProfileImage(MultipartFile file);
}
