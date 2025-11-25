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

import java.io.Serializable;

@Table(name = "hearing_day_panel")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SecondaryTable(name = "HEARING_DAY_DETAILS",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_day_id")})
public class HearingDayPanelEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -5104107251904201708L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "hearing_day_panel_id_seq_generator")
    @SequenceGenerator(name = "hearing_day_panel_id_seq_generator", 
        sequenceName = "hearing_day_panel_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "panel_user_id", nullable = false)
    private String panelUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_day_id")
    private HearingDayDetailsEntity hearingDayDetails;

    @Column(name = "is_presiding")
    private Boolean isPresiding;

}
