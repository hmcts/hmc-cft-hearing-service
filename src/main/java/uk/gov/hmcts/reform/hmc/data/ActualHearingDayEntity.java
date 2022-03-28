package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "actual_hearing_day")
@Entity
@Data
@SecondaryTable(name = "ACTUAL_HEARING",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "actual_hearing_id")})
public class ActualHearingDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "actual_hearing_day_id_seq")
    @Column(name = "actual_hearing_day_id")
    private Long actualHearingDayId;

    @Column(name = "hearing_date", nullable = false)
    private LocalDate hearingDate;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

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
