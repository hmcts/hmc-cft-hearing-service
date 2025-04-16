package uk.gov.hmcts.reform.hmc.client.datastore.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DataStoreCaseDetails {

    @JsonProperty("id")
    @JsonAlias("reference")
    private String id;

    private String jurisdiction;

    @JsonProperty("case_type_id")
    @JsonAlias("case_type") // alias to match with data-store V2 external api GetCase
    private String caseTypeId;

}
