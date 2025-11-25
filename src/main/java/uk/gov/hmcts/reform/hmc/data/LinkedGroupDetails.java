package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Table(name = "linked_group_details")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedGroupDetails extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 9069607205338784728L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "linked_group_details_id_seq_generator")
    @SequenceGenerator(name = "linked_group_details_id_seq_generator", 
        sequenceName = "linked_group_details_id_seq", allocationSize = 1)
    @Column(name = "linked_group_id")
    private Long linkedGroupId;

    @Generated(event = EventType.INSERT)
    @Column(name = "request_id", columnDefinition = "serial", insertable = false, updatable = false)
    private String requestId;

    @Column(name = "request_name")
    private String requestName;

    @Column(name = "request_date_time", nullable = false)
    private LocalDateTime requestDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false)
    private LinkType linkType;

    @Column(name = "reason_for_link", nullable = false)
    private String reasonForLink;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "linked_comments")
    private String linkedComments;

    @Column(name = "linked_group_latest_version")
    private Long linkedGroupLatestVersion;
}
