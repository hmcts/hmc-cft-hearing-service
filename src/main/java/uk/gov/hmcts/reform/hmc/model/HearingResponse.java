package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    @Size(max = 30)
    @NotNull
    private Long hearingRequestId;

    @JsonProperty("status")
    @Size(max = 100)
    @NotNull
    private String status;

    @JsonProperty("timeStamp")
    @NotNull
    private LocalDateTime timeStamp;

    @JsonProperty("versionNumber")
    @NotNull
    private Integer versionNumber;

    @JsonProperty("responseVersion")
    @NotNull
    private Integer responseVersion;

    @JsonProperty("requestVersion")
    @NotNull
    private Integer requestVersion;

    @JsonProperty("partiesNotified")
    private LocalDateTime partiesNotifiedDateTime;

    @JsonProperty("serviceData")
    private Map<String, Object> serviceData;

}
