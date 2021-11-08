package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "non_standard_durations")
@Entity
@Data
public class NonStandardDurationsEntity {

    @Column(name = "case_hearing_id", nullable = false)
    private Long caseHearingID;

    @Column(name = "non_standard_hearing_duration_reason_type")
    private String nonStandardHearingDurationReasonType;

}
