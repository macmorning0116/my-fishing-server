package com.yechan.fishing.fishing_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class FishingApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(FishingApiApplication.class, args);
  }
}
