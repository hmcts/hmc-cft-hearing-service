package uk.gov.hmcts.reform.hmc.model.listassist;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseListing {
    private String caseListingRequestId;
    private Integer caseLinkOrder;
}
