package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
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
import java.util.List;

import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl.UNSUPPORTED_HEARING_STATUS;

@Component
public class HmiHearingResponseMapper {
    public HearingEntity mapHmiHearingToEntity(HearingResponse hearing, HearingEntity hearingEntity) {
        HearingResponseEntity hearingResponseEntity = mapHearingResponseEntity(hearing, hearingEntity);
        HearingDayDetailsEntity hearingDayDetailsEntity = mapHearingDayDetailsEntity(hearing);
        hearingDayDetailsEntity.setHearingResponse(hearingResponseEntity);
        HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity = mapHearingAttendeeDetailsEntity(hearing);
        HearingDayPanelEntity hearingDayPanelEntity = mapHearingDayPanelEntity(hearing);
        hearingDayDetailsEntity.setHearingDayPanel(List.of(hearingDayPanelEntity));
        hearingDayDetailsEntity.setHearingAttendeeDetails(List.of(hearingAttendeeDetailsEntity));
        hearingDayPanelEntity.setHearingDayDetails(hearingDayDetailsEntity);
        hearingAttendeeDetailsEntity.setHearingDayDetails(hearingDayDetailsEntity);
        hearingResponseEntity.setHearingDayDetails(List.of(hearingDayDetailsEntity));
        hearingEntity.getHearingResponses().add(hearingResponseEntity);
        hearingEntity.setStatus(getHearingStatus(hearing, hearingEntity).name());
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
        hmcHearingResponse.setCaseRef(hearing.getCaseHearingRequest().getCaseReference());
        hmcHearingResponse.setHmctsServiceCode(hearing.getCaseHearingRequest().getHmctsServiceID());

        HmcHearingUpdate hmcHearingUpdate = new HmcHearingUpdate();
        hmcHearingUpdate.setHearingResponseReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
        hmcHearingUpdate.setHearingEventBroadcastDateTime(LocalDateTime.now(Clock.systemUTC()));
        hmcHearingUpdate.setHmcStatus(hearing.getStatus());
        if (HearingStatus.valueOf(hearing.getStatus()) != EXCEPTION) {
            hmcHearingUpdate.setHearingListingStatus(ListingStatus.valueOf(hearingResponseEntity.getListingStatus()));
        }
        //There is currently only support for one hearingDayDetail to be provided in HearingResponse From ListAssist
        hmcHearingUpdate.setNextHearingDate(hearingResponseEntity.getHearingDayDetails().get(0).getStartDateTime());
        hmcHearingUpdate.setHearingVenueId(hearingResponseEntity.getHearingDayDetails().get(0).getVenueId());
        if (Boolean.TRUE.equals(
            hearingResponseEntity.getHearingDayDetails().get(0).getHearingDayPanel().get(0).getIsPresiding())) {
            hmcHearingUpdate.setHearingJudgeId(
                hearingResponseEntity.getHearingDayDetails().get(0).getHearingDayPanel().get(0).getPanelUserId());
        }
        hmcHearingResponse.setHearingUpdate(hmcHearingUpdate);
        return hmcHearingResponse;
    }

    private HearingDayPanelEntity mapHearingDayPanelEntity(HearingResponse hearing) {
        HearingDayPanelEntity hearingDayPanelEntity = new HearingDayPanelEntity();
        hearingDayPanelEntity.setPanelUserId(hearing.getHearing().getHearingJoh().getJohCode());
        hearingDayPanelEntity.setIsPresiding(hearing.getHearing().getHearingJoh().getIsPresiding());
        return hearingDayPanelEntity;
    }

    private HearingAttendeeDetailsEntity mapHearingAttendeeDetailsEntity(HearingResponse hearing) {
        HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity = new HearingAttendeeDetailsEntity();
        hearingAttendeeDetailsEntity.setPartyId(hearing.getHearing().getHearingAttendee().getEntityId());
        hearingAttendeeDetailsEntity.setPartySubChannelType(hearing.getHearing()
                                                                .getHearingAttendee().getHearingChannel().getCode());
        return hearingAttendeeDetailsEntity;
    }

    private HearingDayDetailsEntity mapHearingDayDetailsEntity(HearingResponse hearing) {
        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(hearing.getHearing().getHearingStartTime());
        hearingDayDetailsEntity.setEndDateTime(hearing.getHearing().getHearingEndTime());
        if (hearing.getHearing().getHearingVenue().getLocationReference().getKey().equals("EPIMS")) {
            hearingDayDetailsEntity.setVenueId(hearing.getHearing()
                                                   .getHearingVenue().getLocationReference().getValue());
        }
        hearingDayDetailsEntity.setRoomId(hearing.getHearing().getHearingRoom().getRoomName());
        return hearingDayDetailsEntity;
    }

    private HearingResponseEntity mapHearingResponseEntity(HearingResponse hearingResponse, HearingEntity hearing) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setHearing(hearing);
        hearingResponseEntity.setListingTransactionId(hearingResponse.getMeta().getTransactionIdCaseHQ());
        hearingResponseEntity.setRequestTimeStamp(hearingResponse.getMeta().getTimestamp());
        hearingResponseEntity.setRequestVersion(hearingResponse.getHearing().getHearingCaseVersionId().toString());
        hearingResponseEntity.setListingStatus(ListingStatus.valueOf(
            hearingResponse.getHearing().getHearingStatus().getCode()).name());
        hearingResponseEntity.setCancellationReasonType(hearingResponse.getHearing().getHearingCancellationReason());
        hearingResponseEntity.setTranslatorRequired(hearingResponse.getHearing().getHearingTranslatorRequired());
        hearingResponseEntity.setListingCaseStatus(hearingResponse.getHearing()
                                                       .getHearingCaseStatus().getCode().name());
        return hearingResponseEntity;
    }

    public HearingStatus getHearingStatus(HearingResponse hearing, HearingEntity hearingEntity) {
        HearingStatus currentStatus = HearingStatus.valueOf(hearingEntity.getStatus());
        HearingCode laStatus = hearing.getHearing().getHearingCaseStatus().getCode();
        HearingStatus postStatus = null;

        switch (laStatus) {
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            case LISTED:
                int currentVersion = hearingEntity.getCaseHearingRequest().getVersionNumber();
                int hearingVersion = hearing.getHearing().getHearingCaseVersionId();
                postStatus = getHearingStatusWhenLaStatusIsListed(currentStatus, hearingVersion, currentVersion);
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
