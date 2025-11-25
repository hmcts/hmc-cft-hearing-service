package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Table(name = "hearing_channels")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class HearingChannelsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -6716655160254569132L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "hearing_channels_id_seq_generator")
    @SequenceGenerator(name = "hearing_channels_id_seq_generator", 
        sequenceName = "hearing_channels_id_seq", allocationSize = 1)
    @Column(name = "hearing_channels_id")
    private Long hearingChannelsId;

    @Column(name = "hearing_channel_type", nullable = false)
    private String hearingChannelType;

    @ManyToOne
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    public HearingChannelsEntity(HearingChannelsEntity original) {
        this.hearingChannelsId = original.hearingChannelsId;
        this.hearingChannelType = original.hearingChannelType;
        this.caseHearing = original.caseHearing;
    }
}
