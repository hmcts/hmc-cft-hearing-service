package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HearingResponse {

    @JsonProperty("listAssistTransactionID")
    private Long listAssistTransactionID;

    @JsonProperty("laCaseStatus")
    private String laCaseStatus;

    @JsonProperty("listingStatus")
    private String listingStatus;

    @JsonProperty("receivedDateTime")
    private LocalDateTime receivedDateTime;

    @JsonProperty("responseVersion")
    private Long responseVersion;

    @JsonProperty("hearingCancellationReason")
    private String hearingCancellationReason;

}
