package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class HearingActualsOutcome implements Serializable {

    private String hearingType;

    private Boolean hearingFinalFlag;

    private String hearingResult;

    private String hearingResultReasonType;

    private LocalDate hearingResultDate;
}
