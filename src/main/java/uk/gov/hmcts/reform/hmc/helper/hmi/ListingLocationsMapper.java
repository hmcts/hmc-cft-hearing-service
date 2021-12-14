package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;

import java.util.ArrayList;
import java.util.List;

@Component
public class ListingLocationsMapper {

    public List<ListingLocation> getListingLocations(List<HearingLocation> hearingLocationList) {
        List<ListingLocation> listingLocations = new ArrayList<>();
        for (HearingLocation hearingLocation : hearingLocationList) {
            ListingLocation listingLocation = ListingLocation.builder()
                .locationId(hearingLocation.getLocationId())
                .locationType(hearingLocation.getLocationType())
                .build();
            listingLocations.add(listingLocation);
        }
        return listingLocations;
    }
}
