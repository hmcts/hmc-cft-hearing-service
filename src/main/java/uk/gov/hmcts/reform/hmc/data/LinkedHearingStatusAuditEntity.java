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

    @Column(name = "linked_group_id", nullable = false)
    private String linkedGroupId;

    @Column(name = "linked_group_version", nullable = false)
    private String linkedGroupVersion;

    @Column(name = "linked_hearing_event_date_time", nullable = false)
    private LocalDateTime linkedHearingEventDateTime;

    @Column(name = "linked_hearing_event", nullable = false)
    private String linkedHearingEvent;

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

    @Column(name = "other_info", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode otherInfo = null;

    @Column(name = "linked_group_hearings", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode linkedGroupHearings;

}
