package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingMultiDay;

import java.util.ArrayList;

import static uk.gov.hmcts.reform.hmc.constants.Constants.DURATION_OF_DAY;

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

    public Listing getListing(HearingDetails hearingDetails) {

        Listing listing = Listing.builder()
            .listingAutoCreateFlag(hearingDetails.getAutoListFlag())
            .listingPriority(hearingDetails.getHearingPriorityType())
            .listingType(hearingDetails.getHearingType())
            .listingNumberAttendees(hearingDetails.getNumberOfPhysicalAttendees())
            .listingComments(hearingDetails.getListingComments())
            .listingRequestedBy(hearingDetails.getHearingRequester())
            .listingPrivateFlag(hearingDetails.getPrivateHearingRequiredFlag())
            .listingJohs(listingJohsMapper.getListingJohs(hearingDetails.getPanelRequirements()))
            .listingHearingChannels(hearingDetails.getHearingChannels())
            .listingLocations(listingLocationsMapper.getListingLocations(hearingDetails.getHearingLocations()))
            .amendReasonCode(hearingDetails.getAmendReasonCode())
            .listingJohSpecialisms(hearingDetails.getPanelRequirements().getPanelSpecialisms())
            .listingJohTickets(hearingDetails.getPanelRequirements().getAuthorisationSubType())
            .listingOtherConsiderations(
                    listingOtherConsiderationsMapper.getListingOtherConsiderations(
                            hearingDetails.getHearingInWelshFlag(),
                            hearingDetails.getFacilitiesRequired())
                    )
            .build();
        if (hearingDetails.getHearingWindow() != null) {
            listing.setListingDate(hearingDetails.getHearingWindow().getFirstDateTimeMustBe());
            if (hearingDetails.getHearingWindow().getDateRangeStart() != null) {
                listing.setListingStartDate(hearingDetails.getHearingWindow().getDateRangeStart());
            }
            if (hearingDetails.getHearingWindow().getDateRangeEnd() != null) {
                listing.setListingEndDate(hearingDetails.getHearingWindow().getDateRangeEnd());
            }
        }
        if (hearingDetails.getPanelRequirements().getRoleType() != null && !hearingDetails
            .getPanelRequirements().getRoleType().isEmpty()) {
            listing.setListingJohTiers(new ArrayList<>(hearingDetails.getPanelRequirements()
                                                           .getRoleType()));
        }
        if (hearingDetails.isMultiDayHearing()) {
            listing.setListingDuration(DURATION_OF_DAY);
            listing.setListingMultiDay(calculateMultiDayDurations(hearingDetails.getDuration()));
        } else {
            listing.setListingDuration(hearingDetails.getDuration());
        }
        return listing;
    }

    private ListingMultiDay calculateMultiDayDurations(Integer hearingDetailsDuration) {
        int weeks = getWeeks(hearingDetailsDuration);
        int days = getDays(hearingDetailsDuration, weeks);
        int hours = getHours(hearingDetailsDuration, weeks, days);
        return setMultiDay(weeks, days, hours);
    }

    private ListingMultiDay setMultiDay(int weeks, int days, int hours) {
        ListingMultiDay multiDay = new ListingMultiDay();
        multiDay.setWeeks(weeks);
        multiDay.setDays(days);
        multiDay.setHours(hours);
        return multiDay;
    }

    private int getHours(Integer hearingDetailsDuration, int weeks, int days) {
        return hearingDetailsDuration - (weeks * 360 * 5) - (days * 360);
    }

    private int getDays(Integer hearingDetailsDuration, int weeks) {
        return ((hearingDetailsDuration - weeks * 360 * 5) / 360);
    }

    private int getWeeks(Integer hearingDetailsDuration) {
        return (hearingDetailsDuration / (360 * 5));
    }

}
