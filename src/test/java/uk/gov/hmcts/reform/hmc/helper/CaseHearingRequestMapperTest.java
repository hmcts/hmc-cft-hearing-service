package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CaseHearingRequestMapperTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2021-08-10T12:20:00Z"), ZoneOffset.UTC);

    @Test
    void modelToEntity() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingEntity hearingEntity = new HearingEntity();
        CaseCategoriesMapper caseCategoriesMapper = new CaseCategoriesMapper();
        CaseHearingRequestMapper caseHearingRequestMapper = new CaseHearingRequestMapper(caseCategoriesMapper, CLOCK);
        CaseHearingRequestEntity entity = caseHearingRequestMapper.modelToEntity(hearingRequest, hearingEntity, 1);
        assert1(entity);
        assert2(entity);
    }

    private void assert1(CaseHearingRequestEntity entity) {
        assertEquals(Boolean.TRUE, entity.getAutoListFlag());
        assertEquals("Some hearing type", entity.getHearingType());
        assertEquals(360, entity.getRequiredDurationInMinutes());
        assertEquals("Priority type", entity.getHearingPriorityType());
        assertNull(entity.getNumberOfPhysicalAttendees());
        assertNull(entity.getHearingInWelshFlag());
        assertEquals(Boolean.TRUE, entity.getPrivateHearingRequiredFlag());
        assertNull(entity.getLeadJudgeContractType());
        assertNull(entity.getFirstDateTimeOfHearingMustBe());
        assertEquals("ABA1", entity.getHmctsServiceCode());
        assertEquals("1111222233334444", entity.getCaseReference());
        assertEquals(LocalDateTime.now(CLOCK), entity.getHearingRequestReceivedDateTime());
        assertNull(entity.getExternalCaseReference());
        assertEquals("https://www.google.com", entity.getCaseUrlContextPath());
        assertEquals("Internal case name", entity.getHmctsInternalCaseName());
        assertEquals("Public case name", entity.getPublicCaseName());
    }

    private void assert2(CaseHearingRequestEntity entity) {
        assertEquals(Boolean.FALSE, entity.getAdditionalSecurityRequiredFlag());
        assertEquals("CMLC123", entity.getOwningLocationId());
        assertEquals(Boolean.FALSE, entity.getCaseRestrictedFlag());
        assertEquals(Boolean.FALSE, entity.getCaseRestrictedFlag());
        assertEquals(1, entity.getVersionNumber());
        assertNull(entity.getInterpreterBookingRequiredFlag());
        assertNull(entity.getListingComments());
        assertNull(entity.getRequester());
        assertEquals(LocalDate.parse("2017-03-01"), entity.getHearingWindowStartDateRange());
        assertEquals(LocalDate.parse("2017-03-01"), entity.getHearingWindowEndDateRange());
    }

}
