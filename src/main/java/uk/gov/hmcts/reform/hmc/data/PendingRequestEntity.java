package uk.gov.hmcts.reform.hmc.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "pending_requests")
@Entity
@Setter
@Getter
@NoArgsConstructor
public class PendingRequestEntity implements Serializable {

    private static final long serialVersionUID = -5832580267716907071L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "pending_requests_id_seq")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "hearing_id")
    private Long hearingId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "submitted_date_time", nullable = false)
    private Timestamp submittedDateTime;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_tried_date_time", nullable = false)
    private Timestamp lastTriedDateTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "incident_flag")
    private Boolean incidentFlag;

    @Column(name = "message")
    private String message;

    @Column(name = "deployment_id")
    private String deploymentId;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id:<").append(id).append(">,")
            .append("hearingId:<").append(hearingId).append(">,")
            .append("versionNumber:<").append(versionNumber).append(">,")
            .append("messageType:<").append(messageType).append(">,")
            .append("submittedDateTime:<").append(submittedDateTime).append(">,")
            .append("retryCount:<").append(retryCount).append(">,")
            .append("lastTriedDateTime:<").append(lastTriedDateTime).append(">,")
            .append("status:<").append(status).append(">,")
            .append("incidentFlag:<").append(incidentFlag).append(">,")
            .append("message:<").append(message).append(">,")
            .append("deploymentId:<").append(deploymentId).append(">");
        return sb.toString();
    }
}
