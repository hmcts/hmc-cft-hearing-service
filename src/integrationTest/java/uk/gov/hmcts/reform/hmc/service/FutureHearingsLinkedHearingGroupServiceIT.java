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
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FutureHearingsLinkedHearingGroupServiceIT extends BaseTest {

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String INSERT_TEST_DATA_SCRIPT = "classpath:sql/insert-future-hearings-linked-hearings.sql";

    private static final long LINKED_HEARING_GROUP_ID = 100L;
    private static final long HEARING_ID_1 = 200L;
    private static final long HEARING_ID_2 = 201L;

    private final HearingRepository hearingRepository;
    private final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    private final EntityManager entityManager;
    private final FutureHearingsLinkedHearingGroupService futureHearingsLinkedHearingGroupService;

    @Autowired
    public FutureHearingsLinkedHearingGroupServiceIT(
        HearingRepository hearingRepository,
        LinkedGroupDetailsRepository linkedGroupDetailsRepository,
        EntityManager entityManager,
        FutureHearingsLinkedHearingGroupService futureHearingsLinkedHearingGroupService) {
        this.hearingRepository = hearingRepository;
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.entityManager = entityManager;
        this.futureHearingsLinkedHearingGroupService = futureHearingsLinkedHearingGroupService;
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_TEST_DATA_SCRIPT})
    void processDeleteHearingRequestShouldUpdateLinkedHearingGroup() {
        List<HearingEntity> groupHearings = hearingRepository.findByLinkedGroupId(LINKED_HEARING_GROUP_ID);
        LinkedGroupDetails linkedGroupDetails = getLinkedGroupDetails();

        futureHearingsLinkedHearingGroupService.processDeleteHearingRequest(groupHearings, linkedGroupDetails);

        LinkedGroupDetails updatedLinkedGroupDetails = getLinkedGroupDetails();
        assertEquals(2L,
                     updatedLinkedGroupDetails.getLinkedGroupLatestVersion(),
                     "Linked group details has unexpected version number");
        assertEquals("PENDING", updatedLinkedGroupDetails.getStatus(), "Linked group details has unexpected status");

        LinkedGroupDetailsAudit groupAudit = getLinkedGroupDetailsAudit();
        assertEquals(1L, groupAudit.getLinkedGroupVersion(), "Linked group audit has unexpected version");
        assertEquals("1", groupAudit.getRequestId(), "Linked group audit has unexpected request id");
        assertEquals("Ordered", groupAudit.getLinkType().getLabel(), "Linked group audit has unexpected link type");
        assertEquals("ACTIVE", groupAudit.getStatus(), "Linked group audit has unexpected status");
        assertNotNull(groupAudit.getRequestDateTime(), "Linked group audit request date time should not be null");

        assertHearingDetailsAudit(HEARING_ID_1, 1L);
        assertHearingDetailsAudit(HEARING_ID_2, 2L);
    }

    private void assertHearingDetailsAudit(long hearingId, long expectedHearingOrder) {
        LinkedHearingDetailsAudit hearingDetailsAudit = getLinkedHearingDetailsAudit(hearingId);
        assertEquals(1L,
                     hearingDetailsAudit.getLinkedGroupVersion(),
                     "Linked hearing audit for hearing " + hearingId + " has unexpected linked group version");
        assertEquals(expectedHearingOrder,
                     hearingDetailsAudit.getLinkedOrder(),
                     "Linked hearing audit for hearing " + hearingId + " has unexpected linked order");
    }

    private LinkedGroupDetails getLinkedGroupDetails() {
        Optional<LinkedGroupDetails> linkedGroupDetailsOptional =
            linkedGroupDetailsRepository.findById(LINKED_HEARING_GROUP_ID);
        assertTrue(linkedGroupDetailsOptional.isPresent(), "Linked group details should exist");
        return linkedGroupDetailsOptional.get();
    }

    private LinkedGroupDetailsAudit getLinkedGroupDetailsAudit() {
        String query = "SELECT lgda FROM LinkedGroupDetailsAudit lgda WHERE lgda.linkedGroup.linkedGroupId = "
            + LINKED_HEARING_GROUP_ID;
        return entityManager.createQuery(query, LinkedGroupDetailsAudit.class).getSingleResult();
    }

    private LinkedHearingDetailsAudit getLinkedHearingDetailsAudit(long hearingId) {
        String query = "SELECT lhda FROM LinkedHearingDetailsAudit lhda WHERE lhda.hearing.id = " + hearingId;
        return entityManager.createQuery(query, LinkedHearingDetailsAudit.class).getSingleResult();
    }
}
