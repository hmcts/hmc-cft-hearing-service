package uk.gov.hmcts.reform.hmc.client;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.client.datastore.SystemUserAuthHeadersInterceptor;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;

class SystemUserAuthHeadersInterceptorTest {


    public static final String S2S_TOKEN = "dcdsfda";
    @InjectMocks
    private SystemUserAuthHeadersInterceptor interceptor;

    @Mock
    private SecurityUtils securityUtils;
    private RequestTemplate template;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        template = new RequestTemplate();
    }

    @Test
    @DisplayName("System user auth headers should apply")
    void shouldApplyAuthHeaders() {
        given(securityUtils.getS2SToken()).willReturn(S2S_TOKEN);

        interceptor.apply(template);

        assertThat(template.headers().get(SERVICE_AUTHORIZATION)).containsOnly(S2S_TOKEN);
    }
}
