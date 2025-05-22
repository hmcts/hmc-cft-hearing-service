package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;

import java.io.Serializable;
import java.time.LocalDate;

@Table(name = "unavailability")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "hearing_party",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "TECH_PARTY_ID")})
public class UnavailabilityEntity extends BaseEntity  implements Serializable {

    private static final long serialVersionUID = 5423332025288476165L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "unavailability_id_seq_generator")
    @SequenceGenerator(name = "unavailability_id_seq_generator", 
        sequenceName = "unavailability_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week_unavailable")
    private DayOfWeekUnavailable dayOfWeekUnavailable;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week_unavailable_type")
    private DayOfWeekUnAvailableType dayOfWeekUnavailableType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "unavailability_type")
    private String unAvailabilityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    public UnavailabilityEntity(UnavailabilityEntity original) {
        this.id = original.id;
        this.dayOfWeekUnavailable = original.dayOfWeekUnavailable;
        this.dayOfWeekUnavailableType = original.dayOfWeekUnavailableType;
        this.startDate = original.startDate;
        this.endDate = original.endDate;
        this.unAvailabilityType = original.unAvailabilityType;
        this.hearingParty = original.hearingParty;
    }
}
