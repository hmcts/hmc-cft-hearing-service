package uk.gov.hmcts.reform.hmc.model.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EntityUnavailableDate {

    private LocalDateTime unavailableStartDate;

    private LocalDateTime unavailableEndDate;

}
