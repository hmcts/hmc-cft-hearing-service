package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDate;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnavailableDatesMapper {

    public List<EntityUnavailableDate> getUnavailableDates(List<UnavailabilityRanges> unavailabilityRanges) {
        List<EntityUnavailableDate> unavailableDates = new ArrayList<>();
        if (unavailabilityRanges != null) {
            for (UnavailabilityRanges unavailableRange : unavailabilityRanges) {
                EntityUnavailableDate entityUnavailableDates = EntityUnavailableDate.builder()
                    .unavailableStartDate(unavailableRange.getUnavailableFromDate())
                    .unavailableEndDate(unavailableRange.getUnavailableToDate())
                    .unavailableType(unavailableRange.getUnavailabilityType())
                    .build();
                unavailableDates.add(entityUnavailableDates);
            }
        }
        return unavailableDates;
    }
}
