package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.LocationId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "required_locations")
@Entity
@Data
public class RequiredLocationsEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Column(name = "location_level_type", nullable = false)
    private String locationLevelType;

    @Column(name = "location_id", nullable = false)
    private LocationId locationId;

}
