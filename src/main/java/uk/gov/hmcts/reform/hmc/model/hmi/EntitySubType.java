package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntitySubType {

    private String entityClassCode;

    private String entityTitle;

    private String entityFirstName;

    private String entityLastName;

    private Boolean entitySensitiveClient;

    private String entityAlertMessage;

    private String entityInterpreterLanguage;

    private String entityCompanyName;

    private String entitySpecialNeedsOther;

    private String entityCustodyStatus;

}
