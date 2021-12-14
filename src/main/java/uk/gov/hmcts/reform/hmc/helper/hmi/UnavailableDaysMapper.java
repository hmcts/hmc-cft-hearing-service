package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDay;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnavailableDaysMapper {

    public List<EntityUnavailableDay> getUnavailableDays(PartyDetails partyDetails) {
        List<EntityUnavailableDay> unavailableDays = new ArrayList<>();
        if (partyDetails.getUnavailabilityDow() != null) {
            for (UnavailabilityDow unavailabilityDow : partyDetails.getUnavailabilityDow()) {
                EntityUnavailableDay entityUnavailableDay = new EntityUnavailableDay();
                entityUnavailableDay.setUnavailableDayOfWeek(unavailabilityDow.getDow());
                entityUnavailableDay.setUnavailableType(unavailabilityDow.getDowUnavailabilityType());
                unavailableDays.add(entityUnavailableDay);
            }
        }
        return unavailableDays;
    }
}
