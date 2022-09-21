package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GetHearingsResponseMapper extends GetHearingResponseCommonCode {

    public GetHearingsResponse toHearingsResponse(String caseRef, List<CaseHearingRequestEntity> entities) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        if (!entities.isEmpty()) {
            getHearingsResponse.setHmctsServiceCode(entities.get(0).getHmctsServiceCode());
            setCaseHearings(entities, getHearingsResponse);
        } else {
            getHearingsResponse.setCaseHearings(new ArrayList<>());
        }
        return getHearingsResponse;

    }

    private void setCaseHearings(List<CaseHearingRequestEntity> entities, GetHearingsResponse getHearingsResponse) {
        List<CaseHearing> caseHearingList = new ArrayList<>();
        for (CaseHearingRequestEntity entity : entities) {
            CaseHearing caseHearing = getCaseHearing(entity);
            Optional<HearingResponseEntity> latestHearingResponseOpt = getLatestHearingResponse(entity, caseHearing);
            latestHearingResponseOpt.ifPresent(latestHearingResponse ->
                setHearingDaySchedule(caseHearing, latestHearingResponse));
            setHearingGroupRequestId(entity, caseHearing);
            setHearingChannels(entity, caseHearing);
            caseHearingList.add(caseHearing);
        }
        getHearingsResponse.setCaseHearings(caseHearingList);
    }

    private void setHearingDaySchedule(CaseHearing caseHearing,
                                       HearingResponseEntity hearingResponseEntity) {
        List<HearingDaySchedule> scheduleList = new ArrayList<>();

        List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponseEntity.getHearingDayDetails();
        if (!hearingDayDetailEntities.isEmpty()) {
            for (HearingDayDetailsEntity detailEntity : hearingDayDetailEntities) {
                HearingDaySchedule hearingDaySchedule = setHearingDayScheduleDetails(detailEntity);
                if (!CollectionUtils.isEmpty((detailEntity.getHearingDayPanel()))) {
                    setHearingJudgeAndPanelMemberIds(detailEntity.getHearingDayPanel(), hearingDaySchedule);
                }
                setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                hearingDaySchedule.setHearingVenueId(detailEntity.getVenueId());
                scheduleList.add(hearingDaySchedule);
            }
        }
        caseHearing.setHearingDaySchedule(scheduleList);
    }

    private Optional<HearingResponseEntity> getLatestHearingResponse(CaseHearingRequestEntity entity,
                                                                     CaseHearing caseHearing) {
        Optional<HearingResponseEntity> hearingResponseEntityOpt = entity.getHearing().getLatestHearingResponse();
        hearingResponseEntityOpt.ifPresent(hearingResponseEntity ->
            setHearingResponseDetails(caseHearing, hearingResponseEntity));
        return hearingResponseEntityOpt;
    }

    private CaseHearing getCaseHearing(CaseHearingRequestEntity entity) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(entity.getHearing().getId());
        caseHearing.setHearingRequestDateTime(entity.getHearingRequestReceivedDateTime());
        caseHearing.setHearingType(entity.getHearingType());
        caseHearing.setHmcStatus(entity.getHearing().getDerivedHearingStatus());
        caseHearing.setHearingIsLinkedFlag(entity.getHearing().getIsLinkedFlag());
        return caseHearing;
    }

    private void setHearingResponseDetails(CaseHearing caseHearing, HearingResponseEntity hearingResponseEntity) {
        caseHearing.setLastResponseReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
        caseHearing.setHearingListingStatus(hearingResponseEntity.getListingStatus());
        caseHearing.setListAssistCaseStatus(hearingResponseEntity.getListingCaseStatus());
        caseHearing.setRequestVersion(hearingResponseEntity.getRequestVersion());
    }

    private void setHearingGroupRequestId(CaseHearingRequestEntity entity, CaseHearing caseHearing) {
        HearingEntity hearing = entity.getHearing();
        if (hearing.getLinkedGroupDetails() != null) {
            caseHearing.setHearingGroupRequestId(hearing.getLinkedGroupDetails().getLinkedGroupId().toString());
        }
    }

    private void setHearingChannels(CaseHearingRequestEntity entity, CaseHearing caseHearing) {
        List<String> hearingChannels = new ArrayList<>();
        List<HearingChannelsEntity> hearingChannelsEntities = entity.getHearingChannels();
        if (hearingChannelsEntities != null && !hearingChannelsEntities.isEmpty()) {
            for (HearingChannelsEntity hearingChannelsEntity : hearingChannelsEntities) {
                hearingChannels.add(hearingChannelsEntity.getHearingChannelType());
            }
            caseHearing.setHearingChannels(hearingChannels);
        }
    }
}
