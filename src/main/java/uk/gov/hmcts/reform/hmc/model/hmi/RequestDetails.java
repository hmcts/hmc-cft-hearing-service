package uk.gov.hmcts.reform.hmc.model.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class RequestDetails {

    private LocalDateTime timestamp;

    private String status;

    private int versionNumber;

    private LocalDateTime partiesNotified;


}
