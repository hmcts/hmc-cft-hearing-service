package uk.gov.hmcts.reform.hmc.helper.hmi;

import io.jsonwebtoken.lang.Collections;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.constants.Constants;
import uk.gov.hmcts.reform.hmc.helper.RoomAttributesMapper;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingMultiDay;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.DURATION_OF_DAY;

@Component
public class ListingMapper {

    public static final String WELSH_LANGUAGE_TRUE_VALUE = "cym";
    public static final String WELSH_LANGUAGE_FALSE_VALUE = "ENG";
    private final ListingJohsMapper listingJohsMapper;
    private final ListingLocationsMapper listingLocationsMapper;
    private final RoomAttributesMapper roomAttributesMapper;

    @Autowired
    public ListingMapper(ListingJohsMapper listingJohsMapper,
                         ListingLocationsMapper listingLocationsMapper,
                         RoomAttributesMapper roomAttributesMapper) {
        this.listingJohsMapper = listingJohsMapper;
        this.listingLocationsMapper = listingLocationsMapper;
        this.roomAttributesMapper = roomAttributesMapper;
    }

    public Listing getListing(HearingDetails hearingDetails, List<Entity> entitiesList) {

        Listing listing = Listing.builder()
            .listingPriority(hearingDetails.getHearingPriorityType())
            .listingType(hearingDetails.getHearingType())
            .listingNumberAttendees(hearingDetails.getNumberOfPhysicalAttendees())
            .listingComments(hearingDetails.getListingComments())
            .listingRequestedBy(hearingDetails.getHearingRequester())
            .listingPrivateFlag(hearingDetails.getPrivateHearingRequiredFlag())
            .listingJohs(listingJohsMapper.getListingJohs(hearingDetails.getPanelRequirements()))
            .listingHearingChannels(hearingDetails.getHearingChannels())
            .listingLocations(listingLocationsMapper.getListingLocations(hearingDetails.getHearingLocations()))
            .listingJohSpecialisms(hearingDetails.getPanelRequirements().getPanelSpecialisms())
            .listingJohTickets(hearingDetails.getPanelRequirements().getAuthorisationSubType())
            .listingLanguage(Boolean.TRUE.equals(hearingDetails.getHearingInWelshFlag())
                    ? WELSH_LANGUAGE_TRUE_VALUE : WELSH_LANGUAGE_FALSE_VALUE)
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

        if (entitiesList != null && !entitiesList.isEmpty()) {
            if (!roomAttributesMapper.areRoomAttributesFound(entitiesList, hearingDetails, listing)) {
                listing.setListingOtherConsiderations(List.of());
                listing.setRoomAttributes(List.of());
            }
        } else {
            listing.setListingOtherConsiderations(List.of());
            listing.setRoomAttributes(List.of());
        }
        setAutoListFlag(hearingDetails, listing);

        if (!Collections.isEmpty(hearingDetails.getAmendReasonCodes())) {
            listing.setAmendReasonCode(Constants.AMEND_REASON_CODE);
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
        val valueInMinutes = hearingDetailsDuration - (weeks * 360 * 5) - (days * 360);
        val result = (valueInMinutes % 60 == 0) ? valueInMinutes / 60 : Math.floor(valueInMinutes / 60) + 1;
        return Double.valueOf(result).intValue();
    }

    private int getDays(Integer hearingDetailsDuration, int weeks) {
        return ((hearingDetailsDuration - weeks * 360 * 5) / 360);
    }

    private int getWeeks(Integer hearingDetailsDuration) {
        return (hearingDetailsDuration / (360 * 5));
    }

    private void setAutoListFlag(HearingDetails hearingDetails, Listing listing) {
        if (Boolean.TRUE.equals(hearingDetails.getAutoListFlag())
            && !(roomAttributesMapper.bothAreMappedTo())) {
            listing.setListingAutoCreateFlag(false);
        }
    }


}

