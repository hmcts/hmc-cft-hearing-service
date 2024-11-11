package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@Setter
@Getter
public class UnNotifiedHearingsResponse {

    private List<String> hearingIds;

    @NotNull
    private Long totalFound;
}
