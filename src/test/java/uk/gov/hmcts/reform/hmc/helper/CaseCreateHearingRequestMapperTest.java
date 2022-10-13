package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.client.hmi.ListingReasonCode;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseCreateHearingRequestMapperTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2021-08-10T12:20:00Z"), ZoneOffset.UTC);
    private final CaseCategoriesMapper caseCategoriesMapper = new CaseCategoriesMapper();
    private final HearingEntity hearingEntity = new HearingEntity();
    private final CaseHearingRequestMapper caseHearingRequestMapper = new CaseHearingRequestMapper(
        caseCategoriesMapper, CLOCK);

    @Test
    void modelToEntity() {
        CaseHearingRequestEntity expectedEntity = new CaseHearingRequestEntity();
        expectedEntityValues(expectedEntity);
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        CaseHearingRequestEntity actualEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest, hearingEntity, 1, true, true);
        expectedEntity.setHearing(hearingEntity);
        assertEquals(expectedEntity, actualEntity);
    }

    @Test
    void modelToEntityWithoutHearingWindow() {
        CaseHearingRequestEntity expectedEntity = new CaseHearingRequestEntity();
        expectedEntityValues(expectedEntity);
        expectedEntity.setHearingWindowStartDateRange(null);
        expectedEntity.setHearingWindowEndDateRange(null);
        expectedEntity.setFirstDateTimeOfHearingMustBe(null);
        HearingRequest createHearingRequest = new HearingRequest();
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setHearingWindow(null);
        createHearingRequest.setHearingDetails(hearingDetails);
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        CaseHearingRequestEntity actualEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest,
            hearingEntity, 1,
            true, true);
        expectedEntity.setHearing(hearingEntity);
        assertEquals(expectedEntity, actualEntity);
    }

    @Test
    void modelToEntityWhenAutoListFlagIsTrueAndIsMappedToIsFalse() {
        HearingRequest createHearingRequest = buildCreateHearingRequest();
        createHearingRequest.getHearingDetails().setAutoListFlag(true);
        createHearingRequest.getHearingDetails().setListingAutoChangeReasonCode(null);

        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest,
            hearingEntity, 1,
            true, true);
        assertTrue(caseHearingRequestEntity.getAutoListFlag());
        assertNull(caseHearingRequestEntity.getListingAutoChangeReasonCode());
    }

    @Test
    void modelToEntityWhenAutoListFlagAndIsMappedToIsTrue() {
        HearingRequest createHearingRequest = buildCreateHearingRequest();
        createHearingRequest.getHearingDetails().setAutoListFlag(true);
        createHearingRequest.getHearingDetails().setListingAutoChangeReasonCode(null);

        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest,
            hearingEntity, 1,
            true, false);
        assertFalse(caseHearingRequestEntity.getAutoListFlag());
        assertEquals(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel(),
            caseHearingRequestEntity.getListingAutoChangeReasonCode());
    }

    @Test
    void modelToEntityWhenListingAutoChangeReasonCodeIsProvidedAndAutoListFlagFalse() {
        HearingRequest createHearingRequest = buildCreateHearingRequest();
        createHearingRequest.getHearingDetails()
            .setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel());
        createHearingRequest.getHearingDetails().setAutoListFlag(false);

        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(
            createHearingRequest,
            hearingEntity, 1,
            true, true);
        assertFalse(caseHearingRequestEntity.getAutoListFlag());
        assertEquals(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel(),
            caseHearingRequestEntity.getListingAutoChangeReasonCode());
    }

    @Test
    void shouldFail_whenListingAutoChangeReasonCodeIsProvidedAndAutoListFlagIsTrue() {
        HearingRequest createHearingRequest = buildCreateHearingRequest();
        createHearingRequest.getHearingDetails()
            .setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.name());
        createHearingRequest.getHearingDetails().setAutoListFlag(true);
        assertTrue(createHearingRequest.getHearingDetails().getAutoListFlag());

        Exception exception = assertThrows(BadRequestException.class, () ->
            caseHearingRequestMapper.modelToEntity(
                createHearingRequest, hearingEntity, 1, true, true));
        assertEquals(
            "001 autoListFlag must be FALSE if you supply a change reasoncode",
            exception.getMessage());
    }

    private void expectedEntityValues(CaseHearingRequestEntity expectedEntity) {
        expectedEntity.setAutoListFlag(false);
        expectedEntity.setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel());
        expectedEntity.setHearingType("Some hearing type");
        expectedEntity.setRequiredDurationInMinutes(360);
        expectedEntity.setHearingPriorityType("Priority type");
        expectedEntity.setPrivateHearingRequiredFlag(true);
        expectedEntity.setHmctsServiceCode("ABA1");
        expectedEntity.setCaseReference("1111222233334444");
        expectedEntity.setHearingRequestReceivedDateTime(LocalDateTime.now(CLOCK));
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

    private HearingRequest buildCreateHearingRequest() {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        return createHearingRequest;
    }

}
