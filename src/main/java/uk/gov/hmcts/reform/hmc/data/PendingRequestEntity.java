package uk.gov.hmcts.reform.hmc.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
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
    private LocalDateTime submittedDateTime;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_tried_date_time", nullable = false)
    private LocalDateTime lastTriedDateTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "incident_flag")
    private Boolean incidentFlag;

    @Column(name = "message")
    private String message;

    @Column(name = "deployment_id")
    private String deploymentId;

    public String toString() {
        return "id:<" + id + ">,"
            + "hearingId:<" + hearingId + ">,"
            + "versionNumber:<" + versionNumber + ">,"
            + "messageType:<" + messageType + ">,"
            + "submittedDateTime:<" + submittedDateTime + ">,"
            + "retryCount:<" + retryCount + ">,"
            + "lastTriedDateTime:<" + lastTriedDateTime + ">,"
            + "status:<" + status + ">,"
            + "incidentFlag:<" + incidentFlag + ">,"
            + "message:<" + message + ">,"
            + "deploymentId:<" + deploymentId + ">";
    }
}
