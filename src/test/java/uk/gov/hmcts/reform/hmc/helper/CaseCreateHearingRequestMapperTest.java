package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseCreateHearingRequestMapperTest {

    @Test
    void modelToEntity() {
        CaseHearingRequestEntity expectedEntity = new CaseHearingRequestEntity();
        expectedEntity.setAutoListFlag(true);
        expectedEntity.setHearingType("Some hearing type");
        expectedEntity.setRequiredDurationInMinutes(0);
        expectedEntity.setHearingPriorityType("Priority type");
        expectedEntity.setPrivateHearingRequiredFlag(true);
        expectedEntity.setHmctsServiceID("ABA1");
        expectedEntity.setCaseReference(TestingUtil.CASE_REFERENCE);
        expectedEntity.setHearingRequestReceivedDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        expectedEntity.setCaseUrlContextPath("https://www.google.com");
        expectedEntity.setHmctsInternalCaseName("Internal case name");
        expectedEntity.setPublicCaseName("Public case name");
        expectedEntity.setAdditionalSecurityRequiredFlag(false);
        expectedEntity.setOwningLocationId("CMLC123");
        expectedEntity.setCaseRestrictedFlag(false);
        expectedEntity.setVersionNumber(1);
        expectedEntity.setIsLinkedFlag(false);
        expectedEntity.setHearingWindowStartDateRange(LocalDate.parse("2017-03-01"));
        expectedEntity.setHearingWindowEndDateRange(LocalDate.parse("2017-03-01"));
        expectedEntity.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        LocalDateTime time = LocalDateTime.now();
        createHearingRequest.getRequestDetails().setRequestTimeStamp(time);
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingEntity hearingEntity = new HearingEntity();
        CaseCategoriesMapper caseCategoriesMapper = new CaseCategoriesMapper();
        CaseHearingRequestMapper caseHearingRequestMapper = new CaseHearingRequestMapper(caseCategoriesMapper);
        CaseHearingRequestEntity actualEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest,
            hearingEntity
        );
        expectedEntity.setHearing(hearingEntity);
        expectedEntity.setRequestTimeStamp(time);
        assertEquals(expectedEntity, actualEntity);
    }
}
