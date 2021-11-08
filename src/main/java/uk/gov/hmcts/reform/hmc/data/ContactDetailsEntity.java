package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "contact_details")
@Entity
@Data
public class ContactDetailsEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    @Column(name = "contact_type")
    private String contactType;

    @Column(name = "contact_details")
    private String contactDetails;

}
