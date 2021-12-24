package uk.gov.hmcts.reform.hmc.client.datastore.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

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

    @JsonAlias("created_on")
    @JsonProperty("created_date")
    private LocalDateTime createdDate;

    @JsonProperty("last_modified")
    @JsonAlias("last_modified_on")
    private LocalDateTime lastModified;

    @JsonProperty("last_state_modified_date")
    @JsonAlias("last_state_modified_on")
    private LocalDateTime lastStateModifiedDate;

    private String state;

    @JsonProperty("security_classification")
    private SecurityClassification securityClassification;

    @JsonProperty("case_data")
    @JsonAlias("data")
    private Map<String, JsonNode> data;

    @JsonProperty("data_classification")
    private Map<String, JsonNode> dataClassification;

}
