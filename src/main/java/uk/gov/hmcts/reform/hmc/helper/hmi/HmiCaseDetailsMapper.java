package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NOT_REQUIRED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUIRED;

@Component
public class HmiCaseDetailsMapper {

    private final CaseClassificationsMapper caseClassificationsMapper;

    @Autowired
    public HmiCaseDetailsMapper(CaseClassificationsMapper caseClassificationsMapper) {
        this.caseClassificationsMapper = caseClassificationsMapper;
    }

    public HmiCaseDetails getCaseDetails(CaseDetails caseDetails, Long hearingId, Boolean isLinkedFlag) {
        return HmiCaseDetails.builder()
            .caseClassifications(caseClassificationsMapper.getCaseClassifications(caseDetails))
            .caseIdHmcts(caseDetails.getCaseRef())
            .caseListingRequestId(hearingId.toString())
            .caseJurisdiction(caseDetails.getHmctsServiceCode().substring(0, 2))
            .caseTitle(caseDetails.getHmctsInternalCaseName())
            .caseCourt(caseDetails.getCaseManagementLocationCode())
            .caseRegistered(caseDetails.getCaseSlaStartDate())
            .caseInterpreterRequiredFlag(caseDetails.getCaseInterpreterRequiredFlag())
            .caseRestrictedFlag(caseDetails.getCaseRestrictedFlag())
            .linkedHearingGroupStatus(isLinkedFlag == TRUE ? REQUIRED : NOT_REQUIRED)
            .build();
    }
}
