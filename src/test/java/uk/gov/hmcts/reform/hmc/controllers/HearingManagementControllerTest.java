package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.model.*;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = HearingManagementController.class)
class HearingManagementControllerTest {

    @Autowired
    protected ObjectMapper objectMapper;

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

    private static final Logger logger = LoggerFactory.getLogger(HearingManagementControllerTest.class);


    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8"));

    @Test
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

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhn() throws Exception {
        final String validCaseRef = "9372710950276233";
        doReturn(createHearingRequest(validCaseRef)).when(hearingManagementService)
            .validateGetHearingsRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = controller.getHearingsRequest(validCaseRef, null);
        verify(hearingManagementService, times(1)).validateGetHearingsRequest(any(), any());
        Assert.isTrue(hearingRequest.getCaseDetails().getCaseRef().equals(validCaseRef));
    }

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhnAndStatus() throws Exception {
        final String validCaseRef = "9372710950276233";
        final String status = "UPDATED"; // for example
        doReturn(createHearingRequest(validCaseRef, status)).when(hearingManagementService)
            .validateGetHearingsRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = controller.getHearingsRequest(validCaseRef, status);
        verify(hearingManagementService, times(1)).validateGetHearingsRequest(any(), any());
        Assert.isTrue(hearingRequest.getCaseDetails().getCaseRef().equals(validCaseRef));
    }

    private HearingRequest createHearingRequest(String caseRef) {
        return createHearingRequest(caseRef, null);
    }

    private HearingRequest createHearingRequest(String caseRef, String status) {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(createRequestDetails());
        hearingRequest.setHearingDetails(createHearingDetails());
        hearingRequest.setCaseDetails(createCaseDetails(caseRef));
        hearingRequest.setPartyDetails(createPartyDetailsList());
        try {
             logger.info(objectMapper.writeValueAsString(hearingRequest));
        } catch (Exception e) {}
        return hearingRequest;
    }

    private RequestDetails createRequestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

    private CaseDetails createCaseDetails(String caseRef) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef(caseRef);
        caseDetails.setCaseDeepLink("localhost/hearings");
//        caseDetails.setCaseCategories();
        caseDetails.setCaseManagementLocationCode("MLC1");
        caseDetails.setRequestTimeStamp(LocalDateTime.now());
        caseDetails.setCaseAdditionalSecurityFlag(false);
        caseDetails.setCaseInterpreterRequiredFlag(true);
        caseDetails.setCaseSlaStartDate(LocalDate.now());
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setHmctsInternalCaseName("FRAUD1");
        caseDetails.setPublicCaseName("GLOBALFRAUD1");
        return caseDetails;
    }

    private HearingDetails createHearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(false);
        hearingDetails.setDuration(30);
//        hearingDetails.setFacilitiesRequired(false);
//        hearingDetails.setHearingLocations();
//        hearingDetails.setHearingRequester();
        hearingDetails.setHearingType("TYPE1");
        hearingDetails.setHearingInWelshFlag(false);
        hearingDetails.setHearingIsLinkedFlag(false);
        hearingDetails.setHearingPriorityType("TYPE1");
//        hearingDetails.setHearingWindow();
//        hearingDetails.setLeadJudgeContractType();
//        hearingDetails.setListingComments();
//        hearingDetails.setPanelRequirements();
        return hearingDetails;
    }

    private List<PartyDetails> createPartyDetailsList() {
        List<PartyDetails> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(createPartyDetails(true, 1));
        partyDetailsList.add(createPartyDetails(false, 2));
        return partyDetailsList;
    }

    private PartyDetails createPartyDetails(boolean individual, int id) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("PARTYID" + id);
        partyDetails.setPartyRole("PARTYROLE" + id);
        partyDetails.setPartyType("PARTYTYPE" + id);
        if (individual) {
            partyDetails.setIndividualDetails(createIndividualDetails(1));
        } else {
            partyDetails.setOrganisationDetails(createOrganisationDetails(2));
        }
        return partyDetails;
    }

    private IndividualDetails createIndividualDetails(int id) {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("First" + id);
        individualDetails.setLastName("Last" + id);
        individualDetails.setInterpreterLanguage("Turkish");
        return individualDetails;
    }

    private OrganisationDetails createOrganisationDetails(int id) {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setCftOrganisationID("ORGID" + id);
        organisationDetails.setOrganisationType("ORGTYPE");
        organisationDetails.setName("Name" + id);
        return organisationDetails;
    }

}
