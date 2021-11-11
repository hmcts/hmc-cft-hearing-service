package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class RequestDetails {

    @NotNull
    private LocalDateTime requestTimeStamp;
}
