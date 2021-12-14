package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDate;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnavailableDatesMapper {

    public List<EntityUnavailableDate> getUnavailableDates(PartyDetails partyDetails) {
        List<EntityUnavailableDate> unavailableDates = new ArrayList<>();
        if (partyDetails.getUnavailabilityRanges() != null) {
            for (UnavailabilityRanges unavailableDate : partyDetails.getUnavailabilityRanges()) {
                EntityUnavailableDate entityUnavailableDates = new EntityUnavailableDate();
                entityUnavailableDates.setUnavailableStartDate(unavailableDate.getUnavailableFromDate());
                entityUnavailableDates.setUnavailableEndDate(unavailableDate.getUnavailableToDate());
                unavailableDates.add(entityUnavailableDates);
            }
        }
        return unavailableDates;
    }
}
