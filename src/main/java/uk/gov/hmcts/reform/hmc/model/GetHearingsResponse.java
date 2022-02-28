package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetHearingsResponse {

    @JsonProperty("hmctsServiceID")
    private String hmctsServiceId;

    private String caseRef;

    @Valid
    @NotNull
    private List<CaseHearing> caseHearings;

}
