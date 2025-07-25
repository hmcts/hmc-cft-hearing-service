package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.ManageExceptionsService;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ManageExceptionsController.class,
    excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
        {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class ManageExceptionsControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockBean
    ManageExceptionsService manageExceptionsService;

    @MockBean
    SecurityUtils securityUtils;

    @Mock
    HearingStatusAuditService hearingStatusAuditService;

    private static final String CLIENT_S2S_TOKEN = "hmc_tech_admin";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        doReturn(CLIENT_S2S_TOKEN).when(securityUtils)
            .getServiceNameFromS2SToken(any());
        hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(any(),any(),any(),any(),any(),any(),any());
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(any(),any(),any(),any(),any(),any(),any());
    }

    @Nested
    @DisplayName("manageExceptions")
    class ManageExceptions {

        @Test
        void shouldReturn200_whenValidRequest() {
            ManageExceptionRequest request = new ManageExceptionRequest();
            ManageExceptionResponse response = new ManageExceptionResponse();
            when(manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN))
                .thenReturn(response);
            doReturn(response).when(manageExceptionsService).manageExceptions(any(), any());

            ManageExceptionsController controller = new ManageExceptionsController(manageExceptionsService,
                                                                                   securityUtils);

            controller.manageExceptions(CLIENT_S2S_TOKEN, request);
            verify(manageExceptionsService, times(1)).manageExceptions(any(), any());
        }
    }

}
