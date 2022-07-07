package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "required_facilities")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class RequiredFacilitiesEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 5520012537845131680L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "required_facilities_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "facility_type")
    private String facilityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    public RequiredFacilitiesEntity(RequiredFacilitiesEntity original) {
        this.id = original.id;
        this.facilityType = original.facilityType;
        this.caseHearing = original.caseHearing;
    }
}
