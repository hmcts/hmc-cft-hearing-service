package uk.gov.hmcts.reform.hmc.helper;

import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.*;
import uk.gov.hmcts.reform.hmc.model.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetHearingsResponseMapper extends GetHearingResponseCommonCode {

    public GetHearingsResponse toHearingsResponse(String caseRef, List<CaseHearingRequestEntity> entities) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        if (!entities.isEmpty()) {
            getHearingsResponse.setHmctsServiceId(entities.get(0).getHmctsServiceID());
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
            List<HearingResponseEntity> hearingResponses = getHearingResponseEntities(entity, caseHearing);
            setHearingDaySchedule(caseHearingList, caseHearing, hearingResponses);
        }
        getHearingsResponse.setCaseHearings(caseHearingList);
    }

    private void setHearingDaySchedule(List<CaseHearing> caseHearingList, CaseHearing caseHearing,
                                       List<HearingResponseEntity> hearingResponses) {
        List<HearingDaySchedule> scheduleList = new ArrayList<>();

        for (HearingResponseEntity hearingResponseEntity : hearingResponses) {
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
    }

    private List<HearingResponseEntity> getHearingResponseEntities(CaseHearingRequestEntity entity,
                                                                   CaseHearing caseHearing) {
        List<HearingResponseEntity> hearingResponses = entity.getHearing().getHearingResponses();
        setHearingResponseDetails(caseHearing, hearingResponses);
        return hearingResponses;
    }

    private CaseHearing getCaseHearing(CaseHearingRequestEntity entity) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(entity.getHearing().getId());
        caseHearing.setHearingRequestDateTime(entity.getHearingRequestReceivedDateTime());
        caseHearing.setHearingType(entity.getHearingType());
        caseHearing.setHmcStatus(entity.getHearing().getStatus());
        return caseHearing;
    }

    private void setHearingResponseDetails(CaseHearing caseHearing, List<HearingResponseEntity> entities) {
        for (HearingResponseEntity hearingResponseEntity : entities) {
            caseHearing.setLastResponseReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
            caseHearing.setResponseVersion(hearingResponseEntity.getHearingResponseId());
            caseHearing.setHearingListingStatus(hearingResponseEntity.getListingStatus());
            caseHearing.setListAssistCaseStatus(hearingResponseEntity.getListingCaseStatus());
        }
    }

    public HearingActualResponse toHearingActualResponse(HearingEntity hearingEntity) {
        val hearingResponseEntity = hearingEntity.getHearingResponses();
        val response = new HearingActualResponse();
        response.setHmcStatus(hearingEntity.getStatus());
        response.setCaseDetails(setCaseDetails(hearingEntity));
        setHearingPlanned(hearingEntity, response);
        setHearingActuals(hearingEntity, response);
        return response;
    }

    private void setHearingActuals(HearingEntity hearingEntity, HearingActualResponse response) {
        val hearingResponses = hearingEntity.getHearingResponses();

        hearingResponses.stream().map(hearingResponse -> {
            return hearingResponse.getHearingResponseId();
        });
    }

    private void setHearingPlanned(HearingEntity hearingEntity, HearingActualResponse response) {
        val caseHearingRequestEntity = hearingEntity.getCaseHearingRequest();
        val hearingPlanned = new HearingPlanned();
        hearingPlanned.setPlannedHearingType(caseHearingRequestEntity.getHearingType());
        response.setHearingPlanned(hearingPlanned);
    }

    private CaseDetails setCaseDetails(HearingEntity hearingEntity) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(hearingEntity.getCaseHearingRequest().getHmctsServiceID());
        caseDetails.setCaseRef(hearingEntity.getCaseHearingRequest().getCaseReference());
        caseDetails.setExternalCaseReference(hearingEntity.getCaseHearingRequest().getExternalCaseReference());
        caseDetails.setCaseDeepLink(hearingEntity.getCaseHearingRequest().getCaseUrlContextPath());
        caseDetails.setHmctsInternalCaseName(hearingEntity.getCaseHearingRequest().getHmctsInternalCaseName());
        caseDetails.setPublicCaseName(hearingEntity.getCaseHearingRequest().getPublicCaseName());
        caseDetails.setCaseAdditionalSecurityFlag(
            hearingEntity.getCaseHearingRequest().getAdditionalSecurityRequiredFlag());
        caseDetails.setCaseInterpreterRequiredFlag(
            hearingEntity.getCaseHearingRequest().getInterpreterBookingRequiredFlag());
        caseDetails.setCaseCategories(setCaseCategories(hearingEntity));
        caseDetails.setCaseManagementLocationCode(hearingEntity.getCaseHearingRequest().getOwningLocationId());
        caseDetails.setCaseRestrictedFlag(hearingEntity.getCaseHearingRequest().getCaseRestrictedFlag());
        caseDetails.setCaseSlaStartDate(hearingEntity.getCaseHearingRequest().getCaseSlaStartDate());
        return caseDetails;
    }

    private ArrayList<CaseCategory> setCaseCategories(HearingEntity hearingEntity) {
        ArrayList<CaseCategory> caseCategories = new ArrayList<>();
        if (null != hearingEntity.getCaseHearingRequest().getCaseCategories()
            && !hearingEntity.getCaseHearingRequest().getCaseCategories().isEmpty()) {
            for (CaseCategoriesEntity caseCategoriesEntity :
                hearingEntity.getCaseHearingRequest().getCaseCategories()) {
                CaseCategory caseCategory = new CaseCategory();
                caseCategory.setCategoryType(caseCategoriesEntity.getCategoryType().getLabel());
                caseCategory.setCategoryValue(caseCategoriesEntity.getCaseCategoryValue());
                caseCategories.add(caseCategory);
            }
        }
        return caseCategories;
    }
}
