package uk.gov.hmcts.reform.hmc.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn3XX;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn404;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn4XX;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn5XX;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyDeleteLinkedHearingGroups;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

class LinkedHearingGroupServiceIT extends BaseTest {

    @Autowired
    private LinkedHearingGroupService linkedHearingGroupService;

    @Autowired
    private LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";
    public static final String REQUEST_ID2 = "12345";
    public static final String TOKEN = "example-token";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedGroupDetails() {
        stubSuccessfullyDeleteLinkedHearingGroups(TOKEN, REQUEST_ID2);
        LinkedGroupDetails linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(REQUEST_ID2);
        assertNotNull(linkedGroupDetailsBeforeDelete);
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        final Long linkedOrder = hearingEntityBeforeDelete.get().getLinkedOrder();
        linkedHearingGroupService.deleteLinkedHearingGroup(REQUEST_ID2, HMC);
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
        List<LinkedHearingStatusAuditEntity> details = validateLinkedHearingAuditDetails("7700000000");
        assertEquals(DELETE_LINKED_HEARING_REQUEST, details.get(0).getLinkedHearingEvent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails3xxError() {
        stubDeleteLinkedHearingGroupsReturn3XX(TOKEN, REQUEST_ID2);
        LinkedGroupDetails linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(REQUEST_ID2);
        assertNotNull(linkedGroupDetailsBeforeDelete);
        assertEquals("ACTIVE", linkedGroupDetailsBeforeDelete.getStatus());
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        final Long linkedOrder = hearingEntityBeforeDelete.get().getLinkedOrder();
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(REQUEST_ID2, HMC));
        assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage());

        //validating Hearing details
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2100000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertEquals(1, hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7700000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNotNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());

        //validating LinkedGroupDetails
        LinkedGroupDetails linkedGroupDetailsAfterDelete = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(REQUEST_ID2);
        assertNotNull(linkedGroupDetailsAfterDelete);
        assertEquals("ACTIVE", linkedGroupDetailsAfterDelete.getStatus());
        //checking Audit tables
        validateLinkedGroupAuditDetailsAfterDelete();
        validateHearingAuditDetailsAfterDelete();
        List<LinkedHearingStatusAuditEntity> details = validateLinkedHearingAuditDetails("7700000000");
        assertEquals(DELETE_LINKED_HEARING_REQUEST, details.get(0).getLinkedHearingEvent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails4xxError() {
        stubDeleteLinkedHearingGroupsReturn4XX(TOKEN, REQUEST_ID2);
        LinkedGroupDetails linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(REQUEST_ID2);
        assertNotNull(linkedGroupDetailsBeforeDelete);
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        final Long linkedOrder = hearingEntityBeforeDelete.get().getLinkedOrder();
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(REQUEST_ID2, HMC));
        assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage());
        //validating Hearing details
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2100000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertEquals(1, hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7700000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNotNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());

        //validating LinkedGroupDetails
        Long linkedGroupId = linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID2);
        assertEquals(7700000000L, linkedGroupId);
        //checking Audit tables
        validateLinkedGroupAuditDetailsAfterDelete();
        validateHearingAuditDetailsAfterDelete();
        List<LinkedHearingStatusAuditEntity> details = validateLinkedHearingAuditDetails("7700000000");
        assertEquals(DELETE_LINKED_HEARING_REQUEST, details.get(0).getLinkedHearingEvent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails404Error() {
        stubDeleteLinkedHearingGroupsReturn404(TOKEN, REQUEST_ID2);
        LinkedGroupDetails linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(REQUEST_ID2);
        assertNotNull(linkedGroupDetailsBeforeDelete);
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(REQUEST_ID2, HMC));
        assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage());
        //validating Hearing details
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2100000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertEquals(1, hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7700000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNotNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());

        //validating LinkedGroupDetails
        Long linkedGroupId = linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID2);
        assertEquals(7700000000L, linkedGroupId);
        //checking Audit tables
        validateLinkedGroupAuditDetailsAfterDelete();
        validateHearingAuditDetailsAfterDelete();
        List<LinkedHearingStatusAuditEntity> details = validateLinkedHearingAuditDetails("7700000000");
        assertEquals(DELETE_LINKED_HEARING_REQUEST, details.get(0).getLinkedHearingEvent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_LinkedHearingDetails5xxError() {
        stubDeleteLinkedHearingGroupsReturn5XX(TOKEN, REQUEST_ID2);
        linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID2);
        LinkedGroupDetails linkedGroupDetailsBeforeDelete = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(REQUEST_ID2);
        assertNotNull(linkedGroupDetailsBeforeDelete);
        assertEquals("ACTIVE", linkedGroupDetailsBeforeDelete.getStatus());
        Optional<HearingEntity> hearingEntityBeforeDelete = hearingRepository.findById(2100000005L);
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup(REQUEST_ID2, HMC));
        assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage());

        //validating Hearing details
        Optional<HearingEntity> hearingEntityAfterDelete = hearingRepository.findById(2100000005L);
        assertTrue(hearingEntityBeforeDelete.isPresent());
        assertTrue(hearingEntityAfterDelete.isPresent());
        assertEquals(1, hearingEntityBeforeDelete.get().getLinkedOrder());
        assertEquals(1, hearingEntityAfterDelete.get().getLinkedOrder());
        assertEquals(7700000000L, hearingEntityBeforeDelete.get().getLinkedGroupDetails().getLinkedGroupId());
        assertNotNull(hearingEntityAfterDelete.get().getLinkedGroupDetails());

        //validating LinkedGroupDetails
        LinkedGroupDetails linkedGroupDetailsAfterDelete = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(REQUEST_ID2);
        assertNotNull(linkedGroupDetailsAfterDelete);
        assertEquals("ACTIVE", linkedGroupDetailsAfterDelete.getStatus());
        //checking Audit tables
        validateLinkedGroupAuditDetailsAfterDelete();
        validateHearingAuditDetailsAfterDelete();
        List<LinkedHearingStatusAuditEntity> details = validateLinkedHearingAuditDetails("7700000000");
        assertEquals(DELETE_LINKED_HEARING_REQUEST, details.get(0).getLinkedHearingEvent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_WhenHearingGroupDoesNotExist() {
        Exception exception = assertThrows(LinkedGroupNotFoundException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup("7600000123", HMC));
        assertEquals(INVALID_LINKED_GROUP_REQUEST_ID_DETAILS, exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void testDeleteLinkedHearingGroup_WhenHearingGroupStatusIsPending() {
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingGroupService
            .deleteLinkedHearingGroup("44445", HMC));
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

    private void validateHearingAuditDetailsAfterDelete() {
        assertEquals(Collections.emptyList(),
                     entityManager.createNativeQuery("select * from linked_hearing_details_audit where "
                                                         + "hearing_id=2100000005",
                                                     LinkedHearingDetailsAudit.class).getResultList());
    }

    private void validateLinkedGroupAuditDetailsAfterDelete() {
        assertEquals(
            Collections.emptyList(),
            entityManager.createNativeQuery("select * from linked_group_details_audit where "
                                                             + "linked_group_id=7700000000",
                                                         LinkedGroupDetailsAudit.class).getResultList());

    }

    private List<LinkedHearingStatusAuditEntity> validateLinkedHearingAuditDetails(String linkedHearingId) {
        List<LinkedHearingStatusAuditEntity> details = linkedHearingStatusAuditRepository
            .getLinkedHearingAuditDetailsByLinkedGroupId(linkedHearingId);
        assertNotNull(details.get(0).getLinkedGroupHearings());
        assertEquals("ABA1", details.get(0).getHmctsServiceId());
        return details;
    }
}
