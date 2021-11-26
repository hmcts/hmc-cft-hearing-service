package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = HearingManagementController.class)
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
    void shouldReturn200_whenRequestIdIsValid() {
        doNothing().when(hearingManagementService).getHearingRequest(Mockito.any(), anyBoolean());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.getHearing(1234L, true);
        verify(hearingManagementService, times(1)).getHearingRequest(any(), anyBoolean());

    }

    @Test
    void shouldReturn400_whenRequest_Details_Are_NotPresent() {
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());

    }

    @Test
    void shouldReturn400_whenHearing_Details_Are_NotPresent() {
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());
    }

    @Test
    void shouldReturn400_whenCase_Details_Are_NotPresent() {
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());
    }

    @Test
    void shouldReturn201_whenHearingRequestData() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());
    }

}
