package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class HearingStatusAudit implements Serializable {

    @NotNull
    private String hearingServiceId;

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

    private Map<String, Object> errorDescription;

    @NotNull
    private String requestVersion;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime responseDateTime;

}
