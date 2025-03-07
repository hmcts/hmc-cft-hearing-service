package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "actual_hearing_day")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ActualHearingDayEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 7977056936948346510L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "actual_hearing_day_id_seq_generator")
    @SequenceGenerator(name = "actual_hearing_day_id_seq_generator", 
        sequenceName = "actual_hearing_day_id_seq", allocationSize = 1)
    @Column(name = "actual_hearing_day_id")
    private Long actualHearingDayId;

    @Column(name = "hearing_date", nullable = false)
    private LocalDate hearingDate;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "not_required")
    private Boolean notRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_hearing_id")
    private ActualHearingEntity actualHearing;

    @OneToMany(mappedBy = "actualHearingDay", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ActualHearingPartyEntity> actualHearingParty;

    @OneToMany(mappedBy = "actualHearingDay", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ActualHearingDayPausesEntity> actualHearingDayPauses;
}
