package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmiCaseDetailsMapperTest {

    @Mock
    private CaseClassificationsMapper caseClassificationsMapper;

    @InjectMocks
    private HmiCaseDetailsMapper hmiCaseDetailsMapper;

    private static final String CASE_REF = "CaseRef";
    private static final String SERVICE_CODE = "AB1";
    private static final String CASE_NAME = "CaseName";
    private static final String LOCATION_CODE = "LocationCode";

    @Test
    void shouldReturnCaseDetails() {
        LocalDate localDate = LocalDate.now();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef(CASE_REF);
        caseDetails.setHmctsServiceCode(SERVICE_CODE);
        caseDetails.setHmctsInternalCaseName(CASE_NAME);
        caseDetails.setCaseManagementLocationCode(LOCATION_CODE);
        caseDetails.setCaseSlaStartDate(localDate);
        caseDetails.setCaseInterpreterRequiredFlag(false);
        caseDetails.setCaseRestrictedFlag(true);
        caseDetails.setHmctsServiceCode(SERVICE_CODE);
        caseDetails.setCaseDeepLink("wow.woweee.com/boo");
        caseDetails.setPublicCaseName("this is the business");
        caseDetails.setCaseAdditionalSecurityFlag(Boolean.TRUE);
        CaseClassification caseClassification = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType("CategoryValue1")
            .build();
        List<CaseClassification> caseClassifications = Collections.singletonList(caseClassification);
        when(caseClassificationsMapper.getCaseClassifications(any())).thenReturn(caseClassifications);
        Long hearingId = 1L;
        HmiCaseDetails expectedHmiCaseDetails = HmiCaseDetails.builder()
            .caseClassifications(caseClassifications)
            .caseIdHmcts(CASE_REF)
            .caseListingRequestId(hearingId.toString())
            .caseJurisdiction("AB")
            .caseTitle(CASE_NAME)
            .caseCourt(LOCATION_CODE)
            .caseRegistered(localDate)
            .caseInterpreterRequiredFlag(false)
            .caseRestrictedFlag(true)
            .caseLinks(hmiCaseDetailsMapper.getCaseLinksArray(Arrays.asList(caseDetails.getCaseDeepLink())))
            .casePublishedName(caseDetails.getPublicCaseName())
            .caseAdditionalSecurityFlag(Boolean.TRUE)
            .build();
        HmiCaseDetails actualHmiCaseDetails = hmiCaseDetailsMapper.getCaseDetails(caseDetails, hearingId);
        assertEquals(expectedHmiCaseDetails, actualHmiCaseDetails);
    }
}
