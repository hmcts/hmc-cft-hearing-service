package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import java.time.LocalDateTime;
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

@Table(name = "actual_hearing_day_pauses")
@Entity
@Data
@SecondaryTable(name = "ACTUAL_HEARING_DAY",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "actual_hearing_day_id")})
public class ActualHearingDayPausesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "pauses_id_seq")
    @Column(name = "pauses_id")
    private Long pausesId;

    @Column(name = "pause_date_time", nullable = false)
    private LocalDateTime pauseDateTime;

    @Column(name = "resume_date_time", nullable = false)
    private LocalDateTime resumeDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_hearing_day_id")
    private ActualHearingDayEntity actualHearingDay;
}
