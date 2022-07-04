package uk.gov.hmcts.reform.hmc.data;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "panel_authorisation_requirements_id_seq")
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
