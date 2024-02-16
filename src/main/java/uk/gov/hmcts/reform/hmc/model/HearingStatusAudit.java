package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class HearingStatusAudit implements Serializable {

    @NotNull
    private String hearingServiceId;

    @NotNull
    private String hearingId;

    @NotNull
    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull
    private LocalDateTime statusUpdateDateTime;

    @NotNull
    private String hearingEvent;

    @NotNull
    private String httpStatus;

    private String source;

    private String target;

    private JsonNode errorDescription;

    @NotNull
    private String requestVersion;

}
