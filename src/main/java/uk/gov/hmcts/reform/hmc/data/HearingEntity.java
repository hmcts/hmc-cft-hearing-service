package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing")
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class HearingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_id_seq")
    @Column(name = "hearing_id")
    private Long id;

    @Column(name = "status", nullable = false)
    private String status;

    @ToString.Exclude
    @OneToOne(mappedBy = "hearing", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private CaseHearingRequestEntity caseHearingRequest;

    @OneToMany(mappedBy = "hearing", fetch = FetchType.EAGER)
    private List<HearingResponseEntity> hearingResponses;
}
