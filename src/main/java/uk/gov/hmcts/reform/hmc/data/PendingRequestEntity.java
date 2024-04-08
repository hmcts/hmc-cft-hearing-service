package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Table(name = "pending_requests")
@Entity
@Data
@NoArgsConstructor
public class PendingRequestEntity implements Serializable {

    private static final long serialVersionUID = -5832580267716907071L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hearing_id")
    private Long hearingId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

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
    private String extraData;
}
