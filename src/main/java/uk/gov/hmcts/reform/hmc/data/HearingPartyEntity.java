package uk.gov.hmcts.reform.hmc.data;

import org.hibernate.annotations.Type;
import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "hearing_party")
@Entity
@Data
public class HearingPartyEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Id
    @Column(name = "tech_party_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long techPartyId;

    @Column(name = "party_reference", nullable = false)
    private String partyReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
    private PartyType partyType;

    @Column(name = "party_role_type")
    private String partyRoleType;

    @OneToOne(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private IndividualDetailEntity individualDetailEntity;

    @OneToOne(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private OrganisationDetailEntity organisationDetailEntity;

    @OneToMany(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<UnavailabilityEntity> unavailabilityEntity;

    @OneToMany(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ContactDetailsEntity> contactDetailsEntity;

    @OneToMany(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ReasonableAdjustmentsEntity> reasonableAdjustmentsEntity;

}
