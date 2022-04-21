package uk.gov.hmcts.reform.hmc.client.futurehearing;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.time.Clock;
import java.time.Instant;

public class HearingManagementInterfaceRequestInterceptor implements RequestInterceptor {

    private final ApplicationParams applicationParams;
    private final Clock clock;

    public HearingManagementInterfaceRequestInterceptor(ApplicationParams applicationParams,
                                                        @Qualifier("utcClock") Clock clock) {
        this.applicationParams = applicationParams;
        this.clock = clock;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Source-System", applicationParams.getSourceSystem());
        template.header("Destination-System", applicationParams.getDestinationSystem());
        template.header("Request-Created-At", Instant.now(clock).toString());
    }
}
