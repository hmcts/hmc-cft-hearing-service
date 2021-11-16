package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER;

@Component
public class CaseHearingRequestMapper {

    public CaseHearingRequestMapper() {
    }

    public CaseHearingRequestEntity modelToEntity(HearingRequest hearingRequest, HearingEntity hearingEntity) {
        // check time stamp insertion
        final CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        HearingDetails hearingDetails = hearingRequest.getHearingDetails();
        CaseDetails caseDetails = hearingRequest.getCaseDetails();
        caseHearingRequestEntity.setAutoListFlag(hearingDetails.getAutoListFlag());
        caseHearingRequestEntity.setHearingType(hearingDetails.getHearingType());
        caseHearingRequestEntity.setRequiredDurationInMinutes(hearingDetails.getDuration());
        caseHearingRequestEntity.setHearingPriorityType(hearingDetails.getHearingPriorityType());
        caseHearingRequestEntity.setNumberOfPhysicalAttendees(hearingDetails.getNumberOfPhysicalAttendees());
        caseHearingRequestEntity.setHearingInWelshFlag(hearingDetails.getHearingInWelshFlag());
        caseHearingRequestEntity.setPrivateHearingRequiredFlag(hearingDetails.getPrivateHearingRequiredFlag());
        caseHearingRequestEntity.setLeadJudgeContractType(hearingDetails.getLeadJudgeContractType());
        caseHearingRequestEntity.setFirstDateTimeOfHearingMustBe(hearingDetails.getHearingWindow()
                                                                     .getFirstDateTimeMustBe());
        caseHearingRequestEntity.setHmctsServiceID(caseDetails.getHmctsServiceCode());
        caseHearingRequestEntity.setCaseReference(caseDetails.getCaseRef());
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(caseDetails.getRequestTimeStamp());
        caseHearingRequestEntity.setExternalCaseReference(caseDetails.getExternalCaseReference());
        caseHearingRequestEntity.setCaseUrlContextPath(caseDetails.getCaseDeepLink());
        caseHearingRequestEntity.setHmctsInternalCaseName(caseDetails.getHmctsInternalCaseName());
        caseHearingRequestEntity.setPublicCaseName(caseDetails.getPublicCaseName());
        caseHearingRequestEntity.setAdditionalSecurityRequiredFlag(caseDetails.getCaseAdditionalSecurityFlag());
        caseHearingRequestEntity.setOwningLocationId(caseDetails.getCaseManagementLocationCode());
        caseHearingRequestEntity.setCaseRestrictedFlag(caseDetails.getCaseRestrictedFlag());
        caseHearingRequestEntity.setCaseSlaStartDate(caseDetails.getCaseSlaStartDate());
        caseHearingRequestEntity.setVersionNumber(VERSION_NUMBER);
        caseHearingRequestEntity.setInterpreterBookingRequiredFlag(caseDetails.getCaseInterpreterRequiredFlag());
        caseHearingRequestEntity.setIsLinkedFlag(hearingDetails.getHearingIsLinkedFlag());
        caseHearingRequestEntity.setListingComments(hearingDetails.getListingComments());
        caseHearingRequestEntity.setRequester(hearingDetails.getHearingRequester());
        caseHearingRequestEntity.setHearingWindowStartDateRange(hearingDetails.getHearingWindow()
                                                                    .getHearingWindowStartDateRange());
        caseHearingRequestEntity.setHearingWindowEndDateRange(hearingDetails.getHearingWindow()
                                                                  .getHearingWindowEndDateRange());
        caseHearingRequestEntity.setRequestTimeStamp(hearingRequest.getRequestDetails().getRequestTimeStamp());
        caseHearingRequestEntity.setHearing(hearingEntity);
        return caseHearingRequestEntity;
    }

}








