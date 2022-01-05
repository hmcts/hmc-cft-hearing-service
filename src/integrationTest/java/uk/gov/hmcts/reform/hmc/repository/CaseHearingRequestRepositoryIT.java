package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CaseHearingRequestRepositoryIT extends BaseTest {

    @Autowired
    CaseHearingRequestRepository caseHearingRequestRepository;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetVersionNumber_HearingIdIsValid() {
        Integer versionNumber = caseHearingRequestRepository.getVersionNumber(2000000000L);
        assertEquals(1, versionNumber);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetVersionNumber_HearingIdIsInValid() {
        Integer versionNumber = caseHearingRequestRepository.getVersionNumber(2020000001L);
        assertNull(versionNumber);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_ValidCaseRef() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetails("9372710950276233");
        assertEquals(3, entities.size());
        assertEquals("9372710950276233", entities.get(0).getCaseReference());
        assertEquals("9372710950276233", entities.get(1).getCaseReference());
        assertEquals("9372710950276233", entities.get(2).getCaseReference());
        assertEquals("ABA1", entities.get(0).getHmctsServiceID());
        assertEquals("HEARING_UPDATED", entities.get(0).getHearing().getStatus());
        assertEquals("HEARING_REQUESTED", entities.get(1).getHearing().getStatus());
        assertEquals("HEARING_REQUESTED", entities.get(2).getHearing().getStatus());
        assertEquals(2, entities.get(0).getHearing().getHearingResponse().get(0).getHearingResponseId());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testGetHearingDetails_ValidCaseRefAndStatus() {
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository
            .getHearingDetailsWithStatus("9372710950276233", "HEARING_REQUESTED");
        assertEquals(2, entities.size());
        assertEquals("9372710950276233", entities.get(0).getCaseReference());
        assertEquals("9372710950276233", entities.get(1).getCaseReference());
        assertEquals("ABA1", entities.get(0).getHmctsServiceID());
        assertEquals("HEARING_REQUESTED", entities.get(0).getHearing().getStatus());
        assertEquals(3, entities.get(0).getHearing().getHearingResponse().get(0).getHearingResponseId());
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
}
