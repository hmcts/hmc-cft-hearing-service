package uk.gov.hmcts.reform.hmc.service;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedHearingGroupServiceIT extends BaseTest {

    @Autowired
    private LinkedHearingGroupService linkedHearingGroupService;

    @Autowired
    private LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Autowired
    private LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    @Autowired
    private  HearingRepository hearingRepository;

    @Autowired
    private LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup() {
        linkedHearingGroupService.deleteLinkedHearingGroup(7600000000L);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedGroupDetails() {
        Optional<LinkedGroupDetails> linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .findById(7600000000L);
        assertTrue(linkedGroupDetailsBeforeDelete.isPresent());
        linkedHearingGroupService.deleteLinkedHearingGroup(7600000000L);
        Optional<LinkedGroupDetails> linkedGroupDetailsAfterDelete = linkedGroupDetailsRepository
            .findById(7600000000L);
        final Long versionNumberBeforeDelete = linkedGroupDetailsBeforeDelete.get().getLinkedGroupLatestVersion();
        assertTrue(linkedGroupDetailsAfterDelete.isPresent());
        assertEquals("ACTIVE", linkedGroupDetailsBeforeDelete.get().getStatus());
        assertEquals("PENDING", linkedGroupDetailsAfterDelete.get().getStatus());
        assertEquals(versionNumberBeforeDelete, linkedGroupDetailsBeforeDelete.get().getLinkedGroupLatestVersion());
        assertEquals(versionNumberBeforeDelete + 1, linkedGroupDetailsAfterDelete.get()
            .getLinkedGroupLatestVersion());
        assertEquals(7600000000L, linkedGroupDetailsBeforeDelete.get().getLinkedGroupId());
        assertEquals(7600000000L, linkedGroupDetailsAfterDelete.get().getLinkedGroupId());
        assertEquals("good reason", linkedGroupDetailsBeforeDelete.get().getReasonForLink());
        assertEquals("good reason", linkedGroupDetailsAfterDelete.get().getReasonForLink());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails() {
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2000000005L);
        linkedHearingGroupService.deleteLinkedHearingGroup(7600000000L);
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2000000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertNull(hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7600000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedGroupDetailsAudit() {
        linkedHearingGroupService.deleteLinkedHearingGroup(7600000000L);
        Optional<LinkedGroupDetailsAudit> linkedGroupDetailsAudit = linkedGroupDetailsAuditRepository
            .findById(1L);
        assertTrue(linkedGroupDetailsAudit.isPresent());
        assertEquals("ACTIVE", linkedGroupDetailsAudit.get().getStatus());
        assertEquals(1, linkedGroupDetailsAudit.get().getLinkedGroupVersion());
        assertEquals(7600000000L, linkedGroupDetailsAudit.get().getLinkedGroup().getLinkedGroupId());
        assertEquals("good reason", linkedGroupDetailsAudit.get().getReasonForLink());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetailsAudit() {
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2000000005L);
        final Long linkedOrder = hearingEntityBeforeDelete.get().getLinkedOrder();
        Optional<LinkedGroupDetails> linkedGroupDetails = linkedGroupDetailsRepository.findById(7600000000L);
        Long versionNumber = linkedGroupDetails.get().getLinkedGroupLatestVersion();

        linkedHearingGroupService.deleteLinkedHearingGroup(7600000000L);

        Optional<LinkedHearingDetailsAudit> linkedHearingDetailsAudit = linkedHearingDetailsAuditRepository
            .findById(1L);
        assertTrue(linkedHearingDetailsAudit.isPresent());
        assertEquals(versionNumber, linkedHearingDetailsAudit.get().getLinkedGroupVersion());
        assertEquals(2000000005L, linkedHearingDetailsAudit.get().getHearing().getId());
        assertEquals(7600000000L, linkedHearingDetailsAudit.get().getLinkedGroup().getLinkedGroupId());
        assertEquals(linkedOrder, linkedHearingDetailsAudit.get().getLinkedOrder());
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
