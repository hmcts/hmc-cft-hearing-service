package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;

import java.util.ArrayList;
import java.util.List;

@Component
public class ListingMapper {

    private final ListingJohsMapper listingJohsMapper;
    private final ListingLocationsMapper listingLocationsMapper;

    @Autowired
    public ListingMapper(ListingJohsMapper listingJohsMapper, ListingLocationsMapper listingLocationsMapper) {
        this.listingJohsMapper = listingJohsMapper;
        this.listingLocationsMapper = listingLocationsMapper;
    }

    public Listing getListing(HearingDetails hearingDetails, List<String> preferredHearingChannels) {

        Listing listing = Listing.builder()
            .listingAutoCreateFlag(hearingDetails.getAutoListFlag())
            .listingPriority(hearingDetails.getHearingPriorityType())
            .listingType(hearingDetails.getHearingType())
            .listingDuration(hearingDetails.getDuration())
            .listingDate(hearingDetails.getHearingWindow().getFirstDateTimeMustBe())
            .listingNumberAttendees(hearingDetails.getNumberOfPhysicalAttendees())
            .listingComments(hearingDetails.getListingComments())
            .listingRequestedBy(hearingDetails.getHearingRequester())
            .listingPrivateFlag(hearingDetails.getPrivateHearingRequiredFlag())
            .listingJohs(listingJohsMapper.getListingJohs(hearingDetails.getPanelRequirements()))
            .listingHearingChannels(preferredHearingChannels)
            .listingLocations(listingLocationsMapper.getListingLocations(hearingDetails.getHearingLocations()))
            .build();
        if (hearingDetails.getHearingWindow().getDateRangeStart() != null) {
            listing.setListingStartDate(hearingDetails.getHearingWindow().getDateRangeStart());
        }
        if (hearingDetails.getHearingWindow().getDateRangeEnd() != null) {
            listing.setListingEndDate(hearingDetails.getHearingWindow().getDateRangeEnd());
        }
        if (hearingDetails.getPanelRequirements().getRoleType() != null && !hearingDetails
            .getPanelRequirements().getRoleType().isEmpty()) {
            listing.setListingJohTiers(new ArrayList<>(hearingDetails.getPanelRequirements()
                                                           .getRoleType()));
        }
        return listing;
    }
}
