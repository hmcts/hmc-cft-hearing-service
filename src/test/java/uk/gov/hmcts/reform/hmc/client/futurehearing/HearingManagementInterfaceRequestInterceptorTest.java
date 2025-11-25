package uk.gov.hmcts.reform.hmc.client.futurehearing;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

class HearingManagementInterfaceRequestInterceptorTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2021-06-10T04:00:00.08Z"), ZoneOffset.UTC);
    private static final String SOURCE_SYSTEM = "SOURCE_SYSTEM";
    private static final String DESTINATION_SYSTEM = "DESTINATION_SYSTEM";
    private static final String TEST_TOKEN = "test-token";

    @InjectMocks
    HearingManagementInterfaceRequestInterceptor hearingManagementInterfaceRequestInterceptor;

    @Mock
    ApplicationParams applicationParams;
    private RequestTemplate template;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        template = new RequestTemplate();
        template.header(AUTHORIZATION, TEST_TOKEN);
        hearingManagementInterfaceRequestInterceptor = new
            HearingManagementInterfaceRequestInterceptor(applicationParams, fixedClock);
        given(applicationParams.getSourceSystem()).willReturn(SOURCE_SYSTEM);
        given(applicationParams.getDestinationSystem()).willReturn(DESTINATION_SYSTEM);
    }

    @Test
    @DisplayName("Headers should be added if not present")
    void shouldApplyHeaders() {
        hearingManagementInterfaceRequestInterceptor.apply(template);

        assertThat(template.headers().get(AUTHORIZATION)).containsOnly(TEST_TOKEN);
        assertThat(template.headers().get("Source-System")).containsOnly(SOURCE_SYSTEM);
        assertThat(template.headers().get("Destination-System")).containsOnly(DESTINATION_SYSTEM);
        assertThat(template.headers().get("Request-Created-At")).containsOnly("2021-06-10T04:00:00Z");
    }
}
