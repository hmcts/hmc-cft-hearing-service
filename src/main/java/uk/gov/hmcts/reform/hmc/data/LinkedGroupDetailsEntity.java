package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "linked_group_details")
@Entity
@Data
public class LinkedGroupDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "linked_group_details_id_seq")
    @Column(name = "linked_group_id")
    private Long linkedGroupId;

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
