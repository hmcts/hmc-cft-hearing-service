package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import uk.gov.hmcts.reform.hmc.model.RequirementType;

import java.io.Serializable;

@Table(name = "panel_user_requirements")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class PanelUserRequirementsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -4730336666389556107L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "panel_user_requirements_id_seq_generator")
    @SequenceGenerator(name = "panel_user_requirements_id_seq_generator", 
        sequenceName = "panel_user_requirements_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Column(name = "judicial_user_id", nullable = false)
    private String judicialUserId;

    @Column(name = "user_type")
    private String userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false)
    private RequirementType requirementType;

    public PanelUserRequirementsEntity(PanelUserRequirementsEntity original) {
        this.id = original.id;
        this.caseHearing = original.caseHearing;
        this.judicialUserId = original.judicialUserId;
        this.userType = original.userType;
        this.requirementType = original.requirementType;
    }
}
