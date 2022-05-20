package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.model.RequirementType;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "panel_user_requirements")
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class PanelUserRequirementsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "panel_user_requirements_id_seq")
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Column(name = "judicial_user_id", nullable = false)
    private String judicialUserId;

    @Column(name = "user_type")
    private String userType;

    @Column(name ="created_date_time")
    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
    private RequirementType requirementType;

    @PrePersist
    public void prePersist() {
        createdDateTime = LocalDateTime.now();
    }

}
