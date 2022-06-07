package uk.gov.hmcts.reform.hmc;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.hmc.config.MessageReaderFromQueueConfiguration;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.CaseCouldNotBeFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.ServiceException;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingChannel;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
public class RestExceptionHandlerTest extends BaseTest {

    public static String ERROR_PATH_ERROR = "$.errors";
    public static String ERROR_PATH_STATUS = "$.status";
    public static String testExceptionMessage = "test message";
    HearingRequest validRequest;

    @MockBean
    protected HearingManagementServiceImpl service;

    @MockBean
    protected AccessControlService accessControlService;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private MessageReaderFromQueueConfiguration messageReaderFromQueueConfiguration;

    @MockBean
    private ApplicationParams applicationParams;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validRequest = new HearingRequest();
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeEnd(LocalDate.parse("2017-03-01"));
        hearingWindow.setDateRangeStart(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(360);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(Collections.singletonList("RoleType1"));
        panelRequirements.setAuthorisationTypes(Collections.singletonList("AuthorisationType1"));
        panelRequirements.setAuthorisationSubType(Collections.singletonList("AuthorisationSubType2"));
        hearingDetails.setPanelRequirements(panelRequirements);
        HearingLocation location1 = new HearingLocation();
        location1.setLocationType("court");
        location1.setLocationId("Location id");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        HearingChannel hearingChannel = new HearingChannel();
        hearingChannel.setChannelType("someChannelType");
        hearingDetails.setHearingChannels(List.of(hearingChannel));
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABA1");
        caseDetails.setCaseRef("1111222233334444");
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        RequestDetails requestDetails = new RequestDetails();
        validRequest.setHearingDetails(hearingDetails);
        validRequest.setCaseDetails(caseDetails);
    }

    @DisplayName("should return correct response when InvalidRoleAssignmentException is thrown")
    @Test
    void shouldHandleInvalidRoleAssignmentException() throws Exception {

        /// WHEN
        Mockito.doThrow(new InvalidRoleAssignmentException(testExceptionMessage))
            .when(accessControlService)
            .verifyCaseAccess(anyString(), anyList());

        ResultActions result =  this.mockMvc.perform(post("/hearing")
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(validRequest)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.FORBIDDEN.value(), testExceptionMessage, "FORBIDDEN");
    }

    @DisplayName("should return correct response when BadRequestException is thrown")
    @Test
    void shouldHandleBadRequestException() throws Exception {

        /// WHEN
        Mockito.doThrow(new BadRequestException(testExceptionMessage)).when(service)
            .saveHearingRequest(any(HearingRequest.class));

        ResultActions result =  this.mockMvc.perform(post("/hearing")
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(validRequest)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.BAD_REQUEST.value(), testExceptionMessage, "BAD_REQUEST");
    }

    @DisplayName("should return correct response when CaseCouldNotBeFoundException is thrown")
    @Test
    void shouldHandleCaseCouldNotBeFoundException() throws Exception {

        /// WHEN
        Mockito.doThrow(new CaseCouldNotBeFoundException(testExceptionMessage))
            .when(accessControlService).verifyCaseAccess(anyString(), anyList());

        ResultActions result =  this.mockMvc.perform(post("/hearing")
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(validRequest)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.FORBIDDEN.value(), testExceptionMessage, "FORBIDDEN");
    }

    @DisplayName("should return correct response when FeignException is thrown")
    @Test
    void shouldHandleFeignException() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "url",
                                         new HashMap<>(), null, new RequestTemplate());
        Mockito.doThrow(new FeignException.NotFound(testExceptionMessage, request, null,null))
            .when(accessControlService).verifyCaseAccess(anyString(), anyList());

        ResultActions result =  this.mockMvc.perform(post("/hearing")
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(validRequest)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR.value(), testExceptionMessage,
                                "INTERNAL_SERVER_ERROR");
    }

    @DisplayName("should return correct response when ResourceNotFoundException is thrown")
    @Test
    void shouldHandleResourceNotFoundException() throws Exception {

        /// WHEN
        Mockito.doThrow(new ResourceNotFoundException(testExceptionMessage))
            .when(accessControlService).verifyCaseAccess(anyString(), anyList());

        ResultActions result =  this.mockMvc.perform(post("/hearing")
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(validRequest)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.FORBIDDEN.value(), testExceptionMessage, "FORBIDDEN");
    }

    @DisplayName("should return correct response when ServiceException is thrown")
    @Test
    void shouldHandleServiceException() throws Exception {

        /// WHEN
        Mockito.doThrow(new ServiceException(testExceptionMessage))
            .when(accessControlService).verifyCaseAccess(anyString(), anyList());

        ResultActions result =  this.mockMvc.perform(post("/hearing")
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(validRequest)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR.value(), testExceptionMessage,
                                "INTERNAL_SERVER_ERROR");
    }

    private void assertHttpErrorResponse(ResultActions result, int expectedStatusCode, String expectedMessage,
                                         String expectedStatus) throws Exception {

        result
            .andExpect(status().is(expectedStatusCode))
            .andExpect(jsonPath(ERROR_PATH_STATUS).value(expectedStatus))
            .andExpect(jsonPath(ERROR_PATH_ERROR).value(expectedMessage));
    }
}
