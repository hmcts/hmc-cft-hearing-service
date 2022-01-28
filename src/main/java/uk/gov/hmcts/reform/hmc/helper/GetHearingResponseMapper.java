package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.*;
import uk.gov.hmcts.reform.hmc.model.*;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;

@Component
public class GetHearingResponseMapper {

    public GetHearingResponse toHearingResponse(HearingEntity hearingEntity) {
        GetHearingResponse getHearingResponse = new GetHearingResponse();
        getHearingResponse.setRequestDetails(setRequestDetails(hearingEntity));
        getHearingResponse.setHearingDetails(setHearingDetails(hearingEntity));
        getHearingResponse.setCaseDetails(setCaseDetails(hearingEntity));
        getHearingResponse.setPartyDetails(setPartyDetails(hearingEntity));
        getHearingResponse.setHearingResponse(setHearingResponse(hearingEntity));
        return getHearingResponse;

    }

    private HearingResponse setHearingResponse(HearingEntity hearingEntity) {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListAssistTransactionID(hearingEntity.getHearingResponses().get(0).getHearingResponseId());
        hearingResponse.setReceivedDateTime(hearingEntity.getHearingResponses().get(0).getRequestTimeStamp());
        hearingResponse.setResponseVersion(hearingEntity.getHearingResponses().get(0).getHearingResponseId());
        hearingResponse.setLaCaseStatus(hearingEntity.getHearingResponses().get(0).getListingCaseStatus());
        hearingResponse.setListingStatus(hearingEntity.getHearingResponses().get(0).getListingStatus());
        //doesn't exist?
       // hearingResponse.setHearingCancellationReason(hearingEntity.getHearingResponses().get(0).getCancellationReasonType);

        return hearingResponse;
    }

    private PartyDetails setPartyDetails(HearingEntity hearingEntity) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(hearingEntity.getCaseHearingRequest().getHearingParties().get(0).getPartyReference());
        partyDetails.setPartyType(hearingEntity.getCaseHearingRequest().getHearingParties().get(0).getPartyType().getLabel());
        partyDetails.setPartyRole(hearingEntity.getCaseHearingRequest().getHearingParties().get(0).getPartyRoleType());
        return partyDetails;
    }


    private CaseDetails setCaseDetails(HearingEntity hearingEntity) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(hearingEntity.getCaseHearingRequest().getHmctsServiceID());
        caseDetails.setCaseRef(hearingEntity.getCaseHearingRequest().getCaseReference());
        caseDetails.setExternalCaseReference(hearingEntity.getCaseHearingRequest().getExternalCaseReference());
        caseDetails.setCaseDeepLink(hearingEntity.getCaseHearingRequest().getCaseUrlContextPath());
        caseDetails.setHmctsInternalCaseName(hearingEntity.getCaseHearingRequest().getHmctsInternalCaseName());
        caseDetails.setPublicCaseName(hearingEntity.getCaseHearingRequest().getPublicCaseName());
        caseDetails.setCaseAdditionalSecurityFlag(hearingEntity.getCaseHearingRequest().getAdditionalSecurityRequiredFlag());
        caseDetails.setCaseInterpreterRequiredFlag(hearingEntity.getCaseHearingRequest().getInterpreterBookingRequiredFlag());
        caseDetails.setCaseManagementLocationCode(hearingEntity.getCaseHearingRequest().getOwningLocationId());
        caseDetails.setCaseRestrictedFlag(hearingEntity.getCaseHearingRequest().getCaseRestrictedFlag());
        caseDetails.setCaseSlaStartDate(hearingEntity.getCaseHearingRequest().getCaseSlaStartDate());
        return caseDetails;
    }

    private HearingDetails setHearingDetails(HearingEntity hearingEntity) {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(hearingEntity.getCaseHearingRequest().getAutoListFlag());
        hearingDetails.setHearingType(hearingEntity.getCaseHearingRequest().getHearingType());
        hearingDetails.setDuration(hearingEntity.getCaseHearingRequest().getRequiredDurationInMinutes());
        //required list?
       // hearingDetails.setNonStandardHearingDurationReasons(hearingEntity.getCaseHearingRequest().getNonStandardDurations().get(0).getNonStandardHearingDurationReasonType());
        hearingDetails.setHearingPriorityType(hearingEntity.getCaseHearingRequest().getHearingPriorityType());
        hearingDetails.setNumberOfPhysicalAttendees(hearingEntity.getCaseHearingRequest().getNumberOfPhysicalAttendees());
        hearingDetails.setHearingInWelshFlag(hearingEntity.getCaseHearingRequest().getHearingInWelshFlag());
        //required list?
        //hearingDetails.setFacilitiesRequired(hearingEntity.getCaseHearingRequest().getRequiredFacilities().get(0).getFacilityType());
        hearingDetails.setListingComments(hearingEntity.getCaseHearingRequest().getListingComments());
        hearingDetails.setHearingRequester(hearingEntity.getCaseHearingRequest().getRequester());
        hearingDetails.setPrivateHearingRequiredFlag(hearingEntity.getCaseHearingRequest().getPrivateHearingRequiredFlag());
        hearingDetails.setLeadJudgeContractType(hearingEntity.getCaseHearingRequest().getLeadJudgeContractType());
        hearingDetails.setHearingIsLinkedFlag(hearingEntity.getCaseHearingRequest().getIsLinkedFlag());
        return hearingDetails;
    }

    private RequestDetails setRequestDetails(HearingEntity hearingEntity) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setStatus(hearingEntity.getStatus());
        requestDetails.setRequestTimeStamp(hearingEntity.getCaseHearingRequest().getHearingRequestReceivedDateTime());
        requestDetails.setVersionNumber(hearingEntity.getCaseHearingRequest().getVersionNumber());
        //doesn't exist?
      //  requestDetails.setPartiesNotified(hearingEntity.getHearingResponses().get(0).getPartiesNotifiedDateTime());
        return requestDetails;
    }


}
