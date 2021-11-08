package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.RequirementType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "panel_user_requirements")
@Entity
@Data
public class PanelUserRequirementsEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Column(name = "judicial_user_id", nullable = false)
    private String judicialUserId;

    @Column(name = "user_type")
    private String userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "judicial_user_id", nullable = false)
    private RequirementType requirementType;

}
