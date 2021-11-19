package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.*;

@Table(name = "hearing")
@Entity
@Data
public class HearingEntity {

    @Id
    @SequenceGenerator(name="hearing_id_seq",
        sequenceName="hearing_id_seq",
        allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator="hearing_id_seq")
    @Column(name = "hearing_id")
    private Long id;

    @Column(name = "status", nullable = false)
    private String status;

    @OneToOne(mappedBy = "hearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private CaseHearingRequestEntity caseHearingRequest;

}
