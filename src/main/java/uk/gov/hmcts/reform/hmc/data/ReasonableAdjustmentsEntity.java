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

@Table(name = "reasonable_adjustments")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "hearing_party",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "TECH_PARTY_ID")})
public class ReasonableAdjustmentsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 6304356931641668467L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "reasonable_adjustments_id_seq_generator")
    @SequenceGenerator(name = "reasonable_adjustments_id_seq_generator", 
        sequenceName = "reasonable_adjustments_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "reasonable_adjustment_code")
    private String reasonableAdjustmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    public ReasonableAdjustmentsEntity(ReasonableAdjustmentsEntity original) {
        this.id = original.id;
        this.reasonableAdjustmentCode = original.reasonableAdjustmentCode;
        this.hearingParty = original.hearingParty;
    }
}
