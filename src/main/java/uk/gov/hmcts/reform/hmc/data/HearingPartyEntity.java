package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "hearing_party")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class HearingPartyEntity extends BaseEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = -1378995263864233869L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "tech_party_id_seq_generator")
    @SequenceGenerator(name = "tech_party_id_seq_generator", 
        sequenceName = "tech_party_id_seq", allocationSize = 1)
    @Column(name = "tech_party_id", nullable = false)
    private Long techPartyId;

    @Column(name = "party_reference", nullable = false)
    private String partyReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
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

    @OneToMany(mappedBy = "sourceTechParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PartyRelationshipDetailsEntity> partyRelationshipDetailsEntity;

    public HearingPartyEntity(HearingPartyEntity original) {
        this.caseHearing = original.caseHearing;
        this.techPartyId = original.techPartyId;
        this.partyReference = original.partyReference;
        this.partyType = original.partyType;
        this.partyRoleType = original.partyRoleType;
        this.individualDetailEntity =  original.individualDetailEntity;
        this.organisationDetailEntity =  original.organisationDetailEntity;
        this.unavailabilityEntity =  original.unavailabilityEntity;
        this.contactDetailsEntity =  original.contactDetailsEntity;
        this.reasonableAdjustmentsEntity =  original.reasonableAdjustmentsEntity;
        this.partyRelationshipDetailsEntity =  original.partyRelationshipDetailsEntity;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        HearingPartyEntity cloned = (HearingPartyEntity) super.clone();
        cloneUnavailability(cloned);
        cloneContactDetails(cloned);
        cloneReasonableAdjustments(cloned);
        cloneIndividualDetails(cloned);
        cloneOrganisationDetails(cloned);
        clonePartyRelationshipDetails(cloned);
        cloned.setTechPartyId(null);
        return cloned;
    }

    private void cloneIndividualDetails(HearingPartyEntity cloned) {
        //IndividualDetailEntity
        if (null != cloned.getIndividualDetailEntity()) {
            IndividualDetailEntity clonedSubValue = new IndividualDetailEntity(cloned.getIndividualDetailEntity());
            clonedSubValue.setId(null);
            clonedSubValue.setHearingParty(cloned);
            cloned.setIndividualDetailEntity(clonedSubValue);
        }
    }

    private void cloneOrganisationDetails(HearingPartyEntity cloned) {
        //OrganisationDetailEntity
        if (null != cloned.getOrganisationDetailEntity()) {
            OrganisationDetailEntity clonedSubValue =
                 new OrganisationDetailEntity(cloned.getOrganisationDetailEntity());
            clonedSubValue.setId(null);
            clonedSubValue.setHearingParty(cloned);
            cloned.setOrganisationDetailEntity(clonedSubValue);
        }
    }

    private void cloneUnavailability(HearingPartyEntity cloned) {
        //UnavailabilityEntity
        List<UnavailabilityEntity> unavailabilityEntityList = new ArrayList<>();
        if (null != cloned.getUnavailabilityEntity()) {
            for (UnavailabilityEntity ue : cloned.getUnavailabilityEntity()) {
                UnavailabilityEntity clonedSubValue = new UnavailabilityEntity(ue);
                clonedSubValue.setId(null);
                clonedSubValue.setHearingParty(cloned);
                unavailabilityEntityList.add(clonedSubValue);
            }
        }
        cloned.setUnavailabilityEntity(unavailabilityEntityList);
    }

    private void cloneContactDetails(HearingPartyEntity cloned) {
        //ContactDetailsEntity
        List<ContactDetailsEntity> contactDetailsEntityList = new ArrayList<>();
        if (null != cloned.getContactDetailsEntity()) {
            for (ContactDetailsEntity cde : cloned.getContactDetailsEntity()) {
                ContactDetailsEntity clonedSubValue =  new ContactDetailsEntity(cde);
                clonedSubValue.setId(null);
                clonedSubValue.setHearingParty(cloned);
                contactDetailsEntityList.add(clonedSubValue);
            }
        }
        cloned.setContactDetailsEntity(contactDetailsEntityList);
    }

    private void cloneReasonableAdjustments(HearingPartyEntity cloned) {
        //ReasonableAdjustmentsEntity
        List<ReasonableAdjustmentsEntity> reasonableAdjustmentsEntityList = new ArrayList<>();
        if (null != cloned.getReasonableAdjustmentsEntity()) {
            for (ReasonableAdjustmentsEntity rae : cloned.getReasonableAdjustmentsEntity()) {
                ReasonableAdjustmentsEntity clonedSubValue = new ReasonableAdjustmentsEntity(rae);
                clonedSubValue.setId(null);
                clonedSubValue.setHearingParty(cloned);
                reasonableAdjustmentsEntityList.add(clonedSubValue);
            }
        }
        cloned.setReasonableAdjustmentsEntity(reasonableAdjustmentsEntityList);
    }

    private void clonePartyRelationshipDetails(HearingPartyEntity cloned) {
        //PartyRelationshipDetailsEntity
        List<PartyRelationshipDetailsEntity> partyRelationshipDetailsEntityList = new ArrayList<>();
        if (null != cloned.getPartyRelationshipDetailsEntity()) {
            for (PartyRelationshipDetailsEntity prde : cloned.getPartyRelationshipDetailsEntity()) {
                PartyRelationshipDetailsEntity clonedSubValue = new PartyRelationshipDetailsEntity(prde);
                clonedSubValue.setPartyRelationshipDetailsId(null);
                clonedSubValue.setSourceTechParty(cloned);
                partyRelationshipDetailsEntityList.add(clonedSubValue);
            }
        }
        cloned.setPartyRelationshipDetailsEntity(partyRelationshipDetailsEntityList);
    }
}
