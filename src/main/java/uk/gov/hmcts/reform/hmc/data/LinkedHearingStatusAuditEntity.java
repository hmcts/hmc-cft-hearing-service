package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Table(name = "linked_hearing_status_audit")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class LinkedHearingStatusAuditEntity extends AuditBaseEntity implements Serializable {

    private static final long serialVersionUID = 8647223748985181708L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "linked_hearing_status_audit_id_seq_generator")
    @SequenceGenerator(name = "linked_hearing_status_audit_id_seq_generator", 
        sequenceName = "linked_hearing_status_audit_id_seq", allocationSize = 1)
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

    @Column(name = "linked_group_hearings", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode linkedGroupHearings;

}
