package uk.gov.hmcts.reform.hmc.data;

import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
