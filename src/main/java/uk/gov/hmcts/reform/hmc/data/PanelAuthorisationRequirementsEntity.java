package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "panel_authorisation_requirements")
@Entity
@Data
public class PanelAuthorisationRequirementsEntity {

    @Column(name = "case_hearing_id", nullable = false)
    private Long caseHearingID;

    @Column(name = "authorisation_type")
    private String authorisationType;

    @Column(name = "authorisation_subtype")
    private String authorisationSubType;

}
