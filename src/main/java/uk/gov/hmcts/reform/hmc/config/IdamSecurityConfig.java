package uk.gov.hmcts.reform.hmc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
@Setter
@ConfigurationProperties("idam.security")
public class IdamSecurityConfig {

    private List<String> allowedIssuers;
}
