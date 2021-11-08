package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.RequirementType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "panel_user_requirements")
@Entity
@Data
public class PanelUserRequirementsEntity {

    @Column(name = "case_hearing_id", nullable = false)
    private Long caseHearingID;

    @Column(name = "judicial_user_id", nullable = false)
    private String judicialUserId;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "judicial_user_id", nullable = false)
    private RequirementType requirementType;

}
