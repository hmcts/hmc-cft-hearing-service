package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.config.UrlManager;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.UnNotifiedHearingService;
import uk.gov.hmcts.reform.hmc.service.common.OverrideAuditService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UnNotifiedHearingsController.class,
    excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
        {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class UnNotifiedHearingsControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockitoBean
    private UnNotifiedHearingService unNotifiedHearingService;

    @MockitoBean
    private ApplicationParams applicationParams;

    @MockitoBean
    private OverrideAuditService overrideAuditService;

    @MockitoBean
    private UrlManager urlManager;

    List<String> hearingStatus  = List.of("LISTED");

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldReturn200_whenRequestIdIsValid() {
        UnNotifiedHearingsController controller = new UnNotifiedHearingsController(unNotifiedHearingService);
        controller.getUnNotifiedHearings("TEST", LocalDateTime.now(), LocalDateTime.now(), hearingStatus);
        verify(unNotifiedHearingService, times(1)).getUnNotifiedHearings(any(), any(),any(), any());
    }

    @Test
    void shouldReturn200_whenHearingStartDateToNotPresent() {
        UnNotifiedHearingsController controller = new UnNotifiedHearingsController(unNotifiedHearingService);
        controller.getUnNotifiedHearings("TEST", LocalDateTime.now(), null, hearingStatus);
        verify(unNotifiedHearingService, times(1)).getUnNotifiedHearings(any(), any(),any(), any());
    }

}
