package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "cancellation_reasons")
@Entity
@Data
public class CancellationReasonsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "cancellation_reasons_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "cancellation_reason_Type")
    private String cancellationReasonType;

    @Column(name = "case_hearing_id")
    private Long caseHearingID;

}
