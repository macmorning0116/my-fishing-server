package com.yechan.fishing.fishing_api.domain.community.storage;

import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3ImageStorageService implements ImageStorageService {

  private static final int MAX_IMAGE_COUNT = 5;
  private static final int THUMBNAIL_SIZE = 400;
  private static final DateTimeFormatter DATE_PATH_FORMAT =
      DateTimeFormatter.ofPattern("yyyy/MM/dd");
  private static final ExecutorService UPLOAD_EXECUTOR = Executors.newFixedThreadPool(4);

  private final S3Client s3Client;
  private final String bucket;
  private final String baseUrl;

  public S3ImageStorageService(S3Client s3Client, String bucket, String region) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.baseUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com";
  }

  @Override
  public List<StoredCommunityImage> storeCommunityImages(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return List.of();
    }
    if (files.size() > MAX_IMAGE_COUNT) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_COUNT_EXCEEDED);
    }

    String datePath = datePath();

    // 모든 이미지를 병렬로 업로드
    List<CompletableFuture<StoredCommunityImage>> futures = new ArrayList<>();
    for (int index = 0; index < files.size(); index++) {
      MultipartFile file = files.get(index);
      validateImageFile(file);

      String id = uuid();
      String ext = extension(file);
      String originalKey = "community/" + datePath + "/" + id + ext;
      String thumbKey = "community/" + datePath + "/" + id + "_thumb.jpg";
      int sortOrder = index;

      // 원본 바이트를 먼저 읽어두기 (MultipartFile은 스레드 간 공유 불안전)
      byte[] originalBytes;
      byte[] thumbBytes;
      String contentType = file.getContentType();
      long fileSize = file.getSize();
      try {
        originalBytes = file.getBytes();
        thumbBytes = generateThumbnailBytes(file);
      } catch (IOException e) {
        throw new FishingException(ErrorCode.COMMUNITY_IMAGE_UPLOAD_ERROR);
      }

      CompletableFuture<Void> originalFuture =
          CompletableFuture.runAsync(
              () -> uploadBytes(originalBytes, originalKey, contentType), UPLOAD_EXECUTOR);
      CompletableFuture<Void> thumbFuture =
          thumbBytes != null
              ? CompletableFuture.runAsync(
                  () -> uploadBytes(thumbBytes, thumbKey, "image/jpeg"), UPLOAD_EXECUTOR)
              : CompletableFuture.completedFuture(null);

      String thumbUrl = thumbBytes != null ? baseUrl + "/" + thumbKey : null;

      CompletableFuture<StoredCommunityImage> combined =
          originalFuture.thenCombine(
              thumbFuture,
              (a, b) ->
                  new StoredCommunityImage(
                      baseUrl + "/" + originalKey,
                      thumbUrl,
                      sortOrder,
                      contentType,
                      fileSize,
                      null,
                      null));
      futures.add(combined);
    }

    // 모든 업로드 완료 대기
    return futures.stream().map(CompletableFuture::join).toList();
  }

  @Override
  public String storeProfileImage(MultipartFile file) {
    validateImageFile(file);
    String key = "profiles/" + datePath() + "/" + uuid() + extension(file);
    upload(file, key);
    return baseUrl + "/" + key;
  }

  private byte[] generateThumbnailBytes(MultipartFile file) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Thumbnails.of(file.getInputStream())
          .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
          .outputFormat("jpg")
          .outputQuality(0.8)
          .toOutputStream(baos);
      return baos.toByteArray();
    } catch (IOException e) {
      return null;
    }
  }

  private void uploadBytes(byte[] bytes, String key, String contentType) {
    PutObjectRequest request =
        PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build();
    s3Client.putObject(request, RequestBody.fromBytes(bytes));
  }

  private void upload(MultipartFile file, String key) {
    try {
      PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType(file.getContentType())
              .build();
      s3Client.putObject(
          request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException e) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_UPLOAD_ERROR);
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

  private String datePath() {
    return LocalDate.now().format(DATE_PATH_FORMAT);
  }

  private String uuid() {
    return UUID.randomUUID().toString();
  }

  private String extension(MultipartFile file) {
    String original = file.getOriginalFilename();
    if (original != null && original.contains(".")) {
      return original.substring(original.lastIndexOf("."));
    }
    return ".jpg";
  }
}
