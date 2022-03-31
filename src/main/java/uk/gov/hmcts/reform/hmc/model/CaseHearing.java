package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CaseHearing {

    @JsonProperty("hearingID")
    @Size(max = 60)
    @NotNull
    private Long hearingId;

    @NotNull
    private LocalDateTime hearingRequestDateTime;

    @Size(max = 60)
    @NotNull
    private String hearingType;

    @Size(max = 60)
    @NotNull
    private String hmcStatus;

    @NotNull
    private LocalDateTime lastResponseReceivedDateTime;

    @Size(max = 60)
    @NotNull
    private Long responseVersion;

    @Size(max = 60)
    @NotNull
    private String hearingListingStatus;

    @Size(max = 60)
    @NotNull
    private String listAssistCaseStatus;

    @NotNull
    private List<HearingDaySchedule> hearingDaySchedule;

    @Size(max = 30)
    private String hearingGroupRequestId;

    private Boolean hearingIsLinkedFlag;

}
