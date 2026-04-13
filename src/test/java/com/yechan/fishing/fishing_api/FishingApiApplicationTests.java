package com.yechan.fishing.fishing_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class FishingApiApplicationTests {

  @Test
  void contextLoads() {}
}
