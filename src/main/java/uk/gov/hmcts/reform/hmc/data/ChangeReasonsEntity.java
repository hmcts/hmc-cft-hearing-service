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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Table(name = "change_reasons")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class ChangeReasonsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 4353447468967037802L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "change_reasons_id_seq_generator")
    @SequenceGenerator(name = "change_reasons_id_seq_generator", 
        sequenceName = "change_reasons_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "change_reason_type")
    private String changeReasonType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;
}
