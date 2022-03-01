package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class Attendee {

    @JsonProperty("partyID")
    @Size(max = 40)
    @NotNull
    private String partyId;

    @Size(max = 60)
    @NotNull
    private String hearingSubChannel;

}
