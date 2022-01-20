package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "cancellation_reasons")
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class CancellationReasonsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "cancellation_reasons_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "cancellation_reason_type")
    private String cancellationReasonType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

}
