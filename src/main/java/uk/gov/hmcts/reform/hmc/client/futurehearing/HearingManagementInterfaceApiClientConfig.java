package uk.gov.hmcts.reform.hmc.client.futurehearing;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.time.Clock;

public class HearingManagementInterfaceApiClientConfig {

    @Bean
    public HearingManagementInterfaceRequestInterceptor hearingManagementInterfaceRequestInterceptor(
        ApplicationParams applicationParams, @Qualifier("utcClock") Clock clock) {
        return new HearingManagementInterfaceRequestInterceptor(applicationParams, clock);
    }
}
