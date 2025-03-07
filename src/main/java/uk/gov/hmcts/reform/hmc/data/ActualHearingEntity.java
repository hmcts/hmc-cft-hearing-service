package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Table(name = "actual_hearing")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ActualHearingEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -6230201524807926703L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "actual_hearing_id_seq_generator")
    @SequenceGenerator(name = "actual_hearing_id_seq_generator", 
        sequenceName = "actual_hearing_id_seq", allocationSize = 1)
    @Column(name = "actual_hearing_id")
    private Long actualHearingId;

    @Column(name = "actual_hearing_type", nullable = false)
    private String actualHearingType;

    @Column(name = "actual_hearing_is_final_flag", nullable = false)
    private Boolean actualHearingIsFinalFlag;

    @Enumerated(EnumType.STRING)
    @Column(name = "hearing_result_type", nullable = false)
    private HearingResultType hearingResultType;

    @Column(name = "hearing_result_reason_type")
    private String hearingResultReasonType;

    @Column(name = "hearing_result_date", nullable = false)
    private LocalDate hearingResultDate;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "hearing_response_id")
    private HearingResponseEntity hearingResponse;

    @OneToMany(mappedBy = "actualHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ActualHearingDayEntity> actualHearingDay;
}
