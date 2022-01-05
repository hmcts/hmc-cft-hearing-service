package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDay;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnavailableDaysMapperTest {

    private static final String DAY = "MON";
    private static final String DAY_TWO = "FRI";
    private static final String AM_TIME = "AM";
    private static final String PM_TIME = "PM";

    @Test
    void shouldReturnUnavailableDays() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDow(DAY);
        unavailabilityDow.setDowUnavailabilityType(AM_TIME);
        UnavailabilityDow unavailabilityDowTwo = new UnavailabilityDow();
        unavailabilityDowTwo.setDow(DAY_TWO);
        unavailabilityDowTwo.setDowUnavailabilityType(PM_TIME);
        List<UnavailabilityDow> unavailabilityDowList = Arrays.asList(unavailabilityDow, unavailabilityDowTwo);
        UnavailableDaysMapper unavailableDaysMapper = new UnavailableDaysMapper();
        List<EntityUnavailableDay> actualUnavailableDays = unavailableDaysMapper
            .getUnavailableDays(unavailabilityDowList);
        EntityUnavailableDay entityUnavailableDay = EntityUnavailableDay.builder()
            .unavailableType(AM_TIME)
            .unavailableDayOfWeek(DAY)
            .build();
        EntityUnavailableDay entityUnavailableDayTwo = EntityUnavailableDay.builder()
            .unavailableType(PM_TIME)
            .unavailableDayOfWeek(DAY_TWO)
            .build();
        List<EntityUnavailableDay> expectedUnavailableDays = Arrays.asList(entityUnavailableDay,
                                                                           entityUnavailableDayTwo);
        assertEquals(expectedUnavailableDays, actualUnavailableDays);
    }

    @Test
    void shouldHandleNullUnavailabilityDows() {
        UnavailableDaysMapper unavailableDaysMapper = new UnavailableDaysMapper();
        unavailableDaysMapper.getUnavailableDays(null);
    }

    @Test
    void shouldHandleEmptyUnavailabilityDows() {
        UnavailableDaysMapper unavailableDaysMapper = new UnavailableDaysMapper();
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailableDaysMapper.getUnavailableDays(Collections.singletonList(unavailabilityDow));
    }

}
