package uk.gov.hmcts.reform.hmc.client.hmi;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

@Data
@NoArgsConstructor
public class VenueLocationReference {

    private String key;
    @NotNull(message = ValidationError.VENUE_LOCATION_CODE_NULL)
    @Size(max = 30, message = ValidationError.VENUE_LOCATION_CODE_LENGTH)
    private String value;
}
