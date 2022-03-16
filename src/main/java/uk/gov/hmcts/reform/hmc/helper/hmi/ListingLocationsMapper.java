package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.LocationType;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CASE_HQ;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CLUSTER;
import static uk.gov.hmcts.reform.hmc.constants.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EPIMS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.Region;

@Component
public class ListingLocationsMapper {

    public List<ListingLocation> getListingLocations(List<HearingLocation> hearingLocationList) {
        List<ListingLocation> listingLocations = new ArrayList<>();
        for (HearingLocation hearingLocation : hearingLocationList) {
            ListingLocation listingLocation = ListingLocation.builder()
                .locationId(hearingLocation.getLocationId())
                .locationType(getLocationType(hearingLocation.getLocationType()))
                .locationReferenceType(getLocationReferenceType(hearingLocation.getLocationType()))
                .build();
            listingLocations.add(listingLocation);
        }
        return listingLocations;
    }

    private String getLocationReferenceType(String locationTypeInput) {
        String locationReferenceType = CASE_HQ;
        if (LocationType.COURT.equals(LocationType.getByLabel(locationTypeInput))) {
            locationReferenceType = EPIMS;
        }
        return locationReferenceType;
    }

    private String getLocationType(String locationTypeInput) {
        String locationType = Region;
        if (LocationType.COURT.equals(LocationType.getByLabel(locationTypeInput))) {
            locationType = COURT;
        } else if (LocationType.CLUSTER.equals(LocationType.getByLabel(locationTypeInput))) {
            locationType = CLUSTER;
        }
        return locationType;
    }
}
