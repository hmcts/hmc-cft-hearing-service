package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;

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
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Table(name = "hearing")
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class HearingEntity {

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

    /**
     * Gets the most recent hearing response associated with the latest request.
     */
    public Optional<HearingResponseEntity> getHearingResponseForLatestRequest() {
        String latestRequestVersion = getLatestRequestVersion().toString();
        return getHearingResponses() == null ? Optional.empty() : getHearingResponses().stream()
            .filter(hearingResponseEntity -> hearingResponseEntity.getRequestVersion().equals(latestRequestVersion))
            .max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));
    }

    /**
     * Gets the *latest* hearing response - note that this will not necessarily be associated with the latest request.
     */
    public Optional<HearingResponseEntity> getLatestHearingResponse() {
        return getHearingResponses() == null ? Optional.empty() : getHearingResponses().stream()
            .collect(groupingBy(HearingResponseEntity::getRequestVersion, TreeMap::new, toList()))
            .lastEntry()
            .getValue()
            .stream()
            .max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));
    }

    public Integer getNextRequestVersion() {
        return getLatestRequestVersion() + 1;
    }
}
