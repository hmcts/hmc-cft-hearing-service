package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing_response")
@Entity
@Data
@SecondaryTable(name = "HEARING",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_id")})
public class HearingResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_response_id_seq")
    @Column(name = "hearing_response_id")
    private Long hearingResponseId;

    @Column(name = "received_date_time", nullable = false)
    private LocalDateTime requestTimeStamp;

    @Column(name = "listing_status", nullable = false)
    private String listingStatus;

    @Column(name = "listing_case_status", nullable = false)
    private String listingCaseStatus;

    @Column(name = "parties_notified_datetime")
    private LocalDateTime partiesNotifiedDateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id")
    private HearingEntity hearing;

    @OneToMany(mappedBy = "hearingResponse")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HearingDayDetailsEntity> hearingDayDetails;
}
