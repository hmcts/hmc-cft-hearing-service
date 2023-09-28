package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
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
import javax.persistence.PreUpdate;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Table(name = "hearing")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class HearingEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 5837513924648640249L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_id_seq")
    @Column(name = "hearing_id")
    private Long id;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_code")
    private Integer errorCode;

    @Column(name = "error_description")
    private String errorDescription;

    @Column(name = "updated_date_time")
    private LocalDateTime updatedDateTime;

    @OneToMany(mappedBy = "hearing", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<CaseHearingRequestEntity> caseHearingRequests = new ArrayList<>();

    @OneToMany(mappedBy = "hearing", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<HearingResponseEntity> hearingResponses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_group_id")
    private LinkedGroupDetails linkedGroupDetails;

    @Column(name = "linked_order")
    private Long linkedOrder;

    @Column(name = "is_linked_flag")
    private Boolean isLinkedFlag;

    @Column(name = "deployment_id")
    private String deploymentId;


    @PreUpdate
    public void preUpdate() {
        updatedDateTime = LocalDateTime.now();
    }

    public CaseHearingRequestEntity getLatestCaseHearingRequest() {
        return getCaseHearingRequests().stream()
            .max(Comparator.comparingInt(CaseHearingRequestEntity::getVersionNumber))
            .orElseThrow(() -> new ResourceNotFoundException("Cannot find latest case "
                + "hearing request for hearing " + id));
    }

    public CaseHearingRequestEntity getCaseHearingRequest(int version) {
        return getCaseHearingRequests().stream()
            .filter(caseHearingRequestEntity -> version == caseHearingRequestEntity.getVersionNumber())
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cannot find request version " + version
                                                                 + " for hearing " + id));
    }

    public Integer getLatestRequestVersion() {
        return getLatestCaseHearingRequest().getVersionNumber();
    }

    public String getLatestCaseReferenceNumber() {
        return getLatestCaseHearingRequest().getCaseReference();
    }

    /**
     * Gets the most recent hearing response associated with the latest request while updating Hearing request.
     */
    public Optional<HearingResponseEntity> getHearingResponseForLatestRequestForUpdate() {
        Optional<HearingResponseEntity> hearingResponse = getLatestHearingResponse();
        if (hearingResponse.isPresent()) {
            Integer latestRequestVersion = getLatestHearingResponse().get().getRequestVersion();
            return getHearingResponses().stream()
                .filter(hearingResponseEntity -> hearingResponseEntity.getRequestVersion().equals(latestRequestVersion))
                .max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Gets the most recent hearing response associated with the latest request.
     */
    public Optional<HearingResponseEntity> getHearingResponseForLatestRequest() {
        Integer latestRequestVersion = getLatestRequestVersion();
        return hasHearingResponses() ? getHearingResponses().stream()
                .filter(hearingResponseEntity -> hearingResponseEntity.getRequestVersion().equals(latestRequestVersion))
                .max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp))
                :  Optional.empty();
    }

    /**
     * Gets the *latest* hearing response - note that this will not necessarily be associated with the latest request.
     */
    public Optional<HearingResponseEntity> getLatestHearingResponse() {
        return hasHearingResponses() ? getHearingResponses().stream()
            .collect(groupingBy(HearingResponseEntity::getRequestVersion, TreeMap::new, toList()))
            .lastEntry()
            .getValue()
            .stream()
            .max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp))
            : Optional.empty();
    }

    public String getDerivedHearingStatus() {
        String hearingStatus = "";
        switch (this.status) {
            case "LISTED":
            case "UPDATE_REQUESTED":
            case "UPDATE_SUBMITTED":
                hearingStatus = this.status;
                Optional<HearingResponseEntity> hearingResponse = getLatestHearingResponse();
                if (hearingResponse.isPresent()) {
                    HearingResponseEntity latestHearingResponse = hearingResponse.get();
                    Optional<HearingDayDetailsEntity> hearingDayDetails =
                        latestHearingResponse.getEarliestHearingDayDetails();
                    if (latestHearingResponse.hasHearingDayDetails() && hearingDayDetails.isPresent()) {
                        HearingDayDetailsEntity hearingDayDetailsEntity = hearingDayDetails.get();
                        if (hearingDayDetailsEntity.getStartDateTime() != null
                            && !LocalDate.now().isBefore(hearingDayDetailsEntity.getStartDateTime().toLocalDate())) {
                            return "AWAITING_ACTUALS";
                        }
                    }
                }
                break;
            default:
                hearingStatus = this.status;
        }
        return hearingStatus;
    }

    public Integer getNextRequestVersion() {
        return getLatestRequestVersion() + 1;
    }

    public boolean hasHearingResponses() {
        return getHearingResponses() != null && !getHearingResponses().isEmpty();
    }
}
