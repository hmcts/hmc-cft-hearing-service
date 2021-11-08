package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.LocationId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "required_locations")
@Entity
@Data
public class RequiredLocationsEntity {

    @Column(name = "case_hearing_id", nullable = false)
    private Long caseHearingID;

    @Column(name = "location_level_type", nullable = false)
    private String locationLevelType;

    @Column(name = "location_id", nullable = false)
    private LocationId locationId;

}
