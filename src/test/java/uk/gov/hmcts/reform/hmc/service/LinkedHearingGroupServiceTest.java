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
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.HearingManagementInterfaceResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class LinkedHearingGroupServiceTest {

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
    public static final String INVALID_REQUEST_ID = "string value";
    public static final String TOKEN = "example-token";
    private static final String CLIENT_S2S_TOKEN = "xui_webapp";

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

            LinkedGroupDetailsAudit groupDetailsAudit = createGroupDetailsAuditEntity("ACTIVE", groupDetails);
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);

            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));
            given(linkedGroupDetailsAuditMapper.modelToEntity(groupDetails))
                .willReturn(groupDetailsAudit);
            given(hearingRepository.existsById(HEARING_ID1)).willReturn(true);
            given(hearingRepository.existsById(HEARING_ID2)).willReturn(true);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN);

            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(hearingRepository).existsById(HEARING_ID1);
            verify(hearingRepository).existsById(HEARING_ID2);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID1);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID2);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
        }

        @Test
        void shouldReturn404ErrorWhenNonExistentHearingGroup() {
            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(null);

            Exception exception = assertThrows(LinkedGroupNotFoundException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals(INVALID_LINKED_GROUP_REQUEST_ID_DETAILS, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusPENDING() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "PENDING");

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals("007 group is in a PENDING state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusERROR() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ERROR");

            when(linkedGroupDetailsRepository.isFoundForRequestId(any())).thenReturn(HEARING_GROUP_ID);
            when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(any())).thenReturn(groupDetails);

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN));
            assertEquals("007 group is in a ERROR state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsHEARING_REQUESTEDButPlannedHearingDateInThePast() {

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
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsUPDATE_REQUESTEDButPlannedHearingDateInThePast() {

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
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
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
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
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
            given(hearingRepository.existsById(HEARING_ID1)).willReturn(true);
            given(hearingRepository.existsById(HEARING_ID2)).willReturn(true);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN);

            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(hearingRepository).existsById(HEARING_ID1);
            verify(hearingRepository).existsById(HEARING_ID2);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID1);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID2);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
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
            given(hearingRepository.existsById(HEARING_ID1)).willReturn(true);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.of(groupDetails));
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(REQUEST_ID, CLIENT_S2S_TOKEN);

            verify(linkedGroupDetailsRepository, times(1)).isFoundForRequestId(REQUEST_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(hearingRepository).existsById(HEARING_ID1);
            verify(hearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID1);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
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
            assertEquals(REJECTED_BY_LIST_ASSIST, response.getDescription());
            assertEquals(400, response.getResponseCode());
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
            assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, response.getDescription());
            assertEquals(500, response.getResponseCode());
        }

        private HearingResponseEntity createHearingResponseEntityWithHearingDays(
            Integer requestVersion,
            LocalDateTime requestTimestamp,
            List<LocalDateTime> hearingDaysStartDateTime) {

            HearingResponseEntity hearingResponse = new HearingResponseEntity();
            hearingResponse.setRequestVersion(requestVersion);
            hearingResponse.setRequestTimeStamp(requestTimestamp);
            hearingResponse.setHearingDayDetails(
                hearingDaysStartDateTime.stream().map(this::createHearingDayDetails).toList()
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

        private LinkedGroupDetailsAudit createGroupDetailsAuditEntity(String groupStatus,
                                                                      LinkedGroupDetails groupDetails) {
            LinkedGroupDetailsAudit groupDetailsAudit = new LinkedGroupDetailsAudit();
            groupDetailsAudit.setLinkedGroup(groupDetails);
            groupDetailsAudit.setLinkedGroupVersion(1L);
            groupDetailsAudit.setLinkType(ORDERED);
            groupDetailsAudit.setStatus(groupStatus);
            return groupDetailsAudit;
        }

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

        private HearingLinkGroupRequest generateHearingLink(GroupDetails groupDetails,
                                                            List<LinkHearingDetails> hearingDetails) {
            HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
            hearingLinkGroupRequest.setHearingsInGroup(hearingDetails);
            hearingLinkGroupRequest.setGroupDetails(groupDetails);
            return hearingLinkGroupRequest;
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

        private LinkHearingDetails generateHearingDetails(String hearingId, int order) {
            LinkHearingDetails hearingDetails = new LinkHearingDetails();
            hearingDetails.setHearingId(hearingId);
            hearingDetails.setHearingOrder(order);
            return hearingDetails;
        }
    }

    private GroupDetails generateGroupDetails(String groupName, String linkTypeLabel) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupName(groupName);
        groupDetails.setGroupReason("a good reason");
        groupDetails.setGroupLinkType(linkTypeLabel);
        groupDetails.setGroupComments("comment");
        return groupDetails;
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
}
