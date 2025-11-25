package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.URL;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CaseDetails {

    @NotEmpty(message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    @Pattern(regexp = "^\\w{4}$", message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    private String hmctsServiceCode;

    @NotEmpty(message = ValidationError.CASE_REF_EMPTY)
    @LuhnCheck(message = ValidationError.INVALID_CASE_REFERENCE, ignoreNonDigitCharacters = false)
    private String caseRef;

    @Size(max = 70, message = ValidationError.EXTERNAL_CASE_REFERENCE_MAX_LENGTH)
    private String externalCaseReference;

    @NotEmpty(message = ValidationError.CASE_DEEP_LINK_EMPTY)
    @Size(max = 1024, message = ValidationError.CASE_DEEP_LINK_MAX_LENGTH)
    @URL(message = ValidationError.CASE_DEEP_LINK_INVALID)
    private String caseDeepLink;

    @NotEmpty(message = ValidationError.HMCTS_INTERNAL_CASE_NAME_EMPTY)
    @Size(max = 1024, message = ValidationError.HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH)
    @Pattern(regexp = "^[\\p{Ll}\\p{Lm}\\p{Lt}\\p{Lu}\\p{N}\\p{P}\\p{Zs}\\p{Sc}\\p{Sk}\\p{Sm}\\p{Zs}]*$",
        message = ValidationError.INVALID_HMCTS_INTERNAL_CASE_NAME)
    private String hmctsInternalCaseName;

    @NotEmpty(message = ValidationError.PUBLIC_CASE_NAME_EMPTY)
    @Size(max = 1024, message = ValidationError.PUBLIC_CASE_NAME_MAX_LENGTH)
    @Pattern(regexp = "^[\\p{Ll}\\p{Lm}\\p{Lt}\\p{Lu}\\p{N}\\p{P}\\p{Zs}\\p{Sc}\\p{Sk}\\p{Sm}\\p{Zs}]*$",
        message = ValidationError.INVALID_PUBLIC_CASE_NAME)
    private String publicCaseName;

    private Boolean caseAdditionalSecurityFlag = false;

    private Boolean caseInterpreterRequiredFlag;

    @Valid
    @NotNull(message = ValidationError.CASE_CATEGORY_EMPTY)
    @NotEmpty(message = ValidationError.INVALID_CASE_CATEGORIES)
    private List<CaseCategory> caseCategories;

    @NotEmpty(message = ValidationError.CASE_MANAGEMENT_LOCATION_CODE_EMPTY)
    @Size(max = 40, message = ValidationError.CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH)
    private String caseManagementLocationCode;

    @JsonProperty("caserestrictedFlag")
    @NotNull(message = ValidationError.CASE_RESTRICTED_FLAG_NULL_EMPTY)
    private Boolean caseRestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    @NotNull(message = ValidationError.CASE_SLA_START_DATE_EMPTY)
    private LocalDate caseSlaStartDate;

}
