package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.model.LocationType;

import java.io.Serializable;

@Table(name = "required_locations")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class RequiredLocationsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1119281173095751231L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "required_locations_id_seq_generator")
    @SequenceGenerator(name = "required_locations_id_seq_generator", 
        sequenceName = "required_locations_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "location_level_type", columnDefinition = "locationType", nullable = false)
    @Enumerated(EnumType.STRING)
    private LocationType locationLevelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    public RequiredLocationsEntity(RequiredLocationsEntity original) {
        this.id = original.id;
        this.locationId = original.locationId;
        this.locationLevelType = original.locationLevelType;
        this.caseHearing = original.caseHearing;
    }
}
