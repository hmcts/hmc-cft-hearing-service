package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    }

    @Test
    void modelToEntityTest_RangeDetails() {
        UnAvailabilityDetailMapper mapper = new UnAvailabilityDetailMapper();
        List<UnavailabilityRanges> rangeDetails = getUnAvailabilityRangeDetails();
        PartyDetails partyDetail = new PartyDetails();
        partyDetail.setUnavailabilityRanges(rangeDetails);
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<UnavailabilityEntity> entities = mapper.modelToEntity(partyDetail, hearingPartyEntity);
        assertEquals(LocalDate.parse("2020-09-10"), entities.get(0).getEndDate());
        assertEquals(LocalDate.parse("2021-10-10"), entities.get(0).getStartDate());
        assertEquals(LocalDate.parse("2022-10-15"), entities.get(1).getEndDate());
        assertEquals(LocalDate.parse("2023-10-20"), entities.get(1).getStartDate());
    }

    @Test
    void modelToEntityTest_Dow_And_RangeDetails() {
        UnAvailabilityDetailMapper mapper = new UnAvailabilityDetailMapper();
        PartyDetails partyDetail = getUnAvailabilityDetails();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<UnavailabilityEntity> entities = mapper.modelToEntity(partyDetail, hearingPartyEntity);
        assertEquals(DayOfWeekUnavailable.MONDAY, entities.get(0).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.ALL, entities.get(0).getDayOfWeekUnavailableType());
        assertEquals(null, entities.get(0).getEndDate());
        assertEquals(null, entities.get(0).getStartDate());
        assertEquals(DayOfWeekUnavailable.TUESDAY, entities.get(1).getDayOfWeekUnavailable());
        assertEquals(DayOfWeekUnAvailableType.AM, entities.get(1).getDayOfWeekUnavailableType());
        assertEquals(null, entities.get(1).getEndDate());
        assertEquals(null, entities.get(1).getStartDate());

        assertEquals(null, entities.get(2).getDayOfWeekUnavailable());
        assertEquals(null, entities.get(2).getDayOfWeekUnavailableType());
        assertEquals(LocalDate.parse("2020-09-10"), entities.get(2).getEndDate());
        assertEquals(LocalDate.parse("2021-10-10"), entities.get(2).getStartDate());
        assertEquals(null, entities.get(3).getDayOfWeekUnavailable());
        assertEquals(null, entities.get(3).getDayOfWeekUnavailableType());
        assertEquals(LocalDate.parse("2022-10-15"), entities.get(3).getEndDate());
        assertEquals(LocalDate.parse("2023-10-20"), entities.get(3).getStartDate());
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

        UnavailabilityRanges unavailabilityRanges2 = new UnavailabilityRanges();
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2022-10-15"));
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2023-10-20"));


        List<UnavailabilityRanges> ranges = new ArrayList<>();
        ranges.add(unavailabilityRanges1);
        ranges.add(unavailabilityRanges2);
        return ranges;
    }

    private List<UnavailabilityDow> getUnAvailabilityDowDetails() {
        UnavailabilityDow unavailabilityDow1 = new UnavailabilityDow();
        unavailabilityDow1.setDow("MONDAY");
        unavailabilityDow1.setDowUnavailabilityType("ALL");

        UnavailabilityDow unavailabilityDow2 = new UnavailabilityDow();
        unavailabilityDow2.setDow("TUESDAY");
        unavailabilityDow2.setDowUnavailabilityType("AM");


        List<UnavailabilityDow> dowList = new ArrayList<>();
        dowList.add(unavailabilityDow1);
        dowList.add(unavailabilityDow2);
        return dowList;
    }
}
