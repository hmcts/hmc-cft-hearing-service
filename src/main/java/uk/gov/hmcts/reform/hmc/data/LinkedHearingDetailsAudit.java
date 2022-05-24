package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "linked_hearing_details_audit")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class LinkedHearingDetailsAudit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "linked_hearing_details_audit_id_seq")
    @Column(name = "linked_hearing_details_audit_id")
    private Long linkedHearingDetailsAuditId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_group_id")
    private LinkedGroupDetails linkedGroup;

    @Column(name = "linked_group_version", nullable = false)
    private Long linkedGroupVersion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id")
    private HearingEntity hearing;

    @Column(name = "linked_order")
    private Long linkedOrder;
}
