package com.yechan.fishing.fishing_api.domain.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPost;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import com.yechan.fishing.fishing_api.domain.community.repository.CommunityPostRepository;
import com.yechan.fishing.fishing_api.global.external.opensearch.OpenSearchProperties;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CommunitySearchService {

  private static final Logger log = LoggerFactory.getLogger(CommunitySearchService.class);

  private final WebClient webClient;
  private final OpenSearchProperties properties;
  private final ObjectMapper objectMapper;
  private final CommunityPostRepository communityPostRepository;

  public CommunitySearchService(
      @Qualifier("openSearchWebClient") WebClient webClient,
      OpenSearchProperties properties,
      ObjectMapper objectMapper,
      CommunityPostRepository communityPostRepository) {
    this.webClient = webClient;
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.communityPostRepository = communityPostRepository;
  }

  public void indexPost(CommunityPost post) {
    try {
      Map<String, Object> doc = buildDocument(post);
      String body = objectMapper.writeValueAsString(doc);
      webClient
          .put()
          .uri("/{index}/_doc/{id}", properties.getCommunityIndexName(), post.getId())
          .bodyValue(body)
          .retrieve()
          .toBodilessEntity()
          .block();
    } catch (Exception e) {
      log.warn("Failed to index community post {}: {}", post.getId(), e.getMessage());
    }
  }

  public void deletePost(Long postId) {
    try {
      webClient
          .delete()
          .uri("/{index}/_doc/{id}", properties.getCommunityIndexName(), postId)
          .retrieve()
          .toBodilessEntity()
          .block();
    } catch (Exception e) {
      log.warn("Failed to delete community post {} from index: {}", postId, e.getMessage());
    }
  }

  public void ensureIndexExists() {
    try {
      webClient
          .head()
          .uri("/{index}", properties.getCommunityIndexName())
          .retrieve()
          .toBodilessEntity()
          .block();
      log.info("Community search index {} already exists", properties.getCommunityIndexName());
    } catch (Exception e) {
      createIndex();
    }
  }

  public int reindexAll() {
    List<CommunityPost> posts =
        communityPostRepository.findAllWithUserByVisibilityStatus(
            VisibilityStatus.VISIBLE, org.springframework.data.domain.PageRequest.of(0, 10000));
    int count = 0;
    for (CommunityPost post : posts) {
      indexPost(post);
      count++;
    }
    log.info("Reindexed {} community posts to OpenSearch", count);
    return count;
  }

  private void createIndex() {
    try {
      Map<String, Object> mapping = buildIndexMapping();
      String body = objectMapper.writeValueAsString(mapping);
      webClient
          .put()
          .uri("/{index}", properties.getCommunityIndexName())
          .bodyValue(body)
          .retrieve()
          .toBodilessEntity()
          .block();
      log.info("Created community search index {}", properties.getCommunityIndexName());
    } catch (Exception ex) {
      log.warn("Failed to create community search index: {}", ex.getMessage());
    }
  }

  private Map<String, Object> buildDocument(CommunityPost post) {
    Map<String, Object> doc = new HashMap<>();
    doc.put("postId", post.getId());
    doc.put("authorNickname", post.getUser().getNickname());
    doc.put("authorProfileImageUrl", post.getUser().getProfileImageUrl());
    doc.put("content", post.getContent());
    doc.put("region", post.getRegion());
    doc.put("placeName", post.getPlaceName());
    doc.put("species", post.getSpecies());
    doc.put("tackleType", post.getTackleType() != null ? post.getTackleType().name() : null);
    doc.put("fishedAt", post.getFishedAt() != null ? post.getFishedAt().toString() : null);
    doc.put("createdAt", post.getCreatedAt().toString());
    doc.put("published_at", post.getCreatedAt().toString());
    doc.put("thumbnailImageUrl", post.getThumbnailImageUrl());
    return doc;
  }

  private Map<String, Object> buildIndexMapping() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("postId", Map.of("type", "long"));
    props.put("authorNickname", Map.of("type", "text"));
    props.put("authorProfileImageUrl", Map.of("type", "keyword", "index", false));
    props.put("content", Map.of("type", "text"));
    props.put("region", Map.of("type", "keyword"));
    props.put("placeName", Map.of("type", "text"));
    props.put("species", Map.of("type", "keyword"));
    props.put("tackleType", Map.of("type", "keyword"));
    props.put("fishedAt", Map.of("type", "date"));
    props.put("createdAt", Map.of("type", "date"));
    props.put("published_at", Map.of("type", "date"));
    props.put("thumbnailImageUrl", Map.of("type", "keyword", "index", false));

    Map<String, Object> mapping = new LinkedHashMap<>();
    mapping.put("mappings", Map.of("properties", props));
    return mapping;
  }
}
