package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.LocationId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "required_locations")
@Entity
@Data
public class RequiredLocationsEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_level_type", nullable = false)
    private String locationLevelType;

    @Column(name = "location_id", columnDefinition = "locationid", nullable = false)
    @Enumerated(EnumType.STRING)
    private LocationId locationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

}
