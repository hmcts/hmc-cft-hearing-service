package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.io.Serializable;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "required_facilities_id_seq_generator")
    @SequenceGenerator(name = "required_facilities_id_seq_generator", 
        sequenceName = "required_facilities_id_seq", allocationSize = 1)
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
