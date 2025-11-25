package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "non_standard_durations_id_seq_generator")
    @SequenceGenerator(name = "non_standard_durations_id_seq_generator", 
        sequenceName = "non_standard_durations_id_seq", allocationSize = 1)
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
