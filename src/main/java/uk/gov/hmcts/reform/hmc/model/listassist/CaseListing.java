package uk.gov.hmcts.reform.hmc.model.listassist;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CaseListing {
    private String caseListingRequestId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer caseLinkOrder;
}
