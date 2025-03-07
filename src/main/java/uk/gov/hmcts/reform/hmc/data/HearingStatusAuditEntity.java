package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
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

@Table(name = "hearing_status_audit")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class HearingStatusAuditEntity extends AuditBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "hearing_status_audit_id_seq_generator")
    @SequenceGenerator(name = "hearing_status_audit_id_seq_generator", 
        sequenceName = "hearing_status_audit_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "hmcts_service_id", nullable = false)
    private String hmctsServiceId;

    @Column(name = "hearing_id", nullable = false)
    private String hearingId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "status_update_date_time", nullable = false)
    private LocalDateTime statusUpdateDateTime;

    @Column(name = "hearing_event", nullable = false)
    private String hearingEvent;

    @Column(name = "request_version", nullable = false)
    private String requestVersion;

    @Column(name = "response_date_time")
    private LocalDateTime responseDateTime;

}
