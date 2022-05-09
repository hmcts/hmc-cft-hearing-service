package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedHearingDetails {

    private Long hearingId;

    private Long hearingOrder;

    private String caseRef;

    private String hmctsInternalCaseName;
}
