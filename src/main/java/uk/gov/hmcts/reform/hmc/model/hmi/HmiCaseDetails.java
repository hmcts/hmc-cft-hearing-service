package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HmiCaseDetails {

    @JsonProperty("caseIdHMCTS")
    private String caseIdHmcts;

    private String caseListingRequestId;

    private String caseTitle;

    private String caseJurisdiction;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T00:00:00Z'")
    private LocalDate caseRegistered;

    private ListingLocation caseCourt;

    private List<CaseClassification> caseClassifications;

    private Boolean caseInterpreterRequiredFlag;

    private Boolean caseRestrictedFlag;

    private Integer caseVersionId;

    private List<CaseLinks> caseLinks;

    private String casePublishedName;

    private Boolean caseAdditionalSecurityFlag;

    private String linkedHearingGroupStatus;

}
