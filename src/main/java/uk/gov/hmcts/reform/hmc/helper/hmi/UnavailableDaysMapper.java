package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDay;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnavailableDaysMapper {

    public List<EntityUnavailableDay> getUnavailableDays(List<UnavailabilityDow> unavailabilityDows) {
        List<EntityUnavailableDay> unavailableDays = new ArrayList<>();
        if (unavailabilityDows != null) {
            for (UnavailabilityDow unavailabilityDow : unavailabilityDows) {
                EntityUnavailableDay entityUnavailableDay = EntityUnavailableDay.builder()
                    .unavailableDayOfWeek(unavailabilityDow.getDow())
                    .unavailableType(unavailabilityDow.getDowUnavailabilityType())
                    .build();
                unavailableDays.add(entityUnavailableDay);
            }
        }
        return unavailableDays;
    }
}
