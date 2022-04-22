package uk.gov.hmcts.reform.hmc.model.listassist;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CaseListing {
    private String caseListingRequestId;
    private Integer caseLinkOrder;
}
