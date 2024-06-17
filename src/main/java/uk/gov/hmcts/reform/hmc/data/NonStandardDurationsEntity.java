package uk.gov.hmcts.reform.hmc.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "non_standard_durations")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class NonStandardDurationsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2548536101352732983L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "non_standard_durations_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "non_standard_hearing_duration_reason_type")
    private String nonStandardHearingDurationReasonType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    public NonStandardDurationsEntity(NonStandardDurationsEntity original) {
        this.id = original.id;
        this.nonStandardHearingDurationReasonType = original.nonStandardHearingDurationReasonType;
        this.caseHearing = original.caseHearing;
    }
}
