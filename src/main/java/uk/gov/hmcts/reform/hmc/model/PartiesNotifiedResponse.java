package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartiesNotifiedResponse {

    private String responseVersion;

    private LocalDateTime responseReceivedDateTime;

    private String requestVersion;

    private LocalDateTime partiesNotified;

    private JsonNode serviceData;
}
