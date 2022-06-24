package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.io.Serializable;
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

@Table(name = "hearing_day_details")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SecondaryTable(name = "HEARING_RESPONSE",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_response_id")})
public class HearingDayDetailsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -7404453999051585377L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_day_id_seq")
    @Column(name = "hearing_day_id")
    private Long hearingDayId;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "venue_id")
    private String venueId;

    @Column(name = "room_id")
    private String roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_response_id")
    private HearingResponseEntity hearingResponse;

    @OneToMany(mappedBy = "hearingDayDetails", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HearingDayPanelEntity> hearingDayPanel;

    @OneToMany(mappedBy = "hearingDayDetails", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HearingAttendeeDetailsEntity> hearingAttendeeDetails;

}
