package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetHearingsResponse {

    @JsonProperty("hmctsServiceCode")
    private String hmctsServiceCode;

    private String caseRef;

    @Valid
    @NotNull
    private List<CaseHearing> caseHearings;

}
