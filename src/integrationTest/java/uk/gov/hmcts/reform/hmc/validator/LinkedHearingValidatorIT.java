package uk.gov.hmcts.reform.hmc.validator;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedHearingValidatorIT extends BaseTest {

    private static final Long HEARING_ID_ONE = 2000000000L;
    private static final Long HEARING_ID_TWO = 2000000001L;

    private static final Long HEARING_ID_DOES_NOT_EXIST_ONE = 2000000002L;
    private static final Long HEARING_ID_DOES_NOT_EXIST_TWO = 2000000003L;

    private static final String SCRIPT_DELETE_HEARING_TABLES = "classpath:sql/delete-hearing-tables.sql";
    private static final String SCRIPT_INSERT_HEARINGS_FOR_LINKING = "classpath:sql/insert-hearings-for-linking.sql";

    private final EntityManager entityManager;

    private final HearingRepository hearingRepository;

    private final LinkedHearingValidator linkedHearingValidator;

    @Autowired
    public LinkedHearingValidatorIT(EntityManager entityManager,
                                    HearingRepository hearingRepository,
                                    LinkedHearingValidator linkedHearingValidator) {
        this.entityManager = entityManager;
        this.hearingRepository = hearingRepository;
        this.linkedHearingValidator = linkedHearingValidator;
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARINGS_FOR_LINKING})
    void updateHearingWithLinkGroup_ShouldCreateGroupAndUpdateHearings() {
        List<LinkHearingDetails> hearingsInGroup = List.of(new LinkHearingDetails(String.valueOf(HEARING_ID_ONE), 1),
                                                           new LinkHearingDetails(String.valueOf(HEARING_ID_TWO), 2));
        HearingLinkGroupRequest request = createHearingLinkGroupRequest(hearingsInGroup);

        LinkedGroupDetails linkedGroupDetails = linkedHearingValidator.updateHearingWithLinkGroup(request);

        assertLinkedGroupDetails(linkedGroupDetails);
        assertHearingLinkedOrderAndGroup(HEARING_ID_ONE, 1L, linkedGroupDetails);
        assertHearingLinkedOrderAndGroup(HEARING_ID_TWO, 2L, linkedGroupDetails);
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARINGS_FOR_LINKING})
    void updateHearingWithLinkGroup_ShouldCreateGroupAndNotUpdateHearings() {
        List<LinkHearingDetails> hearingsInGroup =
            List.of(new LinkHearingDetails(String.valueOf(HEARING_ID_DOES_NOT_EXIST_ONE), 1),
                    new LinkHearingDetails(String.valueOf(HEARING_ID_DOES_NOT_EXIST_TWO), 2));
        HearingLinkGroupRequest request = createHearingLinkGroupRequest(hearingsInGroup);

        LinkedGroupDetails linkedGroupDetails = linkedHearingValidator.updateHearingWithLinkGroup(request);

        assertFalse(hearingRepository.existsById(HEARING_ID_DOES_NOT_EXIST_ONE),
                    "Hearing " + HEARING_ID_DOES_NOT_EXIST_ONE + " should not exist");
        assertFalse(hearingRepository.existsById(HEARING_ID_DOES_NOT_EXIST_TWO),
                    "Hearing " + HEARING_ID_DOES_NOT_EXIST_TWO + " should not exist");

        assertLinkedGroupDetails(linkedGroupDetails);
        assertHearingNotLinked(HEARING_ID_ONE);
        assertHearingNotLinked(HEARING_ID_TWO);
    }

    private HearingLinkGroupRequest createHearingLinkGroupRequest(List<LinkHearingDetails> hearingsInGroup) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupName("name");
        groupDetails.setGroupReason("GR");
        groupDetails.setGroupLinkType("ORDERED");
        groupDetails.setGroupComments("comment");

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setGroupDetails(groupDetails);
        hearingLinkGroupRequest.setHearingsInGroup(hearingsInGroup);

        return hearingLinkGroupRequest;
    }

    private void assertLinkedGroupDetails(LinkedGroupDetails linkedGroupDetails) {
        String errorMessagePrefix = "Linked group details ";

        assertNotNull(linkedGroupDetails, errorMessagePrefix + "should not be null");

        assertNotNull(linkedGroupDetails.getLinkedGroupId(), errorMessagePrefix + "linked group id should not be null");
        assertNotNull(linkedGroupDetails.getRequestId(), errorMessagePrefix + "request id should not be null");
        assertEquals("name",
                     linkedGroupDetails.getRequestName(),
                     errorMessagePrefix + "request name has unexpected value");
        assertEquals("GR",
                     linkedGroupDetails.getReasonForLink(),
                     errorMessagePrefix + "reason for link has unexpected value");
        assertEquals(LinkType.ORDERED,
                     linkedGroupDetails.getLinkType(),
                     errorMessagePrefix + "link type has unexpected value");
        assertEquals("comment",
                     linkedGroupDetails.getLinkedComments(),
                     errorMessagePrefix + "linked comments has unexpected value");
        assertEquals("PENDING",
                     linkedGroupDetails.getStatus(),
                     errorMessagePrefix + "status has unexpected value");
        assertNotNull(linkedGroupDetails.getRequestDateTime(),
                      errorMessagePrefix + "request date time should not be null");
        assertEquals(1L,
                     linkedGroupDetails.getLinkedGroupLatestVersion(),
                     errorMessagePrefix + "linked group latest version has unexpected value");
        assertNotNull(linkedGroupDetails.getCreatedDateTime(),
                      errorMessagePrefix + "created date time should not be null");
    }

    private void assertHearingLinkedOrderAndGroup(Long hearingId,
                                                  Long expectedLinkedOrder,
                                                  LinkedGroupDetails expectedLinkedGroupDetails) {
        String hearingErrorMessagePrefix = "Hearing " + hearingId + " ";
        String linkedGroupDetailsErrorMessagePrefix = "Hearing " + hearingId + " linked group details ";

        HearingEntity hearing = getHearingAndLinkedGroup(hearingId);
        assertNotNull(hearing, hearingErrorMessagePrefix + "should exist");

        assertEquals(expectedLinkedOrder,
                     hearing.getLinkedOrder(),
                     hearingErrorMessagePrefix + "has unexpected linked order");

        LinkedGroupDetails hearingLinkedGroupDetails = hearing.getLinkedGroupDetails();
        assertNotNull(hearingLinkedGroupDetails, linkedGroupDetailsErrorMessagePrefix + "should not be null");

        assertEquals(expectedLinkedGroupDetails.getLinkedGroupId(),
                     hearingLinkedGroupDetails.getLinkedGroupId(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected linked group id");
        assertEquals(expectedLinkedGroupDetails.getRequestId(),
                     hearingLinkedGroupDetails.getRequestId(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected request id");
        assertEquals(expectedLinkedGroupDetails.getRequestName(),
                     hearingLinkedGroupDetails.getRequestName(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected request name");
        assertEquals(expectedLinkedGroupDetails.getReasonForLink(),
                     hearingLinkedGroupDetails.getReasonForLink(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected reason for link");
        assertEquals(expectedLinkedGroupDetails.getLinkType(),
                     hearingLinkedGroupDetails.getLinkType(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected link type");
        assertEquals(expectedLinkedGroupDetails.getLinkedComments(),
                     hearingLinkedGroupDetails.getLinkedComments(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected linked comments");
        assertEquals(expectedLinkedGroupDetails.getStatus(),
                     hearingLinkedGroupDetails.getStatus(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected status");
        assertEquals(expectedLinkedGroupDetails.getRequestDateTime(),
                     hearingLinkedGroupDetails.getRequestDateTime(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected request date time");
        assertEquals(expectedLinkedGroupDetails.getLinkedGroupLatestVersion(),
                     hearingLinkedGroupDetails.getLinkedGroupLatestVersion(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected linked group latest version");
        assertEquals(expectedLinkedGroupDetails.getCreatedDateTime(),
                     hearingLinkedGroupDetails.getCreatedDateTime(),
                     linkedGroupDetailsErrorMessagePrefix + "has unexpected created date time");
    }

    private void assertHearingNotLinked(Long hearingId) {
        String errorMessagePrefix = "Hearing " + hearingId + " ";

        Optional<HearingEntity> hearingOptional = hearingRepository.findById(hearingId);
        assertTrue(hearingOptional.isPresent(), errorMessagePrefix + "should exist");

        HearingEntity hearing = hearingOptional.get();
        assertNull(hearing.getLinkedOrder(), errorMessagePrefix + "should not have a linked order");
        assertNull(hearing.getLinkedGroupDetails(), errorMessagePrefix + "should not have linked group details");
    }

    private HearingEntity getHearingAndLinkedGroup(Long hearingId) {
        String query = "SELECT h FROM HearingEntity h JOIN FETCH h.linkedGroupDetails WHERE h.id = " + hearingId;
        return entityManager.createQuery(query, HearingEntity.class).getSingleResult();
    }
}
