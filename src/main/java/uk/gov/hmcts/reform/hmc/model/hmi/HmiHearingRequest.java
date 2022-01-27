package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HmiHearingRequest {

    @JsonProperty("_case")
    private HmiCaseDetails caseDetails;

    private Listing listing;

    private List<Entity> entities;
}
