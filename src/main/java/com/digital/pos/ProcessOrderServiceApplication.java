package com.digital.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProcessOrderServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessOrderServiceApplication.class, args);
  }

}
