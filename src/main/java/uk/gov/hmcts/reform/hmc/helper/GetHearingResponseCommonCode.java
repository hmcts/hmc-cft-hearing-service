package uk.gov.hmcts.reform.hmc.helper;

import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.util.ArrayList;
import java.util.List;

public class GetHearingResponseCommonCode {

    protected HearingDaySchedule setHearingDayScheduleDetails(HearingDayDetailsEntity detailEntity) {
        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        hearingDaySchedule.setHearingStartDateTime(detailEntity.getStartDateTime());
        hearingDaySchedule.setHearingEndDateTime(detailEntity.getEndDateTime());
        hearingDaySchedule.setListAssistSessionId(detailEntity.getListAssistSessionId());
        hearingDaySchedule.setHearingVenueId(detailEntity.getVenueId());
        hearingDaySchedule.setHearingRoomId(detailEntity.getRoomId());
        return hearingDaySchedule;
    }

    protected void setAttendeeDetails(List<HearingAttendeeDetailsEntity> attendeeDetailsEntities,
                                    HearingDaySchedule hearingDaySchedule) {
        List<Attendee> attendeeList = new ArrayList<>();
        for (HearingAttendeeDetailsEntity attendeeDetailEntity : attendeeDetailsEntities) {
            Attendee attendee = new Attendee();
            attendee.setPartyId(attendeeDetailEntity.getPartyId());
            attendee.setHearingSubChannel(attendeeDetailEntity.getPartySubChannelType());
            attendeeList.add(attendee);
        }
        hearingDaySchedule.setAttendees(attendeeList);
    }

    protected void setHearingJudgeAndPanelMemberIds(HearingDayPanelEntity hearingDayPanelEntity,
                                                  HearingDaySchedule hearingDaySchedule) {
        if (null == hearingDayPanelEntity.getIsPresiding() || !hearingDayPanelEntity.getIsPresiding()) {
            hearingDaySchedule.setPanelMemberId(hearingDayPanelEntity.getPanelUserId());
        } else {
            hearingDaySchedule.setHearingJudgeId(hearingDayPanelEntity.getPanelUserId());
        }
    }


    protected CaseDetails setCaseDetails(HearingEntity hearingEntity) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(hearingEntity.getCaseHearingRequest().getHmctsServiceCode());
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
