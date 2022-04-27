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
import uk.gov.hmcts.reform.hmc.client.hmi.HearingVenue;
import uk.gov.hmcts.reform.hmc.client.hmi.MetaResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.VenueLocationReference;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;

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
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.LISTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.UPDATE_REQUESTED;

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

    private ErrorDetails generateErrorDetails(String description, int code) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorDescription(description);
        errorDetails.setErrorCode(code);
        return errorDetails;
    }
}
