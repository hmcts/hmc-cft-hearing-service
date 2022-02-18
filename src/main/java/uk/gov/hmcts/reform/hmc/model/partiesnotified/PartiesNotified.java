package uk.gov.hmcts.reform.hmc.model.partiesnotified;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PartiesNotified {

    @NotNull
    private int requestVersion;

    private JsonNode serviceData;
}
