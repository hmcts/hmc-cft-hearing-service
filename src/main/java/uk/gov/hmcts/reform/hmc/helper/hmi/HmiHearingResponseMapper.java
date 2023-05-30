package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingAttendee;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingJoh;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingSession;
import uk.gov.hmcts.reform.hmc.client.hmi.SyncResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.VenueLocationReference;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HmcHearingUpdate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.UPDATE_SUBMITTED;
import static uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl.UNSUPPORTED_HEARING_STATUS;

@Component
public class HmiHearingResponseMapper {
    public HearingEntity mapHmiHearingToEntity(HearingResponse hearing, HearingEntity hearingEntity) {
        HearingResponseEntity hearingResponseEntity = mapHearingResponseEntity(hearing, hearingEntity);

        List<HearingSession> hearingSessions = hearing.getHearing().getHearingSessions();
        List<HearingDayDetailsEntity> hearingDayDetailsEntitiesList = new ArrayList<>();
        if (hearingSessions != null && !hearingSessions.isEmpty()) {

            List<HearingSession> uniqueHearingSessionsPerDay = findUniqueHearingSessionsPerDay(hearingSessions);

            // put total attendees and panels here to save later?
            for (HearingSession hearingSession : uniqueHearingSessionsPerDay) {
                List<HearingDayDetailsEntity> hearingDayDetailsEntities =
                    mapHearingDayDetailsFromSessionDetails(hearingSession);
                List<HearingAttendeeDetailsEntity> hearingAttendeeDetailsEntities =
                    mapHearingAttendeeDetailsFromSessionDetails(hearingSession.getHearingAttendees());
                List<HearingDayPanelEntity> hearingDayPanelEntities =
                    mapHearingDayPanelFromSessionDetails(hearingSession.getHearingJohs());

                for (HearingDayDetailsEntity hearingDayDetailsEntity : hearingDayDetailsEntities) {
                    setHearingDayDetails(hearingResponseEntity,
                                         hearingDayDetailsEntity,
                                         hearingDayPanelEntities,
                                         hearingAttendeeDetailsEntities);
                }
                hearingDayDetailsEntitiesList.addAll(hearingDayDetailsEntities);
            }
        }
        HearingDayDetailsEntity hearingDayDetailsEntity = mapHearingDayDetailsEntity(hearing);
        List<HearingAttendeeDetailsEntity> hearingAttendeeDetailsEntities =
            mapHearingAttendeeDetailsEntity(hearing);
        List<HearingDayPanelEntity> hearingDayPanelEntities = mapHearingDayPanelEntity(hearing);

        setHearingDayDetails(
            hearingResponseEntity,
            hearingDayDetailsEntity,
            hearingDayPanelEntities,
            hearingAttendeeDetailsEntities
        );
        hearingResponseEntity.setHearingDayDetails(new ArrayList<>(List.of(hearingDayDetailsEntity)));

        hearingDayDetailsEntitiesList.add(hearingDayDetailsEntity);
        hearingResponseEntity.setHearingDayDetails(hearingDayDetailsEntitiesList);

        hearingEntity.getHearingResponses().add(hearingResponseEntity);
        hearingEntity.setStatus(getHearingStatus(hearing, hearingEntity).name());
        return hearingEntity;
    }

    private List<HearingSession> findUniqueHearingSessionsPerDay(List<HearingSession> hearingSessions) {

        final Map<LocalDateTime, List<HearingSession>> uniqueDays = hearingSessions.stream()
                .collect(
                        Collectors.groupingBy(hearingSession ->
                                hearingSession.getHearingStartTime().truncatedTo(DAYS)));

        return uniqueDays.keySet().stream().map(date -> {
            HearingSession hearingSessionWithEarliestStartTime =
                    uniqueDays.get(date).stream().min(Comparator.comparing(HearingSession::getHearingStartTime)).get();
            HearingSession hearingSessionWithLatestEndTime =
                    uniqueDays.get(date).stream().max(Comparator.comparing(HearingSession::getHearingEndTime)).get();

            hearingSessionWithEarliestStartTime.setHearingEndTime(hearingSessionWithLatestEndTime.getHearingEndTime());

            return hearingSessionWithEarliestStartTime;
        }).collect(Collectors.toList());
    }

    private void setHearingDayDetails(HearingResponseEntity hearingResponseEntity,
                                      HearingDayDetailsEntity hearingDayDetailsEntity,
                                      List<HearingDayPanelEntity> hearingDayPanelEntity,
                                      List<HearingAttendeeDetailsEntity> hearingAttendeeDetailsEntity
    ) {
        hearingDayDetailsEntity.setHearingResponse(hearingResponseEntity);
        hearingDayDetailsEntity.setHearingDayPanel(hearingDayPanelEntity);
        hearingDayDetailsEntity.setHearingAttendeeDetails(hearingAttendeeDetailsEntity);

        for (HearingDayPanelEntity hdpe : hearingDayDetailsEntity.getHearingDayPanel()) {
            hdpe.setHearingDayDetails(hearingDayDetailsEntity);
        }
        for (HearingAttendeeDetailsEntity hade : hearingDayDetailsEntity.getHearingAttendeeDetails()) {
            hade.setHearingDayDetails(hearingDayDetailsEntity);
        }
    }

    public HearingEntity mapHmiSyncResponseToEntity(SyncResponse syncResponse, HearingEntity hearingEntity) {
        if (!syncResponse.isSuccess()) {
            hearingEntity.setErrorCode(syncResponse.getListAssistErrorCode());
            hearingEntity.setErrorDescription(syncResponse.getListAssistErrorDescription());
            hearingEntity.setStatus(EXCEPTION.name());
        } else {
            hearingEntity.setStatus(getHearingStatusForSyncResponse(hearingEntity).name());
        }
        return hearingEntity;
    }

    public HearingEntity mapHmiHearingErrorToEntity(ErrorDetails hearing, HearingEntity hearingEntity) {
        hearingEntity.setErrorCode(hearing.getErrorCode());
        hearingEntity.setErrorDescription(hearing.getErrorDescription());
        hearingEntity.setStatus(EXCEPTION.name());
        return hearingEntity;
    }

    public HmcHearingResponse mapEntityToHmcModel(HearingResponseEntity hearingResponseEntity, HearingEntity hearing) {
        HmcHearingResponse hmcHearingResponse = new HmcHearingResponse();
        hmcHearingResponse.setHearingID(hearing.getId().toString());
        CaseHearingRequestEntity matchingCaseHearingRequestEntity = hearing
            .getCaseHearingRequest(hearingResponseEntity.getRequestVersion());
        hmcHearingResponse.setCaseRef(matchingCaseHearingRequestEntity.getCaseReference());
        hmcHearingResponse.setHmctsServiceCode(matchingCaseHearingRequestEntity.getHmctsServiceCode());

        //There is currently only support for one hearingDayDetail to be provided in HearingResponse From ListAssist
        HmcHearingUpdate hmcHearingUpdate = new HmcHearingUpdate();
        hmcHearingUpdate.setHmcStatus(hearing.getStatus());
        if (HearingStatus.valueOf(hearing.getStatus()) != EXCEPTION) {
            hmcHearingUpdate.setHearingResponseReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
            hmcHearingUpdate.setHearingEventBroadcastDateTime(LocalDateTime.now(Clock.systemUTC()));
            if (hearingResponseEntity.getListingStatus() != null) {
                hmcHearingUpdate.setHearingListingStatus(hearingResponseEntity.getListingStatus());
            }
            hmcHearingUpdate.setNextHearingDate(hearingResponseEntity.getHearingDayDetails().get(0).getStartDateTime());
            hmcHearingUpdate.setHearingVenueId(hearingResponseEntity.getHearingDayDetails().get(0).getVenueId());
            for (HearingDayPanelEntity hearingDayPanelEntity :
                hearingResponseEntity.getHearingDayDetails().get(0).getHearingDayPanel()) {
                if (Boolean.TRUE.equals(hearingDayPanelEntity.getIsPresiding())) {
                    hmcHearingUpdate.setHearingJudgeId(hearingDayPanelEntity.getPanelUserId());
                }
            }
            hmcHearingUpdate.setListAssistCaseStatus(HearingCode.getByLabel(hearingResponseEntity
                                                                                      .getListingCaseStatus()).name());
            hmcHearingUpdate.setHearingRoomId(hearingResponseEntity.getHearingDayDetails().get(0).getRoomId());
        }
        hmcHearingResponse.setHearingUpdate(hmcHearingUpdate);
        return hmcHearingResponse;
    }

    public HmcHearingResponse mapEntityToHmcModel(HearingEntity hearing) {
        HmcHearingResponse hmcHearingResponse = new HmcHearingResponse();
        hmcHearingResponse.setHearingID(hearing.getId().toString());
        CaseHearingRequestEntity matchingCaseHearingRequestEntity = hearing.getLatestCaseHearingRequest();
        hmcHearingResponse.setCaseRef(matchingCaseHearingRequestEntity.getCaseReference());
        hmcHearingResponse.setHmctsServiceCode(matchingCaseHearingRequestEntity.getHmctsServiceCode());

        HmcHearingUpdate hmcHearingUpdate = new HmcHearingUpdate();
        hmcHearingUpdate.setHmcStatus(hearing.getStatus());
        if (HearingStatus.valueOf(hearing.getStatus()) != EXCEPTION) {
            hmcHearingUpdate.setHearingEventBroadcastDateTime(LocalDateTime.now(Clock.systemUTC()));
        }
        hmcHearingResponse.setHearingUpdate(hmcHearingUpdate);
        return hmcHearingResponse;
    }

    private ArrayList<HearingDayPanelEntity> mapHearingDayPanelEntity(HearingResponse hearing) {
        ArrayList<HearingDayPanelEntity> hearingDayPanelEntityArrayList = new ArrayList<>();
        if (hearing.getHearing().getHearingJohs() != null) {
            for (HearingJoh hearingJoh : hearing.getHearing().getHearingJohs()) {
                HearingDayPanelEntity hearingDayPanelEntity = new HearingDayPanelEntity();
                hearingDayPanelEntity.setPanelUserId(hearingJoh.getJohCode());
                hearingDayPanelEntity.setIsPresiding(hearingJoh.getIsPresiding());
                hearingDayPanelEntityArrayList.add(hearingDayPanelEntity);
            }
        }
        return hearingDayPanelEntityArrayList;
    }

    private ArrayList<HearingAttendeeDetailsEntity> mapHearingAttendeeDetailsEntity(HearingResponse hearing) {

        ArrayList<HearingAttendeeDetailsEntity> hearingAttendeeDetailsEntityArrayList = new ArrayList<>();
        if (hearing.getHearing().getHearingAttendees() != null) {
            for (HearingAttendee hearingAttendee : hearing.getHearing().getHearingAttendees()) {
                HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity = new HearingAttendeeDetailsEntity();
                if (hearingAttendee.getEntityId() != null) {
                    hearingAttendeeDetailsEntity.setPartyId(hearingAttendee.getEntityId());
                }
                if (hearingAttendee.getHearingChannel() != null) {
                    hearingAttendeeDetailsEntity.setPartySubChannelType(hearingAttendee.getHearingChannel().getCode());
                }
                hearingAttendeeDetailsEntityArrayList.add(hearingAttendeeDetailsEntity);
            }
        }

        return hearingAttendeeDetailsEntityArrayList;
    }

    private List<HearingDayDetailsEntity> mapHearingDayDetailsFromSessionDetails(HearingSession hearingSession) {

        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();

        hearingDayDetailsEntity.setStartDateTime(hearingSession.getHearingStartTime());
        hearingDayDetailsEntity.setEndDateTime(hearingSession.getHearingEndTime());

        for (VenueLocationReference venueLocationReference :
            hearingSession.getHearingVenue().getLocationReferences()) {
            if (venueLocationReference.getKey().equals("EPIMS")) {
                hearingDayDetailsEntity.setVenueId(venueLocationReference.getValue());
            }
        }
        hearingDayDetailsEntity.setRoomId(hearingSession.getHearingRoom().getLocationName());

        List<HearingDayDetailsEntity> hearingDayDetailsEntities = new ArrayList<>();
        hearingDayDetailsEntities.add(hearingDayDetailsEntity);
        return hearingDayDetailsEntities;
    }

    private ArrayList<HearingAttendeeDetailsEntity> mapHearingAttendeeDetailsFromSessionDetails(
        List<HearingAttendee> hearingAttendees) {
        ArrayList<HearingAttendeeDetailsEntity> hearingAttendeeDetailsEntities = new ArrayList<>();

        if (hearingAttendees != null) {
            for (HearingAttendee hearingAttendee : hearingAttendees) {
                HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity = new HearingAttendeeDetailsEntity();
                hearingAttendeeDetailsEntity.setPartyId(hearingAttendee.getEntityId());
                if (hearingAttendee.getHearingChannel() != null) {
                    hearingAttendeeDetailsEntity.setPartySubChannelType(hearingAttendee.getHearingChannel().getCode());
                }
                hearingAttendeeDetailsEntities.add(hearingAttendeeDetailsEntity);
            }
        }

        return hearingAttendeeDetailsEntities;
    }

    private ArrayList<HearingDayPanelEntity> mapHearingDayPanelFromSessionDetails(List<HearingJoh> hearingJohs) {
        ArrayList<HearingDayPanelEntity> hearingDayPanelEntities = new ArrayList<>();
        if (hearingJohs != null) {
            for (HearingJoh hearingJoh : hearingJohs) {
                HearingDayPanelEntity hearingDayPanelEntity = new HearingDayPanelEntity();
                hearingDayPanelEntity.setPanelUserId(hearingJoh.getJohCode());
                hearingDayPanelEntity.setIsPresiding(hearingJoh.getIsPresiding());
                hearingDayPanelEntities.add(hearingDayPanelEntity);
            }
        }
        return hearingDayPanelEntities;
    }

    private HearingDayDetailsEntity mapHearingDayDetailsEntity(HearingResponse hearing) {
        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        if (hearing.getHearing().getHearingStartTime() != null) {
            hearingDayDetailsEntity.setStartDateTime(hearing.getHearing().getHearingStartTime());
        }
        if (hearing.getHearing().getHearingEndTime() != null) {
            hearingDayDetailsEntity.setEndDateTime(hearing.getHearing().getHearingEndTime());
        }
        if (hearing.getHearing().getHearingVenue() != null
            && hearing.getHearing().getHearingVenue().getLocationReferences() != null)  {
            for (VenueLocationReference venueLocationReference :
                hearing.getHearing().getHearingVenue().getLocationReferences()) {
                if (venueLocationReference.getKey().equals("EPIMS")) {
                    hearingDayDetailsEntity.setVenueId(venueLocationReference.getValue());
                }
            }
        }
        if (hearing.getHearing().getHearingRoom() != null
            && hearing.getHearing().getHearingRoom().getLocationName() != null) {
            hearingDayDetailsEntity.setRoomId(hearing.getHearing().getHearingRoom().getLocationName());
        }
        return hearingDayDetailsEntity;
    }

    private HearingResponseEntity mapHearingResponseEntity(HearingResponse hearingResponse, HearingEntity hearing) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setHearing(hearing);
        hearingResponseEntity.setListingTransactionId(hearingResponse.getMeta().getTransactionIdCaseHQ());
        hearingResponseEntity.setRequestTimeStamp(hearingResponse.getMeta().getTimestamp());
        hearingResponseEntity.setRequestVersion(hearingResponse.getHearing().getHearingCaseVersionId());
        if (hearingResponse.getHearing().getHearingStatus() != null
            && hearingResponse.getHearing().getHearingStatus().getCode() != null) {
            hearingResponseEntity.setListingStatus(hearingResponse.getHearing().getHearingStatus().getCode());
        }
        hearingResponseEntity.setCancellationReasonType(hearingResponse.getHearing().getHearingCancellationReason());
        hearingResponseEntity.setTranslatorRequired(hearingResponse.getHearing().getHearingTranslatorRequired());
        hearingResponseEntity.setListingCaseStatus(HearingCode.getByNumber(hearingResponse.getHearing()
                                                       .getHearingCaseStatus().getCode()).name());
        return hearingResponseEntity;
    }

    public HearingStatus getHearingStatus(HearingResponse hearing, HearingEntity hearingEntity) {
        HearingStatus currentStatus = HearingStatus.valueOf(hearingEntity.getStatus());
        HearingCode laStatus = HearingCode.getByNumber(hearing.getHearing().getHearingCaseStatus().getCode());
        HearingStatus postStatus = null;
        switch (laStatus) {
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            case LISTED:
                int hearingVersion = hearing.getHearing().getHearingCaseVersionId();
                postStatus = getHearingStatusWhenLaStatusIsListed(currentStatus, hearingVersion,
                                                                  hearingEntity.getLatestRequestVersion());
                break;
            case PENDING_RELISTING:
                postStatus = currentStatus;
                break;
            case CLOSED:
                postStatus = getHearingStatusWhenLaStatusIsClosed(currentStatus);
                break;
            default:
                throw new MalformedMessageException(
                        getUnsupportedHearingStatusMessage(currentStatus.name(), laStatus.toString())
                );


        }
        return postStatus;
    }

    public HearingStatus getHearingStatusForSyncResponse(HearingEntity hearingEntity) {
        HearingStatus currentStatus = HearingStatus.valueOf(hearingEntity.getStatus());
        HearingStatus postStatus = null;

        switch (currentStatus) {
            case HEARING_REQUESTED:
                postStatus = AWAITING_LISTING;
                break;
            case UPDATE_REQUESTED:
            case UPDATE_SUBMITTED:
                postStatus = UPDATE_SUBMITTED;
                break;
            case CANCELLATION_REQUESTED:
                postStatus = CANCELLATION_SUBMITTED;
                break;
            case CANCELLED:
                postStatus = EXCEPTION;
                break;
            default:
                postStatus = currentStatus;
        }
        return postStatus;
    }

    private HearingStatus getHearingStatusWhenLaStatusIsListed(HearingStatus currentStatus,
                                                               int hearingVersion,
                                                               int currentVersion) {
        HearingStatus postStatus = null;
        switch (currentStatus) {
            case AWAITING_LISTING:
            case UPDATE_SUBMITTED:
            case LISTED:
                postStatus = HearingStatus.LISTED;
                break;
            case UPDATE_REQUESTED:
                if (hearingVersion == currentVersion) {
                    postStatus = HearingStatus.LISTED;
                } else {
                    postStatus = HearingStatus.UPDATE_REQUESTED;
                }
                break;
            case CANCELLATION_REQUESTED:
                if (hearingVersion == currentVersion) {
                    postStatus = EXCEPTION;
                } else {
                    postStatus = HearingStatus.CANCELLATION_REQUESTED;
                }
                break;
            case CANCELLATION_SUBMITTED:
                if (hearingVersion == currentVersion) {
                    postStatus = EXCEPTION;
                } else {
                    postStatus = HearingStatus.CANCELLATION_SUBMITTED;
                }
                break;
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            default:
                throw new MalformedMessageException(
                        getUnsupportedHearingStatusMessage(currentStatus.name(), ListAssistCaseStatus.LISTED.name()));
        }
        return postStatus;
    }

    private HearingStatus getHearingStatusWhenLaStatusIsClosed(HearingStatus currentStatus) {
        HearingStatus postStatus = null;
        switch (currentStatus) {
            case AWAITING_LISTING:
            case UPDATE_SUBMITTED:
            case LISTED:
            case UPDATE_REQUESTED:
            case CANCELLATION_REQUESTED:
            case CANCELLATION_SUBMITTED:
                postStatus = HearingStatus.CANCELLED;
                break;
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            default:
                throw new MalformedMessageException(
                        getUnsupportedHearingStatusMessage(currentStatus.name(),
                                ListAssistCaseStatus.CASE_CLOSED.name()));
        }
        return postStatus;
    }

    private String getUnsupportedHearingStatusMessage(String currentStatus, String laStatus) {
        return new StringBuilder(UNSUPPORTED_HEARING_STATUS)
                .append("; current status:").append(currentStatus)
                .append("; LA status:").append(laStatus).toString();
    }
}
