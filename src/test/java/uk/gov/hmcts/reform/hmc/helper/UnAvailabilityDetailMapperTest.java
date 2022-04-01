package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_DOW_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_RANGE_TYPE;

class UnAvailabilityDetailMapperTest {

    @Test
    void modelToEntityTest_DowDetails() {
        UnAvailabilityDetailMapper mapper = new UnAvailabilityDetailMapper();
        List<UnavailabilityDow> dowList = getUnAvailabilityDowDetails();
        PartyDetails partyDetail = new PartyDetails();
        partyDetail.setUnavailabilityDow(dowList);
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<UnavailabilityEntity> entities = mapper.modelToEntity(partyDetail, hearingPartyEntity);
        assertEquals(DayOfWeekUnavailable.MONDAY, entities.get(0).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.ALL, entities.get(0).getDayOfWeekUnavailableType());
        assertEquals(DayOfWeekUnavailable.TUESDAY, entities.get(1).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.AM, entities.get(1).getDayOfWeekUnavailableType());
        assertEquals(DayOfWeekUnavailable.WEDNESDAY, entities.get(2).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.PM, entities.get(2).getDayOfWeekUnavailableType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "random"})
    void shouldThrowErrorWhenUnavailabilityTypeIs(String arg) {
        UnAvailabilityDetailMapper mapper = new UnAvailabilityDetailMapper();
        List<UnavailabilityRanges> rangeDetails = getUnAvailabilityRangeDetails(arg);
        PartyDetails partyDetail = new PartyDetails();
        partyDetail.setUnavailabilityRanges(rangeDetails);
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        Exception exception = assertThrows(BadRequestException.class, () ->
            mapper.modelToEntity(partyDetail, hearingPartyEntity));
        assertEquals("unsupported type for unavailability type", exception.getMessage());
    }

    @Test
    void shouldThrowErrorWhenUnavailabilityTypeIsNull() {
        UnAvailabilityDetailMapper mapper = new UnAvailabilityDetailMapper();
        List<UnavailabilityRanges> rangeDetails = getUnAvailabilityRangeDetails(null);
        PartyDetails partyDetail = new PartyDetails();
        partyDetail.setUnavailabilityRanges(rangeDetails);
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        Exception exception = assertThrows(BadRequestException.class, () ->
            mapper.modelToEntity(partyDetail, hearingPartyEntity));
        assertEquals("unsupported type for unavailability type", exception.getMessage());
    }

    @Test
    void modelToEntityTest_RangeDetailsEmpty() {
        UnAvailabilityDetailMapper mapper = new UnAvailabilityDetailMapper();
        List<UnavailabilityRanges> rangeDetails = getUnAvailabilityRangeDetails();
        PartyDetails partyDetail = new PartyDetails();
        partyDetail.setUnavailabilityRanges(rangeDetails);
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<UnavailabilityEntity> entities = mapper.modelToEntity(partyDetail, hearingPartyEntity);
        assertEquals(LocalDate.parse("2020-09-10"), entities.get(0).getEndDate());
        assertEquals(LocalDate.parse("2021-10-10"), entities.get(0).getStartDate());
        assertEquals(DayOfWeekUnAvailableType.ALL, entities.get(0).getDayOfWeekUnavailableType());
        assertEquals(LocalDate.parse("2022-10-15"), entities.get(1).getEndDate());
        assertEquals(LocalDate.parse("2023-10-20"), entities.get(1).getStartDate());
        assertEquals(DayOfWeekUnAvailableType.AM, entities.get(1).getDayOfWeekUnavailableType());
    }

    @Test
    void modelToEntityTest_Dow_And_RangeDetails() {
        UnAvailabilityDetailMapper mapper = new UnAvailabilityDetailMapper();
        PartyDetails partyDetail = getUnAvailabilityDetails();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<UnavailabilityEntity> entities = mapper.modelToEntity(partyDetail, hearingPartyEntity);
        assertEquals(DayOfWeekUnavailable.MONDAY, entities.get(0).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.ALL, entities.get(0).getDayOfWeekUnavailableType());
        assertNull(entities.get(0).getEndDate());
        assertNull(entities.get(0).getStartDate());
        assertEquals(UNAVAILABILITY_DOW_TYPE, entities.get(0).getUnAvailabilityType());

        assertEquals(DayOfWeekUnavailable.TUESDAY, entities.get(1).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.AM, entities.get(1).getDayOfWeekUnavailableType());
        assertNull(entities.get(1).getEndDate());
        assertNull(entities.get(1).getStartDate());
        assertEquals(UNAVAILABILITY_DOW_TYPE, entities.get(1).getUnAvailabilityType());

        assertEquals(DayOfWeekUnavailable.WEDNESDAY, entities.get(2).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.PM, entities.get(2).getDayOfWeekUnavailableType());
        assertNull(entities.get(2).getEndDate());
        assertNull(entities.get(2).getStartDate());
        assertEquals(UNAVAILABILITY_DOW_TYPE, entities.get(2).getUnAvailabilityType());

        assertNull(entities.get(3).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.ALL, entities.get(3).getDayOfWeekUnavailableType());
        assertEquals(LocalDate.parse("2020-09-10"), entities.get(3).getEndDate());
        assertEquals(LocalDate.parse("2021-10-10"), entities.get(3).getStartDate());
        assertEquals(UNAVAILABILITY_RANGE_TYPE, entities.get(3).getUnAvailabilityType());

        assertNull(entities.get(4).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.AM, entities.get(4).getDayOfWeekUnavailableType());
        assertEquals(LocalDate.parse("2022-10-15"), entities.get(4).getEndDate());
        assertEquals(LocalDate.parse("2023-10-20"), entities.get(4).getStartDate());
        assertEquals(UNAVAILABILITY_RANGE_TYPE, entities.get(4).getUnAvailabilityType());
    }

    private PartyDetails getUnAvailabilityDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setUnavailabilityRanges(getUnAvailabilityRangeDetails());
        partyDetails.setUnavailabilityDow(getUnAvailabilityDowDetails());
        return partyDetails;
    }


    private List<UnavailabilityRanges> getUnAvailabilityRangeDetails() {
        UnavailabilityRanges unavailabilityRanges1 = new UnavailabilityRanges();
        unavailabilityRanges1.setUnavailableToDate(LocalDate.parse("2020-09-10"));
        unavailabilityRanges1.setUnavailableFromDate(LocalDate.parse("2021-10-10"));
        unavailabilityRanges1.setUnavailabilityType(DayOfWeekUnAvailableType.ALL.label);

        UnavailabilityRanges unavailabilityRanges2 = new UnavailabilityRanges();
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2022-10-15"));
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2023-10-20"));
        unavailabilityRanges2.setUnavailabilityType(DayOfWeekUnAvailableType.AM.label);

        List<UnavailabilityRanges> ranges = new ArrayList<>();
        ranges.add(unavailabilityRanges1);
        ranges.add(unavailabilityRanges2);
        return ranges;
    }

    private List<UnavailabilityRanges> getUnAvailabilityRangeDetails(String value) {
        UnavailabilityRanges unavailabilityRanges1 = new UnavailabilityRanges();
        unavailabilityRanges1.setUnavailableToDate(LocalDate.parse("2020-09-10"));
        unavailabilityRanges1.setUnavailableFromDate(LocalDate.parse("2021-10-10"));
        unavailabilityRanges1.setUnavailabilityType(value);

        UnavailabilityRanges unavailabilityRanges2 = new UnavailabilityRanges();
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2022-10-15"));
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2023-10-20"));
        unavailabilityRanges2.setUnavailabilityType(DayOfWeekUnAvailableType.AM.label);

        List<UnavailabilityRanges> ranges = new ArrayList<>();
        ranges.add(unavailabilityRanges1);
        ranges.add(unavailabilityRanges2);
        return ranges;
    }

    private List<UnavailabilityDow> getUnAvailabilityDowDetails() {
        List<UnavailabilityDow> dowList = new ArrayList<>();
        UnavailabilityDow unavailabilityDow1 = new UnavailabilityDow();
        unavailabilityDow1.setDow("Monday");
        unavailabilityDow1.setDowUnavailabilityType("All Day");
        dowList.add(unavailabilityDow1);

        UnavailabilityDow unavailabilityDow2 = new UnavailabilityDow();
        unavailabilityDow2.setDow("tuesday");
        unavailabilityDow2.setDowUnavailabilityType("AM");
        dowList.add(unavailabilityDow2);

        UnavailabilityDow unavailabilityDow3 = new UnavailabilityDow();
        unavailabilityDow3.setDow("WEDNESDAY");
        unavailabilityDow3.setDowUnavailabilityType("pm");
        dowList.add(unavailabilityDow3);
        return dowList;
    }
}
