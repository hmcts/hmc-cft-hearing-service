package uk.gov.hmcts.reform.hmc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {
    Application.class,
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class BaseTest {

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;


    @Configuration
    static class WireMockTestConfiguration {
        @Bean
        public WireMockConfigurationCustomizer wireMockConfigurationCustomizer() {
            return config -> config.extensions(new WiremockFixtures.ConnectionClosedTransformer());
        }
    }

}
