package com.yechan.fishing.fishing_api.global.external.opensearch;

import com.yechan.fishing.fishing_api.domain.search.service.CommunitySearchService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchIndexInitializer implements ApplicationRunner {

  private final CommunitySearchService communitySearchService;

  public OpenSearchIndexInitializer(CommunitySearchService communitySearchService) {
    this.communitySearchService = communitySearchService;
  }

  @Override
  public void run(ApplicationArguments args) {
    communitySearchService.ensureIndexExists();
  }
}
