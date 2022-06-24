package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "actual_hearing")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ActualHearingEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -6230201524807926703L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "actual_hearing_id_seq")
    @Column(name = "actual_hearing_id")
    private Long actualHearingId;

    @Column(name = "actual_hearing_type", nullable = false)
    private String actualHearingType;

    @Column(name = "actual_hearing_is_final_flag", nullable = false)
    private Boolean actualHearingIsFinalFlag;

    @Enumerated(EnumType.STRING)
    @Column(name = "hearing_result_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
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
