package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

@Table(name = "max_hearing_request_version")
@EqualsAndHashCode(callSuper = true)
@Entity
@Immutable
public class MaxHearingRequestVersionView extends BaseEntity {

    @Id
    @Column(name = "hearing_id")
    private Long hearingId;

    @Column(name = "max_hearing_request_version")
    private Integer maxHearingRequestVersion;

}
