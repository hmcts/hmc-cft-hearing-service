package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

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
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing_day_details")
@Entity
@Data
@SecondaryTable(name = "HEARING_RESPONSE",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_response_id")})
public class HearingDayDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_day_id_seq")
    @Column(name = "hearing_day_id")
    private Long hearingDayId;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "venue_id", nullable = false)
    private String venueId;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name ="created_date_time")
    private LocalDateTime createdDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_response_id")
    private HearingResponseEntity hearingResponse;

    @OneToMany(mappedBy = "hearingDayDetails", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HearingDayPanelEntity> hearingDayPanel;

    @OneToMany(mappedBy = "hearingDayDetails", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HearingAttendeeDetailsEntity> hearingAttendeeDetails;

    @PrePersist
    public void prePersist() {
        createdDateTime = LocalDateTime.now();
    }

}
