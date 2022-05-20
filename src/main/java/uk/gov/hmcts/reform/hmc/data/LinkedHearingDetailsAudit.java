package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Table(name = "linked_hearing_details_audit")
@Entity
@Data
public class LinkedHearingDetailsAudit {

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

    @Column(name ="created_date_time")
    private LocalDateTime createdDateTime;

    @PrePersist
    public void prePersist() {
        createdDateTime = LocalDateTime.now();
    }
}
