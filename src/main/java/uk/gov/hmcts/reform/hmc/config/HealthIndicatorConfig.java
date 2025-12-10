package uk.gov.hmcts.reform.hmc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceHealthIndicator;
import uk.gov.hmcts.reform.hmc.repository.FutureHearingRepository;

@Configuration
public class HealthIndicatorConfig {

    @Bean
    public HearingManagementInterfaceHealthIndicator hearingManagementInterfaceHealthIndicator(
        FutureHearingRepository futureHearingRepository) {
        return new HearingManagementInterfaceHealthIndicator(futureHearingRepository);
    }
}
