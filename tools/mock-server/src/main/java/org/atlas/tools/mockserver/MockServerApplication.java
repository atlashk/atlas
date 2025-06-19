package org.atlas.tools.mockserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "org.atlas",
})
public class MockServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MockServerApplication.class, args);
  }
}
