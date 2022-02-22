package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing_response")
@Entity
@Data
@SecondaryTable(name = "hearing",
        pkJoinColumns = {
                @PrimaryKeyJoinColumn(name = "hearing_id")})
public class PartiesNotifiedEntity {

    @Id
    @Column(name = "hearing_response_id")
    private Long hearingResponseId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hearing_id")
    private HearingEntity hearing;

    @Column(name = "received_date_time", nullable = false)
    private LocalDateTime requestTimeStamp;

    @Column(name = "response_version", nullable = false)
    private String responseVersion;

    @Column(name = "request_version", nullable = false)
    private String requestVersion;

    @Column(name = "parties_notified_datetime")
    private LocalDateTime partiesNotifiedDateTime;

    @Column(name = "service_data")
    @Convert(converter = JsonDataConverter.class)
    private JsonNode serviceData;

}
