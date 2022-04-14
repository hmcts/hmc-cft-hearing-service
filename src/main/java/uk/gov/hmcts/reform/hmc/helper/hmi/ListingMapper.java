package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_DETAILS_DURATION;

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
            //.listingDuration(hearingDetails.getDuration())
            .listingDate(hearingDetails.getHearingWindow().getFirstDateTimeMustBe())
            .listingNumberAttendees(hearingDetails.getNumberOfPhysicalAttendees())
            .listingComments(hearingDetails.getListingComments())
            .listingRequestedBy(hearingDetails.getHearingRequester())
            .listingPrivateFlag(hearingDetails.getPrivateHearingRequiredFlag())
            .listingJohs(listingJohsMapper.getListingJohs(hearingDetails.getPanelRequirements()))
            .listingHearingChannels(preferredHearingChannels)
            .listingLocations(listingLocationsMapper.getListingLocations(hearingDetails.getHearingLocations()))
            .amendReasonCode(hearingDetails.getAmendReasonCode())
            .listingJohSpecialisms(hearingDetails.getPanelRequirements().getPanelSpecialisms())
            .listingJohTickets(hearingDetails.getPanelRequirements().getAuthorisationSubType())
            .listingOtherConsiderations(
                listingOtherConsiderationsMapper.getListingOtherConsiderations(
                    hearingDetails.getHearingInWelshFlag(),
                    hearingDetails.getFacilitiesRequired()
                )
            )
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
        if (hearingDetails.getDuration() < HEARING_DETAILS_DURATION) {
            listing.setListingDuration(hearingDetails.getDuration());
        } else {
            listing.setListingDuration(HEARING_DETAILS_DURATION);
        }
        return listing;
    }
}
