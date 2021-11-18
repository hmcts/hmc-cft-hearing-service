package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_DOW_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_RANGE_TYPE;

@Component
public class UnAvailabilityDetailMapper {

    public UnAvailabilityDetailMapper() {
    }

    public List<UnavailabilityEntity> modelToEntity(PartyDetails partyDetail, HearingPartyEntity hearingPartyEntity) {
        List<UnavailabilityEntity> unavailabilityEntities = new ArrayList<>();
        setDowDetails(partyDetail, hearingPartyEntity, unavailabilityEntities);
        setRangeDetails(partyDetail, hearingPartyEntity, unavailabilityEntities);
        return unavailabilityEntities;
    }

    private void setRangeDetails(PartyDetails partyDetail, HearingPartyEntity hearingPartyEntity,
                                 List<UnavailabilityEntity> unavailabilityEntities) {
        for (UnavailabilityRanges range : partyDetail.getUnavailabilityRanges()) {
            final UnavailabilityEntity rangeEntity = new UnavailabilityEntity();
            rangeEntity.setStartDate(range.getUnavailableFromDate());
            rangeEntity.setEndDate(range.getUnavailableToDate());
            rangeEntity.setUnAvailabilityType(UNAVAILABILITY_RANGE_TYPE);
            rangeEntity.setHearingParty(hearingPartyEntity);
            unavailabilityEntities.add(rangeEntity);
        }
    }

    private void setDowDetails(PartyDetails partyDetail, HearingPartyEntity hearingPartyEntity,
                               List<UnavailabilityEntity> unavailabilityEntities) {
        for (UnavailabilityDow dow : partyDetail.getUnavailabilityDow()) {
            final UnavailabilityEntity dowEntity = new UnavailabilityEntity();
            dowEntity.setDayOfWeekUnavailable(DayOfWeekUnavailable.valueOf(dow.getDow()));
            dowEntity.setDayOfWeekUnavailableType(
                DayOfWeekUnAvailableType.valueOf(dow.getDowUnavailabilityType()));
            dowEntity.setUnAvailabilityType(UNAVAILABILITY_DOW_TYPE);
            dowEntity.setHearingParty(hearingPartyEntity);
            unavailabilityEntities.add(dowEntity);
        }
    }
}
