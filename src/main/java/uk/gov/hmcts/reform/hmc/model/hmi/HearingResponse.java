package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    private List<HearingDaySchedule> hearingDaySchedule;

}
