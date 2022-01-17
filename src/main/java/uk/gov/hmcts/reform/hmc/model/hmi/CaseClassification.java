package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseClassification {

    private String caseClassificationService;

    private String caseClassificationType;

    private String caseClassificationSubType;

}
