package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "organisation_detail")
@Entity
@Data
public class OrganisationDetailEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    @Column(name = "organisation_name", nullable = false)
    private String organisationName;

    @Column(name = "organisation_type_code", nullable = false)
    private String organisationTypeCode;

    @Column(name = "hmcts_organisation_reference", nullable = false)
    private String hmctsOrganisationReference;

}
