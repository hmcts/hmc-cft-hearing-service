package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;

import java.util.Optional;
import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn4XX;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn5XX;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyDeleteLinkedHearingGroups;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

//@Disabled("Work in development under HMAN-97")
class LinkedHearingGroupServiceIT extends BaseTest {

    @Autowired
    private LinkedHearingGroupService linkedHearingGroupService;

    @Autowired
    private LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Autowired
    private LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    @Autowired
    private EntityManager entityManager;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";

    public static final String REQUEST_ID = "44444";
    public static final String REQUEST_ID2 = "12345";
    public static final String TOKEN = "example-token";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup() {
        linkedHearingGroupService.deleteLinkedHearingGroup(7600000000L);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedGroupDetails() {
        stubSuccessfullyDeleteLinkedHearingGroups(TOKEN, REQUEST_ID);
        Optional<LinkedGroupDetails> linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertTrue(linkedGroupDetailsBeforeDelete.isPresent());
        linkedHearingGroupService.deleteLinkedHearingGroup(7700000000L);
        Long linkedGroupId = linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID2);
        assertNull(linkedGroupId);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails4XXError() {
        stubDeleteLinkedHearingGroupsReturn4XX(TOKEN, REQUEST_ID);
        Optional<LinkedGroupDetails> linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertTrue(linkedGroupDetailsBeforeDelete.isPresent());
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(7700000000L));
        assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage());
        Long linkedGroupId = linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID2);
        assertNull(linkedGroupId);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails5XXError() {
        stubDeleteLinkedHearingGroupsReturn5XX(TOKEN, REQUEST_ID);
        Optional<LinkedGroupDetails> linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertTrue(linkedGroupDetailsBeforeDelete.isPresent());
        assertEquals("ACTIVE", linkedGroupDetailsBeforeDelete.get().getStatus());
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(7700000000L));
        assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage());
        Optional<LinkedGroupDetails> linkedGroupDetailsAfterDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertNotNull(linkedGroupDetailsAfterDelete);
        assertEquals("ERROR", linkedGroupDetailsBeforeDelete.get().getStatus());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_WhenHearingGroupDoesNotExist() {
        Exception exception = assertThrows(LinkedHearingGroupNotFoundException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(7600000123L));
        assertEquals("No hearing group found for reference: 7600000123", exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_WhenHearingGroupStatusIsPending() {
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(7600000501L));
        assertEquals("007 group is in a PENDING state", exception.getMessage());
    }
}
