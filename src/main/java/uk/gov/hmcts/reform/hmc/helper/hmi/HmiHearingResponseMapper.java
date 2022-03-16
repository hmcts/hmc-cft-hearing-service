package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;

import java.util.List;

@Component
public class HmiHearingResponseMapper {

    private final InboundQueueService inboundQueueService;

    public HmiHearingResponseMapper(InboundQueueService inboundQueueService) {
        this.inboundQueueService = inboundQueueService;
    }

    public HearingEntity mapHmiHearingToEntity(HearingResponse hearing, HearingEntity hearingEntity) {

        HearingResponseEntity hearingResponseEntity = mapHearingResponseEntity(hearing);
        HearingDayDetailsEntity hearingDayDetailsEntity = mapHearingDayDetailsEntity(hearing);
        HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity = mapHearingAttendeeDetailsEntity(hearing);
        HearingDayPanelEntity hearingDayPanelEntity = mapHearingDayPanelEntity(hearing);

        hearingDayDetailsEntity.setHearingDayPanel(List.of(hearingDayPanelEntity));
        hearingDayDetailsEntity.setHearingAttendeeDetails(List.of(hearingAttendeeDetailsEntity));
        hearingResponseEntity.setHearingDayDetails(List.of(hearingDayDetailsEntity));
        HearingStatus postStatus = inboundQueueService.getHearingStatus(hearing, hearingEntity);
        List<HearingResponseEntity> responseEntities = hearingEntity.getHearingResponses();
        responseEntities.add(hearingResponseEntity);
        hearingEntity.setHearingResponses(responseEntities);
        hearingEntity.setStatus(postStatus.name());
        return hearingEntity;
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
        hearingAttendeeDetailsEntity.setPartySubChannelType(hearing.getHearing().getHearingAttendee().getHearingChannel().getCode());
        return hearingAttendeeDetailsEntity;
    }

    private HearingDayDetailsEntity mapHearingDayDetailsEntity(HearingResponse hearing) {
        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(hearing.getHearing().getHearingStartTime());
        hearingDayDetailsEntity.setEndDateTime(hearing.getHearing().getHearingEndTime());
        if (hearing.getHearing().getHearingVenue().getLocationReference().getKey().equals("EPIMS")) {
            hearingDayDetailsEntity.setVenueId(hearing.getHearing().getHearingVenue().getLocationReference().getValue());
        }
        hearingDayDetailsEntity.setRoomId(hearing.getHearing().getHearingRoom().getRoomName());
        return hearingDayDetailsEntity;
    }

    private HearingResponseEntity mapHearingResponseEntity(HearingResponse hearing) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        // hearingResponseEntity.setListingTransactionId(hearing.getMeta().getTransactionIdCaseHQ());
        hearingResponseEntity.setRequestTimeStamp(hearing.getMeta().getTimestamp());
        hearingResponseEntity.setRequestVersion(hearing.getHearing().getHearingCaseVersionId().toString());
        hearingResponseEntity.setListingStatus(hearing.getHearing().getHearingStatus().toString());
        //hearingResponseEntity.setCancellationReasonType(hearing.getHearing().getHearingCancellationReason());
        //hearingResponseEntity.setTranslatorRequired(hearing.getHearing().getHearingTranslatorRequired());
        hearingResponseEntity.setListingCaseStatus(hearing.getHearing().getHearingCaseStatus().getCode().toString());
        hearingResponseEntity.setListingStatus(hearing.getHearing().getHearingStatus().getCode());
        return hearingResponseEntity;
    }

}
