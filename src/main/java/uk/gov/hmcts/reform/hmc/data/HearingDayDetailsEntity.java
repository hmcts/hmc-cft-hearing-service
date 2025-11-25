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
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "hearing_day_details_id_seq_generator")
    @SequenceGenerator(name = "hearing_day_details_id_seq_generator", 
        sequenceName = "hearing_day_details_id_seq", allocationSize = 1)
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

    public List<HearingDayPanelEntity> getHearingDayPanel() {
        List<HearingDayPanelEntity> mutableHearingDayPanelEntities =
            null == hearingDayPanel ? new ArrayList<>() : new ArrayList<>(hearingDayPanel);
        mutableHearingDayPanelEntities.sort(Comparator.comparing(HearingDayPanelEntity::getPanelUserId));
        return mutableHearingDayPanelEntities;
    }

}
