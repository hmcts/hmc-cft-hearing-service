package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing_response")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SecondaryTable(name = "hearing",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_id")})
public class HearingResponseEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -3354306831150920356L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_response_id_seq")
    @Column(name = "hearing_response_id")
    private Long hearingResponseId;

    @Column(name = "received_date_time", nullable = false)
    private LocalDateTime requestTimeStamp;

    @Column(name = "listing_status")
    private String listingStatus;

    @Column(name = "listing_case_status", nullable = false)
    private String listingCaseStatus;

    @Column(name = "list_assist_transaction_id", nullable = false)
    private String listAssistTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id")
    private HearingEntity hearing;

    @OneToMany(mappedBy = "hearingResponse", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonSerialize
    private List<HearingDayDetailsEntity> hearingDayDetails;

    @Column(name = "request_version", nullable = false)
    private Integer requestVersion;

    @Column(name = "parties_notified_datetime")
    private LocalDateTime partiesNotifiedDateTime;

    @Column(name = "service_data", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode serviceData;

    @OneToOne(mappedBy = "hearingResponse", fetch = FetchType.EAGER, orphanRemoval = true)
    private ActualHearingEntity actualHearingEntity;

    @Column(name = "cancellation_reason_type")
    private String cancellationReasonType;

    @Column(name = "translator_required")
    private Boolean translatorRequired;

    @Column(name = "listing_transaction_id")
    private String listingTransactionId;

    public Optional<HearingDayDetailsEntity> getEarliestHearingDayDetails() {
        return getHearingDayDetails().stream()
            .min(Comparator.comparing(HearingDayDetailsEntity::getStartDateTime));
    }

    public boolean hasHearingDayDetails() {
        return getHearingDayDetails() != null && !getHearingDayDetails().isEmpty();
    }
}
