package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseCreateHearingRequestMapperTest {

    @Test
    void modelToEntity() {
        Clock clock = Clock.fixed(Instant.parse("2021-08-10T12:20:00Z"), ZoneOffset.UTC);
        CaseHearingRequestEntity expectedEntity = new CaseHearingRequestEntity();
        expectedEntityValues(clock, expectedEntity);
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingEntity hearingEntity = new HearingEntity();
        CaseCategoriesMapper caseCategoriesMapper = new CaseCategoriesMapper();
        CaseHearingRequestMapper caseHearingRequestMapper = new CaseHearingRequestMapper(caseCategoriesMapper, clock);
        CaseHearingRequestEntity actualEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest,
            hearingEntity, 1
        );
        expectedEntity.setHearing(hearingEntity);
        assertEquals(expectedEntity, actualEntity);
    }

    @Test
    void modelToEntityWithoutHearingWindow() {
        Clock clock = Clock.fixed(Instant.parse("2021-08-10T12:20:00Z"), ZoneOffset.UTC);
        CaseHearingRequestEntity expectedEntity = new CaseHearingRequestEntity();
        expectedEntityValues(clock, expectedEntity);
        expectedEntity.setHearingWindowStartDateRange(null);
        expectedEntity.setHearingWindowEndDateRange(null);
        expectedEntity.setFirstDateTimeOfHearingMustBe(null);
        HearingRequest createHearingRequest = new HearingRequest();
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setHearingWindow(null);
        createHearingRequest.setHearingDetails(hearingDetails);
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingEntity hearingEntity = new HearingEntity();
        CaseCategoriesMapper caseCategoriesMapper = new CaseCategoriesMapper();

        CaseHearingRequestMapper caseHearingRequestMapper = new CaseHearingRequestMapper(caseCategoriesMapper, clock);
        CaseHearingRequestEntity actualEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest,
            hearingEntity, 1
        );
        expectedEntity.setHearing(hearingEntity);
        assertEquals(expectedEntity, actualEntity);
    }

    private void expectedEntityValues(Clock clock, CaseHearingRequestEntity expectedEntity) {
        expectedEntity.setAutoListFlag(true);
        expectedEntity.setHearingType("Some hearing type");
        expectedEntity.setRequiredDurationInMinutes(360);
        expectedEntity.setHearingPriorityType("Priority type");
        expectedEntity.setPrivateHearingRequiredFlag(true);
        expectedEntity.setHmctsServiceCode("ABA1");
        expectedEntity.setCaseReference("1111222233334444");
        expectedEntity.setHearingRequestReceivedDateTime(LocalDateTime.now(clock));
        expectedEntity.setCaseUrlContextPath("https://www.google.com");
        expectedEntity.setHmctsInternalCaseName("Internal case name");
        expectedEntity.setPublicCaseName("Public case name");
        expectedEntity.setAdditionalSecurityRequiredFlag(false);
        expectedEntity.setOwningLocationId("CMLC123");
        expectedEntity.setCaseRestrictedFlag(false);
        expectedEntity.setVersionNumber(1);
        expectedEntity.setHearingWindowStartDateRange(LocalDate.parse("2017-03-01"));
        expectedEntity.setHearingWindowEndDateRange(LocalDate.parse("2017-03-01"));
        expectedEntity.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
    }

}
