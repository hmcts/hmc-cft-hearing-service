package uk.gov.hmcts.reform.hmc.client.futurehearing;

import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ActiveDirectoryApiClientConfig {

    @Bean
    public Retryer activeDirectoryRetryer(@Value("${fh.ad.retryer.period}") long period,
                                          @Value("${fh.ad.retryer.max-period}") long maxPeriod,
                                          @Value("${fh.ad.retryer.max-attempts}") int maxAttempts) {
        return new Retryer.Default(period, maxPeriod, maxAttempts);
    }
}
