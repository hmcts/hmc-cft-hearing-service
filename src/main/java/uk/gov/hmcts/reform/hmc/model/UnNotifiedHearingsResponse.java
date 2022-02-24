package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@Setter
@Getter
public class UnNotifiedHearingsResponse {

    @NotNull
    private List<String> hearingIds;

    @NotNull
    private Long totalFound;
}
