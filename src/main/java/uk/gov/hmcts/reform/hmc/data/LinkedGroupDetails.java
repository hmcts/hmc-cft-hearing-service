package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedGroupDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "linked_group_details_id_seq")
    @Column(name = "linked_group_id")
    private Long linkedGroupId;

    @Generated(GenerationTime.INSERT)
    @Column(name = "request_id", columnDefinition = "serial", insertable = false, updatable = false)
    private String requestId;

    @Column(name = "request_name")
    private String requestName;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "request_date_time", nullable = false)
    private LocalDateTime requestDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
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
