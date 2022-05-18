package uk.gov.hmcts.reform.hmc.helper;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.Hearing;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingAttendee;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCaseStatus;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingChannel;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingJoh;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingRoom;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingSession;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingVenue;
import uk.gov.hmcts.reform.hmc.client.hmi.MetaResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.SyncResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.VenueLocationReference;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.LISTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.UPDATE_SUBMITTED;

class HmiHearingResponseMapperTest {


    private HmiHearingResponseMapper hmiHearingResponseMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hmiHearingResponseMapper = new HmiHearingResponseMapper();
    }

    @Test
    void mapHmiHearingToEntityErrorPayload() {
        HearingEntity response = hmiHearingResponseMapper.mapHmiHearingErrorToEntity(
            generateErrorDetails("random", 1),
            generateHearingEntity("AWAITING_LISTING", 1)
        );
        assertAll(
            () -> assertThat(response.getStatus(), is(EXCEPTION.name())),
            () -> assertThat(response.getErrorCode(), is(1)),
            () -> assertThat(response.getErrorDescription(), is("random"))
        );
    }

    @Test
    void mapHmiHearingToEntity() {
        HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
            generateHmiHearing("random", HearingCode.EXCEPTION, 1, ListingStatus.DRAFT),
            generateHearingEntity("AWAITING_LISTING", 1)
        );
        assertAll(
            () -> assertThat(response.getHearingResponses().size(), is(2)),
            () -> assertThat(
                response.getHearingResponses().get(1).getListingTransactionId(),
                is("transactionIdCaseHQ")
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getRequestTimeStamp(),
                is(LocalDateTime.parse("2021-08-10T12:20:00"))
            ),
            () -> assertThat(response.getHearingResponses().get(1).getRequestVersion(), is(1)),
            () -> assertThat(response.getHearingResponses().get(1).getListingStatus(), is(ListingStatus.DRAFT.name())),
            () -> assertThat(response.getHearingResponses().get(1).getCancellationReasonType(), is("reason")),
            () -> assertThat(response.getHearingResponses().get(1).getTranslatorRequired(), is(true)),
            () -> assertThat(response.getHearingResponses().get(1).getListingCaseStatus(), is(EXCEPTION.name())),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getStartDateTime(),
                is(LocalDateTime.parse("2021-08-10T12:20:00"))
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getEndDateTime(),
                is(LocalDateTime.parse("2021-08-10T12:20:00"))
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getRoomId(),
                is("roomName")
            ),
            () -> assertNull(response.getHearingResponses().get(1).getHearingDayDetails().get(0).getVenueId()),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                                 .getHearingAttendeeDetails().get(0).getPartyId(), is("entityId")),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                    .getHearingAttendeeDetails().get(0).getPartySubChannelType(),
                is("codeSubChannel")
            ),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                                 .getHearingDayPanel().get(0).getPanelUserId(), is("JohCode")),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                                 .getHearingDayPanel().get(0).getIsPresiding(), is(true))
        );
    }

    @Test
    void mapHmiMultiDayHearingToEntity() {
        HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
            generateHmiMultiDayHearing("random", HearingCode.EXCEPTION, 1, ListingStatus.DRAFT),
            generateHearingEntity("AWAITING_LISTING", 1)
        );
        assertAll(
            () -> assertThat(response.getHearingResponses().size(), is(2)),
            () -> assertThat(
                response.getHearingResponses().get(1).getListingTransactionId(),
                is("transactionIdCaseHQ")
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getRequestTimeStamp(),
                is(LocalDateTime.parse("2021-08-10T12:20:00"))
            ),
            () -> assertThat(response.getHearingResponses().get(1).getRequestVersion(), is(1)),
            () -> assertThat(response.getHearingResponses().get(1).getListingStatus(), is(ListingStatus.DRAFT.name())),
            () -> assertThat(response.getHearingResponses().get(1).getCancellationReasonType(), is("reason")),
            () -> assertThat(response.getHearingResponses().get(1).getTranslatorRequired(), is(true)),
            () -> assertThat(response.getHearingResponses().get(1).getListingCaseStatus(), is(EXCEPTION.name())),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getStartDateTime(),
                is(LocalDateTime.parse("2021-10-11T12:20:00"))
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getEndDateTime(),
                is(LocalDateTime.parse("2021-10-12T12:20:00"))
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getRoomId(),
                is("multiDayRoomName")
            ),
            () -> assertNull(response.getHearingResponses().get(1).getHearingDayDetails().get(0).getVenueId()),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                                 .getHearingAttendeeDetails().get(0).getPartyId(), is("entityId")),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                    .getHearingAttendeeDetails().get(0).getPartySubChannelType(),
                is("codeSubChannel")
            ),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                                 .getHearingDayPanel().get(0).getPanelUserId(), is("JohCode")),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                                 .getHearingDayPanel().get(0).getIsPresiding(), is(true))
        );
    }

    @Test
    void mapHmiHearingToEntityWithEpims() {
        HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
            generateHmiHearing("EPIMS", HearingCode.EXCEPTION, 1, ListingStatus.DRAFT),
            generateHearingEntity("AWAITING_LISTING", 1)
        );
        assertAll(
            () -> assertThat(response.getHearingResponses().size(), is(2)),
            () -> assertThat(
                response.getHearingResponses().get(1).getListingTransactionId(),
                is("transactionIdCaseHQ")
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getRequestTimeStamp(),
                is(LocalDateTime.parse("2021-08-10T12:20:00"))
            ),
            () -> assertThat(response.getHearingResponses().get(1).getRequestVersion(), is(1)),
            () -> assertThat(response.getHearingResponses().get(1).getListingStatus(), is(ListingStatus.DRAFT.name())),
            () -> assertThat(response.getHearingResponses().get(1).getCancellationReasonType(), is("reason")),
            () -> assertThat(response.getHearingResponses().get(1).getTranslatorRequired(), is(true)),
            () -> assertThat(response.getHearingResponses().get(1).getListingCaseStatus(), is(EXCEPTION.name())),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getStartDateTime(),
                is(LocalDateTime.parse("2021-08-10T12:20:00"))
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getEndDateTime(),
                is(LocalDateTime.parse("2021-08-10T12:20:00"))
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getRoomId(),
                is("roomName")
            ),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0).getVenueId(),
                is("value")
            ),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails()
                                 .get(0).getHearingAttendeeDetails().get(0).getPartyId(), is("entityId")),
            () -> assertThat(
                response.getHearingResponses().get(1).getHearingDayDetails()
                    .get(0).getHearingAttendeeDetails().get(0).getPartySubChannelType(),
                is("codeSubChannel")
            ),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails()
                                 .get(0).getHearingDayPanel().get(0).getPanelUserId(), is("JohCode")),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails()
                                 .get(0).getHearingDayPanel().get(0).getIsPresiding(), is(true))
        );
    }

    @Test
    void mapHmiSyncResponseToEntityForSuccess() {

        HearingEntity response = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
            generateSyncResponse(404, 20000, "unable to create case"),
            generateHearingEntity("AWAITING_LISTING", 1, 1L)
        );
        assertAll(
            () -> assertThat(response.getErrorDescription(), is("unable to create case")),
            () -> assertThat(response.getErrorCode(), is(20000)),
            () -> assertThat(response.getStatus(), is(HearingCode.EXCEPTION.name()))
        );
    }

    @Test
    void mapHmiHearingToEntityWhenHearingChannelIsNotPresent() {
        HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
            generateHmiHearing_NoHearingChannel_Joh("random", HearingCode.EXCEPTION, 1,
                                                    ListingStatus.DRAFT),
            generateHearingEntity("AWAITING_LISTING", 1)
        );
        assertAll(
            () -> assertThat(response.getHearingResponses().size(), is(2)),
            () -> assertThat(response.getHearingResponses().get(1).getListingCaseStatus(), is(EXCEPTION.name())),
            () -> assertNull(
                response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                    .getHearingAttendeeDetails().get(0).getPartySubChannelType()),
            () -> assertThat(response.getHearingResponses().get(1).getHearingDayDetails().get(0)
                                 .getHearingDayPanel().size(), is(0))
        );
    }

    @Nested
    @DisplayName("getPostStateForSyncResponse")
    class GetPostStateForSyncResponse {
        @Test
        void mapHmiSyncResponseToEntityForErrorPreStateHearingRequestPostStateAwaitingListing() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
                generateSyncResponse(200, 20000, "unable to create case"),
                generateHearingEntity(HEARING_REQUESTED.name(), 1, 1L)
            );
            assertAll(
                () -> assertThat(response.getStatus(), is(AWAITING_LISTING.name()))
            );
        }

        @Test
        void mapHmiSyncResponseToEntityForErrorPreStateUpdateRequestedPostStateUpdateSubmitted() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
                generateSyncResponse(200, 20000, "unable to create case"),
                generateHearingEntity(UPDATE_REQUESTED.name(), 1, 1L)
            );
            assertAll(
                () -> assertThat(response.getStatus(), is(UPDATE_SUBMITTED.name()))
            );
        }

        @Test
        void mapHmiSyncResponseToEntityForErrorPreStateUpdateSubmittedPostStateUpdateSubmitted() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
                generateSyncResponse(200, 20000, "unable to create case"),
                generateHearingEntity(UPDATE_SUBMITTED.name(), 1, 1L)
            );
            assertAll(
                () -> assertThat(response.getStatus(), is(UPDATE_SUBMITTED.name()))
            );
        }

        @Test
        void mapHmiSyncResponseToEntityForErrorPreStateCancellationSubmittedPostStateCancellationSubmitted() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
                generateSyncResponse(200, 20000, "unable to create case"),
                generateHearingEntity(CANCELLATION_REQUESTED.name(), 1, 1L)
            );
            assertAll(
                () -> assertThat(response.getStatus(), is(CANCELLATION_SUBMITTED.name()))
            );
        }

        @Test
        void mapHmiSyncResponseToEntityForErrorPreStateCancelledPostStateException() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
                generateSyncResponse(200, 20000, "unable to create case"),
                generateHearingEntity(CANCELLED.name(), 1, 1L)
            );
            assertAll(
                () -> assertThat(response.getStatus(), is(EXCEPTION.name()))
            );
        }
    }

    @Test
    void mapHmiHearingToEntityToHmcModel() {
        HmcHearingResponse response = hmiHearingResponseMapper.mapEntityToHmcModel(
            generateHearingResponseEntity(1,
                                          LocalDateTime.of(2019, 1, 10, 11, 20, 00),
                                          ListingStatus.DRAFT.name(),
                                          LocalDateTime.of(2019, 1, 10, 11, 20, 00),
                                          "12", true, "11", HearingCode.LISTED.name()),
            generateHearingEntity("AWAITING_LISTING", 1, 1L)
        );
        assertAll(
            () -> assertThat(response.getHearingID(), is("1")),
            () -> assertThat(response.getHearingUpdate().getHearingResponseReceivedDateTime(),
                             is(LocalDateTime.parse("2019-01-10T11:20"))),
            () -> assertThat(response.getHearingUpdate().getHmcStatus(), is("AWAITING_LISTING")),
            () -> assertThat(response.getHearingUpdate().getHearingListingStatus(), is(ListingStatus.DRAFT)),
            () -> assertThat(response.getHearingUpdate().getNextHearingDate(),
                             is(LocalDateTime.parse("2019-01-10T11:20"))),
            () -> assertThat(response.getHearingUpdate().getHearingVenueId(), is("12")),
            () -> assertThat(response.getHearingUpdate().getHearingJudgeId(), is("11")),
            () -> assertThat(response.getHearingUpdate().getListAssistCaseStatus(), is(HearingCode.LISTED.name()))
        );
    }

    @Test
    void mapHmiHearingToEntityToHmcModelForError() {
        HmcHearingResponse response = hmiHearingResponseMapper.mapEntityToHmcModel(
            generateHearingEntity("AWAITING_LISTING", 1, 1L)
        );
        assertAll(
            () -> assertThat(response.getHearingID(), is("1")),
            () -> assertThat(response.getHearingUpdate().getHmcStatus(), is("AWAITING_LISTING"))
        );
    }

    @Nested
    @DisplayName("getHearingStatus")
    class GetHearingStatus {

        @Test
        void shouldGetPostStateOfExceptionWhenLaStateIsException() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.EXCEPTION, 1, ListingStatus.DRAFT),
                generateHearingEntity("AWAITING_LISTING", 1)
            );
            assertEquals(response.getStatus(), EXCEPTION.name());
        }

        @Test
        void shouldGetPostStateOfCurrentStateWhenLaStateIsPendingRelisting() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.PENDING_RELISTING, 1, ListingStatus.DRAFT),
                generateHearingEntity("AWAITING_LISTING", 1)
            );
            assertEquals(response.getStatus(), AWAITING_LISTING.name());
        }

        @Test
        void shouldGetPostStateOfClosedWhenLaStateIsClosedAndCurrentIsAwaitingListing() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.CLOSED, 1, ListingStatus.DRAFT),
                generateHearingEntity("AWAITING_LISTING", 1)
            );
            assertEquals(response.getStatus(), CANCELLED.name());
        }

        @Test
        void shouldGetPostStateOfClosedWhenLaStateIsClosedAndCurrentIsUpdateSubmitted() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.CLOSED, 1, ListingStatus.DRAFT),
                generateHearingEntity("UPDATE_SUBMITTED", 1)
            );
            assertEquals(response.getStatus(), CANCELLED.name());
        }

        @Test
        void shouldGetPostStateOfClosedWhenLaStateIsClosedAndCurrentIsListed() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.CLOSED, 1, ListingStatus.DRAFT),
                generateHearingEntity("LISTED", 1)
            );
            assertEquals(response.getStatus(), CANCELLED.name());
        }

        @Test
        void shouldGetPostStateOfClosedWhenLaStateIsClosedAndCurrentIsUpdatedRequested() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.CLOSED, 1, ListingStatus.DRAFT),
                generateHearingEntity("UPDATE_REQUESTED", 1)
            );
            assertEquals(response.getStatus(), CANCELLED.name());
        }

        @Test
        void shouldGetPostStateOfClosedWhenLaStateIsClosedAndCurrentIsCancellationRequested() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.CLOSED, 1, ListingStatus.DRAFT),
                generateHearingEntity("CANCELLATION_REQUESTED", 1)
            );
            assertEquals(response.getStatus(), CANCELLED.name());
        }

        @Test
        void shouldGetPostStateOfExceptionWhenLaStateIsClosedAndCurrentIsException() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.CLOSED, 1, ListingStatus.DRAFT),
                generateHearingEntity("EXCEPTION", 1)
            );
            assertEquals(response.getStatus(), EXCEPTION.name());
        }

        @Test
        void shouldGetPostStateOfListedWhenLaStateIsListedAndCurrentIsAwaitingListing() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                generateHearingEntity("AWAITING_LISTING", 1)
            );
            assertEquals(response.getStatus(), LISTED.name());
        }

        @Test
        void shouldGetPostStateOfListedWhenLaStateIsListedAndCurrentIsUpdateSubmitted() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                generateHearingEntity("UPDATE_SUBMITTED", 1)
            );
            assertEquals(response.getStatus(), LISTED.name());
        }

        @Test
        void shouldGetPostStateOfListedWhenLaStateIsListedAndCurrentIsListed() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                generateHearingEntity("LISTED", 1)
            );
            assertEquals(response.getStatus(), LISTED.name());
        }

        @Test
        void shouldGetPostStateOfExceptionWhenLaStateIsListedAndCurrentIsException() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                generateHearingEntity("EXCEPTION", 1)
            );
            assertEquals(response.getStatus(), EXCEPTION.name());
        }

        @Test
        void shouldGetPostStateOfListedWhenLaStateIsListedAndCurrentIsUpdateRequestedAndVersionIsEqual() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                generateHearingEntity("UPDATE_REQUESTED", 1)
            );
            assertEquals(response.getStatus(), LISTED.name());
        }

        @Test
        void shouldGetPostStateOfUpdateRequestedWhenLaStateIsListedAndCurrentIsUpdateRequestedAndVersionIsNotEqual() {
            HearingEntity hearingEntity = generateHearingEntity("UPDATE_REQUESTED", 11);
            CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
            caseHearingRequestEntity.setVersionNumber(1);
            hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntity);

            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                hearingEntity
            );
            assertEquals(response.getStatus(), UPDATE_REQUESTED.name());
        }

        @Test
        void shouldGetPostStateOfExceptionWhenLaStateIsListedAndCurrentIsCancellationRequestedAndVersionIsEqual() {
            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                generateHearingEntity("CANCELLATION_REQUESTED", 1)
            );
            assertEquals(response.getStatus(), EXCEPTION.name());
        }

        @Test
        void shouldGetPostStateOfCancellationRequestedWhenLaStateIsListedAndCurrentIsCancellationRequested() {
            HearingEntity hearingEntity = generateHearingEntity("CANCELLATION_REQUESTED", 11);
            CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
            caseHearingRequestEntity.setVersionNumber(1);
            hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntity);

            HearingEntity response = hmiHearingResponseMapper.mapHmiHearingToEntity(
                generateHmiHearing("random", HearingCode.LISTED, 1, ListingStatus.DRAFT),
                hearingEntity
            );
            assertEquals(response.getStatus(), CANCELLATION_REQUESTED.name());
        }
    }

    private HearingResponse generateHmiHearing(String key, HearingCode hearingCode, int version, ListingStatus status) {
        HearingResponse hearingResponse = new HearingResponse();

        MetaResponse metaResponse = new MetaResponse();
        metaResponse.setTimestamp(LocalDateTime.parse("2021-08-10T12:20:00"));
        metaResponse.setTransactionIdCaseHQ("transactionIdCaseHQ");
        hearingResponse.setMeta(metaResponse);

        Hearing hearing = new Hearing();
        hearing.setHearingCaseVersionId(version);
        hearing.setHearingCancellationReason("reason");
        hearing.setHearingStartTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearing.setHearingEndTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearing.setHearingTranslatorRequired(true);

        uk.gov.hmcts.reform.hmc.client.hmi.HearingStatus hearingStatus =
            new uk.gov.hmcts.reform.hmc.client.hmi.HearingStatus();
        hearingStatus.setCode(status);
        hearing.setHearingStatus(hearingStatus);

        HearingCaseStatus hearingCaseStatus = new HearingCaseStatus();
        hearingCaseStatus.setCode(String.valueOf(HearingCode.getNumber(hearingCode)));
        hearing.setHearingCaseStatus(hearingCaseStatus);

        HearingVenue hearingVenue = new HearingVenue();
        VenueLocationReference venueLocationReference = new VenueLocationReference();
        venueLocationReference.setKey(key);
        venueLocationReference.setValue("value");
        hearingVenue.setLocationReferences(new ArrayList<>(List.of(venueLocationReference)));
        hearing.setHearingVenue(hearingVenue);

        HearingRoom hearingRoom = new HearingRoom();
        hearingRoom.setLocationName("roomName");
        hearing.setHearingRoom(hearingRoom);

        HearingAttendee hearingAttendee = new HearingAttendee();
        hearingAttendee.setEntityId("entityId");
        HearingChannel hearingChannel = new HearingChannel();
        hearingChannel.setCode("codeSubChannel");
        hearingAttendee.setHearingChannel(hearingChannel);
        hearing.setHearingAttendees(new ArrayList<>(List.of(hearingAttendee)));

        HearingJoh hearingJoh = new HearingJoh();
        hearingJoh.setJohCode("JohCode");
        hearingJoh.setIsPresiding(true);
        hearing.setHearingJohs(new ArrayList<>(List.of(hearingJoh)));

        HearingSession hearingSession = new HearingSession();
        hearingSession.setHearingVenue(hearingVenue);
        hearingSession.setHearingRoom(hearingRoom);
        hearingSession.setHearingStartTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearingSession.setHearingEndTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearing.setHearingSessions(new ArrayList<>(List.of(hearingSession)));

        hearingResponse.setHearing(hearing);
        return hearingResponse;
    }

    private HearingResponse generateHmiMultiDayHearing(String key,
                                                       HearingCode hearingCode,
                                                       int version,
                                                       ListingStatus status) {
        HearingResponse hearingResponse = new HearingResponse();

        MetaResponse metaResponse = new MetaResponse();
        metaResponse.setTimestamp(LocalDateTime.parse("2021-08-10T12:20:00"));
        metaResponse.setTransactionIdCaseHQ("transactionIdCaseHQ");
        hearingResponse.setMeta(metaResponse);

        Hearing hearing = new Hearing();
        hearing.setHearingCaseVersionId(version);
        hearing.setHearingCancellationReason("reason");
        hearing.setHearingStartTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearing.setHearingEndTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearing.setHearingTranslatorRequired(true);

        uk.gov.hmcts.reform.hmc.client.hmi.HearingStatus hearingStatus =
            new uk.gov.hmcts.reform.hmc.client.hmi.HearingStatus();
        hearingStatus.setCode(status);
        hearing.setHearingStatus(hearingStatus);

        HearingCaseStatus hearingCaseStatus = new HearingCaseStatus();
        hearingCaseStatus.setCode(String.valueOf(HearingCode.getNumber(hearingCode)));
        hearing.setHearingCaseStatus(hearingCaseStatus);

        HearingVenue hearingVenue = new HearingVenue();
        VenueLocationReference venueLocationReference = new VenueLocationReference();
        venueLocationReference.setKey(key);
        venueLocationReference.setValue("value");
        hearingVenue.setLocationReferences(new ArrayList<>(List.of(venueLocationReference)));
        hearing.setHearingVenue(hearingVenue);

        HearingRoom hearingRoom = new HearingRoom();
        hearingRoom.setLocationName("multiDayRoomName");
        hearing.setHearingRoom(hearingRoom);

        HearingAttendee hearingAttendee = new HearingAttendee();
        hearingAttendee.setEntityId("entityId");
        HearingChannel hearingChannel = new HearingChannel();
        hearingChannel.setCode("codeSubChannel");
        hearingAttendee.setHearingChannel(hearingChannel);
        hearing.setHearingAttendees(new ArrayList<>(List.of(hearingAttendee)));

        HearingJoh hearingJoh = new HearingJoh();
        hearingJoh.setJohCode("JohCode");
        hearingJoh.setIsPresiding(true);
        hearing.setHearingJohs(new ArrayList<>(List.of(hearingJoh)));

        HearingSession hearingSession = new HearingSession();
        hearingSession.setHearingStartTime(LocalDateTime.parse("2021-10-11T12:20:00"));
        hearingSession.setHearingEndTime(LocalDateTime.parse("2021-10-12T12:20:00"));
        hearingSession.setHearingRoom(hearingRoom);
        hearingSession.setHearingVenue(hearingVenue);
        hearing.setHearingSessions(List.of(hearingSession));

        hearingResponse.setHearing(hearing);
        return hearingResponse;
    }

    private HearingResponse generateHmiHearing_NoHearingChannel_Joh(String key, HearingCode hearingCode, int version,
                                                                ListingStatus status) {
        HearingResponse hearingResponse = new HearingResponse();

        MetaResponse metaResponse = new MetaResponse();
        metaResponse.setTimestamp(LocalDateTime.parse("2021-08-10T12:20:00"));
        metaResponse.setTransactionIdCaseHQ("transactionIdCaseHQ");
        hearingResponse.setMeta(metaResponse);

        Hearing hearing = new Hearing();
        hearing.setHearingCaseVersionId(version);
        hearing.setHearingCancellationReason("reason");
        hearing.setHearingStartTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearing.setHearingEndTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        hearing.setHearingTranslatorRequired(true);

        uk.gov.hmcts.reform.hmc.client.hmi.HearingStatus hearingStatus =
            new uk.gov.hmcts.reform.hmc.client.hmi.HearingStatus();
        hearingStatus.setCode(status);
        hearing.setHearingStatus(hearingStatus);

        HearingCaseStatus hearingCaseStatus = new HearingCaseStatus();
        hearingCaseStatus.setCode(String.valueOf(HearingCode.getNumber(hearingCode)));
        hearing.setHearingCaseStatus(hearingCaseStatus);

        HearingVenue hearingVenue = new HearingVenue();
        VenueLocationReference venueLocationReference = new VenueLocationReference();
        venueLocationReference.setKey(key);
        venueLocationReference.setValue("value");
        hearingVenue.setLocationReferences(new ArrayList<>(List.of(venueLocationReference)));
        hearing.setHearingVenue(hearingVenue);

        HearingRoom hearingRoom = new HearingRoom();
        hearingRoom.setLocationName("roomName");
        hearing.setHearingRoom(hearingRoom);

        HearingAttendee hearingAttendee = new HearingAttendee();
        hearingAttendee.setEntityId("entityId");
        hearing.setHearingAttendees(new ArrayList<>(List.of(hearingAttendee)));

        hearingResponse.setHearing(hearing);
        return hearingResponse;
    }

    private HearingEntity generateHearingEntity(String status, int version) {
        HearingEntity hearingEntity = new HearingEntity();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setVersionNumber(version);
        hearingEntity.setCaseHearingRequests(Lists.newArrayList(caseHearingRequestEntity));

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingEntity.setHearingResponses(Lists.newArrayList(hearingResponseEntity));
        hearingEntity.setStatus(status);

        return hearingEntity;
    }

    private HearingEntity generateHearingEntity(String status, int version, Long id) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(id);
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setVersionNumber(version);
        hearingEntity.setCaseHearingRequests(Lists.newArrayList(caseHearingRequestEntity));

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingEntity.setHearingResponses(Lists.newArrayList(hearingResponseEntity));
        hearingEntity.setStatus(status);

        return hearingEntity;
    }

    private HearingResponseEntity generateHearingResponseEntity(int requestVersion, LocalDateTime dateTime,
                                                        String listingStatus,
                                                        LocalDateTime startTime, String venueId, Boolean isPresiding,
                                                        String panelId, String listingCaseStatus) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setRequestVersion(requestVersion);
        hearingResponseEntity.setRequestTimeStamp(dateTime);
        hearingResponseEntity.setListingStatus(listingStatus);
        hearingResponseEntity.setListingCaseStatus(listingCaseStatus);

        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(startTime);
        hearingDayDetailsEntity.setVenueId(venueId);
        HearingDayPanelEntity hearingDayPanelEntity = new HearingDayPanelEntity();
        hearingDayPanelEntity.setIsPresiding(isPresiding);
        hearingDayPanelEntity.setPanelUserId(panelId);
        hearingDayDetailsEntity.setHearingDayPanel(List.of(hearingDayPanelEntity));
        hearingResponseEntity.setHearingDayDetails(List.of(hearingDayDetailsEntity));

        return hearingResponseEntity;
    }

    private ErrorDetails generateErrorDetails(String description, int code) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorDescription(description);
        errorDetails.setErrorCode(code);
        return errorDetails;
    }

    private SyncResponse generateSyncResponse(int httpCode, int errorCode, String description) {
        return SyncResponse.builder()
            .listAssistErrorCode(errorCode)
            .listAssistHttpStatus(httpCode)
            .listAssistErrorDescription(description)
            .build();
    }
}
