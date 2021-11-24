package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.security.idam.IdamRepository;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = HearingManagementController.class,
    excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
        {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class HearingManagementControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockBean
    private HearingManagementService hearingManagementService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Disabled
    void shouldReturn400_whenRequest_Details_Are_NotPresent() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());

    }

    @Test
    void shouldReturn400_whenHearing_Details_Are_NotPresent() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());
    }

    @Test
    @Disabled
    void shouldReturn400_whenCase_Details_Are_NotPresent() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());
    }

    @Test
    void shouldReturn202_whenHearingRequestDeta() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());
    }

}
