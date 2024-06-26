package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.ChangeReasonsEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseHearingRequestRepositoryIT extends BaseTest {

    @Autowired
    CaseHearingRequestRepository caseHearingRequestRepository;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

    private static final String UN_NOTIFIED_HEARINGS_DATA_SCRIPT = "classpath:sql/unNotified-hearings-request.sql";

    private static final String INSERT_CASE_HEARING_CHANGE_REASONS_DATA_SCRIPT =
            "classpath:sql/insert-case_hearing_request_change_reasons.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetVersionNumber_HearingIdIsValid() {
        Integer versionNumber = caseHearingRequestRepository.getLatestVersionNumber(2000000000L);
        assertEquals(1, versionNumber);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetVersionNumber_HearingIdIsInValid() {
        Integer versionNumber = caseHearingRequestRepository.getLatestVersionNumber(2020000001L);
        assertNull(versionNumber);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_ValidCaseRef() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetails("9372710950276233");
        assertEquals(3, entities.size());
        assertEquals("9372710950276233", entities.get(0).getCaseReference());
        assertEquals(2000000010L, entities.get(0).getHearing().getId());
        assertEquals(20, entities.get(0).getVersionNumber());
        assertEquals("9372710950276233", entities.get(1).getCaseReference());
        assertEquals(2000000009L, entities.get(1).getHearing().getId());
        assertEquals(30, entities.get(1).getVersionNumber());
        assertEquals("9372710950276233", entities.get(2).getCaseReference());
        assertEquals(2000000000L, entities.get(2).getHearing().getId());
        assertEquals(10, entities.get(2).getVersionNumber());
        assertEquals("TEST", entities.get(0).getHmctsServiceCode());
        assertEquals("HEARING_UPDATED", entities.get(0).getHearing().getStatus());
        assertEquals("HEARING_REQUESTED", entities.get(1).getHearing().getStatus());
        assertEquals("HEARING_REQUESTED", entities.get(2).getHearing().getStatus());
        assertEquals(2, entities.get(0).getHearing().getHearingResponses().get(0).getHearingResponseId());
        assertTrue(entities.get(0).getHearing().getIsLinkedFlag());
        assertFalse(entities.get(1).getHearing().getIsLinkedFlag());
        assertTrue(entities.get(2).getHearing().getIsLinkedFlag());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_ValidCaseRefAndStatus() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetailsWithStatus("9372710950276233", "HEARING_REQUESTED");
        assertEquals(2, entities.size());
        assertEquals("9372710950276233", entities.get(0).getCaseReference());
        assertEquals(2000000009L, entities.get(0).getHearing().getId());
        assertEquals(30, entities.get(0).getVersionNumber());
        assertEquals("9372710950276233", entities.get(1).getCaseReference());
        assertEquals(2000000000L, entities.get(1).getHearing().getId());
        assertEquals(10, entities.get(1).getVersionNumber());
        assertEquals("TEST", entities.get(0).getHmctsServiceCode());
        assertEquals("HEARING_REQUESTED", entities.get(0).getHearing().getStatus());
        assertEquals(3, entities.get(0).getHearing().getHearingResponses().get(0).getHearingResponseId());
        assertFalse(entities.get(0).getHearing().getIsLinkedFlag());
        assertTrue(entities.get(1).getHearing().getIsLinkedFlag());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_ValidCaseRefAndInvalidStatus() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetailsWithStatus("9372710950276233", "InvalidStatus");
        assertEquals(0, entities.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_InvalidCaseRefAndNullStatus() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetailsWithStatus("9372710950276233", "");
        assertEquals(0, entities.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_InvalidCaseRefAndValidStatus() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetailsWithStatus("9372710950276234", "HEARING_UPDATED");
        assertEquals(0, entities.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_InvalidCaseRef() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetails("9372710950276234");
        assertEquals(0, entities.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetCaseHearingId_HearingIdIsValid() {
        CaseHearingRequestEntity caseHearing = caseHearingRequestRepository.getLatestCaseHearingRequest(2000000000L);
        assertNotNull(caseHearing);
        assertNotNull(caseHearing.getCaseHearingID());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetCaseHearingId_HearingIdNotInDb() {
        CaseHearingRequestEntity caseHearing = caseHearingRequestRepository.getLatestCaseHearingRequest(2200000000L);
        assertNull(caseHearing);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetHmctsServiceIdNotInDb() {
        Long results = caseHearingRequestRepository.getHmctsServiceCodeCount("AA12");
        assertEquals(0L, results);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetHmctsServiceIdIsValid() {
        Long results = caseHearingRequestRepository.getHmctsServiceCodeCount("ACA2");
        assertEquals(2L, results);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_CHANGE_REASONS_DATA_SCRIPT})
    void testGetChangeReasons() {
        CaseHearingRequestEntity caseHearing = caseHearingRequestRepository.getLatestCaseHearingRequest(2000000011L);
        final List<ChangeReasonsEntity> changeReasonsEntities = caseHearing.getAmendReasonCodes();
        assertNotNull(changeReasonsEntities);
        assertEquals(3, changeReasonsEntities.size());
        final List<String> changeReasons =
                changeReasonsEntities.stream()
                        .map(ChangeReasonsEntity::getChangeReasonType)
                        .collect(Collectors.toList());

        assertTrue(changeReasons.containsAll(List.of("reason 1", "reason 2", "reason 3")));
    }
}
