package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CASE_HQ;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CLUSTER;
import static uk.gov.hmcts.reform.hmc.constants.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EPIMS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.Region;

class ListingLocationsMapperTest {

    private static final String LOCATION_ID = "LocationType";
    private static final String LOCATION_ID_TWO = "LocationIdTwo";
    private static final String LOCATION_TYPE = "court";
    private static final String LOCATION_TYPE_TWO = "Region";

    @Test
    void shouldReturnListingLocations() {
        HearingLocation hearingLocation = new HearingLocation();
        hearingLocation.setLocationId(LOCATION_ID);
        hearingLocation.setLocationType(LOCATION_TYPE);
        HearingLocation hearingLocationTwo = new HearingLocation();
        hearingLocationTwo.setLocationId(LOCATION_ID_TWO);
        hearingLocationTwo.setLocationType(LOCATION_TYPE_TWO);
        List<HearingLocation> hearingLocationList = Arrays.asList(hearingLocation, hearingLocationTwo);
        ListingLocationsMapper listingLocationsMapper = new ListingLocationsMapper();
        List<ListingLocation> actualListingLocations = listingLocationsMapper.getListingLocations(hearingLocationList);
        ListingLocation listingLocation = ListingLocation.builder()
            .locationId(LOCATION_ID)
            .locationType(COURT)
            .locationReferenceType(EPIMS)
            .build();
        ListingLocation listingLocationTwo = ListingLocation.builder()
            .locationId(LOCATION_ID_TWO)
            .locationType(Region)
            .locationReferenceType(CASE_HQ)
            .build();
        List<ListingLocation> expectedListingLocations = Arrays.asList(listingLocation, listingLocationTwo);
        assertEquals(expectedListingLocations, actualListingLocations);
    }

    @Test
    void shouldReturnListingLocationsWhenTypeIsCluster() {
        HearingLocation hearingLocation = new HearingLocation();
        hearingLocation.setLocationId(LOCATION_ID);
        hearingLocation.setLocationType("cluster");
        HearingLocation hearingLocationTwo = new HearingLocation();
        hearingLocationTwo.setLocationId(LOCATION_ID_TWO);
        hearingLocationTwo.setLocationType(LOCATION_TYPE_TWO);
        List<HearingLocation> hearingLocationList = Arrays.asList(hearingLocation, hearingLocationTwo);
        ListingLocationsMapper listingLocationsMapper = new ListingLocationsMapper();
        List<ListingLocation> actualListingLocations = listingLocationsMapper.getListingLocations(hearingLocationList);
        ListingLocation listingLocation = ListingLocation.builder()
            .locationId(LOCATION_ID)
            .locationType(CLUSTER)
            .locationReferenceType(CASE_HQ)
            .build();
        ListingLocation listingLocationTwo = ListingLocation.builder()
            .locationId(LOCATION_ID_TWO)
            .locationType(Region)
            .locationReferenceType(CASE_HQ)
            .build();
        List<ListingLocation> expectedListingLocations = Arrays.asList(listingLocation, listingLocationTwo);
        assertEquals(expectedListingLocations, actualListingLocations);
    }
}
