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
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Table(name = "panel_authorisation_requirements")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class PanelAuthorisationRequirementsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 7526815208919075769L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "panel_authorisation_requirements_id_seq_generator")
    @SequenceGenerator(name = "panel_authorisation_requirements_id_seq_generator", 
        sequenceName = "panel_authorisation_requirements_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Column(name = "authorisation_type")
    private String authorisationType;

    @Column(name = "authorisation_subtype")
    private String authorisationSubType;

    public PanelAuthorisationRequirementsEntity(PanelAuthorisationRequirementsEntity original) {
        this.id = original.id;
        this.caseHearing = original.caseHearing;
        this.authorisationType = original.authorisationType;
        this.authorisationSubType = original.authorisationSubType;
    }
}
