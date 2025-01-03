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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;

import java.time.LocalDateTime;


@Table(name = "linked_group_details_audit")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class LinkedGroupDetailsAudit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "linked_group_details_audit_id")
    private Long linkedGroupDetailsAuditId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_group_id")
    private LinkedGroupDetails linkedGroup;

    @Column(name = "linked_group_version")
    private Long linkedGroupVersion;

    @Column(name = "linked_comments")
    private String linkedComments;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false)
    private LinkType linkType;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "request_name")
    private String requestName;

    @Column(name = "reason_for_link", nullable = false)
    private String reasonForLink;

    @Column(name = "request_date_time", nullable = false)
    private LocalDateTime requestDateTime;

    @Column(name = "status", nullable = false)
    private String status;

}

