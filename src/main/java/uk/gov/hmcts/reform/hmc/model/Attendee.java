package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

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
