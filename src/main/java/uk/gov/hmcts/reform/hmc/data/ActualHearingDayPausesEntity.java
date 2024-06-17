package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name = "actual_hearing_day_pauses")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ActualHearingDayPausesEntity extends BaseEntity {

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
