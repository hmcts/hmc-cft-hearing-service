package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FhBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.HearingManagementInterfaceResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
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
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.INVALID_REQUEST;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.SERVER_ERROR;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType.ORDERED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

@ExtendWith(MockitoExtension.class)
class LinkHearingGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkHearingGroupServiceTest.class);

    public static final String FOR_DELETE_INVALID_HEARING_STATUS = "HEARING_STATUS_INVALID";
    public static final long HEARING_GROUP_ID = 567L;
    public static final long HEARING_ID1 = 1234L;
    public static final long HEARING_ID2 = 1235L;
    public static final LocalDateTime START_DATE_TIME_IN_THE_FUTURE =
        LocalDateTime.of(2500, 10, 1, 1, 1);
    public static final LocalDateTime START_DATE_TIME_IN_THE_PAST =
        LocalDateTime.of(2000, 10, 1, 1, 1);
    public static final LocalDateTime HEARING_RESPONSE_DATE_TIME = LocalDateTime.now();
    public static final String REQUEST_ID = "44444";
    public static final String TOKEN = "example-token";

    @InjectMocks
    private LinkedHearingGroupServiceImpl service;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @Mock
    AccessControlService accessControlService;

    @Mock
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    ActualHearingRepository actualHearingRepository;

    @Mock
    ActualHearingDayRepository actualHearingDayRepository;

    @Mock
    LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;

    @Mock
    LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;

    FutureHearingsLinkedHearingGroupService futureHearingsLinkedHearingGroupService;

    HearingIdValidator hearingIdValidator;

    LinkedHearingValidator linkedHearingValidator;

    @Mock
    private DefaultFutureHearingRepository futureHearingRepository;

    @Mock
    ObjectMapperService objectMapperService;

    private static final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();

    @Mock
    LinkedHearingStatusAuditService linkedHearingStatusAuditService;

    private static final String CLIENT_S2S_TOKEN = "xui_webapp";

    @BeforeEach
    void setUp() {
        hearingIdValidator = new HearingIdValidator(hearingRepository, actualHearingRepository,
                                                    actualHearingDayRepository
        );
        linkedHearingValidator = new LinkedHearingValidator(hearingIdValidator, hearingRepository,
                                                            linkedGroupDetailsRepository, linkedHearingDetailsRepository
        );
        futureHearingsLinkedHearingGroupService =
            new FutureHearingsLinkedHearingGroupService(
                hearingRepository,
                linkedGroupDetailsRepository,
                linkedHearingValidator,
                linkedHearingDetailsAuditRepository,
                linkedGroupDetailsAuditRepository,
                linkedGroupDetailsAuditMapper,
                linkedHearingDetailsAuditMapper,
                accessControlService
            );
        service =
            new LinkedHearingGroupServiceImpl(
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
    }

    @Nested
    @DisplayName("postLinkedHearingGroup")
    class PostLinkedHearingGroup {
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
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
            );
            LinkedGroupDetails groupDetailsAlternate = generateLinkGroupDetails(
                202L,
                "requestId2",
                "request name2",
                "Same Slot",
                "status",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
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
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                service.linkHearing(hearingLinkGroupRequest,CLIENT_S2S_TOKEN);
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
            hearingEntity.getCaseHearingRequests().get(0).setHearingWindowStartDateRange(LocalDate.now().minusDays(2));
            hearingEntity.getCaseHearingRequests().get(0).setHearingWindowEndDateRange(LocalDate.now().plusDays(1));

            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            });
            assertTrue(exception.getMessage().startsWith("Invalid value"));
            assertTrue(exception.getMessage().contains("for GroupLinkType"));
        }

        @Test
        void shouldPassWhenHearingOrderIsSameSlot() {
            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            when(hearingRepository.existsById(2000000002L)).thenReturn(true);
            when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(hearingEntity));

            given(linkedGroupDetailsRepository.save(any())).willReturn(TestingUtil.linkedGroupDetailsEntity());

            HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
            response.setResponseCode(200);
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
            doNothing().when(futureHearingRepository).createLinkedHearingGroup(any());
            given(hearingRepository.findByLinkedGroupId(any())).willReturn(List.of(
                TestingUtil.hearingEntityWithLinkDetails(), TestingUtil.hearingEntityWithLinkDetails()));

            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.SAME_SLOT.label, "reason"
            );

            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );
            service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(2)).existsById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000000L);
            verify(hearingRepository, times(2)).existsById(2000000002L);
            verify(hearingRepository, times(2)).findById(2000000002L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(eq(2000000000L), any(), eq(1L));
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(eq(2000000002L), any(), eq(1L));
            verify(linkedGroupDetailsRepository, times(2)).save(any());
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                              any(),any(),any(),any(),any());
        }

        @Test
        void shouldPassWhenHearingOrderIsSameSlotWithOrderZero() {
            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );
            hearingEntity.setLinkedOrder(null);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            when(hearingRepository.existsById(2000000002L)).thenReturn(true);
            when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(hearingEntity));

            given(linkedGroupDetailsRepository.save(any())).willReturn(TestingUtil.linkedGroupDetailsEntity());

            HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
            response.setResponseCode(200);
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 0);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 0);
            doNothing().when(futureHearingRepository).createLinkedHearingGroup(any());
            given(hearingRepository.findByLinkedGroupId(any())).willReturn(List.of(
                TestingUtil.hearingEntityWithLinkDetails(), TestingUtil.hearingEntityWithLinkDetails()));

            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.SAME_SLOT.label, "reason"
            );

            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );
            service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(2)).existsById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000000L);
            verify(hearingRepository, times(2)).existsById(2000000002L);
            verify(hearingRepository, times(2)).findById(2000000002L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(eq(2000000000L), any(), isNull());
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(eq(2000000002L), any(), isNull());
            verify(linkedGroupDetailsRepository, times(2)).save(any());
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldPassWithValidLinkedHearing() {
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
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            given(linkedGroupDetailsRepository.save(any())).willReturn(TestingUtil.linkedGroupDetailsEntity());
            HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
            response.setResponseCode(200);
            doNothing().when(futureHearingRepository).createLinkedHearingGroup(any());
            given(hearingRepository.findByLinkedGroupId(any())).willReturn(List.of(
                TestingUtil.hearingEntityWithLinkDetails(), TestingUtil.hearingEntityWithLinkDetails()));

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

            logger.info("hearingLinkGroupRequest : {}", hearingLinkGroupRequest);
            service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(2)).existsById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000000L);
            verify(hearingRepository, times(2)).existsById(2000000002L);
            verify(hearingRepository, times(2)).findById(2000000002L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(eq(2000000000L), any(), eq(1L));
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(eq(2000000002L), any(), eq(2L));
            verify(linkedGroupDetailsRepository, times(2)).save(any());
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldFailBecauseOfError4xxFromListAssist() {
            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            when(hearingRepository.existsById(2000000002L)).thenReturn(true);
            when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(hearingEntity));

            LinkedGroupDetails linkedGroupDetails = generateLinkGroupDetails(1L,
                                                                             "1",
                                                                             "name",
                                                                             "Same Slot",
                                                                             "PENDING",
                                                                             "reason",
                                                                             "comment",
                                                                             LocalDateTime.now(),
                                                                             1L);
            given(linkedGroupDetailsRepository.save(any())).willReturn(linkedGroupDetails);
            given(hearingRepository.findByLinkedGroupId(1L)).willReturn(
                List.of(TestingUtil.hearingEntityWithLinkDetails(), TestingUtil.hearingEntityWithLinkDetails()));

            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(null)).thenReturn(null);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId("1")).thenReturn(linkedGroupDetails);

            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(400);
            BadFutureHearingRequestException badFutureHearingRequestException = new BadFutureHearingRequestException(
                INVALID_REQUEST);
            doThrow(badFutureHearingRequestException).when(futureHearingRepository).createLinkedHearingGroup(any());

            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.SAME_SLOT.label, "reason"
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
            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            });
            assertTrue(exception.getMessage().contains(REJECTED_BY_LIST_ASSIST));
            verify(hearingRepository, times(2)).findById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000002L);
            verify(hearingRepository, times(3)).existsById(2000000000L);
            verify(hearingRepository, times(3)).existsById(2000000002L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(2000000000L, linkedGroupDetails, 1L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(2000000002L, linkedGroupDetails, 1L);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(2000000000L);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(2000000002L);
            verify(hearingRepository).findByLinkedGroupId(1L);
            verify(linkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(null);
            verify(linkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId("1");
            verify(linkedGroupDetailsRepository).save(any(LinkedGroupDetails.class));
            verify(linkedGroupDetailsRepository).delete(linkedGroupDetails);
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldFailBecauseOfError5xxFromListAssist() {
            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            when(hearingRepository.existsById(2000000002L)).thenReturn(true);
            when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(hearingEntity));

            LinkedGroupDetails linkedGroupDetails = generateLinkGroupDetails(1L,
                                                                             "1",
                                                                             "name",
                                                                             "Same Slot",
                                                                             "PENDING",
                                                                             "reason",
                                                                             "comment",
                                                                             LocalDateTime.now(),
                                                                             1L);
            given(linkedGroupDetailsRepository.save(any())).willReturn(linkedGroupDetails);
            given(hearingRepository.findByLinkedGroupId(1L)).willReturn(List.of(
                TestingUtil.hearingEntityWithLinkDetails(), TestingUtil.hearingEntityWithLinkDetails()));

            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(null)).thenReturn(null);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId("1")).thenReturn(linkedGroupDetails);

            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(500);
            FutureHearingServerException futureHearingServerException = new FutureHearingServerException(SERVER_ERROR);
            doThrow(futureHearingServerException).when(futureHearingRepository).createLinkedHearingGroup(any());
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.SAME_SLOT.label, "reason"
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
            Exception exception = assertThrows(FhBadRequestException.class, () -> {
                service.linkHearing(hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            });
            assertTrue(exception.getMessage().contains(LIST_ASSIST_FAILED_TO_RESPOND));
            verify(hearingRepository, times(2)).findById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000002L);
            verify(hearingRepository, times(3)).existsById(2000000000L);
            verify(hearingRepository, times(3)).existsById(2000000002L);
            verify(hearingRepository)
                .updateLinkedGroupDetailsAndOrder(eq(2000000000L), any(LinkedGroupDetails.class), eq(1L));
            verify(hearingRepository)
                .updateLinkedGroupDetailsAndOrder(eq(2000000002L), any(LinkedGroupDetails.class), eq(1L));
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(2000000000L);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(2000000002L);
            verify(hearingRepository).findByLinkedGroupId(1L);
            verify(linkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(null);
            verify(linkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId("1");
            verify(linkedGroupDetailsRepository).save(any(LinkedGroupDetails.class));
            verify(linkedGroupDetailsRepository).delete(linkedGroupDetails);
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }
    }

    @Nested
    @DisplayName("putLinkedHearingGroup")
    class PutLinkedHearingGroup {
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(false);

            Exception exception = assertThrows(HearingNotFoundException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
            );
            LinkedGroupDetails groupDetailsAlternate = generateLinkGroupDetails(
                202L,
                "requestId2",
                "request name2",
                "Same Slot",
                "status",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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
            hearingEntity.getCaseHearingRequests().get(0).setHearingWindowStartDateRange(LocalDate.now().minusDays(2));
            hearingEntity.getCaseHearingRequests().get(0).setHearingWindowEndDateRange(LocalDate.now().plusDays(1));
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
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

            LinkedGroupDetails linkedGroupDetails = generateLinkGroupDetails(
                1L,
                "requestId",
                "request name 1",
                "Same Slot",
                "ACTIVE",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
            );
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);
            when(hearingRepository.findByLinkedGroupId(any())).thenReturn(Arrays.asList(hearingEntity));
            when(linkedGroupDetailsRepository.save(any())).thenReturn(linkedGroupDetails);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );
            service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(2)).existsById(2000000000L);
            verify(hearingRepository, times(3)).findById(2000000000L);
            verify(hearingRepository, times(2)).existsById(2000000002L);
            verify(hearingRepository, times(3)).findById(2000000002L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(2000000000L, linkedGroupDetails, 1L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(2000000002L, linkedGroupDetails, 1L);
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(any());
            verify(linkedGroupDetailsRepository, times(2)).save(any());
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            LinkedGroupDetails linkedGroupDetails = generateLinkGroupDetails(
                1L,
                "requestId",
                "request name 1",
                "Same Slot",
                "ACTIVE",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
            );
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);
            when(hearingRepository.findByLinkedGroupId(any())).thenReturn(Arrays.asList(hearingEntity));
            when(linkedGroupDetailsRepository.save(any())).thenReturn(linkedGroupDetails);


            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );
            logger.info("hearingLinkGroupRequest : {}", hearingLinkGroupRequest);
            service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(2)).existsById(2000000000L);
            verify(hearingRepository, times(3)).findById(2000000000L);
            verify(hearingRepository, times(2)).existsById(2000000002L);
            verify(hearingRepository, times(3)).findById(2000000002L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(2000000000L, linkedGroupDetails, 1L);
            verify(hearingRepository).updateLinkedGroupDetailsAndOrder(2000000002L, linkedGroupDetails, 2L);
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(any());
            verify(linkedGroupDetailsRepository, times(2)).save(any());
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldFailBecauseOfError4xxFromListAssist() {
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            LinkedGroupDetails linkedGroupDetails = generateLinkGroupDetails(
                1L,
                "requestId",
                "request name 1",
                "Same Slot",
                "ACTIVE",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
            );
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);
            when(hearingRepository.findByLinkedGroupId(any())).thenReturn(Arrays.asList(hearingEntity));
            when(linkedGroupDetailsRepository.save(any())).thenReturn(linkedGroupDetails);

            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );
            logger.info("hearingLinkGroupRequest : {}", hearingLinkGroupRequest);
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(400);
            BadFutureHearingRequestException badFutureHearingRequestException = new BadFutureHearingRequestException(
                INVALID_REQUEST);
            doThrow(badFutureHearingRequestException).when(futureHearingRepository).updateLinkedHearingGroup(
                any(),
                any()
            );

            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            });
            assertTrue(exception.getMessage().contains(REJECTED_BY_LIST_ASSIST));
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldFailBecauseOfError5xxFromListAssist() {
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

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(hearingRepository.existsById(any())).thenReturn(true);
            when(hearingRepository.findById(any())).thenReturn(Optional.of(hearingEntity));

            LinkedGroupDetails linkedGroupDetails = generateLinkGroupDetails(
                1L,
                "requestId",
                "request name 1",
                "Same Slot",
                "ACTIVE",
                "reason",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11),
                1L
            );
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(1L);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(linkedGroupDetails);
            when(hearingRepository.findByLinkedGroupId(any())).thenReturn(Arrays.asList(hearingEntity));
            when(linkedGroupDetailsRepository.save(any())).thenReturn(linkedGroupDetails);

            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );


            logger.info("hearingLinkGroupRequest : {}", hearingLinkGroupRequest);
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(500);
            FutureHearingServerException futureHearingServerException = new FutureHearingServerException(SERVER_ERROR);
            doThrow(futureHearingServerException).when(futureHearingRepository).updateLinkedHearingGroup(any(), any());
            Exception exception = assertThrows(BadRequestException.class, () -> {
                service.updateLinkHearing("1", hearingLinkGroupRequest, CLIENT_S2S_TOKEN);
            });
            assertTrue(exception.getMessage().contains(LIST_ASSIST_FAILED_TO_RESPOND));
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }
    }

    @Nested
    @DisplayName("deleteHearingGroup")
    class DeleteHearingGroup {

        @Test
        void shouldDeleteHearingGroupDetails() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setLinkedGroupDetails(groupDetails);
            hearing1.setStatus(POST_HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(
                                                               START_DATE_TIME_IN_THE_FUTURE,
                                                               START_DATE_TIME_IN_THE_FUTURE
                                                           )
                )
            ));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setLinkedGroupDetails(groupDetails);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));

            LinkedGroupDetailsAudit groupDetailsAudit = createGroupDetailsAuditEntity(HEARING_GROUP_ID,
                                                                                      "ACTIVE", groupDetails
            );
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID)).willReturn(List.of(hearing1, hearing2));
            given(linkedGroupDetailsAuditMapper.modelToEntity(groupDetails))
                .willReturn(groupDetailsAudit);
            given(hearingRepository.existsById(HEARING_ID1))
                .willReturn(true);
            given(hearingRepository.existsById(HEARING_ID2))
                .willReturn(true);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN);

            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(hearingRepository).existsById(HEARING_ID1);
            verify(hearingRepository).existsById(HEARING_ID2);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID1);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID2);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldReturn404ErrorWhenNonExistentHearingGroup() {
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(null);

            Exception exception = assertThrows(LinkedGroupNotFoundException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals(INVALID_LINKED_GROUP_REQUEST_ID_DETAILS, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
            verify(linkedHearingStatusAuditService, never()).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                              any(),any(),any(),any(),any());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusPending() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "PENDING");

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals("007 group is in a PENDING state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
            verify(linkedHearingStatusAuditService, never()).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                              any(),any(),any(),any(),any());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusError() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ERROR");

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals("007 group is in a ERROR state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsHearing_RequestedButPlannedHearingDateInThePast() {

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setStatus(POST_HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(
                                                               START_DATE_TIME_IN_THE_FUTURE,
                                                               START_DATE_TIME_IN_THE_FUTURE
                                                           )
                )
            ));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(
                                                               START_DATE_TIME_IN_THE_FUTURE,
                                                               START_DATE_TIME_IN_THE_PAST
                                                           )
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )
            ));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals(
                "008 Invalid state for unlinking hearing request " + HEARING_ID2,
                exception.getMessage()
            );
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedHearingStatusAuditService, times(1))
                .saveLinkedHearingAuditTriageDetails(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsUpdate_RequestedButPlannedHearingDateInThePast() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(HEARING_STATUS_UPDATE_REQUESTED);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(
                                                               START_DATE_TIME_IN_THE_FUTURE,
                                                               START_DATE_TIME_IN_THE_PAST
                                                           )
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals(
                "008 Invalid state for unlinking hearing request " + HEARING_ID1,
                exception.getMessage()
            );
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedHearingStatusAuditService, times(1)).saveLinkedHearingAuditTriageDetails(
                any(),
                any(), any(), any(),
                any(), any(), any());
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsInvalidForUnlinking() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(FOR_DELETE_INVALID_HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals(
                "008 Invalid state for unlinking hearing request " + HEARING_ID1,
                exception.getMessage()
            );
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedHearingStatusAuditService, times(1))
                .saveLinkedHearingAuditTriageDetails(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldDeleteHearingGroupDetailsFilteringOutInvalidMultipleResponseHearingVersions() {

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setStatus(POST_HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(2, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           // should not fail as will get filtered out
                                                           List.of(
                                                               START_DATE_TIME_IN_THE_PAST,
                                                               START_DATE_TIME_IN_THE_FUTURE
                                                           )
                )
            ));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");
            hearing1.setLinkedGroupDetails(groupDetails);
            hearing2.setLinkedGroupDetails(groupDetails);
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));
            given(hearingRepository.existsById(HEARING_ID1))
                .willReturn(true);
            given(hearingRepository.existsById(HEARING_ID2))
                .willReturn(true);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            HearingManagementInterfaceResponse response = getHearingResponseFromListAssist(
                200, "Success");
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN);

            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(hearingRepository).existsById(HEARING_ID1);
            verify(hearingRepository).existsById(HEARING_ID2);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID1);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID2);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldDeleteHearingGroupDetailsFilteringOutHearingResponsesWithNonRecentTimestampForSameVersion() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(POST_HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME.minusDays(1),
                                                           // should not fail as will get filtered out
                                                           List.of(START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )
            ));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");
            hearing.setLinkedGroupDetails(groupDetails);
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            given(hearingRepository.existsById(HEARING_ID1))
                .willReturn(true);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            HearingManagementInterfaceResponse response = getHearingResponseFromListAssist(
                200, "Success");
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN);

            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(hearingRepository).existsById(HEARING_ID1);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID1);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                               any(),any(),any(),any(),any());
        }

        @Test
        void shouldDeleteHearingGroupDetails_ListAssistReturns4xxError() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(POST_HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME.minusDays(1),
                                                           // should not fail as will get filtered out
                                                           List.of(START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )
            ));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");
            hearing.setLinkedGroupDetails(groupDetails);
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            listAssistThrows4xxError();
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            final HearingManagementInterfaceResponse response = getHearingResponseFromListAssist(
                400, "005 rejected by List Assist");
            assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            assertEquals(REJECTED_BY_LIST_ASSIST, response.getDescription());
            assertEquals(400, response.getResponseCode());
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                              any(),any(),any(),any(),any());
        }

        @Test
        void shouldDeleteHearingGroupDetails_ListAssistReturns5xxError() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(POST_HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME.minusDays(1),
                                                           // should not fail as will get filtered out
                                                           List.of(START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )
            ));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");
            hearing.setLinkedGroupDetails(groupDetails);
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            final HearingManagementInterfaceResponse response = getHearingResponseFromListAssist(
                500, "006 List Assist failed to respond");
            listAssistThrows5xxError();
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedGroupDetailsRepository, times(1))
                .getLinkedGroupDetailsByRequestId(any());
            assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, response.getDescription());
            assertEquals(500, response.getResponseCode());
            verify(linkedHearingStatusAuditService, times(2)).saveLinkedHearingAuditTriageDetails(any(),any(),
                                                                              any(),any(),any(),any(),any());
        }

        private HearingResponseEntity createHearingResponseEntityWithHearingDays(
            Integer requestVersion,
            LocalDateTime requestTimestamp,
            List<LocalDateTime> hearingDaysStartDateTime) {

            HearingResponseEntity hearingResponse = new HearingResponseEntity();
            hearingResponse.setRequestVersion(requestVersion);
            hearingResponse.setRequestTimeStamp(requestTimestamp);
            hearingResponse.setHearingDayDetails(
                hearingDaysStartDateTime.stream().map(this::createHearingDayDetails).collect(Collectors.toList())
            );
            return hearingResponse;
        }

        private HearingDayDetailsEntity createHearingDayDetails(LocalDateTime hearingDayStartDateTime) {
            HearingDayDetailsEntity hearingDayDetails1 = new HearingDayDetailsEntity();
            hearingDayDetails1.setStartDateTime(hearingDayStartDateTime);
            return hearingDayDetails1;
        }

        private LinkedGroupDetails createGroupDetailsEntity(Long hearingGroupId, String groupStatus) {
            LinkedGroupDetails groupDetails = new LinkedGroupDetails();
            groupDetails.setLinkedGroupId(hearingGroupId);
            groupDetails.setStatus(groupStatus);
            groupDetails.setLinkedGroupLatestVersion(1L);
            groupDetails.setRequestId(REQUEST_ID);
            return groupDetails;
        }

        private LinkedGroupDetailsAudit createGroupDetailsAuditEntity(long hearingGroupId, String groupStatus,
                                                                      LinkedGroupDetails groupDetails) {
            LinkedGroupDetailsAudit groupDetailsAudit = new LinkedGroupDetailsAudit();
            groupDetailsAudit.setLinkedGroup(groupDetails);
            groupDetailsAudit.setLinkedGroupVersion(1L);
            groupDetailsAudit.setLinkType(ORDERED);
            groupDetailsAudit.setStatus(groupStatus);
            return groupDetailsAudit;
        }

    }

    private HearingManagementInterfaceResponse getHearingResponseFromListAssist(Integer errorCode, String description) {
        HearingManagementInterfaceResponse response = new HearingManagementInterfaceResponse();
        response.setResponseCode(errorCode);
        response.setDescription(description);
        return response;
    }

    private void listAssistThrows4xxError() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(400);
        BadFutureHearingRequestException badFutureHearingRequestException = new BadFutureHearingRequestException(
            INVALID_REQUEST);
        doThrow(badFutureHearingRequestException).when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
    }

    private void listAssistThrows5xxError() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(500);
        FutureHearingServerException futureHearingServerException = new FutureHearingServerException(SERVER_ERROR);
        doThrow(futureHearingServerException).when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
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

        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntity));

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setHearingDayDetails(hearingDayDetailsEntities);
        hearingResponseEntity.setHearing(hearingEntity);
        hearingResponseEntity.setRequestVersion(versionNumber);
        hearingResponseEntity.setRequestTimeStamp(requestTimestamp);

        hearingEntity.setHearingResponses(List.of(hearingResponseEntity));
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
                                                        String comments, LocalDateTime date, Long version) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupId(linkGroupId);
        linkedGroupDetails.setLinkType(LinkType.getByLabel(linkTypeLabel));
        linkedGroupDetails.setLinkedComments(comments);
        linkedGroupDetails.setRequestDateTime(date);
        linkedGroupDetails.setReasonForLink(reason);
        linkedGroupDetails.setStatus(status);
        linkedGroupDetails.setRequestId(requestId);
        linkedGroupDetails.setRequestName(requestName);
        linkedGroupDetails.setLinkedGroupLatestVersion(version);

        return linkedGroupDetails;
    }
}
