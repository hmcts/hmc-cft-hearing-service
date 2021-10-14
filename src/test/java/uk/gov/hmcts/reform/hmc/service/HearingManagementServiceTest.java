package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class HearingManagementServiceTest {

    @Mock
    private HearingManagementServiceImpl managementService;

    private HearingManagementServiceImpl hearingManagementService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingManagementService = new HearingManagementServiceImpl();
    }

    @Test
    void shouldFailAsRequestDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Request details are required", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldFailAsHearingDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Hearing Details are required", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldFailAsCaseDetailsCaseCategoriesNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.getCaseDetails().setCaseCategories(new CaseCategory[0]);
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Case categories are required", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldFailAsCaseDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Case details are required", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldFailAsPanelRequirementsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Panel requirements are required", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldFailAsHearingLocationsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.getHearingDetails().setHearingLocations(new HearingLocation[0]);
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Hearing locations are required", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldFailAsHearingWindowDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.getHearingDetails().getHearingWindow().setHearingWindowStartDateRange(null);
        hearingRequest.getHearingDetails().getHearingWindow().setHearingWindowEndDateRange(null);
        hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Hearing window details are required", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldFailAsDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        try {
            hearingManagementService.validateHearingRequest(hearingRequest);
            Assertions.fail("Expected an BadRequestException to be thrown");
        } catch (Exception exception) {
            assertEquals("Invalid details", exception.getMessage());
            assertThat(exception).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void shouldPassWithHearing_Case_Request_Details_Valid() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        managementService.validateHearingRequest(hearingRequest);
        verify(managementService, times(1)).validateHearingRequest(any());
    }

    @Test
    void shouldPassWithHearing_Case_Request_Party_Details_Valid() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        PartyDetails partyDetail = TestingUtil.partyDetails();
        PartyDetails[] partyDetails = {partyDetail};
        hearingRequest.setPartyDetails(partyDetails);
        managementService.validateHearingRequest(hearingRequest);
        verify(managementService, times(1)).validateHearingRequest(any());
    }

}
