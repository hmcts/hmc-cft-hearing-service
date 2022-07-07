package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.model.LocationType;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "required_locations_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "location_level_type", columnDefinition = "locationType", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
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
