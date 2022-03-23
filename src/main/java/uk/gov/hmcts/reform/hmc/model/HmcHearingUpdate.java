package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HmcHearingUpdate {
    @NotNull(message = ValidationError.HEARING_RESPONSE_DATETIME_NULL)
    private LocalDateTime hearingResponseReceivedDateTime;

    @NotNull(message = ValidationError.HEARING_BROADCAST_DATETIME_NULL)
    private LocalDateTime hearingEventBroadcastDateTime;

    @NotEmpty(message = ValidationError.HMCTS_STATUS_NULL)
    @Size(max = 100, message = ValidationError.HMCTS_STATUS_LENGTH)
    @JsonProperty("HMCStatus")
    private String hmcStatus;

    @NotNull(message = ValidationError.LISTING_STATUS_NULL)
    private ListingStatus hearingListingStatus;

    private LocalDateTime nextHearingDate;

    private String hearingVenueId;

    private String hearingJudgeId;

}
