package uk.gov.hmcts.reform.hmc.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingNotValidForUnlinkingException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

class LinkedHearingValidatorTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkedHearingValidatorTest.class);

    @InjectMocks
    private LinkedHearingValidator linkedHearingValidation;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    private LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linkedHearingValidation =
                new LinkedHearingValidator(hearingRepository, linkedGroupDetailsRepository,
                        linkedHearingDetailsRepository);
    }

    @Test
    void shouldFailAsRequestIdIsNull() {
        String requestId = null;
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingValidation
                .validateRequestId(requestId, null));
        assertEquals(LINKED_GROUP_ID_EMPTY, exception.getMessage());
    }

    @Test
    void shouldFailAsRequestIdDoesNotExist() {
        String requestId = "9176";
        String errorMessage = "This is a test error message";
        when(linkedGroupDetailsRepository.isFoundForRequestId(requestId)).thenReturn(0L);
        Exception exception = assertThrows(LinkedGroupNotFoundException.class, () ->
                linkedHearingValidation.validateRequestId(requestId, errorMessage));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldSucceedAsRequestIdDoesExist() {
        String requestId = "9176";
        String errorMessage = "This is a test error message";
        LinkedGroupDetails lgdExpected = generateLinkedGroupDetails(1L, PutHearingStatus.HEARING_REQUESTED.name(),
                "request1","request 1 in action");
        logger.info("lgdExpected: {}", lgdExpected);
        when(linkedGroupDetailsRepository.isFoundForRequestId(requestId)).thenReturn(1L);
        linkedHearingValidation.validateRequestId(requestId, errorMessage);
    }

    @Test
    void shouldFailAsObsoleteDetailsNotForDeleting() {
        String requestId = "9176";
        String requestName = "Special request";
        when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId)).thenReturn(null);
        List<LinkedHearingDetailsAudit> listLinkedHearingDetails =
                generateLinkedHearingDetailsList(requestId, requestName, 20L);
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.HEARING_REQUESTED.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(2L, hearing2,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(2L, "NO_DELETE");
        listLinkedHearingDetails.add(generateLinkedHearingDetails(3L, hearing3,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 3L));

        List<String> errorMessages = linkedHearingValidation.validateObsoleteLinkedHearings(listLinkedHearingDetails);
        assertFalse(errorMessages.isEmpty());
        assertEquals(1, errorMessages.size());
        assertEquals("008 Invalid state for unlinking hearing request 2", errorMessages.get(0));
    }

    @Test
    void shouldSucceedAsObsoleteDetailsAreForDeleting() {
        final String requestId = "9176";
        final String requestName = "Special request";

        List<LinkedHearingDetailsAudit> listLinkedHearingDetails =
                generateLinkedHearingDetailsList(requestId.toString(), requestName, 10L);
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.HEARING_REQUESTED.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(2L, hearing2,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(3L, hearing3,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 3L));

        List<LinkedHearingDetailsAudit> listObsolete = new ArrayList<>();
        listObsolete.add(listLinkedHearingDetails.get(0));

        when(linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId)).thenReturn(listObsolete);

        List<String> errorMessages = linkedHearingValidation.validateObsoleteLinkedHearings(listLinkedHearingDetails);
        assertTrue(errorMessages.isEmpty());
    }

    @Test
    void shouldExtractObsoleteDetailsForDeleting() {
        final String requestId = "89176";
        final String requestName = "compare and extract obsolete details";
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateValidLinkedHearingDetailsList(requestId, requestName, 7L);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "1",
                existingInDataList.get(1).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "3",
                existingInDataList.get(3).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "5",
                existingInDataList.get(5).getLinkedOrder().intValue()));

        List<LinkedHearingDetailsAudit> listObsoleteDetails =
            linkedHearingValidation.extractObsoleteLinkedHearings(payloadList, existingInDataList);
        assertFalse(listObsoleteDetails.isEmpty());
        assertEquals(3, listObsoleteDetails.size());
        assertTrue(listObsoleteDetails.contains(existingInDataList.get(1)));
        assertTrue(listObsoleteDetails.contains(existingInDataList.get(3)));
        assertTrue(listObsoleteDetails.contains(existingInDataList.get(5)));
    }

    @Test
    void shouldFailWithObsoleteDetailsNotValidForUnlinking() {
        final String requestId = "78176";
        final String requestName = "Find obsolete details ARE NOT not valid for unlinking";
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateLinkedHearingDetailsListWithBadStatus(requestId, requestName, 8L);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "7",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "8",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        HearingEntity hearing9 = generateHearing(9L, PutHearingStatus.LISTED.name());
        existingInDataList.add(generateLinkedHearingDetails(9L, hearing9,
                existingInDataList.get(0).getLinkedGroup(), 9L));

        when(linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId))
                .thenReturn(existingInDataList);

        Exception exception = assertThrows(LinkedHearingNotValidForUnlinkingException.class, () ->
                linkedHearingValidation.validateLinkedHearingsForUpdate(requestId, payloadList));
        final String expectedErrorMessage = "["
                + "008 Invalid state for unlinking hearing request 2, "
                + "008 Invalid state for unlinking hearing request 4, "
                + "008 Invalid state for unlinking hearing request 5, "
                + "008 Invalid state for unlinking hearing request 6"
                + "]";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void shouldPassWithObsoleteDetailsValidForUnlinking() {
        final String requestId = "181896";
        final String requestName = "Find obsolete details ARE valid for unlinking";
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateValidLinkedHearingDetailsList(requestId, requestName, 9L);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "7",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "8",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        HearingEntity hearing9 = generateHearing(9L, PutHearingStatus.LISTED.name());
        existingInDataList.add(generateLinkedHearingDetails(9L, hearing9,
                existingInDataList.get(0).getLinkedGroup(), 9L));

        when(linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId))
                .thenReturn(existingInDataList);

        linkedHearingValidation.validateLinkedHearingsForUpdate(requestId, payloadList);
    }

    @Nested
    @DisplayName("validateLinkedHearingGroup")
    class ValidateLinkedHearingGroup {

        @Test
        void shouldFailWithHearingNotFound() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(false);

            Exception exception = assertThrows(HearingNotFoundException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithInsufficientRequestIds() throws JsonProcessingException {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000000", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(hearingLinkGroupRequest);
            logger.info(json);
            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("001 Insufficient requestIds", exception.getMessage());
        }

        @Test
        void shouldFailWithHearingRequestIsLinkedIsFalse() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                DeleteHearingStatus.UPDATE_REQUESTED.name(),
                1,
                false,
                LocalDateTime.now(),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now())),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("002 hearing request isLinked is False", exception.getMessage());
        }

        @Test
        void shouldFailWithHearingRequestAlreadyInGroup() {
            LinkedGroupDetails groupDetails1 = generateLinkGroupDetails(
                200L,
                "requestId",
                "request name 1",
                "Same Slot",
                "status",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11)
            );
            LinkedGroupDetails groupDetailsAlternate = generateLinkGroupDetails(
                202L,
                "requestId2",
                "request name2",
                "Same Slot",
                "status",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11)
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                DeleteHearingStatus.UPDATE_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now(),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now())),
                groupDetails1
            );

            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkedHearingDetailsAudit hearingDetails1Data = new LinkedHearingDetailsAudit();
            hearingDetails1Data.setHearing(hearingEntity);
            hearingDetails1Data.setLinkedHearingDetailsAuditId(Long.parseLong(hearingDetails1.getHearingId()));
            hearingDetails1Data.setLinkedGroup(groupDetailsAlternate);

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));
            when(linkedHearingDetailsRepository.getLinkedHearingDetailsByHearingId(any()))
                .thenReturn(hearingDetails1Data);

            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );

            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("003 hearing request already in a group", exception.getMessage());
        }

        @Test
        void shouldFailWithInvalidState() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                "status",
                1,
                true,
                LocalDateTime.now(),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now())),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("004 Invalid state for hearing request 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithInvalidDate() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );


            HearingDayDetailsEntity hearingDayDetailsEntity =
                generateHearingDetailsEntity(
                    2000000002L,
                    LocalDateTime.of(2020, 11, 11, 12, 1)
                );
            HearingDayDetailsEntity hearingDayDetailsEntity1 =
                generateHearingDetailsEntity(
                    2000000002L,
                    LocalDateTime.of(2021, 11, 11, 12, 1)
                );
            HearingDayDetailsEntity hearingDayDetailsEntity2 =
                generateHearingDetailsEntity(
                    2000000000L,
                    LocalDateTime.of(2022, 11, 11, 12, 1)
                );


            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().minusDays(1),
                Arrays.asList(hearingDayDetailsEntity, hearingDayDetailsEntity1, hearingDayDetailsEntity2),
                null
            );
            // set the hearing window to prior to current date - invalid
            hearingEntity.getCaseHearingRequest().setHearingWindowStartDateRange(LocalDate.now().minusDays(2));
            hearingEntity.getCaseHearingRequest().setHearingWindowEndDateRange(LocalDate.now().plusDays(1));

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("004 Invalid state for hearing request 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithHearingOrderIsNotUnique() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("005 Hearing Order is not unique", exception.getMessage());
        }

        @Test
        void shouldFailWithNoHearingOrderWhenLinkTypeIsOrdered() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails = new LinkHearingDetails();
            hearingDetails.setHearingId("2000000000");
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertEquals("Hearing order must exist and be greater than 0", exception.getMessage());
        }

        @Test
        void shouldFailWithHearingOrderInvalidValue() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered One", "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            });
            assertTrue(exception.getMessage().startsWith("Invalid value"));
            assertTrue(exception.getMessage().contains("for GroupLinkType"));
        }

        @Test
        void shouldPassWhenHearingOrderIsSameSlot() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.SAME_SLOT.label, "reason"
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            verify(hearingRepository).existsById(2000000000L);
            verify(hearingRepository).findById(2000000000L);
            verify(hearingRepository).existsById(2000000002L);
            verify(hearingRepository).findById(2000000002L);
        }

        @Test
        void shouldPassWithValidLinkedHearing() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000000L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            logger.info("hearingLinkGroupRequest : {}", hearingLinkGroupRequest);
            linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            verify(hearingRepository).existsById(2000000000L);
            verify(hearingRepository).findById(2000000000L);
            verify(hearingRepository).existsById(2000000002L);
            verify(hearingRepository).findById(2000000002L);
        }

        @Test
        void shouldPassWithValidLinkedHearingWhenNoHearingResponse() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED.label, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingEntity hearingEntity = generateHearingEntityNoHearingResponse(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                null
            );

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            logger.info("hearingLinkGroupRequest : {}", hearingLinkGroupRequest);
            linkedHearingValidation.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
            verify(hearingRepository).existsById(2000000000L);
            verify(hearingRepository).findById(2000000000L);
            verify(hearingRepository).existsById(2000000002L);
            verify(hearingRepository).findById(2000000002L);
        }

    }

    private HearingLinkGroupRequest generateHearingLink(GroupDetails groupDetails,
                                                        List<LinkHearingDetails> hearingDetails) {

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setHearingsInGroup(hearingDetails);
        hearingLinkGroupRequest.setGroupDetails(groupDetails);

        return hearingLinkGroupRequest;
    }

    private GroupDetails generateGroupDetails(String groupComments, String groupName, String linkTypeLabel,
                                              String groupReason) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupComments(groupComments);
        groupDetails.setGroupName(groupName);
        groupDetails.setGroupLinkType(linkTypeLabel);
        groupDetails.setGroupReason(groupReason);
        return groupDetails;
    }

    private LinkHearingDetails generateHearingDetails(String hearingId, int order) {
        LinkHearingDetails hearingDetails = new LinkHearingDetails();
        hearingDetails.setHearingId(hearingId);
        hearingDetails.setHearingOrder(order);
        return hearingDetails;
    }

    private HearingEntity generateHearingEntity(Long hearingId, String status, Integer versionNumber,
                                                boolean isLinked, LocalDateTime requestTimestamp,
                                                List<HearingDayDetailsEntity> hearingDayDetailsEntities,
                                                LinkedGroupDetails linkedGroupDetails) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(hearingId);
        hearingEntity.setStatus(status);
        hearingEntity.setIsLinkedFlag(isLinked);
        hearingEntity.setLinkedGroupDetails(linkedGroupDetails);

        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
        caseHearingRequestEntity.setHearingWindowStartDateRange(LocalDate.now().plusDays(2));
        caseHearingRequestEntity.setHearingWindowEndDateRange(LocalDate.now().plusDays(4));
        caseHearingRequestEntity.setVersionNumber(versionNumber);

        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setHearingDayDetails(hearingDayDetailsEntities);
        hearingResponseEntity.setHearing(hearingEntity);
        hearingResponseEntity.setResponseVersion(versionNumber.toString());
        hearingResponseEntity.setRequestTimeStamp(requestTimestamp);

        hearingEntity.setHearingResponses(List.of(hearingResponseEntity));
        return hearingEntity;
    }


    private HearingEntity generateHearingEntityNoHearingResponse(Long hearingId, String status, Integer versionNumber,
                                                boolean isLinked,
                                                LinkedGroupDetails linkedGroupDetails) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(hearingId);
        hearingEntity.setStatus(status);
        hearingEntity.setIsLinkedFlag(isLinked);
        hearingEntity.setLinkedGroupDetails(linkedGroupDetails);

        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
        caseHearingRequestEntity.setHearingWindowStartDateRange(LocalDate.now().plusDays(2));
        caseHearingRequestEntity.setHearingWindowEndDateRange(LocalDate.now().plusDays(4));
        caseHearingRequestEntity.setVersionNumber(versionNumber);

        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    private HearingDayDetailsEntity generateHearingDetailsEntity(Long hearingId, LocalDateTime hearingDateTime) {
        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(hearingDateTime);
        hearingDayDetailsEntity.setHearingDayId(hearingId);
        return hearingDayDetailsEntity;
    }

    private LinkedGroupDetails generateLinkGroupDetails(Long linkGroupId, String requestId, String requestName,
                                                        String linkTypeLabel, String status, String reason,
                                                        String comments, LocalDateTime date) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupId(linkGroupId);
        linkedGroupDetails.setLinkType(LinkType.getByLabel(linkTypeLabel));
        linkedGroupDetails.setLinkedComments(comments);
        linkedGroupDetails.setRequestDateTime(date);
        linkedGroupDetails.setReasonForLink(reason);
        linkedGroupDetails.setStatus(status);
        linkedGroupDetails.setRequestId(requestId);
        linkedGroupDetails.setRequestName(requestName);

        return linkedGroupDetails;
    }

    private List<LinkedHearingDetailsAudit> generateLinkedHearingDetailsListWithBadStatus(
            String requestId, String requestName, Long groupId) {

        List<LinkedHearingDetailsAudit> existingInDataList =
                generateLinkedHearingDetailsList(requestId, requestName, groupId);
        HearingEntity hearing2 = generateHearing(2L, "WRONG_STATUS_2");
        existingInDataList.add(generateLinkedHearingDetails(2L, hearing2,
                existingInDataList.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(3L, hearing3,
                existingInDataList.get(0).getLinkedGroup(), 3L));
        HearingEntity hearing4 = generateHearing(4L, "WRONG_STATUS_4");
        existingInDataList.add(generateLinkedHearingDetails(4L, hearing4,
                existingInDataList.get(0).getLinkedGroup(), 4L));
        HearingEntity hearing5 = generateHearing(5L, "WRONG_STATUS_5");
        existingInDataList.add(generateLinkedHearingDetails(5L, hearing5,
                existingInDataList.get(0).getLinkedGroup(), 5L));
        HearingEntity hearing6 = generateHearing(6L, "WRONG_STATUS_6");
        existingInDataList.add(generateLinkedHearingDetails(6L, hearing6,
                existingInDataList.get(0).getLinkedGroup(), 6L));
        return existingInDataList;
    }

    private List<LinkedHearingDetailsAudit> generateValidLinkedHearingDetailsList(String requestId, String requestName,
                                                                                  Long groupId) {
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateLinkedHearingDetailsList(requestId, requestName, groupId);
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(2L, hearing2,
                existingInDataList.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(3L, hearing3,
                existingInDataList.get(0).getLinkedGroup(), 3L));
        HearingEntity hearing4 = generateHearing(4L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(4L, hearing4,
                existingInDataList.get(0).getLinkedGroup(), 4L));
        HearingEntity hearing5 = generateHearing(5L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(5L, hearing5,
                existingInDataList.get(0).getLinkedGroup(), 5L));
        HearingEntity hearing6 = generateHearing(6L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(6L, hearing6,
                existingInDataList.get(0).getLinkedGroup(), 6L));
        return existingInDataList;
    }



    private List<LinkedHearingDetailsAudit> generateLinkedHearingDetailsList(String requestId,
                                                                             String requestName, Long groupId) {
        HearingEntity hearing = generateHearing(1L, PutHearingStatus.HEARING_REQUESTED.name());
        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(
                groupId, PutHearingStatus.HEARING_REQUESTED.name(), requestId, requestName);
        List<LinkedHearingDetailsAudit> linkedHearingDetailsList = new ArrayList<>();
        linkedHearingDetailsList.add(generateLinkedHearingDetails(1L, hearing, groupDetails, 1L));
        return linkedHearingDetailsList;
    }

    private LinkedGroupDetails generateLinkedGroupDetails(Long id, String status,
                                                          String requestId, String requestName) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(id);
        groupDetails.setLinkType(LinkType.SAME_SLOT);
        groupDetails.setReasonForLink("reason for link");
        groupDetails.setRequestDateTime(LocalDateTime.now());
        groupDetails.setRequestId(requestId);
        groupDetails.setRequestName(requestName);
        groupDetails.setStatus(status);
        return groupDetails;
    }

    private HearingEntity generateHearing(Long id, String status) {
        HearingEntity hearing = new HearingEntity();
        hearing.setId(id);
        hearing.setStatus(status);
        return hearing;
    }

    private LinkedHearingDetailsAudit generateLinkedHearingDetails(Long id, HearingEntity hearing,
                                                                   LinkedGroupDetails groupDetails, Long linkedOrder) {
        LinkedHearingDetailsAudit linkedHearingDetails = new LinkedHearingDetailsAudit();
        linkedHearingDetails.setHearing(hearing);
        linkedHearingDetails.setLinkedHearingDetailsAuditId(id);
        linkedHearingDetails.setLinkedGroup(groupDetails);
        linkedHearingDetails.setLinkedOrder(linkedOrder);
        return linkedHearingDetails;
    }

}
