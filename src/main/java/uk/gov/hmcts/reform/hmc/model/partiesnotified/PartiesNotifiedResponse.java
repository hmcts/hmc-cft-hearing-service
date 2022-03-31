package uk.gov.hmcts.reform.hmc.model.partiesnotified;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartiesNotifiedResponse {

    private Integer responseVersion;

    private LocalDateTime responseReceivedDateTime;

    private Integer requestVersion;

    private LocalDateTime partiesNotified;

    private JsonNode serviceData;
}
