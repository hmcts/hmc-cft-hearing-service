package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Listing {

    private Boolean listingAutoCreateFlag;

    private String listingPriority;

    private String listingType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T00:00:00Z'")
    private LocalDate listingStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T00:00:00Z'")
    private LocalDate listingEndDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
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

    private String amendReasonCode;

    private List<String> listingJohTickets;

    private List<String> listingJohSpecialisms;

    private List<String> listingOtherConsiderations;

    private ListingMultiDay listingMultiDay;

    private String listingLanguage;

    private List<String> listingRoomAttributes;
}
