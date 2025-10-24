package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Table(name = "actual_hearing_day_pauses")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ActualHearingDayPausesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "pauses_id_seq_generator")
    @SequenceGenerator(name = "pauses_id_seq_generator", 
        sequenceName = "pauses_id_seq", allocationSize = 1)
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
