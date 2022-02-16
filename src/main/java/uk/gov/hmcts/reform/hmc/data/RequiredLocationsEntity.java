package uk.gov.hmcts.reform.hmc.data;

import org.hibernate.annotations.Type;
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
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "required_locations")
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class RequiredLocationsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "required_locations_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "location_level_type", nullable = false)
    private String locationLevelType;

    @Column(name = "location_id", columnDefinition = "locationid", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
    @Enumerated(EnumType.STRING)
    private LocationId locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing = null;

}
