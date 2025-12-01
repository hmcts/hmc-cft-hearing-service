package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkedHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.service.common.LinkedHearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType.ORDERED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class LinkedHearingGroupServiceTest {

    public static final String REQUEST_ID = "44444";
    public static final String INVALID_REQUEST_ID = "string value";

    @InjectMocks
    private LinkedHearingGroupServiceImpl service;

    @Mock
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    ActualHearingRepository actualHearingRepository;

    @Mock
    ActualHearingDayRepository actualHearingDayRepository;

    @Mock
    LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @Mock
    LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;

    @Mock
    LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;

    HearingIdValidator hearingIdValidator;

    LinkedHearingValidator linkedHearingValidator;

    FutureHearingsLinkedHearingGroupService futureHearingsLinkedHearingGroupService;

    @Mock
    DefaultFutureHearingRepository futureHearingRepository;

    @Mock
    ObjectMapperService objectMapperService;

    @Mock
    AccessControlService accessControlService;

    @Mock
    LinkedHearingStatusAuditService linkedHearingStatusAuditService;

    private static final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();

    @BeforeEach
    void setUp() {
        hearingIdValidator = new HearingIdValidator(hearingRepository, actualHearingRepository,
                                                    actualHearingDayRepository
        );
        linkedHearingValidator = new LinkedHearingValidator(hearingIdValidator, hearingRepository,
                                                            linkedGroupDetailsRepository, linkedHearingDetailsRepository
        );

        futureHearingsLinkedHearingGroupService = new FutureHearingsLinkedHearingGroupService(
            hearingRepository,
            linkedGroupDetailsRepository,
            linkedHearingValidator,
            linkedHearingDetailsAuditRepository,
            linkedGroupDetailsAuditRepository,
            linkedGroupDetailsAuditMapper,
            linkedHearingDetailsAuditMapper,
            accessControlService
        );

        service = new LinkedHearingGroupServiceImpl(
            hearingRepository,
            linkedGroupDetailsRepository,
            linkedHearingValidator,
            futureHearingRepository,
            objectMapperService,
            accessControlService,
            futureHearingsLinkedHearingGroupService,
            linkedHearingStatusAuditService,
            objectMapper
        );
        linkedHearingStatusAuditService.saveLinkedHearingAuditTriageDetails(any(),any(),any(),any(),any(),any(),any());
    }

    @Nested
    @DisplayName("getHearingGroup")
    class GetHearingGroup {
        @Test
        void shouldReturnErrorWhenRequestIdIsNotFound() {
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.getLinkedHearingGroupResponse(INVALID_REQUEST_ID));
            assertEquals(INVALID_LINKED_GROUP_REQUEST_ID_DETAILS, exception.getMessage());
        }

        @Test
        void shouldGetLinkedHearingGroupDetails() {
            LinkedGroupDetails linkedGroupDetails = createLinkedGroupDetails("100", "ACTIVE");
            linkedGroupDetails.setRequestName("group name");
            linkedGroupDetails.setReasonForLink("a good reason");
            linkedGroupDetails.setLinkedComments("comment");

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                List.of(generateHearingDayDetailsEntity(2000000000L, LocalDateTime.now().plusDays(1))),
                linkedGroupDetails
            );
            hearingEntity.setLinkedOrder(1L);

            when(linkedGroupDetailsRepository.isFoundForRequestId(REQUEST_ID)).thenReturn(100L);
            when(hearingRepository.findByRequestId(REQUEST_ID)).thenReturn(List.of(hearingEntity));
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(REQUEST_ID))
                .thenReturn(linkedGroupDetails);
            when(hearingRepository.findByLinkedGroupId(100L)).thenReturn(List.of(hearingEntity));

            GetLinkedHearingGroupResponse response = service.getLinkedHearingGroupResponse(REQUEST_ID);

            assertGroupDetails(response.getGroupDetails());
            for (LinkedHearingDetails linkedHearingDetails : response.getHearingsInGroup()) {
                assertHearingsInGroup(linkedHearingDetails);
            }

            verify(linkedGroupDetailsRepository).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository).findByRequestId(REQUEST_ID);
            verify(accessControlService).verifyAccess(2000000000L, Lists.newArrayList("hearing-viewer"));
            verify(linkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(REQUEST_ID);
            verify(hearingRepository).findByLinkedGroupId(100L);
        }

        @Test
        void sortHearingsInGroup_SortsByHearingOrderAndHearingId() {

            HearingEntity hearingEntity1 = generateHearingEntity(
                2000000002L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(6))),
                null, 3L
            );
            HearingEntity hearingEntity2 = generateHearingEntity(
                2000000001L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000001L, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(2000000001L, LocalDateTime.now().plusDays(2)),
                    generateHearingDayDetailsEntity(2000000001L, LocalDateTime.now().plusDays(6))),
                null, 3L
            );
            HearingEntity hearingEntity3 = generateHearingEntity(
                2000000004L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000004L, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(2000000004L, LocalDateTime.now().plusDays(2)),
                    generateHearingDayDetailsEntity(2000000004L, LocalDateTime.now().plusDays(6))),
                null, 2L
            );
            HearingEntity hearingEntity4 = generateHearingEntity(
                2000000003L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000003L, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(2000000003L, LocalDateTime.now().plusDays(2))),
                null, 2L
            );
            HearingEntity hearingEntity5 = generateHearingEntity(
                2000000006L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000006L, LocalDateTime.now().plusDays(2)),
                    generateHearingDayDetailsEntity(2000000006L, LocalDateTime.now().plusDays(6))),
                null, 1L
            );
            HearingEntity hearingEntity6 = generateHearingEntity(
                2000000005L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000005L, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(2000000005L, LocalDateTime.now().plusDays(2)),
                    generateHearingDayDetailsEntity(2000000005L, LocalDateTime.now().plusDays(6))),
                null, 1L
            );

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(Long.parseLong(REQUEST_ID));

            LinkedGroupDetails linkedGroupDetails = createLinkedGroupDetails(REQUEST_ID, "ACTIVE");
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(Long.parseLong(REQUEST_ID));
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);
            when(hearingRepository.findByLinkedGroupId(any())).thenReturn(List.of(hearingEntity6,hearingEntity5,
                                                                                  hearingEntity3,hearingEntity4,
                                                                                  hearingEntity2,hearingEntity1));

            GetLinkedHearingGroupResponse response = service.getLinkedHearingGroupResponse(REQUEST_ID);
            List<LinkedHearingDetails> hearingsInGroup = response.getHearingsInGroup();

            assertThat(hearingsInGroup.get(0).getHearingOrder()).isEqualTo(1L);
            assertThat(hearingsInGroup.get(1).getHearingOrder()).isEqualTo(1L);
            assertThat(hearingsInGroup.get(2).getHearingOrder()).isEqualTo(2L);
            assertThat(hearingsInGroup.get(3).getHearingOrder()).isEqualTo(2L);
            assertThat(hearingsInGroup.get(4).getHearingOrder()).isEqualTo(3L);
            assertThat(hearingsInGroup.get(5).getHearingOrder()).isEqualTo(3L);
            assertThat(hearingsInGroup.get(0).getHearingId()).isEqualTo(2000000006L);
            assertThat(hearingsInGroup.get(1).getHearingId()).isEqualTo(2000000005L);
            assertThat(hearingsInGroup.get(2).getHearingId()).isEqualTo(2000000004L);
            assertThat(hearingsInGroup.get(3).getHearingId()).isEqualTo(2000000003L);
            assertThat(hearingsInGroup.get(4).getHearingId()).isEqualTo(2000000002L);
            assertThat(hearingsInGroup.get(5).getHearingId()).isEqualTo(2000000001L);
        }

        @Test
        void sortHearingsInGroup_EmptyList() {

            LinkedGroupDetails linkedGroupDetails = createLinkedGroupDetails(REQUEST_ID, "ACTIVE");
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(Long.parseLong(REQUEST_ID));
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);

            GetLinkedHearingGroupResponse response = service.getLinkedHearingGroupResponse(REQUEST_ID);

            assertThat(response.getHearingsInGroup()).isEmpty();
        }

        @Test
        void sortHearingsInGroup_SingleElement() {

            final Long HEARING_ID = 2000000001L;

            when(linkedGroupDetailsRepository.isFoundForRequestId(anyString())).thenReturn(Long.parseLong(REQUEST_ID));
            LinkedGroupDetails linkedGroupDetails = createLinkedGroupDetails(REQUEST_ID, "ACTIVE");
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);

            HearingEntity hearingEntity1 = generateHearingEntity(
                HEARING_ID, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(HEARING_ID, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(HEARING_ID, LocalDateTime.now().plusDays(2)),
                    generateHearingDayDetailsEntity(HEARING_ID, LocalDateTime.now().plusDays(6))),
                linkedGroupDetails,null);
            when(hearingRepository.findByLinkedGroupId(any())).thenReturn(List.of(hearingEntity1));

            GetLinkedHearingGroupResponse response = service.getLinkedHearingGroupResponse(REQUEST_ID);

            assertThat(response.getHearingsInGroup().get(0).getHearingId()).isEqualTo(HEARING_ID);
        }

        @Test
        void sortHearingsInGroup_NullLinkedHearingOrderIsLast() {

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(Long.parseLong(REQUEST_ID));
            LinkedGroupDetails linkedGroupDetails = createLinkedGroupDetails(REQUEST_ID, "ACTIVE");
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);

            HearingEntity hearingEntity1 = generateHearingEntity(
                2000000001L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000001L, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(2000000001L, LocalDateTime.now().plusDays(2)),
                    generateHearingDayDetailsEntity(2000000001L, LocalDateTime.now().plusDays(6))),
                linkedGroupDetails,null);

            HearingEntity hearingEntity2 = generateHearingEntity(
                2000000002L, HEARING_REQUESTED.name(), 1, true,
                List.of(
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1)),
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(2)),
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(3)),
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(4)),
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(5)),
                    generateHearingDayDetailsEntity(2000000002L, LocalDateTime.now().plusDays(6))),
                null,1L);


            when(hearingRepository.findByLinkedGroupId(any())).thenReturn(List.of(hearingEntity1, hearingEntity2));

            GetLinkedHearingGroupResponse response = service.getLinkedHearingGroupResponse(REQUEST_ID);

            assertThat(response.getHearingsInGroup().get(0).getHearingId()).isEqualTo(2000000002L);
            assertThat(response.getHearingsInGroup().get(1).getHearingId()).isEqualTo(2000000001L);
        }

        private void assertGroupDetails(GroupDetails returnedGroupDetails) {
            assertNotNull(returnedGroupDetails);
            assertEquals("group name", returnedGroupDetails.getGroupName());
            assertEquals("a good reason", returnedGroupDetails.getGroupReason());
            assertEquals(returnedGroupDetails.getGroupLinkType(), ORDERED.label);
            assertEquals("comment", returnedGroupDetails.getGroupComments());
        }

        private void assertHearingsInGroup(LinkedHearingDetails linkedHearingDetails) {
            assertAll(
                () -> assertNotNull(linkedHearingDetails.getHearingId()),
                () -> assertNotNull(linkedHearingDetails.getHearingOrder()),
                () -> assertEquals("122211123211", linkedHearingDetails.getCaseRef()),
                () -> assertEquals("Some internal code", linkedHearingDetails.getHmctsInternalCaseName())
            );
        }

        private HearingEntity generateHearingEntity(Long hearingId, String status,
                                                    Integer versionNumber, boolean isLinked,
                                                    List<HearingDayDetailsEntity> hearingDayDetailsEntities,
                                                    LinkedGroupDetails linkedGroupDetails) {
            return generateHearingEntity(hearingId, status, versionNumber, isLinked, hearingDayDetailsEntities,
                                        linkedGroupDetails, null);
        }

        private HearingEntity generateHearingEntity(Long hearingId, String status,
                                                    Integer versionNumber, boolean isLinked,
                                                    List<HearingDayDetailsEntity> hearingDayDetailsEntities,
                                                    LinkedGroupDetails linkedGroupDetails,
                                                    Long linkedOrder) {
            HearingEntity hearingEntity = new HearingEntity();
            hearingEntity.setId(hearingId);
            hearingEntity.setStatus(status);
            hearingEntity.setIsLinkedFlag(isLinked);
            hearingEntity.setLinkedGroupDetails(linkedGroupDetails);
            hearingEntity.setLinkedOrder(linkedOrder);

            CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
            caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
            caseHearingRequestEntity.setHearingWindowStartDateRange(LocalDate.now().plusDays(2));
            caseHearingRequestEntity.setHearingWindowEndDateRange(LocalDate.now().plusDays(4));
            caseHearingRequestEntity.setVersionNumber(versionNumber);
            caseHearingRequestEntity.setCaseReference("122211123211");
            caseHearingRequestEntity.setHmctsInternalCaseName("Some internal code");

            hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntity));

            HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
            hearingResponseEntity.setHearingDayDetails(hearingDayDetailsEntities);
            hearingResponseEntity.setHearing(hearingEntity);
            hearingResponseEntity.setRequestVersion(versionNumber);
            hearingResponseEntity.setRequestTimeStamp(LocalDateTime.now().plusDays(1));

            hearingEntity.setHearingResponses(List.of(hearingResponseEntity));
            return hearingEntity;
        }
    }

    private LinkedGroupDetails createLinkedGroupDetails(String hearingGroupId, String groupStatus) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(Long.parseLong(hearingGroupId));
        groupDetails.setStatus(groupStatus);
        groupDetails.setLinkedGroupLatestVersion(1L);
        groupDetails.setRequestId(REQUEST_ID);
        groupDetails.setLinkType(ORDERED);
        return groupDetails;
    }

    private HearingDayDetailsEntity generateHearingDayDetailsEntity(Long hearingId, LocalDateTime hearingDateTime) {
        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(hearingDateTime);
        hearingDayDetailsEntity.setHearingDayId(hearingId);
        return hearingDayDetailsEntity;
    }
}
