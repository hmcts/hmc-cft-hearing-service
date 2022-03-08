package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Table(name = "linked_group_details_audit")
@Entity
@Data
public class LinkedGroupDetailsAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "linked_group_details_audit_id_seq")
    @Column(name = "linked_group_details_audit_id")
    private Long linkedGroupDetailsAuditId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_group_id")
    private LinkedGroupDetails linkedGroup;

    @Column(name = "linked_group_version")
    private Long linkedGroupVersion;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "request_name")
    private String requestName;

    @Column(name = "request_date_time", nullable = false)
    private LocalDateTime requestDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.LinkType")
    private String linkType;

    @Column(name = "reason_for_link", nullable = false)
    private String reasonForLink;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "linked_comments")
    private String linkedComments;

}

