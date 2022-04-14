package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseLinks;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EPIMS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NOT_REQUIRED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUIRED;

@Component
public class HmiCaseDetailsMapper {

    private final CaseClassificationsMapper caseClassificationsMapper;

    @Autowired
    public HmiCaseDetailsMapper(CaseClassificationsMapper caseClassificationsMapper) {
        this.caseClassificationsMapper = caseClassificationsMapper;
    }

    public HmiCaseDetails getCaseDetails(CaseDetails caseDetails, Integer versionNumber,
                                         Long hearingId, Boolean isLinkedFlag) {
        return HmiCaseDetails.builder()
                .caseClassifications(caseClassificationsMapper.getCaseClassifications(caseDetails))
                .caseIdHmcts(caseDetails.getCaseRef())
                .caseListingRequestId(hearingId.toString())
                .caseJurisdiction(caseDetails.getHmctsServiceCode().substring(0, 2))
                .caseTitle(caseDetails.getHmctsInternalCaseName())
                .caseCourt(ListingLocation.builder()
                               .locationId(caseDetails.getCaseManagementLocationCode())
                               .locationReferenceType(EPIMS)
                               .locationType(COURT)
                               .build())
                .caseRegistered(caseDetails.getCaseSlaStartDate())
                .caseInterpreterRequiredFlag(caseDetails.getCaseInterpreterRequiredFlag())
                .caseRestrictedFlag(caseDetails.getCaseRestrictedFlag())
                .caseVersionId(versionNumber)
                .caseLinks(getCaseLinksArray(Arrays.asList(caseDetails.getCaseDeepLink())))
                .casePublishedName(caseDetails.getPublicCaseName())
                .caseAdditionalSecurityFlag(caseDetails.getCaseAdditionalSecurityFlag())
                .linkedHearingGroupStatus(isLinkedFlag == TRUE ? REQUIRED : NOT_REQUIRED)
                .build();
    }

    public List<CaseLinks> getCaseLinksArray(List<String> urls) {
        List<CaseLinks> caseLinks = new ArrayList<>();
        urls.forEach(e -> caseLinks.add(getCaseLinks(e)));
        return caseLinks;
    }

    private CaseLinks getCaseLinks(String url) {
        return CaseLinks.builder().url(url).build();
    }

}
