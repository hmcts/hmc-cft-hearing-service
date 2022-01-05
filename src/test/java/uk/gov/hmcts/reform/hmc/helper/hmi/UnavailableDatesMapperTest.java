package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnavailableDatesMapperTest {

    @Test
    void shouldReturnUnavailableDates() {
        LocalDate localDate  = LocalDate.now();
        UnavailabilityRanges unavailabilityRanges = new UnavailabilityRanges();
        unavailabilityRanges.setUnavailableToDate(localDate);
        unavailabilityRanges.setUnavailableFromDate(localDate.minusDays(2L));
        UnavailabilityRanges unavailabilityRangesTwo = new UnavailabilityRanges();
        unavailabilityRangesTwo.setUnavailableToDate(localDate);
        unavailabilityRangesTwo.setUnavailableFromDate(localDate.minusDays(3L));
        List<UnavailabilityRanges> unavailabilityRangesList = Arrays.asList(unavailabilityRanges,
                                                                                    unavailabilityRangesTwo);
        UnavailableDatesMapper unavailableDatesMapper = new UnavailableDatesMapper();
        List<EntityUnavailableDate> actualUnavailableDates = unavailableDatesMapper
            .getUnavailableDates(unavailabilityRangesList);
        EntityUnavailableDate entityUnavailableDate = EntityUnavailableDate.builder()
            .unavailableEndDate(localDate)
            .unavailableStartDate(localDate.minusDays(2L))
            .build();
        EntityUnavailableDate entityUnavailableDateTwo = EntityUnavailableDate.builder()
            .unavailableEndDate(localDate)
            .unavailableStartDate(localDate.minusDays(3L))
            .build();
        List<EntityUnavailableDate> expectedUnavailableDates = Arrays.asList(entityUnavailableDate,
                                                                             entityUnavailableDateTwo);
        assertEquals(expectedUnavailableDates, actualUnavailableDates);
    }

    @Test
    void shouldHandleNullUnavailabilityRanges() {
        UnavailableDatesMapper unavailableDatesMapper = new UnavailableDatesMapper();
        List<EntityUnavailableDate> entityUnavailableDates = unavailableDatesMapper
            .getUnavailableDates(null);
        assertTrue(entityUnavailableDates.isEmpty());
    }
}
