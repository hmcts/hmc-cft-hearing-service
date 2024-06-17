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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "organisation_detail_id_seq")
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
