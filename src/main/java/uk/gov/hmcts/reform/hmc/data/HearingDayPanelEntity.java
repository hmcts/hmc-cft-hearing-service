package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

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

@Table(name = "hearing_day_panel")
@Entity
@Data
@SecondaryTable(name = "HEARING_DAY_DETAILS",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_day_id")})
public class HearingDayPanelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_day_panel_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "panel_user_id", nullable = false)
    private String panelUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_day_id")
    private HearingDayDetailsEntity hearingDayDetails;

    @Column(name = "is_presiding")
    private boolean isPresiding;

}
