package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "panel_requirements")
@Entity
@Data
public class PanelRequirementsEntity {

    @Column(name = "case_hearing_id", nullable = false)
    private Long caseHearingID;

    @Column(name = "role_type")
    private String roleType;
}
