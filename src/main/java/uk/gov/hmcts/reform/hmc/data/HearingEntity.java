package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "hearing")
@Entity
@Data
public class HearingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hearing_id")
    private Long id;

    @Column(name = "status", nullable = false)
    private String status;

    @OneToOne(mappedBy = "hearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private CaseHearingRequestEntity caseHearingRequest;

}
