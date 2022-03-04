package uk.gov.hmcts.reform.hmc.model.partiesnotified;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PartiesNotifiedResponses {

    private Long hearingID;

    private List<PartiesNotifiedResponse> responses;

}
