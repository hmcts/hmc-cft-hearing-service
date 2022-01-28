package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HearingResponse {

    @JsonProperty("listAssistTransactionID")
    @NotNull
    private Long listAssistTransactionID;

    @JsonProperty("laCaseStatus")
    @NotNull
    private String laCaseStatus;

    @JsonProperty("listingStatus")
    private String listingStatus;

    @JsonProperty("receivedDateTime")
    @NotNull
    private LocalDateTime receivedDateTime;

    @JsonProperty("responseVersion")
    @NotNull
    private Long responseVersion;

    @JsonProperty("hearingCancellationReason")
    private String hearingCancellationReason;

}
