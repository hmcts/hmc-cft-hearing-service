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
    public static final String REQUEST_ID2 = "12345";
    public static final String TOKEN = "example-token";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedGroupDetails() {
        stubSuccessfullyDeleteLinkedHearingGroups(TOKEN, REQUEST_ID2);
        Optional<LinkedGroupDetails> linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertTrue(linkedGroupDetailsBeforeDelete.isPresent());
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        final Long linkedOrder = hearingEntityBeforeDelete.get().getLinkedOrder();
        linkedHearingGroupService.deleteLinkedHearingGroup(7700000000L);
        //validating Hearing details
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2100000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertNull(hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7700000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());
        //validating LinkedGroupDetails
        Long linkedGroupId = linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID2);
        assertNull(linkedGroupId);
        //checking Audit tables
        validateLinkedGroupAuditDetails();
        validateHearingAuditDetails(linkedOrder);

    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails4xxError() {
        stubDeleteLinkedHearingGroupsReturn4XX(TOKEN, REQUEST_ID2);
        Optional<LinkedGroupDetails> linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertTrue(linkedGroupDetailsBeforeDelete.isPresent());
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        final Long linkedOrder = hearingEntityBeforeDelete.get().getLinkedOrder();
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(7700000000L));
        assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage());
        //validating Hearing details
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2100000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertNull(hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7700000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());

        //validating LinkedGroupDetails
        Long linkedGroupId = linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID2);
        assertNull(linkedGroupId);
        //checking Audit tables
        validateLinkedGroupAuditDetails();
        validateHearingAuditDetails(linkedOrder);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails5xxError() {
        stubDeleteLinkedHearingGroupsReturn5XX(TOKEN, REQUEST_ID2);
        Optional<LinkedGroupDetails> linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertTrue(linkedGroupDetailsBeforeDelete.isPresent());
        assertEquals("ACTIVE", linkedGroupDetailsBeforeDelete.get().getStatus());
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        final Long linkedOrder = hearingEntityBeforeDelete.get().getLinkedOrder();
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(7700000000L));
        assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage());

        //validating Hearing details
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2100000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertNull(hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7700000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());

        //validating LinkedGroupDetails
        Optional<LinkedGroupDetails> linkedGroupDetailsAfterDelete = linkedGroupDetailsRepository
            .findById(7700000000L);
        assertNotNull(linkedGroupDetailsAfterDelete);
        assertEquals("ERROR", linkedGroupDetailsAfterDelete.get().getStatus());
        //checking Audit tables
        validateLinkedGroupAuditDetails();
        validateHearingAuditDetails(linkedOrder);
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

    private void validateHearingAuditDetails(Long linkedOrder) {
        LinkedHearingDetailsAudit entity = (LinkedHearingDetailsAudit) entityManager
            .createNativeQuery("select * from linked_hearing_details_audit where "
                                   + "hearing_id=2100000005", LinkedHearingDetailsAudit.class)
            .getSingleResult();
        assertEquals(1, entity.getLinkedGroupVersion());
        assertEquals(2100000005L, entity.getHearing().getId());
        assertEquals(7700000000L, entity.getLinkedGroup().getLinkedGroupId());
        assertEquals(linkedOrder, entity.getLinkedOrder());
    }

    private void validateLinkedGroupAuditDetails() {
        LinkedGroupDetailsAudit entity = (LinkedGroupDetailsAudit) entityManager
            .createNativeQuery("select * from linked_group_details_audit where "
                                   + "linked_group_id=7700000000", LinkedGroupDetailsAudit.class).getSingleResult();
        assertEquals("ACTIVE", entity.getStatus());
        assertEquals(1, entity.getLinkedGroupVersion());
        assertEquals(7700000000L, entity.getLinkedGroup().getLinkedGroupId());
        assertEquals("good reason", entity.getReasonForLink());
    }
}
