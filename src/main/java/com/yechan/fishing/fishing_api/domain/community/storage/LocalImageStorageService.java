package com.yechan.fishing.fishing_api.domain.community.storage;

import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

public class LocalImageStorageService implements ImageStorageService {

  private static final int MAX_IMAGE_COUNT = 5;
  private static final int THUMBNAIL_SIZE = 400;
  private static final DateTimeFormatter DATE_PATH_FORMAT =
      DateTimeFormatter.ofPattern("yyyy/MM/dd");

  private final CommunityStorageProperties storageProperties;

  public LocalImageStorageService(CommunityStorageProperties storageProperties) {
    this.storageProperties = storageProperties;
  }

  @Override
  public List<StoredCommunityImage> storeCommunityImages(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return List.of();
    }
    if (files.size() > MAX_IMAGE_COUNT) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_COUNT_EXCEEDED);
    }

    Path uploadDir =
        Path.of(storageProperties.getLocalRoot())
            .resolve("community")
            .resolve(LocalDate.now().format(DATE_PATH_FORMAT));

    try {
      Files.createDirectories(uploadDir);
    } catch (IOException e) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_UPLOAD_ERROR);
    }

    List<StoredCommunityImage> storedImages = new ArrayList<>();
    String relativePrefix = "community/" + LocalDate.now().format(DATE_PATH_FORMAT);
    for (int index = 0; index < files.size(); index++) {
      MultipartFile file = files.get(index);
      validateImageFile(file);

      String extension = resolveExtension(file);
      String storedFileName = UUID.randomUUID() + extension;
      Path destination = uploadDir.resolve(storedFileName);

      try {
        file.transferTo(destination);
      } catch (IOException e) {
        throw new FishingException(ErrorCode.COMMUNITY_IMAGE_UPLOAD_ERROR);
      }

      Integer width = null;
      Integer height = null;
      try {
        BufferedImage image = ImageIO.read(destination.toFile());
        if (image != null) {
          width = image.getWidth();
          height = image.getHeight();
        }
      } catch (IOException ignored) {
        // 이미지 메타 정보 추출 실패는 업로드 자체를 막지 않습니다.
      }

      // 썸네일 생성
      String thumbFileName =
          storedFileName.substring(0, storedFileName.lastIndexOf('.')) + "_thumb.jpg";
      String thumbnailUrl = generateThumbnail(destination, uploadDir.resolve(thumbFileName));
      if (thumbnailUrl != null) {
        thumbnailUrl =
            normalizePublicBasePath(storageProperties.getPublicBasePath())
                + "/"
                + relativePrefix
                + "/"
                + thumbFileName;
      }

      String publicUrl =
          normalizePublicBasePath(storageProperties.getPublicBasePath())
              + "/"
              + relativePrefix
              + "/"
              + storedFileName;

      storedImages.add(
          new StoredCommunityImage(
              publicUrl,
              thumbnailUrl,
              index,
              file.getContentType(),
              file.getSize(),
              width,
              height));
    }

    return storedImages;
  }

  @Override
  public String storeProfileImage(MultipartFile file) {
    validateImageFile(file);

    Path uploadDir =
        Path.of(storageProperties.getLocalRoot())
            .resolve("profiles")
            .resolve(LocalDate.now().format(DATE_PATH_FORMAT));

    try {
      Files.createDirectories(uploadDir);
    } catch (IOException e) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_UPLOAD_ERROR);
    }

    String extension = resolveExtension(file);
    String storedFileName = UUID.randomUUID() + extension;
    Path destination = uploadDir.resolve(storedFileName);

    try {
      file.transferTo(destination);
    } catch (IOException e) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_UPLOAD_ERROR);
    }

    String relativePrefix = "profiles/" + LocalDate.now().format(DATE_PATH_FORMAT);
    return normalizePublicBasePath(storageProperties.getPublicBasePath())
        + "/"
        + relativePrefix
        + "/"
        + storedFileName;
  }

  private String generateThumbnail(Path source, Path thumbPath) {
    try {
      Thumbnails.of(source.toFile())
          .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
          .outputFormat("jpg")
          .outputQuality(0.8)
          .toFile(thumbPath.toFile());
      return thumbPath.toString();
    } catch (IOException e) {
      return null;
    }
  }

  private void validateImageFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new FishingException(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE);
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
      throw new FishingException(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE);
    }
  }

  private String resolveExtension(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    if (originalFilename != null) {
      int extensionIndex = originalFilename.lastIndexOf('.');
      if (extensionIndex >= 0) {
        return originalFilename.substring(extensionIndex);
      }
    }

    String contentType = file.getContentType();
    if ("image/png".equalsIgnoreCase(contentType)) {
      return ".png";
    }
    if ("image/webp".equalsIgnoreCase(contentType)) {
      return ".webp";
    }
    if ("image/gif".equalsIgnoreCase(contentType)) {
      return ".gif";
    }
    return ".jpg";
  }

  private String normalizePublicBasePath(String publicBasePath) {
    if (publicBasePath == null || publicBasePath.isBlank()) {
      return "/uploads";
    }

    String normalized = publicBasePath.startsWith("/") ? publicBasePath : "/" + publicBasePath;
    return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
  }
}
