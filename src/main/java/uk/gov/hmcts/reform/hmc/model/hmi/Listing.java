package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Listing {

    private Boolean listingAutoCreateFlag;

    private String listingPriority;

    private String listingType;

    private LocalDateTime listingStartDate;

    private LocalDateTime listingEndDate;

    private LocalDateTime listingDate;

    private Integer listingDuration;

    private Integer listingNumberAttendees;

    private String listingComments;

    private String listingRequestedBy;

    private Boolean listingPrivateFlag;

    private List<String> listingJohTiers;

    private List<ListingJoh> listingJohs;

    private List<String> listingHearingChannels;

    private List<ListingLocation> listingLocations;

}
