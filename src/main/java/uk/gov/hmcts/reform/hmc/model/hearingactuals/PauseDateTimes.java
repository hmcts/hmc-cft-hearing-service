package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class PauseDateTimes {

    private LocalDateTime pauseStartTime;
    private LocalDateTime pauseEndTime;
}
