package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FhBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;

import java.util.ArrayList;
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
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPostCreateLinkHearingGroup;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPostCreateLinkHearingGroupReturn400;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPostCreateLinkHearingGroupReturn500;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPutUpdateLinkHearingGroup;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyDeleteLinkedHearingGroups;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UPDATE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

class LinkedHearingGroupServiceIT extends BaseTest {

    private final LinkedHearingGroupService linkedHearingGroupService;

    private final LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    private final HearingRepository hearingRepository;

    private final EntityManager entityManager;

    private final LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;

    private final ObjectMapper objectMapper;

    private static final String INSERT_HEARINGS_FOR_LINKING_SCRIPT = "classpath:sql/insert-hearings-for-linking.sql";
    private static final String INSERT_LINKED_HEARINGS_FOR_AMENDING_SCRIPT
        = "classpath:sql/insert-linked-hearings-for-amending.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";
    public static final String REQUEST_ID2 = "12345";
    public static final String TOKEN = "example-token";

    @Autowired
    public LinkedHearingGroupServiceIT(LinkedHearingGroupService linkedHearingGroupService,
                                       LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                       HearingRepository hearingRepository,
                                       EntityManager entityManager,
                                       LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository) {
        this.linkedHearingGroupService = linkedHearingGroupService;
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.hearingRepository = hearingRepository;
        this.entityManager = entityManager;
        this.linkedHearingStatusAuditRepository = linkedHearingStatusAuditRepository;

        objectMapper = new ObjectMapper();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_FOR_LINKING_SCRIPT})
    void testCreateLinkedHearingGroup_LinkedGroupDetails() {
        stubPostCreateLinkHearingGroup(202, "/resources/linked-hearing-group", TOKEN);

        List<Long> hearingIdsToLink = List.of(2000000000L, 2000000001L);
        HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupOrderedRequest(hearingIdsToLink);

        HearingLinkGroupResponse response = linkedHearingGroupService.linkHearing(hearingLinkGroupRequest, HMC);

        assertHearingLinked(2000000000L, 1L, hearingLinkGroupRequest.getGroupDetails(), 1L);
        assertHearingLinked(2000000001L, 2L, hearingLinkGroupRequest.getGroupDetails(), 1L);

        HearingEntity hearing = getHearingAndLinkedGroup(2000000000L);

        String linkedGroupRequestId = hearing.getLinkedGroupDetails().getRequestId();
        assertEquals(String.valueOf(linkedGroupRequestId),
                     response.getHearingGroupRequestId(),
                     "Returned linked group id does not match expected linked group id");

        Long linkedGroupId = hearing.getLinkedGroupDetails().getLinkedGroupId();

        LinkedHearingStatusAuditEntity hearingStatusAuditEntryForLinkedGroupId = new LinkedHearingStatusAuditEntity();
        hearingStatusAuditEntryForLinkedGroupId.setLinkedGroupId(String.valueOf(linkedGroupId));
        List<LinkedHearingStatusAuditEntity> linkedHearingStatusAuditList =
            linkedHearingStatusAuditRepository.findAll(Example.of(hearingStatusAuditEntryForLinkedGroupId),
                                                       Sort.by(Sort.Direction.ASC, "id"));

        assertEquals(2,
                     linkedHearingStatusAuditList.size(),
                     "Unexpected number of linked hearing status audit entities");

        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.getFirst(),
            new ExpectedLinkedHearingStatusAudit(
                linkedGroupId, 1L, CREATE_LINKED_HEARING_REQUEST, null, HMC, FH, null, hearingIdsToLink
            )
        );
        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.get(1),
            new ExpectedLinkedHearingStatusAudit(
                linkedGroupId, 1L, CREATE_LINKED_HEARING_REQUEST, 200, FH, HMC, null, hearingIdsToLink
            )
        );
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_FOR_LINKING_SCRIPT})
    void testCreateLinkedHearingGroup_LinkedGroupDetails400Error() {
        stubPostCreateLinkHearingGroupReturn400(2000000000L, TOKEN);

        List<Long> hearingIdsToLink = List.of(2000000000L, 2000000001L);
        HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupOrderedRequest(hearingIdsToLink);

        BadRequestException exception =
            assertThrows(BadRequestException.class,
                         () -> linkedHearingGroupService.linkHearing(hearingLinkGroupRequest, HMC));

        assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage(), "Exception has unexpected message");

        hearingIdsToLink.forEach(this::assertHearingNotLinked);

        List<LinkedHearingStatusAuditEntity> linkedHearingStatusAuditList = getAllLinkedHearingStatusAuditEntities();
        assertEquals(2,
                     linkedHearingStatusAuditList.size(),
                     "Unexpected number of linked hearing status audit entities");

        assertLinkedHearingStatusAuditExcludingGroupId(
            linkedHearingStatusAuditList.getFirst(),
            new ExpectedLinkedHearingStatusAudit(
                null, 1L, CREATE_LINKED_HEARING_REQUEST, null, HMC, FH, null, hearingIdsToLink
            )
        );
        JsonNode errorRejectedByListAssist = convertErrorDescriptionToJsonNode(REJECTED_BY_LIST_ASSIST);
        assertLinkedHearingStatusAuditExcludingGroupId(
            linkedHearingStatusAuditList.get(1),
            new ExpectedLinkedHearingStatusAudit(
                null, 1L, CREATE_LINKED_HEARING_REQUEST, 400, FH, HMC, errorRejectedByListAssist, hearingIdsToLink
            )
        );

        // Confirm that group referenced by linked hearing status audit entity has been deleted
        String requestId = linkedHearingStatusAuditList.getFirst().getLinkedGroupId();
        LinkedGroupDetails linkedGroupDetails =
            linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        assertNull(linkedGroupDetails);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_FOR_LINKING_SCRIPT})
    void testCreateLinkedHearingGroup_LinkedGroupDetails500Error() {
        stubPostCreateLinkHearingGroupReturn500(TOKEN);

        List<Long> hearingIdsToLink = List.of(2000000000L, 2000000001L);
        HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupOrderedRequest(hearingIdsToLink);

        FhBadRequestException exception =
            assertThrows(FhBadRequestException.class,
                         () -> linkedHearingGroupService.linkHearing(hearingLinkGroupRequest, HMC));

        assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage(), "Exception has unexpected message");

        hearingIdsToLink.forEach(this::assertHearingNotLinked);

        List<LinkedHearingStatusAuditEntity> linkedHearingStatusAuditList = getAllLinkedHearingStatusAuditEntities();
        assertEquals(2,
                     linkedHearingStatusAuditList.size(),
                     "Unexpected number of linked hearing status audit entities");

        assertLinkedHearingStatusAuditExcludingGroupId(
            linkedHearingStatusAuditList.getFirst(),
            new ExpectedLinkedHearingStatusAudit(
                null, 1L, CREATE_LINKED_HEARING_REQUEST, null, HMC, FH, null, hearingIdsToLink
            )
        );
        JsonNode errorLaFailedToRespond = convertErrorDescriptionToJsonNode(LIST_ASSIST_FAILED_TO_RESPOND);
        assertLinkedHearingStatusAuditExcludingGroupId(
            linkedHearingStatusAuditList.get(1),
            new ExpectedLinkedHearingStatusAudit(
                null, 1L, CREATE_LINKED_HEARING_REQUEST, 500, FH, HMC, errorLaFailedToRespond, hearingIdsToLink
            )
        );

        String requestId = linkedHearingStatusAuditList.getFirst().getLinkedGroupId();
        LinkedGroupDetails linkedGroupDetails =
            linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        assertNull(linkedGroupDetails);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_FOR_AMENDING_SCRIPT})
    void testAmendLinkedHearingGroup_LinkedGroupDetails() {
        stubPutUpdateLinkHearingGroup(200, "100000", TOKEN);

        List<Long> newHearingIds = List.of(2000000002L, 2000000003L);
        HearingLinkGroupRequest hearingLinkGroupRequest =
            createHearingLinkGroupOrderedRequest("Updated group name",
                                                 "Updated group reason",
                                                 "Updated group comments",
                                                 newHearingIds);

        linkedHearingGroupService.updateLinkHearing("100000", hearingLinkGroupRequest, HMC);

        List<Long> oldHearingIds = List.of(2000000000L, 2000000001L);
        oldHearingIds.forEach(this::assertHearingNotLinked);

        assertHearingLinked(2000000002L, 1L, hearingLinkGroupRequest.getGroupDetails(), 2L);
        assertHearingLinked(2000000003L, 2L, hearingLinkGroupRequest.getGroupDetails(), 2L);

        assertLinkedGroupDetailsAudit(hearingLinkGroupRequest.getGroupDetails());

        assertLinkedHearingDetailsAudit(2000000002L, 1L);
        assertLinkedHearingDetailsAudit(2000000003L, 2L);

        List<LinkedHearingStatusAuditEntity> linkedHearingStatusAuditList = getAllLinkedHearingStatusAuditEntities();
        assertEquals(2,
                     linkedHearingStatusAuditList.size(),
                     "Unexpected number of linked hearing status audit entities");

        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.getFirst(),
            new ExpectedLinkedHearingStatusAudit(
                100000L, 2L, UPDATE_LINKED_HEARING_REQUEST, null, HMC, FH, null, oldHearingIds
            )
        );
        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.get(1),
            new ExpectedLinkedHearingStatusAudit(
                100000L, 2L, UPDATE_LINKED_HEARING_REQUEST, 200, FH, HMC, null, oldHearingIds
            )
        );
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_FOR_AMENDING_SCRIPT})
    void testAmendLinkedHearingGroup_LinkedGroupDetails400Error() {
        stubPutUpdateLinkHearingGroup(400, "100000", TOKEN);

        List<Long> newHearingIds = List.of(2000000002L, 2000000003L);
        HearingLinkGroupRequest hearingLinkGroupRequest =
            createHearingLinkGroupOrderedRequest("Updated group name",
                                                 "Updated group reason",
                                                 "Updated group comments",
                                                 newHearingIds);

        BadRequestException exception =
            assertThrows(BadRequestException.class,
                         () -> linkedHearingGroupService.updateLinkHearing("100000", hearingLinkGroupRequest, HMC));

        assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage(), "Exception has unexpected message");

        newHearingIds.forEach(this::assertHearingNotLinked);

        GroupDetails currentGroupDetails = createGroupDetailsOrdered("Group name", "Group reason", "Group comments");
        final List<Long> currentHearingIds = List.of(2000000000L, 2000000001L);

        assertHearingLinked(2000000000L, 1L, currentGroupDetails, 1L);
        assertHearingLinked(2000000001L, 2L, currentGroupDetails, 1L);

        LinkedGroupDetailsAudit linkedGroupDetailsAudit = getLinkedGroupDetailsAudit();
        assertNull(linkedGroupDetailsAudit, "Linked group details audit should be deleted");

        newHearingIds.forEach(hearingId ->
                                  assertNull(getLinkedHearingDetailsAudit(hearingId),
                                             "Linked hearing details audit should be deleted ")
        );

        List<LinkedHearingStatusAuditEntity> linkedHearingStatusAuditList = getAllLinkedHearingStatusAuditEntities();
        assertEquals(2,
                     linkedHearingStatusAuditList.size(),
                     "Unexpected number of linked hearing status audit entities");

        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.getFirst(),
            new ExpectedLinkedHearingStatusAudit(
                100000L, 2L, UPDATE_LINKED_HEARING_REQUEST, null, HMC, FH, null, currentHearingIds
            )
        );
        JsonNode errorRejectedByListAssist = convertErrorDescriptionToJsonNode(REJECTED_BY_LIST_ASSIST);
        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.get(1),
            new ExpectedLinkedHearingStatusAudit(
                100000L, 2L, UPDATE_LINKED_HEARING_REQUEST, 400, FH, HMC, errorRejectedByListAssist, currentHearingIds
            )
        );
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_FOR_AMENDING_SCRIPT})
    void testAmendLinkedHearingGroup_LinkedGroupDetails500Error() {
        stubPutUpdateLinkHearingGroup(500, "100000", TOKEN);

        List<Long> newHearingIds = List.of(2000000002L, 2000000003L);
        HearingLinkGroupRequest hearingLinkGroupRequest =
            createHearingLinkGroupOrderedRequest("Updated group name",
                                                 "Updated group reason",
                                                 "Updated group comments",
                                                 newHearingIds);

        BadRequestException exception =
            assertThrows(BadRequestException.class,
                         () -> linkedHearingGroupService.updateLinkHearing("100000", hearingLinkGroupRequest, HMC));

        assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage(), "Exception has unexpected message");

        newHearingIds.forEach(this::assertHearingNotLinked);

        GroupDetails currentGroupDetails = createGroupDetailsOrdered("Group name", "Group reason", "Group comments");
        final List<Long> currentHearingIds = List.of(2000000000L, 2000000001L);

        assertHearingLinked(2000000000L, 1L, currentGroupDetails, 1L);
        assertHearingLinked(2000000001L, 2L, currentGroupDetails, 1L);

        LinkedGroupDetailsAudit linkedGroupDetailsAudit = getLinkedGroupDetailsAudit();
        assertNull(linkedGroupDetailsAudit, "Linked group details audit should be deleted");

        newHearingIds.forEach(hearingId ->
                                  assertNull(getLinkedHearingDetailsAudit(hearingId),
                                             "Linked hearing details audit should be deleted ")
        );

        List<LinkedHearingStatusAuditEntity> linkedHearingStatusAuditList = getAllLinkedHearingStatusAuditEntities();
        assertEquals(2,
                     linkedHearingStatusAuditList.size(),
                     "Unexpected number of linked hearing status audit entities");

        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.getFirst(),
            new ExpectedLinkedHearingStatusAudit(
                100000L, 2L, UPDATE_LINKED_HEARING_REQUEST, null, HMC, FH, null, currentHearingIds
            )
        );
        JsonNode errorLaFailedToRespond = convertErrorDescriptionToJsonNode(LIST_ASSIST_FAILED_TO_RESPOND);
        assertLinkedHearingStatusAudit(
            linkedHearingStatusAuditList.get(1),
            new ExpectedLinkedHearingStatusAudit(
                100000L, 2L, UPDATE_LINKED_HEARING_REQUEST, 500, FH, HMC, errorLaFailedToRespond, currentHearingIds
            )
        );
    }

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
        assertEquals(Collections.emptyList(), details);
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
        assertEquals(Collections.emptyList(), details);
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
        assertEquals(Collections.emptyList(), details);
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
        assertEquals(Collections.emptyList(), details);
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

    private HearingLinkGroupRequest createHearingLinkGroupOrderedRequest(List<Long> hearings) {
        return createHearingLinkGroupOrderedRequest("Group name", "Group reason", "Group comments", hearings);
    }

    private HearingLinkGroupRequest createHearingLinkGroupOrderedRequest(String name,
                                                                         String reason,
                                                                         String comments,
                                                                         List<Long> hearings) {
        GroupDetails groupDetails = createGroupDetailsOrdered(name, reason, comments);

        List<LinkHearingDetails> hearingsInGroup = new ArrayList<>();
        hearings.forEach(hearing -> hearingsInGroup
            .add(new LinkHearingDetails(String.valueOf(hearing), hearingsInGroup.size() + 1))
        );

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setGroupDetails(groupDetails);
        hearingLinkGroupRequest.setHearingsInGroup(hearingsInGroup);

        return hearingLinkGroupRequest;
    }

    private GroupDetails createGroupDetailsOrdered(String name, String reason, String comments) {
        GroupDetails groupDetails = new GroupDetails();

        groupDetails.setGroupName(name);
        groupDetails.setGroupReason(reason);
        groupDetails.setGroupLinkType("Ordered");
        groupDetails.setGroupComments(comments);

        return groupDetails;
    }

    private void assertHearingLinked(Long hearingId,
                                     Long expectedLinkedOrder,
                                     GroupDetails expectedGroupDetails,
                                     Long expectedGroupVersion) {
        String hearingErrorMessagePrefix = "Hearing " + hearingId + " ";

        HearingEntity hearing = getHearingAndLinkedGroup(hearingId);
        assertNotNull(hearing, hearingErrorMessagePrefix + "should exist");

        assertEquals(expectedLinkedOrder,
                     hearing.getLinkedOrder(),
                     hearingErrorMessagePrefix + "has unexpected linked order");

        LinkedGroupDetails linkedGroupDetails = hearing.getLinkedGroupDetails();
        assertNotNull(linkedGroupDetails, hearingErrorMessagePrefix + "should have linked group details");

        String linkedGroupErrorMessagePrefix = "Hearing " + hearingId + " linked group details ";

        assertEquals(expectedGroupVersion,
                     linkedGroupDetails.getLinkedGroupLatestVersion(),
                     linkedGroupErrorMessagePrefix + "has unexpected version");
        assertEquals(expectedGroupDetails.getGroupName(),
                     linkedGroupDetails.getRequestName(),
                     linkedGroupErrorMessagePrefix + "has unexpected request name");
        assertEquals(expectedGroupDetails.getGroupReason(),
                     linkedGroupDetails.getReasonForLink(),
                     linkedGroupErrorMessagePrefix + "has unexpected reason for link");
        assertEquals(LinkType.getByLabel(expectedGroupDetails.getGroupLinkType()),
                     linkedGroupDetails.getLinkType(),
                     linkedGroupErrorMessagePrefix + "has unexpected link type");
        assertEquals(expectedGroupDetails.getGroupComments(),
                     linkedGroupDetails.getLinkedComments(),
                     linkedGroupErrorMessagePrefix + "has unexpected linked comments");
        assertEquals("ACTIVE", linkedGroupDetails.getStatus(), linkedGroupErrorMessagePrefix + "has unexpected status");
        assertNotNull(linkedGroupDetails.getRequestDateTime(),
                      linkedGroupErrorMessagePrefix + "request date time should not be null");
    }

    private void assertHearingNotLinked(Long hearingId) {
        String errorMessagePrefix = "Hearing " + hearingId + " ";

        Optional<HearingEntity> hearing = hearingRepository.findById(hearingId);
        assertTrue(hearing.isPresent(), errorMessagePrefix + "should be present");

        HearingEntity presentHearing = hearing.get();
        assertNull(presentHearing.getLinkedOrder(), errorMessagePrefix + "should not have a linked order");
        assertNull(presentHearing.getLinkedGroupDetails(), errorMessagePrefix + "should not have link group details");
    }

    private void assertLinkedHearingStatusAudit(LinkedHearingStatusAuditEntity linkedHearingStatusAudit,
                                                ExpectedLinkedHearingStatusAudit expectedLinkedHearingStatusAudit) {
        assertEquals(String.valueOf(expectedLinkedHearingStatusAudit.groupId()),
                     linkedHearingStatusAudit.getLinkedGroupId(),
                     "Linked hearing status audit has unexpected linked group id");

        assertLinkedHearingStatusAuditExcludingGroupId(linkedHearingStatusAudit, expectedLinkedHearingStatusAudit);
    }

    private void assertLinkedHearingStatusAuditExcludingGroupId(
        LinkedHearingStatusAuditEntity linkedHearingStatusAudit,
        ExpectedLinkedHearingStatusAudit expectedLinkedHearingStatusAudit) {
        assertEquals("ABA1",
                     linkedHearingStatusAudit.getHmctsServiceId(),
                     "Linked hearing status audit has unexpected HMCTS serivce id");
        assertEquals(String.valueOf(expectedLinkedHearingStatusAudit.version()),
                     linkedHearingStatusAudit.getLinkedGroupVersion(),
                     "Linked hearing status audit has unexpected linked group version");
        assertNotNull(linkedHearingStatusAudit.getLinkedHearingEventDateTime(),
                      "Linked hearing status audit should have a linked hearing event date/time");
        assertEquals(expectedLinkedHearingStatusAudit.event(),
                     linkedHearingStatusAudit.getLinkedHearingEvent(),
                     "Linked hearing status audit has unexpected linked hearing event");

        Integer httpStatus = expectedLinkedHearingStatusAudit.httpStatus();
        if (httpStatus == null) {
            assertNull(linkedHearingStatusAudit.getHttpStatus(),
                       "Linked hearing status audit HTTP status should be null");
        } else {
            assertEquals(String.valueOf(httpStatus),
                         linkedHearingStatusAudit.getHttpStatus(),
                         "Linked hearing status audit has unexpected HTTP status");
        }

        assertEquals(expectedLinkedHearingStatusAudit.source(),
                     linkedHearingStatusAudit.getSource(),
                     "Linked hearing status audit has unexpected source");
        assertEquals(expectedLinkedHearingStatusAudit.target(),
                     linkedHearingStatusAudit.getTarget(),
                     "Linked hearing status audit has unexpected destination");

        JsonNode errorDescription = expectedLinkedHearingStatusAudit.errorDescription();
        if (errorDescription == null) {
            assertNull(linkedHearingStatusAudit.getErrorDescription(),
                       "Linked hearing status audit error description should be null");
        } else {
            assertEquals(errorDescription,
                         linkedHearingStatusAudit.getErrorDescription(),
                         "Linked hearing status audit has unexpected error description");
        }

        assertEquals(convertHearingIdsToJsonNode(expectedLinkedHearingStatusAudit.hearingIds()),
                     linkedHearingStatusAudit.getLinkedGroupHearings(),
                     "Linked hearing status audit has unexpected linked group hearings");
    }

    private void assertLinkedGroupDetailsAudit(GroupDetails expectedGroupDetails) {
        LinkedGroupDetailsAudit linkedGroupDetailsAudit = getLinkedGroupDetailsAudit();
        assertNotNull(linkedGroupDetailsAudit,
                      "Linked group details audit should exist for linked group 100000");

        assertEquals(2L,
                     linkedGroupDetailsAudit.getLinkedGroupVersion(),
                     "Linked group details audit has unexpected group version");
        assertEquals(expectedGroupDetails.getGroupComments(),
                     linkedGroupDetailsAudit.getLinkedComments(),
                     "Linked group details audit has unexpected linked comments");
        assertEquals(LinkType.getByLabel(expectedGroupDetails.getGroupLinkType()),
                     linkedGroupDetailsAudit.getLinkType(),
                     "Linked group details audit has unexpected link type");
        assertEquals(expectedGroupDetails.getGroupName(),
                     linkedGroupDetailsAudit.getRequestName(),
                     "Linked group details audit has unexpected request name");
        assertEquals(expectedGroupDetails.getGroupReason(),
                     linkedGroupDetailsAudit.getReasonForLink(),
                     "Linked group details audit has unexpected reason for link");
        assertNotNull(linkedGroupDetailsAudit.getRequestDateTime(),
                      "Linked group details audit request date time should not be null");
        assertEquals("PENDING",
                     linkedGroupDetailsAudit.getStatus(),
                     "Linked group details audit status has unexpected value");
    }

    private void assertLinkedHearingDetailsAudit(Long hearingId, Long expectedLinkedOrder) {
        String errorMessagePrefix = "Linked hearing details audit for hearing " + hearingId + " ";

        LinkedHearingDetailsAudit linkedHearingDetailsAudit = getLinkedHearingDetailsAudit(hearingId);
        assertNotNull(linkedHearingDetailsAudit, errorMessagePrefix + "should exist");

        assertEquals(100000L,
                     linkedHearingDetailsAudit.getLinkedGroup().getLinkedGroupId(),
                     errorMessagePrefix + "has unexpected linked group id");
        assertEquals(2L,
                     linkedHearingDetailsAudit.getLinkedGroupVersion(),
                     errorMessagePrefix + "has unexpected linked group version");
        assertEquals(expectedLinkedOrder,
                     linkedHearingDetailsAudit.getLinkedOrder(),
                     errorMessagePrefix + "has unexpected linked order");
    }

    private JsonNode convertHearingIdsToJsonNode(List<Long> hearingIds) {
        // Convert hearing ids to strings first as they are stored as strings
        List<String> hearingIdsStringList = hearingIds.stream().map(String::valueOf).toList();
        return objectMapper.valueToTree(hearingIdsStringList);
    }

    private JsonNode convertErrorDescriptionToJsonNode(String errorDescription) {
        return objectMapper.convertValue(errorDescription, JsonNode.class);
    }

    private List<LinkedHearingStatusAuditEntity> getAllLinkedHearingStatusAuditEntities() {
        return linkedHearingStatusAuditRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    private HearingEntity getHearingAndLinkedGroup(Long hearingId) {
        // Use JOIN FETCH to ensure linked group is retrieved.  Without this lazy loading prevents linked
        // group being retrieved which causes a Hibernate session error when attempting to check details of it.
        String query = "SELECT h FROM HearingEntity h JOIN FETCH h.linkedGroupDetails WHERE h.id = " + hearingId;
        return entityManager.createQuery(query, HearingEntity.class).getSingleResult();
    }

    private LinkedGroupDetailsAudit getLinkedGroupDetailsAudit() {
        String query =
            "SELECT lgda FROM LinkedGroupDetailsAudit lgda WHERE lgda.linkedGroup.linkedGroupId = 100000";
        try {
            return entityManager.createQuery(query, LinkedGroupDetailsAudit.class).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private LinkedHearingDetailsAudit getLinkedHearingDetailsAudit(Long hearingId) {
        String query = "SELECT lhda FROM LinkedHearingDetailsAudit lhda WHERE lhda.hearing.id = " + hearingId;
        try {
            return entityManager.createQuery(query, LinkedHearingDetailsAudit.class).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
        if (!details.isEmpty()) {
            assertNotNull(details.get(0).getLinkedGroupHearings());
            assertEquals("ABA1", details.get(0).getHmctsServiceId());
        }
        return details;
    }

    private record ExpectedLinkedHearingStatusAudit(Long groupId,
                                                    Long version,
                                                    String event,
                                                    Integer httpStatus,
                                                    String source,
                                                    String target,
                                                    JsonNode errorDescription,
                                                    List<Long> hearingIds) {}
}
