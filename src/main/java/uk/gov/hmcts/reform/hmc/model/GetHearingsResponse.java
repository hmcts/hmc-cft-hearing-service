package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
