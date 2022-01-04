package uk.gov.hmcts.reform.hmc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@SuppressWarnings("HideUtilityClassConstructor")
// Spring needs a constructor, it is not a utility class
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
