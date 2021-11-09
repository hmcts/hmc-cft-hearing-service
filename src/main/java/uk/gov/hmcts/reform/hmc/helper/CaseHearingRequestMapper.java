package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;

@Component
public class CaseHearingRequestMapper {

    public CaseHearingRequestEntity modelToEntity(RequestDetails requestDetails) {
        final CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setRequestTimeStamp(requestDetails.getRequestTimeStamp());
        return caseHearingRequestEntity;

    }

    public CaseHearingRequestEntity modelToEntity(HearingDetails hearingDetails,
                                                  CaseHearingRequestEntity caseHearingRequestEntity) {
        caseHearingRequestEntity.setAutoListFlag(hearingDetails.getAutoListFlag());
        caseHearingRequestEntity.setHearingType(hearingDetails.getHearingType());
        caseHearingRequestEntity.setHearingWindowStartDateRange(hearingDetails.getHearingWindow()
                                                                    .getHearingWindowStartDateRange());
        caseHearingRequestEntity.setHearingWindowEndDateRange(hearingDetails.getHearingWindow()
                                                                  .getHearingWindowEndDateRange());
        caseHearingRequestEntity.setRequiredDurationInMinutes(hearingDetails.getDuration());
        caseHearingRequestEntity.setFirstDateTimeOfHearingMustBe(hearingDetails.getHearingWindow()
                                                                     .getFirstDateTimeMustBe());
        caseHearingRequestEntity.setHearingPriorityType(hearingDetails.getHearingPriorityType());
        caseHearingRequestEntity.setNumberOfPhysicalAttendees(hearingDetails.getNumberOfPhysicalAttendees());
        caseHearingRequestEntity.setHearingInWelshFlag(hearingDetails.getHearingInWelshFlag());
        caseHearingRequestEntity.setListingComments(hearingDetails.getListingComments());
        caseHearingRequestEntity.setRequester(hearingDetails.getHearingRequester());
        caseHearingRequestEntity.setPrivateHearingRequiredFlag(hearingDetails.getPrivateHearingRequiredFlag());
        caseHearingRequestEntity.setLeadJudgeContractType(hearingDetails.getLeadJudgeContractType());


        // panel requirements
        caseHearingRequestEntity.setLinkedFlag(hearingDetails.getHearingIsLinkedFlag());
        return caseHearingRequestEntity;

    }
}
