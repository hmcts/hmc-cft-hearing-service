package uk.gov.hmcts.reform.hmc.helper;

import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
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
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        caseDetails.setHmctsServiceCode(caseHearingRequestEntity.getHmctsServiceCode());
        caseDetails.setCaseRef(caseHearingRequestEntity.getCaseReference());
        caseDetails.setExternalCaseReference(caseHearingRequestEntity.getExternalCaseReference());
        caseDetails.setCaseDeepLink(caseHearingRequestEntity.getCaseUrlContextPath());
        caseDetails.setHmctsInternalCaseName(caseHearingRequestEntity.getHmctsInternalCaseName());
        caseDetails.setPublicCaseName(caseHearingRequestEntity.getPublicCaseName());
        caseDetails.setCaseAdditionalSecurityFlag(
            caseHearingRequestEntity.getAdditionalSecurityRequiredFlag());
        caseDetails.setCaseInterpreterRequiredFlag(
            caseHearingRequestEntity.getInterpreterBookingRequiredFlag());
        caseDetails.setCaseCategories(setCaseCategories(hearingEntity));
        caseDetails.setCaseManagementLocationCode(caseHearingRequestEntity.getOwningLocationId());
        caseDetails.setCaseRestrictedFlag(caseHearingRequestEntity.getCaseRestrictedFlag());
        caseDetails.setCaseSlaStartDate(caseHearingRequestEntity.getCaseSlaStartDate());
        return caseDetails;
    }

    private ArrayList<CaseCategory> setCaseCategories(HearingEntity hearingEntity) {
        ArrayList<CaseCategory> caseCategories = new ArrayList<>();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        if (null != caseHearingRequestEntity.getCaseCategories()
            && !caseHearingRequestEntity.getCaseCategories().isEmpty()) {
            for (CaseCategoriesEntity caseCategoriesEntity :
                caseHearingRequestEntity.getCaseCategories()) {
                CaseCategory caseCategory = new CaseCategory();
                caseCategory.setCategoryType(caseCategoriesEntity.getCategoryType().getLabel());
                caseCategory.setCategoryValue(caseCategoriesEntity.getCaseCategoryValue());
                caseCategory.setCategoryParent(caseCategoriesEntity.getCaseCategoryParent());
                caseCategories.add(caseCategory);
            }
        }
        return caseCategories;
    }

}
