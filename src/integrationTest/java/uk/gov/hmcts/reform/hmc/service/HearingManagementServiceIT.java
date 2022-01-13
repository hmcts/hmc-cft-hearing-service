package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;

class HearingManagementServiceIT extends BaseTest {

    @Autowired
    private HearingManagementService hearingManagementService;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithAllMandatoryFields() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER,response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithPartyDetails() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER,response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithOutOrgDetails() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER,response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithOutIndividualDetails() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER,response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithOutRelatedPartyDetails() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(
            TestingUtil.individualWithoutRelatedPartyDetails());
        hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER,response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithAllMandatoryFields() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        hearingManagementService.deleteHearingRequest(2000000000L, request);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithNullHearingId() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(null, request);
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithHearingId_NotPresentInDB() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingManagementService.deleteHearingRequest(2000000001L, request);
        });
        assertEquals("No hearing found for reference: 2000000001", exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithInvalidHearingIdFormat() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(300000000L, request);
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithInvalidHearingStatus() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(2000000011L, request);
        });
        assertEquals(INVALID_DELETE_HEARING_STATUS, exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testUpdateHearingRequest_WithInvalidHearingStatus() {
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.updateHearingRequest(2000000011L, request);
        });
        assertEquals(INVALID_PUT_HEARING_STATUS, exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testUpdateHearingRequest_WithValidData() {
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        hearingManagementService.updateHearingRequest(2000000000L, request);
    }

}
