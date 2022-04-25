package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.HearingManagementInterfaceResponse;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.INVALID_REQUEST;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.SERVER_ERROR;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType.ORDERED;
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
    public static final String TOKEN = "example-token";

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

    HearingIdValidator hearingIdValidator;

    LinkedHearingValidator linkedHearingValidator;

    @Mock
    LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;

    @Mock
    LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;

    @Mock
    DefaultFutureHearingRepository futureHearingRepository;

    @Mock
    ObjectMapperService objectMapper;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingIdValidator = new HearingIdValidator(hearingRepository, actualHearingRepository,
                actualHearingDayRepository);
        linkedHearingValidator = new LinkedHearingValidator(hearingIdValidator, hearingRepository,
                linkedGroupDetailsRepository, linkedHearingDetailsRepository);

        service = new LinkedHearingGroupServiceImpl(hearingRepository,
                                                    linkedGroupDetailsRepository,
                                                    linkedHearingValidator,
                                                    linkedHearingDetailsAuditRepository,
                                                    linkedGroupDetailsAuditRepository,
                                                    linkedGroupDetailsAuditMapper,
                                                    linkedHearingDetailsAuditMapper,
                                                    futureHearingRepository,
                                                    objectMapper
        );
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
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setLinkedGroupDetails(groupDetails);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));

            LinkedGroupDetailsAudit groupDetailsAudit = createGroupDetailsAuditEntity("ACTIVE",groupDetails);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                    .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));
            given(linkedGroupDetailsAuditMapper.modelToEntity(groupDetails))
                .willReturn(groupDetailsAudit);
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
        }

        @Test
        void shouldReturn404ErrorWhenNonExistentHearingGroup() {
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.empty());

            Exception exception = assertThrows(LinkedHearingGroupNotFoundException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("No hearing group found for reference: " + HEARING_GROUP_ID, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusPENDING() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "PENDING");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("007 group is in a PENDING state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusERROR() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ERROR");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("007 group is in a ERROR state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
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
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("008 Invalid state for unlinking hearing request " + HEARING_ID2,
                         exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
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
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_PAST)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("008 Invalid state for unlinking hearing request " + HEARING_ID1,
                         exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
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

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("008 Invalid state for unlinking hearing request " + HEARING_ID1,
                         exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
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
                                                           List.of(START_DATE_TIME_IN_THE_PAST,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

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
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
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
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");
            hearing.setLinkedGroupDetails(groupDetails);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            doNothing().when(futureHearingRepository).deleteLinkedHearingGroup(REQUEST_ID);
            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
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
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            listAssistThrows4xxError();
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            final HearingManagementInterfaceResponse response = getHearingResponseFromListAssist(
                400, "005 rejected by List Assist");
            assertEquals(REJECTED_BY_LIST_ASSIST, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedGroupDetailsRepository, times(1)).delete(groupDetails);
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
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));
            final HearingManagementInterfaceResponse response = getHearingResponseFromListAssist(
                500, "006 List Assist failed to respond");
            listAssistThrows5xxError();
            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals(LIST_ASSIST_FAILED_TO_RESPOND, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
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
