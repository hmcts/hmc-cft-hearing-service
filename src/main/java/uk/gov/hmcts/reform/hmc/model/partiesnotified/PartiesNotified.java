package uk.gov.hmcts.reform.hmc.model.partiesnotified;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PartiesNotified {

    @NotNull
    private int requestVersion;

    private Object serviceData;
}
