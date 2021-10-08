package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CaseDetails {

    @NotNull(message = ValidationError.HMCTS_SERVICE_CODE_EMPTY)
    @Pattern(regexp = "^\\w{4}$")
    private String hmctsServiceCode;

    @NotNull(message = ValidationError.CASE_REF_EMPTY)
    @Pattern(regexp = "^\\d{16}$")
    private String caseRef;

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_EMPTY)
    private String requestTimeStamp;

    @Size(max = 70, message = ValidationError.EXTERNAL_CASE_REFERENCE_MAX_LENGTH)
    private String externalCaseReference;

    @NotNull(message = ValidationError.CASE_DEEP_LINK_EMPTY)
    @Size(max = 1024, message = ValidationError.CASE_DEEP_LINK_MAX_LENGTH)
    //uri format
    private String caseDeepLink;

    @NotNull(message = ValidationError.HMCTS_INTERNAL_CASE_NAME_EMPTY)
    @Size(max = 1024, message = ValidationError.HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH)
    private String hmctsInternalCaseName;

    @NotNull(message = ValidationError.PUBLIC_CASE_NAME_EMPTY)
    @Size(max = 1024, message = ValidationError.PUBLIC_CASE_NAME_MAX_LENGTH)
    private String publicCaseName;

    private Boolean caseAdditionalSecurityFlag = false;

    private Boolean caseInterpreterRequiredFlag;

    private CaseCategory[] caseCategories;

    @NotNull(message = ValidationError.CASE_MANAGEMENT_LOCATION_CODE_EMPTY)
    @Size(max = 40, message = ValidationError.CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH)
    private String caseManagementLocationCode;

    private Boolean caserestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    @NotNull(message = ValidationError.CASE_SLA_START_DATE_EMPTY)
    private String caseSlaStartDate;

}
