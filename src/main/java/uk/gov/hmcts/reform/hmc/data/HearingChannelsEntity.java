package uk.gov.hmcts.reform.hmc.data;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_channels_id_seq")
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
