package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetHearingsResponseMapper extends GetHearingResponseCommonCode {

    public GetHearingsResponse toHearingsResponse(String caseRef, List<CaseHearingRequestEntity> entities) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        if (!entities.isEmpty()) {
            getHearingsResponse.setHmctsServiceId(entities.get(0).getHmctsServiceCode());
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
            HearingResponseEntity latestHearingResponse = getHearingResponseEntities(entity, caseHearing);
            setHearingDaySchedule(caseHearingList, caseHearing, latestHearingResponse);
        }
        getHearingsResponse.setCaseHearings(caseHearingList);
    }

    private void setHearingDaySchedule(List<CaseHearing> caseHearingList, CaseHearing caseHearing,
                                       HearingResponseEntity hearingResponseEntity) {
        List<HearingDaySchedule> scheduleList = new ArrayList<>();

        List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponseEntity.getHearingDayDetails();
        if (!hearingDayDetailEntities.isEmpty()) {
            for (HearingDayDetailsEntity detailEntity : hearingDayDetailEntities) {
                HearingDaySchedule hearingDaySchedule = setHearingDayScheduleDetails(detailEntity);
                setHearingJudgeAndPanelMemberIds(detailEntity.getHearingDayPanel().get(0), hearingDaySchedule);
                setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                scheduleList.add(hearingDaySchedule);
            }
        }
        caseHearing.setHearingDaySchedule(scheduleList);
        caseHearingList.add(caseHearing);
    }

    private HearingResponseEntity getHearingResponseEntities(CaseHearingRequestEntity entity,
                                                             CaseHearing caseHearing) {
        HearingResponseEntity hearingResponseEntity = entity.getHearing().getLatestHearingResponse().orElseThrow();
        setHearingResponseDetails(caseHearing, hearingResponseEntity);
        return hearingResponseEntity;
    }

    private CaseHearing getCaseHearing(CaseHearingRequestEntity entity) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(entity.getHearing().getId());
        caseHearing.setHearingRequestDateTime(entity.getHearingRequestReceivedDateTime());
        caseHearing.setHearingType(entity.getHearingType());
        caseHearing.setHmcStatus(entity.getHearing().getStatus());
        return caseHearing;
    }

    private void setHearingResponseDetails(CaseHearing caseHearing, HearingResponseEntity hearingResponseEntity) {
        caseHearing.setLastResponseReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
        caseHearing.setResponseVersion(hearingResponseEntity.getHearingResponseId());
        caseHearing.setHearingListingStatus(hearingResponseEntity.getListingStatus());
        caseHearing.setListAssistCaseStatus(hearingResponseEntity.getListingCaseStatus());
    }

}
