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
    private final ListingOtherConsiderationsMapper listingOtherConsiderationsMapper;

    @Autowired
    public ListingMapper(ListingJohsMapper listingJohsMapper,
                         ListingLocationsMapper listingLocationsMapper,
                         ListingOtherConsiderationsMapper listingOtherConsiderationsMapper) {
        this.listingJohsMapper = listingJohsMapper;
        this.listingLocationsMapper = listingLocationsMapper;
        this.listingOtherConsiderationsMapper = listingOtherConsiderationsMapper;
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
            .listingJohSpecialisms(hearingDetails.getPanelRequirements().getPanelSpecialisms())
            .listingJohTickets(hearingDetails.getPanelRequirements().getAuthorisationSubType())
            .listingOtherConsiderations(
                    listingOtherConsiderationsMapper.getListingOtherConsiderations(
                            hearingDetails.getHearingInWelshFlag(),
                            hearingDetails.getFacilitiesRequired())
                    )
            .build();
        if (hearingDetails.getHearingWindow().getHearingWindowStartDateRange() != null) {
            listing.setListingStartDate(hearingDetails.getHearingWindow().getHearingWindowStartDateRange());
        }
        if (hearingDetails.getHearingWindow().getHearingWindowEndDateRange() != null) {
            listing.setListingEndDate(hearingDetails.getHearingWindow().getHearingWindowEndDateRange());
        }
        if (hearingDetails.getPanelRequirements().getRoleType() != null && !hearingDetails
            .getPanelRequirements().getRoleType().isEmpty()) {
            listing.setListingJohTiers(new ArrayList<>(hearingDetails.getPanelRequirements()
                                                           .getRoleType()));
        }
        return listing;
    }
}
