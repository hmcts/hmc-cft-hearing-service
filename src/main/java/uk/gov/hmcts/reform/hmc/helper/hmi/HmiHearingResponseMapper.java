package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingAttendee;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingJoh;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.SyncResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.VenueLocationReference;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HmcHearingUpdate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.UPDATE_SUBMITTED;
import static uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl.UNSUPPORTED_HEARING_STATUS;

@Component
public class HmiHearingResponseMapper {
    public HearingEntity mapHmiHearingToEntity(HearingResponse hearing, HearingEntity hearingEntity) {
        HearingResponseEntity hearingResponseEntity = mapHearingResponseEntity(hearing, hearingEntity);
        HearingDayDetailsEntity hearingDayDetailsEntity = mapHearingDayDetailsEntity(hearing);
        hearingDayDetailsEntity.setHearingResponse(hearingResponseEntity);
        ArrayList<HearingAttendeeDetailsEntity> hearingAttendeeDetailsEntity = mapHearingAttendeeDetailsEntity(hearing);
        ArrayList<HearingDayPanelEntity> hearingDayPanelEntity = mapHearingDayPanelEntity(hearing);
        hearingDayDetailsEntity.setHearingDayPanel(hearingDayPanelEntity);
        hearingDayDetailsEntity.setHearingAttendeeDetails(hearingAttendeeDetailsEntity);
        for (HearingDayPanelEntity hdpe : hearingDayDetailsEntity.getHearingDayPanel()) {
            hdpe.setHearingDayDetails(hearingDayDetailsEntity);
        }
        for (HearingAttendeeDetailsEntity hade : hearingDayDetailsEntity.getHearingAttendeeDetails()) {
            hade.setHearingDayDetails(hearingDayDetailsEntity);
        }
        hearingResponseEntity.setHearingDayDetails(List.of(hearingDayDetailsEntity));
        hearingEntity.getHearingResponses().add(hearingResponseEntity);
        hearingEntity.setStatus(getHearingStatus(hearing, hearingEntity).name());
        return hearingEntity;
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
            hmcHearingUpdate.setHearingListingStatus(ListingStatus.valueOf(hearingResponseEntity.getListingStatus()));
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
        for (HearingAttendee hearingAttendee : hearing.getHearing().getHearingAttendees()) {
            HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity = new HearingAttendeeDetailsEntity();
            hearingAttendeeDetailsEntity.setPartyId(hearingAttendee.getEntityId());
            if (hearingAttendee.getHearingChannel() != null) {
                hearingAttendeeDetailsEntity.setPartySubChannelType(hearingAttendee.getHearingChannel().getCode());
            }
            hearingAttendeeDetailsEntityArrayList.add(hearingAttendeeDetailsEntity);
        }

        return hearingAttendeeDetailsEntityArrayList;
    }

    private HearingDayDetailsEntity mapHearingDayDetailsEntity(HearingResponse hearing) {
        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(hearing.getHearing().getHearingStartTime());
        hearingDayDetailsEntity.setEndDateTime(hearing.getHearing().getHearingEndTime());
        for (VenueLocationReference venueLocationReference :
            hearing.getHearing().getHearingVenue().getLocationReferences()) {
            if (venueLocationReference.getKey().equals("EPIMS")) {
                hearingDayDetailsEntity.setVenueId(venueLocationReference.getValue());
            }
        }
        hearingDayDetailsEntity.setRoomId(hearing.getHearing().getHearingRoom().getLocationName());
        return hearingDayDetailsEntity;
    }

    private HearingResponseEntity mapHearingResponseEntity(HearingResponse hearingResponse, HearingEntity hearing) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setHearing(hearing);
        hearingResponseEntity.setListingTransactionId(hearingResponse.getMeta().getTransactionIdCaseHQ());
        hearingResponseEntity.setRequestTimeStamp(hearingResponse.getMeta().getTimestamp());
        hearingResponseEntity.setRequestVersion(hearingResponse.getHearing().getHearingCaseVersionId());
        hearingResponseEntity.setListingStatus(hearingResponse.getHearing().getHearingStatus().getCode().name());
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
                throw new MalformedMessageException(UNSUPPORTED_HEARING_STATUS);
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
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            default:
                throw new MalformedMessageException(UNSUPPORTED_HEARING_STATUS);
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
                postStatus = HearingStatus.CANCELLED;
                break;
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            default:
                throw new MalformedMessageException(UNSUPPORTED_HEARING_STATUS);
        }
        return postStatus;
    }
}
