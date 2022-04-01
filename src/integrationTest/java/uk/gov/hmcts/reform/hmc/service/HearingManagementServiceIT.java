package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.config.MessageReaderFromQueueConfiguration;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;

class HearingManagementServiceIT extends BaseTest {

    @Autowired
    private HearingManagementService hearingManagementService;

    @MockBean
    private MessageReaderFromQueueConfiguration messageReaderFromQueueConfiguration;

    @MockBean
    private ApplicationParams applicationParams;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

    private static final String UPDATE_HEARINGS_DATA_SCRIPT = "classpath:sql/update-case-hearing-request.sql";

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithAllMandatoryFields() {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(POST_HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithPartyDetails() {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(POST_HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithOutOrgDetails() {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(POST_HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithOutIndividualDetails() {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        createHearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(POST_HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void testValidateHearingRequest_WithOutRelatedPartyDetails() {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setIndividualDetails(
            TestingUtil.individualWithoutRelatedPartyDetails());
        createHearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(POST_HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithAllMandatoryFields() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        HearingResponse response = hearingManagementService.deleteHearingRequest(2000000000L, request);
        assertNotNull(response.getVersionNumber());
        assertEquals(CANCELLATION_REQUESTED, response.getStatus());
        assertNotNull(response.getHearingRequestId());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithNullHearingId() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .deleteHearingRequest(null, request));
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithHearingId_NotPresentInDB() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingManagementService
            .deleteHearingRequest(2000000001L, request));
        assertEquals("No hearing found for reference: 2000000001", exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testDeleteHearingRequest_WithInvalidHearingIdFormat() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .deleteHearingRequest(300000000L, request));
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
        hearingManagementService.updateHearingRequest(2000000000L, TestingUtil.updateHearingRequest(1));
        HearingResponse response = hearingManagementService
            .updateHearingRequest(2000000000L, TestingUtil.updateHearingRequest(2));
        assertEquals(2000000000L, response.getHearingRequestId());
        assertEquals(PutHearingStatus.HEARING_REQUESTED.name(), response.getStatus());
        assertEquals(3, response.getVersionNumber());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testUpdateHearingRequest_WhenStatus_Update_Requested() {
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        HearingResponse response = hearingManagementService.updateHearingRequest(2000000012L, request);
        assertEquals(2000000012L, response.getHearingRequestId());
        assertEquals(response.getStatus(), PutHearingStatus.UPDATE_REQUESTED.name());
        assertEquals(2, response.getVersionNumber());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UPDATE_HEARINGS_DATA_SCRIPT})
    void testUpdateHearingRequest_WhenStatus_Awaiting_Listing() {
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        HearingResponse response = hearingManagementService.updateHearingRequest(2000000024L, request);
        assertEquals(2000000024L, response.getHearingRequestId());
        assertEquals(PutHearingStatus.UPDATE_REQUESTED.name(), response.getStatus());
        assertEquals(2, response.getVersionNumber());
        assertNotNull(response.getTimeStamp());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearings_WithValidCaseRef() {
        GetHearingsResponse response = hearingManagementService.getHearings("9372710950276233", "");
        testGetHearings_WithValidCaseRef_assertPt1(response);
        testGetHearings_WithValidCaseRef_assertPt2(response);
    }

    void testGetHearings_WithValidCaseRef_assertPt1(GetHearingsResponse response) {
        assertEquals("9372710950276233", response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceCode());
        assertEquals(3, response.getCaseHearings().size());
        assertEquals(2000000010L, response.getCaseHearings().get(0).getHearingId());
        assertEquals(2000000009L, response.getCaseHearings().get(1).getHearingId());
        assertEquals(2000000000L, response.getCaseHearings().get(2).getHearingId());
        assertEquals("hearingType2", response.getCaseHearings().get(0).getHearingType());
        assertEquals("hearingType3", response.getCaseHearings().get(1).getHearingType());
        assertEquals("hearingType1", response.getCaseHearings().get(2).getHearingType());
        assertEquals("HEARING_REQUESTED", response.getCaseHearings().get(2).getHmcStatus());
        assertEquals("HEARING_REQUESTED", response.getCaseHearings().get(1).getHmcStatus());
        assertEquals("HEARING_UPDATED", response.getCaseHearings().get(0).getHmcStatus());
        assertEquals(2, response.getCaseHearings().get(0).getResponseVersion());
        assertEquals(3, response.getCaseHearings().get(1).getResponseVersion());
        assertEquals(1, response.getCaseHearings().get(2).getResponseVersion());
        assertEquals(ListingStatus.FIXED.name(), response.getCaseHearings().get(1).getHearingListingStatus());
        assertEquals(ListAssistCaseStatus.LISTED.name(), response.getCaseHearings().get(1).getListAssistCaseStatus());
        assertEquals(1, response.getCaseHearings().get(1).getHearingDaySchedule().size());
        assertEquals("session1-2", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getListAssistSessionId());
        assertEquals("venue3-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("venue2-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("venue1-2", response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(1).getHearingVenueId());
        assertEquals("room3-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getHearingRoomId());
        assertEquals("room1-1", response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(0).getHearingRoomId());
        assertTrue(response.getCaseHearings().get(0).getHearingIsLinkedFlag());
        assertFalse(response.getCaseHearings().get(1).getHearingIsLinkedFlag());
        assertTrue(response.getCaseHearings().get(2).getHearingIsLinkedFlag());
    }

    void testGetHearings_WithValidCaseRef_assertPt2(GetHearingsResponse response) {
        assertEquals("panel2-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingJudgeId());
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getPanelMemberId());
        assertEquals("panel3-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getPanelMemberId());
        assertNull(response.getCaseHearings().get(1).getHearingDaySchedule().get(0).getHearingJudgeId());
        assertEquals("panel1-1", response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(0).getHearingJudgeId());
        assertNull(response.getCaseHearings().get(2).getHearingDaySchedule().get(0).getPanelMemberId());
        assertEquals("panel1-2", response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(1).getPanelMemberId());
        assertNull(response.getCaseHearings().get(2).getHearingDaySchedule().get(1).getHearingJudgeId());
        assertEquals("panel1-1", response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(0).getHearingJudgeId());
        assertEquals(1, response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getAttendees().size());
        assertEquals(1, response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getAttendees().size());
        assertEquals(2, response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(0).getAttendees().size());
        assertEquals("party2-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getPartyId());
        assertEquals("party3-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getPartyId());
        assertEquals("party1-2", response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(0).getAttendees().get(1).getPartyId());
        assertEquals("subChannel2-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getHearingSubChannel());
        assertEquals("subChannel3-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getHearingSubChannel());
        assertEquals("subChannel1-1", response.getCaseHearings().get(2)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getHearingSubChannel());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearings_WithValidCaseRefAndStatus() {
        GetHearingsResponse response = hearingManagementService.getHearings("9372710950276233", "HEARING_REQUESTED");
        testGetHearings_WithValidCaseRefAndStatus_assertPart1(response);
        testGetHearings_WithValidCaseRefAndStatus_assertPart2(response);
    }

    void testGetHearings_WithValidCaseRefAndStatus_assertPart1(GetHearingsResponse response) {
        assertEquals("9372710950276233", response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceCode());
        assertEquals(2, response.getCaseHearings().size());
        assertEquals(2000000009L, response.getCaseHearings().get(0).getHearingId());
        assertEquals(2000000000L, response.getCaseHearings().get(1).getHearingId());
        assertEquals("hearingType3", response.getCaseHearings().get(0).getHearingType());
        assertEquals("hearingType1", response.getCaseHearings().get(1).getHearingType());
        assertEquals("HEARING_REQUESTED", response.getCaseHearings().get(1).getHmcStatus());
        assertEquals("HEARING_REQUESTED", response.getCaseHearings().get(0).getHmcStatus());
        assertEquals(3, response.getCaseHearings().get(0).getResponseVersion());
        assertEquals(1, response.getCaseHearings().get(1).getResponseVersion());
        assertEquals(ListingStatus.FIXED.name(), response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals(ListAssistCaseStatus.LISTED.name(),
                response.getCaseHearings().get(0).getListAssistCaseStatus());
        assertEquals(2, response.getCaseHearings().get(1).getHearingDaySchedule().size());
        assertEquals("session1-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getListAssistSessionId());
        assertEquals("venue1-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("venue3-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("venue1-2", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(1).getHearingVenueId());
        assertEquals("room1-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getHearingRoomId());
        assertEquals("panel3-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getPanelMemberId());
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
        assertNull(null, response.getCaseHearings().get(1).getHearingDaySchedule().get(0).getPanelMemberId());
        assertEquals("panel1-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getHearingJudgeId());
        assertNull(null, response.getCaseHearings().get(1).getHearingDaySchedule().get(1).getHearingJudgeId());
        assertEquals("panel1-2", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(1).getPanelMemberId());
        assertEquals(1, response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getAttendees().size());
        assertEquals(2, response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getAttendees().size());
        assertFalse(response.getCaseHearings().get(0).getHearingIsLinkedFlag());
        assertTrue(response.getCaseHearings().get(1).getHearingIsLinkedFlag());
    }

    void testGetHearings_WithValidCaseRefAndStatus_assertPart2(GetHearingsResponse response) {
        assertEquals("party3-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getPartyId());
        assertEquals("party1-1", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getPartyId());
        assertEquals("subChannel3-1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getAttendees().get(0).getHearingSubChannel());
        assertEquals("subChannel1-2", response.getCaseHearings().get(1)
            .getHearingDaySchedule().get(0).getAttendees().get(1).getHearingSubChannel());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearings_WithInValidCaseRef() {
        GetHearingsResponse response = hearingManagementService.getHearings("9372710950276234", "");
        assertEquals("9372710950276234", response.getCaseRef());
        assertNull(response.getHmctsServiceCode());
        assertEquals(0, response.getCaseHearings().size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearings_WithInValidCaseRefAndValidStatus() {
        GetHearingsResponse response = hearingManagementService.getHearings("9372710950276234", "HEARING_REQUESTED");
        assertEquals("9372710950276234", response.getCaseRef());
        assertNull(response.getHmctsServiceCode());
        assertEquals(0, response.getCaseHearings().size());
    }

}
