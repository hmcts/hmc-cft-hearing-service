package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Table(name = "actual_hearing_audit")
@Entity
@Data
public class ActualHearingAuditEntity implements Serializable {

    private static final long serialVersionUID = -7665052824202241967L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "actual_hearing_audit_id_seq_generator")
    @SequenceGenerator(name = "actual_hearing_audit_id_seq_generator",
        sequenceName = "actual_hearing_audit_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "hearing_response_id")
    private Long hearingResponseId;

    @Column(name = "hearing_id")
    private Long hearingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_response_id", referencedColumnName = "hearing_response_id",
        insertable = false, updatable = false)
    private HearingResponseEntity hearingResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id", referencedColumnName = "hearing_id", insertable = false, updatable = false)
    private HearingEntity hearing;

    @Column(name = "audit_create_date_time")
    private LocalDateTime auditCreateDateTime;

    @Column(name = "actual_hearing_audit_record", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode actualHearingAuditRecord;

}

