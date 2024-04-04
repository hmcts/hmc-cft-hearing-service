package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "linked_hearing_status_audit")
@EqualsAndHashCode()
@Entity
@Data
public class LinkedHearingStatusAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "linked_hearing_status_audit_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "hmcts_service_id", nullable = false)
    private String hmctsServiceId;

    @Column(name = "linked_hearing_group_id", nullable = false)
    private String linkedHearingGroupId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "status_update_date_time", nullable = false)
    private LocalDateTime statusUpdateDateTime;

    @Column(name = "hearing_event", nullable = false)
    private String hearingEvent;

    @Column(name = "http_status")
    private String httpStatus;

    @Column(name = "source")
    private String source;

    @Column(name = "target")
    private String target;

    @Column(name = "error_description", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode errorDescription;

    @Column(name = "request_version", nullable = false)
    private String requestVersion;

    @Column(name = "response_date_time")
    private LocalDateTime responseDateTime;

}
