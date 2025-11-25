package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Table(name = "organisation_detail")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "hearing_party",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "TECH_PARTY_ID")})
public class OrganisationDetailEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1283927209461686116L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "organisation_detail_id_seq_generator")
    @SequenceGenerator(name = "organisation_detail_id_seq_generator", 
        sequenceName = "organisation_detail_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "organisation_name", nullable = false)
    private String organisationName;

    @Column(name = "organisation_type_code", nullable = false)
    private String organisationTypeCode;

    @Column(name = "hmcts_organisation_reference")
    private String hmctsOrganisationReference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    public OrganisationDetailEntity(OrganisationDetailEntity original) {
        this.id = original.id;
        this.organisationName = original.organisationName;
        this.organisationTypeCode = original.organisationTypeCode;
        this.hmctsOrganisationReference = original.hmctsOrganisationReference;
        this.hearingParty = original.hearingParty;
    }
}
