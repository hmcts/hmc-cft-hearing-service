package uk.gov.hmcts.reform.hmc.client.futurehearing;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FutureHearingApiClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FutureHearingErrorDecoder();
    }
}
